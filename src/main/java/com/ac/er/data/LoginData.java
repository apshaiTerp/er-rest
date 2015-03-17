package com.ac.er.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author ac010168
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginData {

  private long hospitalID;
  private long ambulanceID;
  private String password;
  
  public LoginData() {
    setHospitalID(-1);
    setAmbulanceID(-1);
    setPassword(null);
  }
  
  public LoginData(long hospitalID, long ambulanceID, String password) {
    setHospitalID(hospitalID);
    setAmbulanceID(ambulanceID);
    setPassword(password);
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
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }
}
