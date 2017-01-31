package com.magnifis.parking.model;

import com.magnifis.parking.Xml.ML;

public class HelpFile {
	@ML("title")
	protected String title = null;
	@ML("topic")
	protected HelpTopic topics[] = null;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public HelpTopic[] getTopics() {
		return topics;
	}

	public void setTopics(HelpTopic[] topics) {
		this.topics = topics;
	}

}
