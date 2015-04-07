package com.inverseinnovations.VBulletinAPI;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Exception.APIIOException;
import com.inverseinnovations.VBulletinAPI.Exception.APIIllegalStateException;
import com.inverseinnovations.VBulletinAPI.Exception.APISocketTimeoutException;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidAPISignature;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidAccessToken;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedin;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedout;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;

public class Member{
	protected String username;
	protected int userid;
	protected String avatarurl;
	protected String usertitle;
	protected int joindate;//TODO is this string?(need to check)
	//TODO many variables missing
	
	
	
	public String getUsername() {
		return username;
	}
	public int getUserId() {
		return userid;
	}
	public String getAvatarUrl() {
		return avatarurl;
	}
	public String getUserTitle() {
		return usertitle;
	}
	public int getJoinDate() {
		return joindate;
	}

	/** Parses json from viewMember into
	 * username
	 * forumid
	 * forumjoindate
	 * avatarurl
	 * @param response from viewMember (callMethod)
	 * @return HashMap<String, String>
	 * @throws APIIllegalStateException
	 * @throws APIIOException
	 * @throws APISocketTimeoutException
	 * @throws InvalidAccessToken
	 * @throws NoPermissionLoggedin
	 * @throws NoPermissionLoggedout
	 * @throws InvalidAPISignature
	 */
	@SuppressWarnings("unchecked")
	protected Member parse(LinkedTreeMap<String, Object> response) throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("prepared")){
					if(response2.get("prepared") instanceof LinkedTreeMap){
						LinkedTreeMap<String, Object> prepared = (LinkedTreeMap<String, Object>)response2.get("prepared");
						this.username = Functions.fetchString(prepared, "username");
						this.userid = Functions.fetchInt(prepared, "userid");
						this.joindate = Functions.fetchInt(prepared, "joindate");
						this.avatarurl = Functions.fetchString(prepared, "avatarurl");
					}
				}
				Functions.responseErrorCheck(response);
			}
		}
		if(VBulletinAPI.DEBUG){
			System.out.println("member all ->");
			System.out.println(response.toString());
		}
		return this;
	}
	
}
