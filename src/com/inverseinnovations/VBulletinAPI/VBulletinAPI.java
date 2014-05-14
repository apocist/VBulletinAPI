package com.inverseinnovations.VBulletinAPI;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;					//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.google.gson.internal.LinkedTreeMap;	//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.google.gson.reflect.TypeToken;		//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.google.gson.stream.JsonReader;		//Copyright 2008-2011 Google Inc. http://www.apache.org/licenses/LICENSE-2.0
import com.inverseinnovations.VBulletinAPI.Exception.*;


/** A class to provide an easy to use wrapper around the vBulletin REST API.*/
public final class VBulletinAPI extends Thread{
	public class Message{
		public String pmid;
		public String sendtime;
		public String statusicon;
		public String title;
		public int userid;
		public String username;
		public String message;

		public void setUserid(String id){
			if(isInteger(id)){
				userid = Integer.parseInt(id);
			}
		}
	}
	final public static double VERSION = 0.2;
	/**
	 * Checks if a String may be translated as an int
	 * @param s String to check
	 */
	private static boolean isInteger(String s){
		if(s != null && s != "")return isInteger(s,10);
		return false;
	}
	private static boolean isInteger(String s, int radix){
		if(s.isEmpty()) return false;
		for(int i = 0; i < s.length(); i++) {
			if(i == 0 && s.charAt(i) == '-') {
				if(s.length() == 1) return false;
				continue;
			}
			if(Character.digit(s.charAt(i),radix) < 0) return false;
		}
		return true;
	}
	/**
	 * Encrypts a String to MD5
	 * @param md5 String
	 * @return String encrypted to MD5
	 */
	private static final String MD5(String str) {
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			final byte[] array = md.digest(str.getBytes("UTF-8"));
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i) {
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
			return sb.toString();
		}
		catch (java.security.NoSuchAlgorithmException e) {}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	private static void queryAddCharEntity(Integer aIdx, StringBuilder aBuilder){
		String padding = "";
		if( aIdx <= 9 ){
		padding = "00";
		}
		else if( aIdx <= 99 ){
		padding = "0";
		}
		else {
		//no prefix
		}
		String number = padding + aIdx.toString();
		aBuilder.append("&#" + number + ";");
	}
	private static String querySafeString(String aText){
		final StringBuilder result = new StringBuilder();
		if(aText != null){
			final StringCharacterIterator iterator = new StringCharacterIterator(aText);
			char character =  iterator.current();
			while (character != CharacterIterator.DONE ){
				if (character == '"') {
					result.append("&quot;");
				}
				else if (character == '\"') {
					result.append("&quot;");
				}
				else if (character == '\t') {
					queryAddCharEntity(9, result);
				}
				else if (character == '\'') {
					queryAddCharEntity(39, result);
				}
				else if (character == '\\') {
					queryAddCharEntity(92, result);
				}
				else {
					//the char is not a special one
					//add it to the result as is
					result.append(character);
				}
				character = iterator.next();
			}
		}
		return result.toString();
	}
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
	private String password;
	/**
	 * Instantiates a new vBulletin API wrapper. This will initialise the API
	 * connection as well, with OS name and version pulled from property files
	 * and unique ID generated from the hashcode of the system properties.
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
	 * Instantiates a new vBulletin API wrapper. This will initialise the API
	 * connection as well, with OS name and version pulled from property files
	 * and unique ID generated from the hashcode of the system properties.
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
	 * Instantiates a new vBulletin API wrapper. This will initialise the API
	 * connection as well, with OS name and version pulled from property files
	 * and unique ID generated from the hashcode of the system properties.
	 * Will attempt to login with provided credientals.
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
		LinkedTreeMap<String, Object> map = null;

		try{

			StringBuffer queryStringBuffer = new StringBuffer("api_m=" + methodname);
			SortedSet<String> keys = new TreeSet<String>(params.keySet());
			for (String key : keys) {// ' " \ are unsafe
				//queryStringBuffer.append("&" + key + "=" + URLEncoder.encode(params.get(key), "UTF-8"));
				String value = querySafeString(params.get(key));
				queryStringBuffer.append("&" + key + "=" + URLEncoder.encode(value, "UTF-8"));
			}
			if (sign) {
				//System.out.println("encoded: "+queryStringBuffer.toString());
				//queryStringBuffer.append("&api_sig="+ generateHash( (queryStringBuffer.toString() + apiAccessToken+ apiClientID + secret + apikey)).toLowerCase());
				queryStringBuffer.append("&api_sig="+ MD5( (queryStringBuffer.toString() + getAPIAccessToken()+ apiClientID + getSecret() + getAPIkey())).toLowerCase());
				System.out.println("encoded: "+queryStringBuffer.toString());
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
			//StringBuffer returnBuffer = new StringBuffer();
			InputStream is = null;
			try{
				is = conn.getInputStream();
			}
			finally{
				if(is != null){
					String json = IOUtils.toString( is );

					Gson gson = new Gson();
					JsonReader reader = new JsonReader(new StringReader(json));
					reader.setLenient(true);
					map = gson.fromJson(reader,new TypeToken<Map<String, Object>>() {}.getType());
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
	 */
	private void errorsCommon(String errorMsg) throws InvalidAPISignature, NoPermissionLoggedout, NoPermissionLoggedin, InvalidAccessToken, APISocketTimeoutException, APIIOException{
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
	}
	/**
	 * Attempts to login no more than 3 times
	 */
	public boolean forum_Login() throws BadCredentials, VBulletinAPIException{
		if(IsConnected()){
			String errorMsg = "";
			for(int i = 0;i < 3;i++){
				errorMsg = parseResponse(forum_LoginDirect());if(errorMsg == null){errorMsg = "";}
				if(errorMsg.equals("redirect_login")){//if login is succesful
					setConnected(true);
					setLoggedin(true);
					return true;
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
	/**Login using the preset credientals*/
	private LinkedTreeMap<String, Object> forum_LoginDirect(){
		return forum_LoginDirect(this.username, this.password);
	}
	/**Login using defined credientals
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
	/**Grabs all data with this username
	 * Returning:
	 * username
	 * forumid
	 * forumjoindate
	 * avatarurl
	 * */
	public HashMap<String, String> forum_ViewMember(String user){
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("username", user);
		return parseViewMember(callMethod("member", params, true));
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
			//Base.Console.debug("apiAccessToken = "+apiAccessToken);
			//Base.Console.debug("apiClientID = "+apiClientID);
			//Base.Console.debug("secret = "+secret);
			return initvalues;
		}
		catch(Exception e){
			return null;
		}
	}
	/**
	 * Returns if connected into vBulletin forum
	 */
	public boolean IsConnected(){
		return CONNECTED;
	}
	/**Is the username and password set?
	 * @return
	 */
	public boolean IsCredentialsSet(){
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
	public boolean IsLoggedin(){
		return LOGGEDIN;
	}
	/**Parses response, designed specifically for gathering the list of all messages. Messages only have the header at this point, the actual message is not included
	 * @param response
	 * @return ArrayList<Message>
	 */
	@SuppressWarnings("rawtypes")
	private ArrayList<Message> parseMessages(LinkedTreeMap<String, Object> response){
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
			}
		}
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
			System.out.println("all ->");//XXX: for testing
			System.out.println(response.toString());
		}
		//Base.Console.debug("SC2Mafia API return error: "+theReturn);
		return theReturn;
	}
	/** Parses json from viewMember into
	 * username
	 * forumid
	 * forumjoindate
	 * avatarurl
	 * @param response from viewMember (callMethod)
	 * @return HashMap<String, String>
	 */
	@SuppressWarnings("rawtypes")
	private HashMap<String, String> parseViewMember(LinkedTreeMap<String, Object> response){
		HashMap<String, String> theReturn = new HashMap<String, String>();
		theReturn.put("forumid", null);
		theReturn.put("forumjoindate", null);
		theReturn.put("avatarurl", null);
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
							theReturn.put("username", (String) prepared.get("username"));
						}
					}
					if(prepared.containsKey("userid")){
						className = prepared.get("userid").getClass().getName();
						if(className.equals("java.lang.String")){
							theReturn.put("forumid", (String) prepared.get("userid"));
						}
					}
					if(prepared.containsKey("joindate")){
						className = prepared.get("joindate").getClass().getName();
						if(className.equals("java.lang.String")){
							theReturn.put("forumjoindate", (String) prepared.get("joindate"));
						}
					}
					if(prepared.containsKey("avatarurl")){
						className = prepared.get("avatarurl").getClass().getName();
						if(className.equals("java.lang.String")){
							theReturn.put("avatarurl", (String) prepared.get("avatarurl"));
						}
					}
				}
			}
		}
		System.out.println(response.toString());
		return theReturn;
	}
	/**Attempts to empty the primary PM Inbox
	 * @param folderid which folder to empty
	 * @return true on success
	 * @throws InvalidId on non existant Private Message Folder
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
	 * @throws InvalidId on non existant Private Message Folder
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
	 * @throws InvalidId on non existant Private Message Folder
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
	 * @throws InvalidId on non existant Private Message Folder
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to empty private message folders
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean pm_EmptyInbox(String folderid, String dateToDelete, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();//150885
			//params.put("dateline", "9876543210");
			params.put("dateline", dateToDelete);
			params.put("folderid", folderid);
			errorMsg = parseResponse(callMethod("private_confirmemptyfolder", params, true));
			if(loop < 4){//no inifinite loop by user
				if(errorMsg != null){
					if(errorMsg.equals("pm_messagesdeleted")){//success
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						forum_Login();
						if(IsLoggedin()){
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
	private ArrayList<Message> pm_ListPMs(int loop) throws NoPermissionLoggedout, VBulletinAPIException{//TODO need to reserve to order to show oldest first
		if(IsConnected()){
			String errorMsg;
			HashMap<String, String> params = new HashMap<String, String>();
			LinkedTreeMap<String,Object> linkmap = callMethod("private_messagelist", params, true);
			errorMsg = parseResponse(linkmap);
			loop++;
			if(loop < 4){
				if(errorMsg != null){
					if(errorMsg.equals("totalmessages")){//is the inbox
						ArrayList<Message> msgList = parseMessages(linkmap);
						for(Message msg:msgList){
							//try {
								msg.message = pm_ViewPM(msg.pmid);
							//}catch (VBulletinAPIException e) {e.printStackTrace();}
						}
						return msgList;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						forum_Login();
						if(IsLoggedin()){
							return pm_ListPMs(loop);
						}
					}
					else if(errorMsg.equals("invalid_api_signature")){
						return pm_ListPMs(loop);
					}
				}
			}
			errorsCommon(errorMsg);
			throw new VBulletinAPIException("vBulletin API Unknown Error - "+errorMsg);
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
	 * @param loop how many iretations it went through
	 * @return "pm_messagesent" on success
	 * @throws PMRecipTurnedOff when the recipient is not allowing private messages
	 * @throws PMRecipientsNotFound when the user does not exist
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to send private messages
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean pm_SendNew(String user,String title,String message, boolean signature, int loop) throws PMRecipTurnedOff, PMRecipientsNotFound, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
			loop++;
			String errorMsg;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("title", title);
			params.put("message", message);
			params.put("recipients", user);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("private_insertpm", params, true));
			if(loop < 4){//no inifinite loop by user
				if(errorMsg != null){
					if(errorMsg.equals("pm_messagesent")){
						return true;
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")||errorMsg.equals("invalid_api_signature")){
						forum_Login();
						if(IsLoggedin()){
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
	 * @throws InvalidId on non existant Private Message
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public String pm_ViewPM(String pmId) throws InvalidId, NoPermissionLoggedout, VBulletinAPIException{
		return pm_ViewPM(pmId, 0);
	}
	/**Grabs the message from the PM specified by the pmID
	 * @param pmId
	 * @param loop increasing int to prevent inifinite loops
	 * @return message text as String
	 * @throws InvalidId on non existant Private Message
	 * @throws NoPermissionLoggedout when logged out
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private String pm_ViewPM(String pmId, int loop) throws InvalidId, NoPermissionLoggedout, VBulletinAPIException{
		if(IsConnected()){
			String errorMsg = null;
			loop++;
			if(pmId != null){
				HashMap<String, String> params = new HashMap<String, String>();
				params.put("pmid", pmId);
				errorMsg = parseResponse(callMethod("private_showpm", params, true));
				if(loop < 4){//no inifinite loop by user
					if(errorMsg != null){
						if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(IsLoggedin()){
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
	 * @return true on successs
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existant Post
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
	 * @return true on successs
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existant Post
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
	 * @return true on successs
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existant Post
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
	 * @param loop how many iretations it went through
	 * @return true on successs
	 * @throws ThreadClosed when attempting to edit a post in a closed thread
	 * @throws InvalidId on non existant Post
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to edit this post
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean post_Edit(String postid,String message, boolean signature, int loop) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
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
						if(IsLoggedin()){
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
	 * @throws InvalidId on non existant Thread
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
	 * @throws InvalidId on non existant Thread
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
	 * @throws InvalidId on non existant Thread
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
	 * @param loop how many iretations it went through
	 * @return int[0] = threadid / int[1] = postid
	 * @throws ThreadClosed when attempting to post in a closed thread
	 * @throws InvalidId on non existant Thread
	 * @throws NoPermissionLoggedout when logged out, and does not accept annon posts
	 * @throws NoPermissionLoggedin when account does not have permission to post in this thread
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private int[] post_New(String threadid,String message, boolean signature, int loop) throws ThreadClosed, InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
			loop++;
			String errorMsg;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("newreply_postreply", params, true));
			if(loop < 4){
				if(errorMsg != null){
					if(isInteger(errorMsg.substring(0, 1))){//success
						if(errorMsg.contains(" ")){
							String[] ids = errorMsg.split(" ");
							ids[1] = ids[1].substring(0, ids[1].length() - 2);
							int[] theReturn = new int[2];
							if(isInteger(ids[0])){
								theReturn[0] = (Integer.parseInt(ids[0]));
							}
							if(isInteger(ids[1])){
								theReturn[1] = (Integer.parseInt(ids[1]));
							}
							return theReturn;
						}
					}
					else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
						forum_Login();
						if(IsConnected()){
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
			if(IsCredentialsSet()){//only try to login if the user/pass is set
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
	 * @throws InvalidId on non existant Thread
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
	 * @throws InvalidId on non existant Thread
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
	 * @throws InvalidId on non existant Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to close own or other's threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean thread_Close(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_close", params, true));
			if(loop < 4){//no inifinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(IsLoggedin()){
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
	 * @throws InvalidId on non existant Thread
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
	 * @throws InvalidId on non existant Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Delete(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Delete(threadid, 0);
	}
	/**Attempts to delete a Thread in the forum
	 * @param threadid
	 * @param loop how many iretations it went through
	 * @return true on success
	 * @throws InvalidId on non existant Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to delete own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean thread_Delete(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("threadid", threadid);
			errorMsg = parseResponse(callMethod("inlinemod_dodeletethreads", params, true));
			if(loop < 4){//no inifinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(errorMsg.equals("something...need success")){//success//TODO need the success result....
							return true;
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(IsLoggedin()){
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
	 * @throws InvalidId on non existant Forum
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
	 * @throws InvalidId on non existant Forum
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
	 * @throws InvalidId on non existant Forum
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
	 * @param loop how many iretations it went through
	 * @return int[0] = threadid / int[1] = postid
	 * @throws InvalidId on non existant Forum
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to create threads in the forum
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private int[] thread_New(String forumid,String subject,String message, boolean signature, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
			String errorMsg = null;
			loop++;
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("forumid", forumid);
			params.put("subject", subject);
			params.put("message", message);
			if(signature){params.put("signature", "1");}else{params.put("signature", "0");}
			errorMsg = parseResponse(callMethod("newthread_postthread", params, true));
			if(loop < 4){//no inifinite loop by user
				if(errorMsg != null){
					if(errorMsg.length() > 0){
						if(isInteger(errorMsg.substring(0, 1))){//success
							if(errorMsg.contains(" ")){
								String[] ids = errorMsg.split(" ");
								ids[1] = ids[1].substring(0, ids[1].length() - 2);
								int[] theReturn = new int[2];
								if(isInteger(ids[0])){
									theReturn[0] = (Integer.parseInt(ids[0]));
								}
								if(isInteger(ids[1])){
									theReturn[1] = (Integer.parseInt(ids[1]));
								}
								return theReturn;
							}
						}
						else if(errorMsg.equals("nopermission_loggedout")||errorMsg.equals("invalid_accesstoken")){
							forum_Login();
							if(IsLoggedin()){
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
	 * @throws InvalidId on non existant Thread
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
	 * @throws InvalidId on non existant Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	public boolean thread_Open(String threadid) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		return thread_Open(threadid, 0);
	}
	/**Attempts to open a Thread in the forum
	 * @param threadid
	 * @param loop how many iretations it went through
	 * @return true on success
	 * @throws InvalidId on non existant Thread
	 * @throws NoPermissionLoggedout when logged out
	 * @throws NoPermissionLoggedin when account does not have permission to open own or other threads
	 * @throws VBulletinAPIException when less common errors occur
	 */
	private boolean thread_Open(String threadid, int loop) throws InvalidId, NoPermissionLoggedout, NoPermissionLoggedin, VBulletinAPIException{
		if(IsConnected()){
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
							if(IsLoggedin()){
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
}
