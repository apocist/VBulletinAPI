package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Post;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidId;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedin;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedout;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;

public class ForumThread{
	
	protected int firstPage;//TODO this my not be named right...given 1 and 12 in a 12 post thread
	protected int FIRSTPOSTID;
	protected int forumid;
	protected int lastPage;
	protected int LASTPOSTID;
	protected String meta_description;
	protected int numberguest;
	protected int numberregistered;
	protected int pagenumber;
	protected int perpage;
	protected ArrayList<Post> posts = new ArrayList<Post>();
	protected int threadid;
	protected String title;
	protected int totalonline;
	protected int totalposts;
	
	//Not implemented yet
	
	//activeusers
	//protected String forumrules;
	//protected String tag_list;
	//protected ArrayList<String> keywords = new ArrayList<String>();
	//show
	/*
	 * threadinfo=1.0,
		threadedmode=0.0,
		linearmode=1.0,
		hybridmode=0.0,
		viewpost=0.0,
		managepost=0.0,
		approvepost=0.0,
		managethread=0.0,
		approveattachment=0.0,
		inlinemod=0.0,
		spamctrls=0.0,
		rating=0.0,
		largereplybutton=1.0,
		multiquote_global=1.0,
		firstunreadlink=1.0,
		tag_box=0.0,
		manage_tag=0.0,
		activeusers=1.0,
		deleteposts=0.0,
		editthread=0.0,
		movethread=1.0,
		stickunstick=0.0,
		openclose=1.0,
		moderatethread=0.0,
		deletethread=1.0,
		adminoptions=1.0,
		addpoll=1.0,
		search=1.0,
		subscribed=0.0,
		threadrating=0.0,
		ratethread=0.0,
		closethread=0.0,
		approvethread=0.0,
		unstick=0.0,
		reputation=1.0,
		sendtofriend=0.0,
		next_prev_links=1.0
	 */
	
	
	
	public String getDescription() {
		return meta_description;
	}


	public int getFirstPage() {
		return firstPage;
	}


	public int getFirstPostId() {
		return FIRSTPOSTID;
	}


	public int getForumId() {
		return forumid;
	}


	public int getLastPage() {
		return lastPage;
	}


	public int getLastPostId() {
		return LASTPOSTID;
	}


	public int getNumberGuest() {
		return numberguest;
	}


	public int getNumberRegistered() {
		return numberregistered;
	}


	public int getPageNumber() {
		return pagenumber;
	}


	public int getPerPage() {
		return perpage;
	}


	public ArrayList<Post> getPosts() {
		return new ArrayList<Post>(posts);
	}


	public int getThreadId() {
		return threadid;
	}


	public String getTitle() {
		return title;
	}


	public int getTotalOnline() {
		return totalonline;
	}


	public int getTotalPosts() {
		return totalposts;
	}


	/**Returns a Thread containing all the Posts within(or specified)
	 * @param response from callMethod
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected ForumThread parse(LinkedTreeMap<String, Object> response) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("totalposts")){
					this.totalposts = Functions.convertToInt(response2.get("totalposts"));
				}
				if(response2.containsKey("FIRSTPOSTID")){
					this.FIRSTPOSTID = Functions.convertToInt(response2.get("FIRSTPOSTID"));
				}
				if(response2.containsKey("LASTPOSTID")){
					this.LASTPOSTID = Functions.convertToInt(response2.get("LASTPOSTID"));
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
				if(response2.containsKey("totalonline")){
					this.totalonline = Functions.convertToInt(response2.get("totalonline"));
				}
				if(response2.containsKey("pagenumbers")){
					if(response2.get("pagenumbers") instanceof LinkedTreeMap){
						LinkedTreeMap<String, Object> pagenumbers = (LinkedTreeMap<String, Object>)response2.get("pagenumbers");
						if(pagenumbers.containsKey("first")){
							this.firstPage = Functions.convertToInt(pagenumbers.get("first"));
						}
						if(pagenumbers.containsKey("last")){
							this.lastPage = Functions.convertToInt(pagenumbers.get("last"));
						}
					}
				}
				if(response2.containsKey("thread")){
					if(response2.get("thread") instanceof LinkedTreeMap){
						LinkedTreeMap<String, Object> thread = (LinkedTreeMap<String, Object>)response2.get("thread");
						if(thread.containsKey("meta_description")){
							this.meta_description = Functions.convertToString(thread.get("meta_description"));
						}
						if(thread.containsKey("title")){
							this.title = Functions.convertToString(thread.get("title"));
						}
						if(thread.containsKey("threadid")){
							this.threadid = Functions.convertToInt(thread.get("threadid"));
						}
						if(thread.containsKey("forumid")){
							this.forumid = Functions.convertToInt(thread.get("forumid"));
						}
						//TODO get keywords
					}
				}
				if(response2.containsKey("postbits")){
					if(response2.get("postbits") instanceof ArrayList){//multiple posts
						ArrayList<LinkedTreeMap<String, Object>> postbits = (ArrayList<LinkedTreeMap<String, Object>>) response2.get("postbits");
						for(LinkedTreeMap<String, Object> postHolder : postbits){
							this.posts.add(new Post().parse(postHolder));
						}
					}
					else if(response2.get("postbits") instanceof LinkedTreeMap){//single post
						LinkedTreeMap<String, Object> postHolder = (LinkedTreeMap<String, Object>) response2.get("postbits");
						this.posts.add(new Post().parse(postHolder));
					}
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("errormessage")){
					String theError = "";
					String errorSecond = "";
					String className = ((LinkedTreeMap)response.get("response")).get("errormessage").getClass().getName();
					if(className.equals("java.lang.String")){
						theError = ((String) ((LinkedTreeMap)response.get("response")).get("errormessage"));
						if(theError.equals("redirect_postthanks")){//this is for newthread and newpost
							if(response.containsKey("show")){
								if(((LinkedTreeMap)response.get("show")).containsKey("threadid")){
									theError = ""+Functions.convertToInt(((LinkedTreeMap)response.get("show")).get("threadid"));
									theError += " "+Functions.convertToInt(((LinkedTreeMap)response.get("show")).get("postid"));
								}
							}
						}
					}
					else if(className.equals("java.util.ArrayList")){
						Object[] errors = ((ArrayList) ((LinkedTreeMap)response.get("response")).get("errormessage")).toArray();
						if(errors.length > 0){
							theError = errors[0].toString();
						}
						if(errors.length > 1){
							errorSecond = errors[1].toString();
						}
					}
					//parse theError here
					if(theError.equals("noid")){
						System.out.println("Thread Parse InvalidId "+errorSecond);
						throw new InvalidId(errorSecond);
					}
					VBulletinAPI.errorsCommon(theError);
					System.out.println("responseError  response -> errormessage type unknown: "+className);
					throw new VBulletinAPIException("vBulletin API Unknown Error - "+className);
				}
			}
		}
		if(VBulletinAPI.DEBUG){
			System.out.println("thread all ->");
			System.out.println(response.toString());
		}
		return this;
	}
}