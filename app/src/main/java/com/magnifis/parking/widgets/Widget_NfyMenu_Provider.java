package com.magnifis.parking.widgets;

import com.magnifis.parking.Log;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;

public class Widget_NfyMenu_Provider extends AppWidgetProvider {
	private final static String TAG=Widget_NfyMenu_Provider.class.getSimpleName();

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
	
	public static void updateWidgets() {
        /*
		AppWidgetManager wm = AppWidgetManager.getInstance(App.self);
		ComponentName cn=new ComponentName(App.self,Widget_NfyMenu_Provider.class);
		int ids[]=wm.getAppWidgetIds(cn);
		if (!isEmpty(ids)) {
			for (int id:ids) try {
				//AppWidgetProviderInfo wi=wm.getAppWidgetInfo(id);
				RemoteViews rws=new RemoteViews(App.class.getPackage().getName()  , R.layout.widget_nfy_menu);
		     	Tray.updateViewsState(rws);
		     	Tray.bindToggles(rws);
				wm.updateAppWidget(id, rws);
			} catch(Throwable t) {
			  t.printStackTrace();
			}
		}
				*/
	}
	
}
