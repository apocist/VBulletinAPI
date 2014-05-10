package com.inverseinnovations.VBulletinAPI.Exception;

public class APIIOException extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public APIIOException(){
		super("API IOException  - vBulletin API unable to process request due to java.io.IOException");
	}
}
