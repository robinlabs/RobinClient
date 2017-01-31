package com.magnifis.parking.cmd;

import android.content.Context;
import android.view.View;

import com.magnifis.parking.MainActivity;
import com.magnifis.parking.UnderstandingProcessorBase;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.ISwitchToBubbles;

public class CmdHandlerBase implements ISwitchToBubbles {
	
	protected MainActivity mainActivity=null;
	protected Context context=null;
	
	public CmdHandlerBase(Context context) {
		this.context=context;
		if (context instanceof MainActivity) this.mainActivity=(MainActivity)context;	
		touchLastInteractionTime();
	}
	
	protected void touchLastInteractionTime() {
		this.lastInteractionTime=System.currentTimeMillis();
	}
	
	protected long lastInteractionTime;
	
	protected long getTimeountToDeactivate() {
		return 180000;
	}
	
	protected boolean isTimeoutTooLong() {
		long to=getTimeountToDeactivate();
		return to>0&&System.currentTimeMillis()-lastInteractionTime>to;
	}

	@Override
	public boolean shouldSwitchToBubbles() {
		return true;
	}
	
}
