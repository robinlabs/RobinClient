package com.magnifis.parking.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateClassifier {
	
	public final static int
	      DATE_IS_OTHER=0, DATE_IS_TODAY=1, DATE_IS_YESTERDAY=2;
	
	private Calendar cal;
    private Date todayStart, yesterdayStart;
	
	public DateClassifier() {
		cal = new GregorianCalendar();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		todayStart=new Date(cal.getTime().getTime());
		cal.roll(Calendar.DAY_OF_MONTH, false);
		yesterdayStart=new Date(cal.getTime().getTime());

	}
	
	public int classify(Date dat) {
		boolean 
		  today=dat.after(todayStart)||dat.equals(todayStart),
		  yesterday=dat.after(yesterdayStart)||dat.equals(yesterdayStart);
		
		if (yesterday&&!today) return DATE_IS_YESTERDAY;
		if (today) return DATE_IS_TODAY;
		
		return DATE_IS_OTHER;
	}
	
	public String formatDatePart(Date d, DateFormat df) {
    	switch (classify(d)) {
    	case DATE_IS_TODAY: return ""; 
    	case DATE_IS_YESTERDAY: return "Yesterday";
    	}
    	return df.format(d);
    }
}
