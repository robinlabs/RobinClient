package com.magnifis.parking.model;

import com.magnifis.parking.db.SqliteDB.DB;

@DB(table="answers", primaryKey="question")
public class LearnedAnswer {

	@DB(isPrimaryKey=true)
	protected String question=null;
	
	@DB
	protected String answer=null;

	@DB
	protected int add_say=1;

	public int getSay() {
		return add_say;
	}

	public void setSay(int add_say) {
		this.add_say = add_say;
	}

	public String getQuestion() {
		return question;
	}

	public LearnedAnswer setQuestion(String id) {
		this.question = id;
		return this;
	}

	public String getAnswer() {
		return answer;
	}

	public LearnedAnswer setAnswer(String definition) {
		this.answer = definition;
		return this;
	}
	


}
