package com.inverseinnovations.VBulletinAPI;

import com.inverseinnovations.VBulletinAPI.Functions;

public class Message{
	public int pmid;
	public String sendtime;//TODO is this a String?(check)
	public String statusicon;
	public String title;
	public int userid;
	public String username;
	public String message = "";

	public void setUserid(String id){
		if(Functions.isInteger(id)){
			userid = Integer.parseInt(id);
		}
	}
}
