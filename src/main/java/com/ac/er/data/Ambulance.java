package com.ac.er.data;

import java.util.Date;

/**
 * @author ac010168
 *
 */
public class Ambulance {
  private long   ambulanceID;
  private double ambLat;
  private double ambLon;
  private Date   lastUpdate;
  private int    targetHospital;
  private String eta;
  private String patientAge;
  private String patientCategory;
  
  public Ambulance() {
    ambulanceID     = -1;
    ambLat          = 0.0;
    ambLon          = 0.0;
    lastUpdate      = null;
    targetHospital  = -1;
    eta             = null;
    patientAge      = null;
    patientCategory = null;
  }

  /**
   * @return the ambulanceID
   */
  public long getAmbulanceID() {
    return ambulanceID;
  }

  /**
   * @param ambulanceID the ambulanceID to set
   */
  public void setAmbulanceID(long ambulanceID) {
    this.ambulanceID = ambulanceID;
  }

  /**
   * @return the ambLat
   */
  public double getAmbLat() {
    return ambLat;
  }

  /**
   * @param ambLat the ambLat to set
   */
  public void setAmbLat(double ambLat) {
    this.ambLat = ambLat;
  }

  /**
   * @return the ambLon
   */
  public double getAmbLon() {
    return ambLon;
  }

  /**
   * @param ambLon the ambLon to set
   */
  public void setAmbLon(double ambLon) {
    this.ambLon = ambLon;
  }

  /**
   * @return the lastUpdate
   */
  public Date getLastUpdate() {
    return lastUpdate;
  }

  /**
   * @param lastUpdate the lastUpdate to set
   */
  public void setLastUpdate(Date lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  /**
   * @return the targetHospital
   */
  public int getTargetHospital() {
    return targetHospital;
  }

  /**
   * @param targetHospital the targetHospital to set
   */
  public void setTargetHospital(int targetHospital) {
    this.targetHospital = targetHospital;
  }

  /**
   * @return the eta
   */
  public String getEta() {
    return eta;
  }

  /**
   * @param eta the eta to set
   */
  public void setEta(String eta) {
    this.eta = eta;
  }

  /**
   * @return the patientAge
   */
  public String getPatientAge() {
    return patientAge;
  }

  /**
   * @param patientAge the patientAge to set
   */
  public void setPatientAge(String patientAge) {
    this.patientAge = patientAge;
  }

  /**
   * @return the patientCategory
   */
  public String getPatientCategory() {
    return patientCategory;
  }

  /**
   * @param patientCategory the patientCategory to set
   */
  public void setPatientCategory(String patientCategory) {
    this.patientCategory = patientCategory;
  }
}
