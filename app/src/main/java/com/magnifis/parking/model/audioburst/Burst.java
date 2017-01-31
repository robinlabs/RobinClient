package com.magnifis.parking.model.audioburst;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class Burst implements Serializable {
	@ML("no")
  	protected Integer  no=null;     //	1
	@ML("burstId")
	protected String   burstId=null; //	"14b37bea-af94-4b99-9566-eb201d684382"
	@ML("title")
	protected String   title=null;   //	"I found the following 5 bursts. Aired one minute ago, On 760 KFMB Radio."
	@ML("audioURL")
	protected String   audioURL=null;	//"https://storageaudiobursts.blob.core.windows.net/audio/14b37bea-af94-4b99-9566-eb201d684382_48.mp3"
	@ML("score")
	protected Double   score=null;	//1.7258712
	@ML("details")
	protected Details  details=null;
		
	
	public Details getDetails() {
		return details;
	}
	public void setDetails(Details details) {
		this.details = details;
	}
	public Integer getNo() {
		return no;
	}
	public void setNo(Integer no) {
		this.no = no;
	}
	public String getBurstId() {
		return burstId;
	}
	public void setBurstId(String burstId) {
		this.burstId = burstId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAudioURL() {
		return audioURL;
	}
	public void setAudioURL(String audioURL) {
		this.audioURL = audioURL;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	

}
