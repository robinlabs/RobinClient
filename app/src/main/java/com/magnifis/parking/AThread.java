package com.magnifis.parking;

public class AThread extends Thread {
	
	public static class AbortedException extends Exception {
	  public AbortedException() {
		  super("aborted");
	  }
	}
	
    public boolean fAbort=false;
    
    public void condAbort() throws AbortedException {
  	  if (fAbort) throw new AbortedException();
    }
    
    protected Runnable myAborter=new Runnable() {
			@Override
			public void run() {
				fAbort=true;
				interrupt();
			}
	 };
	 
	public void setAborter() {
		MainActivity.get().setAborter(myAborter);
	}

	public AThread() {
	}

	public AThread(Runnable runnable) {
		super(runnable);
	}

	public AThread(String threadName) {
		super(threadName);
	}

	public AThread(Runnable runnable, String threadName) {
		super(runnable, threadName);
	}

	public AThread(ThreadGroup group, Runnable runnable) {
		super(group, runnable);
	}

	public AThread(ThreadGroup group, String threadName) {
		super(group, threadName);
		// TODO Auto-generated constructor stub
	}

	public AThread(ThreadGroup group, Runnable runnable, String threadName) {
		super(group, runnable, threadName);
		// TODO Auto-generated constructor stub
	}

	public AThread(ThreadGroup group, Runnable runnable, String threadName,
			long stackSize) {
		super(group, runnable, threadName, stackSize);
		// TODO Auto-generated constructor stub
	}

}
