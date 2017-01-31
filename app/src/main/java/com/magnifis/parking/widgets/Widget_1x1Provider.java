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
import com.magnifis.parking.R;
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

public class Widget_1x1Provider extends AppWidgetProvider {
	private final static String TAG=Widget_1x1Provider.class.getSimpleName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG,"onUpdate");
		/*
		if (!(widget_visible||
		  isForegroundActivity(App.self, MainActivity.class.getCanonicalName())))
			widget_visible=true;
			*/
		updateWidgets();
	}
	
	/**
	 * init onClick event only once
	 * @param packageName
	 * @param remoteViews
	 */
	private static void initClickEvent(Context context, RemoteViews remoteViews) {
		Intent it = new Intent();
		it.setClass(context, ListenAndLaunchActivity.class);
		PendingIntent pi = PendingIntent.getActivity(context, 0, it, 0);
		remoteViews.setOnClickPendingIntent(R.id.WidgetButton, pi);
	}
	
	private static void updateWidgets() {
		Log.d(TAG,"updateWidgets: "/*+widget_visible*/);
		AppWidgetManager wm = AppWidgetManager.getInstance(App.self);
		ComponentName cn=new ComponentName(App.self,Widget_1x1Provider.class);
		int ids[]=wm.getAppWidgetIds(cn);
		if (!isEmpty(ids)) {
			for (int id:ids) try {
				//AppWidgetProviderInfo wi=wm.getAppWidgetInfo(id);
				RemoteViews rws=new RemoteViews(App.class.getPackage().getName()  , R.layout.widget_1x1);
				initClickEvent(App.self,rws);
				wm.updateAppWidget(id, rws);
			} catch(Throwable t) {
			  t.printStackTrace();
			}
		}		
	}
	
}
