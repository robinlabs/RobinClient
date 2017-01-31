package com.magnifis.parking.twitter;

import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.UserMentionEntity;

import android.util.Log;

import com.magnifis.parking.Phrases;
import com.magnifis.parking.utils.DateClassifier;
import com.magnifis.parking.utils.SmartDateFormatter;

public class TwitterStatusFormatter {
	private SmartDateFormatter sdf=new SmartDateFormatter();
	
	private Pattern ptrnLink=Pattern.compile("http(s)?\\:\\/\\/\\S+");
	private Pattern ptrnTagTail = Pattern.compile("[^(at|in)] #[A-Za-z0-9_]+$"); // at end of string, unless preceded by (at|in)
	private Pattern ptrnRetweet = Pattern.compile("(RT|cc|CC)[ ]*@[A-Za-z0-9_]+"); // retweet / CC
	
	private static final int IMPROVISE_PHRASE_FREQ_ONE_IN_N = 5; 
	//static String[] strSays = {" says: ", " posts: ", " notes: ", " remarks: ", " says: "}; 
/*	
	public String format(Status st, boolean forSpeach) {
		String txt=st.getText();
		
		if (forSpeach) txt=ptrnLink.matcher(st.getText()).replaceAll("a link");
		
		Date d=st.getCreatedAt();
		
		if (forSpeach) {
			UserMentionEntity umes[]=st.getUserMentionEntities();
			if (umes!=null) {
				for (UserMentionEntity ume:umes) {
					String s=ume.getName()+" "+ume.getScreenName();
					//Log.d(TAG,"ume: "+s);
					txt=txt.replaceAll("@"+ume.getScreenName()+"(\\W|$)", ume.getName()+"$1");
				}
				//Log.d(TAG,txt);
			}
		}
		
		txt=sdf.format(d)+"  "+st.getUser().getName()+" posted "+txt;

		return 	txt;

	}
*/
	public String format(Status st, boolean forSpeach) {
		String txt=st.getText().trim();

		if (forSpeach) {
			
			// remove links
			String newTxt = ptrnLink.matcher(txt).replaceAll("").trim();
			
			// remove hash-tags at the tail completely (typically not part of the content)
			// as well as retweet tags 
			do {
				txt = newTxt;
				newTxt = ptrnTagTail.matcher(txt).replaceAll("").trim();
				newTxt = ptrnRetweet.matcher(newTxt).replaceAll("").trim();
			} while (!newTxt.equals(txt));
			
			
			txt = txt.replaceAll("#", ""); // remove just the remaining "#" signs 
			txt = txt.replaceAll("\\*{2,}", ""); // ***
			
			UserMentionEntity umes[]=st.getUserMentionEntities();
			if (umes!=null) {
				for (UserMentionEntity ume:umes) {
					String s=ume.getName()+" "+ume.getScreenName();
					//Log.d(TAG,"ume: "+s);
					txt=txt.replaceAll("@"+ume.getScreenName()+"(\\W|$)", ume.getName()+"$1");
				}
				//Log.d(TAG,txt);
			}
			
//			if (linkedAttached) {
//				txt += ". Link attached."; 
//			}
		}
		
		// once in N, spice it up with a short improvisation
		
		//int choice = new Random(System.currentTimeMillis()%100000).nextInt(IMPROVISE_PHRASE_FREQ_ONE_IN_N); 

		
		txt =  st.getUser().getName()+ Phrases.pickRemarkPhrase() + txt + "."; 
		return 	txt;

	}	
	
}
