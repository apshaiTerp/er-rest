package com.ac.er.rest;

import java.net.UnknownHostException;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ac.er.data.Hospital;
import com.ac.er.message.SimpleErrorMessage;
import com.ac.er.message.SimpleMessage;
import com.ac.er.mongo.HospitalConverter;
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
@RequestMapping("/hospital")
public class HospitalController {

  @RequestMapping(method = RequestMethod.GET, produces="application/json;charset=UTF-8")
  public Object getHospital(@RequestParam(value="hospitalid") long hospitalID) {
    if (hospitalID < 0) return new SimpleErrorMessage("Invalid Parameters", "The Hospital ID Provided was not a valid value");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection hospitalCollection = mongoDB.getCollection("hospital");
    BasicDBObject queryObject = HospitalConverter.convertIDTOQuery(hospitalID);
    DBCursor cursor = hospitalCollection.find(queryObject);
    Hospital hospital = null;
    
    while (cursor.hasNext()) {
      DBObject resultObject = cursor.next();
      hospital = HospitalConverter.convertMongoToHospital(resultObject);
    }
    try { cursor.close(); } catch (Throwable t) { /** Ignore Errors */ }
    try { client.close(); } catch (Throwable t) { /** Ignore Errors */ }

    if (hospital == null)
      return new SimpleMessage ("Hospital Not Found", "Unable to find a hospital with an ID of " + hospitalID);
    
    return hospital;
  }
  
  @RequestMapping(method = RequestMethod.POST)
  public Object postHospital(@RequestBody Hospital hospital) {
    if (hospital == null) return new SimpleErrorMessage("Invalid Parameters", "The Hospital Data Provided was not valid");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
      client.setWriteConcern(WriteConcern.JOURNALED);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection hospitalCollection = mongoDB.getCollection("hospital");

    //We're going to bypass the whole insert/upsert/update dilmena by using the update with upsert option
    try {
      hospitalCollection.update(HospitalConverter.convertIDTOQuery(hospital), 
          HospitalConverter.convertHospitalToMongo(hospital), true, false);
    } catch (Throwable t) {
      t.printStackTrace();
      try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
      return new SimpleErrorMessage("Database Operation Exception", "Database Operation Failed: " + t.getMessage());
    }
    return new SimpleMessage("Operation Successful", "The POST operation was successful");
  }
  
  @RequestMapping(method = RequestMethod.PUT)
  public Object putHospital(@RequestBody Hospital hospital) {
    if (hospital == null) return new SimpleErrorMessage("Invalid Parameters", "The Hospital Data Provided was not valid");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
      client.setWriteConcern(WriteConcern.JOURNALED);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection hospitalCollection = mongoDB.getCollection("hospital");

    //We're going to bypass the whole insert/upsert/update dilmena by using the update with upsert option
    try {
      hospitalCollection.update(HospitalConverter.convertIDTOQuery(hospital), 
          HospitalConverter.convertHospitalToMongo(hospital), true, false);
    } catch (Throwable t) {
      t.printStackTrace();
      try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
      return new SimpleErrorMessage("Database Operation Exception", "Database Operation Failed: " + t.getMessage());
    }
    return new SimpleMessage("Operation Successful", "The POST operation was successful");
  }
  
  @RequestMapping(method = RequestMethod.DELETE)
  public Object deleteHospital(@RequestParam(value="hospitalid") long hospitalID) {
    if (hospitalID < 0) return new SimpleErrorMessage("Invalid Parameters", "The Hospital ID Provided was not a valid value");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection hospitalCollection = mongoDB.getCollection("hospital");
    
    try {
      hospitalCollection.remove(HospitalConverter.convertIDTOQuery(hospitalID));
    } catch (Throwable t) {
      try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
      return new SimpleErrorMessage("Database Operation Exception", "Database Operation Failed: " + t.getMessage());
    }
    try { client.close(); } catch (Throwable t2) { /** Ignore Errors */ }
    return new SimpleMessage("Operation Successful", "The DELETE operation was successful");
  }
}
