package com.ac.er.rest;

import java.net.UnknownHostException;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ac.er.data.Ambulance;
import com.ac.er.message.SimpleErrorMessage;
import com.ac.er.message.SimpleMessage;
import com.ac.er.mongo.AmbulanceConverter;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

/**
 * @author ac010168
 *
 */
@RestController
@RequestMapping("/ambulance")
public class AmbulanceController {

  @RequestMapping(method = RequestMethod.GET, produces="application/json;charset=UTF-8")
  public Object getAmbulance(@RequestParam(value="ambulanceid") long ambulanceID) {
    if (ambulanceID < 0) return new SimpleErrorMessage("Invalid Parameters", "The Ambulance ID Provided was not a valid value");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection ambulanceCollection = mongoDB.getCollection("ambulance");
    
    BasicDBObject queryObject = AmbulanceConverter.convertIDTOQuery(ambulanceID);
    DBCursor cursor = ambulanceCollection.find(queryObject);
    Ambulance ambulance = null;
    
    while (cursor.hasNext()) {
      DBObject resultObject = cursor.next();
      ambulance = AmbulanceConverter.convertMongoToAmbulance(resultObject);
    }
    try { cursor.close(); } catch (Throwable t) { /** Ignore Errors */ }
    try { client.close(); } catch (Throwable t) { /** Ignore Errors */ }
    
    if (ambulance == null)
      return new SimpleMessage ("Ambulance Not Found", "Unable to find an ambulance with an ID of " + ambulanceID);

    return ambulance;
  }
  
  @RequestMapping(method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces="application/json;charset=UTF-8")
  public Object postAmbulance(@RequestBody Ambulance ambulance) {
    if (ambulance == null) return new SimpleErrorMessage("Invalid Parameters", "The Ambulance Data Provided was not valid");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
      client.setWriteConcern(WriteConcern.JOURNALED);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection ambulanceCollection = mongoDB.getCollection("ambulance");

    //We're going to bypass the whole insert/upsert/update dilmena by using the update with upsert option
    try {
      ambulanceCollection.update(AmbulanceConverter.convertIDTOQuery(ambulance), 
          AmbulanceConverter.convertAmbulanceToMongo(ambulance), true, false);
    } catch (Throwable t) {
      t.printStackTrace();
      try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
      return new SimpleErrorMessage("Database Operation Exception", "Database Operation Failed: " + t.getMessage());
    }
    return new SimpleMessage("Operation Successful", "The POST operation was successful");
  }
  
  @RequestMapping(method = RequestMethod.PUT, consumes = "application/json;charset=UTF-8", produces="application/json;charset=UTF-8")
  public Object putAmbulance(@RequestParam(value="ambulanceid") long ambulanceID, 
                             @RequestBody Ambulance ambulance) {
    if (ambulance == null) return new SimpleErrorMessage("Invalid Parameters", "The Ambulance Data Provided was not valid");
    if (ambulanceID != ambulance.getAmbulanceID())
      return new SimpleErrorMessage("Invalid Parameters", "The Ambulance Data Provided was not valid");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
      client.setWriteConcern(WriteConcern.JOURNALED);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection ambulanceCollection = mongoDB.getCollection("ambulance");

    //We're going to bypass the whole insert/upsert/update dilmena by using the update with upsert option
    try {
      ambulanceCollection.update(AmbulanceConverter.convertIDTOQuery(ambulance), 
          AmbulanceConverter.convertAmbulanceToMongo(ambulance), true, false);
    } catch (Throwable t) {
      t.printStackTrace();
      try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
      return new SimpleErrorMessage("Database Operation Exception", "Database Operation Failed: " + t.getMessage());
    }
    try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
    return new SimpleMessage("Operation Successful", "The PUT operation was successful");
  }
  
  @RequestMapping(method = RequestMethod.DELETE, produces="application/json;charset=UTF-8")
  public Object deleteAmbulance(@RequestParam(value="ambulanceid") long ambulanceID) {
    if (ambulanceID < 0) return new SimpleErrorMessage("Invalid Parameters", "The Ambulance ID Provided was not a valid value");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection ambulanceCollection = mongoDB.getCollection("ambulance");
    
    try {
      ambulanceCollection.remove(AmbulanceConverter.convertIDTOQuery(ambulanceID));
    } catch (Throwable t) {
      try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
      return new SimpleErrorMessage("Database Operation Exception", "Database Operation Failed: " + t.getMessage());
    }
    try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
    return new SimpleMessage("Operation Successful", "The DELETE operation was successful");
  }
}
