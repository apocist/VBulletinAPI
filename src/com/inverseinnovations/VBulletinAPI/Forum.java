package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidId;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedin;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedout;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;

public class Forum{
	protected int forumid;
	protected int threadcount;
	protected int replycount;
	protected String title;
	protected String title_clean;
	protected String description;
	protected String description_clean;
	protected int prefixrequired;//TODO boolean?
	protected String statusicon; //(link)
	protected int browsers;
	protected boolean parent_is_category;
	protected int numberguest;
	protected int numberregistered;
	protected int totalmods;
	protected int totalonline;
	protected int totalthreads;
	protected int pagenumber;
	protected int perpage;
	protected int limitlower;
	protected int limitupper;
	protected int daysprune;
	protected ArrayList<Forum> subforums = new ArrayList<Forum>();//subforums and childforums are the same thing
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
	
	public int getForumId() {
		return forumid;
	}

	public int getThreadCount() {
		return threadcount;
	}

	public int getReplyCount() {
		return replycount;
	}

	public String getTitle() {
		return title;
	}

	public String getTitleClean() {
		return title_clean;
	}

	public String getDescription() {
		return description;
	}

	public String getDescriptionClean() {
		return description_clean;
	}

	public int getPrefixRequired() {//TODO boolean?
		return prefixrequired;
	}

	public String getStatusIcon() {
		return statusicon;
	}

	public int getBrowsers() {
		return browsers;
	}

	public boolean isParentIsCategory() {
		return parent_is_category;
	}

	public int getNumberGuest() {
		return numberguest;
	}

	public int getNumberRegistered() {
		return numberregistered;
	}

	public int getTotalMods() {
		return totalmods;
	}

	public int getTotalOnline() {
		return totalonline;
	}

	public int getTotalThreads() {
		return totalthreads;
	}

	public int getPageNumber() {
		return pagenumber;
	}

	public int getPerPage() {
		return perpage;
	}

	public int getLimitLower() {
		return limitlower;
	}

	public int getLimitUpper() {
		return limitupper;
	}

	public int getDaysPrune() {
		return daysprune;
	}

	public ArrayList<Forum> getSubForums() {
		return new ArrayList<Forum>(subforums);
	}

	/**Returns a Forum containing all the Forums within
	 * @param response from callMethod
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Forum parse(LinkedTreeMap<String, Object> response) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				this.daysprune = Functions.fetchInt(response2, "daysprune");
				this.limitlower = Functions.fetchInt(response2, "limitlower");
				this.limitupper = Functions.fetchInt(response2, "limitupper");
				this.numberguest = Functions.fetchInt(response2, "numberguest");
				this.numberregistered = Functions.fetchInt(response2, "numberregistered");
				this.pagenumber = Functions.fetchInt(response2, "pagenumber");
				this.perpage = Functions.fetchInt(response2, "perpage");
				this.totalmods = Functions.fetchInt(response2, "totalmods");
				this.totalonline = Functions.fetchInt(response2, "totalonline");
				this.totalthreads = Functions.fetchInt(response2, "totalthreads");
				if(response2.containsKey("foruminfo")){
					LinkedTreeMap foruminfo = (LinkedTreeMap<String, Object>)response2.get("foruminfo");
					this.forumid = Functions.fetchInt(foruminfo, "forumid");
					this.title = Functions.fetchString(foruminfo, "title");
					this.title_clean = Functions.fetchString(foruminfo, "title_clean");
					this.description = Functions.fetchString(foruminfo, "description");
					this.description_clean = Functions.fetchString(foruminfo, "description_clean");
					this.prefixrequired = Functions.fetchInt(foruminfo, "prefixrequired");
				}
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
			//System.out.println(response.toString());
		}
		return this;
	}
	
	/**Returns a Forum containing all the Forums within
	 * @param LinkedTreeMap from pre-parsed function
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Forum parseSub(LinkedTreeMap<String, Object> forumbit) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(forumbit != null){
			this.parent_is_category = Functions.fetchBoolean(forumbit, "parent_is_category");
			if(forumbit.containsKey("forum")){
				LinkedTreeMap forumdata = (LinkedTreeMap<String, Object>)forumbit.get("forum");
				this.forumid = Functions.fetchInt(forumdata, "forumid");
				this.threadcount = Functions.fetchInt(forumdata, "threadcount");
				this.replycount = Functions.fetchInt(forumdata, "replycount");
				this.title = Functions.fetchString(forumdata, "title");
				this.title_clean = Functions.fetchString(forumdata, "title_clean");
				this.description = Functions.fetchString(forumdata, "description");
				this.description_clean = Functions.fetchString(forumdata, "description_clean");
				this.statusicon = Functions.fetchString(forumdata, "statusicon");
				this.browsers = Functions.fetchInt(forumdata, "browsers");
				if(forumbit.containsKey("childforumbits")){//yes...within the forumdata if statement
					if(forumbit.get("childforumbits") instanceof ArrayList){
						ArrayList<LinkedTreeMap<String, Object>> childforumbits = (ArrayList<LinkedTreeMap<String, Object>>) forumbit.get("childforumbits");
						for(LinkedTreeMap<String, Object> childforumHolder : childforumbits){
							this.subforums.add(new Forum().parseSub(childforumHolder));
						}
					}
					else if(forumbit.get("childforumbits") instanceof LinkedTreeMap){
						LinkedTreeMap<String, Object> childforumbit = (LinkedTreeMap<String, Object>) forumbit.get("childforumbits");
						this.subforums.add(new Forum().parseSub(childforumbit));
					}
				}
				if(forumdata.containsKey("subforums")){
					if(forumdata.get("subforums") instanceof ArrayList){
						ArrayList<LinkedTreeMap<String, Object>> subforums = (ArrayList<LinkedTreeMap<String, Object>>) forumdata.get("subforums");
						for(LinkedTreeMap<String, Object> subforumHolder : subforums){
							this.subforums.add(new Forum().parseSub(subforumHolder));
						}
					}
					else if(forumdata.get("subforums") instanceof LinkedTreeMap){
						LinkedTreeMap<String, Object> subforum = (LinkedTreeMap<String, Object>) forumdata.get("subforums");
						this.subforums.add(new Forum().parseSub(subforum));
					}
				}
			}
		}
		return this;
	}
}
