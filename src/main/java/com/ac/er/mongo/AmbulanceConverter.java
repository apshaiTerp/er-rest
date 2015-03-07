package com.ac.er.mongo;

import java.util.Date;

import com.ac.er.data.Ambulance;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author ac010168
 *
 */
public class AmbulanceConverter {

  public static BasicDBObject convertIDTOQuery(Ambulance ambulance) {
    if (ambulance == null) return null;
    
    BasicDBObject dbObject = new BasicDBObject("ambulanceID", ambulance.getAmbulanceID());
    return dbObject;
  }

  public static BasicDBObject convertIDTOQuery(long ambulanceID) {
    if (ambulanceID < 0) return null;
    
    BasicDBObject dbObject = new BasicDBObject("ambulanceID", ambulanceID);
    return dbObject;
  }
  
  public static BasicDBObject convertAmbulanceToMongo(Ambulance ambulance) {
    if (ambulance == null) return null;
    
    BasicDBObject dbObject = new BasicDBObject("ambulanceID", ambulance.getAmbulanceID());
    if (ambulance.getAmbLat() != 0.0)           dbObject.append("ambLat", ambulance.getAmbLat());
    if (ambulance.getAmbLon() != 0.0)           dbObject.append("ambLon", ambulance.getAmbLon());
    if (ambulance.getLastUpdate() != null)      dbObject.append("lastUpdate", ambulance.getLastUpdate());
    if (ambulance.getTargetHospital() != -1)    dbObject.append("targetHospital", ambulance.getTargetHospital());
    if (ambulance.getEta() != null)             dbObject.append("eta", ambulance.getEta());
    if (ambulance.getPatientAge() != null)      dbObject.append("patientAge", ambulance.getPatientAge());
    if (ambulance.getPatientCategory() != null) dbObject.append("patientCategory", ambulance.getPatientCategory());

    return dbObject;
  }
  
  public static Ambulance convertMongoToAmbulance(DBObject dbObject) {
    if (dbObject == null) return null;
    
    Ambulance ambulance = new Ambulance();
    if (dbObject.containsField("ambulanceID"))     ambulance.setAmbulanceID((Long)dbObject.get("ambulanceID"));
    if (dbObject.containsField("ambLat"))          ambulance.setAmbLat((Double)dbObject.get("ambLat"));
    if (dbObject.containsField("ambLon"))          ambulance.setAmbLon((Double)dbObject.get("ambLon"));
    if (dbObject.containsField("lastUpdate"))      ambulance.setLastUpdate((Date)dbObject.get("lastUpdate"));
    if (dbObject.containsField("targetHospital"))  ambulance.setTargetHospital((Integer)dbObject.get("targetHospital"));
    if (dbObject.containsField("eta"))             ambulance.setEta((String)dbObject.get("eta"));
    if (dbObject.containsField("patientAge"))      ambulance.setPatientAge((String)dbObject.get("patientAge"));
    if (dbObject.containsField("patientCategory")) ambulance.setPatientCategory((String)dbObject.get("patientCategory"));

    return ambulance;
  }
}
