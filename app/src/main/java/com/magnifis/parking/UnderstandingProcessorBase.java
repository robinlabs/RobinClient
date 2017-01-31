package com.magnifis.parking;

import static com.magnifis.parking.RequestFormers.createPoiRequest;
import static com.magnifis.parking.RequestFormers.createTrafficOrNewsRequest;
import static com.magnifis.parking.RequestFormers.createWeatherRequest;
import static com.magnifis.parking.VoiceIO.fireOpes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.app.Activity;
import android.app.ActivityManager.RecentTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import compat.org.json.JSONException;
import compat.org.json.JSONObject;
import compat.org.json.JSONTokener;

import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.MagReplyHandler;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GeoSpannable;
import com.magnifis.parking.model.GooWeather;
import com.magnifis.parking.model.GooWeatherForecastCondition;
import com.magnifis.parking.model.GooXmlApiReply;
import com.magnifis.parking.model.Horoscope;
import com.magnifis.parking.model.MagNews;
import com.magnifis.parking.model.MagReply;
import com.magnifis.parking.model.MagTraffic;
import com.magnifis.parking.model.Origin;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.model.PoiLike;
import com.magnifis.parking.model.PoiLikeGeoSpannable;
import com.magnifis.parking.model.PoiReply;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.model.WWOnlineResponse;
import com.magnifis.parking.model.audioburst.ABFeed;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.toast.ToastController;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;
import static com.robinlabs.utils.BaseUtils.isEmpty;

public  class UnderstandingProcessorBase extends XMLFetcher<MagReply> {
	final static String TAG=UnderstandingProcessorBase.class.getSimpleName();
	
	final protected MultipleEventHandler.EventSource es;
	final protected MainAborter mainAborter;
	final protected Context context;
    protected ComponentName topActivity=null;
	protected ProgressIndicatorHolder progressIndicatorHolder=null;
	protected MainActivity mainActivity = null;
	protected UnderstandingStatus us=UnderstandingStatus.get();
	
	
	
	@Override
	protected void onNetworkCommunicationError() {
		CmdHandlerHolder.onNetworkCommunicationError();
	}
/*
	protected View getRootView() {
		ICmdHandlerHolder chh=getCmdHandlerHolder();
		return chh==null?null:chh.getRootView();
	}*/
	
	public UnderstandingProcessorBase(Context ma, MultipleEventHandler.EventSource es) {
		this.es = es;
		this.context=ma;
		mainAborter = new MainAborter();
		if (ma instanceof AborterHolder)
		   ((AborterHolder)ma).setAborter(mainAborter);
		if (ma instanceof MainActivity)
			mainActivity = (MainActivity)ma;
		else {
			topActivity = Utils.getTopActivity();
			
			Log.d(TAG,"!!!! "+ topActivity.getClassName().toString() );
		/*
			RecentTaskInfo rti=Utils.getTopTask();
			if (rti!=null) {
				if (rti.origActivity!=null) Log.d(TAG, rti.origActivity.toString());
				Log.d(TAG, rti.baseIntent.toString());
				Uri dt=rti.baseIntent.getData();
				if (dt!=null) Log.d(TAG, dt.toString());
				if (rti.description!=null) Log.d(TAG, rti.description.toString());
			}
		*/
		}
		if (ma instanceof ProgressIndicatorHolder) progressIndicatorHolder=(ProgressIndicatorHolder)ma;
	}
	

	@Override
	protected void onCancelled() {
		fireOpes();
		Utils.runInMainUiThread(new Runnable() {

			@Override
			public void run() {
				es.fireEvent();
			}

		});
		super.onCancelled();
	}
	
	protected boolean fAborted=false;
	
	public  class MainAborter implements Runnable {
		@Override
		public void run() {
			fAborted=true;
			VoiceIO.interruptPendingRequest();
		}
	};
	
	@Override
	protected MagReply consumeData(Object o) {
		return consumeUnderstanding((Understanding) o);
	}
	
	boolean handleCustomLocationIfAny(Understanding u) {
		Origin org = u.getOrigin(), dst = u.getDestination();
		boolean ro = true, rd = true;
		if (dst != null)
			rd = dst.resoleCustomLocationIfAny(u, true);
		if (org != null)
			ro = org.resoleCustomLocationIfAny(u, true);
		return ro && rd;
	}
	
	boolean force = false, toastIsShown = false;

	void showToast(QueryInterpretation qi) {
		if (qi != null)
			showToast(qi.getToShow());
	}

	void showToast(final String text) {
		toastIsShown = true;
		if (!isEmpty(text)) new ToastController(App.self, text);
	}
	
	@Override
	protected MagReply consumeXmlData(Element root) {
		if (!fAborted&&root!=null) try {
			return consumeUnderstanding(Xml.setPropertiesFrom(root, Understanding.class));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	//protected MainActivity mainActivity = null;
	
	/** First phase of processing request - performed in the background thread.
	 *  Attention: GUI operation are prohibited here! */
	public MagReply consumeUnderstanding(Understanding understanding) {
		if (understanding == null) {
			exactNcePlace("UpBase#1");
			return null;
		}
		final MagReply reply = new MagReply();
		reply.setUnderstanding(understanding);
	
		if (Config.bubles&&(mainActivity != null))
			if (!Utils.isEmpty(understanding.getQuery()))
				mainActivity.bubleQueryCorrect(understanding.getQuery());
		
		SuziePopup sp = SuziePopup.get();
		if (sp != null)
			sp.bubleQueryCorrect(understanding.getQuery());
			
		if (understanding.isError())
			return reply;

		force = false;

		if (!understanding.isLockTaken()) try {
			App.self.voiceIO.getOperationTracker().acquire(understanding.getCommand());
			understanding.setLockTaken(true);
		} catch (InterruptedException e) {
			Log.d(TAG, "operationTracker.interrupted");
			return reply;
		}

		understanding.getQueryInterpretation().calculate();
		understanding.fixContactNames();

		App.self.voiceIO.setShouldListenAfterCommand(understanding.isActivateMicrophone());

		understanding.expandMacros();
		
		if (us!=null) {
		  us.status.setLastCommand(understanding.getCommand());
		  us.status.setLastCommandDomain(understanding.getDomain());
		}
		
		MagReplyHandler ch = CmdHandlerHolder.getMagReplyHandler();
		for (;;) {
			if (ch != null) {
				if (ch.handleReplyInBg(reply)) {
					reply.setProcessedByHandlerInBg(true);
					return reply;
				}
				MagReplyHandler ch1 = CmdHandlerHolder.getMagReplyHandler();
				if (ch1!=null&&ch1!=ch) {
					ch=ch1;
					continue; // try with another handler
				}
			}
			break;
		}
		CmdHandlerHolder.setRelevantHandlerIfNeed(understanding,context);
		ch = CmdHandlerHolder.getMagReplyHandler();
		if (ch != null && ch.handleReplyInBg(reply)) {
			reply.setProcessedByHandlerInBg(true);
			return reply;
		}
		
		return reply;
	}	

	protected <T extends PoiLikeGeoSpannable> T fetchPoiInfo(Understanding understanding,
			String q, DoublePoint origin, boolean cat, String addr, Integer rad,
			Class<T> cls
	) {
		URL u=createPoiRequest(q,origin,cat, addr, rad,understanding.getOrderBy());
		try {
			InputStream is=invokeRequest(u,null, null, null);
			if (is!=null) try {
               Document doc=Xml.loadXmlFile(is);
               if (doc!=null) {
            	 T pr=Xml.setPropertiesFrom(
            			 doc.getDocumentElement(), cls);
            	 if (pr!=null) return pr;
               }
			} finally {
			  is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;					
	}
	
	protected String [] fetchTrafficReports(DoublePoint origin) {
		URL u=createTrafficOrNewsRequest(origin,origin,true,null);
		try {
			InputStream is=invokeRequest(u,null, null, null);
			if (is!=null) try {
               Document doc=Xml.loadXmlFile(is);
               if (doc!=null) {
            	 MagTraffic mt=Xml.setPropertiesFrom(
            			 doc.getDocumentElement(), MagTraffic.class);
            	 if (mt!=null) return mt.getReports();
               }
			} finally {
			  is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected ABFeed fetchABFeed(String topic) {
		try {
			if (Utils.isEmptyOrBlank(topic)) return null;
		    URL u=new URL(
		    //  "http://sapi.audioburst.com/app/digest?appKey=Robin&device=Android&user=111"
		    "http://sapi.audioburst.com/app/question?appKey=Robin&mode=demo&extended=true&device=Android&value="+
		       URLEncoder.encode(topic)
		    );
			InputStream is=invokeRequest(u, null, null, null);
			if (is!=null) try {
				JSONTokener jsto = new JSONTokener(is);
				JSONObject jso=new JSONObject(jsto);
				Element el=Json.convertToDom(jso);
                if (el!=null) try {
                	CharSequence s=Xml.domToText(el);
                	ABFeed abf=Xml.setPropertiesFrom(el, ABFeed.class);
                	return abf;
                } catch (Throwable t) {
    				t.printStackTrace();
                }
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
			  is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String [] fetchNews(DoublePoint origin, String topic) {
		URL u=createTrafficOrNewsRequest(origin,origin,true,topic);
		try {
			InputStream is=invokeRequest(u,null, null, null);
			if (is!=null) try {
               Document doc=Xml.loadXmlFile(is);
               if (doc!=null) {
            	 MagNews mt=Xml.setPropertiesFrom(
            			 doc.getDocumentElement(), MagNews.class);
            	 if (mt!=null) return mt.getItems();
               }
			} finally {
			  is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected String  fetchHoroscope(DoublePoint origin, Date birth) {
		Calendar cal=Calendar.getInstance();
		cal.setTime(birth);
		URL u=createTrafficOrNewsRequest(origin,origin,true,"horoscope",
		  "bmonth",
		   Integer.toString(cal.get(Calendar.MONTH)+1),
		  "bday",
		   Integer.toString(cal.get(Calendar.DAY_OF_MONTH))
		);
		try {
			InputStream is=invokeRequest(u,null, null, null);
			if (is!=null) try {
               Document doc=Xml.loadXmlFile(is);
               if (doc!=null) {
            	 Horoscope mt=Xml.setPropertiesFrom(
            			 doc.getDocumentElement(), Horoscope.class);
            	 if (mt!=null) return mt.getDescription();
               }
			} finally {
			  is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	protected GooWeather weather=null;
	protected GooWeatherForecastCondition forecast=null;
	
	
	protected void fetchWWOReport(DoublePoint dp) {
		Log.d(TAG, "Fetching weather report for " + dp.toString()); 
		
		URL u=RequestFormers.createWorldWeatherOnlineRequest(dp);
		
		try {
			InputStream is=invokeRequest(u,null, null, null);
			
			if (null == is) // try again 
				is=invokeRequest(u,null, null, null);
			
			if (is!=null) try {
               Document doc=Xml.loadXmlFile(is);
               if (doc!=null) {
   //         	  Log.d(TAG,Xml.domToText(doc.getDocumentElement(), true, true).toString());
            	   WWOnlineResponse ww=Xml.setPropertiesFrom(
            			 doc.getDocumentElement(), WWOnlineResponse.class
            	  );
                  if (ww!=null) {
                	  GooWeather w=ww.toGooWeather();
                	  if (w!=null&&w.getCurrentConditions()!=null) weather=w;
                	  Log.d(TAG, "Fetching weather report is ready ");
                  }
               }
			} finally {
			  is.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	protected void onPostExecute(MagReply reply) {
		es.fireEvent();
	}
}
