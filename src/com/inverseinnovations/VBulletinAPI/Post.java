package com.inverseinnovations.VBulletinAPI;

import com.google.gson.internal.LinkedTreeMap;

public class Post{
	protected int postid;
	protected long posttime;
	protected int threadid;
	protected int userid;
	protected String username;
	protected String avatarurl;
	protected String usertitle;
	protected long joindate;
	protected String title;
	protected boolean isfirstshown;
	protected boolean islastshown;
	protected String message;
	protected String message_plain;
	protected String message_bbcode;
	
	public int getPostId() {
		return postid;
	}
	public long getPostTime() {
		return posttime;
	}
	public int getThreadId() {
		return threadid;
	}
	public int getUserId() {
		return userid;
	}
	public String getUsername() {
		return username;
	}
	public String getAvatarUrl() {
		return avatarurl;
	}
	public String getUserTitle() {
		return usertitle;
	}
	public long getJoinDate() {
		return joindate;
	}
	public String getTitle() {
		return title;
	}
	public boolean isFirstShown() {
		return isfirstshown;
	}
	public boolean isLastShown() {
		return islastshown;
	}
	public String getMessage() {
		return message;
	}
	public String getMessagePlain() {
		return message_plain;
	}
	public String getMessageBbcode() {
		return message_bbcode;
	}

	/**Returns a Post after parsing the LinkedTreeMap
	 * @param LinkedTreeMap from ForumThread
	 * @return Post
	 */
	@SuppressWarnings("unchecked")
	protected Post parse(LinkedTreeMap<String, Object> postHolder) {
		if(postHolder.containsKey("post")){
			LinkedTreeMap<String, Object> postPost = (LinkedTreeMap<String, Object>) postHolder.get("post");
			this.postid = Functions.fetchInt(postPost, "postid");
			if(postPost.containsKey("posttime")){
				if(Functions.isInteger((String) postPost.get("posttime"))){
					this.posttime = Long.parseLong((String) postPost.get("posttime"));//TODO check
				}
			}
			this.threadid = Functions.fetchInt(postPost, "threadid");
			this.userid = Functions.fetchInt(postPost, "userid");
			this.username = Functions.fetchString(postPost, "username");
			this.avatarurl = Functions.fetchString(postPost, "avatarurl");
			this.usertitle = Functions.fetchString(postPost, "usertitle");
			if(postPost.containsKey("joindate")){
				if(Functions.isInteger((String) postPost.get("joindate"))){
					this.joindate = Long.parseLong((String) postPost.get("joindate"));//TODO check
				}
			}
			this.title = Functions.fetchString(postPost, "title");
			this.isfirstshown = Functions.fetchBoolean(postPost, "isfirstshown");
			this.islastshown = Functions.fetchBoolean(postPost, "islastshown");
			this.message = Functions.fetchString(postPost, "message");
			this.message_plain = Functions.fetchString(postPost, "message_plain");
			this.message_bbcode = Functions.fetchString(postPost, "message_bbcode");
		}
		return this;
	}
}
