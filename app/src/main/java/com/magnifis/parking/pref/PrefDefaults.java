package com.magnifis.parking.pref;

import java.io.IOException;
import java.util.Hashtable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;

import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.magnifis.parking.utils.Utils;

public class PrefDefaults {
	
   final static String TAG=PrefDefaults.class.getSimpleName();
	
   public <T> T getDefaultValue(int key, Class<T> c) {
	   return getDefaultValue(App.self.getString(key),c);
   }
	
   public <T> T getDefaultValue(String key, Class<T> c) {
	   String v=tod.get(key);
	   if (v!=null) {
	     if (c==Boolean.class||c==boolean.class) {
	    	 return (T)(Boolean)Boolean.parseBoolean(v);
	     } 
	     if (c==Integer.class||c==int.class) {
	    	 return (T)(Integer)Integer.parseInt(v);
	     }
	   }
	   return (T)v;
   }
   
   public String getStringDefault(String key) {
	   return tod.get(key);
   }
   
   public String getStringDefault(int key) {
	   return tod.get(App.self.getString(key));
   }
   
   public int getIntDefault(int key) {
	   return getIntDefault(App.self.getString(key));
   }
   
   public int getIntDefault(String key) {
	 String v=tod.get(key);
	 return v==null?-1:Integer.parseInt(v);	   
   }
   
   public boolean getBooleanDefault(String key) {
	   String v=tod.get(key);
	   boolean bv=v==null?false:Boolean.parseBoolean(v);
	   return bv;
   }
   
   public boolean getBooleanDefault(int key) {
	   return getBooleanDefault(App.self.getString(key));
   }
	
   final static String android="http://schemas.android.com/apk/res/android";
	
   private Hashtable<String,String> tod=new Hashtable<String,String>(); 
   
   public static String resolveRef(String v) {
	  if (v!=null&&v.length()>1&&v.charAt(0)=='@') {
		 try {
		   return App.self.getString(Integer.parseInt(v.substring(1)));
		 } catch(Throwable t) {}
	  } 
	  return v;
   }
   
   public PrefDefaults(int id) {
	   XmlResourceParser  xml=App.self.getResources().getXml(id);
	   try {
		   xml.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES , true);
		   int et=xml.getEventType();
		   while (et!=XmlPullParser.END_DOCUMENT) {
			   if (et==XmlPullParser.START_TAG) {
				   String key=resolveRef(xml.getAttributeValue(android, "key"));
				   String def=resolveRef(xml.getAttributeValue(android, "defaultValue"));
				   if (def!=null&&key!=null) {
					   tod.put(key, def);
				   }
			   }
			   et=xml.next();
		   }
	   } catch (XmlPullParserException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   } catch (IOException e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   } finally {
		   xml.close();
	   }
	 //  inheritDefaultsFromPrevVersion();
   }

   private static String inherited_boolean_prefs[][]={
   };
   
   private void inheritDefaultsFromPrevVersion() {
	   SharedPreferences prefs=App.self.getPrefs();
	   Editor ed=null;
	   for (String ss[]: inherited_boolean_prefs) if (Utils.isBooleanPrefNotSet(ss[0])&&Utils.isBooleanPrefSet(ss[1])) {
		  Log.d(TAG,"p1");
		  boolean d=getBooleanDefault(ss[0]),old=prefs.getBoolean(ss[1], d);
		  if (old!=d) {
			Log.d(TAG,"p2");
			if (ed==null) ed=prefs.edit();
			ed.putBoolean(ss[0], old);
		     //tod.put(ss[0], Boolean.toString(prefs.getBoolean(ss[1], old)));
		  }
	   }
	   if (ed!=null) ed.commit();
   }
   
}
