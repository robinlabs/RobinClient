package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Date;

import com.magnifis.parking.Xml.ML;

public class PkRateQuery implements Serializable {
  @ML("duration")
  protected Integer duration=null;
  @ML("max_duration")
  protected Integer max_duration=null;
  @ML("entry_dt")
  protected Date entry_dt=null;
  @ML("rate_request")
  protected String rate_request=null;
  /**
   * @return the duration
   */
  public Integer getDuration() {
	  return duration;
  }
  /**
   * @param duration the duration to set
   */
  public void setDuration(Integer duration) {
	  this.duration = duration;
  }
  /**
   * @return the max_duration
   */
  public Integer getMax_duration() {
	  return max_duration;
  }
  /**
   * @param max_duration the max_duration to set
   */
  public void setMax_duration(Integer max_duration) {
	  this.max_duration = max_duration;
  }
  /**
   * @return the entry_dt
   */
  public Date getEntry_dt() {
	  return entry_dt;
  }
  /**
   * @param entry_dt the entry_dt to set
   */
  public void setEntry_dt(Date entry_dt) {
	  this.entry_dt = entry_dt;
  }
  /**
   * @return the rate_request
   */
  public String getRate_request() {
	  return rate_request;
  }
  /**
   * @param rate_request the rate_request to set
   */
  public void setRate_request(String rate_request) {
	  this.rate_request = rate_request;
  }
}
