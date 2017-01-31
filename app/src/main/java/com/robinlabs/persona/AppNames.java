package com.robinlabs.persona;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.magnifis.parking.App;
import com.magnifis.parking.Log;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by oded on 5/18/15.
 */
public class AppNames {
	
	private static AppNames instance = new AppNames(); 
	JSONObject jsonApps = null; 
	
	private AppNames() {
	}
	
	public static AppNames getInstance () {
		return instance; 
	}
	
    public void publish(final Context context) {
    	
    	Runnable bgSync = new Runnable() {
			@Override
			public void run() {
				 try {
			            new PostJSON("http://personacache.appspot.com/apps", getApps(context)) {
			                @Override
			                public void onResponse(HttpResponse httpResponse, JSONObject jsonResponse) {
			                    //do nothing
			                }
			            };
			        } catch (UnsupportedEncodingException e) {
			            throw new RuntimeException(e);
			        } catch (JSONException e) {
			            throw new RuntimeException(e);
			     }
			}
		};
		bgSync.run();
    }

    public JSONObject getApps(Context context) throws JSONException {
    	
    	if (jsonApps != null) { // lazy evaluation 
    		return jsonApps; 
    	}
    	
    	
        JSONObject jsonWrapper = new JSONObject();
        JSONArray jsonAppsArray = new JSONArray();
        JSONArray jsonContactsArray = new JSONArray();
        JSONArray jsonSongsArray = new JSONArray();
        JSONArray jsonCurContextListArray = new JSONArray();
        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(
                PackageManager.GET_UNINSTALLED_PACKAGES |
                        PackageManager.GET_DISABLED_COMPONENTS
        );

        for (ApplicationInfo app : apps) {
            JSONObject jsonObject = new JSONObject();
            String label = pm.getApplicationLabel(app).toString();
            jsonObject.put(label, app.packageName);
            jsonAppsArray.put(jsonObject);
        }

        try {
        	 String userId = App.self.android_id;
             jsonWrapper.put("user_id", userId);
  
            jsonWrapper.put("contact", jsonContactsArray);
            jsonWrapper.put("song", jsonSongsArray);
            jsonWrapper.put("context_list", jsonCurContextListArray);
            jsonWrapper.put("app", jsonAppsArray);
          
        } catch (JSONException e) {
            throw new RuntimeException();
        }

        Log.d("json_print", jsonWrapper.toString());
        return (jsonApps = jsonWrapper);
    }

}
