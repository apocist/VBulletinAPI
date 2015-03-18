package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

public class ForumHome{
	protected int notifications_total;
	protected int activemembers;
	protected int numberguest;
	protected int numbermembers;
	protected int numberregistered;
	protected int recordusers;
	protected int totalonline;
	protected int totalposts;
	protected int totalthreads;
	protected ArrayList<Forum> subforums = new ArrayList<Forum>();
	//recordtime=1.411365986E9,
	//today=2015-03-06,
	/*pmbox={lastvisittime=1410109725},
	notifications_menubits=,*/
	/*
	activeusers={
	1={
		userid=8226,
		username=eMafia Game Master,
		invisible=1.0,
		inforum=0.0,
		lastactivity=1.425614515E9,
		lastvisit=1410109725,
		usergroupid=156,
		displaygroupid=2,
		infractiongroupid=0,
		musername=<span class="registereduser">eMafia Game Master</span>,
		displaygrouptitle=Registered Users,
		displayusertitle=,
		comma=,	,
		buddymark=,
		invisiblemark=*,
		online=invisible,
		onlinestatusphrase=x_is_invisible
	},
	*/
	//birthdays=[],
	/*newuserinfo={
			userid=15607,
			username=olegsander
		},*/
	/*
	show={
		birthdays=0.0,
		notices=0.0,
		notifications=0.0,
		loggedinusers=1.0,
		pmlink=1.0,
		homepage=0.0,
		addfriend=0.0,
		emaillink=0.0,
		activemembers=1.0
	}
	 */
	public int getNotificationsTotal() {
		return notifications_total;
	}
	public int getActiveMembers() {
		return activemembers;
	}
	public int getNumberGuest() {
		return numberguest;
	}
	public int getNumberMembers() {
		return numbermembers;
	}
	public int getNumberRegistered() {
		return numberregistered;
	}
	public int getRecordUsers() {
		return recordusers;
	}
	public int getTotalOnline() {
		return totalonline;
	}
	public int getTotalPosts() {
		return totalposts;
	}
	public int getTotalThreads() {
		return totalthreads;
	}
	public ArrayList<Forum> getSubForums() {
		return new ArrayList<Forum>(subforums);
	}
	
}
