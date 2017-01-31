package com.magnifis.parking.db;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.magnifis.parking.utils.Utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class AndroidContentProvider {
	final static String TAG=AndroidContentProvider.class.getSimpleName();

	public static <T> List<T> get(Uri uri, Class<T> cls, String columns[], String selection, String orderBy) {
         return get(uri, cls,columns, selection, null, orderBy);
	}
	
	public static <T> List<T> get(Uri uri, Class<T> cls, String columns[], String selection,String[] selectionArgs, String orderBy) {
		Map<String,Field> flds = SqliteDB.getTableFields(cls);
		if (!Utils.isEmpty(flds)) try {
			ContentResolver cr = App.self.getContentResolver();
			Cursor c=cr.query(
				  uri,
				  columns,
	              selection, selectionArgs, orderBy
	        );
			if (c!=null) try {
				Log.d(TAG,Utils.dump(c).toString());
                return SqliteDB.convert(c, cls, flds);
			} finally {
			  c.close();
			}
		} catch(Throwable t) {
			Log.e(TAG, "error ",t);
		}
		return null;
	}

}
