package com.magnifis.parking.model;

import java.io.Serializable;

import android.graphics.drawable.BitmapDrawable;

import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.utils.Utils;

public class HelpTopic implements Serializable {
	
	public static class Example implements Serializable  {
		@ML
		protected String text=null;
		@ML(attr="noexec")
		protected Boolean noexec=false;
		
		public String toString() {
        	return text;
        }
        public boolean isExecutable() {
          return !noexec;
        }
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public Boolean getNoexec() {
			return noexec;
		}
		public void setNoexec(Boolean noexec) {
			this.noexec = noexec;
		}
        
	}
	
	@ML("icon")
	protected String iconUrl=null;
	@ML("launch")
	protected String launch=null;
	@ML("open_url")
	protected String openUrl=null;

	
	public String getOpenUrl() {
		return openUrl;
	}

	public void setOpenUrl(String openUrl) {
		this.openUrl = openUrl;
	}

	public String getLaunch() {
		return launch;
	}

	public void setLaunch(String launch) {
		this.launch = launch;
	}

	public BitmapDrawable getIcon() {
		return Utils.isEmpty(iconUrl)?null:Utils.getImgResourceDrawable("/res/help/"+iconUrl);
	}
	
    public String getIconUrl() {
		return iconUrl;
	}
	public void setIconUrl(String icon) {
		this.iconUrl = icon;
	}
	
	
	@Override
	public String toString() {
	  return " "+name;
	}
	
	@ML("name")
	protected String name = null;
	@ML("example")
	protected Example examples[] = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Example[] getExamples() {
		return examples;
	}

	public void setExamples(Example[] examples) {
		this.examples = examples;
	}
}
