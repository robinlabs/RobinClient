package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class PkNotification implements Serializable {
/****
       "notification_id": "16",
      "region": "{ileFt`jjV{kJ??}pJzkJ??|pJ",
      "max_zoom": 22,
      "min_zoom": 13,
      "link": "http://webmap.realtimeparking.com/index_mobile.html?lat=37.79286870952298&lon=-122.39775934441377&zoom=16",
      "bbox": "-122.445064081818|37.7515014127014|-122.385870840732|37.8098810175174",
      "msg": null,
      "freq": "None",
      "slug": "CLICK HERE for Real Time Meters in San Francisco!",
      "icon": null 
 */
  @ML("notification_id")
  protected Long id=null;
  @ML("max_zoom")
  protected Integer maxZoom=null;
  @ML("min_zoom")
  protected Integer minZoom=null;  
  @ML("link")
  protected String link=null;
  @ML("bbox")
  protected String bbox=null;
  /**
   * @return the id
   */
  public Long getId() {
	  return id;
  }
  /**
   * @param id the id to set
   */
  public void setId(Long id) {
	  this.id = id;
  }
  /**
   * @return the maxZoom
   */
  public Integer getMaxZoom() {
	  return maxZoom;
  }
  /**
   * @param maxZoom the maxZoom to set
   */
  public void setMaxZoom(Integer maxZoom) {
	  this.maxZoom = maxZoom;
  }
  /**
   * @return the minZoom
   */
  public Integer getMinZoom() {
	  return minZoom;
  }
  /**
   * @param minZoom the minZoom to set
   */
  public void setMinZoom(Integer minZoom) {
	  this.minZoom = minZoom;
  }
  /**
   * @return the link
   */
  public String getLink() {
	  return link;
  }
  /**
   * @param link the link to set
   */
  public void setLink(String link) {
	  this.link = link;
  }
  /**
   * @return the bbox
   */
  public String getBbox() {
	  return bbox;
  }
  /**
   * @param bbox the bbox to set
   */
  public void setBbox(String bbox) {
	  this.bbox = bbox;
  }
}
