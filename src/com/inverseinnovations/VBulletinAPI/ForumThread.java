package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.inverseinnovations.VBulletinAPI.Post;

public class ForumThread{
	
	public int totalposts;
	public int FIRSTPOSTID;
	public int LASTPOSTID;
	public int pagenumber;
	public int perpage;
	
	public ArrayList<Post> posts = new ArrayList<Post>();
	
	public int numberguest;
	public int numberregistered;
	
	//Not implemented yet
	
	public String meta_description;
	public String title;
	public int forumid;
	public int totalonline;
	//public String tag_list;
	//public String keywords;
	//show
	//public String forumrules;
}