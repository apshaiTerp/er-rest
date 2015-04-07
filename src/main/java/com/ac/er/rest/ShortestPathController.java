package com.ac.er.rest;

import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.ac.er.data.Hospital;
import com.ac.er.data.HospitalRouteData;
import com.ac.er.message.SimpleErrorMessage;
import com.ac.er.mongo.HospitalConverter;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * @author ac010168
 *
 */
@RestController
@RequestMapping("/shortestpath")
public class ShortestPathController {
  
  public final static DecimalFormat formatter = new DecimalFormat("##.########");
  
  //This is the 
  public static String URL_ROOT = "https://maps.googleapis.com/maps/api/distancematrix/json?units=imperial&origins=<originLat>,<originLon>&destinations=<destLat>,<destLon>&clientID=662159346848-deoqbkle9scov01ehtobm9lealqglt5a.apps.googleusercontent.com";
  
  /**
   * 
   * @param ambLat    <latitude coordinate of ambulance>
   * @param ambLon    <longitude coordinate of ambulance>
   * @param age       Should be one of {child|teen|adult}
   * @param condition Should be one of {severe|minor|basicER|burn|stemi|stroke}
   * @param exclude   A List, potentially comma separated, of hospitals to exclude from the algorithm
   * 
   * @return A List of 3 Hospitals that should be reflect our top three (or potentially fewer) destinations.
   */
  @RequestMapping(method = RequestMethod.GET, produces="application/json;charset=UTF-8")
  public Object getShortestPath(@RequestParam(value="amblat", defaultValue="0.0") double ambLat,
                                @RequestParam(value="amblon") double ambLon,
                                @RequestParam(value="age") String age,
                                @RequestParam(value="condition") String condition,
                                @RequestParam(value="exclude", defaultValue="") String exclude) {
    
    if (ambLat == 0.0)
      return new SimpleErrorMessage("Invalid Parameters", "No Ambulance Latitude Value was provided");
    if (ambLon == 0.0)
      return new SimpleErrorMessage("Invalid Parameters", "No Ambulance Longitude Value was provided");
    if (age == null)
      return new SimpleErrorMessage("Invalid Parameters", "No Age Value was provided");
    if (!age.equalsIgnoreCase("child") && !age.equalsIgnoreCase("teen") && !age.equalsIgnoreCase("adult"))
      return new SimpleErrorMessage("Invalid Parameters", "The Age Value was not in the set of {child|teen|adult}");
    if (condition == null)
      return new SimpleErrorMessage("Invalid Parameters", "No Condition Value was provided");
    if (!condition.equalsIgnoreCase("severe") && !condition.equalsIgnoreCase("minor") && 
        !condition.equalsIgnoreCase("stroke") && !condition.equalsIgnoreCase("basicER") &&
        !condition.equalsIgnoreCase("burn") && !condition.equalsIgnoreCase("stemi"))
      return new SimpleErrorMessage("Invalid Parameters", "The Condition Value was not in the set of {severe|minor|basicER|burn|stemi|stroke}");
    List<Long> excludeIDs = null;
    try {
      if (exclude.trim().length() == 0) excludeIDs = new ArrayList<Long>(0);
      else                              excludeIDs = parseExcludeIDs(exclude);
    } catch (Throwable t) {
      return new SimpleErrorMessage("Invalid Parameters", "Unable to Parse ID values from exclude list");
    }
    
    //New - This is where we are going to convert the condition value into the 'true' condition
    //value that will map to the correct hospital
    String trueCondition = null;
    String altCondition  = null;
    
    //If it's a child, we want to convert the true term to the Pediatric term and set the true term as the alternate
    if (!age.equalsIgnoreCase("adult")) {
      if (condition.equalsIgnoreCase("basicER")) {
        trueCondition = "basicERPed";
        altCondition  = "basicER";
      } else if (condition.equalsIgnoreCase("burn")) {
        trueCondition = "burnPed";
        altCondition  = "trauma2";
      } else if (condition.equalsIgnoreCase("severe")) {
        trueCondition = "traumaPed";
        altCondition  = "trauma2";
      } else if (condition.equalsIgnoreCase("minor")) {
        trueCondition = "traumaPed";
        altCondition  = "trauma3";
      } else {
        //If we got here, we somehow have a child manifesting with a stroke or heart attack....
        trueCondition = "traumaPed";
        altCondition  = "trauma2";
      }
    } else {
      //These are only the adult conditions
      if (condition.equalsIgnoreCase("basicER")) {
        trueCondition = "basicER";
        //No need for an alternate here
        altCondition  = null;
      } else if (condition.equalsIgnoreCase("burn")) {
        trueCondition = "burn";
        altCondition  = "trauma2";
      } else if (condition.equalsIgnoreCase("stemi")) {
        trueCondition = "STEMI";
        //No need for an alternate here, this patient needs to go to a STEMI center
        altCondition  = null;
      } else if (condition.equalsIgnoreCase("stroke")) {
        trueCondition = "stroke";
        //No need for an alternate here, this patient needs to go to a Stroke Center
        altCondition   = null;
      } else if (condition.equalsIgnoreCase("severe")) {
        trueCondition = "trauma2";
        altCondition  = "trauma3";
      } else if (condition.equalsIgnoreCase("minor")) {
        trueCondition = "trauma3";
        altCondition  = null;
      } 
    }
    if (trueCondition == null)
      return new SimpleErrorMessage("Invalid Parameters", "I'm not sure what to make of condition (" + condition + "), but it wasn't right");
    
    //DEBUG Output
    System.out.println ("Processing trueCondition: " + trueCondition);
    System.out.println ("Considering altCondition: " + altCondition);
    
    //At this point, we have validated that all our expected fields were provided.  Now we need to open our database connection
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection hospitalCollection = mongoDB.getCollection("hospital");
    
    //Now we need to construct the correct the correct query to get what we want
    //db.hospital.find( { "levelOfCare": { $in: ["level1", "level2"] }})
    BasicDBObject queryObject  = new BasicDBObject("levelOfCare", trueCondition);
    if (trueCondition.startsWith("trauma")) {
      queryObject.append("traumaDivert", "open");
      queryObject.append("traumaBedsFree", new BasicDBObject("$gt", 0));
    } else if (trueCondition.startsWith("basicER")) {
      queryObject.append("erDivert", "open");
      queryObject.append("erBedsFree", new BasicDBObject("$gt", 0));
    } else if (trueCondition.startsWith("burn")) {
      queryObject.append("burnDivert", "open");
      queryObject.append("traumaBedsFree", new BasicDBObject("$gt", 0));
    } else if (trueCondition.equalsIgnoreCase("stemi")) {
      queryObject.append("stemiDivert", "open");
      queryObject.append("erBedsFree", new BasicDBObject("$gt", 0));
    } else if (trueCondition.equalsIgnoreCase("stroke")) {
      queryObject.append("strokeDivert", "open");
      queryObject.append("erBedsFree", new BasicDBObject("$gt", 0));
    }
    
    List<Hospital> qualifyingHospitals = new LinkedList<Hospital>();
    DBCursor cursor = hospitalCollection.find(queryObject);
    while (cursor.hasNext()) {
      DBObject dbObject = cursor.next();
      Hospital hospital = HospitalConverter.convertMongoToHospital(dbObject);
      
      //Make sure the ID is not in our exclude list
      if (!excludeIDs.contains(hospital.getHospitalID()))
        qualifyingHospitals.add(hospital);
    }
    try { cursor.close(); } catch (Throwable t) { /** Ignore Errors */ }
    
    //DEBUG - Remove before completing
    System.out.println ("There are " + qualifyingHospitals.size() + " candidate hospitals:");
    for (Hospital hospital : qualifyingHospitals) {
      System.out.println (hospital.getHospitalName() + " (hospitalID:" + hospital.getHospitalID() + ")");
    }
    
    List<Hospital> alternateHospitals = new LinkedList<Hospital>();
    if (altCondition != null) { 
      BasicDBObject queryObject2 = new BasicDBObject("levelOfCare", altCondition);
      if (altCondition.startsWith("trauma")) {
        queryObject2.append("traumaDivert", "open");
        queryObject2.append("traumaBedsFree", new BasicDBObject("$gt", 0));
      } else if (altCondition.startsWith("basicER")) {
        queryObject2.append("erDivert", "open");
        queryObject2.append("erBedsFree", new BasicDBObject("$gt", 0));
      }
      
      DBCursor cursor2 = hospitalCollection.find(queryObject2);
      while (cursor2.hasNext()) {
        DBObject dbObject = cursor2.next();
        Hospital hospital = HospitalConverter.convertMongoToHospital(dbObject);
        
        //Make sure the ID is not in our exclude list
        if (!excludeIDs.contains(hospital.getHospitalID())) {
          //Check to make sure it's also not in our primary list...
          boolean foundIt = false;
          for (Hospital qualifyHospital : qualifyingHospitals) {
            if (qualifyHospital.getHospitalID() == hospital.getHospitalID()) {
              foundIt = true;
              break;
            }
          }
          if (!foundIt)
            alternateHospitals.add(hospital);
        }
      }
      try { cursor2.close(); } catch (Throwable t) { /** Ignore Errors */ }
      
      //DEBUG - Remove before completing
      System.out.println ("There are " + alternateHospitals.size() + " candidate alternate hospitals:");
      for (Hospital hospital : alternateHospitals) {
        System.out.println (hospital.getHospitalName() + " (hospitalID:" + hospital.getHospitalID() + ")");
      }
    }
    
    try { client.close(); } catch (Throwable t) { /** Ignore Errors */ }
    
    //Before we move on, I want to go ahead and convert into the return type objects to help clean things up
    List<HospitalRouteData> qualifyingRouteHospitals = new ArrayList<HospitalRouteData>(qualifyingHospitals.size());
    List<HospitalRouteData> alternateRouteHospitals  = new ArrayList<HospitalRouteData>(alternateHospitals.size());
    
    //Now let's copy everything over into the new lists
    for (Hospital hospital : qualifyingHospitals) {
      HospitalRouteData route = new HospitalRouteData();
      route.setHospitalID(hospital.getHospitalID());
      route.setHospitalLat(hospital.getHospitalLat());
      route.setHospitalLon(hospital.getHospitalLon());
      route.setHospitalString(hospital.getAddress());
      
      //If the true Condition is a specialty, we want to indicate that in the name
      if (!trueCondition.equalsIgnoreCase("basicER")) {
        if (trueCondition.equalsIgnoreCase("basicERPed")) {
          route.setHospitalName(hospital.getHospitalName() + " (Pediatric Emergency Care)");
        } else if (trueCondition.equalsIgnoreCase("traumaPed")) {
          route.setHospitalName(hospital.getHospitalName() + " (Pediatric Trauma Center)");
        } else if (trueCondition.equalsIgnoreCase("trauma1")) {
          route.setHospitalName(hospital.getHospitalName() + getTraumaText(hospital));
        } else if (trueCondition.equalsIgnoreCase("trauma2")) {
          route.setHospitalName(hospital.getHospitalName() + getTraumaText(hospital));
        } else if (trueCondition.equalsIgnoreCase("trauma3")) {
          route.setHospitalName(hospital.getHospitalName() + getTraumaText(hospital));
        } else if (trueCondition.equalsIgnoreCase("STEMI")) {
          route.setHospitalName(hospital.getHospitalName() + " (STEMI Receiving Center)");
        } else if (trueCondition.equalsIgnoreCase("stroke")) {
          route.setHospitalName(hospital.getHospitalName() + " (Primary Stroke Center)");
        } else if (trueCondition.equalsIgnoreCase("burn")) {
          route.setHospitalName(hospital.getHospitalName() + " (Burn Unit)");
        } else if (trueCondition.equalsIgnoreCase("burnPed")) {
          route.setHospitalName(hospital.getHospitalName() + " (Pediatric Burn Unit)");
        } else {
          //This shouldn't happen, but just in case
          route.setHospitalName(hospital.getHospitalName());
        }
      } else {
        route.setHospitalName(hospital.getHospitalName());
      }
      qualifyingRouteHospitals.add(route);
    }
    
    //This also applies to the alternateLists
    for (Hospital hospital : alternateHospitals) {
      HospitalRouteData route = new HospitalRouteData();
      route.setHospitalID(hospital.getHospitalID());
      route.setHospitalLat(hospital.getHospitalLat());
      route.setHospitalLon(hospital.getHospitalLon());
      route.setHospitalString(hospital.getAddress());

      if (altCondition == null) route.setHospitalName(hospital.getHospitalName());
      else if (!altCondition.equalsIgnoreCase("basicER")) {
        if (altCondition.equalsIgnoreCase("trauma1")) {
          route.setHospitalName(hospital.getHospitalName() + getTraumaText(hospital));
        } else if (altCondition.equalsIgnoreCase("trauma2")) {
          route.setHospitalName(hospital.getHospitalName() + getTraumaText(hospital));
        } else if (altCondition.equalsIgnoreCase("trauma3")) {
          route.setHospitalName(hospital.getHospitalName() + getTraumaText(hospital));
        }
      } else route.setHospitalName(hospital.getHospitalName());
      alternateRouteHospitals.add(route);
    }
    
    /*******************************************************************************************************
    //NEW! - You're going to do the sort twice, possibly.  You start with the qualifyingRouteHospitals List.
    //Run it through the sorting algorithm.  If the size is greater than three, remove any elements beyond
    //the third and return that list.
    
    //IFF that list size is less than three, run the sorting algorithm on the alternateRouteHospitals List.
    //Add as many elements as needed to fill out the qualifyingRouteHospitals list until it gets to three,
    //then return it.

    This is where we will make our External calls to figure out what needs to happen.
    Spring provides some basic classes for making these calls.
    *******************************************************************************************************/
    
   // The actual code here will be something like this:
    Map<Integer, HospitalRouteData> sortMap = new HashMap<Integer, HospitalRouteData>();
    for (HospitalRouteData hospital : qualifyingRouteHospitals) {
      String dynamicURL = URL_ROOT.replace("<originLat>", formatter.format(ambLat));
      dynamicURL = dynamicURL.replace("<originLon>", formatter.format(ambLon));
      dynamicURL = dynamicURL.replace("<destLat>", formatter.format(hospital.getHospitalLat()));
      dynamicURL = dynamicURL.replace("<destLon>", formatter.format(hospital.getHospitalLon()));
      
      //DEBUG
      System.out.println ("dynamicURL:" + dynamicURL);
      
      RestTemplate restTemplate = new RestTemplate();
      //MyCustomObject models the return fields from the JSON API call.
      String jsonResults = restTemplate.getForObject(dynamicURL, String.class);
      JSONObject jsonObject = new JSONObject(jsonResults);
      JSONObject distanceObject = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance");
      JSONObject durationObject = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration");
      
      hospital.setDistance(distanceObject.getString("text"));
      sortMap.put(new Integer(durationObject.getInt("value")), hospital);
    }
    
    List<Integer> sortKeySet = new ArrayList<Integer>(sortMap.keySet());
    Collections.sort(sortKeySet);
    
    List<HospitalRouteData> finalDataSet = new ArrayList<HospitalRouteData>();
    for (int i = 0; (i < 3) && (i < sortKeySet.size()); i++) {
      HospitalRouteData data = sortMap.get(sortKeySet.get(i));
      data.setEta(convertTimeToETA(sortKeySet.get(i)));
      
      finalDataSet.add(data);
    }
        
    if (qualifyingRouteHospitals.size() < 3) {
      //If we did not get 3 elements, we need to run the comparison for alternate sites, sort it, and pad our results till we get three.
      
      if (alternateRouteHospitals.size() > 0) {
        sortMap.clear();
        sortKeySet.clear();
        
        for (HospitalRouteData hospital : alternateRouteHospitals) {
          String dynamicURL = URL_ROOT.replace("<originLat>", formatter.format(ambLat));
          dynamicURL = dynamicURL.replace("<originLon>", formatter.format(ambLon));
          dynamicURL = dynamicURL.replace("<destLat>", formatter.format(hospital.getHospitalLat()));
          dynamicURL = dynamicURL.replace("<destLon>", formatter.format(hospital.getHospitalLon()));
          
          //DEBUG
          System.out.println ("dynamicURL:" + dynamicURL);
          
          RestTemplate restTemplate = new RestTemplate();
          //MyCustomObject models the return fields from the JSON API call.
          String jsonResults = restTemplate.getForObject(dynamicURL, String.class);
          JSONObject jsonObject = new JSONObject(jsonResults);
          JSONObject distanceObject = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("distance");
          JSONObject durationObject = jsonObject.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration");
          
          hospital.setDistance(distanceObject.getString("text"));
          sortMap.put(new Integer(durationObject.getInt("value")), hospital);
        }
        
        sortKeySet = new ArrayList<Integer>(sortMap.keySet());
        Collections.sort(sortKeySet);
        
        int pos = 0;
        while ((finalDataSet.size() < 3) && (pos < alternateRouteHospitals.size())) {
          HospitalRouteData data = sortMap.get(sortKeySet.get(pos));
          data.setEta(convertTimeToETA(sortKeySet.get(pos)));
          
          finalDataSet.add(data);
          pos++;
        }
      }
    }
    
    return finalDataSet;
  }
  
  private List<Long> parseExcludeIDs(String exclude) throws Exception {
    List<Long> excludeIDs = new LinkedList<Long>();
    if (exclude.trim().length() > 0) {
      while (true) {
        String longString = ""; 
        if (exclude.indexOf(",") != -1) {
          longString = exclude.substring(0, exclude.indexOf(","));
          exclude = exclude.substring(exclude.indexOf(",") + 1);
        } else {
          longString = exclude;
          exclude = "";
        }
        long actualValue = Long.parseLong(longString);
        excludeIDs.add(actualValue);
        if (exclude.trim().length() == 0)
          break;
      }
    }
    return excludeIDs;
  }
  
  private String getTraumaText(Hospital hospital) {
    boolean hasTrauma1 = false;
    boolean hasTrauma2 = false;
    boolean hasTrauma3 = false;
    
    for (String level : hospital.getLevelOfCare()) {
      if (level.equalsIgnoreCase("trauma1"))      hasTrauma1 = true;
      else if (level.equalsIgnoreCase("trauma2")) hasTrauma2 = true;
      else if (level.equalsIgnoreCase("trauma3")) hasTrauma3 = true;
    }
    
    if (hasTrauma1) return " (Level I Trauma Center)";
    if (hasTrauma2) return " (Level II Trauma Center)";
    if (hasTrauma3) return " (Level III Trauma Center)";
    return "";
  }
  
  private String convertTimeToETA(int timeInSeconds) {
    int minutes = timeInSeconds / 60;
    int seconds = timeInSeconds % 60;
    if (seconds < 10)
      return "" + minutes + ":0" + seconds;
    else return "" + minutes + ":" + seconds;
  }
}
