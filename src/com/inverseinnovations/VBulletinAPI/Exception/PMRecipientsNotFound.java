package com.inverseinnovations.VBulletinAPI.Exception;

public class PMRecipientsNotFound extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public PMRecipientsNotFound(){
		super("PMRecipientsNotFound Exception - vBulletin users are unable to be message because they don't exist");
	}
}