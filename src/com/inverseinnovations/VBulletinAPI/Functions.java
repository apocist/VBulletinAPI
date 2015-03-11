package com.inverseinnovations.VBulletinAPI;

import java.io.UnsupportedEncodingException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class Functions {
	
	/**Converts raw objects(String/Integer/Boolean/Double) to Int
	 * @param obj
	 * @return 0 if not a number
	 */
	protected static int convertToInt(Object object){
		int retur = 0;
		if(object instanceof String){
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
	protected static String convertToString(Object object){
		String retur = "";
		if(object instanceof String){
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
	protected static boolean convertToBoolean(Object object){
		boolean retur = false;
		if(object instanceof String){
			object = ((String) object).toLowerCase();
			if(((String)object).equals("true") || ((String)object).equals("1")){
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
}
