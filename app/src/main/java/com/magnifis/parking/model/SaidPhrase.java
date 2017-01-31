package com.magnifis.parking.model;

import com.magnifis.parking.App;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.db.SqliteDB.DB;
import com.magnifis.parking.utils.Utils;

@DB(table="said_phrase", primaryKey="phrase_id")
public class SaidPhrase {

	@DB(value="phrase_id", isPrimaryKey=true)
	protected int phraseId;

	public int getPhraseId() {
		return phraseId;
	}

	public SaidPhrase setPhraseId(int phraseId) {
		this.phraseId = phraseId;
		return this;
	}
	
    public static void mark(int id) {
       RobinDB rdb=RobinDB.getInstance(App.self);
       rdb.insertOrIgnore(new SaidPhrase().setPhraseId(id));
    }	
    
    public static boolean test(int id) {
    	RobinDB rdb=RobinDB.getInstance(App.self);
    	try  {
    	  return rdb.getBy(id, SaidPhrase.class)!=null;
    	} catch(Throwable t) {}
    	return false;
    }
    
    public static boolean test(int  ids[]) {
    	if (!Utils.isEmpty(ids)) {
    	RobinDB rdb=RobinDB.getInstance(App.self);
    	try  {
    	  StringBuilder sb=new StringBuilder("phrase_id in (");
    	  sb.append(ids[0]);
    	  for (int i=1;i<ids.length;i++) {
    		  sb.append(',');
    		  sb.append(ids[i]);
    	  }
    	  sb.append(')');
    	  return rdb.getWhere(sb.toString(),null, SaidPhrase.class)!=null;
    	} catch(Throwable t) {}
    	}
    	return false;
    }

}
