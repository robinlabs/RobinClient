package com.magnifis.parking;

import android.graphics.Point;
import android.graphics.Rect;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.magnifis.parking.model.PkFacility;

public abstract class GeoOverlayItem<DataItem> extends OverlayItem {

	public GeoOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
	}
	
	abstract public DataItem getItem();
	
	abstract public int getIntrinsicHeight();

	abstract public int getIntrinsicWidth();
	
	abstract public Rect getClickableBox(Point pt, boolean isSel);
	
}