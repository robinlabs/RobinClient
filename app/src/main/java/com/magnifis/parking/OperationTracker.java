package com.magnifis.parking;

import static com.magnifis.parking.VoiceIO.fireOpes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.magnifis.parking.utils.Utils;

import android.content.Context;
import android.util.Log;

public class OperationTracker extends Semaphore implements TextMessageQue {
	
	
	@Override
	public void queTextMessage(Context ctx, Object o) {
		queTextMessage(ctx, o, o);
	}

	public void queTextMessage(final Context ctx, final Object toSay, final Object toShow) {
		App.self.voiceIO.getOperationTracker().queOperation(new Runnable() {
			@Override
			public void run() {
				Output.sayAndShow(ctx, toShow, toSay, false);
				fireOpes();
			}
		}, true);
	}
	
	    private static class QueuedOperation {
		   final Runnable op;
		   final boolean inGui;
		   public QueuedOperation(Runnable op,boolean inGui) {
			   this.op=op;
			   this.inGui=inGui;
		   }
		}
		
		private Thread queOpThread=null;
		private List<QueuedOperation> opQue=new ArrayList<QueuedOperation>();

		public void queOperation(Runnable op, boolean inGui) {
			synchronized(opQue) {
				opQue.add(new QueuedOperation(op,inGui));
				if (queOpThread==null||!queOpThread.isAlive()) {
					queOpThread=new Thread("queOperation") {
						@Override
						public void run() {
							for (;;) try {
								synchronized(opQue) {
								   if (opQue.isEmpty()) break;
								}
								App.self.voiceIO.getOperationTracker().acquire("queOperation");
								QueuedOperation qop=opQue.remove(0);
								VR vr=VR.get();
								if (vr!=null) vr.abort();
								if (qop.inGui)
									Utils.runInMainUiThread(qop.op);
								else
									qop.op.run();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							queOpThread=null;
						}
					};
					queOpThread.start();
				}
			}
		}
	
	

	public OperationTracker(int permits, boolean f) {
		super(permits, f);
	}

	public final static String TAG = OperationTracker.class.getSimpleName();

	public void acquire(String note) throws InterruptedException {
		Log.d(TAG, "acquire " + ((note == null) ? "" : note));
/*
		try {
			super.acquire();
			Log.d(TAG, "operationTracker.acquired");
		} catch (InterruptedException e) {
			Log.d(TAG, "acquire !!! interrupted");
			throw e;
		}
		*/
	}

	@Override
	public boolean tryAcquire() {
		Log.d(TAG, "tryAcquire");
		return true;//super.tryAcquire();
	}

	@Override
	public void release() {
		Log.d(TAG, "relsease");
		/*
		if (this.availablePermits() < 1) {
			Log.d(TAG, "actually release");
			super.release();
		}
		*/
	}

}