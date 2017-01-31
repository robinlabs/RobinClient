package com.magnifis.parking;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.magnifis.parking.GeoItemsOverlay.TheOverlayItem;
import com.magnifis.parking.model.GasPrice;
import com.magnifis.parking.model.GasStation;
import com.magnifis.parking.utils.ImageFetcher;
import com.magnifis.parking.views.TheMapView;

public class GasOverlay extends GeoItemsOverlay<GasStation> {

	public GasOverlay(MapItemSetContoller<GasStation> controller, TheMapView mv) {
		super(controller, mv);
	}
	
	final static int //FuelGreen=Color.argb(255, 0, 180, 0),
			         BestPriceColor=
			        		 Color.argb(255, 255, 92, 89)
			         //Color.RED
			       ;
	
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
						GasPrice gp=fas.getPrice();
						//boolean pricePresents=gp!=null&&gp.getPrice()!=null;
						
						Bitmap im=ImageFetcher.getFromCache(fas.getImageUrl());
						Rect srcR=im==null?null:new Rect(0,0,im.getWidth()-1,im.getHeight()-1);
						mm.draw(canvas);
						if (sel) {
						  int h=10;
    					  if (im!=null) {
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
						  
						  t=fas.getName()+" - ";//+fas.getFormattedPrice();
						  if (t==null) t="";
						  
						  Paint pt=new Paint(paintBig);
						  
						  pt.getTextBounds(t, 0, t.length(), br);
						  /*
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
						  */
						  						  
						 // xoff=(br.width()<avl)?(avl-br.width())/2:0;

						  canvas.drawText(
									t, 
									loff+xoff,
									-mm.getIntrinsicHeight()+paintBig.getTextSize()+2,
									pt
									);
						  
						  /*if (pricePresents)*/ {
							  if (gp.isMin())  pt.setColor(BestPriceColor);

							  canvas.drawText(
									  " "+fas.getFormattedPrice(), 
									  loff+xoff+br.width(),
									  -mm.getIntrinsicHeight()+paintBig.getTextSize()+2,
									  pt
									  );	
						  }
	
					   } else { // !sel
						   if (im!=null) {
							   int     h=mm.getIntrinsicWidth()-22,
									   a=-h/2, 
									   b=4-mm.getIntrinsicHeight();
							   Rect dstR=new Rect(
									   a,
									   b,
									   a+h,
									   b+h
							   );
							   canvas.drawBitmap(im, srcR, dstR, null);						   
						   }
						   /*if (pricePresents)*/ {
							   String pr=gp.getFormattedPrice();
							   Rect br=new Rect();
							   Paint pt=new Paint(paint);
							   pt.setTextScaleX(1.0f);

							   pt.getTextBounds(pr, 0, pr.length(), br);

							   int color=Color.BLACK;

							   if (gp.isMin()) color=BestPriceColor;//FuelGreen; 
							   /*
						     else
						    	if (gp.isMax()) color=Color.RED;
							    */

							   pt.setColor(color);

							   canvas.drawText(
									   pr, 
									   -br.width()/2,
									   2*-mm.getIntrinsicHeight()/7,
									   pt
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
