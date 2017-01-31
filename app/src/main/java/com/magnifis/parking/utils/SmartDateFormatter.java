package com.magnifis.parking.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmartDateFormatter {
	
	public SmartDateFormatter() {
		this(new DateClassifier());
	}
	
	public SmartDateFormatter(DateClassifier dc) {
		classifier=dc;
		Locale loc=new Locale("en");
	    dfOther=new SimpleDateFormat("EEEE, d MMMM", loc); //zb MMMMM
		dfTime=new SimpleDateFormat("h:mm a", loc);
	}
	
	protected DateClassifier classifier;

	public DateClassifier getClassifier() {
		return classifier;
	}

	public void setClassifier(DateClassifier classifier) {
		this.classifier = classifier;
	}
	
	final protected  DateFormat dfOther,dfTime;
	
	public String format(Date d) {
		return classifier.formatDatePart(d, dfOther)+
		" at "+
		dfTime.format(d);
	}
}
