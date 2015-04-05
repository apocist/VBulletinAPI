package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedin;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedout;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;

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
	 recordtime=1.411365986E9,
	 today=2015-03-06,
	 */
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
	
	/**Returns a Forum Homepage containing all the Forums within
	 * @param response from callMethod
	 * @return ForumHome
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ForumHome parse(LinkedTreeMap<String, Object> response) throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("header")){
					LinkedTreeMap header = (LinkedTreeMap<String, Object>)response2.get("header");
					/*if(response2.containsKey("pmbox")){
						LinkedTreeMap pmbox = (LinkedTreeMap<String, Object>)header.get("pmbox");
						
					}
					this.notifications_menubits = Functions.fetchInt(header, "notifications_menubits");*/
					this.notifications_total = Functions.fetchInt(header, "notifications_total");
				}
				this.activemembers = Functions.fetchInt(response2, "activemembers");
				/*if(response2.containsKey("activeusers")){
					LinkedTreeMap activeusers = (LinkedTreeMap<String, Object>)response2.get("activeusers");
				}*/
				/*if(response2.containsKey("birthdays")){
					LinkedTreeMap birthdays = (LinkedTreeMap<String, Object>)response2.get("birthdays");//although an array/not linkedtreemap
				}*/
				if(response2.containsKey("forumbits")){
					if(response2.get("forumbits") instanceof ArrayList){//multiple posts
						ArrayList<LinkedTreeMap<String, Object>> forumbits = (ArrayList<LinkedTreeMap<String, Object>>) response2.get("forumbits");
						for(LinkedTreeMap<String, Object> forumHolder : forumbits){
							this.subforums.add(new Forum().parseSub(forumHolder));
						}
					}
					else if(response2.get("forumbits") instanceof LinkedTreeMap){//multiple posts
						LinkedTreeMap<String, Object> forumbit = (LinkedTreeMap<String, Object>) response2.get("forumbits");
						this.subforums.add(new Forum().parseSub(forumbit));
					}
				}
				/*if(response2.containsKey("newuserinfo")){
					LinkedTreeMap newuserinfo = (LinkedTreeMap<String, Object>)response2.get("newuserinfo");
				}*/
				this.numberguest = Functions.fetchInt(response2, "numberguest");
				this.numbermembers = Functions.fetchInt(response2, "numbermembers");
				this.numberregistered = Functions.fetchInt(response2, "numberregistered");
				//this.recordtime = Functions.fetchInt(response2, "recordtime");
				this.recordusers = Functions.fetchInt(response2, "recordusers");
				//this.today = Functions.fetchString(response2, "today");
				this.totalonline = Functions.fetchInt(response2, "totalonline");
				this.totalposts = Functions.fetchInt(response2, "totalposts");
				this.totalthreads = Functions.fetchInt(response2, "totalthreads");

				
				//TODO need to run the error checks through a function
				if(response2.containsKey("errormessage")){
					String theError = "";
					if(response2.get("errormessage") instanceof String){
						theError = (String)response2.get("errormessage");
						if(theError.equals("redirect_postthanks")){//this is for newthread and newpost
							if(response.get("show") instanceof LinkedTreeMap){
								LinkedTreeMap<String, Object> show = (LinkedTreeMap<String, Object>)response.get("show");
								if(show.containsKey("threadid")){
									theError = ""+Functions.convertToInt(show.get("threadid"));
								}
								if(show.containsKey("postid")){
									theError += " "+Functions.convertToInt(show.get("postid"));
								}
							}
						}
					}
					else if(response2.get("errormessage") instanceof ArrayList){
						Object[] errors = ((ArrayList<String>) response2.get("errormessage")).toArray();
						if(errors.length > 0){
							theError = errors[0].toString();
						}
					}
					VBulletinAPI.errorsCommon(theError);
					System.out.println("responseError  response -> errormessage type unknown: "+response2.get("errormessage").getClass().getName());
					throw new VBulletinAPIException("vBulletin API Unknown Error - "+response2.get("errormessage").getClass().getName());
				}
			}
		}
		if(VBulletinAPI.DEBUG){
			//System.out.println("thread all ->");
			System.out.println(response.toString());
		}
		return this;
	}
}
