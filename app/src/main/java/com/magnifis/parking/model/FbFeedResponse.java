package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;
/**
 * @author chek
 * Facebook feed class, for parsing facebook feed
 */
public class FbFeedResponse implements Serializable {
	
	@ML("data")
	protected FbFeedPost posts[]=null;
	
	/**
	 * @return the posts
	 */
	public FbFeedPost[] getPosts() {
		return posts;
	}

	public void setPosts(FbFeedPost[] posts) {
		this.posts = posts;
	}	
}
