package com.magnifis.parking.db;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import com.magnifis.parking.Xml.ML;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import com.magnifis.parking.Log;
import com.magnifis.parking.utils.Utils;
import com.robinlabs.utils.BaseUtils;

public class SqliteDB {
	
	final static String TAG=SqliteDB.class.getCanonicalName();
	
	private  String databaseName = null;
	
	protected int getVersion() {
		return 1;
	}

	protected Context context;
	public SQLiteDatabase db;

	private static WeakHashMap<String,SqliteDB> instances=new WeakHashMap<String,SqliteDB>();
	
    public final Object lock=new Object();
    
	public static <T extends SqliteDB> T 
	   getInstance(Context context,String databaseName,Class<T> cls) 
	{
		synchronized(SqliteDB.class) {
			T db=(T) instances.get(databaseName);
			if (db==null) {
				try {
					Constructor<T> c=cls.getDeclaredConstructor(Context.class,String.class);
					c.setAccessible(true);
					db=c.newInstance(context,databaseName);
				} catch (Throwable e) {
					Log.e(TAG, " -- ",e);
				}
				//db=new SqliteDB(packageName,databaseName);
				instances.put(databaseName, db);
			}
			return db;
		}
	}

	public static SqliteDB getInstance(Context context,String databaseName) {
		return getInstance(context,databaseName,SqliteDB.class);
	}
	
	protected SQLiteOpenHelper getOpenHelper() {
		return new OpenHelper();
	}
	
	public long lastNumberOfChanges() {
		SQLiteStatement selectChangesStmt=db.compileStatement("SELECT CHANGES()");
		try {
		    return selectChangesStmt.simpleQueryForLong();
		} finally {
			selectChangesStmt.close();
		}
	}
	
	protected <O  extends SQLiteOpenHelper> SqliteDB(Context context,String databaseName) {
		this.context = context;
		SQLiteOpenHelper openHelper=getOpenHelper();
		this.db = openHelper.getWritableDatabase();
		db.setLockingEnabled(true);

	}
	
	
	  @Retention(value=RetentionPolicy.RUNTIME)
	  public static @interface DB {
	    public String value() default "";
	    public String table() default "";
	    public String primaryKey() default "id";
	    public boolean isPrimaryKey() default false;
	    public boolean autoincrement() default false;
	    public boolean notnull()  default false;
	    public String  defaultValue() default "";
	  };
	  
	 @SuppressWarnings("rawtypes")
	 private static HashMap<Class,String> typesMap= new HashMap<Class,String>() {
		  {
			  put(Integer.class,"INTEGER");
			  put(int.class,"INTEGER");
			  put(Long.class,"INTEGER");
			  put(long.class,"INTEGER");
			  put(Short.class,"INTEGER");
			  put(short.class,"INTEGER");
			  put(String.class,"TEXT");
			  put(Boolean.class,"BOOLEAN");
			  put(boolean.class,"BOOLEAN");
			  put(java.util.Date.class,"INTEGER");
			  put(byte [].class,"BLOB");
		  }  
	  };
	  
	  /*
	public  <T> T get(Object primaryKey, T record) {
      Class cls=record.getClass();
	  String tbl=getTableName(cls);
	  
	  
	  
      return record;
	}*/
	  
	static public String getTableFieldName(Field fl) {
        DB an=fl.getAnnotation(DB.class);
        return (an.value()=="")?fl.getName():an.value();	
	}
	
	static public Map<String,Field> getTableFields(Class tbl) {
		List<Field> lst=Utils.getAnnotatedFields(tbl, DB.class, true);
		if (!Utils.isEmpty(lst)) {
			Map<String,Field> res=new HashMap<String,Field>();
			for (Field f:lst) res.put(getTableFieldName(f), f);
			return res;
		}
		return null;
	}
	
	public <T> boolean doesExist(Object key, Class<T> cls) {
	   return doesExist(getPkName(cls),key,cls);
	}
	
	private final static String no_such_table="no such table";
	private final static String duplicate_column_name="duplicate column name";
	
	public <T> boolean doesExist(String keyField, Object key, Class<T> cls) {
		try {
			String tbl=getTableName(cls);
			Cursor cr=db.query(tbl, new String[] {keyField}, keyField+"=?", new String[] {key.toString()}, 
					null, null, null);
			return cr.getCount()>0;
		} catch(SQLiteException ex) {
			if (!ex.getMessage().contains(no_such_table)) {
				Log.e(TAG, " -- ", ex);
			}
		}
		return false;
	}
	
	public <T> List<T>  getAll(Class<T> cls, String ...orderBy) {
		return getBy(null,null,cls,orderBy);
	}
	
	public <T> T getBy(Object key, Class<T> cls,String ...orderBy) {
		List<T> res=getBy(getPkName(cls),key,"=",cls,orderBy);
		return  Utils.isEmpty(res)?null:res.get(0);
	}
	
	public <T> List<T> getBy(String keyField, Object key, Class<T> cls, String ...orderBy) {
		return getBy(keyField, key, "=" , cls, orderBy);
	}
	
	public <T> T getOneBy(String keyField, Object key, Class<T> cls,String ...orderBy) {
		List<T> res=getBy(keyField, key, cls, orderBy);
		return  Utils.isEmpty(res)?null:res.get(0);
	}	
	
	
	public <T> List<T> getBy(Object vals[], Class<T> cls, String ...orderBy) {
		return getBy(getPkName(cls), vals, cls, orderBy);
	}
	
	public <T> List<T> getBy(String keyField, Object vals[], Class<T> cls, String ...orderBy) {

		String args[]=Utils.isEmpty(vals)?null:new String[vals.length];
		StringBuilder cond=new StringBuilder();
		
		if (keyField!=null&&!Utils.isEmpty(vals)) {
			cond.append(keyField);
			cond.append(" in (");
			for (int i=0;i<vals.length; i++) { 
				if (i!=0) cond.append(',');
				cond.append('?'); 
				args[i]=vals[i].toString();
			}
			cond.append(')'); 
		}
		
	    return getWhere(cond.length()==0?null:cond.toString(), args, cls, orderBy);
		
   } 	
	
	public <T> List<T> getBy(String keyField, Object key, String op , Class<T> cls, String ...orderBy) {

			String args[]=null, cond=null;
			
			if (keyField!=null) {
				cond=keyField+op+"?";//"= ?"; // remove trailing ","
			    args=new String [1];
			    args[0]=(key==null)?null:key.toString();
			}
			
		    return getWhere(cond, args, cls, orderBy);
			
	} 
	
	public static <T> List<T> convert(Cursor c, Class<T> cls) {
      return SqliteDB.convert(c, cls, SqliteDB.getTableFields(cls));
	}
	
	public static <T> List<T> convert(Cursor cr, Class<T> cls, Map<String,Field> fields) {
		if (cr.moveToFirst()) {
			ArrayList<T> res=new ArrayList<T>();
			do {
				try {
					T obj=cls.newInstance();
					res.add(obj);
					for (Map.Entry<String,Field> en:fields.entrySet()) try {
						Field fl=en.getValue();
						Class t=fl.getType();
						int ci=cr.getColumnIndex(en.getKey());
						if (ci>=0)  {
							if (t==String.class) {
								fl.set(obj, cr.getString(ci));
							} else 
								if (t==Integer.class||t==int.class) {
									fl.set(obj, cr.getInt(ci));
								} else
									if (t==Long.class||t==long.class) {
										fl.set(obj, cr.getLong(ci));
									} else
										if (t==boolean.class) {
											if (!cr.isNull(ci)) fl.setBoolean(obj, cr.getLong(ci)!=0);
										} else if (t==Boolean.class) {
											fl.set(obj, cr.isNull(ci)?null:Boolean.valueOf(cr.getLong(ci)!=0));
										} else if (t==byte [].class) {
											fl.set(obj, cr.getBlob(ci));
										} else if (/*Utils.isSubclassOf(t, java.util.Date.class)*/t==java.util.Date.class) {
											fl.set(obj, cr.isNull(ci)?null:new java.util.Date(cr.getLong(ci)));
										} else  {
											fl.set(obj, Utils.bytes2obj(cr.getBlob(ci)));
										} 						
						}
					} catch (Throwable e0) {
						Log.e(TAG, " -- ", e0);
					}
				} catch (Throwable e) {
					Log.e(TAG, " -- ", e);
				}
			} while (cr.moveToNext());
			return res;
		} 
		return null;
	}
	
	public <T> List<T> getWhere(String condition, String args[], Class<T> cls, String ...orderBy) {
		return getWhere(getTableName(cls), condition, args, cls, orderBy);
	}
	  
	public <T> List<T> getWhere(String tbl, String condition, String args[], Class<T> cls, String ...orderBy) {
	   List<Class> clss=Utils.getSuperClasses(cls);
	   String stmt="select ";
	   
		HashMap<String,Field> fields=new HashMap<String,Field>();
		for (Class cl:clss) for (Field fl:cl.getDeclaredFields()) {
		      fl.setAccessible(true);
		      if (fl.isAnnotationPresent(DB.class)) {
		      	Class flType=fl.getType();
		        DB an=fl.getAnnotation(DB.class);
		        String fldn=getTableFieldName(fl);
                stmt+=fldn+",";
                fields.put(fldn, fl);
		      }
		}	   
		
		stmt=stmt.substring(0,  stmt.length()-1)+" from "+tbl;
		
		if (condition!=null) {
			stmt+=" where "+condition;
		}
		
		if (!BaseUtils.isEmpty(orderBy)) {
			stmt+=" order by ";
			for (int i=0;i<orderBy.length;i++) {
			  if (i>0) stmt+=',';
			  stmt+=orderBy[i];
			}
		}
		
		Log.d(getClass().getCanonicalName(), stmt);
		
		Cursor cr;
		
		try {
	      cr=db.rawQuery(stmt, args);
		} catch(SQLiteException ex) {
			if (!ex.getMessage().contains(no_such_table)) {
				Log.e(TAG, " -- ", ex);
			}
			return null;
		}
		
		if (cr!=null) try {
			return convert(cr,cls,fields);
		} finally {
		  cr.close();
		}
	
	    return null;
		
	} 
	
	static public String getPkName(Class cls) {
	   DB clsan=(DB) cls.getAnnotation(DB.class);
	   if (clsan.primaryKey()!="") return clsan.primaryKey();
		
		
	   List<Class> clss=Utils.getSuperClasses(cls);
	   
	   HashMap<String,Field> fields=new HashMap<String,Field>();
	   for (Class cl:clss) for (Field fl:cl.getDeclaredFields()) {
		      fl.setAccessible(true);
		      if (fl.isAnnotationPresent(DB.class)) {	   
		    	 DB an=fl.getAnnotation(DB.class);
		    	 if (an.isPrimaryKey()) {
		    		return an.value()!=null?an.value():fl.getName();
		    	 }
		      }
	   };
	   return null;
	}
	
	static public String getTableName(Class cls) {
		String tbl=cls.getSimpleName();

		if (cls.isAnnotationPresent(DB.class)) {
			DB an=(DB) cls.getAnnotation(DB.class);
			String ann=(an.table()=="")?an.value():an.table();
			if (ann!="") tbl=ann;
		}
		return tbl;
	}
	
	public <T> int setBy(String key,Object keyVal,String fld,T fldVal,Class cls) {
		try {
			String tbl=getTableName(cls);
			ContentValues values=new ContentValues();
			putValue(values,fld,fldVal,fldVal.getClass()); // fix to handle null
			return db.update(tbl, values, key+"=?", new String[] {keyVal==null?null:keyVal.toString()});	
		} catch(Throwable t) {
		}
		return 0;
	}
	
	protected void putValue(ContentValues values,String df,Object val,Class fc) {
		if (!typesMap.containsKey(fc)) {
			// handle BLOB
			values.put(df, Utils.obj2bytes(val));
		} else if (fc==String.class) 
			values.put(df,(String)val);
		  else if (fc==Integer.class) 
			values.put(df,(Integer)val);
		  else if (fc==Long.class) 
		    values.put(df,(Long)val);
		  else if (fc==byte[].class)
			values.put(df,(byte[])val);	
	}
	
	public void updateTableStructure(Class cls) {
		String tbl=getTableName(cls);
		List<Class> clss=Utils.getSuperClasses(cls);
		
		for (Class cl:clss) for (Field fl:cl.getDeclaredFields()) {
		      fl.setAccessible(true);
		      if (fl.isAnnotationPresent(DB.class)) {
		      	 Class flType=fl.getType();
		         DB an=fl.getAnnotation(DB.class);
		         String fldn=getTableFieldName(fl);
		         if (!an.isPrimaryKey()) {
		        	// try to update the field
		        	 /*
		        	 String stmt="alter table "+tbl+" add column "+fldn+
		        	   " "+(typesMap.containsKey(flType)?typesMap.get(flType):"BLOB");
		        	 if (an.notnull()) stmt+=" not null default '"+
		        			 DatabaseUtils.sqlEscapeString(an.defaultValue())+
		        	 "'";
		        	 */
		        	 String stmt=completeColumnDeclaration(fldn,fl,an,"alter table "+tbl+" add column ")
		        			  .toString();
		        	 try {
		        		Log.d(TAG,stmt);
		        	    db.execSQL(stmt);
		        	 } catch(SQLiteException t) {
		        		String msg=t.getMessage();
		     			if (msg.contains(no_such_table)||
		     				msg.contains(duplicate_column_name)
		     			) {
		     				// do nothing;
		     			} else {
		    			   Log.e(TAG, " -- ", t);
		    			}     		 
		        	 }
		         }
		      }
		}	
		
	}
	
	public <T> void empty(Class<T> cls) {
		db.delete(getTableName(cls), null, null);
	}
	
	private static CharSequence completeColumnDeclaration(String fldn,Field fl, DB an, CharSequence stmt) {
	   StringBuilder sb=new StringBuilder(stmt);
	   sb.append(' ');
	   sb.append(fldn);
	   sb.append(' ');
	   Class flType=fl.getType();
	   sb.append((typesMap.containsKey(flType)?typesMap.get(flType):"BLOB"));
       if (an.notnull()) {
    	   sb.append(" NOT NULL");
           if (BaseUtils.isNumberType(fl)) {
        	   if (!BaseUtils.isEmpty(an.defaultValue())) {
        	     sb.append(" default ");
        	     sb.append(an.defaultValue());
        	   }
           } else if (!BaseUtils.isEmpty(an.defaultValue())) {
        	   sb.append(" default ");
 			   sb.append(DatabaseUtils.sqlEscapeString(an.defaultValue()));

           }
       }
       return sb;
	}
	
	@SuppressLint("NewApi") public long delete(Class cls, CharSequence tail, String ...selectionArgs) {
		String tbl=getTableName(cls);
		StringBuilder sb=new StringBuilder("delete from ");
		sb.append(tbl);
		if (!Utils.isEmpty(tail)) {
			sb.append(" where ");
			sb.append(tail);
		}
		
		SQLiteStatement st=null;
		try {
			if (Utils.isAndroid3orAbove) {
			  st=db.compileStatement(sb.toString());
			  if (!BaseUtils.isEmpty(selectionArgs)) st.bindAllArgsAsStrings(selectionArgs);
			  return st.executeUpdateDelete();
			} else {
			  db.execSQL(sb.toString(), selectionArgs);
			  return /*lastNumberOfChanges()*/0; // does not work properly
			}
		} catch(SQLiteException ex) {
			if (!ex.getMessage().contains(no_such_table)) {
				Log.e(TAG, " -- ", ex);
			}
		} finally {
		  if (st!=null) st.close();
		}
		return 0;		
	}
	
	
	
	@SuppressLint("NewApi") public long update(Class cls, CharSequence tail, String ...selectionArgs) {
		String tbl=getTableName(cls);
		StringBuilder sb=new StringBuilder("update ");
		sb.append(tbl);
		sb.append(" set ");
		sb.append(tail);
		
		Log.d(TAG, "update: "+sb);
		
		SQLiteStatement st=null;
		try {
			if (Utils.isAndroid3orAbove) {
			  st=db.compileStatement(sb.toString());
			  if (!BaseUtils.isEmpty(selectionArgs)) st.bindAllArgsAsStrings(selectionArgs);
			  return st.executeUpdateDelete();
			} else {
			  db.execSQL(sb.toString(), selectionArgs);
			  return /*lastNumberOfChanges()*/0; // does not work properly
			}
		} catch(SQLiteException ex) {
			if (!ex.getMessage().contains(no_such_table)) {
				Log.e(TAG, " -- ", ex);
			}
		} finally {
		  if (st!=null) st.close();
		}
		return 0;
	} 
	
	public <T> Integer save(T record, String ...dbfToUpdate) {
		return _insertOr("REPLACE",record,dbfToUpdate);
	}
	
	public <T> Integer insertOr(String operator,T record) {
	   return _insertOr(operator,record);
	}
	
	public <T> Integer insertOrIgnore(T record) {
		return insertOr("IGNORE",record);
	}
	
	private <T> Integer _insertOr(String operator,T _record, String ...dbfToUpdate) {
		Class cls=_record.getClass();
		boolean isArray=cls.isArray();
		if (isArray) cls=cls.getComponentType();
		
		boolean processWholeObject=dbfToUpdate==null||dbfToUpdate.length==0;
		
		String tbl=getTableName(cls);
		
		String stmt="create table if not exists "+tbl+" (";
		List<Class> clss=Utils.getSuperClasses(cls);
		
		boolean primaryKeyFound=false;
		
		LinkedHashMap<String,Field> fields=new LinkedHashMap<String,Field>();
		boolean first=true;
		for (Class cl:clss) for (Field fl:cl.getDeclaredFields()) {
		      fl.setAccessible(true);
		      if (fl.isAnnotationPresent(DB.class)) {
		      //	Class flType=fl.getType();
		        DB an=fl.getAnnotation(DB.class);
		        String fldn=getTableFieldName(fl);
		        if (processWholeObject) {
		        	/*
		        	stmt+=fldn+" "+(typesMap.containsKey(flType)?typesMap.get(flType):"BLOB");
		        	if (an.notnull()) stmt+=" NOT NULL default '"+
       			      DatabaseUtils.sqlEscapeString(an.defaultValue())+"'";
       			      */
		        	stmt=completeColumnDeclaration(fldn,fl,an,stmt).toString();
		        	if (an.isPrimaryKey()) {
		        		stmt+=" primary key"; 
		        		if (an.autoincrement()) stmt+=" autoincrement";
		        		primaryKeyFound=true;
		        	}
		        	stmt+=",";
		        }
                
                if (processWholeObject||Utils.contains(dbfToUpdate, fldn)) 
                  fields.put(fldn, fl);
		      }
		}
		
		String pkName=null; Method pkm=null; 
		
		if (primaryKeyFound) { 
			stmt=stmt.substring(0,  stmt.length()-1); // remove trailing ","
		} else {// generate primary key field
			try {
			  DB clsan=(DB) cls.getAnnotation(DB.class);
			  pkName=clsan.primaryKey();	
			  if (pkName.indexOf(',')>0) {
				 // composite key
				  stmt+="primary key ("+pkName+")";
			  } else {
			    pkm=cls.getMethod("getPrimaryKey");
			    Class rt=pkm.getReturnType();
			    if (processWholeObject) stmt+=pkName+" "+typesMap.containsKey(rt)+" primary key";
			    fields.put(pkName, null);
			  }
			} catch (Throwable  e) {
			  stmt=stmt.substring(0,  stmt.length()-1); // remove trailing ","
			  Log.e(this.getClass().getCanonicalName()," -- ",e);
			}	
		} 
		
		if (processWholeObject) {
			stmt+=")";
			Log.d(SqliteDB.class.getCanonicalName(), stmt);
			db.execSQL(stmt); // create table if not exists
		}
		//-------------------------------------------------------------------------------------
		
		// then do insert
		if (processWholeObject) {
			StringBuilder sb=new StringBuilder("INSERT OR "+operator+" INTO ");
			Object ar=isArray?_record:new Object[] {_record};
			Class acls=ar.getClass();
			int arSize=Array.getLength(ar);
			sb.append(tbl);
			sb.append(" (");
			
			int nValues=fields.size(),jj=0;
			for (Field fl:fields.values()) if (fl!=null) {
				  DB an=fl.getAnnotation(DB.class);
				  /*
				  if (an!=null&&an.isPrimaryKey()&&an.autoincrement()) {
					  // skip autoincrement pk
					  --nValues; continue;
				  }*/
				  if (jj>0) sb.append(",");
				  sb.append( getTableFieldName(fl) );
				  ++jj;
			}

		    Object values[]=new Object[nValues*arSize];

			sb.append(") ");

			int j=0;
			for (int z=0;z<arSize;z++) {
				if (z>0) sb.append(" union ");
				sb.append(" select ");
				Object record=Array.get(ar, z);
				/////////////////////////////////////////////////////////
				
				for (Entry<String,Field> fld:fields.entrySet()) {
					Field fl=fld.getValue();
					/*
					if (fl!=null) {
						DB an=fl.getAnnotation(DB.class);
						if (an!=null&&an.isPrimaryKey()&&an.autoincrement()) {
							// skip autoincrement pk
							continue;
						}
					}
					*/

					try {
						values[j]=(fl==null)?pkm.invoke(record):fl.get(record);
						if (fl!=null&&!typesMap.containsKey(fl.getType())) {
							// handle BLOB
							values[j]=Utils.obj2bytes(values[j]);
						}
					} catch (Throwable t) {
						Log.e(this.getClass().getCanonicalName(), " -- ",t);
					}
					j++;
				}
				for (int i=0;i<nValues;i++) { if (i>0) sb.append(","); sb.append("?");  }
				//sb.append(')');
    	    }
			Log.d(TAG, sb.toString());
			SQLiteStatement st=db.compileStatement(sb.toString());
			try {
			  for (int i=1;i<=values.length;i++) {
				Object v=values[i-1];
				if (v==null) st.bindNull(i); else 
				if (v instanceof byte[]) st.bindBlob(i, (byte[])v); else
				if (v instanceof Long) st.bindLong(i, (Long)v); else
				if (v instanceof Integer) st.bindLong(i, (Integer)v); else
				if (v instanceof Short) st.bindLong(i, (Short)v); else
				if (v instanceof Byte) st.bindLong(i, (Byte)v); else
			    if (v instanceof java.util.Date) st.bindLong(i, ((java.util.Date)v).getTime() ); else
				if (v instanceof Boolean) st.bindLong(i, ((Boolean)v?1:0)); else
					st.bindString(i, v.toString());
			  }
			  st.execute();
			} finally {
				st.close();
			}
				
			//Log.d(TAG, sb.toString());
			//db.execSQL(sb.toString(),values);
		} else {
			ContentValues values=new ContentValues();
			
			for (String df:dbfToUpdate) {
				try {
					Field fl=fields.get(df);
					Class fc=fl.getType();
					Object val=(fl==null)?pkm.invoke(_record):fl.get(_record);
					if (fl!=null&&!typesMap.containsKey(fc)) {
						// handle BLOB
						values.put(df, Utils.obj2bytes(val));
					} else if (fc==String.class) 
						values.put(df,(String)val);
					  else if (fc==Integer.class||fc==int.class) 
						values.put(df,(Integer)val);
					  else if (fc==Long.class||fc==long.class) 
					    values.put(df,(Long)val);
					  else if (fc==Boolean.class||fc==boolean.class) 
						values.put(df,((Boolean)val));
					  else if (fc==java.util.Date.class) 
						values.put(df,((Date)val).getTime());
					  else if (fc==byte[].class)
						values.put(df,(byte[])val); 
					
				} catch (Throwable t) {
					Log.e(this.getClass().getCanonicalName(), " -- ",t);
				}
			}
			
			String pkv=null;
			try {
				if (pkm!=null) {
					Object o=pkm.invoke(_record);
					if (o!=null) pkv=o.toString();
				} else {
					Field fl=fields.get(pkName);
					Object o=fl.get(_record);
					if (o!=null) pkv=o.toString();
				}
			} catch(Throwable t) {
                Log.e(TAG, " -- ",t);
			}
			
            return db.update(tbl, values, pkName+"=?", new String[] {pkv});			
		}
		
        return null;
	
	}
	
/*
	public void deleteAll() {
		this.db.
		
		this.db.delete(tableName, null, null);
	}
*/
	/*
	public List<String> selectAll() {
		List<String> list = new ArrayList<String>();
		Cursor cursor = this.db.query(tableName, new String[] { "name" },
				null, null, null, null, "name desc");
		if (cursor.moveToFirst()) {
			do {
				list.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		return list;
	}
	*/

	protected class OpenHelper extends SQLiteOpenHelper {

		OpenHelper() {
			super(context, databaseName, null, getVersion());
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			db.execSQL("CREATE TABLE " + tableName + 
				"(id STRING PRIMARY KEY, PurchaseInfo BLOB)");
				*/
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			/*
			Log.w("Example", "Upgrading database, this will drop tables and recreate.");
			db.execSQL("DROP TABLE IF EXISTS " + tableName);
			onCreate(db);
			*/
		}
	}
}
