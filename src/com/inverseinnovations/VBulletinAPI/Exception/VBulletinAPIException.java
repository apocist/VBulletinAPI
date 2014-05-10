package com.inverseinnovations.VBulletinAPI.Exception;

public class VBulletinAPIException extends Exception{
	private static final long serialVersionUID = 1L;
	public VBulletinAPIException(){
		super("vBulletin API Error Returned - The forum is unable to complete your request");
	}
	public VBulletinAPIException(String message) { super(message); }
	public VBulletinAPIException(String message, Throwable cause) { super(message, cause); }
	public VBulletinAPIException(Throwable cause) { super(cause); }
}
