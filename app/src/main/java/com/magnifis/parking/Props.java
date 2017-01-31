package com.magnifis.parking;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import android.content.Context;


public class Props extends Properties {
	final static String TAG="Props";
	
	private File propFile=null;
	
	public Props(File pfile) 
			throws InvalidPropertiesFormatException, FileNotFoundException, IOException 
	{
		propFile=pfile;
		if (pfile.exists()) try {
			loadFromXML(new FileInputStream(pfile));
		} catch(Throwable t) {
			Log.e(Props.class.getCanonicalName(), " -- ",t);
		}
	}
	
	public void save() throws FileNotFoundException, IOException {
		storeToXML(new FileOutputStream(propFile), null);
	}
	
	
	private static Props _props=null;
	
	public static Props getInstance(Context ctx) {
		synchronized(Props.class) {
			if (_props==null) try {
				_props=new Props(new File(ctx.getFilesDir(),"app.properties"));
			} catch(Throwable t) {
				Log.e(TAG,t.getMessage(),t);
			}
		}
		return _props;
	}	
	
	public void setAndSave(String propName, String propValue ) {
		setProperty(propName, propValue);
		try {
			save();
		} catch (Throwable e) {
			e.printStackTrace();
		}		
	}
};