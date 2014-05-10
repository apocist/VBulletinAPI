package com.inverseinnovations.VBulletinAPI.Exception;

public class PMRecipTurnedOff extends VBulletinAPIException{
	private static final long serialVersionUID = 1L;
	public PMRecipTurnedOff(){
		super("PMRecipTurnedOff Exception - vBulletin user not allowing PMs");
	}
}
