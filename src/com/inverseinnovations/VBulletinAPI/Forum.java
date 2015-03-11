package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

public class Forum{
	public int forumid;
	public int threadcount;
	public int replycount;
	public String title;
	public String title_clean;
	public String description;
	public String description_clean;
	public int prefixrequired;
	public String statusicon; //(link)
	public int browsers;
	public boolean parent_is_category;
	public int numberguest;
	public int numberregistered;
	public int totalmods;
	public int totalonline;
	public int totalthreads;
	public int pagenumber;
	public int perpage;
	public int limitlower;
	public int limitupper;
	public int daysprune;
	public ArrayList<Forum> subforums = new ArrayList<Forum>();//subforums and childforums are the same thing
	/*activeusers={
			1=[]
	},*/
	/*subforums[{
	  	forum={
			forumid=293,
			threadcount=2,
			replycount=28,
			title=Signups</a><span class="shade" style="font-size:10px;" title="Threads/Posts"> (2/28)</span>VSa,
			description=,
			title_clean=Signups,
			description_clean=,
			statusicon=new
		}
	}]*/
	/*daysprunesel={
		all=1.0
	},*/
	/*lastpostinfo:
	 	lastposter=Cosmo31,
		lastposterid=15453,
		lastthread=Day Length in FM,
		lastthreadid=30256,
		lastposttime=1425390961,
		trimthread=Day Length in FM,
		prefix=
	 */
	/*order={desc=1.0},*/
	/*moderatorslist={1=[], 2=[]},*/
	/*prefix_options=,
	prefix_selected={
		0=anythread,
		anythread=1.0,
		none=
	},*/
	/*sort={lastpost=1.0},
	threadbits={
		thread={
			threadtitle=
		}
	},*/
	//threadbits
	/*forumrules={
			bbcodeon=On,
			can={
				postnew=0.0,
				replyown=0.0,
				replyothers=0.0,
				reply=0.0,
				editpost=128.0,
				postattachment=0.0,
				attachment=0.0
			},
			htmlcodeon=On,
			imgcodeon=On,
			videocodeon=On,
			smilieson=On
		}*/
	
	//show
	/*
		foruminfo=1.0,
		forumsubscription=0.0,
		forumdescription=1.0,
		subforums=0.0,
		browsers=1.0
		newthreadlink=0.0,
		threadicons=1.0,
		threadratings=1.0,
		subscribed_to_forum=0.0,
		moderators=1.0,
		activeusers=1.0,
		post_queue=0.0,
		attachment_queue=0.0,
		mass_move=0.0,
		mass_prune=0.0,
		post_new_announcement=0.0,
		addmoderator=0.0,
		adminoptions=0.0,
		movethread=0.0,
		deletethread=0.0,
		approvethread=0.0,
		openthread=0.0,
		inlinemod=0.0,
		spamctrls=0.0,
		noposts=1.0,
		dotthreads=1.0,
		threadslist=1.0,
		forumsearch=1.0,
		forumslist=1.0,
		stickies=0.0*/
}
