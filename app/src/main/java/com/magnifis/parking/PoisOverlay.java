package com.magnifis.parking;

import java.net.MalformedURLException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.magnifis.parking.GeoItemsOverlay.TheOverlayItem;
import com.magnifis.parking.MultipleEventHandler.EventSource;
import com.magnifis.parking.model.Poi;
import com.magnifis.parking.utils.ImageFetcher;
import com.magnifis.parking.views.TheMapView;

public class PoisOverlay extends GeoItemsOverlay<Poi> {

	public PoisOverlay(MapItemSetContoller<Poi> controller, TheMapView mv) {
		super(controller, mv);
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return new TheOverlayItem(controller.get(i)) {
			@Override
			public Drawable getMarker(int stateBitset) {
			    
				final Drawable mm=super.getMarker(stateBitset);
				
			    final boolean sel=controller.isTooltipAllowed()&&controller.isSelected(fas);
			    
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
						if (sel) {
						  String iUrl=fas.getImageUrl();
						  int h=10;
						  if (iUrl!=null) {
						    Bitmap im=ImageFetcher.getFromCache(iUrl);
    					    if (im!=null) {
							  Rect srcR=new Rect(0,0,im.getWidth()-1,im.getHeight()-1);
							  h=2*mm.getIntrinsicHeight()/3-10;
							  int a=10-mm.getIntrinsicWidth()/2, 
								  b=8-mm.getIntrinsicHeight();
							  Rect dstR=new Rect(
									a,
									b,
									a+h,
									b+h
									
							   );
							  canvas.drawBitmap(im, srcR, dstR, null);
							  h+=20;
						    } else
						      loadIconAndRepaint(iUrl);
						  }
						  
						  Rect br=new Rect();
						  String t=fas.getAddress();
						  int avl=mm.getIntrinsicWidth()-h-30;//-(5*mm.getIntrinsicWidth()/12);
								  
								  //(10*mm.getIntrinsicWidth()/12)-h;
						  int loff=/*(-5*mm.getIntrinsicWidth()/12)+*/
								  -mm.getIntrinsicWidth()/2+h,
								   xoff=0;
						  if (t!=null) {
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
							 // xoff=(br.width()<avl)?(avl-br.width())/2:0;
							  canvas.drawText(
										t, 
										loff+xoff,
										-mm.getIntrinsicHeight()
										  +paintBig.getTextSize()+9
										  +paintAddr.getTextSize(),
										paintAddr
										);									  
						  }
						  
						  t=fas.getName();
						  if (t==null) t="";
						  
						  Paint pt=paintBig;
						  
						  pt.getTextBounds(t, 0, t.length(), br);
						  if (br.width()>avl) {
							  pt=paintAddr; 
							  pt.getTextBounds(t, 0, t.length(), br);
							  if (br.width()>avl) for (;;) {
								 t=t.substring(0,t.lastIndexOf(' '));
								 String tt=t+" ...";
								 pt.getTextBounds(tt, 0, tt.length(), br);
								 if (br.width()<=avl) {
									 t=tt; break;
								 }
							  }
						  }
						  						  
						 // xoff=(br.width()<avl)?(avl-br.width())/2:0;

						  canvas.drawText(
									t, 
									loff+xoff,
									-mm.getIntrinsicHeight()+paintBig.getTextSize()+2,
									pt
									);
	
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
