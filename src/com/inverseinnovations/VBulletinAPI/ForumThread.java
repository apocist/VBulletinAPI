package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Post;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidId;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedin;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedout;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;

public class ForumThread{
	
	protected int totalposts;
	protected int FIRSTPOSTID;
	protected int LASTPOSTID;
	protected int pagenumber;
	protected int perpage;
	
	protected ArrayList<Post> posts = new ArrayList<Post>();
	
	protected int numberguest;
	protected int numberregistered;
	
	//Not implemented yet
	
	protected String meta_description;
	protected String title;
	protected int forumid;
	protected int totalonline;
	//public String tag_list;
	//public String keywords;
	//show
	//public String forumrules;
	
	
	public int getTotalPosts() {
		return totalposts;
	}


	public int getFirstPostId() {
		return FIRSTPOSTID;
	}


	public int getLastPostId() {
		return LASTPOSTID;
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


	public int getNumberGuest() {
		return numberguest;
	}


	public int getNumberRegistered() {
		return numberregistered;
	}


	public String getMetaDescription() {
		return meta_description;
	}


	public String getTitle() {
		return title;
	}


	public int getForumId() {
		return forumid;
	}


	public int getTotalOnline() {
		return totalonline;
	}


	/**Returns a Thread containing all the Posts within(or specified)
	 * @param response from callMethod
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings("rawtypes")
	protected ForumThread parse(LinkedTreeMap<String, Object> response) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				@SuppressWarnings("unchecked")
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
				if(response2.containsKey("pagenumber")){
					this.pagenumber = Functions.convertToInt(response2.get("pagenumber"));
				}
				if(response2.containsKey("perpage")){
					this.perpage = Functions.convertToInt(response2.get("perpage"));
				}
				if(response2.containsKey("postbits")){
					if(response2.get("postbits") instanceof ArrayList){//multiple posts
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap<String, Object>> postbits = (ArrayList<LinkedTreeMap<String, Object>>) response2.get("postbits");
						for(LinkedTreeMap<String, Object> postHolder : postbits){
							this.posts.add(new Post().parse(postHolder));
						}
					}
					else if(response2.get("postbits") instanceof LinkedTreeMap){//single post
						@SuppressWarnings("unchecked")
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