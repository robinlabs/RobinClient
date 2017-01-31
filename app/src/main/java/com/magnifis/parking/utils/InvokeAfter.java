package com.magnifis.parking.utils;

public class InvokeAfter {
	
	final Runnable todo;
	final long timeout;
	long when;

	public InvokeAfter(Runnable todo, long timeout) {
       this.todo=todo;
       this.timeout=timeout;
	}
	
	private Thread worker=null;
	
	public void touch() {
	   when=System.currentTimeMillis()+timeout;
	   synchronized(this) {
		  if (worker==null||!worker.isAlive()) {
			  worker=new Thread("InvokeAfter_worker") {
				 long tm=timeout;
				 @Override
				 public void run() {
					for (;;) {
					  try {
						sleep(tm);
						tm=when-System.currentTimeMillis();
						if (tm<=0) {
						  todo.run();
						  synchronized(this) {
							  worker=null;
						  }
						  break;
						}
					  } catch (InterruptedException e) {
					  }
					}
				 }
			  };
			  worker.start();
		  }
	   }
	}

}
