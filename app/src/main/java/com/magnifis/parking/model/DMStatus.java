package com.magnifis.parking.model;

import com.magnifis.parking.db.SqliteDB.DB;

public class DMStatus {
/*
uri=http://173.255.249.124/russian-alyona-lf-22khz.zip
status=200
hint=file:///mnt/sdcard/Download/russian-alyona-lf-22khz.zip
media_type=application/zip
total_size=21678080
last_modified_timestamp=1382005284620
bytes_so_far=21678080
local_uri=file:///mnt/sdcard/Download/russian-alyona-lf-22khz.zip
reason=placeholder
 * */	
	
	@DB
	protected String uri=null;
	@DB
	protected Integer status=null;
	@DB
	protected String hint=null;
	@DB("media_type")
	protected String mediaType=null;
	@DB("total_size")
	protected Long totalSize=null;
	@DB("last_modified_timestamp")
	protected Long lastModifiedTimestamp=null;
	@DB("bytes_so_far")
	protected Long bytesSoFar=null;
	@DB("local_uri")
	protected String localUri=null;
	@DB
	protected String reason=null;
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	public String getMediaType() {
		return mediaType;
	}
	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}
	public Long getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(Long totalSize) {
		this.totalSize = totalSize;
	}
	public Long getLastModifiedTimestamp() {
		return lastModifiedTimestamp;
	}
	public void setLastModifiedTimestamp(Long lastModifiedTimestamp) {
		this.lastModifiedTimestamp = lastModifiedTimestamp;
	}
	public Long getBytesSoFar() {
		return bytesSoFar;
	}
	public void setBytesSoFar(Long bytesSoFar) {
		this.bytesSoFar = bytesSoFar;
	}
	public String getLocalUri() {
		return localUri;
	}
	public void setLocalUri(String localUri) {
		this.localUri = localUri;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
}
