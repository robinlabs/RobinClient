package com.magnifis.parking.model.audioburst;

import java.io.Serializable;
import java.util.Date;

import com.magnifis.parking.Xml.ML;

import android.text.format.Time;

public class Details implements Serializable {
	@ML("status")
	protected Integer status=null;	//0
	@ML("category")
	protected String category=null;	

	@ML(tag="dated", attr="date", format="yyyy-MM-dd HH:mm")
	protected Date  dated=null; //	"2016-11-28 08:29"

	@ML("text")
	protected String text=null; //	"To be said about donald trump and goes after romney and and again my whole point is why well you don't have to run a to this did we donald trump created all this it's donald trump and said he wants run that that wants to consider romney why and they question would donald trump is doing like i said this just two can try to come tribe something's up here and i really do think met maybe part of it is because there was so much blow back from trump's early trump supporters coming out and say no not going to go after hillary oh you third nice people all of this i think many trump supporters culpa such a betrayal but they've got to give them something and who they give the head admit romney and i i think maybe trump maybe just naturally expected dipped some point romney to come out and say listen you know i said a lot of things that that that were true or i was wrong about i'm one i said them or whatever after meeting with them i realize that i'm wrong with him wrong about him and and maybe is i'm happy that he hasn't heard that yeah."
	@ML("source")
	protected String source=null; //	"760 KFMB Radio"
	@ML("title")
	protected String title=null; //	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	
	public Date getDated() {
		return dated;
	}
	public void setDated(Date dated) {
		this.dated = dated;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	
}
