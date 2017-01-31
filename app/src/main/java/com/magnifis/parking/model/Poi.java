package com.magnifis.parking.model;

import java.io.Serializable;
import java.util.Date;

import static com.magnifis.parking.utils.Utils.*;

import com.magnifis.parking.Log;
import com.magnifis.parking.Xml.ML;

public class Poi extends PoiLike implements Serializable {
	
	final static String TAG=Poi.class.getSimpleName();
	
	public static class Hours implements Serializable  {
	   @ML("day")
	   protected Day days[]=null;

	   public Day[] getDays() {
		   return days;
	   }

	   public void setDays(Day[] days) {
		   this.days = days;
	   }

	}
	
	public static class Day implements Serializable  {
	   @ML("open")
	   protected Open open[]=null;
	   @ML(attr="seq")
	   protected Integer seq=null;
	   
	   public Open[] getOpen() {
		   return open;
	   }
	   public void setOpen(Open[] open) {
		   this.open = open;
	   }
	   public Integer getSeq() {
		   return seq;
	   }
	   public void setSeq(Integer seq) {
		   this.seq = seq;
	   }
	   
	}
	
	public static class Open implements Serializable   {
	   @ML(attr="from", format="HHmm")	
	   protected Date from=null;
	   @ML(attr="to", format="HHmm")
	   protected Date to=null;

		public Date getFrom() {
			return from;
		}

		public void setFrom(Date from) {
			this.from = from;
		}

		public Date getTo() {
			return to;
		}

		public void setTo(Date to) {
			this.to = to;
		}

	}
	
	@ML("hours")
	protected Hours hours=null;

	
    public Hours getHours() {
		return hours;
	}

	public void setHours(Hours hours) {
		this.hours = hours;
	}


	@ML("source")
    protected String source=null;
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	public boolean isFromYelp() {
	  return "yelp".equalsIgnoreCase(source);
	}
	
	public boolean isFromGoogle() {
	  return "google".equalsIgnoreCase(source);
	}
	
	
	@ML("review_count")
	protected Integer reviewCount=null;
	
	
	public Integer getReviewCount() {
		return reviewCount;
	}

	public void setReviewCount(Integer reviewCount) {
		this.reviewCount = reviewCount;
	}

	
	public Double getRating() {
		return rating;
	}
	public void setRating(Double rating) {
		this.rating = rating;
	}
	@ML("rating")
	protected Double rating=null;

	/**
	 * @return the phone
	 */
	@Override
	public String getPhone() {
		return phone;
	}
	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}
	/**
	 * @return the category
	 */
	@Override
	public String getCategory() {
		return category;
	}
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	/**
	 * @return the url
	 */
	@Override
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	@ML("phone")
	protected String phone=null;
	@ML("category")
	protected String category=null;
	@ML("url")
	protected String url=null;
	
	@ML("recommendation")
	protected String recommendation=null;
	
	@ML("attribution")
	protected String attribution=null;
	
	final static int MAX_WORDS=14;
	
	public String getRecommendationForDetailsView() {
		return getRecommendationForDetailsView(MAX_WORDS);
	}
	
	public String getRecommendationForDetailsView(int wcLimit) {
	   if (!isEmpty(recommendation)) {
		   String rec = recommendation.trim().replace("\\n+", " ");
		   if (!isEmpty(rec)) {
			  StringBuilder sb=new StringBuilder();
			  int wc=0; boolean inword=false;
			  for (int i=0; i<rec.length(); i++) {
				 char c=rec.charAt(i);
				 if (Character.isLetter(c)) {
					if (!inword) {
						inword=true; ++wc;
					} 
				 } else {
				   inword=false;
				 }
				 sb.append(c);
				 if (!inword&&(wc>=wcLimit)) break;
			  }
			  if (sb.charAt(sb.length()-1)!='.') sb.append(" ...");
			  return sb.toString();
		   }
		   return rec;
	   }
	   return recommendation;
	}
	
	public String getRecommendation() {
	   return recommendation;
	}

	public String getAttribution() {
		return attribution; 
	}

	public String getRecommendationForSpeach() {
		
		  if (!isEmpty(recommendation)) {
			   String rec = recommendation.trim().replace("\\n+", " ").replace("[.]+", ".");
			   if (!isEmpty(rec)) {
				  StringBuilder sb=new StringBuilder();
				  String a[]=rec.split("[.]");
				  int wc=0, wcnext=-1, l1=a.length-1;
				  for (int i=0; i<a.length; i++) {
					 sb.append(a[i]); sb.append('.');
					 wc+=wcnext<0?countWords(a[i]):wcnext;
					 if (wc>=MAX_WORDS) break;
					 if (i<l1) {
						wcnext=countWords(a[i+1]);
						if (wc+wcnext>MAX_WORDS&&(wc>MAX_WORDS/2)) break;
					 } else
						wcnext=-1;
				  }
				 // Log.d(TAG,"!!! "+a.length+" statements");
				  return sb.toString();
			   }
			   return rec;
		   }
		   return recommendation;		
	}

	public void setRecommendation(String recommendation) {
		this.recommendation = recommendation;
	}
	
}
