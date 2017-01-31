package com.magnifis.parking;

import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.UserLocationProvider.LocationInfo;
import com.magnifis.parking.ads.AdManager;
import com.magnifis.parking.cmd.GoogleTranslateFetcher;
import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.cmd.i.ISwitchToBubbles;
import com.magnifis.parking.cmd.i.LocalCommandHandler;
import com.magnifis.parking.cmd.i.MagReplyHandler;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.feed.AudioburstFeedController;
import com.magnifis.parking.feed.SmsFeedController;
import com.magnifis.parking.geo.GoogleGeocoder;
import com.magnifis.parking.model.DelegateAgentPhone;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GasReply;
import com.magnifis.parking.model.GasStation;
import com.magnifis.parking.model.GcResult;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.GeoSpannable;
import com.magnifis.parking.model.GooWeather;
import com.magnifis.parking.model.GooWeatherForecastCondition;
import com.magnifis.parking.model.LearnAttribute;
import com.magnifis.parking.model.LearnedAnswer;
import com.magnifis.parking.model.MagReply;
import com.magnifis.parking.model.Origin;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.model.PkResponse;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.model.PoiLike;
import com.magnifis.parking.model.PoiLikeGeoSpannable;
import com.magnifis.parking.model.PoiReply;
import com.magnifis.parking.model.PushAd;
import com.magnifis.parking.model.QueryInterpretation;
import com.magnifis.parking.model.Script;
import com.magnifis.parking.model.SortableByPrice;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.model.audioburst.ABFeed;
import com.magnifis.parking.model.audioburst.Burst;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.sportfeeds.ChadwickFeedFetcher;
import com.magnifis.parking.suzie.SuziePopup;
import com.magnifis.parking.traffic.EtaMonitor;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Analytics;
import com.magnifis.parking.utils.ClientParser;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.WeatherView;

import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import compat.org.json.JSONObject;
import compat.org.json.JSONTokener;

import static com.magnifis.parking.Launchers.composeEmail;
import static com.magnifis.parking.Launchers.composeTextMessage;
import static com.magnifis.parking.Launchers.composeTwitt;
import static com.magnifis.parking.Launchers.killAllProcesses;
import static com.magnifis.parking.Launchers.killProcess;
import static com.magnifis.parking.Launchers.killProcessLike;
import static com.magnifis.parking.Launchers.launchAgenda;
import static com.magnifis.parking.Launchers.launchAirplaneSettings;
import static com.magnifis.parking.Launchers.launchBatteryUsage;
import static com.magnifis.parking.Launchers.launchBluetoothSettings;
import static com.magnifis.parking.Launchers.launchBrowser;
import static com.magnifis.parking.Launchers.launchCamera;
import static com.magnifis.parking.Launchers.launchGpsSettings;
import static com.magnifis.parking.Launchers.launchPhoneSettings;
import static com.magnifis.parking.Launchers.launchWifiSettings;
import static com.magnifis.parking.Launchers.lookAtMarketFor;
import static com.magnifis.parking.Launchers.runProcessLike;
import static com.magnifis.parking.Launchers.showContacts;
import static com.magnifis.parking.Launchers.startNestedActivity;
import static com.magnifis.parking.Launchers.youtube;
import static com.magnifis.parking.Phrases.formCurrentTime;
import static com.magnifis.parking.Phrases.pickFoundOptionsPhrase;
import static com.magnifis.parking.Phrases.sayOrder;
import static com.magnifis.parking.Phrases.sayVacancy;
import static com.magnifis.parking.RequestFormers.createParkingRequest;
import static com.magnifis.parking.VoiceIO.condListenAfterTheSpeech;
import static com.magnifis.parking.VoiceIO.fireOpes;
import static com.magnifis.parking.VoiceIO.listenAfterTheSpeech;
import static com.magnifis.parking.VoiceIO.playTextAlerts;
import static com.magnifis.parking.VoiceIO.sayAndShow;
import static com.magnifis.parking.VoiceIO.sayAndShowFromGui;
import static com.magnifis.parking.VoiceIO.sayFromGui;
import static com.magnifis.parking.tts.MyTTS.speakText;
import static com.magnifis.parking.utils.Utils.isEmpty;
import static com.robinlabs.utils.BaseUtils.f2c;
import static com.robinlabs.utils.BaseUtils.urlencode;

public class UnderstandingProcessor extends UnderstandingProcessorBase  implements ClientStateInformer {

    final static String TAG=UnderstandingProcessor.class.getSimpleName();
    static FlashLight flashLightSurface = null;

    Analytics analytics;

    void performTrafficCommand(DoublePoint originLocation,
                               DoublePoint dstLocation, String[] alerts) {
        mainActivity.enableTraffic();
        if (originLocation == null) {
            speakText(R.string.mainactivity_please_repeat);
        } else {
            if (dstLocation == null) {
                mainActivity.showLocation(originLocation);
                mainActivity.mapController.setZoom(13);
            }

            playTextAlerts(alerts,
                    context.getString(R.string.P_NEWS_INTRO));
        }

    }

    public UnderstandingProcessor(Context ma,MultipleEventHandler.EventSource es) {
        super(ma,es);
        this.analytics = new Analytics(ma);
    }

    /** First phase of processing request - performed in the background thread.
     *  Attention: GUI operation are prohibited here! */
    @Override
    public MagReply consumeUnderstanding(Understanding understanding) {
        if (Config.roku_version && "movies".equals(understanding.getAction())) {
            understanding.setCommandByCode(Understanding.CMD_MOVIES);
            understanding.setUrl(null);
            understanding.setQueryInterpretation("Now this is TV !");
        }

        final MagReply reply =super.consumeUnderstanding(understanding);
        if (reply==null||understanding==null||understanding.isError()||reply.isProcessedByHandlerInBg()) {
			exactNcePlace="UP#1";
        	return reply;
        }
        return continueUnderstanding(reply,understanding,reply.getCommandCode());
    }

    protected boolean canBeHandleOutOfMainActivity(int cmd) {
        switch(cmd) {
            case Understanding.CMD_REPEAT:
            case Understanding.CMD_NEWS:
            case Understanding.CMD_AUDIOBURST:
            case Understanding.CMD_WEATHER:
            case Understanding.CMD_UNKNOWN:
//		case Understanding.CMD_HOROSCOPE:
            case Understanding.CMD_CAMERA:
            case Understanding.CMD_INFO:
            case Understanding.CMD_INTRO_VIDEO:
            case Understanding.CMD_INITIALIZATION:
            case Understanding.CMD_GPS:
            case Understanding.CMD_WIFI:
            case Understanding.CMD_BLUTOOTH:
            case Understanding.CMD_ROBIN_SETTINGS:
            case Understanding.CMD_SETTINGS:
            case Understanding.CMD_FEEDBACK:
            case Understanding.CMD_TRANSLATE:
            case Understanding.CMD_OPENAPP:
            	/*
            case Understanding.CMD_VOICE_FEMALE:
            case Understanding.CMD_VOICE_MALE:
            case Understanding.CMD_VOICE_SWITCH:
            */
            case Understanding.CMD_BATTERY:
            case Understanding.CMD_SWITCH_TO_ENGLISH:
            case Understanding.CMD_SWITCH_TO_RUSSIAN:
            case Understanding.CMD_DRIVING:
            case Understanding.CMD_NOT_DRIVING:
            case Understanding.CMD_YOUTUBE:
            case Understanding.CMD_MAIL:
            case Understanding.CMD_JOKE:
            case Understanding.CMD_HELLO:
            case Understanding.CMD_HOW_ARE_YOU:
            case Understanding.CMD_JOKE_SEXY:
            case Understanding.CMD_QUOTE:
            case Understanding.CMD_SAY:
            case Understanding.CMD_NOTE:
            case Understanding.CMD_APP_NAME:
            case Understanding.CMD_SHOW_ROBIN:
            case Understanding.CMD_NAVIGATE:
            case Understanding.CMD_PLAY:
            case Understanding.CMD_CONTACTS:
            case Understanding.CMD_TEXT:
            case Understanding.CMD_OPEN_URL:
            case Understanding.CMD_BBALL_NEWS:
            case Understanding.CMD_TIME:
            case Understanding.CMD_TURNOFF:
            case Understanding.CMD_FLOATING_ON:
            case Understanding.CMD_FLOATING_OFF:
            case Understanding.CMD_FUCK:
            case Understanding.CMD_SILENT:
            case Understanding.CMD_VIBRATE:
            case Understanding.CMD_AIRPLANE:
            case Understanding.CMD_VOLUME_DOWN:
            case Understanding.CMD_VOLUME_UP:
            case Understanding.CMD_WIFI_ON:
            case Understanding.CMD_WIFI_OFF:
            case Understanding.CMD_BLUETOOTH_ON:
            case Understanding.CMD_BLUETOOTH_OFF:
            case Understanding.CMD_CALL_ALERTS_ON:
            case Understanding.CMD_CALL_ALERTS_OFF:
            case Understanding.CMD_MESSAGE_ALERTS_ON:
            case Understanding.CMD_MESSAGE_ALERTS_OFF:
            case Understanding.CMD_CLOSE_APP:
            case Understanding.CMD_CLOSE_ALL:
            case Understanding.CMD_SONG_PAUSE:
            case Understanding.CMD_FLASHLIGHT_ON:
            case Understanding.CMD_FLASHLIGHT_OFF:
            case Understanding.CMD_ROKU:
            case Understanding.CMD_MOVIES:
            case Understanding.CMD_DELEGATE_AGENT:
			
			/*
		case Understanding.CMD_YES:
		case Understanding.CMD_NO:
		case Understanding.CMD_DO_IT:
		*/
                return true
                        ;
        }
        return false;
    }

    protected boolean switchToBubles(int cmd) {
        switch(cmd) {
            case Understanding.CMD_ROUTE:
            case Understanding.CMD_MAP:
            case Understanding.CMD_SEARCH:
            case Understanding.CMD_GAS:
            case Understanding.CMD_PARKING:
            case Understanding.CMD_TRAFFIC:
            case Understanding.CMD_YES:
            case Understanding.CMD_NO:
                //case Understanding.CMD_dlg_cancel
            case Understanding.CMD_OTHER:
            case Understanding.CMD_COST:
            case Understanding.CMD_VACANCY:
                //case Understanding.CMD_ours:?
            case Understanding.CMD_CHEAP:
            case Understanding.CMD_DETAILS:
            case Understanding.CMD_NEAR:
            case Understanding.CMD_ZOOMIN:
            case Understanding.CMD_ZOOMOUT:
            case Understanding.CMD_MY_PARKING:
            case Understanding.CMD_DAILY_UPDATE:

                return false;
        }
        return true;
    }

    protected boolean switchToMap(int cmd) {
        switch(cmd) {
            case Understanding.CMD_ROUTE:
            case Understanding.CMD_MAP:
            case Understanding.CMD_SEARCH:
            case Understanding.CMD_GAS:
            case Understanding.CMD_PARKING:
            case Understanding.CMD_TRAFFIC:
            case Understanding.CMD_SATELLITE_VIEW:
            case Understanding.CMD_MAP_VIEW:
            case Understanding.CMD_CHEAP:
            case Understanding.CMD_NEAR:
            case Understanding.CMD_ZOOMIN:
            case Understanding.CMD_ZOOMOUT:
            case Understanding.CMD_MY_PARKING:

                return true;
        }
        return false;
    }

    protected MagReply continueUnderstanding(final MagReply reply,Understanding understanding, int cmd) {

        // should we show an ad?
        if (processAdToPush(understanding))
            return reply;

        // Handle FB status command iterations
        if (cmd == Understanding.CMD_FACEBOOK_STATUS
                || FacebookStatusCommandHandler.isActiveCommand()) {
            if (FacebookStatusCommandHandler.handleReply(understanding))
                return reply;
        }

        String snippet = understanding.getSnippet();
        String snippetType = understanding.getSnippetType();
        if (snippet != null && snippetType != null && snippetType.equalsIgnoreCase("url"))
            Launchers.launchBrowser(context, snippet);

        // Handle repeat command
        if (RepeatCommandHandler.handleReply(understanding))
            return reply;

        if (cmd == Understanding.CMD_UNKNOWN)
            return reply;

        if (cmd != Understanding.CMD_NOP) {
            Log.d(TAG + ".command: ", reply.getUnderstanding().getCommand());
        }

        if (cmd == Understanding.CMD_TIME) {
            QueryInterpretation qi = understanding.getQueryInterpretation();
            if (qi != null) {
                qi.setToSay(formCurrentTime(qi.getToSay()));
                qi.setToShow(formCurrentTime(qi.getToShow()));
            }
        }

        if (cmd == Understanding.CMD_AIRPLANE) {
            if (android.os.Build.VERSION.SDK_INT >= 17)
                understanding.setQueryInterpretation(R.string.P_airplane_fail_auto);
        }

        if (cmd == Understanding.CMD_NOTE) {
            QueryInterpretation qi = understanding.getQueryInterpretation();
            if (qi != null) {
                String myMail = App.self.getGmailAccountName();
                if (!Utils.isEmpty(myMail)) {
                    qi.setToSay(null);
                    qi.setToShow(null);
                }
                else {
                    qi.setToSay(App.self.getString(R.string.P_note_fail));
                    qi.setToShow(App.self.getString(R.string.P_note_fail));
                }
            }
        }

        if (cmd == Understanding.CMD_INITIALIZATION) {
            Props props=Props.getInstance(context);
            boolean anyMessageForUser=false;
            QueryInterpretation qi=understanding.getQueryInterpretation();
            try {
                // try get message for this user...
                String last_new_frazes_date = understanding.getTimedate().toString();

                // found message for this user!
                // check: already said message?
                if (last_new_frazes_date.equals((String)props.get("last_new_frazes_date"))) {
                    // check: already said advertisement?
                    String s = qi.getToSay();
                    if (s != null && !s.equals((String)props.get("last_advertisement")))
                        anyMessageForUser=true;
                }

            } catch(Exception e) {}

            if (!anyMessageForUser) {
                // not found message for this user!
                // check: already said advertisement?
                String s = qi==null?null:qi.getToSay();
                if (!isEmpty(s)) {
                    if (s.equals((String)props.get("last_advertisement")))
                        if (App.self.isGreetingDisabled())
                            understanding.getQueryInterpretation().set(null);
                        else
                            understanding.setQueryInterpretation(App.self.robin().sayHello());
                    else
                        props.setAndSave("last_advertisement", s);
                } else
                    if (App.self.isGreetingDisabled())
                        understanding.getQueryInterpretation().set(null);
                    else
                        understanding.setQueryInterpretation(App.self.robin().sayHello());
            }


            return reply;
        }

        if (mainActivity!=null) {

            if (mainActivity.feedController != null)
                switch (reply.getCommandCode()) {
                    case Understanding.CMD_PREV:
                    case Understanding.CMD_OTHER:
                    case Understanding.CMD_REPEAT:
                    case Understanding.CMD_MORE:
                    case Understanding.CMD_DETAILS:
                        return reply;
                }


            boolean procceedCommandWithVoiceAndToast = true;
            switch (reply.getCommandCode()) {
                case Understanding.CMD_PARKING:
                case Understanding.CMD_SEARCH:
                case Understanding.CMD_ROUTE:
                case Understanding.CMD_GAS:
                case Understanding.CMD_LEARN:
                case Understanding.CMD_PLAY_TWITTER:
                case Understanding.CMD_RETWEET:
                case Understanding.CMD_MORE:
                case Understanding.CMD_REMINDER:
                case Understanding.CMD_ALARM:
                case Understanding.CMD_CALL:
                case Understanding.CMD_READ:
                case Understanding.CMD_INFO:
                case Understanding.CMD_INTRO_VIDEO:
                case Understanding.CMD_UNKNOWN:
                case Understanding.CMD_MY_BIRTHDAY:
                case Understanding.CMD_LEARN_BIRTHDAY:
                case Understanding.CMD_DICTATE:
                case Understanding.CMD_FACEBOOK_NEWS:
                case Understanding.CMD_NEWS:
                case Understanding.CMD_HOROSCOPE:
                case Understanding.CMD_TRANSLATE:
                case Understanding.CMD_ROBIN_SETTINGS:
                    break;
                case Understanding.CMD_NO:
                    if (us.savedReply != null) {
                        String sNO = us.savedReply
                                .getConfirmationRequiredOnNo();
                        if (!isEmpty(sNO))
                            understanding.setQueryInterpretation(sNO);
                    }
                    if (procceedCommandWithVoiceAndToast) {
                        MyTTS.syncSayFromGUI(context,understanding.getQueryInterpretation()
                                .getToSay());
                    }
                default:
                    if (procceedCommandWithVoiceAndToast) {
                        if (understanding.getQueryInterpretation() != null && !isEmpty(understanding.getQueryInterpretation().toString())||
                                !isEmpty(understanding.getQuery())
                                )
                            Output.show(
                                    context,
                                    understanding.getQueryInterpretation().getToShow(),
                                    understanding.getQuery()
                            );
                    }
                    break;
            }


        }

        Understanding actualUnderstanding = understanding;



        if (us.savedReply != null) {
            Log.d(TAG, "savedReply!=null; cmd=" + cmd);
            ssr: switch (cmd) {
                case Understanding.CMD_NO:
                    if (us.savedReply != null) {
                        switch (us.savedReply.getCommandCode()) {
                            case Understanding.CMD_LEARN:
                                if (us.savedReply.getLearnAttribute()
                                        .isTextInputFallbackRequired())
                                    return reply;
							/*
							 * case Understanding.CMD_ROUTE: return reply;
							 */
                            default:
                                break ssr;
                        }
                    }
                    break;
                case Understanding.CMD_YES:
                case Understanding.CMD_DO_IT:
                    String sYES = null;
                    if (us.savedReply != null) {
                        sYES = us.savedReply
                                .getConfirmationRequiredOnYes();
                        reply.setUnderstanding(understanding = us.savedReply);
                    }

                    understanding.setConfirmationRequired(false);
                    cmd = reply.getUnderstanding().getCommandCode();
					/*
					 * switch (savedReply.getCommandCode()) { case
					 * Understanding.CMD_SEARCH: case Understanding.CMD_ROUTE: case
					 * Understanding.CMD_PARKING: case Understanding.CMD_AUTOADVERT:
					 */
                    understanding
                            .setQueryInterpretation(isEmpty(sYES) ? actualUnderstanding
                                    .getQueryInterpretation()
                                    : new QueryInterpretation(sYES));
                    // }
                    force = true;
            }
            us.savedReply = null;
        }

        switch (cmd) {
            case Understanding.CMD_DELEGATE_AGENT:
            case Understanding.CMD_AUTOADVERT:
                if (understanding.isConfirmationRequired()) {
                    cleintState=SN_YES_NO;
                    us.savedReply = understanding;
                    return reply;
                }
        }

        // if (!packageName.loadMapFromBg()) return null;

        if ((cmd == Understanding.CMD_LEARN)
                && (understanding.getLearnAttribute() != null)
                && !understanding.getLearnAttribute().isCustomAddress()) {
            if (understanding.isConfirmationRequired()) {
                cleintState=SN_YES_NO;
                us.savedReply = understanding;
            } else if (actualUnderstanding.getCommandCode() == Understanding.CMD_NO) {
                return reply;
            } else {
                understanding.setQueryInterpretation(actualUnderstanding
                        .getQueryInterpretation());
                understanding.learn();
            }
            understanding.expandMacros();
            // showToast(understanding.getQueryInterpretation());
            return reply;
        }

        boolean cmd_delegate_agent=false;
        switch (cmd) {
	        ///////////////////////////////////////////////////////////
        case Understanding.CMD_DELEGATE_AGENT: 
        	cmd_delegate_agent=true;
        	break;
            case Understanding.CMD_OPENAPP: {
                if (!runProcessLike(understanding.getDescription()))
                    understanding.setQueryInterpretation(
                            Utils.getString(R.string.P_CAN_NOT_LAUNCH,"app", understanding.getDescription())
                    );

                return reply;
            }

            case Understanding.CMD_MY_PARKING: {
                DoublePoint dstLocation = DoublePoint.from(App.self.getStringPref(PrefConsts.PF_LAST_PARKING_PLACE));
                MainActivity.debug_car_mode("My parking: "+dstLocation);
                if (dstLocation == null)
                    understanding.getQueryInterpretation().set(App.self.getString(R.string.mainactivity_onpostexecute_dont_know_last_parking_place));
                return reply;
            }

            case Understanding.CMD_READ:
                return reply;
            case Understanding.CMD_REMINDER:
                if ("add".equals(understanding.getAction())) {
                    String dsc = understanding.getDescription();
                    boolean allDay = understanding.getTimestamp() == null;

                    CalReminding calReminder = CalReminding.getInstance();



                    if (mainActivity.calReminder.doesEventExist(
                            understanding.getTimedate(), dsc, allDay) == CalReminding.SUCH_EVENT_ALREADY_EXISTS) {

                        understanding
                                .setQueryInterpretation(R.string.P_SUCH_EVENT_ALREADY_EXISTS);

                        understanding.setCommandCode(Understanding.CMD_NOP);

                        return reply;
                    }
                    if (understanding.isConfirmationRequired() && !force) { // need
                        // confirmation
                        us.savedReply = understanding;
                        reply.setUnderstanding((understanding = new Understanding())
                                .setCommandCode(Understanding.CMD_NOP)
                                .setActivateMicrophone(true));
                        QueryInterpretation qi = us.savedReply
                                .getQueryInterpretation();
                        if (qi == null || isEmpty(qi.toString()))
                            return reply;
                        cleintState=SN_YES_NO;
                        understanding.setQueryInterpretation(qi.clone().append(
                                QueryInterpretation.TO_SAY,
                                R.string.P_is_that_right, "\n"));
                        return reply;
                    }
                    int reminderAddingResult = mainActivity.calReminder.set(
                            context,
                            understanding.getTimedate(), 
                            dsc,
                            understanding.getOrigin()!=null
                               ?understanding.getOrigin().getFullAddress():null,
                            allDay
                    );
                    switch (reminderAddingResult) {
                        case CalReminding.OK:
                            MyTTS.syncSayFromGUI(context,
                                    R.string.mainactivity_consumexmldata_reminder_is_set);
                            break;
                        case CalReminding.SUCH_EVENT_ALREADY_EXISTS:
                            MyTTS.syncSayFromGUI(context,R.string.P_SUCH_EVENT_ALREADY_EXISTS);
                            break;
                        case CalReminding.ERROR:
                        default:
                            MyTTS.syncSayFromGUI(context,R.string.P_SOMETHING_WENT_WRONG);
                    }
                    doNothing = true;
                } else if ("clarify".equals(understanding.getAction())) {
                    understanding.setCommandCode(Understanding.CMD_NOP);
                    // doNothing = true;
                    // Output.sayAndShow(MainActivity.this,
                    // understanding.getQueryInterpretation().getToShow(),
                    // understanding.getQueryInterpretation().getToSay(), false);
                } else if ("cancel".equals(understanding.getAction())) {
                    understanding.setCommandCode(Understanding.CMD_NOP);
                    // doNothing = true;
                    // Output.sayAndShow(MainActivity.this,
                    // understanding.getQueryInterpretation().getToShow(),
                    // understanding.getQueryInterpretation().getToSay(), false);
                }
                return reply;
            case Understanding.CMD_ALARM:
                if (!Utils.isAndroid23orAbove) {
                    MyTTS.syncSayFromGUI(context,R.string.P_ANDROID23_REQUIRED);
                    doNothing = true;
                    return reply;
                }
                if ("add".equals(understanding.getAction())) {
                    if (understanding.isConfirmationRequired() && !force) { // need
                        // confirmation
                        Output.sayAndShowFromGui(context, understanding
                                .getQueryInterpretation().getToSay(), understanding
                                .getQuery(), null, true, false);
                        us.savedReply = understanding;
                        cleintState=SN_YES_NO;
                        MyTTS.syncSayFromGUI(context,R.string.P_is_that_right);
                        return reply;
                    }
                    String dsc = understanding.getDescription();
                    switch (AlarmSetter.set(context, understanding.getTimedate(), dsc)) {
                        case AlarmSetter.OK:
                            MyTTS.syncSayFromGUI(context,R.string.mainactivity_consumexmldata_alarm_is_set);
                            break;
                        case AlarmSetter.SUCH_EVENT_ALREADY_EXISTS:
                            MyTTS.syncSayFromGUI(context,R.string.P_SUCH_EVENT_ALREADY_EXISTS);
                            break;
                        case AlarmSetter.ERROR:
                            MyTTS.syncSayFromGUI(context,R.string.P_SOMETHING_WENT_WRONG);
                    }
                    doNothing = true;
                } else if ("clarify".equals(understanding.getAction())) {
                    understanding.setCommandCode(Understanding.CMD_NOP);
                    // doNothing = true;
                    // Output.sayAndShow(packageName,
                    // understanding.getQueryInterpretation().getToShow(),
                    // understanding.getQueryInterpretation().getToSay(), false);
                } else if ("cancel".equals(understanding.getAction())) {
                    understanding.setCommandCode(Understanding.CMD_NOP);
                    // doNothing = true;
                    // Output.sayAndShow(packageName,
                    // understanding.getQueryInterpretation().getToShow(),
                    // understanding.getQueryInterpretation().getToSay(), false);
                }

                return reply;

            //1
            case Understanding.CMD_CHEAP:
            case Understanding.CMD_CHEAPEST:
                if (mainActivity.currentController != null &&
                        mainActivity.currentController.size() > 0) {
                    GeoSpannable gs = mainActivity.currentController.getSpannable();
                    if (gs instanceof SortableByPrice) {
                        ((SortableByPrice) gs).orderByPrice();
                    }
                }
                return reply;
            case Understanding.CMD_NEAR:
            case Understanding.CMD_NEAREST:
                if (mainActivity.currentController != null&&
                        mainActivity.currentController.size() > 0) {
                    GeoSpannable gs = mainActivity.currentController.getSpannable();
                    gs.orderByDistance();
                }
                return reply;
            //2
            case Understanding.CMD_CLEAR_MAP:
                resetStatus(null, null, reply);
                return reply;
            case Understanding.CMD_AUDIOBURST:
        	    reply.setAbFeed(fetchABFeed(understanding.getDescription()));
        	    return reply;        	
            case Understanding.CMD_NEWS:
				if (understanding.getOrigin() == null) {
					reply.setTrafficReports(
							fetchNews(UserLocationProvider.readLocationPoint(), understanding.getDescription()));
					return reply;
				}
                break;
            case Understanding.CMD_TRANSLATE:
                translate(understanding, reply);
                return reply;
            case Understanding.CMD_ROKU:
            case Understanding.CMD_MOVIES:
                if (Config.roku_version) {
                    ClientParser cp = new ClientParser(understanding.getQuery()) {
                        @Override
                        public void run(Uri uri,String text) {
                            if (text != null)
                                sayAndShow(text);

                            if(uri!=null) {
                                Intent it = new Intent(Intent.ACTION_VIEW);
                                it.setData(uri);
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                App.self.startActivity(it);
                            }

                        }
                    };
                    return reply;
                }
                else
                    break;
            case Understanding.CMD_OPEN_URL:
    			// is there also a script to run on top of the page? 
    			String jsFuncUrl = null; 
    			String action = understanding.getAction(); 
    			if (!Utils.isEmpty(action) && action.startsWith("script")) {
    				Script scriptVal = understanding.getScript(); 
    				if (scriptVal != null) {
    					jsFuncUrl = scriptVal.getJsFuncName(); 
    				
    				String jsFuncCode = Utils.readFileFromUrl(jsFuncUrl); 
    					if (!Utils.isEmpty(jsFuncCode))
    						reply.setScriptCode(jsFuncCode); 
    				}
    			}
    			return reply; 
            case Understanding.CMD_BBALL_NEWS:
                new ChadwickFeedFetcher();// will do the rest
                return reply;
            case Understanding.CMD_HOROSCOPE:
                return reply;
            case Understanding.CMD_TRAFFIC:
            case Understanding.CMD_SEARCH:
            case Understanding.CMD_PARKING:
            case Understanding.CMD_WEATHER:
            case Understanding.CMD_GAS:
            case Understanding.CMD_LEARN:
                if (understanding.getOrigin() == null) {
                    Origin org = new Origin();
                    understanding.setOrigin(org);
				/*
				 * // probably we need something near the user location Origin
				 * org=new Origin(); understanding.setOrigin(org); DoublePoint
				 * gpsloc=understanding.getGpsLocation(); if (gpsloc==null) { if
				 * (packageName.mlo.reportLocation()!=UserLocationProvider.
				 * LOC_UNAVAILABE) gpsloc=packageName.getLastKnownLocation(); }
				 * if (gpsloc!=null) org.setLocation(gpsloc);
				 */
                }
                if (!handleCustomLocationIfAny(understanding)) {
                    understanding.setCommandCode(Understanding.CMD_NOP);
                    return reply;
                }
                break;
            case Understanding.CMD_ROUTE:
                if (understanding.getDestination() != null) {
                    if (understanding.getOrigin() == null)
                        understanding.setOrigin(new Origin());
                    if (!handleCustomLocationIfAny(understanding)) {
                        understanding.setCommandCode(Understanding.CMD_NOP);
                        return reply;
                    }
                    if (understanding.getDestination().anyPoiInfo()) {
                        understanding.setOrigin(understanding.getDestination());
                        understanding.setDestination(null);
                        understanding
                                .setCommandCode(cmd = Understanding.CMD_SEARCH);
                    }
                    break;
                }
            default:
                return reply;
        }

        Origin org = understanding.getOrigin();

        boolean cmdRoute = cmd == Understanding.CMD_ROUTE, cmdTraffic = cmd == Understanding.CMD_TRAFFIC, cmdNews = cmd == Understanding.CMD_NEWS, cmdSearch = cmd == Understanding.CMD_SEARCH, cmdGas = cmd == Understanding.CMD_GAS, cmdParking = cmd == Understanding.CMD_PARKING, cmdLearn = cmd == Understanding.CMD_LEARN;

        if (cmdSearch && (org != null) && !org.anyPoiInfo()) {
            // fallback to the parking search
            cmdSearch = false;
            cmdParking = true;
            mainActivity.indirectParkingSearch = true;
            mainActivity.silentParking = true;
            understanding.setCommandCode(cmd = Understanding.CMD_PARKING);
        }

        // Ilya modified to prevent too much info
        // if (!cmdRoute) sayAndShowFromGUI(reply.getUnderstanding());

        LocationInfo reportLcRes = UserLocationProvider
                .queryLocation();
        DoublePoint lc = reportLcRes.getLocationDP();
        
        if (cmd_delegate_agent) {
        	if ("sms".equalsIgnoreCase(understanding.getAction())) {
        		RobinDB rdb=RobinDB.getInstance(context);
        		
        		//{ fake phone numbers for debugging
        		/*
        		if (!understanding.isDontSaveAgentPhones()) {
        			understanding.setPhoneNumbers("+18098036954","+18293502866");
        		}
        		*/
        		//}
        		
        		
        		understanding.setDescription(understanding.getPhoneNumbers()[0]);
         		
        		if (!understanding.isDontSaveAgentPhones()) {
	        		rdb.delete(
	                  DelegateAgentPhone.class, 
	             	  "agent=?", 
	             	   understanding.getContactNames()[0]
	     			);
	        		for (String s:understanding.getPhoneNumbers()) {
	        		  rdb.save(new DelegateAgentPhone(understanding.getContactNames()[0], s.replaceAll("\\s+|[-]+", "")));
	        		}
        		} 
        		
        		understanding.setCommandByCode(Understanding.CMD_SEND);
        		understanding.setNumber(true);
        		
        		String fa=null;
 			    try {
				  GcResult gr=GoogleGeocoder.getFromLatlonRefined(lc);
				  if (gr!=null) {
					  boolean compact = true; 
					  fa=gr.getFormattedAddress(compact);
				  }
			    } catch (IOException e) {
				  e.printStackTrace();
			    }
        		
        		if (isEmpty(fa)) fa=lc.toString();
        		understanding.setMessage(Utils.expandMacro(understanding.getMessage(), "user_location", fa).toString());
        		return this.consumeUnderstanding(understanding);
        	}
            return reply;
        }
        
        
        if (org == null)
            understanding.setOrigin(org = new Origin());
        

        

        Log.d(TAG + ".dstLocation:", "");
        understanding.calculateDestinationLocation(lc);
        
        
        
        if (cmdRoute) {
            return reply; // just ignore the origin change
        }

        DoublePoint destinationLocation = understanding
                .getDestinationLocation();

        Log.d(TAG + ".orgLocation:", "");
        if (cmdParking && (org.getLocation() != null)// &&
            // status.getMode()==MAStatus.MODE_POI
                ) {
            MapItemSetContoller controller = mainActivity
                    .getSelectionController();
            if (controller != null) {
                // the parking coordiantes are known
                // and we are in the poi Mode
                GeoObject sel = controller.getSelected();
                if ((sel != null)
                        && (org.getLocation().distanceInMeters(sel.getPoint()) < 100)) {
                    org.setFullAddress(sel.getAddress());
                    if (sel instanceof Poi) {
                        PoiReply pr = new PoiReply();
                        pr.setPois(new Poi[] { (Poi) sel });
                        reply.setPois(pr);
                    } else if (sel instanceof GasStation) {
                        GasReply gr = new GasReply();
                        gr.setPois(new GasStation[] { (GasStation) sel });
                        reply.setGasStations(gr);
                    }

                    if (sel instanceof PoiLike) {
                        understanding
                                .setQueryInterpretation("Looking for parking at "
                                        + sel.getName()
                                        + " "
                                        + ((PoiLike) sel).getStreetAddress());
                    }
                }
            }

        }
		/*
		 * if (!(cmdRoute||cmdLearn))
		 * sayFromGui(understanding.getQueryInterpretation()); if
		 * (!toastIsShown) showToast(understanding);
		 */
        boolean ok = understanding.calculateOriginLocation(lc);
        DoublePoint originLocation = understanding.getOriginLocation();

        if (!ok && (cmdTraffic || org.anyPoiInfo() && !org.anyAddressInfo())) {
            // looking for POI's near you
            if (lc == null) {
                sayAndShowFromGui(R.string.P_LOOK_AROUND_GPS);
                return reply;
            } else {
                org.setLocation(originLocation = lc);
                ok = true;
            }
        }

        if (ok) {
            if (cmd == Understanding.CMD_WEATHER) {
                Date dt = reply.getUnderstanding().getTimedate();

                if (dt != null) {
                    Log.d(TAG, "timedate: " + dt);
                    Calendar now = Calendar.getInstance();
                    now.setTimeInMillis(System.currentTimeMillis());
                    now.add(Calendar.DAY_OF_YEAR, 3);
                    if (dt.after(now.getTime())) {
                        understanding
                                .setQueryInterpretation(R.string.P_forecast_restriction);
                        return reply;
                    }
                }

                fetchWWOReport(org.getLocation());
                if (weather != null)
                    forecast = weather.getForecastFor(dt);
                return reply;
            }
            if ((understanding.getQueryInterpretation()!=null||!isEmpty(understanding.getQuery()))&&
                    !(cmdRoute || cmdLearn || cmdTraffic)
                    ) {
                if (!toastIsShown) {
                    Output.sayAndShowFromGui(context, understanding
                            .getQueryInterpretation().getToSay(), understanding
                            .getQuery(), null, true, false);
                } else
                    MyTTS.syncSayFromGUI(context,understanding.getQueryInterpretation()
                            .getToSay());
            }
        }

		/*
		 * // fake originLocation=//new DoublePoint(47.6069,-122.3304); //
		 * distance new DoublePoint(37.79562,-122.4); // occupancy
		 */
        if (ok)
            lOk: switch (0) {
                default:
                    if (cmdTraffic) {
                        Origin dst = understanding.getDestination();
                        if (null == dst) {
                            MyTTS.syncSayFromGUI(context,understanding.getQueryInterpretation()
                                    .getToSay());
                            mainActivity.loadMapFromBg();
                            reply.setTrafficReports(fetchTrafficReports(originLocation));
                            return reply;
                        } else {
                            Log.i(TAG,
                                    "Calculating traffic and ETA to "
                                            + dst.getFullAddress());
                            resetStatus(originLocation, dst.getLocation(), reply);
                            EtaMonitor monitor = new EtaMonitor();
                            boolean isFromCurrentLocation = true; // TODO: not
                            // always true!
                            monitor.getRoutesAndEtas(originLocation.toString(),
                                    dst.getFullAddress(), isFromCurrentLocation,
                                    true);

                            return reply;
                        }
                    }

                    if (cmdNews) {
                    	reply.setTrafficReports(fetchNews(originLocation,
                    			understanding.getDescription()));
                        return reply;
                    }

                    if (!force
                            && (us.status.getNumberOfAvailableOptions() > 0)
                            || understanding.isConfirmationRequired()) {

                        if (understanding.isConfirmationRequired()) {
                            cleintState=SN_YES_NO;
                            us.savedReply = understanding;
                            return reply;
                        }

                        DoublePoint oldLocation = us.status
                                .getDestinationLocation();

                        if (oldLocation == null)
                            oldLocation = us.status.getOriginLocation();

                        if ((oldLocation != null)) {
                            if (oldLocation.distanceInMeters(originLocation) > App.self
                                    .getResources().getInteger(
                                            R.integer.far_away_distance)) {
                                // more than 50km
                                if (us.status.isFresh()
                                        || mainActivity.isSpecCommandRecent()) {
                                    us.savedReply = understanding;
                                    return reply;
                                }
                            }
                        }
                    }

                    if (org.anyPoiInfo() || cmdGas) {
                        PoiLike pois[] = null;
                        PoiLikeGeoSpannable rp = null;

                        String q = cmdGas ? "gas stations" : org.getPoiName();
                        if (isEmpty(q))
                            q = org.getPoiCategory();
                        if (!isEmpty(q)) {
                            rp = fetchPoiInfo(understanding,q, originLocation,
                                    !isEmpty(org.getPoiCategory()),
                                    org.getFullAddress(),
                                    understanding.getRadMeters(),
                                    cmdGas ? GasReply.class : PoiReply.class);
                            if (rp != null) {
                                rp.calculate(context, understanding);
                                if (rp instanceof GasReply)
                                    reply.setGasStations((GasReply) rp);
                                else
                                    reply.setPois(rp);
                                pois = (PoiLike[]) rp.getFacilities();
                                Log.d(TAG, "pois are set from the poi server");
                            }
                        }

                        if (isEmpty(pois))
                            break lOk;

                        if (cmdSearch || cmdGas) {
                            rp.preloadImages(cmdSearch ? 1 : 0);
                            if (cmdSearch) {
                                final PoiLikeGeoSpannable rpp = rp;
                                new Thread() {
                                    @Override
                                    public void run() {
                                        rpp.preloadImages();
                                    }
                                }.start();
                            }
                            reply.setMode(cmdGas ? MAStatus.MODE_GAS
                                    : MAStatus.MODE_POI);
                            resetStatus(originLocation, destinationLocation, reply);
                            return reply;
                        }

                        // for parking
                        originLocation = pois[0].getPoint();

                    }

                    if (cmdLearn) {
                        if (understanding.isConfirmationRequired()) {
                            // it never works now
                            cleintState=SN_YES_NO;
                            us.savedReply = understanding;
                            MyTTS.syncSayFromGUI(context, understanding.getQueryInterpretation()
                                    .getToSay());
                        } else {

                            understanding
                                    .setQueryInterpretation(actualUnderstanding
                                            .getQueryInterpretation());

                            sayFromGui(understanding.getQueryInterpretation()
                                    .getToSay());

                            understanding.learn();
                            LearnAttribute la = understanding.getLearnAttribute();
                            if (!isEmpty(la.getAfterTextInput()))
                                MyTTS.syncSayFromGUI(context, la.getAfterTextInput());
                        }
                        doNothing = true;
                        return reply;
                    }

                    if (originLocation != null) {
                        reply.setMode(MAStatus.MODE_PARKING);

                        us.status.setOriginLocation(null);

                        reply.setParking(hanldeNewParkingLocation(originLocation,
                                destinationLocation, reply));
                    }

                    return reply;
            } // ok

        doNothing = true;
        if ((org != null) && !org.isLocationGood()
                && !isEmpty(org.getOnGcError()))
            sayFromGui(org.getOnGcError());
        else
            sayFromGui(R.string.POI_none_at_location);
        return reply;
    }

    private boolean processAdToPush(Understanding understanding) {

        final PushAd ad=understanding.getPushAd();
        if (ad==null) return false;

        final boolean doPushAd[] = {true};
        boolean useAppPackageFilter = ad.getPushAdIfAppNotThere();
        String packageName = ad.getPushAdPackage();

        if (useAppPackageFilter && !Utils.isEmpty(packageName)) {
            boolean isAppInstalled = Utils.isAppInstalled(packageName);
            doPushAd[0] = !isAppInstalled;
        }

        // app already installed
        if (!doPushAd[0]) {
            AdManager.sendToServer(packageName, AdManager.TRACKING_STATUS_ALREADY_INSTALLED);
            understanding.setPushAd(null); // cancel all
            return false;
        }

        String fgu=ad.getUrl();
        if (!isEmpty(fgu)) try {
            Log.d(TAG,"rdu0: "+fgu);
            speakText(ad.getPushAdPhrase());
            while (fgu.startsWith("http")) {
                HttpURLConnection uc=(HttpURLConnection)(new URL(fgu).openConnection());
                uc.setAllowUserInteraction(false);
                uc.setInstanceFollowRedirects(false);
                try {
                    uc.connect();
                } catch (IOException x) {
                    understanding.setPushAd(null);
                    AdManager.sendToServer(packageName, AdManager.TRACKING_STATUS_INVALID_URL);
                    return false;
                }
                String loc=uc.getHeaderField("Location");
                uc.disconnect();
                if (isEmpty(loc)) break;
                fgu=loc;
                if (UnderstandingProcessor.this.isCancelled()) {
                    understanding.setPushAd(null);
                    return false;
                }
            }
            Log.d(TAG,"rdu1: "+fgu);
            ad.setPackageUrl(fgu);
            if (!Utils.isPlayStoreUrl(fgu)) {
                final String fgu0=fgu;
                // try to resolve with web view
                Log.d(TAG, "the application is not resolved by server redirect");
                //doPushAd=false;
                Utils.runInGuiAndWait(context,
                        new Runnable() {
                            @Override
                            public void run() {
                                doPushAd[0]=false;
                                WebView wv=new WebView(context);
                                wv.setWebViewClient(
                                        new WebViewClient() {
                                            public boolean  shouldOverrideUrlLoading(WebView view, String url) {
                                                Log.d(TAG,"wv000="+url);
                                                if (Utils.isPlayStoreUrl(url)) {
                                                    ad.setPackageUrl(url);
                                                    doPushAd[0]=true;
                                                    Log.d(TAG,"wv="+url);
                                                    return true;
                                                }
                                                return false;
                                            }

                                        }
                                );
                                Log.d(TAG,"wv");
                                wv.getSettings().setJavaScriptEnabled(true);
                                wv.loadUrl(fgu0);
                                Log.d(TAG,"wv1");
                            }
                        }
                );
                long t1=System.currentTimeMillis();
                while (System.currentTimeMillis()-t1<15000/*10s*/) {
                    try {
                        if (doPushAd[0]||UnderstandingProcessor.this.isCancelled()) break;
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // url not resolved
        if (!doPushAd[0]){
            AdManager.sendToServer(packageName, AdManager.TRACKING_STATUS_INVALID_URL);
            understanding.setPushAd(null);
            return false;
        }

        AdManager.sendToServer(packageName, AdManager.TRACKING_STATUS_IMPRESSION_RECEIVED);

        return true;
    }

    private void translate(Understanding u, MagReply reply) {

        String fromLanguage = u.getFromLanguage();
        String text = u.getDescription();

        if (Utils.isEmpty(u.getFromLanguageCode()))
            fromLanguage = GoogleTranslateFetcher.getSystemLang();

        final String fromLanguageCode = Utils.isEmpty(u.getFromLanguageCode())?
                GoogleTranslateFetcher.getSystemLangCode():u.getFromLanguageCode();

        Props props = Props.getInstance(App.self);
        String def_lang_to = u.getLanguageCode();
        if (Utils.isEmpty(def_lang_to)) {
            def_lang_to = props.getProperty("default_lang_to_translate");
            if (Utils.isEmpty(def_lang_to))
                if (GoogleTranslateFetcher.getSystemLangCode().equals(fromLanguageCode))
                    def_lang_to = "it";
                else
                    def_lang_to = GoogleTranslateFetcher.getSystemLangCode();
        }

        // from one to one ???
        if (def_lang_to.equals(fromLanguageCode))
            if (def_lang_to.equals(GoogleTranslateFetcher.getSystemLangCode()))
                def_lang_to = "it";
            else
                def_lang_to = GoogleTranslateFetcher.getSystemLangCode();

        final String toLanguageCode = def_lang_to;

        if (!Utils.isEmpty(u.getLanguageCode()))
            props.setAndSave("default_lang_to_translate", u.getLanguageCode());

        if (Utils.isEmpty(text)) {

            VR.useLanguage = fromLanguage;

            Understanding u2 = new Understanding()
                    .setCommandCode(Understanding.CMD_NOP);
            reply.setUnderstanding(u2);

            Output.sayAndShow(context, App.self.getString(R.string.P_SAY_YOUR_TEXT_TO_TRANSLATE) + " " + fromLanguage);
            listenAfterTheSpeech();

            CmdHandlerHolder.setCommandHandler(new LocalCommandHandler() {

                @Override
                public void abort(int x) {
                    CmdHandlerHolder.setCommandHandler(null);
                }

                @Override
                public boolean onVoiceInput(List<String> matches, boolean partial) {

                    if (partial)
                        return true;

                    abort(0);
                    if (matches == null || matches.size() < 1)
                        return true;

                    // translate and say result
                    new GoogleTranslateFetcher(fromLanguageCode, toLanguageCode, matches.get(0));

                    return true;
                }
            });

            return;
        }

        // translate and say result
        new GoogleTranslateFetcher(fromLanguageCode, toLanguageCode, text);
    }

    protected boolean doNothing = false;

    void _resetStatus(DoublePoint orgLoc, DoublePoint dstLoc, MagReply reply) {
        Understanding understanding = reply.getUnderstanding();
        try {
            us.prevStatus = us.status.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        us.status.setOriginLocation(orgLoc);
        us.status.setDestinationLocation(dstLoc);
        us.status.setSelectedParking(null);
        us.status.setParkingResponse(null);
        us.status.setParkings(null);
        mainActivity.feedController = null;

        switch (understanding.getCommandCode()) {
            case Understanding.CMD_SEARCH:
                us.status.setMode(MAStatus.MODE_POI);
                break;
            case Understanding.CMD_PARKING:
                us.status.setMode(MAStatus.MODE_PARKING);
                break;
            case Understanding.CMD_GAS:
                us.status.setMode(MAStatus.MODE_GAS);
                break;
            case Understanding.CMD_TRAFFIC:
                us.status.setMode(MAStatus.MODE_TRAFFIC);
        }

        us.status.setPois(null);
        us.status.setSelectedPoi(null);
        mainActivity.pksOverlay.doPopulate();
        mainActivity.poisOverlay.doPopulate();

        us.status.setGasStations(null);
        us.status.setSelectedGasStation(null);

        mainActivity.gasOverlay.doPopulate();

        if (dstLoc != null && orgLoc != null) {
            mainActivity.setDefaultZoom();
            DoublePoint ct = DoublePoint.center(orgLoc, dstLoc);
			mainActivity.showMark2(dstLoc);
            mainActivity.showLocation(ct);
            mainActivity.zlAfterAdjust = mainActivity.mv.moveAndZoomToCover(
                    orgLoc, dstLoc);
        } else {
			mainActivity.showMark2(orgLoc);
            if (orgLoc != null)
                mainActivity.showLocation(orgLoc);
        }
        mainActivity.setLogo();

        mainActivity.mv.invalidate();
    }

    void resetStatus(final DoublePoint originLocation,
                     final DoublePoint destinationLocation, final MagReply reply) {
        mainActivity.loadMapFromBg();
        mainActivity.mv.waitForMaturing();
        Utils.runInGuiAndWait(context, new Runnable() {
            public void run() {
                _resetStatus(originLocation, destinationLocation, reply);
            }
        });
    }

    PkResponse hanldeNewParkingLocation(final DoublePoint originLocation,
                                        final DoublePoint destinationLocation, final MagReply reply) {

        resetStatus(originLocation, destinationLocation, reply);
        Understanding understanding = reply.getUnderstanding();

        Log.d(TAG + ".originLocation", originLocation.toString());
        int radius = Utils.isEmpty(understanding.getRadMeters()) ? 700
                												  : understanding.getRadMeters();
        URL pku = createParkingRequest(originLocation, radius);
        Log.d(TAG + ".parkingRequest", pku.toString());
        try {
            InputStream pkis = invokeRequest(pku, null, null, null);
            if (pkis != null) {
                JSONTokener jsto = new JSONTokener(pkis);
                JSONObject jso = new JSONObject(jsto);
                // speakText("we have some parking info");
                pkis.close();

                Log.d(TAG + ".parkingResponse: ", jso.toString(3));

                Element el = Json.convertToDom(jso);

                // Log.d(TAG+".parkingResponse.dom:",Xml.domToText(el).toString());

                PkResponse pkrsp = Xml.setPropertiesFrom(el, PkResponse.class);

                //Log.d(TAG + ".parkingResponse.dom:", pkrsp.toString());

                pkrsp.calculate(App.self, understanding);

                return pkrsp;
            }
        } catch (Exception e) {
            if (!App.self.isReleaseBuild)
                sayFromGui(e.getMessage());
            e.printStackTrace();
            return null;
        }

        if (!mainActivity.silentParking)
            sayFromGui(
                    R.string.mainactivity_hanldenewparkinglocation_data_source_is_silent
            );

        return null;
    }

    /** Second phase of processing request - performed in GUI Thread */
    @Override
    protected void onPostExecute(MagReply reply) {
        super.onPostExecute(reply);

        if (reply == null || reply.getUnderstanding() == null) {
        	exactNcePlace("UP1");
        	String s=Utils.getString(R.string.mainactivity_onpostexecute_network_not_connected);
        	
            if (Config.show_nce_place) {
              String s1=s+" "+exactNcePlace;
              if (exactNceException!=null) s1+=" "+exactNceException.getMessage();
              Output.sayAndShow(context, s1, s, false); 
            } else
              speakText(s);
            return;
        }

        if (reply.isProcessedByHandlerInFg()) return;

        final Understanding understanding = reply.getUnderstanding();

        if (understanding.isError()) {
            QueryInterpretation qi = understanding.getQueryInterpretation();
            if (qi != null)
                Output.sayAndShow(context, qi.getToShow(), qi.getToSay(),
                        understanding.getQuery(), false);

            listenAfterTheSpeech();
            return;
        }

        //is this an intent code snippet?
        String snippet = understanding.getSnippet();
        String snippetType = understanding.getSnippetType();
        if (snippet != null && snippetType != null && snippetType.equalsIgnoreCase("android_intent")) {
            try {
                Intent snippetIntent = Intent.parseUri(snippet,0);
                context.startActivity(snippetIntent);
            } catch (Exception e) {}
        }


        //is this a user who does not no what he wants? and just saying hello?
        if(Understanding.CMD_HELLO==understanding.getCommandCode()){
            sayAndShow(App.self.robin().suggestSomething());
            listenAfterTheSpeech();
            return;
        }

        //is robin waiting for a response?
        if(App.self.robin().expectedResponse!=null){
            boolean success = App.self.robin().handleResponse(understanding.getCommandCode());
            if (success)return;
        }

        // should we show an ad?
        PushAd ad = understanding.getPushAd();
        if (ad != null) {
            final String fgURL = ad.getPackageUrl();
            if (!Utils.isEmpty(fgURL)) {
                //speakText(ad.getPushAdPhrase());
                Launchers.launchPlayStore(context, fgURL);
                try {
                    RobinDB rdb=RobinDB.getInstance(context);
                    ad.setWhenRequested(new Date());
                    rdb.save(ad);
                } catch(Throwable t) { t.printStackTrace();  }
            }

            App.self.voiceIO.fireOpes();
            return;
        }

        App.self.voiceIO.setShouldListenAfterCommand(understanding.isActivateMicrophone());

        if (doNothing) {
            condListenAfterTheSpeech();
            return;
        }

        MagReplyHandler ch = CmdHandlerHolder.getMagReplyHandler();
        if (ch != null) {
            if ((mainActivity!=null)&&(ch instanceof ISwitchToBubbles)&&((ISwitchToBubbles)ch).shouldSwitchToBubbles())
                mainActivity.showBubles();

            if (ch.handleReplyInUI(reply)) {
                reply.setProcessedByHandlerInFg(true);
                return;
            }
        }

        if (mainActivity!=null) {
            if (mainActivity.isWebViewShown()) {
                switch (understanding.getCommandCode()) {
                    case Understanding.CMD_PREV:
                        if (!mainActivity.webView.hanldeBack())
                            mainActivity.switchMode(false);
                        // if (webView.canGoBack()) webView.goBack();
                        fireOpes();
                        return;
                    case Understanding.CMD_OTHER:
                        if (mainActivity.webView.canGoForward())
                            mainActivity.webView.goForward();
                        fireOpes();
                        return;
                }
            }

            if (switchToBubles(understanding.getCommandCode()))
                mainActivity.showBubles();

            if (switchToMap(understanding.getCommandCode()))
                mainActivity.showMap();

            if (mainActivity.feedController != null) {
                switch (understanding.getCommandCode()) {
                    case Understanding.CMD_PREV:
                        mainActivity.feedController.readPrevious();
                        return;
                    case Understanding.CMD_OTHER:
                        mainActivity.feedController.readNext();
                        return;
                    case Understanding.CMD_REPEAT:
                        mainActivity.feedController.readAgain();
                        return;
                    case Understanding.CMD_DETAILS:
                        mainActivity.feedController.readDetailed();
                        return;
                }
            }
        }

        if (understanding.getCommandCode() == Understanding.CMD_INITIALIZATION) {
            QueryInterpretation qi = understanding.getQueryInterpretation();
            if (qi != null)
                Output.sayAndShow(context, qi.getToShow(), qi.getToSay(),
                        understanding.getQuery(), false);

            if (App.self.getCountExecution()==Consts.GREETING_SAY_RUN_TIMES&&
                    App.self.isGreetingDisabled()
                    ) Output.sayAndShow(context,R.string.P_popping_up_silently);

            listenAfterTheSpeech();
            return;
        }


        if (FacebookStatusCommandHandler.isActiveCommand()) {
            if (FacebookStatusCommandHandler.handleUI())
                return;
        }
        if (RepeatCommandHandler.handleUI())
            return;

        PkResponse parkingInfo = reply.getParking();
        GeoSpannable<Poi> poisInfo = reply.getPois();
        GasReply gasInfo = reply.getGasStations();

        if (mainActivity!=null) mainActivity.setLogo();

        int cmd = reply.getCommandCode();

        if (cmd == Understanding.CMD_SEARCH || cmd == Understanding.CMD_PARKING
                || cmd == Understanding.CMD_GAS) {
            mainActivity.initMap();

            boolean anyInfo = false;

            if (parkingInfo != null) {
                anyInfo = true;
                int ca = parkingInfo.countAvailable();
                if (ca > 0) {

                    us.status.setParkingResponse(parkingInfo);
                    us.status.updateTime();
                    us.status.setParkings(us.status
                            .getParkingResponse().facilities());

                    String prefix = "";
                    if (mainActivity.indirectParkingSearch) {
                        prefix = App.self
                                .getString(R.string.mainactivity_onpostexecute_just_in_case)
                                + " ";
                        mainActivity.indirectParkingSearch = false;
                    }

                    if (ca == 1) {
                        speakText(prefix
                                + App.self
                                .getString(R.string.mainactivity_onpostexecute_one_parking_option));
                    } else {
                        // speakText("I found  " + ca +
                        // " parking options for you");
                        speakText(prefix + pickFoundOptionsPhrase());
                        sayOrder(understanding.getOrderBy());
                    }

                    mainActivity.currentController = mainActivity.pksController;
                    mainActivity.pksOverlay.doPopulate();
                    mainActivity.pksController.select(us.status
                            .getParkings().next(), true);

                    if (poisInfo != null) {
                        us.status.setPois(poisInfo);
                        mainActivity.poisOverlay.doPopulate();
                    }

                    if (gasInfo != null) {
                        us.status.setGasStations(gasInfo);
                        mainActivity.gasOverlay.doPopulate();
                    }

                    us.waitingForConfirmation = false;

                    mainActivity.setLogo();

                    fireOpes();
                    return;
                } else
                    mainActivity.setDefaultZoom();
                if (!mainActivity.silentParking)
                    mainActivity.handleNoParking(understanding);
            }

            if ((cmd == Understanding.CMD_SEARCH) && poisInfo != null) {
                anyInfo = true;
                GeoObject obs[] = poisInfo.getFacilities();
                if (!isEmpty(obs)) {
                    int ca = obs.length;
                    // ///////////
                    if (ca > 0) {

                        us.status.setPois(poisInfo);
                        us.status.updateTime();

                        String poiName = understanding.getOrigin().getPoiName();
                        if (poiName == null)
                            poiName = "";

                        if (ca == 1) {
                            // speakText("I found one "+poiName+" option for you. It ");
                            speakText(App.self
                                    .getString(R.string.mainactivity_onpostexecute_found_one));
                        } else {
                            // speakText("I found  " + ca +
                            // " parking options for you");
                            
//                        	speakText(pickFoundOptionsPhrase(poiName));
                            sayOrder(understanding.getOrderBy());
                        }

                        mainActivity.currentController = mainActivity.poisController;
                        mainActivity.poisOverlay.doPopulate();

                        mainActivity.poisController.select(us.status
                                .getPoiIterator().next(), true);

                        if (Utils.in1per3times())
                            Output.sayAndShow(context,R.string.P_first_POI_hint);

                        us.waitingForConfirmation = false;

                        mainActivity.setLogo();

                        fireOpes();
                        return;
                    }
                    mainActivity.handleNoOption(understanding, true);

                    // ///////////
                }
            }

            if ((cmd == Understanding.CMD_GAS) && gasInfo != null) {
                anyInfo = true;
                GeoObject obs[] = gasInfo.getFacilities();
                if (!isEmpty(obs)) {
                    int ca = obs.length;
                    // ///////////
                    if (ca > 0) {

                        us.status.setGasStations(gasInfo);
                        us.status.updateTime();

                        String poiName = App.self
                                .getString(R.string.mainactivity_onpostexecute_gas_station);

                        if (ca == 1) {
                            speakText(App.self
                                    .getString(R.string.mainactivity_onpostexecute_found_one)
                                    + " "
                                    + poiName
                                    + " "
                                    + App.self
                                    .getString(R.string.mainactivity_onpostexecute_gas_station_for_you));
                        } else {
                            // speakText("I found  " + ca +
                            // " parking options for you");
                        	
//                            speakText(pickFoundOptionsPhrase(poiName));
                            sayOrder(understanding.getOrderBy());
                        }

                        mainActivity.currentController = mainActivity.gasController;
                        mainActivity.gasOverlay.doPopulate();

                        mainActivity.gasController.select(us.status
                                .getGasStationIterator().next(), true);

                        us.waitingForConfirmation = false;

                        fireOpes();
                        return;
                    }
                    mainActivity.handleNoOption(understanding, true);

                    // ///////////
                }
            }

            if (anyInfo) {
                if (!mainActivity.silentParking
                        && (us.prevStatus
                        .getNumberOfAvailableOptions() > 0)) {
                    speakText(R.string.P_want_to_return);
                    us.waitingForConfirmation = true;

                }
                if (mainActivity.silentParking)
                    fireOpes();
                else
                    listenAfterTheSpeech();
                return;
            }

        }

        // GeoSpannable<Poi> poisInfo=reply;

        if (cmd != Understanding.CMD_NOP && cmd != Understanding.CMD_UNKNOWN && !Understanding.isLikeUnknown(cmd)) {
            switch (cmd) {
                case Understanding.CMD_SEARCH:
                case Understanding.CMD_PARKING:
                case Understanding.CMD_GAS:
                    if (us.savedReply != null) {
                        speakText(us.savedReply.getOrigin()
                                .isLocationGood() ? R.string.P_sure_to_go_there
                                : R.string.P_sure);
                        listenAfterTheSpeech();
                    } else
                        fireOpes();
                    us.waitingForConfirmation = false;
                    return;
                case Understanding.CMD_NO:
                    us.waitingForConfirmation = false;
                    if (us.savedReply != null) {
                        switch (us.savedReply.getCommandCode()) {
                            case Understanding.CMD_LEARN:
                                String vp = us.savedReply.getLearnAttribute()
                                        .getVoiceInputPrompt();
                                speakText(isEmpty(vp) ? understanding
                                        .getQueryInterpretation() : vp);
                                mainActivity
                                        .learnFromTextInput(us.savedReply);
						/*
						 * break; case Understanding.CMD_ROUTE:
						 * restoreLocationAndMark();
						 */
                        }
                        String onNo = us.savedReply
                                .getConfirmationRequiredOnNo();
                        us.savedReply = null;
                        if (isEmpty(onNo)) {
                            fireOpes();
                            return;
                        }
                        break;
                    }
                    condListenAfterTheSpeech();
                    return;
                case Understanding.CMD_YES:
                case Understanding.CMD_DO_IT:
				/*
				if (!packageName.waitingForConfirmation) {
					packageName.fireOpes();
					return;
				}
				*/
                    us.waitingForConfirmation = false;
                    break;
                case Understanding.CMD_GO_BACK:
                    us.status = us.prevStatus;
                    mainActivity.currentController.getOverlay().doPopulate();
                    if (mainActivity.currentController.size() > 0)
                        mainActivity.currentController.select(
                                mainActivity.currentController.getSelected(), true);
                    else if (us.status.getOriginLocation() != null)
                        mainActivity.showLocation(us.status
                                .getOriginLocation());

                    us.prevStatus = new MAStatus();
                    fireOpes();
                    return;
                case Understanding.CMD_FORGET:
                    if (understanding.isConfirmationRequired()) {
                        cleintState=SN_YES_NO;
                        understanding.setConfirmationRequired(false);
                        us.savedReply = understanding;
                        understanding.getQueryInterpretation().sayAndShow(context);
                        listenAfterTheSpeech();
                    } else {
                        VR vr = VR.get();
                        if (vr != null) {
                            vr.teachClear();
                            sayAndShow(R.string.P_teach_forget);
                        }
                        condListenAfterTheSpeech();
                    }
                    return;

                case Understanding.CMD_TEACH:
                    CrowdBrain.shortcutMenu(mainActivity, null, new CrowdBrain.ChangeListener() {
                        @Override
                        public void onNewIntentAdded(Map.Entry<String, String> entry) {
                            VR vr = VR.get();
                            if (vr != null) {
                                LearnedAnswer qa = new LearnedAnswer();
                                qa.setQuestion(entry.getKey());
                                qa.setAnswer(entry.getValue());
                                qa.setSay(0);
                                vr.teachAdd(qa);
                            }
                        }
                    });
                    return;
            }

			/*
			 * or the user's previous command was next/previous/closer/cheaper,
			 * and was given within the last 1 minute
			 */
            switch (cmd) {
                case Understanding.CMD_OTHER:
                case Understanding.CMD_PREV:
                case Understanding.CMD_CHEAP:
                case Understanding.CMD_CHEAPEST:
                case Understanding.CMD_NEAR:
                case Understanding.CMD_NEAREST:
                    mainActivity.updateSpecCommandTime();
            }

            if (mainActivity!=null) {
                boolean parkingMode = us.status.getMode() == MAStatus.MODE_PARKING,
                        gasMode = us.status
                                .getMode() == MAStatus.MODE_GAS;
                switch (cmd) {
                    case Understanding.CMD_COST:
                    case Understanding.CMD_CHEAP:
                    case Understanding.CMD_CHEAPEST:
                        if (parkingMode || gasMode)
                            break;
                    case Understanding.CMD_VACANCY:
                        if (parkingMode)
                            break;

                        listenAfterTheSpeech();
                        return;
                }
            }

            final QueryInterpretation qi = understanding
                    .getQueryInterpretation();

            switch (cmd) {
                // this switch is to suppress "speakText" in some commands only
                // don't use for any other purpose
                case Understanding.CMD_OTHER:
                case Understanding.CMD_PREV:
                case Understanding.CMD_ROUTE:
                case Understanding.CMD_TRAFFIC:
                case Understanding.CMD_GAS:
                case Understanding.CMD_CHEAP:
                case Understanding.CMD_CHEAPEST:
                case Understanding.CMD_NEAR:
                case Understanding.CMD_NEAREST:
                case Understanding.CMD_DETAILS:
                case Understanding.CMD_PLAY_TWITTER:
                case Understanding.CMD_RETWEET:
                case Understanding.CMD_MORE:
                case Understanding.CMD_CALL:
                case Understanding.CMD_ALARM:
                case Understanding.CMD_REMINDER:
                case Understanding.CMD_FACEBOOK:
                case Understanding.CMD_READ:
                case Understanding.CMD_INFO:
                case Understanding.CMD_INTRO_VIDEO:
                case Understanding.CMD_FACEBOOK_STATUS:
                case Understanding.CMD_MY_BIRTHDAY:
                case Understanding.CMD_LEARN_BIRTHDAY:
                case Understanding.CMD_DICTATE:
                case Understanding.CMD_VOICE_MAIL:
                case Understanding.CMD_FACEBOOK_NEWS:
                case Understanding.CMD_NEWS:
                case Understanding.CMD_HOROSCOPE:
                case Understanding.CMD_SHARELOCATION:
                case Understanding.CMD_TEACH:
                case Understanding.CMD_ROBIN_SETTINGS:
                case Understanding.CMD_CLOSE_APP:
                case Understanding.CMD_SWITCH_TO_ENGLISH:
                    break;
                case Understanding.CMD_LEARN:case Understanding.CMD_JOKE:case Understanding.CMD_JOKE_SEXY:
                    qi.sayAndShow(context);
                    break;
                default:
                    speakText(understanding.getQueryInterpretation());
            }

            if (understanding.isConfirmationRequired()) {
                cleintState=SN_YES_NO;
                listenAfterTheSpeech();
                return;
            } else
                switch (cmd) {
                    case Understanding.CMD_AUTOADVERT:
                        mainActivity.fbHelper.shareAppToFeed(
                                understanding.getDescription()
                        );
                        getBirthDayFromFBSilently();
                        return;
                }

            switch (cmd) {

                case Understanding.CMD_VOICE_MAIL:
                    Output.sayAndShow(context, understanding
                            .getQueryInterpretation().getToShow(), understanding
                            .getQueryInterpretation().getToSay(), false);
                    mainActivity.getAM().setSpeakerphoneOn(true);
                    Launchers.directdial(context, App.self.getVoiceMailNumber());
                    break;
                case Understanding.CMD_LEARN_BIRTHDAY: {
                    Output.sayAndShow(context, understanding
                            .getQueryInterpretation().getToShow(), understanding
                            .getQueryInterpretation().getToSay(), understanding
                            .getQuery(), false);
                    LearnAttribute la = new LearnAttribute(R.string.PfBirthday);
                    la.learnDate(understanding.getTimedate());
                    condListenAfterTheSpeech();
                    return;
                }
                case Understanding.CMD_MY_BIRTHDAY:
                    LearnAttribute la = new LearnAttribute(R.string.PfBirthday);
                    if (isEmpty(la.getDefinition())) {
                        la.learnDate(mainActivity);
                    } else {
                        sayAndShow(understanding.getQueryInterpretation());
                    }

                    break;
                case Understanding.CMD_DICTATE:
                    break;
                case Understanding.CMD_LEARN:
                    if ("forget".equals(understanding.getAction())) {
                        understanding.getLearnAttribute().forget();
                        mainActivity.learnFromTextInput(understanding);
                    } else
                        fireOpes();
                    return;
                case Understanding.CMD_CHEAP:
                case Understanding.CMD_CHEAPEST:
                case Understanding.CMD_NEAR:
                case Understanding.CMD_NEAREST:
                    if (mainActivity.currentController==null||mainActivity.currentController.size() == 0) {
                        mainActivity.handleNoParking(understanding);
                        return;
                    }
				/*
				 * if (parkingMode)
				 * status.setParkings(status.getParkingResponse().facilities());
				 * else currentController.getIterator().reset();
				 */
                    mainActivity.currentController
                            .setIterator(mainActivity.currentController
                                    .getSpannable().facilities());

                    mainActivity.currentController.getOverlay().doPopulate();
                    sayOrder(mainActivity.currentController.getSpannable()
                            .getOrderBy());
                    mainActivity.currentController.select(
                            mainActivity.currentController.getIterator().next(),
                            true);
                    return;
            }

            switch (cmd) {
                case Understanding.CMD_TURNOFF:
                    MyTTS.execAfterTheSpeech(new Runnable() {
                        @Override
                        public void run() {
                            Robin.shutdown();
                            /*SuziePopup sp = SuziePopup.get();
                            if (sp != null)
                                sp.byeAnimation();*/
                        }
                    });
                    // CHEK: It was closed before speech end
                    // finish();
                    break;
                case Understanding.CMD_HOROSCOPE: {
                    Output.sayAndShow(context, understanding
                            .getQueryInterpretation().getToShow(), understanding
                            .getQueryInterpretation().getToSay(), understanding
                            .getQuery(), false);
                    // if birthday saved in settings don't ask FB to birthday
                    LearnAttribute la = new LearnAttribute(R.string.PfBirthday);
                    if (!isEmpty(la.getDefinition())) {
                        SimpleDateFormat formatter = new SimpleDateFormat(
                                "dd/MM/yyyy");
                        try {
                            final EventSource es = progressIndicatorHolder.showProgress();
                            final Date bDate = formatter.parse(la.getDefinition());
                            new Thread(new Runnable() {
                                public void run() {
                                    final String dsc = fetchHoroscope(
                                            UserLocationProvider
                                                    .readLocationPoint(), bDate);
                                    Utils.runInMainUiThread(context, new Runnable() {
                                        @Override
                                        public void run() {
                                            es.fireEvent();
                                            if (!isEmpty(dsc))
                                                sayAndShow(dsc);
                                            listenAfterTheSpeech();
                                        }
                                    });
                                }
                            }).start();
                        } catch (ParseException e) {
                            es.fireEvent();
                            getBirthDayFromFBAndReadHoroscope();
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    } else {
                        getBirthDayFromFBAndReadHoroscope();
                    }
                    return;
                }
                case Understanding.CMD_OPENAPP:
                    break;
                case Understanding.CMD_YOUTUBE:
                    youtube(context, understanding.getDescription());
                    break;
                case Understanding.CMD_UPDATE:
                    lookAtMarketFor(context,MainActivity.class.getPackage().getName());
                    break;
                case Understanding.CMD_CALENDAR:
                    mainActivity.calReminder.sayRemindersForDay(understanding
                            .getTimedate());
                    launchAgenda(context,understanding.getTimedate());
                    break;
                case Understanding.CMD_NAVIGATE:
                    launchBrowser(context);
                    break;
                case Understanding.CMD_OPEN_URL:
                    if ((mainActivity!=null)&&understanding.isShowEmbedded()) {
                        mainActivity.openUrl(understanding, reply.getScriptCode());
                        condListenAfterTheSpeech();
                        return;
                    } else {
                        Launchers.launchBrowser(context,understanding.getUrl());
                    } break;
                case Understanding.CMD_TRANSLATE:
                    break;
                case Understanding.CMD_BBALL_NEWS:
                    // TODO: remove hard-coded!!
                    String chadwickUrl = context.getString(R.string.chadwick_webapp_url);
                    if (mainActivity==null)
                        Launchers.launchBrowser(context,chadwickUrl);
                    else
                        mainActivity.openUrl(chadwickUrl, true, null, null);
                    break;
                case Understanding.CMD_TIME:
                    listenAfterTheSpeech();
                    break;
                case Understanding.CMD_FEEDBACK:
                    com.magnifis.parking.Log.collectLogThenComposeFeedback(context);
                    break;
                case Understanding.CMD_CAMERA:
                    launchCamera(context);
                    break;
                case Understanding.CMD_RETWEET:
                    mainActivity.twitterPlayer.retweetLast(understanding);
                    break;
                case Understanding.CMD_MORE:
                    if (mainActivity.feedController != null) {
                        mainActivity.feedController.coutinueReading();
                        return;
                    }
                    mainActivity.twitterPlayer.playMore(understanding);
                    break;
                case Understanding.CMD_PLAY_TWITTER:
                    mainActivity.feedController = null;
                    mainActivity.twitterPlayer.play(understanding);
                    break;
                case Understanding.CMD_PLAY:
                {
                    int playMusicRes = Launchers.launchMusicTuner(
                            context,
                            understanding.getDescription(),
                            (Utils.doesActivityHandleIntent(topActivity, MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)
                                    ?topActivity.getPackageName()
                                    :null
                            )
                    );
                    if (playMusicRes == 0) {
                        speakText(App.self.getString(R.string.P_NO_MUSIC_APP));
                    }
                    break;
                }
                case Understanding.CMD_READ:
                    Output.sayAndShow(mainActivity, understanding
                            .getQueryInterpretation().getToShow(), understanding
                            .getQueryInterpretation().getToSay(), understanding
                            .getQuery(), false);
                    if ("facebook".equals(understanding.getChannel())) {
                        mainActivity.fbHelper.beginReading(context,true,understanding
                                .getQueryInterpretation().getToSay());
                        getBirthDayFromFBSilently(); // should be removed completely
                        return;
                    }
                    if ("sms".equals(understanding.getChannel())) {
                        String isNew = understanding.getFilter();
                        if (isNew == null || isNew.length() == 0)
                            isNew = "old";
                        else
                            isNew = isNew.toLowerCase();
                        mainActivity.smsController.beginReading(context,"new".equals(isNew),understanding
                                .getQueryInterpretation().getToSay());
                        return;
                    }
                    if ("mail".equals(understanding.getChannel())) {
                        mainActivity.mailController.beginReading(context,true,understanding
                                .getQueryInterpretation().getToSay());
                        return;
                    }
                    break;
                case Understanding.CMD_TWITTER:
                    composeTwitt(context);
                    break;
                case Understanding.CMD_FACEBOOK:
                    mainActivity.fbHelper.beginReading(context,true,understanding
                            .getQueryInterpretation().getToSay());
                    getBirthDayFromFBSilently();
                    break;
                case Understanding.CMD_FACEBOOK_NEWS:
                    (new DailyUpdate(App.self)).getNews();
                    break;
                case Understanding.CMD_ROBIN_SETTINGS:
                    MyTTS.speakText(understanding.getQueryInterpretation());
                    Intent it = new Intent(App.self, PrefsActivity.class);
                    startNestedActivity(context,it);
                    condListenAfterTheSpeech();
                    return;
                case Understanding.CMD_SETTINGS:
                    launchPhoneSettings(context);
                    break;
                case Understanding.CMD_MAIL:
                    composeEmail(context);
                    break;
                case Understanding.CMD_NOTE:
                    String myMail = App.self.getGmailAccountName();
                    if (!Utils.isEmpty(myMail))
                        SendCmdHandler.startReplyTo(App.self.getString(R.string.P_note_receiver)+" <"+myMail+">");
                    break;
                case Understanding.CMD_APP_NAME:
                    ComponentName cn = Utils.getTopActivity();
                    if (cn != null)
                        speakText(cn.getPackageName());
                    else
                        speakText("unknown application");

                    break;
                case Understanding.CMD_SHOW_ROBIN:
                    Intent it3 = new Intent(App.self, MainActivity.class);
                    it3.setAction(MainActivity.WAKE_UP);
                    Utils.startActivityFromNowhere(it3);
                    break;
                case Understanding.CMD_DRIVING:
                    App.self.switchCarMode(true, false);
                    // context.startService(new Intent(ApplicationMonitorAction.SWITCH_CAR_MODE.name(),
                    //			null, App.self, SuzieService.class).putExtra("flag", true));
                    break;
                case Understanding.CMD_NOT_DRIVING:
                    App.self.switchCarMode(false, false);
                    break;
                case Understanding.CMD_TEXT:
                    composeTextMessage(context,App.self.shouldAdvertInSms());
                    break;
                case Understanding.CMD_CONTACTS:
                    showContacts(context);
                    break;
                case Understanding.CMD_DETAILS: {
                    if (mainActivity.getSelected() == null) {
                        speakText(R.string.P_no_selection);
                        listenAfterTheSpeech();
                        return;
                    } else {
                        speakText(understanding.getQueryInterpretation());
                        mainActivity.currentController.showDetails();
                        // MainActivity.this.sayAgain(); // not really needed
                        // anymore
                    }
                    break;
                }
                case Understanding.CMD_COST: {
                    GeoObject go = mainActivity.getSelected();
                    if (go != null) {
                        String inf = go.getPriceInfoToSpeak();
                        if (inf != null) {
                            speakText(inf);
                            condListenAfterTheSpeech();
                            return;
                        }
                    }
                }
                speakText(R.string.P_no_details);
                listenAfterTheSpeech();
                return;
                case Understanding.CMD_VACANCY: {
                    PkFacility pkf = us.status.getSelectedParking();
                    if (pkf != null) {
                        sayVacancy(pkf);
                        condListenAfterTheSpeech();
                        return;
                    }
                }
                speakText(R.string.P_no_details);
                listenAfterTheSpeech();
                return;
                case Understanding.CMD_ZOOMIN:
                    if (mainActivity.mapController!=null)
                        mainActivity.mapController.zoomIn();
                    condListenAfterTheSpeech();
                    return;
                case Understanding.CMD_ZOOMOUT:
                    if (mainActivity.mapController!=null)
                        mainActivity.mapController.zoomOut();
                    condListenAfterTheSpeech();
                    return;
                case Understanding.CMD_SHARELOCATION:
                    Output.sayAndShow(context, understanding
                            .getQueryInterpretation().getToShow(), understanding
                            .getQueryInterpretation().getToSay(), understanding
                            .getQuery(), false);
                    mainActivity.shareCurrentLocation(understanding.getChannel());
                    break;
                case Understanding.CMD_MAP:
                    mainActivity.onMyLocation(null);
                    break;
                case Understanding.CMD_AUDIOBURST:
	                {
	                    ABFeed abf=reply.getAbFeed();
	                    AudioburstFeedController fc=new AudioburstFeedController(super.mainActivity, abf);
	                    mainActivity.feedController=fc;
                        fc.readAll();
	                    condListenAfterTheSpeech();
	                	return;
	                }
                case Understanding.CMD_NEWS:
                    Output.sayAndShow(context, understanding
                            .getQueryInterpretation().getToShow(), understanding
                            .getQueryInterpretation().getToSay(), understanding
                            .getQuery(), false);
                    

	                if (Utils.isEmpty(reply.getTrafficReports())) {
	                    	
	                } else {
		                    VoiceIO.playTextAlerts(reply.getTrafficReports(),
		                            App.self.getString(R.string.P_NEWS_INTRO));
	                }
                   
                    condListenAfterTheSpeech();
                    
                    return;
                case Understanding.CMD_TRAFFIC:
                    performTrafficCommand(understanding.getOriginLocation(),
                            understanding.getDestinationLocation(),
                            reply.getTrafficReports());
                    break;
                case Understanding.CMD_SATELLITE_VIEW:
                    mainActivity.switchMapView(true);
                    break;
                case Understanding.CMD_MAP_VIEW:
                    mainActivity.switchMapView(false);
                    break;
                // case Understanding.CMD_NAVIGATE:
                case Understanding.CMD_YES:
                    if (us.savedReply != null
                            && Understanding.CMD_ROUTE == us.savedReply
                            .getCommandCode())
                        mainActivity.performNavigationIfCan();
                    break;
                case Understanding.CMD_ROUTE: {
                    DoublePoint dstLocation = understanding
                            .getDestinationLocation();
                    if (dstLocation == null) {
                        // speakText();//.setCompletionListener(rrListen);
                        Output.sayAndShow(
                                context,
                                App.self.getString(R.string.mainactivity_onpostexecute_dont_know_to_route),
                                App.self.getString(R.string.mainactivity_onpostexecute_dont_know_to_route),
                                understanding.getQuery(), false);
                        listenAfterTheSpeech();
                        return;
                    } else {
                        if (!force) {
                            if (understanding.getDestination().getLat() == null) {
                                Output.sayAndShow(context, understanding
                                        .getQueryInterpretation().getToShow(),
                                        understanding.getQueryInterpretation()
                                                .getToSay(), understanding
                                        .getQuery(), false);
                                us.savedReply = understanding;
                                cleintState=SN_YES_NO;
                                speakText(R.string.P_is_that_right);
                                condListenAfterTheSpeech();
                                return;
                            }
                        }
                        mainActivity.showMark(dstLocation);
                        mainActivity.navigateTo(dstLocation);
                    }
                    break;
                }
                case Understanding.CMD_MY_PARKING: {
                    DoublePoint dstLocation = DoublePoint.from(App.self.getStringPref(PrefConsts.PF_LAST_PARKING_PLACE));
                    if (dstLocation != null)
                        mainActivity.showMark(dstLocation);
                    break;
                }
                case Understanding.CMD_DAILY_UPDATE:
                    (new DailyUpdate(context)).playAgenda();
                    break;
                case Understanding.CMD_OTHER:
                    if (mainActivity.currentController != null)
                        mainActivity.currentController
                                .sayAndGoOtherIfCan(qi, false);
                    break;
                case Understanding.CMD_PREV:
                    if (mainActivity.currentController != null)
                        mainActivity.currentController.sayAndGoOtherIfCan(qi, true);
                    break;
                case Understanding.CMD_REPEAT:
                    if (mainActivity.currentController != null)
                        mainActivity.sayAgain();
                    listenAfterTheSpeech();
                    break;
                case Understanding.CMD_WEATHER:
                    if (weather != null) {
                        reportWeather(weather, forecast);
                        break;
                    }
                    listenAfterTheSpeech();
                    return;

                case Understanding.CMD_GPS:
                    launchGpsSettings(context);
                    break;

                case Understanding.CMD_WIFI:
                    launchWifiSettings(context);
                    break;

                case Understanding.CMD_BLUTOOTH:
                    launchBluetoothSettings(context);
                    break;

                case Understanding.CMD_BATTERY:
                    launchBatteryUsage(context);
                    break;


                // switch to English language
                case Understanding.CMD_SWITCH_TO_ENGLISH: {
                    if (App.self.isInRussianMode()) {
                        // change and save preferences
                        App.self.setLanguage("en");
                        qi.sayAndShow(context);
                    }
                    condListenAfterTheSpeech();
                    return;
                }

                // floating button on
                case Understanding.CMD_FLOATING_ON: {
                    App.self.setBooleanPref(PrefConsts.PF_FL_BUTTON, true);
                    //MyTTS.speakText(R.string.P_tray_robin_hint_on);
                    analytics.trackButtonPress("turn_on_floating_button_by_voice");
                    break;
                }

                case Understanding.CMD_SILENT: {
                    final AudioManager am1 = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);
                    if (am1 != null) {
                        am1.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        am1.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        am1.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
                    }
                    break;
                }

                case Understanding.CMD_VIBRATE: {
                    final AudioManager am2 = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);
                    if (am2 != null) {
                        am2.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        am2.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        am2.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
                    }
                    break;
                }

                case Understanding.CMD_AIRPLANE: {
                    if (android.os.Build.VERSION.SDK_INT < 17) {
                        Settings.System.putInt(App.self.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
                        Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
                        intent.putExtra("state", 1);
                        App.self.sendBroadcast(intent);
                    }
                    else {
                        launchAirplaneSettings(context);
                    }
                    break;
                }

                case Understanding.CMD_FLASHLIGHT_ON:
                    if (flashLightSurface == null)
                        flashLightSurface = new FlashLight();
                    flashLightSurface.turnOn();

                    break;

                case Understanding.CMD_FLASHLIGHT_OFF:
                    if (flashLightSurface != null) {
                        flashLightSurface.turnOff();
                        flashLightSurface.close();
                        flashLightSurface = null;
                    }

                    break;

                case Understanding.CMD_VOLUME_DOWN: {
                    final AudioManager am3 = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);
                    if (am3 != null)
                        am3.setStreamVolume(VR.TtsAudioStream, am3.getStreamMaxVolume(VR.TtsAudioStream)/2, 0);
                    break;
                }

                case Understanding.CMD_VOLUME_UP: {
                    final AudioManager am4 = (AudioManager) App.self.getSystemService(Context.AUDIO_SERVICE);
                    if (am4 != null) {
                        am4.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        am4.setStreamMute(AudioManager.STREAM_MUSIC, false);
                        am4.setStreamMute(AudioManager.STREAM_VOICE_CALL, false);
                        am4.setStreamVolume(VR.TtsAudioStream, am4.getStreamMaxVolume(VR.TtsAudioStream), 0);
                    }
                    break;
                }

                case Understanding.CMD_WIFI_ON: {
                    WifiManager wifiManager = (WifiManager) App.self.getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager != null)
                        wifiManager.setWifiEnabled(true);
                    break;
                }

                case Understanding.CMD_WIFI_OFF: {
                    WifiManager wifiManager = (WifiManager) App.self.getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager != null)
                        wifiManager.setWifiEnabled(false);
                    break;
                }

                case Understanding.CMD_CLOSE_APP: {
                    String s = understanding.getDescription();
                    String app = Utils.getForegroundPackage();
                    boolean killCurrentOrSelf = false;
                    if (Utils.isEmpty(s))
                        killCurrentOrSelf = true;
                    else {
                        if (!killProcessLike(s)) {
                            killCurrentOrSelf = true;
                            //understanding.setQueryInterpretation(Utils.getString(R.string.P_CAN_NOT_CLOSE,"app", s));
                        }
                    }
                    if (killCurrentOrSelf) {
                        if (Utils.isMyPackage(app) || Utils.isHomePackage(app))
                            MyTTS.execAfterTheSpeech(new Runnable() {
                                @Override
                                public void run() {
                                    if (mainActivity != null)
                                        mainActivity.finish();
                                    SuziePopup sp = SuziePopup.get();
                                    if (sp != null)
                                        sp.byeAnimation();
                                }
                            });
                        else
                            killProcess(app);
                    }

                    qi.sayAndShow(context);
                    break;
                }

                case Understanding.CMD_CLOSE_ALL: {
                    killAllProcesses();
                    break;
                }

                case Understanding.CMD_SONG_PAUSE: {
                    //App.self.sendBroadcast(new Intent("com.android.music.musicservicecommand.pause"));
                    //App.self.sendBroadcast(new Intent("com.sec.android.app.music.musicservicecommand.pause"));
                    //App.self.sendBroadcast(new Intent(Intent.ACTION_MEDIA_BUTTON));

                    Intent downIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP);
                    downIntent.putExtra(Intent.EXTRA_KEY_EVENT, downEvent);
                    App.self.sendBroadcast(downIntent);

                    Intent upIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
                    KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_STOP);
                    upIntent.putExtra(Intent.EXTRA_KEY_EVENT, upEvent);
                    App.self.sendBroadcast(upIntent);
				/*
				Intent i = new Intent("com.android.music.musicservicecommand");
				i.putExtra("command", "pause");
				App.self.sendBroadcast(i);
				*/

                    break;
                }

                case Understanding.CMD_BLUETOOTH_ON: {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                    }
                    break;
                }

                case Understanding.CMD_BLUETOOTH_OFF: {
                    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                    break;
                }

                /*
                case Understanding.CMD_CALL_ALERTS_ON: {
                    SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
                    MyTTS.speakText(R.string.P_tray_phone_hint_on);
                    prefsEditor.putBoolean("speakCallerName", true);
                    prefsEditor.commit();
                    break;
                }

                case Understanding.CMD_CALL_ALERTS_OFF: {
                    SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
                    MyTTS.speakText(R.string.P_tray_phone_hint_off);
                    prefsEditor.putBoolean("speakCallerName", false);
                    prefsEditor.commit();
                    break;
                }

                case Understanding.CMD_MESSAGE_ALERTS_ON: {
                    SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
                    MyTTS.speakText(R.string.P_tray_sms_hint_on);
                    prefsEditor.putBoolean("SMSnotify", true);
                    prefsEditor.commit();
                    break;
                }

                case Understanding.CMD_MESSAGE_ALERTS_OFF: {
                    SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
                    MyTTS.speakText(R.string.P_tray_sms_hint_off);
                    prefsEditor.putBoolean("SMSnotify", false);
                    prefsEditor.commit();
                    break;
                }
                */

                // floating button off
                case Understanding.CMD_FLOATING_OFF: {
                    App.self.setBooleanPref(PrefConsts.PF_FL_BUTTON, false);
                    analytics.trackButtonPress("turn_off_floating_button_by_voice");

                    //MyTTS.speakText(R.string.P_tray_robin_hint_off);
                    if (SuziePopup.isVisible())
                        SuziePopup.get().hideSuzie(SuziePopup.HideAnimation.None);

                    break;
                }

                // switch to Russian language
                case Understanding.CMD_SWITCH_TO_RUSSIAN: {
                    if (!App.self.isInRussianMode()) {
                        // change and save preferences
                        App.self.setLanguage("ru");
                        

                    }
                    condListenAfterTheSpeech();
                    return;
                }

                case Understanding.CMD_INTRO_VIDEO: {
                    if (qi!=null) Output.sayAndShow(
                            context,
                            new MyTTS.Wrapper(qi.getToShow()) {

                                @Override
                                public void onSaid(boolean fAborted) {
                                    super.onSaid(fAborted);
                                    if (!fAborted)
                                        IntroSlidesActivity.start(context, false);
                                }

                            },
                            qi.getToSay(),
                            null,
                            false
                    );
                    break;
                }

                case Understanding.CMD_INFO:
                    speakText(qi.getToSay());
                    HelpActivity.onInfo(context,true);
            }
            fireOpes();
        } else {
            QueryInterpretation qi = understanding.getQueryInterpretation();
            if (qi != null)
                qi.sayAndShow(context);
            condListenAfterTheSpeech();
        }
    }

    private void closeRobin() {

    }

    private void getBirthDayFromFBSilently() {
        if (mainActivity.fbHelper.facebook.isSessionValid()) {
            mainActivity.fbHelper.getUserBirthday(new SuccessFailure<Date>() {
                @Override
                public void onSuccess(final Date d) {
                    Log.d(TAG, "hd=" + d);
                    LearnAttribute la = new LearnAttribute(R.string.PfBirthday);
                    la.learnDate(d);
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onFailure() {
                }

            });
        }
    }

    private void getBirthDayFromFBAndReadHoroscope() {
        final EventSource es = progressIndicatorHolder.showProgress();
        mainActivity.fbHelper.getUserBirthday(new SuccessFailure<Date>() {
            @Override
            public void onSuccess(final Date d) {
                Log.d(TAG, "hd=" + d);

                final String dsc = fetchHoroscope(
                        UserLocationProvider.readLocationPoint(), d);
                Utils.runInMainUiThread(context , new Runnable() {
                    @Override
                    public void run() {
                        LearnAttribute la = new LearnAttribute(
                                R.string.PfBirthday);
                        la.learnDate(d);
                        es.fireEvent();
                        if (!isEmpty(dsc))
                            sayAndShow(dsc);
                        listenAfterTheSpeech();
                    }
                });
            }

            @Override
            public void onCancel() {
                Utils.runInMainUiThread(context ,new Runnable() {
                    @Override
                    public void run() {
                        es.fireEvent();
                        listenAfterTheSpeech();
                    }
                });
            }

            @Override
            public void onFailure() {
                Utils.runInMainUiThread(context ,new Runnable() {

                    @Override
                    public void run() {
                        es.fireEvent();
                        speakText(R.string.P_SOMETHING_WENT_WRONG);
                        listenAfterTheSpeech();
                    }

                });
            }

        });

    }

    private String cleintState=null;

    @Override
    public String getClientStateName() {
        return cleintState;
    }

    void reportWeather(GooWeather weather, GooWeatherForecastCondition forecast) {
        if (forecast != null) {
            boolean c = WeatherView.tempInCelsius();
            speakText(
                    new MyTTS.BubblesInMainActivityOnly(
                            App.self.getString(
                                    R.string.mainactivity_reportweather_forecast)
                                    + " " + weather.getCurrentConditions().getCondition() /*forecast.getCondition() */
                    )
            );

            int tH = c ? f2c(forecast.getHigh()) : forecast.getHigh();

            int tL = c ? f2c(forecast.getLow()) : forecast.getLow();

            speakText(
                    new MyTTS.BubblesInMainActivityOnly(
                            App.self.getString(R.string.mainactivity_reportweather_expected_temperatures)
                                    + " "
                                    + tL
                                    + " "
                                    + App.self.getString(R.string.mainactivity_reportweather_to)
                                    + " "
                                    + tH
                                    + " "
                                    + App.self.getString(R.string.mainactivity_reportweather_degrees)
                    )
            );

            MainActivity ma = MainActivity.get();
            if (ma != null)
                ma.showWeather(weather);
            SuziePopup sp = SuziePopup.get();
            if (sp != null)
                sp.showWeather(weather);
        }
    }
}
