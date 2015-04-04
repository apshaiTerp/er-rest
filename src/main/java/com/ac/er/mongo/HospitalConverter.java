package com.ac.er.mongo;

import com.ac.er.data.Hospital;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * @author ac010168
 *
 */
public class HospitalConverter {
  
  public static BasicDBObject convertIDTOQuery(Hospital hospital) {
    if (hospital == null) return null;
    
    BasicDBObject dbObject = new BasicDBObject("hospitalID", hospital.getHospitalID());
    return dbObject;
  }

  public static BasicDBObject convertIDTOQuery(long hospitalID) {
    if (hospitalID < 0) return null;
    
    BasicDBObject dbObject = new BasicDBObject("hospitalID", hospitalID);
    return dbObject;
  }
  
  public static BasicDBObject convertHospitalToMongo(Hospital hospital) {
    if (hospital == null) return null;
    
    BasicDBObject dbObject = new BasicDBObject("hospitalID", hospital.getHospitalID());
    if (hospital.getHospitalName() != null)     dbObject.append("hospitalName", hospital.getHospitalName());
    if (hospital.getHospitalLat() != 0.0)       dbObject.append("lat", hospital.getHospitalLat());
    if (hospital.getHospitalLon() != 0.0)       dbObject.append("lon", hospital.getHospitalLon());
    if (hospital.getAddress() != null)          dbObject.append("address", hospital.getAddress());
    if (hospital.getLevelOfCare() != null)      dbObject.append("levelOfCare", convertArray(hospital.getLevelOfCare()));
    if (hospital.getHostAges() != null)         dbObject.append("patientAges", convertArray(hospital.getHostAges()));
    if (hospital.getTraumaBeds() != -1)         dbObject.append("traumaBeds", hospital.getTraumaBeds());
    if (hospital.getTraumaBedsFree() != -1)     dbObject.append("traumaBedsFree", hospital.getTraumaBedsFree());
    if (hospital.getTraumaBedsOccupied() != -1) dbObject.append("traumaBedsOccupied", hospital.getTraumaBedsOccupied());
    if (hospital.getTraumaBedsCleanup() != -1)  dbObject.append("traumaBedsCleanup", hospital.getTraumaBedsCleanup());
    if (hospital.getErBeds() != -1)             dbObject.append("erBeds", hospital.getErBeds());
    if (hospital.getErBeds() != -1)             dbObject.append("erBedsFree", hospital.getErBedsFree());
    if (hospital.getErBeds() != -1)             dbObject.append("erBedsOccupied", hospital.getErBedsOccupied());
    if (hospital.getErBedsCleanup() != -1)      dbObject.append("erBedsCleanup", hospital.getErBedsCleanup());

    return dbObject;
  }
  
  public static Hospital convertMongoToHospital(DBObject dbObject) {
    if (dbObject == null) return null;
    
    Hospital hospital = new Hospital();
    if (dbObject.containsField("hospitalID"))         hospital.setHospitalID((Long)dbObject.get("hospitalID"));
    if (dbObject.containsField("hospitalName"))       hospital.setHospitalName((String)dbObject.get("hospitalName"));
    if (dbObject.containsField("lat"))                hospital.setHospitalLat((Double)dbObject.get("lat"));
    if (dbObject.containsField("lon"))                hospital.setHospitalLon((Double)dbObject.get("lon"));
    if (dbObject.containsField("address"))            hospital.setAddress((String)dbObject.get("address"));
    if (dbObject.containsField("levelOfCare"))        hospital.setLevelOfCare(convertDBListToStringArray((BasicDBList)dbObject.get("levelOfCare")));
    if (dbObject.containsField("patientAges"))        hospital.setHostAges(convertDBListToStringArray((BasicDBList)dbObject.get("patientAges")));
    if (dbObject.containsField("traumaBeds"))         hospital.setTraumaBeds((Integer)dbObject.get("traumaBeds"));
    if (dbObject.containsField("traumaBedsFree"))     hospital.setTraumaBedsFree((Integer)dbObject.get("traumaBedsFree"));
    if (dbObject.containsField("traumaBedsOccupied")) hospital.setTraumaBedsOccupied((Integer)dbObject.get("traumaBedsOccupied"));
    if (dbObject.containsField("traumaBedsCleanup"))  hospital.setTraumaBedsCleanup((Integer)dbObject.get("traumaBedsCleanup"));
    if (dbObject.containsField("erBeds"))             hospital.setErBeds((Integer)dbObject.get("erBeds"));
    if (dbObject.containsField("erBedsFree"))         hospital.setErBedsFree((Integer)dbObject.get("erBedsFree"));
    if (dbObject.containsField("erBedsOccupied"))     hospital.setErBedsOccupied((Integer)dbObject.get("erBedsOccupied"));
    if (dbObject.containsField("erBedsCleanup"))      hospital.setErBedsCleanup((Integer)dbObject.get("erBedsCleanup"));
    
    return hospital;
  }
  
  /**
   * Helper method to parse Lists into List format for Mongo.  Parameterized as <?> to
   * allow for generic mapping, provided those objects are simple objects.
   * 
   * @param curList The List of elements (not null) to be converted into an array.
   * @return A new list in BasicDBList format.
   */
  private static BasicDBList convertArray(String[] curArray) {
    if (curArray == null) return null;
    
    BasicDBList newList = new BasicDBList();
    for (Object obj : curArray)
      newList.add(obj);
    return newList;
  }
  
  /**
   * Helper method to parse Lists from Mongo into Java.  
   * 
   * @param curList The List of elements (not null) to be converted into an array.
   * @return A new list in List<String> format.
   */
  private static String[] convertDBListToStringArray(BasicDBList curList) {
    if (curList == null) return null;
    
    String[] newArray = new String[curList.size()];
    for (int i = 0; i < curList.size(); i++) {
      newArray[i] = (String)curList.get(i);
    }
    return newArray;
  }
}
