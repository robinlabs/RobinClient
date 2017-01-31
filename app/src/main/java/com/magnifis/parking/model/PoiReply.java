package com.magnifis.parking.model;

import java.io.Serializable;

import android.content.Context;

import com.magnifis.parking.Xml.ML;

public class PoiReply extends PoiLikeGeoSpannable<Poi> {
	/*
	public void calculate(Context ctx, Understanding rp) {
	  super.calculate(ctx, rp);
	  orderByDistance();
	}
*/
    @ML("poi")
    protected Poi pois[]=null;
	
	public Poi[] getPois() {
		return pois;
	}

	public void setPois(Poi[] pois) {
		this.pois = pois;
	}

	@Override
	public Poi[] getFacilities() {
		return getPois();
	}

}
