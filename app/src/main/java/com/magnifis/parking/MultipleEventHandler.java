/**
 * 
 */
package com.magnifis.parking;

import android.app.Activity;
import android.content.Context;

/**
 * @author zeev
 *
 */
public abstract class MultipleEventHandler<T> {
   final static private String TAG="MultipleEventHandler";
	
   protected volatile T token=null;
	
   protected abstract void onCompletion();
	
   public class EventSource {
	  public boolean isCompleted() {
		 return completed;
	  }
	  public boolean isFired() {
		 return fired;
	  }
	  boolean completed=false, fired=false;
	  public void fireEvent(T t) {
		synchronized(this) {
		    if (fired) return;
		    fired=true;
		}
		if (onFire!=null) onFire.run();
		if (!completed&&counter>0) synchronized(EventSource.this) {
		  if (!completed&&counter>0) {
		    completed=true;
		    if (t!=null) token=t;	    
		    
		    if (--counter==0) {
		      onCompletion();
		    }
		  }
		}
	  }
	  protected Runnable onFire=null;
	  public void setOnFire(Runnable r) {
		  onFire=r; 
	  }
	  public void fireEvent() {
		  fireEvent(null);
	  }
	  public void fireEventFromGui(Activity ctx) {
		 ctx.runOnUiThread(
			new Runnable() {
				@Override
				public void run() {
				   fireEvent();
				}
			}
		 );
	  }
   }
   protected volatile int counter=0;
   
   public EventSource newEventSource() {
	  synchronized(this) {
		++counter;
	    return new EventSource();
	  }
   }
   
   public MultipleEventHandler() {
   }

   /**
    * @return the counter
    */
   public int getCounter() {
	   return counter;
   }


}
