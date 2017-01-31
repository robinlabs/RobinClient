package com.magnifis.parking.model;

import java.util.Date;

import com.magnifis.parking.db.SqliteDB.DB;

public class SmsRecord {
	@DB("_id")
	protected int id;
	@DB("threadId")
	protected Integer threadId=null;
	@DB("read")
	protected Boolean read=null;
	@DB("seen")
	protected Boolean seen=null;
	@DB("address")
	protected String address=null;
	@DB("body")
	protected String body=null;
	@DB("date")
	protected Date date=null;
	@DB("date_sent")
	protected Date sent=null;
	
	
	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public Date getSent() {
		return sent;
	}
	public void setSent(Date sent) {
		this.sent = sent;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Integer getThreadId() {
		return threadId;
	}
	public void setThreadId(Integer thread_id) {
		this.threadId = thread_id;
	}
	public Boolean getRead() {
		return read;
	}
	public void setRead(Boolean read) {
		this.read = read;
	}
	public Boolean getSeen() {
		return seen;
	}
	public void setSeen(Boolean seen) {
		this.seen = seen;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

}
