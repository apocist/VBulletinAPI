package com.inverseinnovations.VBulletinAPI.Exception;

public class APISocketTimeoutException extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public APISocketTimeoutException(){
		super("API SocketTimeoutException - vBulletin API unable to process request due to java.net.SocketTimeoutException");
	}
}

