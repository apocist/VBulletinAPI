package com.inverseinnovations.VBulletinAPI;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
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
	final public static boolean DEBUG = true;
	final public static double VERSION = 0.4;
	final public int loopRequest = 4;//number of times to retry the api call

	private String apiAccessToken;
	private String apiClientID;

	private String apikey;
	private String apiURL;
	private String clientname;
	private String clientversion;
	private boolean CONNECTED = false;

	private boolean LOGGEDIN = false;
	
	private String password;//TODO need a more secure storage
	private String secret;
	private String username;
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
	protected static void errorsCommon(String errorMsg) throws InvalidAPISignature, NoPermissionLoggedout, NoPermissionLoggedin, InvalidAccessToken, APISocketTimeoutException, APIIOException, APIIllegalStateException{
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
	/**Parses response, designed specifically for gathering the list of all messages. Messages only have the header at this point, the actual message is not included
	 * @param response from callMethod
	 * @return ArrayList<Message>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static ArrayList<Message> parseMessages(LinkedTreeMap<String, Object> response) throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		ArrayList<Message> messages = new ArrayList<Message>();
		if(response != null){
			if(response.containsKey("response")){
				//Need more object Data Type checks
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("HTML")){
					LinkedTreeMap<String, Object> HTML = (LinkedTreeMap<String, Object>) response2.get("HTML");
					if(HTML.containsKey("messagelist_periodgroups")){
						if(HTML.get("messagelist_periodgroups") instanceof LinkedTreeMap){
							LinkedTreeMap<String, Object> messageGroup = (LinkedTreeMap<String, Object>) HTML.get("messagelist_periodgroups");
							if(messageGroup.containsKey("messagesingroup")){
								if((double)(messageGroup.get("messagesingroup"))>0){//if there are messages
									if(messageGroup.containsKey("messagelistbits")){
										if(messageGroup.get("messagelistbits") instanceof LinkedTreeMap){//single message
											Message parsedMessage = new Message();
											LinkedTreeMap<String, Object> message = (LinkedTreeMap<String, Object>) messageGroup.get("messagelistbits");
											LinkedTreeMap<String, Object> pm = (LinkedTreeMap<String, Object>) message.get("messagelistbits");
											parsedMessage.pmid = Functions.convertToInt(pm.get("pmid"));
											parsedMessage.sendtime = Functions.convertToInt(pm.get("sendtime"));
											parsedMessage.statusicon = Functions.convertToString(pm.get("statusicon"));
											parsedMessage.title = Functions.convertToString(pm.get("title"));

											parsedMessage.userid = Functions.convertToInt( ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
											parsedMessage.username = Functions.convertToString(  ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username"));
											messages.add(parsedMessage);
										}
										else if(messageGroup.get("messagelistbits") instanceof ArrayList){//multiple messages
											for(LinkedTreeMap<String, Object> message : (ArrayList<LinkedTreeMap<String, Object>>) messageGroup.get("messagelistbits")){
												Message parsedMessage = new Message();
												LinkedTreeMap<String, Object> pm = (LinkedTreeMap<String, Object>) message.get("messagelistbits");
												parsedMessage.pmid = Functions.convertToInt(pm.get("pmid"));
												parsedMessage.sendtime = Functions.convertToInt(pm.get("sendtime"));
												parsedMessage.statusicon = Functions.convertToString(pm.get("statusicon"));
												parsedMessage.title = Functions.convertToString(pm.get("title"));

												parsedMessage.userid = Functions.convertToInt( ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
												parsedMessage.username = Functions.convertToString(  ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username"));
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
												LinkedTreeMap<String, Object> message = (LinkedTreeMap<String, Object>) messageGroup.get("messagelistbits");
												LinkedTreeMap<String, Object> pm = (LinkedTreeMap<String, Object>) message.get("messagelistbits");
												parsedMessage.pmid = Functions.convertToInt(pm.get("pmid"));
												parsedMessage.sendtime = Functions.convertToInt(pm.get("sendtime"));
												parsedMessage.statusicon = Functions.convertToString(pm.get("statusicon"));
												parsedMessage.title = Functions.convertToString(pm.get("title"));

												parsedMessage.userid = Functions.convertToInt( ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
												parsedMessage.username = Functions.convertToString(  ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username"));
												messages.add(parsedMessage);
											}
											else if(messageGroup.get("messagelistbits") instanceof ArrayList){//multiple messages
												for(LinkedTreeMap<String, Object> message : (ArrayList<LinkedTreeMap<String, Object>>) messageGroup.get("messagelistbits")){
													Message parsedMessage = new Message();
													LinkedTreeMap<String, Object> pm = (LinkedTreeMap<String, Object>) message.get("messagelistbits");
													parsedMessage.pmid = Functions.convertToInt(pm.get("pmid"));
													parsedMessage.sendtime = Functions.convertToInt(pm.get("sendtime"));
													parsedMessage.statusicon = Functions.convertToString(pm.get("statusicon"));
													parsedMessage.title = Functions.convertToString(pm.get("title"));

													parsedMessage.userid = Functions.convertToInt( ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("userid"));
													parsedMessage.username = Functions.convertToString(  ((LinkedTreeMap)((LinkedTreeMap) message.get("userbit")).get("userinfo")).get("username"));
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
				Functions.responseErrorCheck(response);
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
	private static String parseResponse(LinkedTreeMap<String, Object> response){
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
		this.setAPIURL(apiURL);
		this.setAPIkey(apikey);
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
			conn.setReadTimeout(10000);//set timeout to 10 seconds
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
					String json = Functions.inputStreamToString(is);

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
					catch(Exception e){//TODO need to check what kind of errors...
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
	/**Displays homepage related information
	 * @return
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumHome forumHome() throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return forumHome(0);
	}
	/**Displays homepage related information
	 * @param loop how many iterations it went through
	 * @return
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private ForumHome forumHome(int loop) throws NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{//TODO think that i forgot that I am grabbing the wrong info..double check
		if(isConnected()){
			ForumHome forum = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			if(loop <= loopRequest){//no infinite loop by user
				try {
					forum = new ForumHome().parse(callMethod("forum", params, true));
				} catch (InvalidAccessToken e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return forumHome(loop);
						}
					}
					throw e;
				} catch (NoPermissionLoggedout e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return forumHome(loop);
						}
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return forumHome(loop);
				}
				return forum;
			}
			return new ForumHome().parse(callMethod("forum", params, true));
		}
		throw new NoConnectionException();
	}
	/**Displays forum data and forums/threads within
	 * @param forumid the forum to view
	 * @return
	 * @throws InvalidId Forum does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Forum forumView(int forumid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return forumView(""+forumid);
	}
	/**Displays forum data and forums/threads within
	 * @param forumid the forum to view
	 * @return
	 * @throws InvalidId Forum does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Forum forumView(String forumid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return forumView(forumid, 0);
	}
	/**Displays forum data and forums/threads within
	 * @param forumid the forum to view
	 * @param loop how many iterations it went through
	 * @return
	 * @throws InvalidId Forum does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private Forum forumView(String forumid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			Forum forum = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("forumid", forumid);
			if(loop <= loopRequest){//no infinite loop by user
				try {
					forum = new Forum().parse(callMethod("forumdisplay", params, true));
				} catch (InvalidAccessToken e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return forumView(forumid, loop);
						}
					}
					throw e;
				} catch (NoPermissionLoggedout e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return forumView(forumid, loop);
						}
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return forumView(forumid, loop);
				}
				return forum;
			}
			return new Forum().parse(callMethod("forumdisplay", params, true));
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
	protected String getAPIClientID() {
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
	protected String getAPIURL() {
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
	public synchronized boolean isConnected(){
		return CONNECTED;
	}
	/**Is the username and password set?
	 * @return
	 */
	public synchronized boolean isCredentialsSet(){
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
	public synchronized boolean isLoggedin(){
		return LOGGEDIN;
	}
	/**
	 * Attempts to login no more than 3 times
	 */
	public boolean login() throws BadCredentials, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = "";
			for(int i = 0;i < 3;i++){
				errorMsg = parseResponse(loginDirect());if(errorMsg == null){errorMsg = "";}
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
	private LinkedTreeMap<String, Object> loginDirect(){
		return loginDirect(this.username, this.password);
	}
	/**Login using defined credentials
	 * @param username
	 * @param password
	 * @return
	 */
	private LinkedTreeMap<String, Object> loginDirect(String username, String password){
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
	protected boolean logout() throws BadCredentials, VBulletinAPIException{
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
	public Member memberView(String user)throws NoPermissionLoggedout, VBulletinAPIException{
		return memberView(user, 0);
	}
	/**Returns Member information
	 * @return Member
	 * @throws NoPermissionLoggedout when only logged in members may view
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private Member memberView(String user, int loop) throws NoPermissionLoggedout, VBulletinAPIException{
		if(isConnected()){
			Member member = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("username", user);
			if(loop <= loopRequest){//no infinite loop by user
				try {
					member = new Member().parse(callMethod("member", params, true));
				} catch (InvalidAccessToken | NoPermissionLoggedout e) {
					login();
					if(isLoggedin()){
						return memberView(user, loop);
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return memberView(user, loop);
				}
				return member;
			}
			throw new VBulletinAPIException("vBulletin API Unknown Error ");
		}
		throw new NoConnectionException();
	}
	
	
	/**Attempts to empty the primary PM Inbox
	 * @param folderid which folder to empty
	 * @return true on success
	 * @throws InvalidId on non existent Private Message Folder
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to empty private message folders
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean messageEmptyInbox() throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return  messageEmptyInbox(0);
	}
	/**Attempts to empty a PM Inbox. folderid 0 is the primary inbox.
	 * @param folderid which folder to empty
	 * @return true on success
	 * @throws InvalidId on non existent Private Message Folder
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to empty private message folders
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean messageEmptyInbox(int folderid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return  messageEmptyInbox(folderid, 9876543210L);
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
	public boolean messageEmptyInbox(int folderid, long dateToDelete) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return  messageEmptyInbox(""+folderid, ""+dateToDelete, 0);
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
	private boolean messageEmptyInbox(String folderid, String dateToDelete, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();//150885
			//params.put("dateline", "9876543210");
			params.put("dateline", dateToDelete);
			params.put("folderid", folderid);
			errorMsg = parseResponse(callMethod("private_confirmemptyfolder", params, true));
			if(loop <= loopRequest){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.equals("pm_messagesdeleted")){//success
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						login();
						if(isLoggedin()){
							return messageEmptyInbox(folderid, dateToDelete, loop);
						}
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return messageEmptyInbox(folderid, dateToDelete, loop);
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
	public ArrayList<Message> messageList() throws NoPermissionLoggedout, VBulletinAPIException{
		return messageList(0);
	}
	//TODO need to redo this entire section
	/**Returns list of PMs in the primary inbox
	 * @return
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private ArrayList<Message> messageList(int loop) throws NoPermissionLoggedout, VBulletinAPIException{
		if(isConnected()){
			ArrayList<Message> msgList = new ArrayList<Message>();
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			if(loop <= loopRequest){//no infinite loop by user
				try {
					msgList = parseMessages(callMethod("private_messagelist", params, true));
					/*for(Message msg: msgList){//TODO should not need to grab the 
						msg.message = messageView(msg.pmid);
					}*/
				} catch (InvalidAccessToken | NoPermissionLoggedout e) {
					login();
					if(isLoggedin()){
						return messageList(loop);
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return messageList(loop);
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
	public boolean messageSend(String user,String title,String message) throws PMRecipTurnedOff, PMRecipientsNotFound, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return messageSend( user, title, message, false, 0);
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
	public boolean messageSend(String user,String title,String message, boolean signature) throws PMRecipTurnedOff, PMRecipientsNotFound, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return messageSend( user, title, message, signature, 0);
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
	private boolean messageSend(String user,String title,String message, boolean signature, int loop) throws PMRecipTurnedOff, PMRecipientsNotFound, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){//TODO add Exception for flood checks
			loop++;
			String errorMsg;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("title", title);
			params.put("message", message);
			params.put("recipients", user);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("private_insertpm", params, true));
			if(loop <= loopRequest){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.equals("pm_messagesent")){
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")||errorMsg.equals("invalid_api_signature")){
						login();
						if(isLoggedin()){
							return messageSend(user, title, message, signature,loop);
						}
						//return errorMsg;
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return messageSend( user, title, message, signature,loop);
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
	 * @return Message
	 * @throws InvalidId on non existent Private Message
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Message messageView(int pmId) throws InvalidId, NoPermissionLoggedout, VBulletinAPIException{
		return messageView(pmId+"", 0);
	}
	/**Grabs the message from the PM specified by the pmID
	 * @param pmId
	 * @return Message
	 * @throws InvalidId on non existent Private Message
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Message messageView(String pmId) throws InvalidId, NoPermissionLoggedout, VBulletinAPIException{
		return messageView(pmId, 0);
	}
	/**Grabs the message from the PM specified by the pmID
	 * @param pmId
	 * @param loop increasing int to prevent infinite loops
	 * @return Message
	 * @throws InvalidId on non existent Private Message
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private Message messageView(String pmId, int loop) throws InvalidId, NoPermissionLoggedout, VBulletinAPIException{
		if(isConnected()){
			Message message = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("pmid", pmId);
			if(loop <= loopRequest){//no infinite loop by user
				try {
					message = new Message().parse(callMethod("private_showpm", params, true));
				} catch (InvalidAccessToken e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return messageView(pmId, loop);
						}
					}
					throw e;
				} catch (NoPermissionLoggedout e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return messageView(pmId, loop);
						}
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return messageView(pmId, loop);
				}
				return message;
			}
			return new Message().parse(callMethod("private_showpm", params, true));
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
	public boolean postEdit(int postid,String message) throws ThreadClosed, InvalidId,NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return postEdit(""+postid, message, false);
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
	public boolean postEdit(int postid,String message, boolean signature) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return postEdit(""+postid, message, signature);
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
	public boolean postEdit(String postid,String message, boolean signature) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return postEdit(postid, message, signature, 0);
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
	private boolean postEdit(String postid,String message, boolean signature, int loop) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("postid", postid);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("editpost_updatepost", params, true));
			if(loop <= loopRequest){
				if(errorMsg != null){
					if(errorMsg.equals("redirect_editthanks")){//success
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						login();
						if(isLoggedin()){
							return postEdit(postid, message, signature, loop);
						}
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return postEdit(postid, message, signature, loop);
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
	public int[] postNew(int threadid,String message) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return postNew(""+threadid, message, false);
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
	public int[] postNew(int threadid,String message, boolean signature) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return postNew(""+threadid, message, signature);
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
	public int[] postNew(String threadid,String message, boolean signature) throws ThreadClosed, InvalidId,NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return postNew(threadid, message, signature, 0);
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
	private int[] postNew(String threadid,String message, boolean signature, int loop) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			loop++;
			String errorMsg;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("newreply_postreply", params, true));
			if(loop <= loopRequest){
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
						login();
						if(isConnected()){
							return postNew(threadid, message, signature, loop);
						}
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return postNew(threadid, message, signature, loop);
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
					login();//attempt to login
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
	private synchronized void setAPIAccessToken(String apiAccessToken) {
		this.apiAccessToken = apiAccessToken;
	}
	/**
	 * Sets the API client ID. You shouldn't need to use this if you use the
	 * init function.
	 *
	 * @param apiClientID
	 *            the new API client ID
	 */
	protected synchronized void setAPIClientID(String apiClientID) {
		this.apiClientID = apiClientID;
	}
	/**
	 * Sets the API key.
	 *
	 * @param apikey
	 *            the new API key
	 */
	public synchronized void setAPIkey(String apikey) {
		this.apikey = apikey;
	}
	/**
	 * Sets the URL of api.php
	 *
	 * @param apiURL
	 *            the new URL
	 */
	public synchronized void setAPIURL(String apiURL) {
		this.apiURL = apiURL;
	}
	/**
	 * Sets if the API successfully connected to the Forum
	 */
	private synchronized void setConnected(boolean arg){
		this.CONNECTED = arg;
	}
	/**Sets the username and password to login
	 * @param username
	 * @param pass
	 */
	public synchronized void setCredentials(String user, String pass) {
		setUsername(user);
		setPassword(pass);
	}
	/**
	 * Sets if the API successfully logged into the Forum
	 */
	private synchronized void setLoggedin(boolean arg){
		this.LOGGEDIN = arg;
	}
	/**Sets the password to login
	 * @param pass
	 */
	public synchronized void setPassword(String pass) {
		this.password = pass;
	}
	/**
	 * Sets the secret value. You shouldn't need to use this if you use the init
	 * function.
	 *
	 * @param secret
	 *            the new secret value
	 */
	private synchronized void setSecret(String secret) {
		this.secret = secret;
	}
	/**Sets the username to login
	 * @param user
	 */
	public synchronized void setUsername(String user) {
		this.username = user;
	}
	/**Attempts to close a Thread in the forum
	 * @param thread Thread to close
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadClose(ForumThread thread) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadClose(""+thread.getThreadId());
	}
	/**Attempts to close a Thread in the forum
	 * @param threadid Id of Thread to close
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadClose(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadClose(""+threadid);
	}
	/**Attempts to close a Thread in the forum
	 * @param threadid Id of Thread to close
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadClose(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadClose(threadid, 0);
	}
	/**Attempts to close a Thread in the forum
	 * @param threadid Id of Thread to close
	 * @param loop
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean threadClose(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_close", params, true));
			if(loop <= loopRequest){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							login();
							if(isLoggedin()){
								return threadClose(threadid, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return threadClose(threadid, loop);
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
	 * @param thread Thread to delete
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadDelete(ForumThread thread) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadDelete(""+thread.getThreadId());
	}
	/**Attempts to delete a Thread in the forum
	 * @param threadid Id of Thread to delete
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadDelete(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadDelete(""+threadid);
	}
	/**Attempts to delete a Thread in the forum
	 * @param threadid Id of Thread to delete
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadDelete(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadDelete(threadid, 0);
	}
	/**Attempts to delete a Thread in the forum
	 * @param threadid Id of Thread to delete
	 * @param loop how many iterations it went through
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean threadDelete(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_dodeletethreads", params, true));
			if(loop <= loopRequest){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							login();
							if(isLoggedin()){
								return threadDelete(threadid, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return threadDelete(threadid, loop);
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
	 * @param forum Forum to make the new Thread in
	 * @param subject The subject of the Thread/Post
	 * @param message The content of the first Post in the Thread
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] threadNew(Forum forum, String subject, String message) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadNew(""+forum.getForumId(),subject,message, false);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use. Does not include the user's signature.
	 * @param forumid Id of Forum to make the new Thread in
	 * @param subject The subject of the Thread/Post
	 * @param message The content of the first Post in the Thread
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] threadNew(int forumid, String subject, String message) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadNew(""+forumid,subject,message, false);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use.
	 * @param forum Forum to make the new Thread in
	 * @param subject The subject of the Thread/Post
	 * @param message The content of the first Post in the Thread
	 * @param signature If you should display the user's signature
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] threadNew(Forum forum, String subject, String message, boolean signature) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadNew(""+forum.getForumId(),subject,message, signature);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use.
	 * @param forumid Id of Forum to make the new Thread in
	 * @param subject The subject of the Thread/Post
	 * @param message The content of the first Post in the Thread
	 * @param signature If you should display the user's signature
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] threadNew(int forumid, String subject, String message, boolean signature) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadNew(""+forumid,subject,message, signature);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use.
	 * @param forumid Id of Forum to make the new Thread in
	 * @param subject The subject of the Thread/Post
	 * @param message The content of the first Post in the Thread
	 * @param signature If you should display the user's signature
	 * @return int[0] = threadid int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public int[] threadNew(String forumid, String subject, String message, boolean signature) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadNew(forumid,subject,message, signature, 0);
	}
	/**Attempts to post a new Thread in the forum, returns the posted Thread id and Post id for later use.
	 * @param forumid Id of Forum to make the new Thread in
	 * @param subject The subject of the Thread/Post
	 * @param message The content of the first Post in the Thread
	 * @param signature If you should display the user's signature
	 * @param loop how many iterations it went through
	 * @return int[0] = threadid / int[1] = postid
	 * @throws InvalidId on non existent Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private int[] threadNew(String forumid, String subject, String message, boolean signature, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("forumid", forumid);
			params.put("subject", subject);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("newthread_postthread", params, true));
			if(loop <= loopRequest){//no infinite loop by user
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
							login();
							if(isLoggedin()){
								return threadNew(forumid, subject, message, signature, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return threadNew(forumid, subject, message, signature, loop);
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
	 * @param thread Thread to open
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadOpen(ForumThread thread) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadOpen(""+thread.getThreadId());
	}
	/**Attempts to open a Thread in the forum
	 * @param threadid ID of Thread to open
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadOpen(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadOpen(""+threadid);
	}
	/**Attempts to open a Thread in the forum
	 * @param threadid ID of Thread to open
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean threadOpen(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadOpen(threadid, 0);
	}
	/**Attempts to open a Thread in the forum
	 * @param threadid ID of Thread to open
	 * @param loop how many iterations it went through
	 * @return true on success
	 * @throws InvalidId on non existent Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean threadOpen(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_open", params, true));
			if(loop <= loopRequest){//no infinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							login();
							if(isLoggedin()){
								return threadOpen(threadid, loop);
							}
						}
						else if(errorMsg.equals("invalid_api_signature")){
							return threadOpen(threadid, loop);
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
	 * @param thread Thread to view
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread threadView(ForumThread thread) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadView(""+thread.getThreadId(), null, null);
	}
	/**Attempts to view a thread and all the posts of page 1. Number of posts per page varies on the account settings.
	 * @param threadid Id of Thread to view
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread threadView(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadView(""+threadid, null, null);
	}
	/**Attempts to view a thread and all the posts of which page the postid is specified is on.
	 * @param thread Thread to view
	 * @param page Page number to view
	 * @param perpage Number of Posts to view per page(alters the results of the page parameter)
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread threadView(ForumThread thread, int page, int perpage) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadView(""+thread.getThreadId(), ""+page, ""+perpage);
	}
	/**Attempts to view a thread and all the posts of which page the postid is specified is on.
	 * @param threadid Id of Thread to view
	 * @param page Page number to view
	 * @param perpage Number of Posts to view per page(alters the results of the page parameter)
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread threadView(int threadid, int page, int perpage) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadView(""+threadid, ""+page, ""+perpage);
	}
	/**Attempts to view a thread and all the posts of which page the postid is specified is on.
	 * @param threadid Id of Thread to view
	 * @param page Page number to view
	 * @param perpage Number of Posts to view per page(alters the results of the page parameter)
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public ForumThread threadView(String threadid, String page, String perpage) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadView(threadid, page, perpage, null, 0);
	}
	/**Attempts to view a thread and all the posts of which page the postid is specified is on.
	 * @param threadid Id of Thread to view
	 * @param page Page number to view
	 * @param perpage Number of Posts to view per page(alters the results of the page parameter)
	 * @param postid determines which page to view based on postid, should not be used with the 'page' parameter
	 * @param loop how many iterations it went through
	 * @return
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private ForumThread threadView(String threadid, String page, String perpage, String postid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(isConnected()){
			ForumThread thread = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			if(postid != null){params.put("p", postid);}
			if(page != null){params.put("page", page);}
			if(perpage != null){params.put("perpage", perpage);}
			if(loop <= loopRequest){//no infinite loop by user
				try {
					thread = new ForumThread().parse(callMethod("showthread", params, true));
				} catch (InvalidAccessToken e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return threadView(threadid, page, perpage, postid, loop);
						}
					}
					throw e;
				} catch (NoPermissionLoggedout e) {
					if(this.isCredentialsSet()){
						login();
						if(isLoggedin()){
							return threadView(threadid, page, perpage, postid, loop);
						}
					}
					throw e;
				} catch (InvalidAPISignature e) {
					return threadView(threadid, page, perpage, postid, loop);
				}
				return thread;
			}
			return new ForumThread().parse(callMethod("showthread", params, true));
		}
		throw new NoConnectionException();
	}
	/**
	 * Return last Post of this thread
	 * @param thread Thread to search
	 * @return null when the post cannot be found in the thread
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread and post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Post threadViewLastPost(ForumThread thread) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadViewLastPost(thread.getThreadId());//yes, just reload it again as there could be a new post
	}
	/**
	 * Return last Post of this thread
	 * @param threadid Id of Thread to search
	 * @return null when the post cannot be found in the thread
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread and post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Post threadViewLastPost(int threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		ForumThread thread = null;
		ForumThread threadReload = null;
		thread = threadView(threadid, 1, 1);//just the first post
		threadReload = threadView(thread.getThreadId(), thread.getTotalPosts(), 1);
		for(Post post : threadReload.getPosts()){
			if(post.isLastShown()){
				return post;
			}
		}
		return null;
	}
	/**Attempts to view a Post residing in a thread.
	 * @param thread Thread the Post resides in
	 * @param postid Id of Post to view
	 * @return null when the post cannot be found in the thread
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread and post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Post threadViewPost(ForumThread thread, int postid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return threadViewPost(thread.getThreadId(), postid);
	}
	/**Attempts to view a Post residing in a thread.
	 * @param threadid If of Thread the Post resides in
	 * @param postid Id of Post to view
	 * @return null when the post cannot be found in the thread
	 * @throws InvalidId Thread does no exist or left blank
	 * @throws NoPermissionLoggedout when logged out and guest do not have viewing rights
	 * @throws NoPermissionLoggedin when account does not have permission to view this thread and post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public Post threadViewPost(int threadid, int postid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		ForumThread thread = threadView(""+threadid, null, "1" , ""+postid, 0);
		for(Post post : thread.posts){
			if(post.postid == postid){
				return post;
			}
		}
		return null;
	}	
	
}
