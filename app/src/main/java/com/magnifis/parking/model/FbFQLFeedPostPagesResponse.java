package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class FbFQLFeedPostPagesResponse   implements Serializable {
	@ML("data")
	protected FbFQLFeedPostPage pages[]=null;
	
	/**
	 * @return the posts
	 */
	public FbFQLFeedPostPage[] getPages() {
		return pages;
	}

	public void setUsers(FbFQLFeedPostPage[] pages) {
		this.pages = pages;
	}
}

