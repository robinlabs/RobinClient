package com.magnifis.parking.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import android.os.Environment;

import static com.magnifis.parking.utils.Utils.*;

public class MountTableEntry {
    protected String device=null, mountPoint=null, fsType=null, flags=null;

    public boolean isRW() {
    	if (!isEmpty(flags))
    	   for (String s: flags.split(",")) if (s.equalsIgnoreCase("rw")) return true;
   
    	return false;
    }


	public String getFsType() {
		return fsType;
	}

	public void setFsType(String fsType) {
		this.fsType = fsType;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}

	public String getMountPoint() {
		return mountPoint;
	}

	public void setMountPoint(String mount_point) {
		this.mountPoint = mount_point;
	}
	
	
	private static Comparator theComparator=new Comparator() {
		@Override
		public int compare(Object lhs, Object rhs) {
			MountTableEntry mte=(MountTableEntry)lhs;
			File f=(File)rhs;
			return mte.mountPoint.compareTo(f.getAbsolutePath());
		}		
	};
	
	public static File[] mountedSdcards() {
		File sdcs[]=null;
		/*
        try {
            IMountService mountService = IMountService.Stub.asInterface(ServiceManager
                    .getService("mount"));
            Parcelable[] volumes = mountService.getVolumeList();
          
        } catch (Exception e) {
            Log.e(TAG, "couldn't talk to MountService", e);
        }
        */
		final MountTableEntry mt[]=readMountTable();
		if (!isEmpty(mt)) {
			File mnt=new File("/mnt/");
			if (mnt.exists()) {
				sdcs=mnt.listFiles(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if  (f.isDirectory()) {
							String nm=f.getName();
							if (!isEmpty(nm)&&nm.contains("sdc")) {
								MountTableEntry mte=Utils.find(mt, f, theComparator);
								if (mte!=null/*&&mte.isRW()*/) return true;
							}
						}
						return false;
					}});
			}
		}
		
		if (isEmpty(sdcs)) {
			File f=Environment.getExternalStorageDirectory();
			if (f!=null&&Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			   return new File[] {f}; 
		}
		return sdcs;
	}
	
	private static MountTableEntry [] example={};
	private static File[] fExample={};

	public static MountTableEntry [] readMountTable() {
    	try {
			FileReader fr=new FileReader("/proc/mounts");
			if (fr!=null) try {
			  BufferedReader br=new BufferedReader(fr);
			  ArrayList<MountTableEntry> mt=new ArrayList<MountTableEntry> ();
			  for (String s=null;(s=br.readLine())!=null;) if (!isEmpty(s)) {
				  String a[]=s.split("\\s+");
				  if (a.length>3) {
					  MountTableEntry mte=new MountTableEntry();
					  mte.setDevice(a[0]);
					  mte.setMountPoint(a[1]);
					  mte.setFsType(a[2]);
					  mte.setFlags(a[3]);
					  mt.add(mte);
				  }
			  }
			  return (MountTableEntry[]) mt.toArray(example);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			  try {
				fr.close();
			  } catch (IOException e) {}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return null;
    }
}
