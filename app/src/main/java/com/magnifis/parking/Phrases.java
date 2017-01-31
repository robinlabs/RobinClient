package com.magnifis.parking;

import static com.magnifis.parking.tts.MyTTS.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import com.magnifis.parking.model.GasStation;
import com.magnifis.parking.model.LearnAttribute;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.utils.Utils;


public class Phrases {
	
	public static String partOfDay() {
	  return partOfDay(new Date());
	}
		
    public static String partOfDay(Date d) {
		  Calendar cal=Calendar.getInstance();
		  cal.setTime(d);
		  int hours=cal.get(Calendar.HOUR_OF_DAY);
		  

			
		  if (hours>=3&&hours<12) return App.self.getString(R.string.phrases_part_of_day_morning); else
		  if (hours>=12&&hours<17) return App.self.getString(R.string.phrases_part_of_day_afternoon); else
		  if (hours>=17&&hours<=23) return App.self.getString(R.string.phrases_part_of_day_evening); else
	      return App.self.getString(R.string.phrases_part_of_day_you_up_late);
	}

	
	public static String getRandomPhrase(int id) {
    	String phrases[] =App.self.getResources().getStringArray(id);
    	
    	int iChosen = new Random((int)(System.currentTimeMillis()%1000000)).nextInt(phrases.length); 
		return phrases[iChosen];		
	}

    public static String pickWelcomePhrase() {
    	return  LearnAttribute.expandMacros(getRandomPhrase(R.array.welcomePhrases));
	}
    
    public static String pickSmsArrivedPhrase() {
    	return getRandomPhrase(R.array.sent_prepare_to_read);
	}
	
    public static String pickTrafficIntelligencePhrase() {
    	return getRandomPhrase(R.array.trafficIntelPhrases);
	}
    
    public static String pickCurrentTrafficCondPhrase() {
    	return getRandomPhrase(R.array.trafficConditionPhrases);
	}
    
    public static String pickConfirmationPrefix() {
    	return getRandomPhrase(R.array.confirmationPrefixes);
	}
    
    
    public static String pickDonePhrase() {
    	return getRandomPhrase(R.array.donePhrases);
	}

	
	
    public static String pickOccupancyPhrase() {
    	return getRandomPhrase(R.array.occupancyPhrases);
	}
    
    public static String pickNoParkingPhrase() {
    	return getRandomPhrase(R.array.P_no_parking);
	}
    
    public static String formNoCustomLocationPhrase(String customLocationName) {
    	return R.string.P_unknown_custom_location + customLocationName + " " + App.self.getString(R.string.phrases_no_custom_location_end); 
	}

    public static String pickNoOptionPhrase() {
    	return getRandomPhrase(R.array.P_no_option);
	}
    
    public static String pickFoundOptionsPhrase() {
    	return getRandomPhrase(R.array.foundOptionPhrases);
	}
    
    
    public static String pickFoundOptionsPhrase(String optName) {
    	return getRandomPhrase(R.array.foundOptionPhrases).replace(App.self.getString(R.string.PARKING), ""/*optName*/);
	}
    
    

	public static String pickNewsSnippetPrefix() {	
		return getRandomPhrase(R.array.newsSnippetPrefixes); 
	}
    

    public static String pickCannotCallPhrase() {
    	return getRandomPhrase(R.array.cannotCallPhrases);
	}
    

    public static String pickRemarkPhrase() {
    	return getRandomPhrase(R.array.remarkPhrases);
	}
    
    
    public static String pickLaunchNavPhrase() {
    	return getRandomPhrase(R.array.launchNavPhrases);
	}
    
    public static void appendDistance(double miles, StringBuilder sb) {
    	boolean inMeters = App.self.getBooleanPref("meters");
    	if (inMeters) {
    		double kilometers = Utils.milesToKilometers(miles);
			if (kilometers<0.1)
				sb.append(App.self.getString(R.string.phrases_append_distance_less_five_hundred_meters));
    		else if (kilometers<0.25)
    			sb.append(App.self.getString(R.string.phrases_append_distance_less_quarter_kilometer));
    		else if (kilometers<0.5)
    			sb.append(App.self.getString(R.string.phrases_append_distance_less_half_kilometer));
    		else if (kilometers<0.75)
    			sb.append(App.self.getString(R.string.phrases_append_distance_less_three_quarters_kilometer));
    		else {
    			kilometers=Math.round(kilometers*10.)/10.;
    			double t = Math.floor(kilometers);
    			if ((kilometers-t) == 0) {
        			long kilometersInt = Math.round(kilometers);
        			sb.append(' ').append(kilometersInt).append((kilometersInt>1)?App.self.getString(R.string.phrases_append_distance_kilometers):App.self.getString(R.string.phrases_append_distance_kilometer));
				} else {
	    			sb.append(' ').append(kilometers).append((kilometers>1)?App.self.getString(R.string.phrases_append_distance_kilometers):App.self.getString(R.string.phrases_append_distance_kilometer));			
				}
    		}
		} else {
			if (miles<0.1)
				sb.append(App.self.getString(R.string.phrases_append_distance_less_five_hundred_feet) + " ");
    		else if (miles<0.25)
    			sb.append(App.self.getString(R.string.phrases_append_distance_less_quarter_mile) + " ");
    		else if (miles<0.5)
    			sb.append(App.self.getString(R.string.phrases_append_distance_less_half_mile) + " ");
    		else if (miles<0.75)
    			sb.append(App.self.getString(R.string.phrases_append_distance_less_three_quarters_mile) + " ");
    		else
    			sb.append(' ').append(miles).append(' ').append((miles>1)?App.self.getString(R.string.phrases_append_distance_miles):App.self.getString(R.string.phrases_append_distance_mile) + " ");
		}
    	sb.append(' ');
  		sb.append(App.self.getString(R.string.phrases_append_distance_away));
    }
    
    public static void sayOrder(String order) {
		if (Understanding.orderByDistance(order))
			speakText(App.self.getString(R.string.phrases_say_order_by_distance));
		else if (Understanding.orderByPrice(order))
			speakText(App.self.getString(R.string.phrases_say_order_by_price));
		else if (Understanding.orderByVacancy(order))
			speakText(App.self.getString(R.string.phrases_say_order_by_vacancy));
		else
			speakText(App.self.getString(R.string.phrases_say_order_by));
    }
    
    public static void sayVacancy(PkFacility pkf) {
    	if (pkf.getLoad()==PkFacility.LOAD_UNKNOWN) {
    	  speakText(R.string.P_no_details);
    	  return;
    	} else {
    	  StringBuilder sb=new StringBuilder();	
    	  formVacancyText(sb,pkf,true);
    	  speakText(sb.toString());
    	  return;
    	}
    }
    
    public static void formVacancyText(StringBuilder sb,PkFacility pkf, boolean sayUnknownToo) {
    	int load=pkf.getLoad();
    	if ((load!=PkFacility.LOAD_UNKNOWN)||sayUnknownToo) {
    		String occupancyPhrase = pickOccupancyPhrase(); 
    	  sb.append(occupancyPhrase);
    	  switch(load) {
    	    case PkFacility.LOAD_UNKNOWN:  sb.append(App.self.getString(R.string.phrases_pks_form_vacancy_unknown) + " "); break;
    	    case PkFacility.LOAD_LOW: sb.append(App.self.getString(R.string.phrases_pks_form_vacancy_low) + " "); break;
    	    case PkFacility.LOAD_MEDIUM: sb.append(App.self.getString(R.string.phrases_pks_form_vacancy_medium) + " "); break;
    	    case PkFacility.LOAD_HIGH: sb.append(App.self.getString(R.string.phrases_pks_form_vacancy_high) + " ");
    	  }
    	  sb.append(App.self.getString(R.string.phrases_pks_form_vacancy_there));
    	} 	
    }
    
    public static void formCostText(StringBuilder sb, GasStation pkf, boolean useand) {
        //sb.append(", one gallon of the "+GasStation.getActiveGasTypeName()+" fuel costs ");
   	    sb.append(App.self.getString(R.string.phrases_gas_form_cost_about) + " ");
    	sb.append(pkf.getFormattedPrice() + " ");
   	    sb.append(App.self.getString(R.string.phrases_gas_form_cost_per_gallon) + " ");
        if (pkf.getPrice().isMin()) sb.append(App.self.getString(R.string.phrases_gas_form_cost_cheapest));
    }

    
    public static void formCostText(StringBuilder sb,PkFacility pkf, boolean useand) {
    	Double hourRate=pkf.getPerHourRate();
    	sb.append(' '); 
    	if (hourRate==null) {
    		String rates[]=pkf.getRates();
    		if (rates!=null&&rates.length>0) {
    			if (useand) sb.append(App.self.getString(R.string.phrases_pks_form_cost_and) + " ");
    	   	    sb.append(App.self.getString(R.string.phrases_pks_form_cost_costs) + " ");
    			for (String r:rates) { sb.append(r); sb.append(' '); }
    		}
    	} else {
    		if (useand) sb.append(App.self.getString(R.string.phrases_pks_form_cost_and) + " ");
    		if (hourRate==0) {
    		   sb.append(App.self.getString(R.string.phrases_pks_form_cost_free_of_charge) + " ");
    		} else if (hourRate==1) {
     		   sb.append(App.self.getString(R.string.phrases_pks_form_cost_per_hour) + " ");
    		} else {
				int intRate = hourRate.intValue();
				double res = Math.abs(hourRate - intRate); 
				sb.append(App.self.getString(R.string.phrases_pks_form_cost_costs2) + " ");
				if (res < 0.05)
					sb.append(intRate);
				else 
					sb.append(hourRate); // remove xtra zeros
				sb.append(" " + App.self.getString(R.string.phrases_pks_form_cost_dollars_hour));
			}
    	}  	
    }
    
    public static void sayDescription(GasStation pkf) {
    	StringBuilder sb=new StringBuilder();
 
    	String name=pkf.getName();
    	
    	if (name!=null) {
    	  name = name.toLowerCase().trim();
    	  if (name.endsWith(" st")) {
    		name = name.substring(0, name.length()-3); 
    	  }
    	  sb.append(name);
    	  sb.append(", ");
    	}
    	boolean useand=false;
    	if (pkf.getDistance()!=null) {
    		double miles=pkf.getDistanceInMiles();
    		appendDistance(Math.round(miles*10.)/10.,sb);
    		useand=true;
    	}
        if (pkf.hasPrice()) formCostText(sb,pkf,useand);

    	speakText(sb.toString());
    }
    
    public static void sayDescription(PkFacility pkf) {
    	StringBuilder sb=new StringBuilder();
 
    	String name=pkf.getName();
    	
    	if (pkf.getType().toLowerCase().equals("on-street")) {
			sb.append(App.self.getString(R.string.phrases_say_description_on_street_spot) + " ");
    	}
    	if (name!=null) {
    	  name = name.toLowerCase().trim();
    	  if (name.endsWith(" st")) {
    		name = name.substring(0, name.length()-3); 
    	  }
    	  sb.append(name);
    	  sb.append(", ");
    	}
    	boolean useand=false;
    	if (pkf.getDistance()!=null) {
    		double miles=pkf.getDistanceInMiles();
    		appendDistance(Math.round(miles*10.)/10.,sb);
    		useand=true;
    	}
    	formCostText(sb,pkf, useand);
    	formVacancyText(sb,pkf, false);

    	speakText(sb.toString());
    }
    
    public static void sayDescription(Poi pkf, boolean sayExtra) {
    	StringBuilder sb=new StringBuilder();
 
    	String name=pkf.getName();
    	
    	if (name!=null) {
    	  name = name.toLowerCase().trim();
    	  if (name.endsWith(" st")) {
    		name = name.substring(0, name.length()-3); 
    	  }
    	  sb.append(name);
    	  sb.append(", ");
    	}
    	 
    	if (sayExtra) {
    		int rating10Scale = (int)(pkf.getRating()*2 + .5); 
    		String starRating = String.valueOf(rating10Scale/2); 
    		if (rating10Scale%2 > 0)
    			starRating += " " + App.self.getString(R.string.phrases_say_description_and_half) + " ";
    		int reviewCnt = pkf.getReviewCount(); 
    		if (rating10Scale > 0) { 
    			sb.append(" " + App.self.getString(R.string.phrases_say_description_rated) + " ").append(starRating).append(" " + App.self.getString(R.string.phrases_say_description_stars) + " "); 
    			if (reviewCnt > 0) {
        			sb.append(" " + App.self.getString(R.string.phrases_say_description_based_on) + " ").append(reviewCnt).append(" " + App.self.getString(R.string.phrases_say_description_reviews)); 
        		}	
    		} 
    	} else {
//    	boolean useand=false;
//	    	if (pkf.getDistance()!=null) {
//	    		double miles=pkf.getDistanceInMiles();
//	    		appendDistance(Math.round(miles*10.)/10.,sb);
//	    		useand=true;
//	    	}
    	}

    	speakText(sb.toString());
    }  
    
    public static String formCurrentTime(String qi) {
    	if (Utils.isEmpty(qi)) return qi;
     	return  qi.replaceAll("\\d{2}[:]{1}\\d{2}(\\s?(am|pm|AM|PM))?.*",
    			new java.text.SimpleDateFormat("h:mm a").format(new Date()));
    	
    }

	
}
