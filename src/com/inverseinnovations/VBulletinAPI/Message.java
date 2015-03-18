package com.inverseinnovations.VBulletinAPI;

public class Message{
	protected int pmid;
	protected String sendtime;//TODO is this a String?(check)
	protected String statusicon;
	protected String title;
	protected int userid;
	protected String username;
	protected String message = "";
	
	public int getPmid() {
		return pmid;
	}
	public String getSendTime() {
		return sendtime;
	}
	public String getStatusIcon() {
		return statusicon;
	}
	public String getTitle() {
		return title;
	}
	public int getUserId() {
		return userid;
	}
	public String getUsername() {
		return username;
	}
	public String getMessage() {
		return message;
	}

}
