package com.magnifis.parking;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GeoSpannable;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.views.ParkingDetails;

public class PksController extends MapItemSetContoller<PkFacility> {
	
	private Drawable icLow,icHigh,icMedium, icUnknown,
             popupLow, popupHigh, popupMedium, popupUnknown;
	
	
	
	public PksController() {
		
		Resources res=App.self.getResources();
		
		icLow=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.green_map_icon));
		icHigh=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.red_map_icon));
		icMedium=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.orange_map_icon));
		icUnknown=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.orange_map_icon));
		
		popupLow=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.short_details_popup_green));
		popupHigh=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.short_details_popup_red));
		popupMedium=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.short_details_popup_orange));
		popupUnknown=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.short_details_popup_orange));		
	}

	@Override
	public Drawable getMarkerFor(PkFacility item) {
		switch(item.getLoad()) {
		case PkFacility.LOAD_MEDIUM: return icMedium; 
		case PkFacility.LOAD_LOW: return icLow; 
		case PkFacility.LOAD_HIGH: return icHigh;  
		}

		return getDefaultMarker();
	}

	@Override
	public Drawable getDefaultMarker() {
		return icMedium;
	}

	@Override
	public Drawable getPopupFor(PkFacility item) {
		switch(item.getLoad()) {
		case PkFacility.LOAD_MEDIUM: return popupMedium; 
		case PkFacility.LOAD_LOW: return popupLow; 
		case PkFacility.LOAD_HIGH: return popupHigh;  
		}

		return getDefaultPopup();

	}

	@Override
	public Drawable getDefaultPopup() {
		// TODO Auto-generated method stub
		return popupMedium;
	}

	@Override
	public void showDetails() {
	  MainActivity ma=MainActivity.get();
	  ma.showDetails(
		 new ParkingDetails(ma),
		 getSelected()
	  );
 	}

	@Override
	public void sayDetails(boolean sayExtra) {
		Phrases.sayDescription(getSelected());
	}

	@Override
	public PkFacility getSelected() {
		MAStatus cs=UnderstandingStatus.get().status;
		return (cs==null)?null:cs.getSelectedParking();
	}

	@Override
	public void setSelected(PkFacility item) {
		MAStatus cs=UnderstandingStatus.get().status;
        if (cs!=null) cs.setSelectedParking(item);
	}

	@Override
	public MapItemIterator<PkFacility> getIterator() {
		MAStatus cs=UnderstandingStatus.get().status;
		return (cs==null)?null:cs.getParkings();
	}

	@Override
	public void setIterator(MapItemIterator<PkFacility> iterator) {
		MAStatus cs=UnderstandingStatus.get().status;
		if (cs!=null) cs.setParkings(iterator);
	}

	@Override
	public void sayMoMoreOptions() {
		MyTTS.speakText(App.self.getString(R.string.pks_no_more_parking));
		VoiceIO.listenAfterTheSpeech();
	}

	@Override
	public GeoSpannable<PkFacility> getSpannable() {
		return UnderstandingStatus.get().status.getParkingResponse();
	}

}
