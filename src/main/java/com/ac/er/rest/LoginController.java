package com.ac.er.rest;

import java.net.UnknownHostException;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ac.er.data.LoginData;
import com.ac.er.message.SimpleErrorMessage;
import com.ac.er.message.SimpleMessage;
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
@RequestMapping("/login")
public class LoginController {

  @RequestMapping(method = RequestMethod.GET, produces="application/json;charset=UTF-8")
  public Object checkLogin(@RequestParam(value="ambulanceid", defaultValue="-1") long ambulanceID, 
                           @RequestParam(value="hospitalid", defaultValue="-1") long hospitalID, 
                           @RequestParam(value="pass") String pass) {
    if ((ambulanceID == -1) && (hospitalID == -1))
      return new SimpleErrorMessage("Invalid Parameters", "At least one ID value must be provided.");
    if ((ambulanceID >= 0) && (hospitalID >= 0))
      return new SimpleErrorMessage("Invalid Parameters", "We cannot login to both a hospital and ambulance.");
    if (pass == null)
      return new SimpleErrorMessage("Invalid Parameters", "No password was provided");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection accessCollection = mongoDB.getCollection("access");
    
    //Let's just query for username, then validate password, so we can generate distinct messages
    BasicDBObject queryObject = new BasicDBObject();
    if (ambulanceID >= 0) queryObject.append("ambulanceID", ambulanceID);
    else                  queryObject.append("hospitalID", hospitalID);
    
    DBCursor results = accessCollection.find(queryObject);
    while (results.hasNext()) {
      DBObject resultObject = results.next();
      if (resultObject.containsField("password")) {
        String dbPassword = (String)resultObject.get("password");
        if (dbPassword.compareTo(pass) == 0) {
          try { results.close(); } catch (Throwable t) { /** Ignore Close Errors */ }
          try { client.close();  } catch (Throwable t) { /** Ignore Close Errors */ }
          
          return new SimpleMessage("Login Successful", "This password was accepted.");
        } else {
          try { results.close(); } catch (Throwable t) { /** Ignore Close Errors */ }
          try { client.close();  } catch (Throwable t) { /** Ignore Close Errors */ }
          
          return new SimpleErrorMessage("Login Unsuccessful", "Password Incorrect!");
        }
      }
    }
    
    try { client.close();  } catch (Throwable t) { /** Ignore Close Errors */ }
    
    return new SimpleErrorMessage("Login Unsuccessful", "Could not find " + 
    (ambulanceID >= 0 ? ("Ambulance ID " + ambulanceID) : ("Hospital ID " + hospitalID)) + "!");
  }

  @RequestMapping(method = RequestMethod.POST, consumes="application/json;charset=UTF-8", produces="application/json;charset=UTF-8")
  public Object postLogin(@RequestBody LoginData loginData) {
    if ((loginData.getAmbulanceID() == -1) && (loginData.getHospitalID() == -1))
      return new SimpleErrorMessage("Invalid Parameters", "At least one ID value must be provided.");
    if ((loginData.getAmbulanceID() >= 0) && (loginData.getHospitalID() >= 0))
      return new SimpleErrorMessage("Invalid Parameters", "We cannot login to both a hospital and ambulance.");
    if (loginData.getPassword() == null)
      return new SimpleErrorMessage("Invalid Parameters", "No password was provided");
    
    MongoClient client = null;
    try {
      client = new MongoClient("localhost", 27017);
    } catch (UnknownHostException e) {
      e.printStackTrace();
      return new SimpleErrorMessage("Database Error", "Database Unavailable!");
    }
    
    DB mongoDB = client.getDB("erdb");
    DBCollection accessCollection = mongoDB.getCollection("access");
    
    //Let's just query for username, then validate password, so we can generate distinct messages
    BasicDBObject queryObject = new BasicDBObject();
    if (loginData.getAmbulanceID() >= 0) queryObject.append("ambulanceID", loginData.getAmbulanceID());
    else                                 queryObject.append("hospitalID", loginData.getHospitalID());
    
    DBCursor results = accessCollection.find(queryObject);
    while (results.hasNext()) {
      DBObject resultObject = results.next();
      if (resultObject.containsField("password")) {
        String dbPassword = (String)resultObject.get("password");
        if (dbPassword.compareTo(loginData.getPassword()) == 0) {
          try { results.close(); } catch (Throwable t) { /** Ignore Close Errors */ }
          try { client.close();  } catch (Throwable t) { /** Ignore Close Errors */ }
          
          return new SimpleMessage("Login Successful", "This password was accepted.");
        } else {
          try { results.close(); } catch (Throwable t) { /** Ignore Close Errors */ }
          try { client.close();  } catch (Throwable t) { /** Ignore Close Errors */ }
          
          return new SimpleErrorMessage("Login Unsuccessful", "Password Incorrect!");
        }
      }
    }
    
    try { client.close();  } catch (Throwable t) { /** Ignore Close Errors */ }
    
    return new SimpleErrorMessage("Login Unsuccessful", "Could not find " + 
    (loginData.getAmbulanceID() >= 0 ? ("Ambulance ID " + loginData.getAmbulanceID()) : ("Hospital ID " + loginData.getHospitalID())) + "!");
  }
}
