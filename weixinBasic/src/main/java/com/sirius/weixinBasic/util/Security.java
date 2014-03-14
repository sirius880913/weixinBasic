package com.sirius.weixinBasic.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * 加密类
 * 
 * @author panji
 * 
 */
public class Security {

	public static String SHA1(String inStr) {
		MessageDigest md = null;
		String outStr = null;
		try {
			md = MessageDigest.getInstance("SHA1"); // 选择SHA-1，也可以选择MD5
			byte[] digest = md.digest(inStr.getBytes()); // 返回的是byet[]，要转化为String存储比较方便
			outStr = bytetoString(digest);
		} catch (NoSuchAlgorithmException nsae) {
			nsae.printStackTrace();
		}
		return outStr;
	}

	private static String bytetoString(byte[] digest) {
		String str = "";
		String tempStr = "";

		for (int i = 0; i < digest.length; i++) {
			tempStr = (Integer.toHexString(digest[i] & 0xff));
			if (tempStr.length() == 1) {
				str = str + "0" + tempStr;
			} else {
				str = str + tempStr;
			}
		}
		return str.toLowerCase();
	}

	public static void main(String[] args) {
		String token = "Pj58233661";
		String time = "1392718416";
		String no = "1392707795";

		String[] array = { token, time, no };
		Arrays.sort(array);

		StringBuilder sb = new StringBuilder();

		for (String str : array) {
			sb.append(str);
		}

		System.out.println(SHA1(sb.toString()));
	}
}
