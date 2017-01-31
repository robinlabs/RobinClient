package com.magnifis.parking.utils;

import java.io.File;
import java.util.Calendar;

import com.magnifis.parking.Consts;

public class DiskCacheClient {
	static private DiskCache _dataCache=null;
	
	public static  DiskCache getDataCache() {
		synchronized(Consts.DATA_CACHE) {
			if (_dataCache==null) {
			   _dataCache=DiskCache.get(new File(Consts.DATA_CACHE));
			   Calendar cal=Calendar.getInstance();
			   cal.setTimeInMillis(System.currentTimeMillis());
		       cal.add(Calendar.DAY_OF_YEAR, -10); // 10 days ago
		       _dataCache.removeOutdated(cal.getTimeInMillis());
			}
			return _dataCache;
		}
	}	
}
