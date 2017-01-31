package com.magnifis.parking.widgets;

import static com.magnifis.parking.utils.Utils.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.magnifis.parking.App;
import com.magnifis.parking.ListenAndLaunchActivity;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.R;
import com.magnifis.parking.RequestFormers;
import com.magnifis.parking.XMLFetcher;
import com.magnifis.parking.Xml;
import com.magnifis.parking.R.id;
import com.magnifis.parking.R.layout;
import com.magnifis.parking.model.MagReply;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.utils.StateStore;
import com.magnifis.parking.utils.Utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

public class WidgetProvider extends AppWidgetProvider {
	private final static String TAG=WidgetProvider.class.getSimpleName();
	
	// TODO: change the server and remove this
	final static int MAX_TEASER_CHARS = 45; 
	static final Pattern preamblePattern = Pattern.compile(
			"^(Joke of the day: )"
			//"^(Here is one...|Let me tell you a joke...|How about this one...|Here comes a joke...|Hmm...|I know one...|Here is a good one...|Listen to this...|Ok, just for you...)"
			, Pattern.CASE_INSENSITIVE);
	
	public static void infromServerOnTheTextClick() {
		queryDataUpdate(false); // this is not a right way to do it, should be replaced
	} 
	
	private static void queryDataUpdate(final boolean fAuto) {
       try {
		  new XMLFetcher<Understanding>() {
			
			@Override
			protected InputStream invokeRequest(URL u, String pd, String ref, String userAgent)throws IOException {
				return super.invokeRequest( 
						  RequestFormers.createMagnifisUnderstandingRqUrl(
								  null,
									Arrays.asList(new String[] {
										fAuto?"daily joke widget":"daily joke"	
									})
								  ),						
						pd, ref, userAgent);
			}

			@SuppressLint("NewApi") protected Understanding consumeXmlData(Element root) {
				if (root!=null) try {
				    Understanding u=Xml.setPropertiesFrom(root, Understanding.class);
					if (u!=null&&!u.isError()) {
						QueryInterpretation qi= u.getQueryInterpretation();
						if (qi!=null) {
							String lastText= getJokeTeaser(qi.getToSay(), false);
							if (!isEmpty(lastText)) {
							    Log.d(TAG, lastText);
							    try {
							      App.self.getWidgetSS().put(u);
							      if (fAuto) updateWidgets();
							    } catch(Throwable t) {
							    	t.printStackTrace();
							    }
							}
						}
					}
				} catch (Throwable e) {
				   e.printStackTrace();
			    }
				return null;
		    }
		  }.execute(  
			  null,
			  null,
			  null
		  );
	   } catch (Throwable e) {
		  e.printStackTrace();
	   };
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG,"onUpdate");
		/*
		if (!(widget_visible||
		  isForegroundActivity(App.self, MainActivity.class.getCanonicalName())))
			widget_visible=true;
			*/
		updateWidgets();
		queryDataUpdate(true);
	}
	
	/**
	 * init onClick event only once
	 * @param packageName
	 * @param remoteViews
	 */
	private static void initClickEvent(Context context, RemoteViews remoteViews) {
		Intent it = new Intent();
		it.setClass(context, ListenAndLaunchActivity.class);
		it.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		PendingIntent pi = PendingIntent.getActivity(context, 0, it, 0);
		remoteViews.setOnClickPendingIntent(R.id.WidgetButton, pi);
	}
	
	private static void initTextClick(RemoteViews remoteViews, Understanding u) {
		Intent it = new Intent(MainActivity.INTERPRET_UNDERSTANDING);
		it.setClass(App.self, MainActivity.class);
		if (u!=null) it.putExtra(MainActivity.EXTRA_UNDERSTANDING, u);
		it.putExtra(MainActivity.EXTRA_FOLLOWUP_REQUEST, true); 
		PendingIntent pi = PendingIntent.getActivity(App.self, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.JokeArea, pi);		
	}
	
	private static void updateWidgets() {
		Log.d(TAG,"updateWidgets: "/*+widget_visible*/);
		AppWidgetManager wm = AppWidgetManager.getInstance(App.self);
		ComponentName cn=new ComponentName(App.self,WidgetProvider.class);
		int ids[]=wm.getAppWidgetIds(cn);
		if (!isEmpty(ids)) {
			Understanding u=null;
			try {
			  u=App.self.getWidgetSS().get();
			} catch(Throwable t) {
				t.printStackTrace();
			}
			if (u!=null) {
				Log.d(TAG, "u!=null");
			}
			for (int id:ids) try {
				//AppWidgetProviderInfo wi=wm.getAppWidgetInfo(id);
				RemoteViews rws=new RemoteViews(App.class.getPackage().getName()  , R.layout.widget_w_joke);
				initClickEvent(App.self,rws);
		//		rws.setInt(R.id.WidgetLayout, "setVisibility", widget_visible?View.VISIBLE:View.GONE);
				
				if (u!=null) {
					Log.d(TAG, "u!=null xxxxx");
			        rws.setTextViewText(R.id.WidgetText, 
			        				getJokeTeaser(u.getQueryInterpretation().getToSay(), 
			        				true));
				}
		        initTextClick(rws,u);	
				/*
				if (isAndroid3orAbove)
					wm.partiallyUpdateAppWidget(id, rws);
				else*/
					wm.updateAppWidget(id, rws);
			} catch(Throwable t) {
			  t.printStackTrace();
			}
		}		
	}

	
	static String getJokeTeaser(String fullJoke, boolean doTruncate) {
		
		String teaser = preamblePattern.matcher(fullJoke).replaceAll(""); // split(fullJoke); 
		if ( Utils.isEmpty(teaser))
			return null; 
		
		if (doTruncate) {
			int iEnd = 0; 
			int length = Math.min(MAX_TEASER_CHARS, teaser.length()); 
			for (int i = 0; i < length; i++) {
				char ch = teaser.charAt(i); 
				if (' ' == ch || '\t' == ch || '\n' == ch || '-' == ch || ',' == ch 
						|| '.' == ch || ':' == ch || ';' == ch || '&' == ch)
					iEnd = i; 
			}
			
			teaser =  App.self.getString(R.string.M_widget_header) + "\n" + teaser.substring(0, iEnd) + " ..."; 
		}
		
		return teaser;
		
	
	}
	
}
