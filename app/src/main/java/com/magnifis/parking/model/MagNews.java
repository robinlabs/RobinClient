package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class MagNews {
	@ML("item")
	protected String items[] = null;

	public String[] getItems() {
		return items;
	}

	public void setItems(String[] items) {
		this.items = items;
	}
}
