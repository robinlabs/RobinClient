package com.magnifis.parking.model;

import java.net.MalformedURLException;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.utils.ImageFetcher;

import android.content.Context;

public abstract class PoiLikeGeoSpannable<T extends PoiLike> extends
		GeoSpannable<T> {
	@ML("poi_name")
	protected String poiName = null;

	public String getPoiName() {
		return poiName;
	}

	public void setPoiName(String poiName) {
		this.poiName = poiName;
	}
	
	public void preloadImages() {
		preloadImages(0);
	}

	public void preloadImages(int limit) {
		T fass[] = getFacilities();
		if (fass != null) {
			int c=0;
			for (T fas : fass)
				if (fas.getImageUrl() != null)
					try {
						ImageFetcher.syncCacheImage(fas.getImageUrl());
						if ((limit!=0)&&(++c>=limit)) break;
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		}
	}

}
