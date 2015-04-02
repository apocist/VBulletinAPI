package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

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
			System.out.println("member all ->");
			System.out.println(response.toString());
		}
		return this;
	}
	
}
