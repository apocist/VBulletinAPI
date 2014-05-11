package com.inverseinnovations.VBulletinAPI.Exception;

public class BadCredentials extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public BadCredentials(){
		super("BadCredentials Exception - vBulletin API unable to login due to incorrect username/password.");
	}
}