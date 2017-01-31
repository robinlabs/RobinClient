package com.magnifis.parking.model;

import java.io.Serializable;

import android.app.Activity;
import android.content.Context;

import com.magnifis.parking.App;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.Output;
import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

public class QueryInterpretation implements Serializable, Cloneable {
	
	public QueryInterpretation clone() {
		try {
			return (QueryInterpretation)super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public final static int TO_SAY=1, TO_SHOW=2, APPEND_ALL=3;
	
	public QueryInterpretation append(int what, int resId, String separator) {
		return append(what,App.self.getString(resId),separator);
	}
	
	public QueryInterpretation append(int what, String s, String separator) {
	  if (separator==null) separator="";
	  if ((what&TO_SHOW)!=0&&(toShow!=null||toSay==null)) toShow=(toShow==null)?s:(toShow+separator+s);
	  if ((what&TO_SAY)!=0) toSay=(toSay==null)?s:(toSay+separator+s);
	  return this;
	}
	
	public QueryInterpretation() {}
	public QueryInterpretation(String s) {
		toSay=toShow=s;
	}
	public QueryInterpretation(QueryInterpretation qi) {
		toSay=qi.toSay;
		toShow=qi.toShow;
	}
	
	@ML
	protected String toSay=null;
	@ML(attr="show")
	protected String toShow=null;
	public String getToSay() {
		return toSay;
	}
	public void setToSay(String toSay) {
		this.toSay = toSay;
	}
	public String getToShow() {
		return Utils.isEmpty(toShow)?getToSay():toShow;
	}
	public void setToShow(String toShow) {
		this.toShow = toShow;
	}
	
	public void set(String s) {
	   toSay=toShow=s;
	}
	
	private String  readableInterpretation(String s) {
		// replace exact coordinates
  		  return Utils.isEmpty(s)
  			  ?s
		      :s.replaceFirst("(\\(\\-*\\d+(\\.\\d*)*\\s*,\\s*\\-*\\d+(\\.\\d*)*\\))", "your location"); 
	}
	
	public void sayAndShow(Context ctx) {
		if (getToShow() != null)
			Output.sayAndShow(ctx, new MyTTS.Wrapper(getToShow()).setShowInASeparateBubble(), getToSay(), false);
	}
	
	@Override
	public String toString() {
		return getToSay();
	}	
	
	public void calculate() {
	  if (!(Utils.isEmpty(toShow)||Utils.isEmpty(toSay))) 
		  toShow=Utils.simpleReplaceAll(toShow,"${speak}",toSay).toString();
	  setToShow(readableInterpretation(toShow));
	}
}
