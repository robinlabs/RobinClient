package com.magnifis.parking;

public abstract class Advance implements Runnable {
	
	protected int timeout=5000;//10000;
	protected String prompt=null;

	public Advance() {}
	public Advance(String prompt) {
		this.prompt=prompt;
	}
	
	public Advance(String prompt, int timeout) {
		this.prompt=prompt;
		this.timeout=timeout;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public String getPrompt() {
		return prompt;
	}
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}
	

}
