package com.magnifis.parking;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.GeoSpannable;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.views.ParkingDetails;
import com.magnifis.parking.views.PoiDetails;

import static com.magnifis.parking.utils.Utils.*;


public class PoisController extends MapItemSetContoller<Poi> {
	
	public boolean anyGoogleInfo() {
		for (int i=0;i<size();i++) if (get(i).isFromGoogle()) {
			return true;
		}
		return false;
	}
	
	public boolean anyYelpInfo() {
		for (int i=0;i<size();i++) if (get(i).isFromYelp()) return true;
		return false;
	}
	

	@Override
	public int size() {
	  int sz=super.size();
	  if (sz>0&&!isCurrent()) return 1;
	  return sz;
	}
	
	private Drawable icPoi,popupPoi;
	
	public PoisController() {
		Resources res=App.self.getResources();
		icPoi=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.map_marker_red));
		popupPoi=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.short_details_popup));
	}

	@Override
	public Poi getSelected() {
		MAStatus cs=UnderstandingStatus.get().status;
		return cs==null?null:cs.getSelectedPoi();
	}

	@Override
	public void setSelected(Poi item) {
	   MAStatus cs=UnderstandingStatus.get().status;
       if (cs!=null) cs.setSelectedPoi(item);
	}

	@Override
	public MapItemIterator<Poi> getIterator() {
		MAStatus cs=UnderstandingStatus.get().status;
		return cs==null?null:cs.getPoiIterator();
	}

	@Override
	public void setIterator(MapItemIterator<Poi> iterator) {
		   MAStatus cs=UnderstandingStatus.get().status;
	       if (cs!=null) cs.setPoiIterator(iterator);
	}

	@Override
	public Drawable getDefaultMarker() {
		// TODO Auto-generated method stub
		return icPoi;
	}

	@Override
	public Drawable getDefaultPopup() {
		// TODO Auto-generated method stub
		return popupPoi;
	}
	
	@Override
	public void sayDetails(boolean sayExtra) {
		Phrases.sayDescription(getSelected(), sayExtra);
	}
	
	@Override
	public void showDetails() {
	  MainActivity ma=MainActivity.get();
	  Poi sel=getSelected();
	  boolean goo=sel.isFromGoogle(), yelp=sel.isFromYelp();
	  ma.showDetails(
		 new PoiDetails(
		    ma, 
		    goo ? R.layout.poi_details_google:  R.layout.poi_details,
		    goo ? R.layout.poi_details_google_landscape:R.layout.poi_details_landscape
		 ),
		 sel
	  );
	  String rec=sel.getRecommendationForSpeach();
	  if (!isEmpty(rec)) {
		  MyTTS.abort();

		  int nReviews = sel.getReviewCount(); 
		  if (goo || App.self.robin().isDebugMode() || App.self.robin().isTestingMode()) {
			  if (nReviews > 0)
				  MyTTS.speakText(R.string.P_BEFORE_POI_REC);
			 MyTTS.speakText(rec);
		  } else if (yelp) {
			sayDetails(true); 
		  }
		  if ((goo||yelp) && nReviews > 0) 
			  
		     MyTTS.speakText(//". Read more at "
		    	App.self.getString(R.string.poi_more_details)
		        +(yelp?App.self.getString(R.string.poi_yelp_reviews):App.self.getString(R.string.poi_google_more)));
	  }
 	}

	@Override
	public GeoSpannable<Poi> getSpannable() {
		return UnderstandingStatus.get().status.getPois();
	}
	

}
