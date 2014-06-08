package com.inverseinnovations.VBulletinAPI.Exception;

public class APIIllegalStateException extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public APIIllegalStateException(){
		super("API IllegalStateException  - vBulletin API unable to process request due to java.lang.IllegalStateException");
	}
}