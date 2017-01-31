/**
 * 
 */
package com.magnifis.parking.model.audioburst;

import java.io.Serializable;
import java.util.Date;

import com.magnifis.parking.Xml.ML;

import android.text.format.Time;

/**
 * @author zeev
 *
 */
public class ABFeed implements Serializable {
	@ML("type")
	protected String    type=null;
	@ML("analysis")
	protected String    analysis=null;	
	@ML("textResponse")
	protected String    textResponse=null;

	@ML(tag="cacheDate", attr="date", format="yyyy-MM-dd HH:mm:ss")
	protected Date      cacheDate=null;	//"2016-11-28 08:30:38"

	@ML("question")
	protected String    question=null;	//"What is the latest on Donald Trump"
	@ML("actual")
	protected String    actual=null;    //"donald trump"
	@ML("queryId")
	protected Integer   queryId=null;
	@ML("bursts")
	protected Burst bursts[]=null;	
	
	public Burst[] getBursts() {
		return bursts;
	}
	public void setBursts(Burst[] bursts) {
		this.bursts = bursts;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getAnalysis() {
		return analysis;
	}
	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}
	public String getTextResponse() {
		return textResponse;
	}
	public void setTextResponse(String textResponse) {
		this.textResponse = textResponse;
	}
	
	/*
	public Date getCacheDate() {
		return cacheDate;
	}
	public void setCacheDate(Date cacheDate) {
		this.cacheDate = cacheDate;
	}*/
	
	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getActual() {
		return actual;
	}
	public void setActual(String actual) {
		this.actual = actual;
	}
	public Integer getQueryId() {
		return queryId;
	}
	public void setQueryId(Integer queryId) {
		this.queryId = queryId;
	}

}
