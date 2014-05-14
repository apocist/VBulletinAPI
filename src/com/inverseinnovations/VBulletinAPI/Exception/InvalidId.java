package com.inverseinnovations.VBulletinAPI.Exception;

public class InvalidId extends VBulletinAPIException{
	public static final int THREAD = 0;
	private static final long serialVersionUID = 1L;
	public InvalidId(){
		super("InvalidId Exception - The id specified either does not exist was was entered incorrectly");
	}
	public InvalidId(String type){
		super("InvalidId Exception - The "+type+" id specified either does not exist was was entered incorrectly");
	}
}