/*
 ******************************************************************************
 * Parts of this code sample are licensed under Apache License, Version 2.0   *
 * Copyright (c) 2009, Android Open Handset Alliance. All rights reserved.    *
 *                                                                            *                                                                         *
 * Except as noted, this code sample is offered under a modified BSD license. *
 * Copyright (C) 2010, Motorola Mobility, Inc. All rights reserved.           *
 *                                                                            *
 * For more details, see MOTODEV_Studio_for_Android_LicenseNotices.pdf        * 
 * in your installation folder.                                               *
 ******************************************************************************
 */

package com.magnifis.parking;

import android.accounts.Account;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Pair;
import android.widget.Toast;

import com.magnifis.parking.fb.FbHelper;
import com.magnifis.parking.feed.MailFeedController;
import com.magnifis.parking.model.LearnAttribute;
import com.magnifis.parking.model.MailService;
import com.magnifis.parking.pref.LearnAttributePreference;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.suzie.SuziePopup;

import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.twitter.TwitterPlayer;
import com.magnifis.parking.utils.Analytics;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.WeatherView;

import static com.magnifis.parking.utils.Utils.isEmpty;

/***
 * PreferenceActivity is a built-in Activity for preferences management
 * 
 * To retrieve the values stored by this packageName in other activities use the
 * following snippet:
 * 
 * SharedPreferences sharedPreferences =
 * PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
 * <Preference Type> preferenceValue = sharedPreferences.get<Preference
 * Type>("<Preference Key>",<default value>);
 */
public class PrefsActivity extends PreferenceActivity 
  implements OnSharedPreferenceChangeListener {
	
	final static String TAG=PrefsActivity.class.getSimpleName();
	
	String pfnResetTwitter, pfnResetActions, pfnResetFacebook;
	
	boolean withAccountManager=App.self.hasPermission("android.permission.MANAGE_ACCOUNTS");
	
	private void setEnabled(PreferenceScreen ps,int ename, boolean enabled) {
		setEnabled(ps, getString(ename), enabled);
	}
	 
	private void setEnabled(PreferenceScreen ps,String ename, boolean enabled) {
		Pair<Preference,PreferenceGroup>  pp=deepFindPreferece(ps,ename);
		if (pp!=null) pp.first.setEnabled(enabled);
	}
	
	private void setEnabled(Pair<Preference,PreferenceGroup>  pp, boolean enabled) {
		if (pp!=null) pp.first.setEnabled(enabled);
	}

	private void insertOrRemove(Pair<Preference,PreferenceGroup> pp0, boolean fInsert) {
		if (pp0==null) return;
		Pair<Preference,PreferenceGroup>  pp=deepFindPreferece(pp0.second,pp0.first.getKey());
		if (fInsert) {
		  if (pp==null) {
			 pp0.second.addPreference(pp0.first); 
		  }
		} else { // remove
		   if (pp!=null) {
			   pp.second.removePreference(pp.first);	
		   }
		}
	}
	
	private void removeEntry(PreferenceScreen ps, int ename) {
		removeEntry(ps,getString(ename));
	}
	
	private void removeEntry(Pair<Preference,PreferenceGroup>  pp) {
		if (pp!=null) pp.second.removePreference(pp.first);		
	}
	
	private void removeEntry(PreferenceScreen ps,String ename) {
		removeEntry(deepFindPreferece(ps,ename));
	}
	
	
	private void addMaleVoiceToOptions(PreferenceGroup pg) {
		Pair<Preference,PreferenceGroup>  pp=deepFindPreferece(pg, R.string.PfVoiceType);
		if (pp!=null) {
			ListPreference vtp = (ListPreference)pp.first;
			vtp.setEntries(R.array.voiceTypePhrase);
			vtp.setEntryValues(R.array.voiceType);
	    }
	}
	
	
	private void removeMaleVoiceFromOptions(PreferenceGroup pg) {
		Pair<Preference,PreferenceGroup>  pp=deepFindPreferece(pg, R.string.PfVoiceType);
		if (pp!=null) {
			ListPreference vtp = (ListPreference)pp.first;
			vtp.setEntries(R.array.voiceTypePhraseNomale);
			vtp.setEntryValues(R.array.voiceTypeNomale);
	    }
	}
	
	void updateMailAccountsVisibility(PreferenceScreen ps,int maType) {
	   boolean gm=maType==App.self.MT_GMAIL, ym=maType==App.self.MT_YAHOO;
	   if (anyGoogleAccount) {
		   insertOrRemove(ppGmailAccount,gm&&gmailAccountSelection); 
		   insertOrRemove(ppGmailPassword,gm);
	   }
	   insertOrRemove(ppYahooDomain,ym);  
	   insertOrRemove(ppMailYahooBox,ym); 
	   insertOrRemove(ppYahooPassword,ym); 
	}
	
	private boolean anyGoogleAccount=false;
	private SharedPreferences prefs=null;
	
	Pair<Preference,PreferenceGroup> 
	   ppMailType, ppGmailAccount, ppYahooDomain, ppMailYahooBox,
	   ppGmailPassword, ppYahooPassword,
	   ppActivationIcon, ppFlButton, ppVoiceType,
	   ppDownloadMaleVoice, ppDownloadAlyonaVoice,
	   ppDownloadFemaleVoice
	   ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		WeatherView.tempInCelsius(); // upgrade the preference from prev. version
		
		addPreferencesFromResource(R.xml.preferences);
		
		pfnResetTwitter=getString(R.string.PfResetTwitter);
		pfnResetFacebook=getString(R.string.PfResetFacebook);
		pfnResetActions=getString(R.string.PfResetActions);
		
		PreferenceScreen ps=getPreferenceScreen();
		
		prefs=ps.getSharedPreferences();
	    
	    ppMailType=deepFindPreferece(ps, R.string.PfMailType);
	    ppGmailAccount=deepFindPreferece(ps, R.string.PfGmailAccount);
	    ppYahooDomain=deepFindPreferece(ps, R.string.PfYahooDomain);
	    ppMailYahooBox=deepFindPreferece(ps, R.string.PfYahooMailBox);
	    ppGmailPassword=deepFindPreferece(ps, "gmailPassword");
	    ppYahooPassword=deepFindPreferece(ps, "yahooPassword");
	    //ppSmsSenderReadInstantly=deepFindPreferece(ps, "smsSenderReadInstantly");
	    //ppActivationIcon=deepFindPreferece(ps, PrefConsts.PF_ACTIVATION_ICON);
	    ppFlButton=deepFindPreferece(ps, PrefConsts.PF_FL_BUTTON);
	    //ppAskToReply=deepFindPreferece(ps, PrefConsts.PF_SMS_ASK_TO_REPLY);
	    ppVoiceType=deepFindPreferece(ps, R.string.PfVoiceType);
	    //ppSmsIncomingVoice=deepFindPreferece(ps, PrefConsts.PF_SMS_INCOMING_VOICE);
	    //ppReadSmsInstantly=deepFindPreferece(ps, PrefConsts.PF_SMS_READ_INSTANTLY);
	    ppDownloadMaleVoice=deepFindPreferece(ps, "download_male_voice");
	    ppDownloadAlyonaVoice=deepFindPreferece(ps, "download_russian_voice");
	    ppDownloadFemaleVoice=deepFindPreferece(ps, "download_female_voice");

		anyGoogleAccount=!isEmpty(App.self.getGoogleAccounts());
		
		if (!Config.floatingButton) {
			removeEntry(ps, PrefConsts.PF_FL_BUTTON);
			ppFlButton=null;
		}
		
	    if (!anyGoogleAccount) {
	    	removeEntry(ps,"gmailPassword");
	    	removeEntry(ps,R.string.PfGmailAccount);
	    }
	    
	    if (App.self.useOnlyGmail) removeEntry(ps,R.string.PfMailType);
	    
	    if (Utils.isAndroid41orAbove)
	    	removeEntry(ps,"InputReadySoundOff");

	    changeLanguageAndVoices(false);
	      
	    
	    for (String hp:Config.hiddenPrefs) removeEntry(ps, hp);

	    initMainAccounts();
		initSummaries(ps);
		updateMailAccountsVisibility(ps, App.self.getMailAccountType());

		for (String hp:Config.hiddenPrefs) removeEntry(ps, hp);
	}
	
	/*
	private void changePpActivationIconVisibility() {
	  setEnabled(ppActivationIcon,  !(App.self.shouldUseProximitySensor()||App.self.shouldUseSuzie()));
	}
	*/
	
	private boolean gmailAccountSelection=false;
	
	void initMainAccounts() {
		String key=getString(R.string.PfGmailAccount);
	    Account acs[]=App.self.getGoogleAccounts();
	    PreferenceScreen ps=getPreferenceScreen();
	    boolean ok=false;
	    if (!isEmpty(acs)) {
			if (acs.length > 1) {
				Pair<Preference, PreferenceGroup> ga = deepFindPreferece(ps,
						key);
				ListPreference lsp = (ListPreference) ga.first;
				String ens[] = new String[acs.length];
				for (int i = 0; i < acs.length; i++)
					ens[i] = acs[i].name;
				lsp.setEntries(ens);
				lsp.setEntryValues(ens);
				lsp.setDefaultValue(acs[0].name);
				lsp.setValue(App.self.getGmailAccountName());
				ok = true;
			}
	    }
	    gmailAccountSelection=ok;
	    if (!ok)	removeEntry(ps,key);		
	}
	
	Pair<Preference,PreferenceGroup> deepFindPreferece(PreferenceGroup pg, int key) {
		return deepFindPreferece(pg,getString(key));
	}
	
	Pair<Preference,PreferenceGroup> deepFindPreferece(PreferenceGroup pg, CharSequence key) {
		for (int i = 0; i < pg.getPreferenceCount(); ++i) {
			Preference p = pg.getPreference(i);
			if (key.equals(p.getKey())) 
				return new Pair<Preference,PreferenceGroup>(p,pg);
			if (p instanceof PreferenceGroup) {
				Pair<Preference,PreferenceGroup> pp=deepFindPreferece((PreferenceGroup) p,key); 
				if (pp!=null) return pp;
			}/* else 
			if (key.equals(p.getKey())) 
				return new Pair<Preference,PreferenceGroup>(p,pg);
				*/
		}
		return null;
	}
	
	private void initSummaries(PreferenceGroup pg) {
		for (int i = 0; i < pg.getPreferenceCount(); ++i) {
			Preference p = pg.getPreference(i);
			if (p instanceof PreferenceGroup)
				this.initSummaries((PreferenceGroup) p); // recursion
			else
				this.setSummary(p);
		}
	}
	
	private void setSummary(String key) {
		setSummary(deepFindPreferece(getPreferenceScreen(),key).first);
	}

	
	private void setSummary(final Preference pref) {
		// react on type or key
		if (pref instanceof PreferenceGroup) {
			PreferenceGroup pg=(PreferenceGroup)pref;
			for (int i=0;i<pg.getPreferenceCount();i++) 
				setSummary(pg.getPreference(i));
		} else
		if (pref instanceof ListPreference) {
			ListPreference listPref = (ListPreference) pref;
			pref.setSummary(listPref.getEntry());
		} else
		if (pref instanceof LearnAttributePreference) {
		   pref.setSummary(new LearnAttribute(pref.getKey()).getReadableDef());
	    } else
	    if (pref instanceof EditTextPreference) {
	    	EditTextPreference edPref = (EditTextPreference) pref;
	    	if ((edPref.getEditText().getInputType()&InputType.TYPE_TEXT_VARIATION_PASSWORD)==0)
	    	   pref.setSummary(edPref.getText());
	    	else
	    	   if ("gmailPassword".equals(pref.getKey())) {
	    		   String gma=App.self.getGmailAccountName();
	    		   if (!isEmpty(gma)) {
	    			   pref.setSummary("for "+gma);
	    		   }
	    	   } else
	    	   if ("yahooPassword".equals(pref.getKey())) {
	    		  MailService ms=MailService.fromPreferences();
	    		  if (ms!=null&&ms.isYahoo()) {
	    			 String ma=ms.getMailAddress();
	    			 if (ma!=null) pref.setSummary("for "+ma);
	    		  }
		       }
	    } else
		if ("connectGoogle".equals(pref.getKey())) {
			 final Account accs[]=App.self.getGoogleAccounts();
			 //boolean ok=false;
			 if (!isEmpty(accs))
			   new Thread() {
				 @Override
				 public void run() {
				   googleToken=App.self.peekGoogleToken(accs[0]);			
				   
				   PrefsActivity.this.runOnUiThread(
					 new Runnable() {

						@Override
						public void run() {
							if (googleToken!=null) {
								pref.setSummary(accs[0].name);
								//pref.setTitle("Disconnect Google Account");
							} else {
								pref.setSummary(null);	   
								pref.setTitle("Connect Google Account");
							}
						}
						 
					 }	  
				   );
				   
				 }
			   }.start();
			 else
			  pref.setSummary(null);
		}
	}
	
	String googleToken=null;

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			final Preference preference) {

  		if (preference.getKey().equals(pfnResetActions)) {
			Launchers.resetDefaults();
			finish();
		} else
		if (preference.getKey().equals("privacyPolicy")) {
			Launchers.launchBrowser(this, getString(R.string.privacy_policy_url));	
		} else if (preference.getKey().equals(pfnResetTwitter)) {
	    	TwitterPlayer.resetConnection();
	    	String s = getResources().getString(R.string.P_TW_CREDENTTIALS_RESETED);
	    	Output.sayAndShow(this, s);
	    	finish();
		} else
	    if (preference.getKey().equals(pfnResetFacebook)) {
	    	FbHelper.getInstance().resetCredentials();
	    	String s = getResources().getString(R.string.P_FB_CREDENTTIALS_RESETED);
	    	Output.sayAndShow(this, s);
	    	finish();
	    } else
			 if (preference.getKey().equals("connectGoogle")) {
				 final Account accs[]=App.self.getGoogleAccounts();
				 if (!isEmpty(accs)) {
				   if (/*googleToken==null*/true) {
				     Log.d(TAG, "email="+accs[0].name);  
				     new Thread() {
				    	@Override
				    	public void run() {
				    		Log.d(TAG, "token="+App.self.updateGoogleToken(accs[0], PrefsActivity.this, false));
				    		setSummary(preference);
				    	}
				     }.start();
				   } else {
					 App.self.invalidateGoogleToken(googleToken);
					 setSummary(preference);
				   }
				 }
			 }
		
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	private void setCurrentVoice(int voice) {
		App.self.setCurrentVoice(voice);
		((ListPreference)ppVoiceType.first).setValue(Integer.toString(voice));
	}
	
	private void changeLanguageAndVoices(boolean forceVoice) {
		if (MainActivity.dontUseAcapella) {
			removeEntry(getPreferenceScreen(), "download_female_voice");
			removeEntry(getPreferenceScreen(), "download_male_voice");
			removeEntry(getPreferenceScreen(), "download_russian_voice");
			removeEntry(ppVoiceType);
			return;
		}
		

		if (App.self.isInRussianMode()) {
           {
				setEnabled(ppVoiceType, false);	
				setCurrentVoice(MyTTS.SYSTEM_VOICE);
				((ListPreference)ppVoiceType.first).setValue(getString(R.string.VSystem));
			};
			insertOrRemove(ppDownloadMaleVoice, false);
			removeMaleVoiceFromOptions(getPreferenceScreen());
			boolean offerDownload=false;
			insertOrRemove(ppDownloadAlyonaVoice, offerDownload);
		} else {		
			
			boolean enableVoiceTypeSelection=true;
			
           {
				setCurrentVoice(MyTTS.SYSTEM_VOICE);
				((ListPreference)ppVoiceType.first).setValue(getString(R.string.VSystem));
				enableVoiceTypeSelection=false;
			}
			setEnabled(ppVoiceType, enableVoiceTypeSelection);	
			
			
			insertOrRemove(ppDownloadAlyonaVoice, false);
			
		}
		boolean offerDownload=false;

		insertOrRemove(ppDownloadFemaleVoice, offerDownload);
		insertOrRemove(ppDownloadMaleVoice, offerDownload);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
        syncPref(key);


        Preference pref = findPreference(key);
        if (pref != null) setSummary(pref);

        String value = "non_string_pref";
        if (key.equals(getString(R.string.PfVoiceType))) {
            MyTTS.setVoice(App.self.getCurrentVoice());
        } else if (key.equals("voiceactivationoncharging")) {
            SphinxRecornizer.stop();
            SphinxRecornizer.start();
        } else if (key.equals("voiceactivation")) {
            Toast.makeText(App.self,App.self.getString(R.string.toast_restart_voice_activation),Toast.LENGTH_SHORT).show();
            SphinxRecornizer.stop();
            SphinxRecornizer.start();
            value = sharedPreferences.getString(key, null);
        } else if (key.equals("lang")) {
            App.self.reloadResources();
            changeLanguageAndVoices(true);
            setSummary(getPreferenceScreen());
        } else if (key.equals(getString(R.string.PfGmailAccount))) {
            setSummary("gmailPassword");
        } else if (key.equals(getString(R.string.PfYahooDomain)) || key.equals(getString(R.string.PfYahooMailBox))) {
            setSummary("yahooPassword");
        } else if (key.equals("car_mode_option_big_button")) {
            SuziePopup sp = SuziePopup.get();
            if (sp != null)
                sp.checkButtonSize();
        }

        Robin.showStickyNotification(0);

        (new Analytics(App.self)).trackEvent("preference_change", key, value);

    }
	
	private void syncPref(String key) {
		PreferenceScreen ps=getPreferenceScreen();
		syncPref(deepFindPreferece(ps, key));
	}
	
	private void syncPref(Pair<Preference, PreferenceGroup> pp) {
	  if (pp!=null&&pp.first!=null) {
		  if (pp.first instanceof CheckBoxPreference) {
			  CheckBoxPreference cbp=(CheckBoxPreference)pp.first;
			  boolean v=App.self.getBooleanPref(cbp.getKey());
			  if (v!=cbp.isChecked()) cbp.setChecked(v);
		  }
	  }
	}
	
	private void mailboxTypeChanged() {
		updateMailAccountsVisibility(getPreferenceScreen(), App.self.getMailAccountType());
		MainActivity ma=MainActivity.get();
		if (ma!=null) {
		  MailFeedController mfc=ma.mailController;
		  if (mfc!=null) mfc.reset();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		App.self.setActiveActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		App.self.removeActiveActivity(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		App.self.notifyStopActivity(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}	

}