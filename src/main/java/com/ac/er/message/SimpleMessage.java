package com.ac.er.message;

/**
 * @author ac010168
 *
 */
public class SimpleMessage {

  private String messageType;
  private String message;
  
  public SimpleMessage() {
    messageType = null;
    message     = null;
  }
  
  public SimpleMessage(String messageType, String message) {
    this.messageType = messageType;
    this.message     = message;
  }

  /**
   * @return the errorType
   */
  public String getMessageType() {
    return messageType;
  }

  /**
   * @param errorType the errorType to set
   */
  public void setMessageType(String errorType) {
    this.messageType = errorType;
  }

  /**
   * @return the errorMessage
   */
  public String getMessage() {
    return message;
  }

  /**
   * @param errorMessage the errorMessage to set
   */
  public void setMessage(String errorMessage) {
    this.message = errorMessage;
  }
}
