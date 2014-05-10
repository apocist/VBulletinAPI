package com.inverseinnovations.VBulletinAPI.Exception;

public class MaxCharacterLengthReached extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public MaxCharacterLengthReached(){
		super("MaxCharacterLengthReached Exception - vBulletin API unable to process request due request being to many characters");
	}
}