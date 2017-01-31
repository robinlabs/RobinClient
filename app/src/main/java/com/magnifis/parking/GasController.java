package com.magnifis.parking;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.magnifis.parking.model.DoublePoint;
import com.magnifis.parking.model.GeoObject;
import com.magnifis.parking.model.GeoSpannable;
import com.magnifis.parking.model.GasStation;
import com.magnifis.parking.model.UnderstandingStatus;
import com.magnifis.parking.views.GasDetails;
import com.magnifis.parking.views.ParkingDetails;
import com.magnifis.parking.views.PoiDetails;

public class GasController extends MapItemSetContoller<GasStation> {

	@Override
	public int size() {
	  int sz=super.size();
	  if (sz>0&&!isCurrent()) return 1;
	  return sz;
	}
	
	private Drawable icGas,popupGas;
	
	public GasController() {
		Resources res=App.self.getResources();
		icGas=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.gas_icon_small));
		popupGas=GeoItemsOverlay.boundCenterBottom(res.getDrawable(R.drawable.short_details_popup));
	}

	@Override
	public GasStation getSelected() {
		MAStatus cs=UnderstandingStatus.get().status;
		return cs==null?null:cs.getSelectedGasStation();
	}

	@Override
	public void setSelected(GasStation item) {
	   MAStatus cs=UnderstandingStatus.get().status;
       if (cs!=null) cs.setSelectedGasStation(item);
	}

	@Override
	public MapItemIterator<GasStation> getIterator() {
		MAStatus cs=UnderstandingStatus.get().status;
		return cs==null?null:cs.getGasStationIterator();
	}

	@Override
	public void setIterator(MapItemIterator<GasStation> iterator) {
		   MAStatus cs=UnderstandingStatus.get().status;
	       if (cs!=null) cs.setGasStationIterator(iterator);
	}

	@Override
	public Drawable getDefaultMarker() {
		// TODO Auto-generated method stub
		return icGas;
	}

	@Override
	public Drawable getDefaultPopup() {
		// TODO Auto-generated method stub
		return popupGas;
	}
	
	@Override
	public void sayDetails(boolean sayExtra) {
		Phrases.sayDescription(getSelected());
	}
	
	@Override
	public void showDetails() {
	  MainActivity ma=MainActivity.get();
	  ma.showDetails(
		 new GasDetails(ma),
		 getSelected()
	  );
 	}

	@Override
	public GeoSpannable<GasStation> getSpannable() {
		return UnderstandingStatus.get().status.getGasStations();
	}
	

}
