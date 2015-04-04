package com.ac.er.rest;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  
  /**
   * 
   * @param ambLat    <latitude coordinate of ambulance>
   * @param ambLon    <longitude coordinate of ambulance>
   * @param age       Should be one of {child|teen|adult}
   * @param condition Should be one of {level1|level2|level3|basicER|burn|stemi|stroke}
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
    if (!condition.equalsIgnoreCase("level1") && !condition.equalsIgnoreCase("level2") && 
        !condition.equalsIgnoreCase("level3") && !condition.equalsIgnoreCase("basicER") &&
        !condition.equalsIgnoreCase("burn") && !condition.equalsIgnoreCase("stemi") && 
        !condition.equalsIgnoreCase("stroke"))
      return new SimpleErrorMessage("Invalid Parameters", "The Condition Value was not in the set of {level1|level2|level3|basicER|burn|stemi|stroke}");
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
        altCondition  = "trauma1";
      } else if (condition.toLowerCase().startsWith("level")) {
        trueCondition = "traumaPed";
        altCondition  = condition.toLowerCase().replace("level", "trauma");
      } else {
        //If we got here, we somehow have a child manifesting with a stroke or heart attack....
        trueCondition = "traumaPed";
        altCondition  = "trauma1";
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
      } else {
        //The only thing left is the levelX trauma conditions
        if (condition.equalsIgnoreCase("level1")) {
          trueCondition = "trauma1";
          altCondition  = "trauma2";
        } else if (condition.equalsIgnoreCase("level2")) {
          trueCondition = "trauma2";
          altCondition  = "trauma3";
        } else if (condition.equalsIgnoreCase("level3")) {
          trueCondition = "trauma3";
          altCondition  = "basicER";
        }
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
      BasicDBObject queryObject2 = new BasicDBObject("levelOfCare", trueCondition);
      
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
        } else if (trueCondition.equalsIgnoreCase("trauma1")) {
          route.setHospitalName(hospital.getHospitalName() + " (Level I Trauma Center)");
        } else if (trueCondition.equalsIgnoreCase("trauma2")) {
          route.setHospitalName(hospital.getHospitalName() + " (Level II Trauma Center)");
        } else if (trueCondition.equalsIgnoreCase("trauma3")) {
          route.setHospitalName(hospital.getHospitalName() + " (Level III Trauma Center)");
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
        if (trueCondition.equalsIgnoreCase("trauma1")) {
          route.setHospitalName(hospital.getHospitalName() + " (Level I Trauma Center)");
        } else if (trueCondition.equalsIgnoreCase("trauma2")) {
          route.setHospitalName(hospital.getHospitalName() + " (Level II Trauma Center)");
        } else if (trueCondition.equalsIgnoreCase("trauma3")) {
          route.setHospitalName(hospital.getHospitalName() + " (Level III Trauma Center)");
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
    *******************************************************************************************************/
    
    //We should now have a list of Hospitals.  Now we need to go run our search algorithm.
    //TODO - Write the algorithm here
    /*****************************************************************************************
    This is where we will make our External calls to figure out what needs to happen.
    Spring provides some basic classes for making these calls.
    
    The actual code here will be something like this:
    for (Hospital hospital : qualifyingHospitals) {
      String dynamicURL = "My API root" + ambLat + "&lon=" + ambLon;
      
      RestTemplate restTemplate = new RestTemplate();
      //MyCustomObject models the return fields from the JSON API call.
      MyCustomObject object = restTemplate.getForObject(dynamicURL, MyCustomObject.class);
      
      //Use the values retrieved to figure out what needs to be done
    
    }
    
    For example or helps, use https://spring.io/guides/gs/consuming-rest/
    *****************************************************************************************/
    
    //TODO - Override this return, right now it will return all qualifyingRouteHospitals
    return qualifyingRouteHospitals;
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

}
