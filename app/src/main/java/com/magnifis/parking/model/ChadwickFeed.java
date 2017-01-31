package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class ChadwickFeed {
	
	 @ML("stories")
	 protected ChadwickStory[] stories=null;
	
	 public ChadwickStory[] getStories() {
		 return stories; 
	 }
	 
}
