package com.sirius.weixinBasic;

import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

public class MsgBuilder {

	public static void main(String[] args) {
		String fromUserName = "user1";
		String openId = "gh_93eb357da0ea";
		
		String msg = "<xml>"
				+ "<ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>"
				+ "<FromUserName><![CDATA[" + openId + "]]></FromUserName>"
				+ "<CreateTime>" + new Date().getTime() + "</CreateTime>"
				+ "<MsgType><![CDATA[text]]></MsgType>"
				+ "<Content><![CDATA[still building...]]></Content>"
				+ "</xml>";
		
		Document resDoc;
		try {
			resDoc = DocumentHelper.parseText(msg);
			System.out.println(resDoc.asXML());
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
