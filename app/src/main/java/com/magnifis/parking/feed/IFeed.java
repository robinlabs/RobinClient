package com.magnifis.parking.feed;

import com.magnifis.parking.App;
import com.magnifis.parking.R;
import com.magnifis.parking.model.Understanding;

public interface IFeed {
/*	
	public String getLastReadId();
	public String getThingName();	
    public String getThingsName();
    public boolean canShare();
    public boolean canPost();
	public void read(final int N, String sinceId, final boolean fNew, boolean fExcl, final String qi);
*/
	public void readAgain();
	public void readDetailed();
	public void readPrevious();
	public void readNext();
	public void coutinueReading();
}
