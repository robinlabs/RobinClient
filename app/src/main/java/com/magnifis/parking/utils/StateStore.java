package com.magnifis.parking.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.magnifis.parking.App;
import com.magnifis.parking.Log;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Parcel;

public class StateStore<T extends Serializable> {
	
	final public static String TAG= StateStore.class.getSimpleName();

	final private String fname;
	
	public StateStore(String name) {
		this.fname=name+".serializable";
	}
	
	public T get() {
		File f=new File (App.self.getFilesDir(),fname);
		synchronized(this) {
           if (f.exists()) {
        	 try {
				FileInputStream fis=new FileInputStream(f);
				if (fis!=null) try {
	               ObjectInputStream ois=new ObjectInputStream(fis);
	               return (T)ois.readObject();
				} finally {
				  fis.close();
				}
			 } catch (Throwable e) {
				e.printStackTrace();
			 }
           }
		}
		return null;
	}
	
	public void put(T v) {
	  File f=new File (App.self.getFilesDir(),fname);
	  synchronized(this) {
		  try {
			  if (f.exists()) f.delete();
			  FileOutputStream ofs=new FileOutputStream(f);
			  if (ofs!=null) try {
				  ObjectOutputStream oos=new ObjectOutputStream(ofs);
				  oos.writeObject(v);
                  oos.flush();
			  } finally {
				ofs.flush();
				ofs.close();
			  }
		  } catch (Throwable e) {
			  e.printStackTrace();
		  }
	  }
	}
	
}
