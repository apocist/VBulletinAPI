package com.inverseinnovations.VBulletinAPI.Exception;

public class InvalidAPISignature extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public InvalidAPISignature(){
		super("InvalidAPISignature Exception - vBulletin API unable to process request due to the Hashed signature not consistent with the request. Thrown with invalid characters ' \" and  \\");
	}
}
