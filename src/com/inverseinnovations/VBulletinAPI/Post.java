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
	protected Post parse(LinkedTreeMap<String, Object> postHolder) {
		if(postHolder.containsKey("post")){
			@SuppressWarnings("unchecked")
			LinkedTreeMap<String, Object> postPost = (LinkedTreeMap<String, Object>) postHolder.get("post");
			if(postPost.containsKey("postid")){
				this.postid = Functions.convertToInt(postPost.get("postid"));
			}
			if(postPost.containsKey("posttime")){
				if(Functions.isInteger((String) postPost.get("posttime"))){
					this.posttime = Long.parseLong((String) postPost.get("posttime"));//TODO check
				}
			}
			if(postPost.containsKey("threadid")){
				this.threadid = Functions.convertToInt(postPost.get("threadid"));
			}
			if(postPost.containsKey("userid")){
				this.userid = Functions.convertToInt(postPost.get("userid"));
			}
			if(postPost.containsKey("username")){
				this.username = Functions.convertToString(postPost.get("username"));
			}
			if(postPost.containsKey("avatarurl")){
				this.avatarurl = Functions.convertToString(postPost.get("avatarurl"));
			}
			if(postPost.containsKey("usertitle")){
				this.usertitle = Functions.convertToString(postPost.get("usertitle"));
			}
			if(postPost.containsKey("joindate")){
				if(Functions.isInteger((String) postPost.get("joindate"))){
					this.joindate = Long.parseLong((String) postPost.get("joindate"));//TODO check
				}
			}
			if(postPost.containsKey("title")){
				this.title = Functions.convertToString(postPost.get("title"));
			}
			if(postPost.containsKey("isfirstshown")){
				this.isfirstshown = Functions.convertToBoolean(postPost.get("isfirstshown"));
			}
			if(postPost.containsKey("islastshown")){
				this.islastshown = Functions.convertToBoolean(postPost.get("islastshown"));
			}
			if(postPost.containsKey("message")){
				this.message = Functions.convertToString(postPost.get("message"));
			}
			if(postPost.containsKey("message_plain")){
				this.message_plain = Functions.convertToString(postPost.get("message_plain"));
			}
			if(postPost.containsKey("message_bbcode")){
				this.message_bbcode = Functions.convertToString(postPost.get("message_bbcode"));
			}
		}
		return this;
	}
}
