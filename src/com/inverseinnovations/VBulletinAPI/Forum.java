package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidId;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedin;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedout;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;

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
	
	/**Returns a Forum containing all the Forums within
	 * @param response from callMethod
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings("rawtypes")
	protected Forum parse(LinkedTreeMap<String, Object> response) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				@SuppressWarnings("unchecked")
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("daysprune")){
					this.daysprune = Functions.convertToInt(response2.get("daysprune"));
				}
				if(response2.containsKey("limitlower")){
					this.limitlower = Functions.convertToInt(response2.get("limitlower"));
				}
				if(response2.containsKey("limitupper")){
					this.limitupper = Functions.convertToInt(response2.get("limitupper"));
				}
				if(response2.containsKey("numberguest")){
					this.numberguest = Functions.convertToInt(response2.get("numberguest"));
				}
				if(response2.containsKey("numberguest")){
					this.numberguest = Functions.convertToInt(response2.get("numberguest"));
				}
				if(response2.containsKey("numberregistered")){
					this.numberregistered = Functions.convertToInt(response2.get("numberregistered"));
				}
				if(response2.containsKey("pagenumber")){
					this.pagenumber = Functions.convertToInt(response2.get("pagenumber"));
				}
				if(response2.containsKey("perpage")){
					this.perpage = Functions.convertToInt(response2.get("perpage"));
				}
				if(response2.containsKey("totalmods")){
					this.totalmods = Functions.convertToInt(response2.get("totalmods"));
				}
				if(response2.containsKey("totalonline")){
					this.totalonline = Functions.convertToInt(response2.get("totalonline"));
				}
				if(response2.containsKey("totalthreads")){
					this.totalthreads = Functions.convertToInt(response2.get("totalthreads"));
				}
				//foruminfo {}
				if(response2.containsKey("foruminfo")){
					LinkedTreeMap foruminfo = (LinkedTreeMap)response2.get("foruminfo");
					if(foruminfo.containsKey("forumid")){
						this.forumid = Functions.convertToInt(foruminfo.get("forumid"));
					}
					if(foruminfo.containsKey("title")){
						this.title = Functions.convertToString(foruminfo.get("title"));
					}
					if(foruminfo.containsKey("title_clean")){
						this.title_clean = Functions.convertToString(foruminfo.get("title_clean"));
					}
					if(foruminfo.containsKey("description")){
						this.description = Functions.convertToString(foruminfo.get("description"));
					}
					if(foruminfo.containsKey("description_clean")){
						this.description_clean = Functions.convertToString(foruminfo.get("description_clean"));
					}
					if(foruminfo.containsKey("prefixrequired")){
						this.prefixrequired = Functions.convertToInt(foruminfo.get("prefixrequired"));
					}
				}
				//forumbits []
				if(response2.containsKey("forumbits")){
					System.out.println("forumbits is "+response2.get("forumbits").getClass().getName());
					if(response2.get("forumbits") instanceof ArrayList){//multiple posts
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap<String, Object>> forumbits = (ArrayList<LinkedTreeMap<String, Object>>) response2.get("forumbits");
						for(LinkedTreeMap<String, Object> forumHolder : forumbits){
							this.subforums.add(new Forum().parseSub(forumHolder));
						}
					}
					else if(response2.get("forumbits") instanceof LinkedTreeMap){//multiple posts
						@SuppressWarnings("unchecked")
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
								@SuppressWarnings("unchecked")
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
						@SuppressWarnings("unchecked")
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
	@SuppressWarnings("rawtypes")
	protected Forum parseSub(LinkedTreeMap<String, Object> forumbit) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(forumbit != null){
			if(forumbit.containsKey("parent_is_category")){
				this.parent_is_category = Functions.convertToBoolean(forumbit.get("parent_is_category"));
			}
			if(forumbit.containsKey("forum")){
				LinkedTreeMap forumdata = (LinkedTreeMap)forumbit.get("forum");
				if(forumdata.containsKey("forumid")){
					this.forumid = Functions.convertToInt(forumdata.get("forumid"));
				}
				if(forumdata.containsKey("threadcount")){
					this.threadcount = Functions.convertToInt(forumdata.get("threadcount"));
				}
				if(forumdata.containsKey("replycount")){
					this.replycount = Functions.convertToInt(forumdata.get("replycount"));
				}
				if(forumdata.containsKey("title")){
					this.title = Functions.convertToString(forumdata.get("title"));
				}
				if(forumdata.containsKey("title_clean")){
					this.title_clean = Functions.convertToString(forumdata.get("title_clean"));
				}
				if(forumdata.containsKey("description")){
					this.description = Functions.convertToString(forumdata.get("description"));
				}
				if(forumdata.containsKey("description_clean")){
					this.description_clean = Functions.convertToString(forumdata.get("description_clean"));
				}
				if(forumdata.containsKey("statusicon")){
					this.statusicon = Functions.convertToString(forumdata.get("statusicon"));
				}
				if(forumdata.containsKey("browsers")){
					this.browsers = Functions.convertToInt(forumdata.get("browsers"));
				}
				if(forumbit.containsKey("childforumbits")){//yes...within the forumdata if statement
					if(forumbit.get("childforumbits") instanceof ArrayList){
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap<String, Object>> childforumbits = (ArrayList<LinkedTreeMap<String, Object>>) forumbit.get("childforumbits");
						for(LinkedTreeMap<String, Object> childforumHolder : childforumbits){
							this.subforums.add(new Forum().parseSub(childforumHolder));
						}
					}
					else if(forumbit.get("childforumbits") instanceof LinkedTreeMap){
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> childforumbit = (LinkedTreeMap<String, Object>) forumbit.get("childforumbits");
						this.subforums.add(new Forum().parseSub(childforumbit));
					}
				}
				if(forumdata.containsKey("subforums")){
					if(forumdata.get("subforums") instanceof ArrayList){
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap<String, Object>> subforums = (ArrayList<LinkedTreeMap<String, Object>>) forumdata.get("subforums");
						for(LinkedTreeMap<String, Object> subforumHolder : subforums){
							this.subforums.add(new Forum().parseSub(subforumHolder));
						}
					}
					else if(forumdata.get("subforums") instanceof LinkedTreeMap){
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> subforum = (LinkedTreeMap<String, Object>) forumdata.get("subforums");
						this.subforums.add(new Forum().parseSub(subforum));
					}
				}
			}
		}
		return this;
	}
}
