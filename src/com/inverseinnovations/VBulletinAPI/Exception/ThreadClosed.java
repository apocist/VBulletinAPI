package com.inverseinnovations.VBulletinAPI.Exception;

public class ThreadClosed extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public ThreadClosed(){
		super("ThreadClosed Exception - vBulletin API unable to process request due to the thread being closed");
	}
}