package com.inverseinnovations.VBulletinAPI;

import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidId;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedin;
import com.inverseinnovations.VBulletinAPI.Exception.NoPermissionLoggedout;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;
//TODO should put Sender User info in it's own class?
public class Message{
	protected String avatarurl;
	protected int folderid;
	protected String fromusername;
	protected int infractions;
	protected int ipoints;
	protected int joindate;
	protected String message;
	protected String message_bbcode;
	protected String message_plain;
	protected String onlinestatusphrase;
	protected int pmid;
	protected int reppower;
	protected boolean savecopy;
	protected int sendtime;
	protected String statusicon;
	protected String title;
	protected int userid;
	protected String username;
	protected String usertitle;
	protected int warnings;
	
	public String getAvatarUrl() {
		return avatarurl;
	}
	public int getFolderId() {
		return folderid;
	}
	public String getFromUsername() {
		return fromusername;
	}
	public int getInfractions() {
		return infractions;
	}
	public int getIPoints() {
		return ipoints;
	}
	public int getJoinDate() {
		return joindate;
	}
	public String getMessage() {
		return message;
	}
	public String getMessage_bbcode() {
		return message_bbcode;
	}
	public String getMessage_plain() {
		return message_plain;
	}
	public String getOnlineStatusPhrase() {
		return onlinestatusphrase;
	}
	public int getPmId() {
		return pmid;
	}
	public int getRepPower() {
		return reppower;
	}
	public int getSendTime() {//
		return sendtime;
	}
	public String getStatusIcon() {
		return statusicon;
	}
	public String getTitle() {
		return title;
	}
	public int getUserId() {
		return userid;
	}
	public String getUsername() {
		return username;
	}
	public String getUserTitle() {
		return usertitle;
	}
	public int getWarnings() {
		return warnings;
	}
	public boolean isSaveCopy() {
		return savecopy;
	}
	/**Returns a Message parsed from the information provided
	 * @param response from callMethod
	 * @return
	 * @throws InvalidId PMid missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Message parse(LinkedTreeMap<String, Object> response) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(response != null){
			if(response.containsKey("response")){
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("HTML")){
					if(response2.get("HTML") instanceof LinkedTreeMap){
						LinkedTreeMap<String, Object> HTML = (LinkedTreeMap<String, Object>)response2.get("HTML");
						//TODO need to test, arrayed?
						//this.bccrecipients = Functions.fetchString(HTML, "bccrecipients");
						//this.ccrecipients = Functions.fetchString(HTML, "ccrecipients");
						if(HTML.containsKey("pm")){
							if(HTML.get("pm") instanceof LinkedTreeMap){
								LinkedTreeMap<String, Object> pm = (LinkedTreeMap<String, Object>) HTML.get("pm");
								this.pmid = Functions.fetchInt(pm, "pmid");
								this.title = Functions.fetchString(pm, "title");
								//this.recipients = Functions.fetchString(pm, "recipients");
								this.savecopy = Functions.fetchBoolean(pm, "savecopy");
								this.folderid = Functions.fetchInt(pm, "folderid");
								this.fromusername = Functions.fetchString(pm, "fromusername");
							}
						}
						if(HTML.containsKey("postbit")){
							if(HTML.get("postbit") instanceof LinkedTreeMap){
								LinkedTreeMap<String, Object> postbit = (LinkedTreeMap<String, Object>) HTML.get("postbit");
								if(postbit.containsKey("post")){
									if(postbit.get("post") instanceof LinkedTreeMap){
										LinkedTreeMap<String, Object> post = (LinkedTreeMap<String, Object>) postbit.get("post");
										this.statusicon = Functions.fetchString(post, "statusicon");
										this.sendtime = Functions.fetchInt(post, "posttime");
										//this.checkbox_value = Functions.fetchBoolean(post, "checkbox_value");
										this.onlinestatusphrase = Functions.fetchString(post, "onlinestatusphrase");
										this.userid = Functions.fetchInt(post, "userid");
										this.username = Functions.fetchString(post, "username");
										this.avatarurl = Functions.fetchString(post, "avatarurl");
										//this.onlinestatus = Functions.fetchString(post, "onlinestatus");
										this.usertitle = Functions.fetchString(post, "usertitle");
										//this.rank = Functions.fetchString(post, "rank");
										//this.reputationdisplay = Functions.fetchString(post, "reputationdisplay");
										this.joindate = Functions.fetchInt(post, "joindate");
										//this.field2 = Functions.fetchString(post, "field2");//TODO is this used globally?
										this.warnings = Functions.fetchInt(post, "warnings");
										this.infractions = Functions.fetchInt(post, "infractions");
										this.ipoints = Functions.fetchInt(post, "ipoints");
										this.reppower = Functions.fetchInt(post, "reppower");
										//this.title = Functions.fetchString(post, "title");//Already defined?
										this.message = Functions.fetchString(post, "message");
										this.message_plain = Functions.fetchString(post, "message_plain");
										this.message_bbcode = Functions.fetchString(post, "message_bbcode");
									}
								}
								//this.postbit_type = Functions.fetchString(postbit, "postbit_type");//TODO may need to process the type here
								/*if(postbit.containsKey("show")){
									if(postbit.get("show") instanceof LinkedTreeMap){
										LinkedTreeMap<String, Object> show = (LinkedTreeMap<String, Object>) postbit.get("show");
									}
								}*/
							}
						}
					}
				}
				/*if(response2.containsKey("show")){
					if(response2.get("show") instanceof LinkedTreeMap){

						if(((LinkedTreeMap<String, Object>) response2.get("show")).containsKey("receiptprompt")){
							this.receiptprompt = Functions.convertToString(((LinkedTreeMap<String, Object>) response2.get("show")).get("receiptprompt"));
						}
					}
				}*/
				//TODO odd error checking - need to redo
				if(response2.containsKey("errormessage")){
					String theError = "";
					String errorSecond = "";
					String className = ((LinkedTreeMap)response.get("response")).get("errormessage").getClass().getName();
					if(className.equals("java.lang.String")){
						theError = ((String) response2.get("errormessage"));
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
