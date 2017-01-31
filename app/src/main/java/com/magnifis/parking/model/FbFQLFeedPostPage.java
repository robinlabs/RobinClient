package com.magnifis.parking.model;

import java.io.Serializable;

import com.magnifis.parking.Xml.ML;

public class FbFQLFeedPostPage  implements Serializable {
	
    public String getPage_id() {
		return page_id;
	}

	public void setPage_id(String page_id) {
		this.page_id = page_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ML(tag="page_id")
	protected String page_id = null;

	@ML("name")
	protected String name = null;

}
