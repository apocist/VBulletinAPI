package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.inverseinnovations.VBulletinAPI.Post;

public class ForumThread{
	public int totalposts;
	public int FIRSTPOSTID;
	public int LASTPOSTID;
	public int pagenumber;
	public int perpage;
	//public String forumrules;
	public ArrayList<Post> posts = new ArrayList<Post>();
}