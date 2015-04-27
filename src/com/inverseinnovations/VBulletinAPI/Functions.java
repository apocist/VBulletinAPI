package com.inverseinnovations.VBulletinAPI;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

import com.google.gson.internal.LinkedTreeMap;
import com.inverseinnovations.VBulletinAPI.Exception.InvalidId;
import com.inverseinnovations.VBulletinAPI.Exception.VBulletinAPIException;

public class Functions {
	
	/**Converts raw objects(String/Integer/Boolean/Double) to Float
	 * @param obj
	 * @return 0 if not a number
	 */
	public static float convertToFloat(Object object){
		float retur = 0;
		if(object == null){
			retur = 0;
		}
		else if(object instanceof String){
			if(isInteger((String) object)){
				retur = Float.parseFloat((String) object);
			}
		}
		else if(object.getClass().getName().equals("java.lang.Double")){
			retur = new Double((double) object).floatValue();
		}
		else if(object instanceof Integer){
			retur = ((Integer) object).floatValue();
		}
		else if(object.getClass().equals(Boolean.class)){
			if((Boolean)object){
				retur = 1;
			}
		}
		return retur;
	}
	/**Converts raw objects(String/Integer/Boolean/Double) to Int
	 * @param obj
	 * @return 0 if not a number
	 */
	public static int convertToInt(Object object){
		int retur = 0;
		if(object == null){
			retur = 0;
		}
		else if(object instanceof String){
			if(isInteger((String) object)){
				retur = Integer.parseInt((String) object);
			}
		}
		else if(object.getClass().getName().equals("java.lang.Double")){
			retur = new Double((double) object).intValue();
		}
		else if(object instanceof Integer){
			retur = ((Integer) object).intValue();
		}
		else if(object.getClass().equals(Boolean.class)){
			if((Boolean)object){
				retur = 1;
			}
		}
		return retur;
	}
	/**Converts raw objects(String/Integer/Boolean/Double) to String
	 * @param obj
	 * @return blank otherwise
	 */
	public static String convertToString(Object object){
		String retur = "";
		if(object == null){
			retur = "";
		}
		else if(object instanceof String){
			retur = (String) object;
		}
		else if(object.getClass().getName().equals("java.lang.Double")){
			retur = new Double((double) object).toString();
		}
		else if(object instanceof Integer){
			retur = ((Integer) object).toString();
		}
		else if(object.getClass().equals(Boolean.class)){
			if((Boolean)object){
				retur = "true";
			}
			else{
				retur = "false";
			}
		}
		return retur;
	}
	/**Converts raw objects(String/Integer/Boolean/Double) to boolean
	 * @param obj
	 * @return blank otherwise
	 */
	public static boolean convertToBoolean(Object object){
		boolean retur = false;
		if(object == null){
			retur = false;
		}
		else if(object instanceof String){
			object = ((String) object).toLowerCase();
			if(((String)object).equals("true") || ((String)object).equals("1") || ((String)object).equals("1.0")){
				retur = true;
			}
		}
		else if(object.getClass().getName().equals("java.lang.Double")){
			if(((double)object)>=1){
				retur = true;
			}
		}
		else if(object instanceof Integer){
			if(((Integer) object)>=1){
				retur = true;
			}
		}
		else if(object.getClass().equals(Boolean.class)){
			retur = (Boolean)object;
		}
		return retur;
	}
	/**
	 * Returns a Variable from the supplied Map if it exist, as a boolean
	 * @param map LinkedTreeMap<String, Object> to search within
	 * @param variable The variable to fetch
	 * @return boolean, false if not existent
	 */
	public static boolean fetchBoolean(LinkedTreeMap<String, Object> map, String variable) {
		  return (map.containsKey(variable)) ? convertToBoolean(map.get(variable)) : false;
	}
	/**
	 * Returns a Variable from the supplied Map if it exist, as an Int
	 * @param map LinkedTreeMap<String, Object> to search within
	 * @param variable The variable to fetch
	 * @return int, 0 if not existent
	 */
	public static int fetchInt(LinkedTreeMap<String, Object> map, String variable) {
		  return (map.containsKey(variable)) ? convertToInt(map.get(variable)) : 0;
	}
	/**
	 * Returns a Variable from the supplied Map if it exist, as a String
	 * @param map LinkedTreeMap<String, Object> to search within
	 * @param variable The variable to fetch
	 * @return String, null if not existent
	 */
	public static String fetchString(LinkedTreeMap<String, Object> map, String variable) {
		  return (map.containsKey(variable)) ? convertToString(map.get(variable)) : null;
	}
	/**
	 * Returns a String built from the InputStream
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	protected static String inputStreamToString(InputStream inputStream) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = inputStream.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return new String(baos.toByteArray(), Charset.defaultCharset());
	}
	/**
	 * Checks if a String may be translated as an int
	 * @param s String to check
	 */
	protected static boolean isInteger(String s){
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
	protected static final String MD5(String str) {
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
	protected static void queryAddCharEntity(Integer aIdx, StringBuilder aBuilder){
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
	protected static String querySafeString(String aText){
		final StringBuilder result = new StringBuilder();
		if(aText != null){
			final StringCharacterIterator iterator = new StringCharacterIterator(aText);
			char character =  iterator.current();
			while (character != CharacterIterator.DONE ){
				if (character == '"') {
					result.append("&quot;");
					//result.append("%22");
				}
				else if (character == '\"') {
					result.append("&quot;");
					//result.append("%22");
				}
				else if (character == '\t') {
					queryAddCharEntity(9, result);
				}
				else if (character == '\'') {
					queryAddCharEntity(39, result);
					//result.append("%27");
				}
				else if (character == '\\') {
					queryAddCharEntity(92, result);
					//result.append("%5C");
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
	//TODO redo the errors
	/**Check if there are errors, if so Throw the correct Exception
	 * @param response from callMethod
	 * @throws VBulletinAPIException All generic or unknown errors
	 */
	@SuppressWarnings("unchecked")
	protected static void responseErrorCheck(LinkedTreeMap<String, Object> response) throws VBulletinAPIException{
		if(response.containsKey("response")){
			if(response.get("response") instanceof LinkedTreeMap){
				LinkedTreeMap<String, Object> response2 = (LinkedTreeMap<String, Object>)response.get("response");
				if(response2.containsKey("errormessage")){
					String theError = "";
					String errorSecond = "";
					if(response2.get("errormessage") instanceof String){
						theError = (String)response2.get("errormessage");
						if(theError.equals("redirect_postthanks")){//this is for newthread and newpost
							if(response.get("show") instanceof LinkedTreeMap){
								LinkedTreeMap<String, Object> show = (LinkedTreeMap<String, Object>)response.get("show");
								if(show.containsKey("threadid")){
									theError = Functions.convertToString(show.get("threadid"));
								}
								if(show.containsKey("postid")){
									errorSecond = Functions.convertToString(show.get("postid"));
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
					if(theError.equals("noid")){
						System.out.println("Thread Parse InvalidId "+errorSecond);
						throw new InvalidId(errorSecond);
					}
					String finalError = theError;
					if(!errorSecond.isEmpty()){
						finalError += " "+errorSecond;
					}
					VBulletinAPI.errorsCommon(finalError);
					System.out.println("responseError  response -> errormessage type unknown: "+response2.get("errormessage").getClass().getName());
					throw new VBulletinAPIException("vBulletin API Unknown Error - "+response2.get("errormessage").getClass().getName());
				}
			}
		}
	}
}
