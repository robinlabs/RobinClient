package com.magnifis.parking.db;

import com.magnifis.parking.Log;
import com.magnifis.parking.model.CalleeAssociation;
import com.magnifis.parking.model.DlStat;
import com.magnifis.parking.model.LearnedAnswer;
import com.magnifis.parking.model.PushAd;
import com.magnifis.parking.model.RobinProps;
import com.magnifis.parking.model.SaidPhrase;
import com.magnifis.parking.utils.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class RobinDB extends SqliteDB {
	
	private final static int DB_VERSION=4;
	final static private String DATABASE_PATH=Environment.getExternalStorageDirectory()+
			  "/.MagnifisRobin/robin_db.db";
	
	public <O extends SQLiteOpenHelper> RobinDB(Context context,
			String databaseName) {
		super(context, databaseName);

	}
	
	@Override
	protected int getVersion() {
		return DB_VERSION;
	}
	
	@Override
	protected SQLiteOpenHelper getOpenHelper() {
		
		
		return new SQLiteOpenHelper(context, DATABASE_PATH, null, DB_VERSION) {

			@Override
			public void onCreate(SQLiteDatabase db) {
				Utils.createFolderForFile(DATABASE_PATH);
				// TODO Auto-generated method stub
				
			}
			
			

			@Override
			public void onOpen(SQLiteDatabase db) {
				super.onOpen(db);
				Log.d(TAG,"onOpen");
				RobinDB.this.db=db; // Always upgrade the table structures
				RobinDB.this.updateTableStructure(RobinProps.class);
				RobinDB.this.updateTableStructure(LearnedAnswer.class);
				RobinDB.this.updateTableStructure(CalleeAssociation.class);
				RobinDB.this.updateTableStructure(PushAd.class);
				RobinDB.this.updateTableStructure(DlStat.class);
				RobinDB.this.updateTableStructure(SaidPhrase.class);
			}



			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				Log.d(TAG,"onUpgrade");
				/*
				RobinDB.this.db=db;
				RobinDB.this.updateTableStructure(LearnedAnswer.class);
				RobinDB.this.updateTableStructure(CalleeAssociation.class);
				*/
			}
	
	    };
	}
	
	public static RobinDB getInstance(Context context) {
		return getInstance(context,DATABASE_PATH,RobinDB.class);
	} 			

}
