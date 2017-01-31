package com.magnifis.parking.model;

import static com.magnifis.parking.utils.Utils.isEmpty;

import java.io.Serializable;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Flags.Flag;

import android.graphics.drawable.Drawable;

import com.magnifis.parking.Log;
import com.magnifis.parking.Xml.ML;
import com.magnifis.parking.Xml.ML_alternatives;
import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.messaging.Message;

/**
 * @author chek Facebook feed post class, for parsing facebook feed
 */
public class FbFeedPost implements Serializable {

	@ML_alternatives({
		   @ML(tag="id"),
		   @ML(tag="post_id")
	})	
	protected String id = null;

	@ML("message")
	protected String message = null;

	@ML("type")
	protected String type = null;

	@ML("description")
	protected String description = null;

	@ML("link")
	protected String link = null;

	@ML("picture")
	protected String picture = null;

	@ML(tag="created_time",format="yyyy-MM-dd'T'HH:mm:ssZ")
	protected Date createdTime = null;
	
	@ML("from")
	protected From from = null;

	public From getFrom() {
		return from;
	}

	public void setFrom(From from) {
		this.from = from;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public Message toMessage() {
		Message msg = new Message();
		msg.setType(Message.TYPE_FACEBOOK);
		msg.setSent(this.getCreatedTime());
		msg.setId(this.getId());
		
		Addressable addressable = new Addressable();
		addressable.setAddress(from.getId());
		addressable.setDisplayName(from.getName());
		msg.setSender(addressable);
		
		if (!isEmpty(message)) {
			msg.setBody(message);
		} else if (!isEmpty(description)) {
			msg.setBody(description);
		}
		return msg;
	}
	
	public static class From {
		@ML("id")
		protected String id = null;

		@ML("name")
		protected String name = null;
				
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}	
}
