package com.magnifis.parking.cmd.etc;

import java.lang.reflect.Constructor;
import java.util.Vector;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;

import com.magnifis.parking.Abortable;
import com.magnifis.parking.Log;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.UnderstandingProcessorBase;
import com.magnifis.parking.VoiceIO;
import com.magnifis.parking.cmd.i.ActivityResultHandler;
import com.magnifis.parking.cmd.i.ClientStateInformer;
import com.magnifis.parking.cmd.i.IIntentHandler;
import com.magnifis.parking.cmd.i.LocalCommandHandler;
import com.magnifis.parking.cmd.i.MagReplyHandler;
import com.magnifis.parking.cmd.i.OnBeforeListeningHandler;
import com.magnifis.parking.cmd.i.OnListeningAbortedHandler;
import com.magnifis.parking.cmd.i.OnOrientationHandler;
import com.magnifis.parking.cmd.i.OnResumeHandler;
import com.magnifis.parking.cmd.i.IPartialVRResultHandler;
import com.magnifis.parking.model.Understanding;
import com.magnifis.parking.suzie.SuziePopup;

public abstract class CmdHandlerHolder {
	
	final static String TAG=CmdHandlerHolder.class.getName();
	
	public static View getTopView() {
		if (SuziePopup.get() != null && SuziePopup.get().isVisible())
			return SuziePopup.get().getTopView();
		if (MainActivity.get() != null)
			return MainActivity.get().getRootView();
		
		return null;
	}
	
	public static void onNetworkCommunicationError() {
		if (SuziePopup.get() != null)
			SuziePopup.get().onNetworkCommunicationError();
		
		if (MainActivity.get() != null)
			MainActivity.get().onNetworkCommunicationError();
	}
	
	static private Object commandHandler = null;

	static public Object getCommandHandler() {
		return commandHandler;
	}
	
	/*
	@Override
	public IPartialVRResultHandler getPartialVRResultHandler() {
		return (commandHandler!=null&&commandHandler instanceof IPartialVRResultHandler)?(IPartialVRResultHandler)commandHandler:null;
	}*/
	
	
	public static Abortable getAbortableCommandHandler() {
		return (commandHandler!=null &&
				commandHandler instanceof Abortable) ? 
						(Abortable)commandHandler:null;
	}
	
	public static OnBeforeListeningHandler getOnBeforeListeningHandler() {
		return (commandHandler != null && 
				commandHandler instanceof OnBeforeListeningHandler) ? 
						(OnBeforeListeningHandler) commandHandler
				: null;
	}
	
	public static OnResumeHandler getOnResumeHandler() {
		return (commandHandler != null && 
				commandHandler instanceof OnResumeHandler) ? 
						(OnResumeHandler) commandHandler
				: null;
	}
	
	public static OnOrientationHandler getOnOrientationHandler() {
		return (commandHandler != null && 
				commandHandler instanceof OnOrientationHandler) ? 
						(OnOrientationHandler) commandHandler
				: null;
	}
	
	public static ClientStateInformer getClientStateInformer() {
		ClientStateInformer si = (commandHandler != null && 
				commandHandler instanceof ClientStateInformer) ? 
						(ClientStateInformer) commandHandler
				: null;
		if (si != null)
			return si;
		
		UnderstandingProcessorBase up=VoiceIO.getCurrentUP();
		
		return (up==null||!(up instanceof ClientStateInformer))?null:((ClientStateInformer)up);
	}
	
	public static ActivityResultHandler getActivityResultHandler() {
		return (commandHandler != null && 
				commandHandler instanceof ActivityResultHandler) ? 
						(ActivityResultHandler) commandHandler
				: null;

	}
	
	public static LocalCommandHandler getLocalCommandHandler() {
		return (commandHandler != null && 
				commandHandler instanceof LocalCommandHandler) ? 
						(LocalCommandHandler) commandHandler
				: null;
	}
	
	public static MagReplyHandler getMagReplyHandler() {
		return (commandHandler != null && 
				commandHandler instanceof MagReplyHandler) ? 
						(MagReplyHandler) commandHandler
				: null;
	}
	
	public static OnListeningAbortedHandler getOnListeningAbortedHandler() {
		return (commandHandler != null && 
				commandHandler instanceof OnListeningAbortedHandler) ? 
						(OnListeningAbortedHandler) commandHandler
				: null;
	}

	private static Vector otherHandlers = new Vector();
	
	public static void pushCommandHandler(Abortable h) {
		otherHandlers.add(commandHandler);
		commandHandler = h;
	}
	
	
	public static void setRelevantHandlerIfNeed(
		Object holder, Vector otherHandlers, Understanding u,
		Context ctx
	) {
		Class c = u.getCommandHandlerFactory();
		boolean otherHandler = false;
		
		//Object commandHandler =holder.getCommandHandler();
		
		if (c != null
				&& (commandHandler == null || (otherHandler = commandHandler
						.getClass() != c))) {
			try {
				Constructor cr = null;
				if (ctx instanceof MainActivity) try {
					cr = c.getConstructor(MainActivity.class);
				} catch (Throwable t0) {
				}
				if (cr==null&&(ctx instanceof Activity)) try {
					cr = c.getConstructor(Activity.class);
				} catch (Throwable t0) {
				}
				if (cr==null&&(ctx instanceof Service)) try {
					cr = c.getConstructor(Service.class);
				} catch (Throwable t0) {
				}
				if (cr==null) try {
					cr = c.getConstructor(Context.class);
				} catch (Throwable t0) {
				}
				if (otherHandler)
					otherHandlers.add(commandHandler);
				setCommandHandler((Abortable) ((cr == null) ? (c.newInstance())
						: cr.newInstance(ctx)));
			} catch (Throwable t) {
			}
		}
		
	}
	
	
	public static void setRelevantHandlerIfNeed(Understanding u, Context ctx) {
		setRelevantHandlerIfNeed(null,otherHandlers,u,ctx);
	}
	
	public static void removeCommandHandler(Abortable _commandHandler) {
		if (_commandHandler == commandHandler)
			commandHandler = otherHandlers.isEmpty() ? null
					: otherHandlers.remove(otherHandlers.size() - 1);
	}
	
	public static void setCommandHandler(Abortable _commandHandler) {
		commandHandler = _commandHandler;
	}
	
	public static IIntentHandler getIntentHandler() {
		return (commandHandler != null && 
				commandHandler instanceof IIntentHandler) ? 
						(IIntentHandler) commandHandler
				: null;
	}


}
