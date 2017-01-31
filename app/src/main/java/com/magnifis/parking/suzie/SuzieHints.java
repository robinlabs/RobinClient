package com.magnifis.parking.suzie;

import java.util.HashMap;

import android.text.format.Time;

import com.magnifis.parking.App;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.utils.Utils;

public class SuzieHints {
	
	HintGroup[] hints = new HintGroup[] {
			
		new HintGroup(new String[] {"launcher", "home"}, 
		new String[] {"open calculator", "open calendar", "take a picture", "what can you do"}),

		new HintGroup(new String[] {"android.settings"}, 
		new String[] {"wi fi settings", "bluethooth settings"}),

		new HintGroup(new String[] {"calendar"}, 
		new String[] {"set meeting tomorrow at 8 pm", "remind me tomorrow bring cofe to office"}),
		
		new HintGroup(new String[] {"calculator"}, 
		new String[] {"calculate 2+2"}),
		
		new HintGroup(new String[] {"translate"}, 
		new String[] {"translate in spanish", "translate from russian"}),
		
		new HintGroup(new String[] {"youtube"}, 
		new String[] {"youtube jackson"}),
		
		new HintGroup(new String[] {"clock", "time"}, 
		new String[] {"what time is in france", "open clock"}),
		
		new HintGroup(new String[] {"gallery"}, 
		new String[] {"open pictures", "take a picture"}),
		
		new HintGroup(new String[] {"maps", "waze"}, 
		new String[] {"show my location", "go to san francisco"}),
		
		new HintGroup(new String[] {"music", "player"}, 
		new String[] {"play rolling stones", "play classical music"}),
		
		new HintGroup(new String[] {"facebook"}, 
		new String[] {"share my location on facebook"}),
		
		new HintGroup(new String[] {".gm"}, 
		new String[] {"check my emails", "send email"}),
		
		new HintGroup(new String[] {"contacts", "mms", "sms"}, 
		new String[] {"call wife", "text wife", "message to Michael"}),
		
		new HintGroup(new String[] {"browser"}, 
		new String[] {"search google Madonna", "tell me sport news"}),
		
		new HintGroup(new String[] {"news"}, 
		new String[] {"tell me sport news"}),
		
		new HintGroup(new String[] {"weather"}, 
		new String[] {"what's weather tomorrow", "weather in san francisco"}),
		
	};
		
	private class HintGroup {

		String[] domains;
		String[] hints;
		
		HintGroup(String[] domains, String[] hints) {
			this.domains = domains;
			this.hints = hints;
		}
	}

	// interval for show hints
	final static long HINT_INTERVAL = 60; // minutes
	static long lastHintTime = System.currentTimeMillis();
	
	// temporary disable hints for HINT_INTERVAL
	public static void disableHints() {
		lastHintTime = System.currentTimeMillis();
	}
	
	// query hint for package 
	// returns null, if package not found
	// returns empty string, if all hints for package already used
	public String findHint(String domain) {
		if (Utils.isEmpty(domain))
			return null;

		for (int h = 0; h < hints.length; h++) {
			for (int d = 0; d < hints[h].domains.length; d++) {
				if (domain.contains(hints[h].domains[d])) {
					
					// check interval 
					long ctime = System.currentTimeMillis();
					if (ctime - lastHintTime < HINT_INTERVAL*60*1000)
						return "";
					
					// find hint that was not shown
					for (int k = 0; k < hints[h].hints.length; k++) {
						if (isHintAllowed(hints[h].hints[k]))
								return hints[h].hints[k];
					}
					return "";
				}
			}
		}
		return null;
	}

	//protected RobinDB rdb = null;
	
	HashMap<Integer, Boolean> usedHints = new HashMap<Integer, Boolean>();
	
	void SuzieHints() {
		//rdb = RobinDB.getInstance(App.self);
		//rdb.db.execSQL("CREATE TABLE IF NOT EXISTS ");
	}
	
	private boolean isHintAllowed(String hint) {
		if (usedHints.get(hint.hashCode()) == null) {
			usedHints.put(hint.hashCode(), true);
			disableHints();
			return true;
		}
		else
			return false;
	}
		

}
