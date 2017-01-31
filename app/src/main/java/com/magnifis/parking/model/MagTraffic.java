package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class MagTraffic {
  @ML("report")
  protected String reports[]=null;

  /**
   * @return the report
   */
  public String[] getReports() {
	  return reports;
  }

  /**
   * @param report the report to set
   */
  public void setReports(String[] reports) {
	  this.reports = reports;
  }
}
