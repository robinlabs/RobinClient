package com.magnifis.parking;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.magnifis.parking.GeoItemsOverlay.TheOverlayItem;
import com.magnifis.parking.model.PkFacility;
import com.magnifis.parking.views.TheMapView;

public class PksOverlay extends GeoItemsOverlay<PkFacility> {

	public PksOverlay(MapItemSetContoller<PkFacility> controller, TheMapView mv) {
		super(controller, mv);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return new TheOverlayItem(controller.get(i)) {
			@Override
			public Drawable getMarker(int stateBitset) {
			    
				final Drawable mm=super.getMarker(stateBitset);
				
			    final boolean sel=controller.isSelected(fas);
			    
			    final Integer ld=fas.getOccupancy_pct();
			    
		    	return new Drawable() {

					@Override
					public int getIntrinsicHeight() {
						return ih;
					}

					@Override
					public int getIntrinsicWidth() {
						return iw;
					}

					@Override
					public void draw(Canvas canvas) {
						mm.draw(canvas);
						String pr=fas.getFormattedPrice();
						Rect br=new Rect();
						if (sel) {
						  String t=fas.getType();
						  if (t==null) t="";
						  if (pr!=null) t+=" - "+pr+" h";
						  paintBig.getTextBounds(t, 0, t.length(), br);

						  canvas.drawText(
									t, 
									-mm.getIntrinsicWidth()/2+mm.getIntrinsicWidth()/5+13,
									-mm.getIntrinsicHeight()+paintBig.getTextSize()+6,
									paintBig
									);
						  
						  t=fas.getAddress();
						  if (t==null) t=fas.getName();
						  if (t!=null) {
							  int avl=3*mm.getIntrinsicWidth()/5;
							  for (;;) {
							     paintAddr.getTextBounds(t, 0, t.length(), br);
							     if (br.width()<=avl) break;
							     int i=t.lastIndexOf(',');
							     if (i>0) {
							    	t=t.substring(0, i);
							     } else {
							        break;
							     }
							  }
							  canvas.drawText(
										t, 
										-mm.getIntrinsicWidth()/2+mm.getIntrinsicWidth()/5+13,
										-mm.getIntrinsicHeight()
										  +paintBig.getTextSize()+9
										  +paintAddr.getTextSize(),
										paintAddr
										);									  
						  }
						  if (ld!=null) {
							t=ld.toString()+"%";
							paintBig.getTextBounds(t, 0, t.length(), br);
							canvas.drawText(
									t, 
									-mm.getIntrinsicWidth()/2+((mm.getIntrinsicWidth()/5-br.width())/2)+2,
									-mm.getIntrinsicHeight()*7/10+paintBig.getTextSize()/2,
									paintBig
									);								  
							}
						} else {
							if (pr!=null) {
								paint.getTextBounds(pr, 0, pr.length(), br);

								canvas.drawText(
										pr, 
										-br.width()/2,
										-mm.getIntrinsicHeight()*5/7+paint.getTextSize()/2,
										paint
										);
							}
						}
					}

					@Override
					public int getOpacity() {
						// TODO Auto-generated method stub
						return 100;
					}

					@Override
					public void setAlpha(int alpha) {
						// TODO Auto-generated method stub	
					}

					@Override
					public void setColorFilter(ColorFilter cf) {
						// TODO Auto-generated method stub	
					}
		    		
		    	};

			  //////////
			}
		};
	}
	
	

}
