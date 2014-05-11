package com.inverseinnovations.VBulletinAPI.Exception;

public class NoPermissionLoggedin extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public NoPermissionLoggedin(){
		super("NoPermissionLoggedin Exception - vBulletin API unable to process request with the provided account's permissions.");
	}
}