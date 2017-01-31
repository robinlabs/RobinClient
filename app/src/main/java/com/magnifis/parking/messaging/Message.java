package com.magnifis.parking.messaging;

import java.io.Serializable;
import java.util.Date;

import com.magnifis.parking.utils.SerializableParcelable;
import com.magnifis.parking.utils.SerializableParcelable.SPCreator;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;

public class Message extends SerializableParcelable {
	private static final long serialVersionUID = 1L;

//	private final String TAG = getClass().getSimpleName();

	public static final int TYPE_SMS = 0;
	public static final int TYPE_EMAIL = 1;
	public static final int TYPE_FACEBOOK = 2;
	public static final int TYPE_TWITTER = 3;

	public Date getSent() {
		return sent;
	}

	public void setSent(Date sent) {
		this.sent = sent;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	
    protected Integer       statusBarNotificationId=null;
	protected String        subject=null, body=null;
	protected Date          received=null, sent=null;
	protected Integer       type=null;
	protected String        id=null;
	protected Addressable   sender=null, to[]=null, cc[]=null, bcc[]=null;
	protected Boolean       read=false;
	protected transient     Object original=null;
	
	
	
	
	public Object getOriginal() {
		return original;
	}

	public void setOriginal(Object original) {
		this.original = original;
	}

	public Boolean isRead() {
		return read!=null&&read;
	}

	public void setRead(Boolean read) {
		this.read = read;
	}

	public Integer getStatusBarNotificationId() {
		return statusBarNotificationId;
	}

	public void setStatusBarNotificationId(Integer statusBarNotificationId) {
		this.statusBarNotificationId = statusBarNotificationId;
	}

	public Addressable[] getTo() {
		return to;
	}

	public void setTo(Addressable[] to) {
		this.to = to;
	}

	public Addressable getSender() {
		return sender;
	}

	public void setSender(Addressable sender) {
		this.sender = sender;
	}

	public Addressable[] getCc() {
		return cc;
	}

	public void setCc(Addressable[] cc) {
		this.cc = cc;
	}

	public Addressable[] getBcc() {
		return bcc;
	}

	public void setBcc(Addressable[] bcc) {
		this.bcc = bcc;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Message() {}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getType() {
		return type;
	}

	public Date getReceived() {
		return received;
	}

	public void setReceived(Date date) {
		this.received = date;
	}
	
	public static final Creator<Message> CREATOR= new SPCreator<Message>(Message.class);


}
