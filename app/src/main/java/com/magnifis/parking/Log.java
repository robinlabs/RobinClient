package com.magnifis.parking;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.magnifis.parking.utils.Utils;

public class Log {
	public static final boolean LOG = true;
	
	public static final String tagFilter[]={/*"Understanding","Mag"*/};

	public static void i(String tag, String string) {
	    if (LOG) android.util.Log.i(tag, string);
	}
	public static void e(String tag, String string) {
	    android.util.Log.e(tag, string);
	}
	public static void e(String tag, String string,Throwable t) {
	    android.util.Log.e(tag, string,t);
	}
	public static void d(String tag, String string) {
	    if (LOG) {
	    	if (!Utils.isEmpty(tagFilter)) {
	    		boolean found=false;
	    		for (String s:tagFilter) if (found=tag.contains(s)) break;
	    		if (!found) return;
	    	}
	    	android.util.Log.d(tag, string);
	    }
	}
	public static void d(String tag, String string,Throwable t) {
	    if (LOG) android.util.Log.d(tag, string,t);
	}
	public static void v(String tag, String string) {
	    if (LOG) android.util.Log.v(tag, string);
	}
	public static void w(String tag, String string) {
	    if (LOG) android.util.Log.w(tag, string);
	}
	
	   private static class CollectLogTask extends AsyncTask<ArrayList<String>, Void, StringBuilder>{
	   
	       
	        @Override
	        protected StringBuilder doInBackground(ArrayList<String>... params){
	            final StringBuilder log = new StringBuilder();
	            try{
	                ArrayList<String> commandLine = new ArrayList<String>();
	                commandLine.add("logcat");//$NON-NLS-1$
	                commandLine.add("-d");//$NON-NLS-1$
	                ArrayList<String> arguments = ((params != null) && (params.length > 0)) ? params[0] : null;
	                if (null != arguments){
	                    commandLine.addAll(arguments);
	                }
	               
	                Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[0]));
	                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	               
	                String line;
	                while ((line = bufferedReader.readLine()) != null){
	                    log.append(line);
	                    log.append('\n');
	                }
	            }
	            catch (IOException e){
	                Log.e("Log", "CollectLogTask.doInBackground failed", e);//$NON-NLS-1$
	            }

	            return log;
	        }

	        @Override
	        protected void onPostExecute(StringBuilder log){
	        	//Launchers.composeFeedback(log);
	        }
	    }

	
	public static void collectLogThenComposeFeedback0() {
		new CollectLogTask().execute();
	}
	
	public static void collectLogThenComposeFeedback(Context ctx) {
		
		if (true) { 
		  Launchers.composeFeedback(ctx);
		  return;
		}
		
		try {
			File f=new File(Environment.getExternalStorageDirectory()+
					  "/.MagnifisRobin/log.txt"); 
					  ;
			
			Process p=
			  new ProcessBuilder()
			   .command("logcat","-d","-f",f.getCanonicalPath())
			   .redirectErrorStream(true)
			   .start();
			
			Launchers.composeFeedback(ctx,null,f);
			
            /*
		    InputStream is=p.getInputStream();
		    if (is!=null) try {
		    	
                ByteArrayOutputStream bais=new ByteArrayOutputStream();
                byte buf[]=new byte[1024];
                for (;;) {
		          if (is.available()<=0) {
		        	for (int attempts=3;attempts>0;attempts--) {
		        	  	Thread.sleep(10);
		        	  	if (is.available()>0) break;
		        	}
		          }
		          int k=is.read(buf);
		          if (k<=0) break;
		          bais.write(buf, 0, k);
		    	}
	            bais.close();
		    	Launchers.composeFeedback(bais.toString());

		    	return;
		    } finally {
		    	is.close();
		    }*/
		    
		    
		} catch (Throwable e) {
			e.printStackTrace();
		} 
		Launchers.composeFeedback(ctx,null,null);
	}
}
