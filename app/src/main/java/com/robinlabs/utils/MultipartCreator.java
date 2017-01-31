package com.robinlabs.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class MultipartCreator {
	protected static class Part {
		InputStream is=null;
		String mimeType=null, partName=null, fileName=null;
		public InputStream getIs() {
			return is;
		}
		public Part setIs(InputStream is) {
			this.is = is;
			return this;
		}
		public String getContentType() {
			return mimeType;
		}
		public Part setContentType(String mimeType) {
			this.mimeType = mimeType;
			return this;
		}
		public String getPartName() {
			return partName;
		}
		public Part setPartName(String partName) {
			this.partName = partName;
			return this;
		}
		public String getFileName() {
			return fileName;
		}
		public Part setFileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
	}
	
	protected String boundary="----=_"+new java.util.Random().nextInt()+"."+System.currentTimeMillis()*7+"=---";
	
	protected List<Part> parts=new ArrayList<Part>();
	
	public synchronized MultipartCreator addPlainText(String txt, String partName) throws UnsupportedEncodingException {
		return addPart(txt, "UTF-8", partName, "text/plain; charset=utf-8");
	}
	
	public synchronized MultipartCreator addPart(String txt, String encoding, String partName, String mimeType) throws UnsupportedEncodingException {
		return addPart(txt.getBytes(encoding), mimeType, partName, null);
	}
	
	public synchronized MultipartCreator addPart(byte bytes[], String mimeType, String partName, String fileName) {
		ByteArrayInputStream bais=new ByteArrayInputStream(bytes);
		return addPart(bais, mimeType, partName, fileName);		
	}
	
	public synchronized MultipartCreator addPart(InputStream is, String mimeType, String partName, String fileName) {
		parts.add(
		   new Part().setFileName(fileName).setIs(is).setPartName(partName).setContentType(mimeType)
		);
		return this;
	}
	
	public String getBoundary() {
	   return boundary;	
	}
	
	public MultipartCreator setContentTypeTo(HttpURLConnection uc) {
		uc.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
		return this;
	}
	
	protected static final String LINE_FEED = "\r\n";
	
	public synchronized void writeTo(OutputStream os) throws IOException {
		for (Part p:parts) {
	      StringBuilder sb=new StringBuilder("--");
	      sb.append(boundary);
	      sb.append(LINE_FEED);
	      if (p.getContentType()!=null) sb.append("Content-Type: ").append(p.getContentType()).append(LINE_FEED);
	      sb.append("Content-Disposition:  form-data; name=\"");
	      sb.append(p.getPartName());
	      sb.append("\";");
	      if (p.getFileName()!=null) {
	    	  sb.append(" filename=\"");
	    	  sb.append(p.getFileName());
	    	  sb.append('"');
	      }
	      sb.append(LINE_FEED);
	      sb.append(LINE_FEED);
		  os.write(sb.toString().getBytes("ISO-8859-1"));
		  StreamUtils.copy(p.getIs(), os);
		  os.write(LINE_FEED.toString().getBytes("ISO-8859-1"));
		}
		os.write(
			new StringBuilder("--")
		      .append(boundary)
		      .append("--")
		      .append(LINE_FEED)
			  .toString().getBytes("ISO-8859-1")
		);
	}
}
