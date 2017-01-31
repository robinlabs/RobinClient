package com.magnifis.parking.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.Xml.ML_alternatives;
import com.magnifis.parking.cmd.CallCmdHandler;
import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.cmd.Teaching;
import com.magnifis.parking.utils.Utils;

import static com.magnifis.parking.utils.Utils.*;

public class Understanding implements Serializable, Cloneable {
	final static String TAG=Understanding.class.getSimpleName();
	
	protected boolean lockTaken=false;

	
	public boolean isLockTaken() {
		return lockTaken;
	}

	public Understanding setLockTaken(boolean lockTaken) {
		this.lockTaken = lockTaken;
		return this;
	}

	@Override
	public Understanding clone() {
		return Utils.cloneSerializable(this);
	}
	
	public Understanding() {}
	public Understanding(int cmdCode) { commandCode=cmdCode; }
	
	@ML("ordinal")
	protected Integer ordinal=null;

	
	public Integer getOrdinal() {
		return ordinal;
	}
	public void setOrdinal(Integer ordinal) {
		this.ordinal = ordinal;
	}
	
	@ML(tag="cancel", ifpresents=true)
	protected boolean cancel=false;

	public boolean isCancel() {
		return cancel;
	}

	public void setCancel(boolean cancel) {
		this.cancel = cancel;
	}

	@ML(tag="number", ifpresents=true)
	protected boolean number=false;
	
	public boolean isPhoneNumberGiven() {
		return number;
	}
	public Understanding setNumber(Boolean number) {
		this.number = number;
		return this;
	}

	@ML("alias")
	protected CmdAlias aliases[]=null;

	
	public CmdAlias[] getAliases() {
		return aliases;
	}
	public void setAliases(CmdAlias[] aliases) {
		this.aliases = aliases;
	}

	@ML(tag="activate_microphone", ifpresents=true)
	protected boolean activateMicrophone=false;
	
	
	public boolean isActivateMicrophone() {
		return activateMicrophone;
	}

	public Understanding setActivateMicrophone(Boolean activateMicrophone) {
		this.activateMicrophone = activateMicrophone;
		return this;
	}
	
	@ML("phone_type")
	protected String phoneType=null;


	public String getPhoneType() {
		return phoneType;
	}
	public void setPhoneType(String phoneType) {
		this.phoneType = phoneType;
	}

	@ML("dialog_phrase")
	protected String dialogPhrase=null;
	
	@ML("channel")
	protected String channel=null;
	
	public String getChannel() {
		return channel;
	}

	public Understanding setChannel(String channel) {
		this.channel = channel;
		return this;
	}

    @ML("filter")
    protected String filter=null;

    public String getFilter() {
        return filter;
    }

    public Understanding setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    public String getDialogPhrase() {
		return dialogPhrase;
	}

	public void setDialogPhrase(String dialogPhrase) {
		this.dialogPhrase = dialogPhrase;
	}

	@ML("gps_location")
    protected DoublePoint gpsLocation=null;
	
	public DoublePoint getGpsLocation() {
		return gpsLocation;
	}

	public void setGpsLocation(DoublePoint gpsLocation) {
		this.gpsLocation = gpsLocation;
	}

	@ML("route_type")
	protected String routeType=null;
	
	public String getRouteType() {
		return routeType;
	}

	public void setRouteType(String routeType) {
		this.routeType = routeType;
	}
	
	@ML("script")
	protected Script script=null;
	public Script getScript() {
		return script; 
	}


	@ML("learn_attribute")
	protected LearnAttribute learnAttribute=null; 
	public LearnAttribute getLearnAttribute() {
		return learnAttribute;
	}

	public void setLearnAttribute(LearnAttribute learnAttribute) {
		this.learnAttribute = learnAttribute;
	}
	
    @ML("url")
    protected String url=null;
    
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@ML("action")
	protected String action=null;
	@ML("description")
	protected String description="";
	
	@ML("language")
	protected String language=null;
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	@ML_alternatives({
      @ML(tag="description", attr="contact_name"),
	  @ML("contact_name"),
    })
	
	protected String contactNames[]=null;	
	
	@ML("phn")
	protected String phoneNumbers[]=null;
	
	public String[] getPhoneNumbers() {
		return phoneNumbers;
	}

	public Understanding setPhoneNumbers(String ... phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
		return this;
	}

	public String[] getContactNames() {
		return contactNames;
	}
	public void setContactNames(String ... contactNames) {
		this.contactNames = contactNames;
	}
	
	public void fixContactNames() {
		fixContactNames(description);	
	}
	
	public void fixContactNamesDictation() {
		fixContactNames(message);	
	}
	
	private void fixContactNames(String _description) {
		if (isEmpty(contactNames)&&!(number||isEmpty(_description))) {
		   contactNames=Utils.simpleSplit(_description,',');
		   //description=null;
		}
	}
	
	public String getAction() {
		return action;
	}

	public Understanding setAction(String action) {
		this.action = action;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	// for dictation: new text position in format: "pos1,pos2"
	@ML("pos")
	protected String pos=null;	
	
	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	@ML("show_embedded")
	protected Boolean showEmbedded=false;
	

	public boolean isShowEmbedded() {
		return showEmbedded!=null&&showEmbedded;
	}

	public void setShowEmbedded(Boolean showEmbedded) {
		this.showEmbedded = showEmbedded;
	}

	/*
	@ML_alternatives({
	   @ML(tag="timedate",attr="date",format="yyyy/MM/dd"),
	   @ML(tag="timedate",attr="timestamp",format="yyyy/MM/dd HH:mm"),
	})
	*/
	@ML(tag="timedate",attr="date",format="yyyy/MM/dd")
	protected Date timedate=null;
	
	@ML(tag="timedate",attr="timestamp",format="yyyy/MM/dd HH:mm")
	protected Date timestamp=null;
	
	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {

        this.timestamp = timestamp;
	}

	public Date getTimedate() {
		if (timedate!=null) return timedate;
		return timestamp;
	}

	public void setTimedate(Date timedate) {
		this.timedate = timedate;
		this.timestamp = timedate;
	}
	
	@ML("timedate")
	protected String customDate=null;

	public String getCustomDate() {
		return customDate;
	}

	public void setCustomDate(String customDate) {
		this.customDate = customDate;
	}
	
	@ML("contr_message")
	protected String contrMessage=null;
	
	public String getContrMessage() {
		return contrMessage;
	}

	public void setContrMessage(String contrMessage) {
		this.contrMessage = contrMessage;
	}
	
	@ML("push_ad")
	protected PushAd pushAd=null;
	
	public PushAd getPushAd() {
		return pushAd;
	}
	public void setPushAd(PushAd ad) {
		pushAd = ad;
	}

    public String getSnippet(){return snippet;}
    public void setSnippet(String value){ snippet = value;}
    public String getSnippetType(){return snippetType;}
    public void setSnippetType(String value){ snippetType = value;}

	@ML("confirm_required")
	protected Boolean confirmationRequired=null;
	@ML(tag="confirm_required", attr="onYes")
	protected String confirmationRequiredOnYes=null;
	@ML(tag="confirm_required", attr="onNo")
	protected String confirmationRequiredOnNo=null;


    @ML("snippet")
    protected String snippet=null;

    @ML(tag="snippet", attr="type")
    protected String snippetType=null;

    public String getConfirmationRequiredOnYes() {
		return confirmationRequiredOnYes;
	}

	public void setConfirmationRequiredOnYes(String confirmationRequiredOnYes) {
		this.confirmationRequiredOnYes = confirmationRequiredOnYes;
	}

	public String getConfirmationRequiredOnNo() {
		return confirmationRequiredOnNo;
	}

	public void setConfirmationRequiredOnNo(String confirmationRequiredOnNo) {
		this.confirmationRequiredOnNo = confirmationRequiredOnNo;
	}

	public Boolean getConfirmationRequired() {
		return confirmationRequired;
	}

	public boolean isConfirmationRequired() {
		return (confirmationRequired!=null)&&confirmationRequired;
	}

	public Understanding setConfirmationRequired(Boolean confirmationRequired) {
		this.confirmationRequired = confirmationRequired;
		return this;
	}

	@ML("type")
	protected String type = null;
	
	public boolean isError() {
	  return "error".equalsIgnoreCase(type)||(type==null&&(command==null&&commandCode==null));
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	@ML("query")
	protected String query = null;
	@ML("domain")
	protected String domain = null;
	@ML("command")
	protected String command = null;
	@ML(tag="query_interpretation", indirect=true /* workaround to a server bug*/)
	protected QueryInterpretation queryInterpretation = null;
	
	@ML("orderby")
	protected String orderBy=null;
	@ML("park_type")
	protected String parkType=null;
	@ML("rad_meters")
	protected Integer radMeters=null;
	
	@ML("param")
	protected ActionParam[] actionParams = null; 
	
	
	public Map<String, String> getActionParamNVPairs() {
		Map<String, String> paramPairs = null; 
		if (!Utils.isEmpty(actionParams)) {
			paramPairs = new HashMap<String, String>(actionParams.length); 
			for (ActionParam param : actionParams) {
				paramPairs.put(param.name, param.value); 
			}
		}
		
		return paramPairs; 
	}
	

	/**
	 * @return the orderBy //  price / distance / vacancy
	 */
	public String getOrderBy() {
		return orderBy;
	}
	
	public boolean orderByPrice() {
		return orderByPrice(orderBy);
	}
	
	public boolean orderByDistance() {
		return orderByDistance(orderBy);
	}
	
	public boolean orderByVacancy() {
		return orderByVacancy(orderBy);
	}
	
	public static final String ORDER_PRICE="price";
	public static final String ORDER_DISTANCE="distance";
	public static final String ORDER_VACANCY="vacancy";
	
	static public boolean orderByPrice(String orderBy) {
		return ORDER_PRICE.equals(orderBy);
	}
	
	static public boolean orderByDistance(String orderBy) {
		return (orderBy==null)||ORDER_DISTANCE.equals(orderBy);
	}
	
	static public boolean orderByVacancy(String orderBy) {
		return ORDER_VACANCY.equals(orderBy);
	}
	

	/**
	 * @param orderBy the orderBy to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * @return the parkType
	 */
	public String getParkType() {
		return parkType;
	}

	/**
	 * @param parkType the parkType to set
	 */
	public void setParkType(String parkType) {
		this.parkType = parkType;
	}

	/**
	 * @return the radMeters
	 */
	public Integer getRadMeters() {
		return radMeters;
	}

	/**
	 * @param radMeters the radMeters to set
	 */
	public void setRadMeters(Integer radMeters) {
		this.radMeters = radMeters;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query
	 *            the query to set
	 */
	public Understanding setQuery(String query) {
		this.query = query;
		return this;
	}

	/**
	 * @return the domain
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * @param domain
	 *            the domain to set
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public final static int 
	  CMD_NAVIGATE=1, 
	  CMD_ROUTE=2, 
	  CMD_DO_IT=3,
	  CMD_SEARCH=4, 
	  CMD_MAP=5, 
	  CMD_REPEAT=6,
	  CMD_YES=7, 
	  CMD_NO=8, 
	  CMD_OTHER=9, 
	  CMD_NEAREST=10, 
	  CMD_CHEAPEST=11, 
	  CMD_ONSTREET=12, 
	  CMD_OFFSTREET=13,
	  CMD_PARKING=14, 
	  CMD_CHEAP=15, 
	  CMD_NEAR=16, 
	  CMD_PREV=17, 
	  CMD_TRAFFIC=18, 
	  CMD_CALL=19,
	  CMD_ZOOMIN=20, 
	  CMD_ZOOMOUT = 21, 
	  CMD_INFO=22,
	  CMD_COST=23,  
	  CMD_VACANCY=24, 
	  CMD_GO_BACK=25,
	  CMD_WEATHER=26, 
	  CMD_DETAILS=27,
	  CMD_GAS=28,
	  CMD_TIME=29, 
	  CMD_CONTACTS=30, 
	  CMD_TEXT=31,
	  CMD_MAIL=32, 
	  CMD_FACEBOOK=33,
	  CMD_TWITTER=34, 
	  CMD_CAMERA=35,
	  CMD_FEEDBACK=36,
	  CMD_LEARN=37,
	  CMD_CALENDAR=38,
	  CMD_YOUTUBE=39,
	  CMD_PLAY_TWITTER=40,
	  CMD_RETWEET=41,
	  CMD_MORE=42,
	  CMD_REMINDER=43,
	  CMD_PLAY=44,
	  CMD_OPEN_URL=45,
	  CMD_ALARM=46,
	  CMD_SHARELOCATION=47,
	  CMD_AUTOADVERT=48,
	  CMD_OPENAPP=49,
	  CMD_SETTINGS=50,
	  CMD_UPDATE=51,
	  CMD_NEWS=52,
	  CMD_HOROSCOPE=53,
	  CMD_TURNOFF=54,
	  CMD_READ=55,
	  CMD_GPS=56,
	  CMD_WIFI=57,
	  CMD_BLUTOOTH=58,
	  CMD_VOICE_FEMALE=59,
	  CMD_VOICE_MALE=60,
	  CMD_VOICE_SWITCH=61,
	  CMD_SEND=62,
	  CMD_DICTATE=63,
	  CMD_FACEBOOK_STATUS=64,
	  CMD_SATELLITE_VIEW=65,
	  CMD_MAP_VIEW=66,
	  CMD_MY_BIRTHDAY=67,
	  CMD_LEARN_BIRTHDAY=68,
	  CMD_BATTERY=69,
	  CMD_FACEBOOK_NEWS=70,
	  CMD_VOICE_MAIL=71,
	  CMD_CLEAR_MAP=72,
	  CMD_TEACH=73,
	  CMD_FORGET=74,
	  CMD_INITIALIZATION=75,
	  CMD_ROBIN_SETTINGS=76, 
	  CMD_REPLY=77,
	  CMD_BBALL_NEWS=78,
	  CMD_TRANSLATE=79,
	  CMD_SWITCH_TO_ENGLISH=80,
	  CMD_SWITCH_TO_RUSSIAN=81,
	  CMD_SAY=82,
	  CMD_JOKE=83,
	  CMD_JOKE_SEXY=84,
	  CMD_QUOTE=85,
	  CMD_NOTE=86,
	  CMD_APP_NAME=87,
	  CMD_SHOW_ROBIN=88,
	  CMD_DRIVING=89,
	  CMD_NOT_DRIVING=90,
	  CMD_FLOATING_ON=91,
	  CMD_FLOATING_OFF=92,
	  CMD_FUCK=93,
	  CMD_SILENT=94,
	  CMD_VIBRATE=95,
	  CMD_AIRPLANE=96,
	  CMD_VOLUME_DOWN=97,
	  CMD_VOLUME_UP=98,
	  CMD_WIFI_ON=99,
	  CMD_WIFI_OFF=100,
	  CMD_BLUETOOTH_ON=101,
	  CMD_BLUETOOTH_OFF=102,
	  CMD_CALL_ALERTS_ON=103,
	  CMD_HELLO=104,
	  CMD_HOW_ARE_YOU=105,
	  CMD_INTRO_VIDEO=106,
	  CMD_CLOSE_APP=107,
	  CMD_CLOSE_ALL=108,
	  CMD_SONG_PAUSE=109,
	  CMD_DICTATE_NEW=110,
	  CMD_FLASHLIGHT_ON=111,
	  CMD_FLASHLIGHT_OFF=112,
      CMD_MY_PARKING=113,
      CMD_DAILY_UPDATE=114,
      CMD_MESSAGE_ALERTS_ON=115,
      CMD_MESSAGE_ALERTS_OFF=116,
      CMD_CALL_ALERTS_OFF=117,
      CMD_ROKU=118,
      CMD_MOVIES=119,
      CMD_DELEGATE_AGENT=120,
      CMD_AUDIOBURST=121,

	  CMD_NOP=0,
	  CMD_UNKNOWN=999;
	;
	
	public static boolean isLikeUnknown(int cmd) {
	  switch(cmd) {
	  case CMD_JOKE: case CMD_JOKE_SEXY: case CMD_FUCK: case CMD_HELLO: case CMD_HOW_ARE_YOU:
		  return true;
	  }
	  return false;
	}
	
	private final HashMap<Integer,Class> _cmdHandlerTable=new HashMap<Integer,Class>() {
		{
			put(CMD_CALL, CallCmdHandler.class);
            if (!Config.new_teaching)
			    put(CMD_TEACH, Teaching.class);
			put(CMD_SEND, SendCmdHandler.class);
			put(CMD_REPLY, SendCmdHandler.class);
			
		/*	put(CMD_DELEGATE_AGENT, SendCmdHandler.class);
		 * 
		    ZB: should not be handled by that way
		 */
		}
	};
	
	private final HashMap<String,Integer> _cmdTable=new HashMap<String,Integer>() {
		{
			put("dlg_browser",CMD_NAVIGATE);
			put("dlg_intro_video",CMD_INTRO_VIDEO);
			put("route",CMD_ROUTE);
			put("map",CMD_MAP);
			put("dlg_doit",CMD_DO_IT);
			put("search",CMD_SEARCH);
			put("dlg_repeat",CMD_REPEAT);
			put("dlg_other",CMD_OTHER);
			put("parking",CMD_PARKING);
			put("traffic",CMD_TRAFFIC);
			put("call",CMD_CALL);
			put("dlg_time",CMD_TIME);
			
			put("dlg_cheap",CMD_CHEAP);//:   cheaper
			put("dlg_near",CMD_NEAR);//:     closer
			put("dlg_prev",CMD_PREV);//:     last one
			
			put("dlg_zoomin",CMD_ZOOMIN);
			put("dlg_zoomout",CMD_ZOOMOUT);
			put("dlg_yes", CMD_YES);
			put("dlg_no", CMD_NO);
			put("dlg_info", CMD_INFO);
			put("dlg_cost", CMD_COST);
			put("dlg_vacancy", CMD_VACANCY);
			put("dlg_previous", CMD_GO_BACK);
			put("weather", CMD_WEATHER);
			put("dlg_details",CMD_DETAILS);
			put("gas stations", CMD_GAS);
			put("dlg_contacts", CMD_CONTACTS);
			put("dlg_text",CMD_TEXT);
			
			put("dlg_mail",CMD_MAIL);
			put("dlg_facebook",CMD_FACEBOOK);
			put("dlg_twit",CMD_TWITTER);
			put("dlg_camera", CMD_CAMERA);
			put("dlg_feedback", CMD_FEEDBACK);
			put("dlg_calendar", CMD_CALENDAR);
			put("learn", CMD_LEARN);
			put("dlg_youtube", CMD_YOUTUBE);
			put("dlg_play_twitter", CMD_PLAY_TWITTER);
			
			put("dlg_retweet", CMD_RETWEET);
			put("dlg_more", CMD_MORE);
			put("reminder", CMD_REMINDER);
			put("play", CMD_PLAY);
			put("open_url", CMD_OPEN_URL);
			put("alarm", CMD_ALARM);
			put("dlg_sharelocation",CMD_SHARELOCATION);
			put("dlg_autoadvert",CMD_AUTOADVERT);
			put("openapp",CMD_OPENAPP);
			put("dlg_settings",CMD_SETTINGS);
			put("dlg_update", CMD_UPDATE);
			put("dlg_news", CMD_NEWS);
			put("dlg_horoscope", CMD_HOROSCOPE);

			put("dlg_turnoff", CMD_TURNOFF);
			put("dlg_read", CMD_READ);
	
			put("dlg_gps", CMD_GPS);
			put("dlg_wifi", CMD_WIFI);
			put("dlg_bluetooth", CMD_BLUTOOTH);
			
			put("dlg_voice_female", CMD_VOICE_FEMALE);
			put("dlg_voice_male", CMD_VOICE_MALE);
			put("dlg_voice_switch", CMD_VOICE_SWITCH);

			put("send",CMD_SEND);
			put("dlg_dictation",CMD_DICTATE);
			
			put("dlg_facebook_status",CMD_FACEBOOK_STATUS);
			
			put("dlg_satellite_view", CMD_SATELLITE_VIEW);
			put("dlg_map_view", CMD_MAP_VIEW);
			
			put("dlg_my_birthday", CMD_MY_BIRTHDAY);
			put("learn_birthday", CMD_LEARN_BIRTHDAY);
			
			put("dlg_battery", CMD_BATTERY);
			
			put("dlg_facebook_news", CMD_FACEBOOK_NEWS);
			
			put("dlg_voicemail", CMD_VOICE_MAIL);
			put("dlg_clear_map", CMD_CLEAR_MAP);
			put("teach", CMD_TEACH);
			put("forget", CMD_FORGET);
			put("dlg_init", CMD_INITIALIZATION);
			put("dlg_menu", CMD_ROBIN_SETTINGS);
			put("dlg_reply", CMD_REPLY);
	        put("dlg_basket_news", CMD_BBALL_NEWS); 
	        put("translate", CMD_TRANSLATE);
	        put("dlg_switch_to_english", CMD_SWITCH_TO_ENGLISH);
	        put("dlg_switch_to_russian", CMD_SWITCH_TO_RUSSIAN);
	        put("say", CMD_SAY);
	        put("dlg_joke", CMD_JOKE);
	        put("dlg_joke_sexy", CMD_JOKE_SEXY);
	        put("dlg_quote", CMD_QUOTE);
	        put("dlg_note", CMD_NOTE);
	        put("dlg_application_name", CMD_APP_NAME);
	        put("dlg_show_robin", CMD_SHOW_ROBIN);
	        put("dlg_driving", CMD_DRIVING);
	        put("dlg_not_driving", CMD_NOT_DRIVING);
	        put("dlg_floating_on", CMD_FLOATING_ON);
	        put("dlg_floating_off", CMD_FLOATING_OFF);
	        put("dlg_fuck", CMD_FUCK);
	        put("dlg_silent", CMD_SILENT);
	        put("dlg_vibrate", CMD_VIBRATE);
	        put("dlg_airplane", CMD_AIRPLANE);
	        put("dlg_volume_down", CMD_VOLUME_DOWN);
	        put("dlg_volume_up", CMD_VOLUME_UP);
	        put("dlg_wifi_on", CMD_WIFI_ON);
	        put("dlg_wifi_off", CMD_WIFI_OFF);
	        put("dlg_bluetooth_on", CMD_BLUETOOTH_ON);
	        put("dlg_bluetooth_off", CMD_BLUETOOTH_OFF);
	        put("dlg_call_alerts_on", CMD_CALL_ALERTS_ON);
	        put("dlg_hello", CMD_HELLO);
	        put("dlg_howareyou", CMD_HOW_ARE_YOU);
	        put("closeapp", CMD_CLOSE_APP);
	        put("dlg_close_all", CMD_CLOSE_ALL);
	        put("dlg_song_pause", CMD_SONG_PAUSE);
	        put("dlg_dictation_new", CMD_DICTATE_NEW);	        
	        put("dlg_flashlight_on", CMD_FLASHLIGHT_ON);	        
	        put("dlg_flashlight_off", CMD_FLASHLIGHT_OFF);	        
            put("dlg_my_parking", CMD_MY_PARKING);
            put("dlg_daily_update", CMD_DAILY_UPDATE);
            put("dlg_message_alerts_on", CMD_MESSAGE_ALERTS_ON);
            put("dlg_message_alerts_off", CMD_MESSAGE_ALERTS_OFF);
            put("dlg_call_alerts_off", CMD_CALL_ALERTS_OFF);
            put("dlg_roku", CMD_ROKU);
            put("dlg_movies", CMD_MOVIES);
            put("delegate_agent", CMD_DELEGATE_AGENT);
            put("audioburst", CMD_AUDIOBURST); 
		}
	};
	
	public boolean isCommandUnknown() {
		return getCommandCode()==CMD_UNKNOWN;
	}
	
	public Class getCommandHandlerFactory() {
	   return _cmdHandlerTable.get(getCommandCode());
	}
	
	/**
	 * @return the commandCode
	 */
	public int getCommandCode() {
		if (commandCode==null) {
			if (command==null) commandCode=CMD_NOP; else commandCode=_cmdTable.get(command);
			if (commandCode==null) {
			   if ("parking".equals(domain)) 
				 commandCode=CMD_SEARCH;
			   else
			     commandCode=CMD_UNKNOWN;
			}
		}
		return commandCode;
	}
	
	/**
	 * @param commandCode the commandCode to set
	 */
	public Understanding setCommandByCode(Integer commandCode) {
		this.commandCode = commandCode;
		if (commandCode!=null) {
			for (Map.Entry<String,Integer> en:_cmdTable.entrySet()) 
				if (commandCode.equals(en.getValue())) {
					setCommand(en.getKey()); break;
			    }
		}
		return this;
	}

	/**
	 * @param commandCode the commandCode to set
	 */
	public Understanding setCommandCode(Integer commandCode) {
		this.commandCode = commandCode;
		return this;
	}

	protected Integer commandCode=null;

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the query_interpretation
	 */
	public QueryInterpretation getQueryInterpretation() {
		synchronized(this) {
			  if (queryInterpretation==null)
				  queryInterpretation=new QueryInterpretation();
			}
		return queryInterpretation;
	}
	
	public void setQueryInterpretation(QueryInterpretation qi) {
		queryInterpretation=qi;
	}
	
	public Understanding setQueryInterpretation(int qi) {
		setQueryInterpretation(App.self.getString(qi));
		return this;
	}
	
	public void setQueryInterpretation(String qi) {
		synchronized(this) {
		  if (queryInterpretation==null) queryInterpretation=new QueryInterpretation();
		}
		queryInterpretation.set(qi);
	}

	@ML("origin")
	protected Origin origin=null;
	
	@ML("destination")
	protected Origin destination=null;
	
	/**
	 * @return the destination
	 */
	public Origin getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(Origin destination) {
		this.destination = destination;
	}

	/**
	 * @return the origin
	 */
	public Origin getOrigin() {
		return origin;
	}

	/**
	 * @param origin the origin to set
	 */
	public void setOrigin(Origin origin) {
		this.origin = origin;
	}
	
	protected DoublePoint originLocation=null;
	
	public void setOriginLocation(DoublePoint originLocation) {
		this.originLocation=originLocation;
	}
	
	public DoublePoint getOriginLocation() {
	   if (originLocation!=null) return originLocation;
	   return origin==null?null:origin.getLocation();
	}
	
	public DoublePoint getDestinationLocation() {
	   if (destination!=null) return destination.getLocation();
	   return null;
	}
	
	public String getOriginAddress() {
		return origin==null?null:origin.getFullAddress();
	}
	
	public boolean calculateOriginLocation(Location myLoc) {
		return calculateOriginLocation(myLoc==null?null:new DoublePoint(myLoc));
	}
	
	
	public boolean calculateOriginLocation(DoublePoint myLoc) {
		if (!isError()&&origin!=null) return origin.calculateLocation(myLoc);
		return false;
	}
	
	public boolean calculateDestinationLocation(DoublePoint myLoc) {
		if (!isError()&&destination!=null) return destination.calculateLocation(myLoc);
		return false;
	}
	
	public boolean calculateDestinationLocation(Location myLoc) {
		if (!isError()&&destination!=null) return destination.calculateLocation(myLoc==null?null:new DoublePoint(myLoc));
		return false;
	}
	
	public boolean anyPoiInfo() {
	  return origin==null?false:origin.anyPoiInfo();
	}
	
	public void expandMacros() {
		if (queryInterpretation!=null) {
			queryInterpretation.setToSay(LearnAttribute.expandMacros(queryInterpretation.getToSay()));
			queryInterpretation.setToShow(LearnAttribute.expandMacros(queryInterpretation.getToShow()));
		}
		
		if (!isEmpty(getConfirmationRequiredOnNo()))
			setConfirmationRequiredOnNo(
					LearnAttribute.expandMacros(getConfirmationRequiredOnNo())
			);
		
		if (!isEmpty(getConfirmationRequiredOnYes()))
			setConfirmationRequiredOnYes(
					LearnAttribute.expandMacros(getConfirmationRequiredOnYes())
			);
		
		if (!isEmpty(description)) description=LearnAttribute.expandMacros(description);
	}
	
	public void learn() {
	  if (learnAttribute!=null) learnAttribute.learn(this);

	}
	
	//Message text for CMD_SEND => CMD_DICTATE
	@ML("message")
	protected String message=null;

	public String getMessage() {
		return message;
	}

	public Understanding setMessage(String message) {
		this.message = message;
		return this;
	}
	
	protected boolean dontSaveAgentPhones=false;	

	public boolean isDontSaveAgentPhones() {
		return dontSaveAgentPhones;
	}

	public Understanding setDontSaveAgentPhones(boolean dontSaveAgentPhones) {
		this.dontSaveAgentPhones = dontSaveAgentPhones;
		return this;
	}

	// **************** TRANSLATE ************************
	@ML("from_language")
	protected String fromLanguage=null;

	public String getFromLanguage() {
		return fromLanguage;
	}

	public void setFromLanguage(String fromLanguage) {
		this.fromLanguage = fromLanguage;
	}

	@ML("from_language_code")
	protected String fromLanguageCode=null;

	public String getFromLanguageCode() {
		return fromLanguageCode;
	}

	public void setFromLanguageCode(String fromLanguageCode) {
		this.fromLanguageCode = fromLanguageCode;
	}

	@ML("language_code")
	protected String languageCode=null;

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	
}
