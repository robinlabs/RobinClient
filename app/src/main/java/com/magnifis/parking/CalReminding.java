package com.magnifis.parking;

import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.*;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import com.magnifis.parking.db.AndroidContentProvider;
import com.magnifis.parking.model.AndroidCalendar;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.text.format.Time;
import android.util.Pair;

public class CalReminding {
    final static String TAG = CalReminding.class.getSimpleName();
    final static int ERROR = -1, OK = 0, SUCH_EVENT_ALREADY_EXISTS = 1;

    public static final String EVENT_TIMEZONE = "eventTimezone";
    public static final String EVENT_END_TIMEZONE = "eventEndTimezone";
    public static final String AUTHORITY4 = "com.android.calendar";
    public static final Uri CONTENT_URI4 = Uri.parse("content://" + AUTHORITY4
            + "/events");

    public static class Reminder {
        protected int id;
        protected String description=null;
        protected String title=null;
        protected Date from=null, to=null;
        protected boolean allDay=false;


        public Date getTo() {
            return to;
        }
        public void setTo(Date to) {
            this.to = to;
        }
        public String getTitle() {
            return title;
        }
        public void setTitle(String title) {
            this.title = title;
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }
        public String getDescription() {
            return description;
        }
        public void setDescription(String description) {
            this.description = description;
        }
        public Date getFrom() {
            return from;
        }
        public void setFrom(Date from) {
            this.from = from;
        }
        public boolean isAllDay() {
            return allDay;
        }
        public void setAllDay(boolean allDay) {
            this.allDay = allDay;
        }
        public String toString() {
            String reminderText = "";
            if (!isAllDay()) {
                if (getFrom() != null) {
                    reminderText = Utils.formatMessageDate(getFrom()) + ". ";
                }
            }
            if (isEmpty(reminderText)) {
                Resources res=App.self.getResources();
                reminderText = res.getString(R.string.P_ALL_DAY_EVENT) + " ";
            }
            if (getTitle() != null) {
                reminderText = reminderText + getTitle();
            }
            return reminderText;
        }

    }



    public void sayRemindersForDay(Date day) {
        try {
            if (day==null) day=new Date();
            ArrayList<String> reminders = getRemindersStringsForDay(day);
            if (isEmpty(reminders))
                speakText(R.string.P_YOU_HAVENT_ANY);
            else
                for (String reminder : reminders)
                    MyTTS.speakText(reminder);


        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public static class CalReminderException extends java.lang.Exception {

        public CalReminderException() {
            super();
            // TODO Auto-generated constructor stub
        }

        public CalReminderException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            // TODO Auto-generated constructor stub
        }

        public CalReminderException(String detailMessage) {
            super(detailMessage);
            // TODO Auto-generated constructor stub
        }

        public CalReminderException(Throwable throwable) {
            super(throwable);
            // TODO Auto-generated constructor stub
        }

    }

    public ArrayList<String> getRemindersStringsForDay(Date day) throws CalReminderException {
        ArrayList<String> remindersStrings = new ArrayList<String>();
        ArrayList<Reminder> reminders = getRemindersForDay(day);
        if (!isEmpty(reminders))
            for (Reminder reminder : reminders) {
                String reminderText = reminder.toString();
                if (!isEmpty(reminderText)) {
                    remindersStrings.add(reminderText);
                }
            }
        return remindersStrings;
    }

    public  ArrayList<Reminder> getRemindersForDay(Date day) throws CalReminderException {
        Pair<Date,Date> from_to=dayStartEnd(day);
        ArrayList<Reminder> reminders = getRemindersForPeriod(
                /*Utils.isToday(day)?new Date():*/from_to.first,
                from_to.second
        );
        return reminders;
    }

    public ArrayList<Reminder> getRemindersForPeriod(Date from, Date to) throws CalReminderException {
        waitForPrepared();
        if (calendarUriBase==null) throw new CalReminderException("Can't get calendar base uri.");
        // we hope that "calendarUriBase" is known here.
        // get calendar;
        ArrayList<Reminder> reminders = new ArrayList<Reminder>();

        Date dayBegin=Utils.dayStartEnd(from).first, dayEnd=Utils.dayStartEnd(to).second;

        Cursor cc = cr.query(
                events_uri,
                null,
                null,// "deleted=0 and hasAlarm<>0",
                null,
                "dtstart"
        );
        if (cc!=null) try {
            if (cc.moveToFirst()) {
                calcIndexes(cc);
                do {

                    if (_ix_deleted!=null&&!cc.isNull(_ix_deleted)&&cc.getInt(_ix_deleted)!=0)
                        continue;

                    /*
                    if (_ix_hasAlarm!=null) {
                        if (cc.isNull(_ix_hasAlarm)||cc.getInt(_ix_hasAlarm)==0) continue;
                    }*/

                    Reminder reminder = rowToReminder(cc);

                    if (reminder.getFrom()==null) continue;

                    if (reminder.isAllDay()) {
                        if (
                                isAmong(reminder.getFrom(), dayBegin, dayEnd)
                                )
                            reminders.add(reminder);
                    } else {
                        if (reminder.getTo()==null) continue;
                        if (
                                isAmong(from, reminder.getFrom(), reminder.getTo())||
                                        isAmong(to, reminder.getFrom(), reminder.getTo())||
                                        isAmong(reminder.getFrom(), from, to)||
                                        isAmong(reminder.getTo(), from, to)
                                )
                            reminders.add(reminder);
                    }
                } while (cc.moveToNext());
            }
        } finally {
            cc.close();
        }

        return reminders;
    }

    /***********
     *
     private void createVersion67EventsTable(SQLiteDatabase db) {
     db.execSQL("CREATE TABLE Events (" +
     "_id INTEGER PRIMARY KEY," +
     "_sync_account TEXT," +
     "_sync_account_type TEXT," +
     "_sync_id TEXT," +
     "_sync_version TEXT," +
     "_sync_time TEXT," +            // UTC
     "_sync_local_id INTEGER," +
     "_sync_dirty INTEGER," +
     "_sync_mark INTEGER," + // To filter out new rows
     "calendar_id INTEGER NOT NULL," +
     "htmlUri TEXT," +
     "title TEXT," +
     "eventLocation TEXT," +
     "description TEXT," +
     "eventStatus INTEGER," +
     "selfAttendeeStatus INTEGER NOT NULL DEFAULT 0," +
     "commentsUri TEXT," +
     "dtstart INTEGER," +               // millis since epoch
     "dtend INTEGER," +                 // millis since epoch
     "eventTimezone TEXT," +         // timezone for event
     "duration TEXT," +
     "allDay INTEGER NOT NULL DEFAULT 0," +
     "visibility INTEGER NOT NULL DEFAULT 0," +
     "transparency INTEGER NOT NULL DEFAULT 0," +
     "hasAlarm INTEGER NOT NULL DEFAULT 0," +
     "hasExtendedProperties INTEGER NOT NULL DEFAULT 0," +
     "rrule TEXT," +
     "rdate TEXT," +
     "exrule TEXT," +
     "exdate TEXT," +
     "originalEvent TEXT," +  // _sync_id of recurring event
     "originalInstanceTime INTEGER," +  // millis since epoch
     "originalAllDay INTEGER," +
     "lastDate INTEGER," +               // millis since epoch
     "hasAttendeeData INTEGER NOT NULL DEFAULT 0," +
     "guestsCanModify INTEGER NOT NULL DEFAULT 0," +
     "guestsCanInviteOthers INTEGER NOT NULL DEFAULT 1," +
     "guestsCanSeeGuests INTEGER NOT NULL DEFAULT 1," +
     "organizer STRING," +
     "deleted INTEGER NOT NULL DEFAULT 0," +
     "dtstart2 INTEGER," + //millis since epoch, allDay events in local timezone
     "dtend2 INTEGER," + //millis since epoch, allDay events in local timezone
     "eventTimezone2 TEXT," + //timezone for event with allDay events in local timezone
     "syncAdapterData TEXT" + //available for use by sync adapters
     ");");
     }
     */

    private Integer
            _ix_id=null, _ix_description=null,
            _ix_dtstart=null, _ix_dtend=null,
            _ix_allDay=null, _ix_title=null,
            _ix_eventTimezone=null,
            _ix_visibility=null,
            _ix_hasAlarm=null,
            _ix_deleted=null
                    ;

    private void calcIndexes() {
        if (_ix_id==null&&events_uri!=null) try {
            Cursor cc = cr.query(
                    events_uri,
                    null,
                    "dtstart isnull",
                    null,
                    null
            );
            if (cr!=null) try {
                calcIndexes(cc);
            } finally {
                cc.close();
            }
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    private void calcIndexes(Cursor cc) {
        if (_ix_id==null) {
            _ix_id=Utils.indexOf(cc, "_id");
            _ix_description=Utils.indexOf(cc, "description");
            _ix_dtstart=Utils.indexOf(cc, "dtstart");
            _ix_dtend=Utils.indexOf(cc, "dtend");
            _ix_allDay=Utils.indexOf(cc, "allDay");
            _ix_title=Utils.indexOf(cc, "title");
            _ix_eventTimezone=Utils.indexOf(cc, "eventTimezone");
            _ix_visibility=Utils.indexOf(cc, "visibility");
            _ix_hasAlarm=Utils.indexOf(cc, "hasAlarm");
            _ix_deleted=Utils.indexOf(cc, "deleted");
        }
    }

    private  Reminder rowToReminder(Cursor cc){
        //  "_id","description","dtstart","allDay","title"
        Reminder reminder = new Reminder();
        reminder.setId(cc.getInt(_ix_id));
        reminder.setDescription(cc.getString(_ix_description));
        String tzId=_ix_eventTimezone==null?null:cc.getString(_ix_eventTimezone);

        if (!cc.isNull(_ix_allDay))
            reminder.setAllDay(cc.getInt(_ix_allDay)==1);

        if (reminder.isAllDay()) {
            if (!cc.isNull(_ix_dtstart))
                reminder.setFrom(new Date(Utils.timeFromGMT(cc.getLong(_ix_dtstart))));
            if (!cc.isNull(_ix_dtend))
                reminder.setTo(new Date(Utils.timeFromGMT(cc.getLong(_ix_dtend))));
        } else {
            if (!cc.isNull(_ix_dtstart))
                reminder.setFrom(Utils.costructForTimezone(cc.getLong(_ix_dtstart),tzId));
            if (!cc.isNull(_ix_dtend))
                reminder.setTo(Utils.costructForTimezone(cc.getLong(_ix_dtend),tzId));
        }

        reminder.setTitle(cc.getString(_ix_title));
        return reminder;
    }


    private static int setCalendarEventICS(
        Context ctx, 
        Date when, 
        String dsc, 
        String location,
        boolean allDay
   ) {

        try {
            long eventTime = when.getTime() ;

            long endtime=allDay?Utils.dayStartEnd(when).second.getTime():(eventTime+1000l);


            Intent intent = new Intent(Intent.ACTION_INSERT)
                    .setType("vnd.android.cursor.item/event")
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, eventTime)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endtime)
                    .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY , allDay) // just included for completeness
                    .putExtra(Events.TITLE, dsc)
                            //.putExtra(Events.DESCRIPTION, vl_hldr[2])
                            //.putExtra(Events.EVENT_LOCATION, vl_hldr[1])
                            // .putExtra(Events.RRULE, "FREQ=DAILY;COUNT=10")
                    .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
                    .putExtra(Events.ACCESS_LEVEL, Events.ACCESS_PRIVATE)
                    .putExtra(Events.ALLOWED_REMINDERS, "METHOD_DEFAULT")
                    .putExtra(Intent.EXTRA_EMAIL, "");
            
            if (!Utils.isEmpty(location)) intent.putExtra("eventLocation", location);

            Launchers._startNestedActivity(ctx, intent);

            return OK;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return ERROR;
    }

    public int  doesEventExist(
            Date when,
            String dsc,
            boolean allDay
    ) {

        if (calendarUriBase==null) return ERROR;


        long et=when.getTime();

        if (allDay) et=Utils.timeToGMT(et);

        return _doesEventExist(
                et,
                dsc,
                allDay)?SUCH_EVENT_ALREADY_EXISTS:OK;
    }

    private final static String
            Q_ALLDAYEVENT_EXISTS0="(dtstart>=?)  and (dtstart<=?) and (title=?) and (allDay=1)",
            Q_ALLDAYEVENT_EXISTS1="(dtstart>=?)  and (dtstart<=?) and (title=?) and (allDay=1) and (deleted=0)",
            Q_EVENT_EXISTS0="(dtstart=?) and (title=?)  and (allDay=0)",
            Q_EVENT_EXISTS1="(dtstart=?) and (title=?)  and (allDay=0) and (deleted=0)"
                    ;


    public  boolean _doesEventExist(
            long eventTime,
            String dsc,
            boolean allDay
    ) {

        calcIndexes();
        Cursor cc = null;
        if (allDay) {
            Pair<Date,Date> se=Utils.dayStartEnd(eventTime);

            cc=cr.query(euri, new String[] { "calendar_id" },
                    (_ix_deleted==null)?Q_ALLDAYEVENT_EXISTS0:Q_ALLDAYEVENT_EXISTS1,
                    new String[] { Long.toString(se.first.getTime()),  Long.toString(se.second.getTime()) , dsc }, null);

        } else {
            cc=cr.query(euri, new String[] { "calendar_id" },
                    (_ix_deleted==null)?Q_EVENT_EXISTS0:Q_EVENT_EXISTS1,
                    new String[] { Long.toString(eventTime), dsc }, null);
        }

        if (cc!=null) try {
            if (cc.moveToFirst())
                return true;
        } finally {
            cc.close();
        }

        return false;
    }

	/*
	 * from {@link
	 * http://stackoverflow.com/questions/5976098/how-to-set-reminder
	 * -in-android}
	 */

    public int set(Context ctx, Date when, String dsc, String location, boolean allDay) {

        waitForPrepared();

        if (calendarUriBase==null) return setCalendarEventICS(ctx, when, dsc, location, allDay);

        Pair<Date,Date> dStartEnd=null;

        if (allDay) {
            dStartEnd=Utils.dayStartEnd(when);
            when=dStartEnd.first;
        }

        // we hope that "calendarUriBase" is known here.
        // get calendar

        TimeZone tz=TimeZone.getDefault();

        long eventTime = when.getTime() ;

        long et=eventTime;

        if (allDay) et=Utils.timeToGMT(eventTime);


        Log.d(TAG, when.toString());


        if (_doesEventExist(et,dsc,allDay)) return SUCH_EVENT_ALREADY_EXISTS;

        long endtime=allDay?Utils.timeToGMT(dStartEnd.second.getTime()):(et+1000l);

        // event insert
        ContentValues values = new ContentValues();
        Uri event = null;


        values.put("dtstart", et);
        values.put("dtend", endtime);
        values.put("title", dsc);
        values.put("description", /*dsc*/"");
        values.put("calendar_id", getDefaultCalendarId());
        values.put("allDay",allDay?1:0);
        if (location!=null) values.put("eventLocation", location);


        if (Utils.isAndroid4orAbove) {
            values.put(EVENT_TIMEZONE, allDay ?"utc":tz.getID());
            try {
                event = cr.insert(CONTENT_URI4, values);
            } catch (Throwable t) {
                Log.d(TAG, " -- ", t);
            }
        } else {

            values.put("hasAlarm", 1);
            values.put("visibility", 0);
            try {
                event = cr.insert(events_uri, values);
            } catch (Throwable t) {
                Log.d(TAG, " -- ", t);
            }
        }

        boolean reminderIsSet=false;
        if (event==null) {
            // TODO -- add rallback
        } else try {
            // reminder insert
            Uri REMINDERS_URI = Uri.parse(calendarUriBase + "reminders");
            values = new ContentValues();
            values.put("event_id", Long.parseLong(event.getLastPathSegment()));
            values.put("method", 1);
            values.put("minutes", 10);
            cr.insert(REMINDERS_URI, values);
            reminderIsSet=true;
        } catch (Throwable t) {
            t.printStackTrace();
        }

////		Intent intent = new Intent(Intent.ACTION_INSERT).setData(Uri.parse(REMINDERS_URI.toString()));
////		intent.putExtra("event_id", Long.parseLong(event.getLastPathSegment()));
////		intent.putExtra("method", 1);
////		intent.putExtra("minutes", 10);
////		intent.putExtra("beginTime", eventTime); 
////		intent.putExtra("endTime", eventTime + 10000L); 
////		intent.putExtra("title", dsc); 
////		intent.putExtra("description", "Robin's reminder"); 
////		
////		Launchers.startNestedActivity(intent);

        if (Utils.isAndroid4orAbove&&!(reminderIsSet &&_doesEventExist(et,dsc, allDay))){
            return setCalendarEventICS(ctx, when, dsc, location, allDay);
        }


        if(reminderIsSet){
            Intent openCalendar = new Intent(Intent.ACTION_VIEW);
            openCalendar.setType("vnd.android.cursor.item/event");
            openCalendar.setData(event);
            startActivityFromNowhere(openCalendar);
        }


        return reminderIsSet?OK:ERROR;

    }

    public static String calendarUri() {
        ContentResolver cr=App.self.getContentResolver();

        String calendarUriBase = null;

        Uri calendars = Uri.parse("content://calendar/calendars");
        Cursor cursor = null;

        try {

            try {
                cursor = cr.query(calendars, null, null, null, null);
            } catch (java.lang.Exception e) {}

            if (cursor != null) {
                calendarUriBase = "content://calendar/";
            } else {
                calendars = Uri.parse("content://com.android.calendar/calendars");
                try {
                    cursor = cr.query(calendars, null, null, null,
                            null);
                } catch (java.lang.Exception e) {
                }
                if (cursor != null) {
                    calendarUriBase = "content://com.android.calendar/";
                }
            }

        } finally {
            if (cursor!=null) cursor.close();
        }

        return calendarUriBase;
    }

    private ContentResolver cr=App.self.getContentResolver();
    private String calendarUriBase=null;
    private Uri events_uri=null, euri=null;
    private boolean _ready=false;
    private static Thread preparer=null;

    private void waitForPrepared() {
        synchronized(CalReminding.class) {
            if (!_ready)
                try {
                    CalReminding.class.wait();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
    }

    protected List<AndroidCalendar>  availableCalendars=null;

    public int getDefaultCalendarId() {
        AndroidCalendar defcal=getDefaultCalendar();
        return defcal==null?1:defcal.getId();
    }

    public AndroidCalendar getDefaultCalendar() {
        if (!isEmpty(availableCalendars)) {
            String gm=App.self.getGmailAccountName();
            AndroidCalendar defcal=null;
            for (AndroidCalendar cal:availableCalendars) {
                if (cal.isGoogle()) {
                    if (defcal!=null) {
                        if (isEmpty(gm)||
                                !gm.equals(cal.getName())
                                ) continue;
                    }
                    defcal=cal;
                }
            }
            return defcal;
        }
        return null;
    }

    public  List<AndroidCalendar>  getAvailableCalendars() {
        return availableCalendars;
    }

    private void prepare() {
        synchronized(CalReminding.class) {
            if (!_ready&&(preparer==null||!preparer.isAlive())) {
                preparer=new Thread() {
                    public void run() {
                        calendarUriBase=calendarUri();
                        if (calendarUriBase!=null) {
                            events_uri=Uri.parse(calendarUriBase + "events");
                            euri=Utils.isAndroid4orAbove?CONTENT_URI4:events_uri;
                            try {
                                Uri u=Uri.parse(calendarUriBase).buildUpon().appendPath("calendars").build();

                                List<AndroidCalendar>  cals=AndroidContentProvider.get(
                                        u, AndroidCalendar.class, null, null, "_id asc"
                                );

                                if (!isEmpty(cals)) {
                                    List<AndroidCalendar>  avlCals=new ArrayList<AndroidCalendar>();
                                    for (AndroidCalendar cal:cals) if (cal.isVisible()&&!cal.isDeleted()) {
                                        avlCals.add(cal);
                                    }
                                    if (!isEmpty(avlCals)) availableCalendars=avlCals;
                                }
                            } catch(Throwable t) {
                                t.printStackTrace();
                            }
                        }
                        synchronized(CalReminding.class) {
                            _ready=true;
                            preparer=null;
                            CalReminding.class.notify();
                        }
                    }
                };
                preparer.start();
            }
        }
    }

    private static WeakReference<CalReminding> selfWr=null;

    public static CalReminding getInstance() {
        synchronized(CalReminding.class.getCanonicalName()) {
            CalReminding r=(selfWr==null)?null:selfWr.get();
            return r==null?new CalReminding():r;
        }
    }

    private CalReminding() {
        selfWr=new WeakReference<CalReminding>(this);
        prepare();
    }
}
