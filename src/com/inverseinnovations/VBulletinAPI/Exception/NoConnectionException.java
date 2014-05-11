package com.inverseinnovations.VBulletinAPI.Exception;

public class NoConnectionException extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public NoConnectionException(){
		super("NoConnectionException - vBulletin API is not connected to any forum, or has been disconnected");
	}
}