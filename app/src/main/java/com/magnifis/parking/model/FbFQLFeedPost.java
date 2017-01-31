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
import com.magnifis.parking.utils.Utils;

/**
 * @author chek Facebook feed post class, for parsing facebook feed
 */
public class FbFQLFeedPost implements Serializable {

	@ML_alternatives({
		   @ML(tag="post_id")
	})	
	protected String id = null;

	@ML("message")
	protected String message = null;

	@ML("type")
	protected Integer type = null;

	@ML("description")
	protected String description = null;
	

	@ML(tag="created_time"/*,format="yyyy-MM-dd'T'HH:mm:ssZ"*/)
	protected Long createdTime = null;
	
	@ML("actor_id")
	protected String actorId = null;

	@ML("name")
	protected String name = null;
	
	@ML("attachment")
	protected Attachment attachment = null;

	public String getAttacmentType() {
		String result = null;
		if (attachment != null && attachment.getMedia() != null && attachment.getMedia().getType() != null) {
			result = attachment.getMedia().getType();
		}
		return result;
	}	
	
	public String getAttachmentCaption() {
		String result = null;
		if (attachment != null && !Utils.isEmpty(attachment.getCaption())) {
			result = attachment.getCaption(); 
		}
		return result;
	}	
	
	public String getActorId() {
		return actorId;
	}

	public void setActorId(String id) {
		this.actorId = id;
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreatedTime() {		
		return new Date(createdTime);
	}

	public void setCreatedTime(Long createdTime) {
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Message toMessage() {
		Message msg = new Message();
		msg.setType(Message.TYPE_FACEBOOK);
		msg.setSent(this.getCreatedTime());
		msg.setId(this.getId());
	
		
		Addressable addressable = new Addressable();
		addressable.setAddress(this.getActorId());
		addressable.setDisplayName(this.getActorId());
		msg.setSender(addressable);
		
		if (attachment != null) {
			if (!isEmpty(attachment.getName())) {
				msg.setSubject(attachment.getName());
			}
			if (!isEmpty(attachment.getDescription())) {
				msg.setBody(attachment.getDescription());
			}
		}
		return msg;
	}

	public static class Attachment {

		@ML("name")
		protected String name = null;
		
		@ML("description")
		protected String description = null;
		
		@ML("caption")
		protected String caption = null;

		@ML("media")
		protected Media media = null;
		
		public Media getMedia() {
			return media;
		}

		public void setMedia(Media media) {
			this.media = media;
		}

		public String getCaption() {
			return caption;
		}
		
		public void setCaption(String caption) {
			this.caption = caption;
		}
		
		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}
				
	public static class Media {

		@ML("type")
		protected String type = null;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		
	}


}
