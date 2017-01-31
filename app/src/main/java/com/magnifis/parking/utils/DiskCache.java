package com.magnifis.parking.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Date;

import android.net.Uri;

public class DiskCache {
   final protected File dir;
   
   private static WeakReference<DiskCache> selfWr;
   
   public static DiskCache get(File dir) {
	  synchronized(DiskCache.class) {
		 if (selfWr!=null) {
		   DiskCache dc=selfWr.get();
		   if (dc!=null) return dc;
		 }
		 return new DiskCache(dir);
	  }
   }
     
   public DiskCache(File dir) {
	 selfWr=new WeakReference<DiskCache>(this);
	 this.dir=dir;
	 dir.mkdirs();
   } 
	
   public long containsKey(String key) {
	  File f=new File(dir,key);
	  if (f.exists()) return f.lastModified();
	  return 0;
   }
   
   public File getFile(String key) {
	  return new File(dir,key);
   }
   
   public void drop(String key) {
	   getFile(key).delete(); 
   }
   
   public FileOutputStream append(String key) {
	   File f=getFile(key);
	   try {
		   return new FileOutputStream(f,true);
	   } catch (FileNotFoundException e) {
		   e.printStackTrace();
	   }
	   return null;
   }
   
   public void put(String key, byte data[]) {
	  File f=getFile(key);
	  try {
	    FileOutputStream fos=new FileOutputStream(f);
	    fos.write(data);
	    fos.close();
	    if (f.length()<data.length) f.delete();
	  } catch(Throwable t) {
		if (f.exists()) f.delete();
	  }
   }
   
   public void touch(String key) {
	 File f=new File(dir,key);
     if (f.exists())
    	f.setLastModified(System.currentTimeMillis());
   }
   
   public byte[] get(String key) {
      File f=getFile(key);
	  if (f.exists()) try {
		FileInputStream fis=new FileInputStream(f);
		byte b[]=new byte[(int) f.length()];
		if (fis.read(b)<b.length) return null;
	  } catch (Throwable t) {
	  }
 	  return null;  
   }
   
   public Uri getUri(String key) {
	   File f=getFile(key);
	   if (f.exists()) try {
		   return Uri.parse("file://"+f.getCanonicalPath());
	   } catch (Throwable t) {
	   }
	   return null;  	   
   }

   
   public FileInputStream getStream(String key) {
	   File f=getFile(key);
	   if (f.exists()) try {
		   return new FileInputStream(f);
	   } catch (Throwable t) {
	   }
	   return null;  	   
   }
   
   public void removeOutdated(long tm) {
	 if (dir.exists()) {
		 Date dt=new Date(tm);
		 File fs[]=dir.listFiles();

		 if (fs!=null) for (File f:fs) 
			 if (new Date(f.lastModified()).before(dt)) f.delete();
	 }
   }
}
