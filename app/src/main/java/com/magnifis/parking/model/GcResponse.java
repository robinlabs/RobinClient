package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class GcResponse implements Serializable {
  public boolean isSuccessful() {
	  return "OK".equals(status);
  }
	
	
  @ML("status")
  protected String status=null;
  @ML("result")
  protected GcResult results[]=null;
  
  //protected GcResult result=null;
  public GcResult[] getResults() {
	  return results;
  }
  
  public void setResults(GcResult[] results) {
	  this.results = results;
  }
 
/**
   * @return the status
   */
  public String getStatus() {
	  return status;
  }
  /**
   * @param status the status to set
   */
  public void setStatus(String status) {
	  this.status = status;
  }

}
