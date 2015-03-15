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
	public String username;
	public int userid;
	public String avatarurl;
	public String usertitle;
	public String joindate;//TODO is this string?(need to check)
	//TODO many variables missing
	
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
	protected Member parse(LinkedTreeMap<String, Object> response) throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				@SuppressWarnings("unchecked")
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("prepared")){
					if(response2.get("prepared") instanceof LinkedTreeMap){
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> prepared = (LinkedTreeMap<String, Object>)response2.get("prepared");
						if(prepared.containsKey("username")){
							this.username = Functions.convertToString(prepared.get("username"));
						}
						if(prepared.containsKey("userid")){
							this.userid = Functions.convertToInt(prepared.get("userid"));
						}
						if(prepared.containsKey("joindate")){
							this.joindate = Functions.convertToString(prepared.get("joindate"));//TODO is this string?(need to check)
						}
						if(prepared.containsKey("avatarurl")){
							this.avatarurl = Functions.convertToString(prepared.get("avatarurl"));
						}
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
			System.out.println("member all ->");
			System.out.println(response.toString());
		}
		return this;
	}
	
}
