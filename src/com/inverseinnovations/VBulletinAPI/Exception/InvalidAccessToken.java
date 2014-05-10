package com.inverseinnovations.VBulletinAPI.Exception;

public class InvalidAccessToken extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public InvalidAccessToken(){
		super("InvalidAccessToken Exception - vBulletin API unable to process request due to incorrect token provided (could be due to exceeding max character length)");
	}
}
