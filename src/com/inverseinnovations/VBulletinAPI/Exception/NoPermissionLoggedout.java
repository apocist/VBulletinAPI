package com.inverseinnovations.VBulletinAPI.Exception;

public class NoPermissionLoggedout extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public NoPermissionLoggedout(){
		super("NoPermissionLoggedout Exception - vBulletin API unable to process request without being logged in first");
	}
}