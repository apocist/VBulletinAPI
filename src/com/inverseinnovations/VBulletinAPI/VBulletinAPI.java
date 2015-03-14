package com.inverseinnovations.VBulletinAPI;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.gson.Gson;					//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.google.gson.internal.LinkedTreeMap;	//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.google.gson.reflect.TypeToken;		//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.google.gson.stream.JsonReader;		//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.inverseinnovations.VBulletinAPI.Exception.*;

/** A class to provide an easy to use wrapper around the vBulletin REST API.*/
public final class VBulletinAPI extends Thread{
	final public static double VERSION = 0.4;
	final public static boolean DEBUG = true;

	private boolean CONNECTED = false;
	private boolean LOGGEDIN = false;
	private String clientname;
	private String clientversion;
	private String apikey;
	private String apiURL;

	private String apiAccessToken;
	private String apiClientID;
	private String secret;
	private String username;
	private String password;//TODO need a more secure storage

	/**
	 * Returns a String built from the InputStream
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	private String inputStreamToString(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return new String(baos.toByteArray(), Charset.defaultCharset());
	}
	/**
	 * Instantiates a new vBulletin API wrapper. This will initialize the API
	 * connection as well, with OS name and version pulled from property files
	 * and unique ID generated from the hash code of the system properties.
	 * Use isConnected() to verify results.
	 *
	 * @param apiURL
	 *            the URL of api.php on the given vBulletin site
	 * @param apikey
	 *            the API key for the site
	 */
	public VBulletinAPI(String apiURL, String apikey){
		this(null, null, apiURL, apikey, "vBulletinAPI", VERSION+"");
	}
	/**
	 * Instantiates a new vBulletin API wrapper. This will initialize the API
	 * connection as well, with OS name and version pulled from property files
	 * and unique ID generated from the hash code of the system properties.
	 * Use isConnected() to verify results.
	 *
	 * @param apiURL
	 *            the URL of api.php on the given vBulletin site
	 * @param apikey
	 *            the API key for the site
	 * @param clientname
	 *            the name of the client
	 * @param clientversion
	 *            the version of the client
	 */
	public VBulletinAPI(String apiURL, String apikey, String clientname,String clientversion){
		this(null, null, apiURL, apikey, clientname, clientversion);
	}
	/**
	 * Instantiates a new vBulletin API wrapper. This will initialize the API
	 * connection as well, with OS name and version pulled from property files
	 * and unique ID generated from the hash code of the system properties.
	 * Will attempt to login with provided credentials.
	 * Use isConnected() and isLoggedin() to verify results.
	 *
	 * @param username
	 *            the username of the account on the given vBulletin site
	 * @param password
	 *            the password of the account on the given vBulletin site
	 * @param apiURL
	 *            the URL of api.php on the given vBulletin site
	 * @param apikey
	 *            the API key for the site
	 * @param clientname
	 *            the name of the client
	 * @param clientversion
	 *            the version of the client
	 * @throws IOException(no)
	 *             If the URL is wrong, or a connection is unable to be made for
	 *             whatever reason.
	 */
	public VBulletinAPI(String username, String password, String apiURL, String apikey, String clientname,String clientversion){ //throws IOException {
		this.apiURL = apiURL;
		this.apikey = apikey;
		this.clientname = clientname;
		this.clientversion = clientversion;
		this.setName("vBulletinAPI");
		this.setDaemon(true);
		this.setUsername(username);
		this.setPassword(password);
		this.start();
	}
	/**
	 * Calls a method through the API.
	 *
	 * @param methodname
	 *            the name of the method to call
	 * @param params
	 *            the parameters as a map
	 * @param sign
	 *            if the request should be signed or not. Generally, you want this to be true
	 * @return the array returned by the server
	 * @throws IOException
	 *             If the URL is wrong, or a connection is unable to be made for
	 *             whatever reason.
	 */
	private LinkedTreeMap<String, Object> callMethod(String methodname,Map<String, String> params, boolean sign){// throws IOException{
		LinkedTreeMap<String, Object> map = new LinkedTreeMap<String, Object>();

		try{

			StringBuffer queryStringBuffer = new StringBuffer("api_m=" + methodname);
			SortedSet<String> keys = new TreeSet<String>(params.keySet());
			for (String key : keys) {// ' " \ are unsafe
				String value = Functions.querySafeString(params.get(key));
				queryStringBuffer.append("&" + key + "=" + URLEncoder.encode(value, "UTF-8"));
			}
			if (sign) {
				queryStringBuffer.append("&api_sig="+ Functions.MD5( (queryStringBuffer.toString() + getAPIAccessToken()+ apiClientID + getSecret() + getAPIkey())).toLowerCase());
				if(DEBUG){System.out.println("encoded: "+queryStringBuffer.toString());}
			}

			queryStringBuffer.append("&api_c=" + apiClientID);
			queryStringBuffer.append("&api_s=" + getAPIAccessToken());
			String queryString = queryStringBuffer.toString();
			queryString = queryString.replace(" ", "%20");
			URL apiUrl = new URL(apiURL + "?" + queryString);
			HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
			conn.setRequestMethod("POST");

			conn.setConnectTimeout(10000); //set timeout to 10 seconds
			conn.setReadTimeout(10000);//set timeout to 15 seconds
			conn.setDoOutput(true);
			conn.setDoInput(true);
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			out.writeBytes(queryString);
			InputStream is = null;
			try{
				is = conn.getInputStream();
			}
			finally{
				if(is != null){
					String json = inputStreamToString(is);

					//need to remove everything before {
					if(json.contains("{")){
						json = json.substring(json.indexOf("{"));
					}

					Gson gson = new Gson();
					JsonReader reader = new JsonReader(new StringReader(json));
					reader.setLenient(true);
					try{
						map = gson.fromJson(reader,new TypeToken<Map<String, Object>>() {}.getType());
					}
					catch(Exception e){
						System.out.println(json);
						e.printStackTrace();
						map = new LinkedTreeMap<String, Object>();
						map.put("custom", new String("IllegalStateException"));
					}
				}

			}
			conn.disconnect();
		}
		catch (java.net.SocketTimeoutException e) {
			map = new LinkedTreeMap<String, Object>();
			map.put("custom", new String("SocketTimeoutException"));
		}
		catch(IOException e){
			map = new LinkedTreeMap<String, Object>();
			map.put("custom", new String("IOException"));
		}
		return map;
	}
	/**
	 * Detects the most common errors and throws them if exist
	 * @param errorMsg
	 * @throws InvalidAPISignature
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws InvalidAccessToken
	 * @throws APISocketTimeoutException
	 * @throws APIIOException
	 * @throws APIIllegalStateException
	 */
	private void errorsCommon(String errorMsg) throws InvalidAPISignature, NoPermissionLoggedout, NoPermissionLoggedin, InvalidAccessToken, APISocketTimeoutException, APIIOException, APIIllegalStateException{
		if(errorMsg != null){
			if(errorMsg.equals("invalid_api_signature")){
				throw new InvalidAPISignature();
			}
			else if(errorMsg.equals("nopermission_loggedout")){
				throw new NoPermissionLoggedout();
			}
			else if(errorMsg.equals("nopermission_loggedin")){
				throw new NoPermissionLoggedin();
			}
			else if(errorMsg.equals("invalid_accesstoken")){
				throw new InvalidAccessToken();
			}
			else if(errorMsg.equals("SocketTimeoutException")){
				throw new APISocketTimeoutException();
			}
			else if(errorMsg.equals("IOException")){
				throw new APIIOException();
			}
			else if(errorMsg.equals("IllegalStateException")){
				if(DEBUG){System.out.println("ERROR IllegalStateException");}
				throw new APIIllegalStateException();
			}
		}
	}
	/**Returns forum homepage related information
	 * @return
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public String forum_Home(int loop) throws VBulletinAPIException{
		if(isConnected()){
			//String errorMsg = "";
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("forumid", "88");
			
			parseForumDisplay(callMethod("forumdisplay", params, true));

			//errorsCommon(errorMsg);
			return "done";
		}
		throw new NoConnectionException();
	}
	/**
	 * Attempts to login no more than 3 times
	 */
	public boolean forum_Login() throws BadCredentials, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = "";
			for(int i = 0;i < 3;i++){
				errorMsg = parseResponse(forum_LoginDirect());if(errorMsg == null){errorMsg = "";}
				if(errorMsg.equals("redirect_login")){//if login is successful
					setConnected(true);
					setLoggedin(true);
					return true;
				}
				else if(errorMsg.equals("badlogin_strikes_passthru")){
					break;
				}
			}
			setLoggedin(false);
			if(errorMsg.equals("badlogin_strikes_passthru")){
				throw new BadCredentials();
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Login using the preset credentials*/
	private LinkedTreeMap<String, Object> forum_LoginDirect(){
		return forum_LoginDirect(this.username, this.password);
	}
	/**Login using defined credentials
	 * @param username
	 * @param password
	 * @return
	 */
	private LinkedTreeMap<String, Object> forum_LoginDirect(String username, String password){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("vb_login_username", username);
		params.put("vb_login_password", password);
		return callMethod("login_login", params, true);
	}
	/**
	 * NOT COMPLETE - functionality is unknown
	 * @return
	 * @throws BadCredentials
	 * @throws VBulletinAPIException
	 */
	public boolean forum_Logout() throws BadCredentials, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = "";
			for(int i = 0;i < 3;i++){
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("vb_login_username", username);
				params.put("vb_login_password", password);
				errorMsg = parseResponse(callMethod("login_logout", params, true));if(errorMsg == null){errorMsg = "";}
				if(errorMsg.equals("redirect_login")){//if login is successful
					setConnected(true);
					setLoggedin(true);
					return true;
				}
				else if(errorMsg.equals("badlogin_strikes_passthru")){
					break;
				}
			}
			setLoggedin(false);
			if(errorMsg.equals("badlogin_strikes_passthru")){
				throw new BadCredentials();
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Returns Member information
	 * @return Member
	 * @throws NoPermissionLoggedout when only logged in members may view
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Member forum_ViewMember(String user)throws NoPermissionLoggedout, VBulletinAPIException{
		return forum_ViewMember(user, 0);
	}
	/**Returns Member information
	 * @return Member
	 * @throws NoPermissionLoggedout when only logged in members may view
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private Member forum_ViewMember(String user, int loop) throws NoPermissionLoggedout, VBulletinAPIException{
		if(isConnected()){
			Member member = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("username", user);
			if(loop < 4){//no infinite loop by user
				try {
					member = parseViewMember(callMethod("member", params, true));
				} catch (InvalidAccessToken | NoPermissionLoggedout e) {
					forum_Login();
					if(isLoggedin()){
						return forum_ViewMember(user, loop);
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return forum_ViewMember(user, loop);
				}
				return member;
			}
			throw new VBulletinAPIException("vBulletin API Unknown Error ");
		}
		throw new NoConnectionException();
	}
	/**
	 * Gets the API access token.
	 *
	 * @return the API access token
	 */
	private String getAPIAccessToken() {
		return apiAccessToken;
	}
	/**
	 * Gets the API client ID.
	 *
	 * @return the API client ID
	 */
	public String getAPIClientID() {
		return apiClientID;
	}
	/**
	 * Gets the API key.
	 *
	 * @return the API key
	 */
	private String getAPIkey() {
		return apikey;
	}
	/**
	 * Gets the URL of api.php
	 *
	 * @return the URL
	 */
	public String getAPIURL() {
		return apiURL;
	}
	/**
	 * Gets the secret value.
	 *
	 * @return the secret value
	 */
	private String getSecret() {
		return secret;
	}
	/**
	 * Inits the connection to SC2MafiaForum and retrieves the secret
	 *
	 * @param clientname
	 *            the name of the client
	 * @param clientversion
	 *            the version of the client
	 * @param platformname
	 *            the name of the platform this application is running on
	 * @param platformversion
	 *            the version of the platform this application is running on
	 * @param uniqueid
	 *            the unique ID of the client. This should be different for each
	 *            user, and remain the same across sessions
	 * @return the array returned by the server
	 * @throws IOException
	 *             If the URL is wrong, or a connection is unable to be made for
	 *             whatever reason.
	 */
	private LinkedTreeMap<String, Object> init(String clientname, String clientversion,String platformname, String platformversion, String uniqueid, boolean loggedIn){// throws IOException{
		try{
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("clientname", clientname);
			params.put("clientversion", clientversion);
			params.put("platformname", platformname);
			params.put("platformversion", platformversion);
			params.put("uniqueid", uniqueid);
			LinkedTreeMap<String, Object> initvalues = callMethod("api_init", params, loggedIn);
			setAPIAccessToken((String) initvalues.get("apiaccesstoken"));
			apiClientID = String.valueOf(initvalues.get("apiclientid"));
			//if((String) initvalues.get("secret") != null){secret = (String) initvalues.get("secret");}
			setSecret((String) initvalues.get("secret"));
			return initvalues;
		}
		catch(Exception e){
			return null;
		}
	}
	/**
	 * Returns if connected into vBulletin forum
	 */
	public boolean isConnected(){
		return CONNECTED;
	}
	/**Is the username and password set?
	 * @return
	 */
	public boolean isCredentialsSet(){
		if(username != null && password != null){
			if(!username.isEmpty() && !password.isEmpty()){
				return true;
			}
		}
		return false;
	}
	/**
	 * Returns if logged into vBulletin forum
	 */
	public boolean isLoggedin(){
		return LOGGEDIN;
	}
	/**Returns a Forum containing all the Forums within
	 * @param response
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings("rawtypes")
	private Forum parseForumDisplay(LinkedTreeMap<String, Object> response) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		Forum forum = new Forum();
		if(response != null){
			if(response.containsKey("response")){
				LinkedTreeMap response2 = (LinkedTreeMap)response.get("response");
				if(response2.containsKey("daysprune")){
					forum.daysprune = Functions.convertToInt(response2.get("daysprune"));
				}
				if(response2.containsKey("limitlower")){
					forum.limitlower = Functions.convertToInt(response2.get("limitlower"));
				}
				if(response2.containsKey("limitupper")){
					forum.limitupper = Functions.convertToInt(response2.get("limitupper"));
				}
				if(response2.containsKey("numberguest")){
					forum.numberguest = Functions.convertToInt(response2.get("numberguest"));
				}
				if(response2.containsKey("numberguest")){
					forum.numberguest = Functions.convertToInt(response2.get("numberguest"));
				}
				if(response2.containsKey("numberregistered")){
					forum.numberregistered = Functions.convertToInt(response2.get("numberregistered"));
				}
				if(response2.containsKey("pagenumber")){
					forum.pagenumber = Functions.convertToInt(response2.get("pagenumber"));
				}
				if(response2.containsKey("perpage")){
					forum.perpage = Functions.convertToInt(response2.get("perpage"));
				}
				if(response2.containsKey("totalmods")){
					forum.totalmods = Functions.convertToInt(response2.get("totalmods"));
				}
				if(response2.containsKey("totalonline")){
					forum.totalonline = Functions.convertToInt(response2.get("totalonline"));
				}
				if(response2.containsKey("totalthreads")){
					forum.totalthreads = Functions.convertToInt(response2.get("totalthreads"));
				}
				//foruminfo {}
				if(response2.containsKey("foruminfo")){
					LinkedTreeMap foruminfo = (LinkedTreeMap)response2.get("foruminfo");
					if(foruminfo.containsKey("forumid")){
						forum.forumid = Functions.convertToInt(foruminfo.get("forumid"));
					}
					if(foruminfo.containsKey("title")){
						forum.title = Functions.convertToString(foruminfo.get("title"));
					}
					if(foruminfo.containsKey("title_clean")){
						forum.title_clean = Functions.convertToString(foruminfo.get("title_clean"));
					}
					if(foruminfo.containsKey("description")){
						forum.description = Functions.convertToString(foruminfo.get("description"));
					}
					if(foruminfo.containsKey("description_clean")){
						forum.description_clean = Functions.convertToString(foruminfo.get("description_clean"));
					}
					if(foruminfo.containsKey("prefixrequired")){
						forum.prefixrequired = Functions.convertToInt(foruminfo.get("prefixrequired"));
					}
				}
				//forumbits []
				if(response2.containsKey("forumbits")){
					System.out.println("forumbits is "+response2.get("forumbits").getClass().getName());
					if(response2.get("forumbits") instanceof ArrayList){//multiple posts
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap<String, Object>> forumbits = (ArrayList<LinkedTreeMap<String, Object>>) response2.get("forumbits");
						for(LinkedTreeMap<String, Object> forumHolder : forumbits){
							forum.subforums.add(parseForumSub(forumHolder));
						}
					}
					else if(response2.get("forumbits") instanceof LinkedTreeMap){//multiple posts
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> forumbit = (LinkedTreeMap<String, Object>) response2.get("forumbits");
						forum.subforums.add(parseForumSub(forumbit));
					}
					/*else if(((LinkedTreeMap)response.get("response")).get("postbits") instanceof LinkedTreeMap){//single post*/
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("errormessage")){
					String theError = "";
					String errorSecond = "";
					String className = response2.get("errormessage").getClass().getName();
					if(className.equals("java.lang.String")){
						theError = (String) response2.get("errormessage");
						if(theError.equals("redirect_postthanks")){//this is for newthread and newpost
							if(response.containsKey("show")){
								if(((LinkedTreeMap)response.get("show")).containsKey("forumid")){
									theError = ""+Functions.convertToInt(((LinkedTreeMap)response.get("show")).get("forumid"));
									//theError += " "+Functions.convertToInt(((LinkedTreeMap)response.get("show")).get("postid"));
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
					errorsCommon(theError);
					System.out.println("responseError  response -> errormessage type unknown: "+className);
					throw new VBulletinAPIException("vBulletin API Unknown Error - "+className);
				}
			}
		}
		if(DEBUG){
			//System.out.println("thread all ->");
			//System.out.println(response.toString());
		}
		return forum;
	}
	/**Returns a Forum containing all the Forums within
	 * @param response
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings("rawtypes")
	private Forum parseForumSub(LinkedTreeMap<String, Object> forumbit) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		Forum forum = new Forum();
		if(forumbit != null){
			if(forumbit.containsKey("parent_is_category")){
				forum.parent_is_category = Functions.convertToBoolean(forumbit.get("parent_is_category"));
			}
			if(forumbit.containsKey("forum")){
				LinkedTreeMap forumdata = (LinkedTreeMap)forumbit.get("forum");
				if(forumdata.containsKey("forumid")){
					forum.forumid = Functions.convertToInt(forumdata.get("forumid"));
				}
				if(forumdata.containsKey("threadcount")){
					forum.threadcount = Functions.convertToInt(forumdata.get("threadcount"));
				}
				if(forumdata.containsKey("replycount")){
					forum.replycount = Functions.convertToInt(forumdata.get("replycount"));
				}
				if(forumdata.containsKey("title")){
					forum.title = Functions.convertToString(forumdata.get("title"));
				}
				if(forumdata.containsKey("title_clean")){
					forum.title_clean = Functions.convertToString(forumdata.get("title_clean"));
				}
				if(forumdata.containsKey("description")){
					forum.description = Functions.convertToString(forumdata.get("description"));
				}
				if(forumdata.containsKey("description_clean")){
					forum.description_clean = Functions.convertToString(forumdata.get("description_clean"));
				}
				if(forumdata.containsKey("statusicon")){
					forum.statusicon = Functions.convertToString(forumdata.get("statusicon"));
				}
				if(forumdata.containsKey("browsers")){
					forum.browsers = Functions.convertToInt(forumdata.get("browsers"));
				}
				
				//if(forumdata.containsKey("subforums")){
				//	forum.subforums = Functions.convertToString(forumdata.get("subforums"));
				//}
				//show{}
				if(forumbit.containsKey("childforumbits")){//yes...within the forumdata if statement
					if(forumbit.get("childforumbits") instanceof ArrayList){
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap<String, Object>> childforumbits = (ArrayList<LinkedTreeMap<String, Object>>) forumbit.get("childforumbits");
						for(LinkedTreeMap<String, Object> childforumHolder : childforumbits){
							forum.subforums.add(parseForumSub(childforumHolder));
						}
					}
					else if(forumbit.get("childforumbits") instanceof LinkedTreeMap){
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> childforumbit = (LinkedTreeMap<String, Object>) forumbit.get("childforumbits");
						forum.subforums.add(parseForumSub(childforumbit));
					}
				}
				if(forumdata.containsKey("subforums")){
					if(forumdata.get("subforums") instanceof ArrayList){
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap<String, Object>> subforums = (ArrayList<LinkedTreeMap<String, Object>>) forumdata.get("subforums");
						for(LinkedTreeMap<String, Object> subforumHolder : subforums){
							forum.subforums.add(parseForumSub(subforumHolder));
						}
					}
					else if(forumdata.get("subforums") instanceof LinkedTreeMap){
						@SuppressWarnings("unchecked")
						LinkedTreeMap<String, Object> subforum = (LinkedTreeMap<String, Object>) forumdata.get("subforums");
						forum.subforums.add(parseForumSub(subforum));
					}
				}
			}
		}
		return forum;
	}
	/**Parses response, designed specifically for gathering the list of all messages. Messages only have the header at this point, the actual message is not included
	 * @param response
	 * @return ArrayList<Message>
	 */
	@SuppressWarnings("rawtypes")
	private ArrayList<Message> parseMessages(LinkedTreeMap<String, Object> response) throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		ArrayList<Message> messages = new ArrayList<Message>();
		if(response != null){
			if(response.containsKey("response")){
				if(((LinkedTreeMap)response.get("response")).containsKey("HTML")){
					LinkedTreeMap HTML = (LinkedTreeMap) ((LinkedTreeMap)response.get("response")).get("HTML");
					if(HTML.containsKey("messagelist_periodgroups")){
						if(HTML.get("messagelist_periodgroups") instanceof LinkedTreeMap){
							LinkedTreeMap messageGroup = (LinkedTreeMap) HTML.get("messagelist_periodgroups");
							if(messageGroup.containsKey("messagesingroup")){
								if((double)(messageGroup.get("messagesingroup"))>0){//if there are messages
									if(messageGroup.containsKey("messagelistbits")){
										if(messageGroup.get("messagelistbits") instanceof LinkedTreeMap){//single message
											Message parsedMessage = new Message();
											LinkedTreeMap message = (LinkedTreeMap) messageGroup.get("messagelistbits");
											parsedMessage.pmid = (String) ((LinkedTreeMap)message.get("pm")).get("pmid");
											parsedMessage.sendtime = (String) ((LinkedTreeMap)message.get("pm")).get("sendtime");
											parsedMessage.statusicon = (String) ((LinkedTreeMap)message.get("pm")).get("statusicon");
											parsedMessage.title = (String) ((LinkedTreeMap)message.get("pm")).get("title");

											parsedMessage.setUserid((String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
											parsedMessage.username = (String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username");
											messages.add(parsedMessage);
										}
										else if(messageGroup.get("messagelistbits") instanceof ArrayList){//multiple messages
											for(Object objInner : (ArrayList) messageGroup.get("messagelistbits")){
												Message parsedMessage = new Message();
												LinkedTreeMap message = (LinkedTreeMap) objInner;
												parsedMessage.pmid = (String) ((LinkedTreeMap)message.get("pm")).get("pmid");
												parsedMessage.sendtime = (String) ((LinkedTreeMap)message.get("pm")).get("sendtime");
												parsedMessage.statusicon = (String) ((LinkedTreeMap)message.get("pm")).get("statusicon");
												parsedMessage.title = (String) ((LinkedTreeMap)message.get("pm")).get("title");

												parsedMessage.setUserid((String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
												parsedMessage.username = (String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username");
												messages.add(parsedMessage);
											}
										}
									}
								}
							}
						}
						else if(HTML.get("messagelist_periodgroups") instanceof ArrayList){
							ArrayList messageGroups = (ArrayList) HTML.get("messagelist_periodgroups");
							for(Object obj : messageGroups){
								LinkedTreeMap messageGroup = (LinkedTreeMap)obj;
								if(messageGroup.containsKey("messagesingroup")){
									if((double)(messageGroup.get("messagesingroup"))>0){//if there are messages
										if(messageGroup.containsKey("messagelistbits")){
											if(messageGroup.get("messagelistbits") instanceof LinkedTreeMap){//single message
												Message parsedMessage = new Message();
												LinkedTreeMap message = (LinkedTreeMap) messageGroup.get("messagelistbits");
												parsedMessage.pmid = (String) ((LinkedTreeMap)message.get("pm")).get("pmid");
												parsedMessage.sendtime = (String) ((LinkedTreeMap)message.get("pm")).get("sendtime");
												parsedMessage.statusicon = (String) ((LinkedTreeMap)message.get("pm")).get("statusicon");
												parsedMessage.title = (String) ((LinkedTreeMap)message.get("pm")).get("title");

												parsedMessage.setUserid((String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
												parsedMessage.username = (String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username");
												messages.add(parsedMessage);
											}
											else if(messageGroup.get("messagelistbits") instanceof ArrayList){//multiple messages
												for(Object objInner : (ArrayList) messageGroup.get("messagelistbits")){
													Message parsedMessage = new Message();
													LinkedTreeMap message = (LinkedTreeMap) objInner;
													parsedMessage.pmid = (String) ((LinkedTreeMap)message.get("pm")).get("pmid");
													parsedMessage.sendtime = (String) ((LinkedTreeMap)message.get("pm")).get("sendtime");
													parsedMessage.statusicon = (String) ((LinkedTreeMap)message.get("pm")).get("statusicon");
													parsedMessage.title = (String) ((LinkedTreeMap)message.get("pm")).get("title");

													parsedMessage.setUserid((String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
													parsedMessage.username = (String) ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username");
													messages.add(parsedMessage);
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("errormessage")){
					String theError = "";
					//String errorSecond = "";
					String className = ((LinkedTreeMap)response.get("response")).get("errormessage").getClass().getName();
					if(className.equals("java.lang.String")){
						theError = ((String) ((LinkedTreeMap)response.get("response")).get("errormessage"));
						if(theError.equals("redirect_postthanks")){//this is for newthread and newpost
							if(response.containsKey("show")){
								if(((LinkedTreeMap)response.get("show")).containsKey("threadid")){
									theError = (String) ((LinkedTreeMap)response.get("show")).get("threadid");
									theError += " "+(double) ((LinkedTreeMap)response.get("show")).get("postid");
								}
							}
						}
					}
					else if(className.equals("java.util.ArrayList")){
						Object[] errors = ((ArrayList) ((LinkedTreeMap)response.get("response")).get("errormessage")).toArray();
						if(errors.length > 0){
							theError = errors[0].toString();
						}
						/*if(errors.length > 1){
							errorSecond = errors[1].toString();
						}*/
					}
					//parse theError here
					errorsCommon(theError);
					System.out.println("responseError  response -> errormessage type unknown: "+className);
					throw new VBulletinAPIException("vBulletin API Unknown Error - "+className);
				}
			}
		}
		System.out.println("message all ->");
		System.out.println(response.toString());
		return messages;
	}
	/**Grabs the 'errormessage' from within the json pulled form callMethod()
	 * Known errors:
	 * 		pm_messagesent = message successfully sent
	 * 		pmrecipientsnotfound = Forum user doesn't exist
	 * 		invalid_accesstoken
	 * @param response data from callMethod()
	 * @return the 'errormessage' inside, if none: null
	 */
	@SuppressWarnings("rawtypes")
	private String parseResponse(LinkedTreeMap<String, Object> response){
		//LinkedTreeMap response = (LinkedTreeMap) response2;
		String theReturn = null;
		String className = null;
		if(response != null){
			if(DEBUG){
				System.out.println("all ->");
				System.out.println(response.toString());
			}
			if(response.containsKey("response")){
				//errormessage
				if(((LinkedTreeMap)response.get("response")).containsKey("errormessage")){
					className = ((LinkedTreeMap)response.get("response")).get("errormessage").getClass().getName();
					if(className.equals("java.lang.String")){
						theReturn = ((String) ((LinkedTreeMap)response.get("response")).get("errormessage"));
						if(theReturn.equals("redirect_postthanks")){//this is for newthread and newpost
							if(response.containsKey("show")){
								if(((LinkedTreeMap)response.get("show")).containsKey("threadid")){
									theReturn = (String) ((LinkedTreeMap)response.get("show")).get("threadid");
									theReturn += " "+(double) ((LinkedTreeMap)response.get("show")).get("postid");
								}
							}
						}
					}
					else if(className.equals("java.util.ArrayList")){
						Object[] errors = ((ArrayList) ((LinkedTreeMap)response.get("response")).get("errormessage")).toArray();
						if(errors.length > 0){
							theReturn = errors[0].toString();
						}
					}
					else{
						System.out.println("responseError  response -> errormessage type unknown: "+className);
					}
				}
				//HTML
				else if(((LinkedTreeMap)response.get("response")).containsKey("HTML")){
					LinkedTreeMap HTML = (LinkedTreeMap) ((LinkedTreeMap)response.get("response")).get("HTML");
					if(HTML.containsKey("totalmessages")){
						theReturn = "totalmessages";
					}
					else if(HTML.containsKey("postbit")){
						if(HTML.get("postbit") instanceof LinkedTreeMap){
							LinkedTreeMap postbit = (LinkedTreeMap) HTML.get("postbit");
							if(postbit.containsKey("post")){
								if(postbit.get("post") instanceof LinkedTreeMap){
									LinkedTreeMap post = (LinkedTreeMap) postbit.get("post");
									if(post.containsKey("message")){
										theReturn = (String) post.get("message");
									}
								}
							}
						}
					}
					else if(HTML.containsKey("postpreview")){
						if(HTML.get("postpreview") instanceof LinkedTreeMap){
							LinkedTreeMap postpreview = (LinkedTreeMap) HTML.get("postpreview");
							if(postpreview.containsKey("errorlist")){
								if(postpreview.get("errorlist") instanceof LinkedTreeMap){
									LinkedTreeMap errorlist = (LinkedTreeMap) postpreview.get("errorlist");
									if(errorlist.containsKey("errors")){
										if(errorlist.get("errors") instanceof ArrayList){
											ArrayList errors = (ArrayList) errorlist.get("errors");
											if(errors.get(0) instanceof ArrayList){
												//response -> postpreview -> errorlist -> errors[0]
												ArrayList errorSub = (ArrayList) errors.get(0);
												theReturn = errorSub.get(0).toString();
											}
										}
									}

								}
							}
						}
					}
				}
				//errorlist
				else if(((LinkedTreeMap)response.get("response")).containsKey("errorlist")){
					ArrayList errorlist = (ArrayList) ((LinkedTreeMap)response.get("response")).get("errorlist");
					System.out.println("Unknown Responses(errorlsit ->): "+errorlist.toString());
				}
				else{//has response..but not common
					System.out.println("Unknown Responses: "+((LinkedTreeMap)response.get("response")).keySet().toString());
				}
			}
			else if(response.containsKey("custom")){
				theReturn = (String) response.get("custom");
			}
			//testing this:

		}
		//Base.Console.debug("SC2Mafia API return error: "+theReturn);
		return theReturn;
	}
	/**Returns a Thread containing all the Posts within(or specified)
	 * @param response
	 * @return
	 * @throws InvalidId ThreadID missing or nonexistent
	 * @throws NoPermissionLoggedout
	 * @throws NoPermissionLoggedin
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings("rawtypes")
	private ForumThread parseThread(LinkedTreeMap<String, Object> response) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		ForumThread thread = new ForumThread();
		if(response != null){
			if(response.containsKey("response")){
				if(((LinkedTreeMap)response.get("response")).containsKey("totalposts")){
					thread.totalposts = new Double((double) ((LinkedTreeMap)response.get("response")).get("totalposts")).intValue();

				}
				if(((LinkedTreeMap)response.get("response")).containsKey("FIRSTPOSTID")){
					thread.FIRSTPOSTID = Functions.convertToInt(((LinkedTreeMap)response.get("response")).get("FIRSTPOSTID"));
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("LASTPOSTID")){
					thread.LASTPOSTID = Functions.convertToInt(((LinkedTreeMap)response.get("response")).get("LASTPOSTID"));
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("pagenumber")){
					thread.pagenumber = Functions.convertToInt(((LinkedTreeMap)response.get("response")).get("pagenumber"));
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("perpage")){
					thread.perpage = Functions.convertToInt(((LinkedTreeMap)response.get("response")).get("perpage"));
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("postbits")){
					if(((LinkedTreeMap)response.get("response")).get("postbits") instanceof ArrayList){//multiple posts
						@SuppressWarnings("unchecked")
						ArrayList<LinkedTreeMap> postbits = (ArrayList<LinkedTreeMap>) ((LinkedTreeMap)response.get("response")).get("postbits");
						for(LinkedTreeMap postHolder : postbits){
							if(postHolder.containsKey("post")){
								Post post = new Post();
								LinkedTreeMap postPost = (LinkedTreeMap) postHolder.get("post");
								if(postPost.containsKey("postid")){
									post.postid = Functions.convertToInt(postPost.get("postid"));
								}
								if(postPost.containsKey("posttime")){
									if(Functions.isInteger((String) postPost.get("posttime"))){
										post.posttime = Long.parseLong((String) postPost.get("posttime"));
									}
								}
								if(postPost.containsKey("threadid")){
									post.threadid = Functions.convertToInt(postPost.get("threadid"));
								}
								if(postPost.containsKey("userid")){
									post.userid = Functions.convertToInt(postPost.get("userid"));
								}
								if(postPost.containsKey("username")){
									post.username = (String) postPost.get("username");
								}
								if(postPost.containsKey("avatarurl")){
									post.avatarurl = (String) postPost.get("avatarurl");
								}
								if(postPost.containsKey("usertitle")){
									post.usertitle = (String) postPost.get("usertitle");
								}
								if(postPost.containsKey("joindate")){
									if(Functions.isInteger((String) postPost.get("joindate"))){
										post.joindate = Long.parseLong((String) postPost.get("joindate"));
									}
								}
								if(postPost.containsKey("title")){
									post.title = (String) postPost.get("title");
								}
								if(postPost.containsKey("isfirstshown")){
									if(Functions.convertToInt(postPost.get("isfirstshown")) == 1){
										post.isfirstshown = true;
									}
									else{post.isfirstshown = false;}
								}
								if(postPost.containsKey("islastshown")){
									if(Functions.convertToInt(postPost.get("islastshown")) == 1){
										post.islastshown = true;
									}
									else{post.islastshown = false;}
								}
								if(postPost.containsKey("message")){
									post.message = (String) postPost.get("message");
								}
								if(postPost.containsKey("message_plain")){
									post.message_plain = (String) postPost.get("message_plain");
								}
								if(postPost.containsKey("message_bbcode")){
									post.message_bbcode = (String) postPost.get("message_bbcode");
								}
								thread.posts.add(post);
							}
						}
					}
					else if(((LinkedTreeMap)response.get("response")).get("postbits") instanceof LinkedTreeMap){//single post
						System.out.println("postbits is map");
						LinkedTreeMap postHolder = (LinkedTreeMap) ((LinkedTreeMap)response.get("response")).get("postbits");
						if(postHolder.containsKey("post")){
							Post post = new Post();
							LinkedTreeMap postPost = (LinkedTreeMap) postHolder.get("post");
							if(postPost.containsKey("postid")){
								post.postid = Functions.convertToInt(postPost.get("postid"));
							}
							if(postPost.containsKey("posttime")){
								if(Functions.isInteger((String) postPost.get("posttime"))){
									post.posttime = Long.parseLong((String) postPost.get("posttime"));
								}
							}
							if(postPost.containsKey("threadid")){
								post.threadid = Functions.convertToInt(postPost.get("threadid"));
							}
							if(postPost.containsKey("userid")){
								post.userid = Functions.convertToInt(postPost.get("userid"));
							}
							if(postPost.containsKey("username")){
								post.username = (String) postPost.get("username");
							}
							if(postPost.containsKey("avatarurl")){
								post.avatarurl = (String) postPost.get("avatarurl");
							}
							if(postPost.containsKey("usertitle")){
								post.usertitle = (String) postPost.get("usertitle");
							}
							if(postPost.containsKey("joindate")){
								if(Functions.isInteger((String) postPost.get("joindate"))){
									post.joindate = Long.parseLong((String) postPost.get("joindate"));
								}
							}
							if(postPost.containsKey("title")){
								post.title = (String) postPost.get("title");
							}
							if(postPost.containsKey("isfirstshown")){
								if(Functions.convertToInt(postPost.get("isfirstshown")) == 1){
									post.isfirstshown = true;
								}
								else{post.isfirstshown = false;}
							}
							if(postPost.containsKey("islastshown")){
								if(Functions.convertToInt(postPost.get("islastshown")) == 1){
									post.islastshown = true;
								}
								else{post.islastshown = false;}
							}
							if(postPost.containsKey("message")){
								post.message = (String) postPost.get("message");
							}
							if(postPost.containsKey("message_plain")){
								post.message_plain = (String) postPost.get("message_plain");
							}
							if(postPost.containsKey("message_bbcode")){
								post.message_bbcode = (String) postPost.get("message_bbcode");
							}
							thread.posts.add(post);
						}
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
					errorsCommon(theError);
					System.out.println("responseError  response -> errormessage type unknown: "+className);
					throw new VBulletinAPIException("vBulletin API Unknown Error - "+className);
				}
			}
		}
		if(DEBUG){
			System.out.println("thread all ->");
			System.out.println(response.toString());
		}
		return thread;
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
	@SuppressWarnings("rawtypes")
	private Member parseViewMember(LinkedTreeMap<String, Object> response) throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		Member theReturn = new Member();
		if(response != null){
			String className = null;
			if(response.containsKey("response")){
				//response -> prepared
				if(((LinkedTreeMap)response.get("response")).containsKey("prepared")){
					className = ((LinkedTreeMap)response.get("response")).get("prepared").getClass().getName();
					if(className.equals("com.google.gson.internal.LinkedTreeMap")){
						LinkedTreeMap prepared = (LinkedTreeMap) ((LinkedTreeMap)response.get("response")).get("prepared");
						if(prepared.containsKey("username")){
							className = prepared.get("username").getClass().getName();
							if(className.equals("java.lang.String")){
								theReturn.username = (String) prepared.get("username");
							}
						}
						if(prepared.containsKey("userid")){
							className = prepared.get("userid").getClass().getName();
							if(className.equals("java.lang.String")){
								theReturn.userid = (String) prepared.get("userid");
							}
						}
						if(prepared.containsKey("public String username;")){
							className = prepared.get("joindate").getClass().getName();
							if(className.equals("java.lang.String")){
								theReturn.joindate = (String) prepared.get("joindate");
							}
						}
						if(prepared.containsKey("avatarurl")){
							className = prepared.get("avatarurl").getClass().getName();
							if(className.equals("java.lang.String")){
								theReturn.avatarurl = (String) prepared.get("avatarurl");
							}
						}
					}
				}
				if(((LinkedTreeMap)response.get("response")).containsKey("errormessage")){
					String theError = "";
					className = ((LinkedTreeMap)response.get("response")).get("errormessage").getClass().getName();
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
					}
					errorsCommon(theError);
					System.out.println("responseError  response -> errormessage type unknown: "+className);
					throw new VBulletinAPIException("vBulletin API Unknown Error - "+className);
				}
			}
		}
		if(DEBUG){
			System.out.println("member all ->");
			System.out.println(response.toString());
		}
		return theReturn;
	}
	/**Attempts to empty the primary PM Inbox
	 * @param folderid which folder to empty
	 * @return true on success
	 * @throws InvalidId on non existent Private Message Folder
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to empty private message folders
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean pm_EmptyInbox() throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return  pm_EmptyInbox(0);
	}
	/**Attempts to empty a PM Inbox. folderid 0 is the primary inbox.
	 * @param folderid which folder to empty
	 * @return true on success
	 * @throws InvalidId on non existent Private Message Folder
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to empty private message folders
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean pm_EmptyInbox(int folderid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return  pm_EmptyInbox(folderid, 9876543210L);
	}
	/**Attempts to empty a PM Inbox of all messages before the given date. folderid 0 is the primary inbox.
	 * @param folderid which folder to empty
	 * @param dateToDelete delete all before this date(forum time)
	 * @return true on success
	 * @throws InvalidId on non existent Private Message Folder
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to empty private message folders
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean pm_EmptyInbox(int folderid, long dateToDelete) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return  pm_EmptyInbox(""+folderid, ""+dateToDelete, 0);
	}
	/**Attempts to empty a PM Inbox of all messages before the given date. folderid 0 is the primary inbox.
	 * @param folderid which folder to empty
	 * @param dateToDelete delete all before this date(forum time)
	 * @param loop how many iretations it went through
	 * @return true on success
	 * @throws InvalidId on non existent Private Message Folder
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to empty private message folders
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean pm_EmptyInbox(String folderid, String dateToDelete, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();//150885
			//params.put("dateline", "9876543210");
			params.put("dateline", dateToDelete);
			params.put("folderid", folderid);
			errorMsg = parseResponse(callMethod("private_confirmemptyfolder", params, true));
			if(loop < 4){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.equals("pm_messagesdeleted")){//success
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						forum_Login();
						if(isLoggedin()){
							return pm_EmptyInbox(folderid, dateToDelete, loop);
						}
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return pm_EmptyInbox(folderid, dateToDelete, loop);
					}
				}
			}
			if(errorMsg.equals("invalidid")){
				throw new InvalidId("folder");
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Returns list of PMs in the primary inbox
	 * @return
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ArrayList<Message> pm_ListPMs() throws NoPermissionLoggedout, VBulletinAPIException{
		return pm_ListPMs(0);
	}
	/**Returns list of PMs in the primary inbox
	 * @return
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private ArrayList<Message> pm_ListPMs(int loop) throws NoPermissionLoggedout, VBulletinAPIException{
		if(isConnected()){
			ArrayList<Message> msgList = new ArrayList<Message>();
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			if(loop < 4){//no infinite loop by user
				try {
					msgList = parseMessages(callMethod("private_messagelist", params, true));
					for(Message msg: msgList){
						msg.message = pm_ViewPM(msg.pmid);
					}
				} catch (InvalidAccessToken | NoPermissionLoggedout e) {
					forum_Login();
					if(isLoggedin()){
						return pm_ListPMs(loop);
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return pm_ListPMs(loop);
				}
				Collections.reverse(msgList);//reverse so it's order is oldest to newest
				return msgList;
			}
			parseMessages(callMethod("private_messagelist", params, true));
		}
		throw new NoConnectionException();
	}
	/**Sends a message to the 'user'. Does not add the account's signature.
	 * @param user
	 * @param title subject
	 * @param message
	 * @return "pm_messagesent" on success
	 * @throws PMRecipTurnedOff when the recipient is not allowing private messages
	 * @throws PMRecipientsNotFound when the user does not exist
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to send private messages
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean pm_SendNew(String user,String title,String message) throws PMRecipTurnedOff, PMRecipientsNotFound, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return pm_SendNew( user, title, message, false, 0);
	}
	/**Sends a message to the 'user'.
	 * @param user
	 * @param title subject
	 * @param message
	 * @param signature post signature
	 * @return "pm_messagesent" on success
	 * @throws PMRecipTurnedOff when the recipient is not allowing private messages
	 * @throws PMRecipientsNotFound when the user does not exist
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to send private messages
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean pm_SendNew(String user,String title,String message, boolean signature) throws PMRecipTurnedOff, PMRecipientsNotFound, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return pm_SendNew( user, title, message, signature, 0);
	}
	/**Sends a message to the 'user'.
	 * pmfloodcheck = too many PMs too fast
	 * @param user
	 * @param title subject
	 * @param message
	 * @param signature post signature
	 * @param loop how many iterations it went through
	 * @return "pm_messagesent" on success
	 * @throws PMRecipTurnedOff when the recipient is not allowing private messages
	 * @throws PMRecipientsNotFound when the user does not exist
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to send private messages
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean pm_SendNew(String user,String title,String message, boolean signature, int loop) throws PMRecipTurnedOff, PMRecipientsNotFound, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){//TODO add Exception for flood checks
			loop++;
			String errorMsg;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("title", title);
			params.put("message", message);
			params.put("recipients", user);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("private_insertpm", params, true));
			if(loop < 4){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.equals("pm_messagesent")){
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")||errorMsg.equals("invalid_api_signature")){
						forum_Login();
						if(isLoggedin()){
							return pm_SendNew(user, title, message, signature,loop);
						}
						//return errorMsg;
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return pm_SendNew( user, title, message, signature,loop);
					}
				}
			}
			if(errorMsg.equals("pmrecipturnedoff")){
				throw new PMRecipTurnedOff();
			}
			else if(errorMsg.equals("pmrecipientsnotfound")){
				throw new PMRecipientsNotFound();
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Grabs the message from the PM specified by the pmID
	 * @param pmId
	 * @return message text as String
	 * @throws InvalidId on non existent Private Message
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public String pm_ViewPM(String pmId) throws InvalidId, NoPermissionLoggedout, VBulletinAPIException{
		return pm_ViewPM(pmId, 0);
	}
	/**Grabs the message from the PM specified by the pmID
	 * @param pmId
	 * @param loop increasing int to prevent infinite loops
	 * @return message text as String
	 * @throws InvalidId on non existent Private Message
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private String pm_ViewPM(String pmId, int loop) throws InvalidId, NoPermissionLoggedout, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			if(pmId != null){
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("pmid", pmId);
				errorMsg = parseResponse(callMethod("private_showpm", params, true));
				if(loop < 4){//no infinite loop by user
					if(errorMsg != null){
						if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(isLoggedin()){
								return pm_ViewPM(pmId, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return pm_ViewPM(pmId, loop);
						}
						else{//success
							return errorMsg;
						}
					}
				}
				else if(errorMsg.equals("invalidid")){
					throw new InvalidId("private message");
				}
				errorsCommon(errorMsg);
			}
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Attempts to edit a post based on the post id, does not post the user's signature
	 * @param postid
	 * @param message
	 * @return true on success
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existent Post
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to edit this post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean post_Edit(int postid,String message) throws ThreadClosed, InvalidId,NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return post_Edit(""+postid, message, false);
	}
	/**Attempts to edit a post based on the post id
	 * @param postid
	 * @param message
	 * @param signature post signature
	 * @return true on success
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existent Post
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to edit this post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean post_Edit(int postid,String message, boolean signature) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return post_Edit(""+postid, message, signature);
	}
	/**Attempts to edit a post based on the post id
	 * @param postid
	 * @param message
	 * @param signature post signature
	 * @return true on success
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existent Post
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to edit this post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean post_Edit(String postid,String message, boolean signature) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return post_Edit(postid, message, signature, 0);
	}
	/**Attempts to edit a post based on the post id
	 * @param postid
	 * @param message
	 * @param signature post signature
	 * @param loop how many iterations it went through
	 * @return true on success
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existent Post
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to edit this post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean post_Edit(String postid,String message, boolean signature, int loop) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("postid", postid);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("editpost_updatepost", params, true));
			if(loop < 4){
				if(errorMsg != null){
					if(errorMsg.equals("redirect_editthanks")){//success
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						forum_Login();
						if(isLoggedin()){
							return post_Edit(postid, message, signature, loop);
						}
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return post_Edit(postid, message, signature, loop);
					}
				}
			}
			if(errorMsg.equals("threadclosed")){
				throw new ThreadClosed();
			}
			else if(errorMsg.equals("invalidid")){
				throw new InvalidId("post");
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Attempts to post a new reply in said Thread, does not post the user's signature
	 * @param threadid
	 * @param message
	 * @return int[0] = threadid / int[1] = postid
	 * @throws ThreadClosed when attempting to post in a closed thread
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out, and does not accept annon posts
	 * @throws NoPermissionLoggedin when account does not have permission to post in this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] post_New(int threadid,String message) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return post_New(""+threadid, message, false);
	}
	/**Attempts to post a new reply in said Thread
	 * @param threadid
	 * @param message
	 * @param signature post signature
	 * @return int[0] = threadid / int[1] = postid
	 * @throws ThreadClosed when attempting to post in a closed thread
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out, and does not accept annon posts
	 * @throws NoPermissionLoggedin when account does not have permission to post in this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] post_New(int threadid,String message, boolean signature) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return post_New(""+threadid, message, signature);
	}
	/**Attempts to post a new reply in said Thread
	 * @param threadid
	 * @param message
	 * @return int[0] = threadid / int[1] = postid
	 * @throws ThreadClosed when attempting to post in a closed thread
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out, and does not accept annon posts
	 * @throws NoPermissionLoggedin when account does not have permission to post in this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] post_New(String threadid,String message, boolean signature) throws ThreadClosed, InvalidId,NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return post_New(threadid, message, signature, 0);
	}
	/**Attempts to post a new reply in said Thread
	 * @param threadid
	 * @param message
	 * @param signature post signature
	 * @param loop how many iterations it went through
	 * @return int[0] = threadid / int[1] = postid
	 * @throws ThreadClosed when attempting to post in a closed thread
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out, and does not accept annon posts
	 * @throws NoPermissionLoggedin when account does not have permission to post in this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private int[] post_New(String threadid,String message, boolean signature, int loop) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			loop++;
			String errorMsg;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("newreply_postreply", params, true));
			if(loop < 4){
				if(errorMsg != null){
					if(Functions.isInteger(errorMsg.substring(0, 1))){//success
						if(errorMsg.contains(" ")){
							String[] ids = errorMsg.split(" ");
							ids[1] = ids[1].substring(0, ids[1].length() - 2);
							int[] theReturn = new int[2];
							if(Functions.isInteger(ids[0])){
								theReturn[0] = (Integer.parseInt(ids[0]));
							}
							if(Functions.isInteger(ids[1])){
								theReturn[1] = (Integer.parseInt(ids[1]));
							}
							return theReturn;
						}
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						forum_Login();
						if(isConnected()){
							return post_New(threadid, message, signature, loop);
						}
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return post_New(threadid, message, signature, loop);
					}
				}
			}
			if(errorMsg.equals("threadclosed")){
				throw new ThreadClosed();
			}
			else if(errorMsg.equals("invalidid")){
				throw new InvalidId("thread");
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	public void run(){
		Properties props = System.getProperties();
		//String errorMsg;
		//handshake with the forum
		if((parseResponse(init(clientname, clientversion, props.getProperty("os.name"),props.getProperty("os.version"),Integer.toString(props.hashCode()),false))) == null){
			//Base.Console.config("SC2Mafia Forum API connected.");
			setConnected(true);
			if(isCredentialsSet()){//only try to login if the user/pass is set
				try {
					forum_Login();//attempt to login
				}catch (VBulletinAPIException e) {e.printStackTrace();}
			}
		}
		else{
			setConnected(false);
		}
	}
	/**
	 * Sets the API access token. You shouldn't need to use this if you use the
	 * init function.
	 *
	 * @param apiAccessToken
	 *            the new API access token
	 */
	private void setAPIAccessToken(String apiAccessToken) {
		this.apiAccessToken = apiAccessToken;
	}
	/**
	 * Sets the API client ID. You shouldn't need to use this if you use the
	 * init function.
	 *
	 * @param apiClientID
	 *            the new API client ID
	 */
	public void setAPIClientID(String apiClientID) {
		this.apiClientID = apiClientID;
	}
	/**
	 * Sets the API key.
	 *
	 * @param apikey
	 *            the new API key
	 */
	public void setAPIkey(String apikey) {
		this.apikey = apikey;
	}
	/**
	 * Sets the URL of api.php
	 *
	 * @param apiURL
	 *            the new URL
	 */
	public void setAPIURL(String apiURL) {
		this.apiURL = apiURL;
	}
	/**
	 * Sets if the API successfully connected to the Forum
	 */
	private void setConnected(boolean arg){
		this.CONNECTED = arg;
	}
	/**
	 * Sets if the API successfully logged into the Forum
	 */
	private void setLoggedin(boolean arg){
		this.LOGGEDIN = arg;
	}
	/**Sets the password to login
	 * @param pass
	 */
	public void setPassword(String pass) {
		this.password = pass;
	}
	/**
	 * Sets the secret value. You shouldn't need to use this if you use the init
	 * function.
	 *
	 * @param secret
	 *            the new secret value
	 */
	private void setSecret(String secret) {
		this.secret = secret;
	}
	/**Sets the username to login
	 * @param user
	 */
	public void setUsername(String user) {
		this.username = user;
	}
	/**Attempts to close a Thread in the forum
	 * @param threadid
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Close(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Close(""+threadid);
	}
	/**Attempts to close a Thread in the forum
	 * @param threadid
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Close(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Close(threadid, 0);
	}
	/**Attempts to close a Thread in the forum
	 * @param threadid
	 * @param loop
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean thread_Close(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_close", params, true));
			if(loop < 4){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(isLoggedin()){
								return thread_Close(threadid, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return thread_Close(threadid, loop);
						}
					}
				}
			}
			if(errorMsg.equals("invalidid")){
				throw new InvalidId("thread");
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Attempts to delete a Thread in the forum
	 * @param threadid
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Delete(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Delete(""+threadid);
	}
	/**Attempts to delete a Thread in the forum
	 * @param threadid
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Delete(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Delete(threadid, 0);
	}
	/**Attempts to delete a Thread in the forum
	 * @param threadid
	 * @param loop how many iterations it went through
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean thread_Delete(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_dodeletethreads", params, true));
			if(loop < 4){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(isLoggedin()){
								return thread_Delete(threadid, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return thread_Delete(threadid, loop);
						}
					}
				}
			}
			if(errorMsg.equals("invalidid")){
				throw new InvalidId("thread");
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use. Does not include the user's signature.
	 * @param forumid
	 * @param subject
	 * @param message
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] thread_New(int forumid,String subject,String message) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_New(""+forumid,subject,message, false);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use.
	 * @param forumid
	 * @param subject
	 * @param message
	 * @param signature post signature
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] thread_New(int forumid,String subject,String message, boolean signature) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_New(""+forumid,subject,message, signature);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use.
	 * @param forumid
	 * @param subject
	 * @param message
	 * @param signature post signature
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] thread_New(String forumid,String subject,String message, boolean signature) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_New(forumid,subject,message, signature, 0);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use.
	 * @param forumid
	 * @param subject
	 * @param message
	 * @param signature post signature
	 * @param loop how many iterations it went through
	 * @return int[0] = threadid / int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private int[] thread_New(String forumid,String subject,String message, boolean signature, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("forumid", forumid);
			params.put("subject", subject);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("newthread_postthread", params, true));
			if(loop < 4){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(Functions.isInteger(errorMsg.substring(0, 1))){//success
							if(errorMsg.contains(" ")){
								String[] ids = errorMsg.split(" ");
								ids[1] = ids[1].substring(0, ids[1].length() - 2);
								int[] theReturn = new int[2];
								if(Functions.isInteger(ids[0])){
									theReturn[0] = (Integer.parseInt(ids[0]));
								}
								if(Functions.isInteger(ids[1])){
									theReturn[1] = (Integer.parseInt(ids[1]));
								}
								return theReturn;
							}
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(isLoggedin()){
								return thread_New(forumid, subject, message, signature, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return thread_New(forumid, subject, message, signature, loop);
						}
					}
				}
			}
			if(errorMsg.equals("invalidid")){
				throw new InvalidId("forum");
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Attempts to open a Thread in the forum
	 * @param threadid
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Open(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Open(""+threadid);
	}
	/**Attempts to open a Thread in the forum
	 * @param threadid
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Open(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Open(threadid, 0);
	}
	/**Attempts to open a Thread in the forum
	 * @param threadid
	 * @param loop how many iterations it went through
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean thread_Open(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_open", params, true));
			if(loop < 4){//no inifinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(isLoggedin()){
								return thread_Open(threadid, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return thread_Open(threadid, loop);
						}
					}
				}
			}
			if(errorMsg.equals("invalidid")){
				throw new InvalidId("thread");
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
		}
		throw new NoConnectionException();
	}
	/**Attempts to view a thread and all the posts of page 1. Number of posts per page varies on the account settings.
	 * @param threadid the thread to view
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread thread_View(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_View(""+threadid, null, null);
	}
	/**Attempts to view a thread and all the posts of which page the postid is specified is on.
	 * @param threadid the thread to view
	 * @param page determines which page to view.
	 * @param perpage determines how many posts to view per page(alters the results of the page parameter)
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread thread_View(int threadid, int page, int perpage) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_View(""+threadid, ""+page, ""+perpage);
	}
	/**Attempts to view a thread and all the posts of which page the postid is specified is on.
	 * @param threadid the thread to view
	 * @param page determines which page to view.
	 * @param perpage determines how many posts to view per page(alters the results of the page parameter)
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread thread_View(String threadid, String page, String perpage) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_View(threadid, page, perpage, null, 0);
	}
	/**Attempts to view a thread and all the posts of which page the postid is specified is on.
	 * @param threadid the thread to view
	 * @param page determines which page to view.
	 * @param perpage determines how many posts to view per page(alters the results of the page parameter)
	 * @param postid determines which page to view based on postid, should not be used with he page parameter
	 * @param loop how many iterations it went through
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private ForumThread thread_View(String threadid, String page, String perpage, String postid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			ForumThread thread = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			if(postid != null){params.put("p", postid);}
			if(page != null){params.put("page", page);}
			if(perpage != null){params.put("perpage", perpage);}
			if(loop < 4){//no infinite loop by user
				try {
					thread = parseThread(callMethod("showthread", params, true));
				} catch (InvalidAccessToken e) {
					forum_Login();
					if(isLoggedin()){
						return thread_View(threadid, page, perpage, postid, loop);
					}
					throw e;
				} catch (NoPermissionLoggedout e) {
					forum_Login();
					if(isLoggedin()){
						return thread_View(threadid, page, perpage, postid, loop);
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return thread_View(threadid, page, perpage, postid, loop);
				}
				return thread;
			}
			parseThread(callMethod("showthread", params, true));
		}
		throw new NoConnectionException();
	}
	/**
	 * @param threadid the thread to search
	 * @return null when the post cannot be found in the thread
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread and post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Post thread_ViewLastPost(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		boolean success = false;
		ForumThread thread = null;
		thread = thread_View(threadid, 1, 1);//just the first post
		success = true;
		if(success){
			success = false;
			thread = thread_View(threadid, thread.totalposts, 1);
			success = true;
			if(success){
				for(Post post : thread.posts){
					if(post.islastshown){
						return post;
					}
				}
			}
		}
		return null;
	}
	/**Attempts to view a post residing in a thread. The correct threadid is required as well
	 * @param threadid the thread the post resides in
	 * @param postid the post to view
	 * @return null when the post cannot be found in the thread
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread and post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Post thread_ViewPost(int threadid, int postid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		ForumThread thread = thread_View(""+threadid, null, "1" , ""+postid, 0);
		for(Post post : thread.posts){
			if(post.postid == postid){
				return post;
			}
		}
		return null;
	}	
	
}
