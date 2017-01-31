package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;
/**
 * @author chek
 * Facebook feed class, for parsing facebook feed
 */
public class FbFQLFeedResponse implements Serializable {
	
	@ML("data")
	protected FbFQLFeedPost posts[]=null;
	
	/**
	 * @return the posts
	 */
	public FbFQLFeedPost[] getPosts() {
		return posts;
	}

	public void setPosts(FbFQLFeedPost[] posts) {
		this.posts = posts;
	}	
}
