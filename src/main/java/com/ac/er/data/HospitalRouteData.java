package com.ac.er.data;

/**
 * @author ac010168
 *
 */
public class HospitalRouteData {

  private long     hospitalID;
  private String   hospitalName;
  private double   hospitalLat;
  private double   hospitalLon;
  private String   hospitalString;
  private String   distance;
  private String   eta;
  
  public HospitalRouteData() {
    hospitalID         = -1;
    hospitalName       = null;
    hospitalLat        = 0.0;
    hospitalLon        = 0.0;
    hospitalString     = null;
    distance           = null;
    eta                = null;
  }

  /**
   * @return the hospitalID
   */
  public long getHospitalID() {
    return hospitalID;
  }

  /**
   * @param hospitalID the hospitalID to set
   */
  public void setHospitalID(long hospitalID) {
    this.hospitalID = hospitalID;
  }

  /**
   * @return the hospitalName
   */
  public String getHospitalName() {
    return hospitalName;
  }

  /**
   * @param hospitalName the hospitalName to set
   */
  public void setHospitalName(String hospitalName) {
    this.hospitalName = hospitalName;
  }

  /**
   * @return the hospitalLat
   */
  public double getHospitalLat() {
    return hospitalLat;
  }

  /**
   * @param hospitalLat the hospitalLat to set
   */
  public void setHospitalLat(double hospitalLat) {
    this.hospitalLat = hospitalLat;
  }

  /**
   * @return the hospitalLon
   */
  public double getHospitalLon() {
    return hospitalLon;
  }

  /**
   * @param hospitalLon the hospitalLon to set
   */
  public void setHospitalLon(double hospitalLon) {
    this.hospitalLon = hospitalLon;
  }

  /**
   * @return the hospitalString
   */
  public String getHospitalString() {
    return hospitalString;
  }

  /**
   * @param hospitalString the hospitalString to set
   */
  public void setHospitalString(String hospitalString) {
    this.hospitalString = hospitalString;
  }

  /**
   * @return the distance
   */
  public String getDistance() {
    return distance;
  }

  /**
   * @param distance the distance to set
   */
  public void setDistance(String distance) {
    this.distance = distance;
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
}
