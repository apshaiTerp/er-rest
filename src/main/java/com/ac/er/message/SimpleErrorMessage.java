package com.ac.er.message;

/**
 * @author ac010168
 *
 */
public class SimpleErrorMessage {

  private String errorType;
  private String errorMessage;
  
  public SimpleErrorMessage() {
    errorType    = null;
    errorMessage = null;
  }
  
  public SimpleErrorMessage(String errorType, String errorMessage) {
    this.errorType    = errorType;
    this.errorMessage = errorMessage;
  }

  /**
   * @return the errorType
   */
  public String getErrorType() {
    return errorType;
  }

  /**
   * @param errorType the errorType to set
   */
  public void setErrorType(String errorType) {
    this.errorType = errorType;
  }

  /**
   * @return the errorMessage
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * @param errorMessage the errorMessage to set
   */
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
