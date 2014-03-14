package com.sirius.weixinBasic;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MsgAnalysis {

	private static final Logger logger = LoggerFactory
			.getLogger(MsgAnalysis.class);

	public static void main(String[] args) {
		String msg = "<xml>"
				+ "<URL><![CDATA[http://115.28.229.0/weixinBasic/receive]]></URL>"
				+ "<ToUserName><![CDATA[test]]></ToUserName>"
				+ "<FromUserName><![CDATA[testopenid]]></FromUserName>"
				+ "<CreateTime>1392776657</CreateTime>"
				+ "<MsgType><![CDATA[text]]></MsgType>"
				+ "<Content><![CDATA[test]]></Content>" + "<MsgId>0001</MsgId>"
				+ "</xml>";

		try {
			Document doc = DocumentHelper.parseText(msg);
			
			System.out.println(doc.asXML());
			
			Element root = doc.getRootElement();

			String url = root.elementText("URL");
			String toUserName = root.elementText("ToUserName");
			String fromUserName = root.elementText("FromUserName");
			String createTime = root.elementText("CreateTime");
			String msgType = root.elementText("MsgType");
			String content = root.elementText("Content");
			String msgId = root.elementText("MsgId");

			System.out.println("url:" + url + ",toUserName:" + toUserName
					+ ",fromUserName:" + fromUserName + ",createTime:"
					+ createTime + ",msgType:" + msgType + ",content:"
					+ content + ",msgId:" + msgId);
		} catch (DocumentException e) {
			logger.error(e.toString());
		}
	}

}
