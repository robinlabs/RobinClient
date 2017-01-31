package com.magnifis.parking;

public abstract class ProgressIndicatorController {

	abstract public void show(String what, String who);
	
    public void show() { show("",""); }
    
    abstract public void hide();

}
