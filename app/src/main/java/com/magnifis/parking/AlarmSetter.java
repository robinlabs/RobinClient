package com.magnifis.parking;

import java.util.Calendar;
import java.util.Date;
import static com.magnifis.parking.utils.Utils.*;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.AlarmClock;

public class AlarmSetter {

	final static String TAG = AlarmSetter.class.getSimpleName();
	final static int ERROR = -1, OK = 0, SUCH_EVENT_ALREADY_EXISTS = 1;
	
/***********
 * 
 * 
 * 	
 public void  [More ...] onCreate(SQLiteDatabase db) {

42

        db.execSQL("CREATE TABLE alarms (" +

43

                   "_id INTEGER PRIMARY KEY," +

44

                   "hour INTEGER, " +

45

                   "minutes INTEGER, " +

46

                   "daysofweek INTEGER, " +

47

                   "alarmtime INTEGER, " +

48

                   "enabled INTEGER, " +

49

                   "vibrate INTEGER, " +

50

                   "message TEXT, " +

51

                   "alert TEXT);");

52

53

        // insert default alarms

54

        String insertMe = "INSERT INTO alarms " +

55

                "(hour, minutes, daysofweek, alarmtime, enabled, vibrate, " +

56

                " message, alert) VALUES ";

57

        db.execSQL(insertMe + "(8, 30, 31, 0, 0, 1, '', '');");

58

        db.execSQL(insertMe + "(9, 00, 96, 0, 0, 1, '', '');");

59

    }
    
  static {
         sURLMatcher.addURI("com.android.deskclock", "alarm", ALARMS);
         sURLMatcher.addURI("com.android.deskclock", "alarm/#", ALARMS_ID);
     }   
 */
	
	public static int set(Context ctx, Date timedate, String description) {
		Calendar cal=Calendar.getInstance();
        cal.setTime(timedate);
	
 	    Intent it = new Intent(ACTION_SET_ALARM);
        it.putExtra(EXTRA_HOUR, cal.get(Calendar.HOUR_OF_DAY));
	    it.putExtra(EXTRA_MINUTES,  cal.get(Calendar.MINUTE));
	    it.putExtra(EXTRA_SKIP_UI, true);
	    it.putExtra(EXTRA_MESSAGE, isEmpty(description)?"":description);
	    
	    try {
          Launchers._startNestedActivity(ctx, it);
          return OK;
	    } catch(Throwable t) {}
/*
 // content://com.htc.android.alarmclock/alarm 
  
 		MainActivity act = MainActivity.get();

		Uri alarms = Uri.parse("content://com.android.alarmclock/alarm");
		Cursor managedCursor = null;
		try {
			managedCursor = act.managedQuery(alarms, null, null, null, null);
			Log.d(TAG, "via "+alarms.toString());
		} catch (Exception e) {
		}
		if (managedCursor==null) {
			alarms = Uri.parse("content://com.android.deskclock/alarm");
			try {
				managedCursor = act.managedQuery(alarms, null, null, null, null);
				Log.d(TAG, "via "+alarms.toString());
			} catch (Exception e) {
			}
		}
	    if (managedCursor!=null) {
	    	Log.d(TAG, "ok ");
	    }		
	*/	
		
		return ERROR;
	}
	
	public static final String ACTION_SET_ALARM = "android.intent.action.SET_ALARM";
	public static final String EXTRA_MESSAGE = "android.intent.extra.alarm.MESSAGE";
	public static final String EXTRA_HOUR = "android.intent.extra.alarm.HOUR";
	public static final String EXTRA_MINUTES = "android.intent.extra.alarm.MINUTES";
	public static final String EXTRA_SKIP_UI = "android.intent.extra.alarm.SKIP_UI";
}
