package com.ac.er.data;

/**
 * 
 * @author ac010168
 *
 */
public class Hospital {

  private long     hospitalID;
  private String   hospitalName;
  private double   hospitalLat;
  private double   hospitalLon;
  private String   address;
  private String[] levelOfCare;
  private String[] hostAges;
  private int      traumaBeds;
  private int      traumaBedsFree;
  private int      traumaBedsOccupied;
  private int      traumaBedsCleanup;
  private int      erBeds;
  private int      erBedsFree;
  private int      erBedsOccupied;
  private int      erBedsCleanup;
  
  private String   erDivert;
  private String   traumaDivert;
  private String   burnDivert;
  private String   stemiDivert;
  private String   strokeDivert;
  
  public Hospital() {
    hospitalID         = -1;
    hospitalName       = null;
    hospitalLat        = 0.0;
    hospitalLon        = 0.0;
    address            = null;
    levelOfCare        = null;
    hostAges           = null;
    traumaBeds         = -1;
    traumaBedsFree     = -1;
    traumaBedsOccupied = -1;
    traumaBedsCleanup  = -1;
    erBeds             = -1;
    erBedsFree         = -1;
    erBedsOccupied     = -1;
    erBedsCleanup      = -1;
    
    erDivert           = null;
    traumaDivert       = null;
    burnDivert         = null;
    stemiDivert        = null;
    strokeDivert       = null;
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
   * @return the hostAges
   */
  public String[] getHostAges() {
    return hostAges;
  }

  /**
   * @param hostAges the hostAges to set
   */
  public void setHostAges(String[] hostAges) {
    this.hostAges = hostAges;
  }

  /**
   * @return the traumaBeds
   */
  public int getTraumaBeds() {
    return traumaBeds;
  }

  /**
   * @param traumaBeds the traumaBeds to set
   */
  public void setTraumaBeds(int traumaBeds) {
    this.traumaBeds = traumaBeds;
  }

  /**
   * @return the traumaBedsFree
   */
  public int getTraumaBedsFree() {
    return traumaBedsFree;
  }

  /**
   * @param traumaBedsFree the traumaBedsFree to set
   */
  public void setTraumaBedsFree(int traumaBedsFree) {
    this.traumaBedsFree = traumaBedsFree;
  }

  /**
   * @return the traumaBedsOccupied
   */
  public int getTraumaBedsOccupied() {
    return traumaBedsOccupied;
  }

  /**
   * @param traumaBedsOccupied the traumaBedsOccupied to set
   */
  public void setTraumaBedsOccupied(int traumaBedsOccupied) {
    this.traumaBedsOccupied = traumaBedsOccupied;
  }

  /**
   * @return the traumaBedsCleanup
   */
  public int getTraumaBedsCleanup() {
    return traumaBedsCleanup;
  }

  /**
   * @param traumaBedsCleanup the traumaBedsCleanup to set
   */
  public void setTraumaBedsCleanup(int traumaBedsCleanup) {
    this.traumaBedsCleanup = traumaBedsCleanup;
  }

  /**
   * @return the erBeds
   */
  public int getErBeds() {
    return erBeds;
  }

  /**
   * @param erBeds the erBeds to set
   */
  public void setErBeds(int erBeds) {
    this.erBeds = erBeds;
  }

  /**
   * @return the erBedsFree
   */
  public int getErBedsFree() {
    return erBedsFree;
  }

  /**
   * @param erBedsFree the erBedsFree to set
   */
  public void setErBedsFree(int erBedsFree) {
    this.erBedsFree = erBedsFree;
  }

  /**
   * @return the erBedsOccupied
   */
  public int getErBedsOccupied() {
    return erBedsOccupied;
  }

  /**
   * @param erBedsOccupied the erBedsOccupied to set
   */
  public void setErBedsOccupied(int erBedsOccupied) {
    this.erBedsOccupied = erBedsOccupied;
  }

  /**
   * @return the erBedsCleanup
   */
  public int getErBedsCleanup() {
    return erBedsCleanup;
  }

  /**
   * @param erBedsCleanup the erBedsCleanup to set
   */
  public void setErBedsCleanup(int erBedsCleanup) {
    this.erBedsCleanup = erBedsCleanup;
  }

  /**
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * @param address the address to set
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * @return the levelOfCare
   */
  public String[] getLevelOfCare() {
    return levelOfCare;
  }

  /**
   * @param levelOfCare the levelOfCare to set
   */
  public void setLevelOfCare(String[] levelOfCare) {
    this.levelOfCare = levelOfCare;
  }

  /**
   * @return the erDivert
   */
  public String getErDivert() {
    return erDivert;
  }

  /**
   * @param erDivert the erDivert to set
   */
  public void setErDivert(String erDivert) {
    this.erDivert = erDivert;
  }

  /**
   * @return the traumaDivert
   */
  public String getTraumaDivert() {
    return traumaDivert;
  }

  /**
   * @param traumaDivert the traumaDivert to set
   */
  public void setTraumaDivert(String traumaDivert) {
    this.traumaDivert = traumaDivert;
  }

  /**
   * @return the burnDivert
   */
  public String getBurnDivert() {
    return burnDivert;
  }

  /**
   * @param burnDivert the burnDivert to set
   */
  public void setBurnDivert(String burnDivert) {
    this.burnDivert = burnDivert;
  }

  /**
   * @return the stemiDivert
   */
  public String getStemiDivert() {
    return stemiDivert;
  }

  /**
   * @param stemiDivert the stemiDivert to set
   */
  public void setStemiDivert(String stemiDivert) {
    this.stemiDivert = stemiDivert;
  }

  /**
   * @return the strokeDivert
   */
  public String getStrokeDivert() {
    return strokeDivert;
  }

  /**
   * @param strokeDivert the strokeDivert to set
   */
  public void setStrokeDivert(String strokeDivert) {
    this.strokeDivert = strokeDivert;
  }
}
