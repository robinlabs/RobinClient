package com.magnifis.parking.model;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.utils.Utils;

public class Intro {
	public static class IntroItem {

		@ML(attr="img")
		protected String img=null;
		@ML
		protected String text=null;
		public String getImg() {
			return img;
		}
		public void setImg(String img) {
			this.img = img;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		
		public Bitmap getIcon() {
			return Utils.isEmpty(img)?null:Utils.getImgResource("/res/intro/"+img);
		}
		
	}
	
	@ML("page")
	protected IntroItem items[]=null;

	public IntroItem[] getItems() {
		return items;
	}

	public void setItems(IntroItem[] items) {
		this.items = items;
	}
	

}
