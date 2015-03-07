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
import com.ac.er.message.SimpleErrorMessage;
import com.ac.er.mongo.HospitalConverter;
import com.mongodb.BasicDBList;
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
   * @param condition Should be one of {level1|level2|level3|basicER}
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
        !condition.equalsIgnoreCase("level3") && !condition.equalsIgnoreCase("basicER"))
      return new SimpleErrorMessage("Invalid Parameters", "The Condition Value was not in the set of {level1|level2|level3|basicER}");
    List<Long> excludeIDs = null;
    try {
      if (exclude.trim().length() == 0) excludeIDs = new ArrayList<Long>(0);
      else                              excludeIDs = parseExcludeIDs(exclude);
    } catch (Throwable t) {
      return new SimpleErrorMessage("Invalid Parameters", "Unable to Parse ID values from exclude list");
    }
    
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
    
    //db.hospital.find( { "patientAges": { $in: ["child"] }})
    //db.hospital.find( { "levelOfCare": { $in: ["level1", "level2"] }})
    BasicDBList ageList = new BasicDBList();
    ageList.add(age);
    
    BasicDBList conditionList = new BasicDBList();
    if (condition.equalsIgnoreCase("level1")) {
      conditionList.add("level1");
    }
    if (condition.equalsIgnoreCase("level2")) {
      conditionList.add("level1");
      conditionList.add("level2");
    }
    if (condition.equalsIgnoreCase("level3")) {
      conditionList.add("level1");
      conditionList.add("level2");
      conditionList.add("level3");
    }
    if (condition.equalsIgnoreCase("basicER")) {
      conditionList.add("level1");
      conditionList.add("level2");
      conditionList.add("level3");
      conditionList.add("basicER");
    }
    
    //Now we need to construct the correct the correct query to get what we want
    //db.hospital.find( { "patientAges": { $in: ["child"] }, "levelOfCare": { $in: ["level1", "level2"] }})
    BasicDBObject queryObject  = new BasicDBObject();
    queryObject.append("patientAges", new BasicDBObject("$in", ageList));
    queryObject.append("levelOfCare", new BasicDBObject("$in", conditionList));
    
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
    
    //TODO - Override this return, right now it will return all qualifyingHospitals
    return qualifyingHospitals;
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
