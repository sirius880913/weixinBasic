package com.sirius.weixinBasic;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.sirius.weixinBasic.services.UserService;
import com.sirius.weixinBasic.util.Security;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

	String openId = "gh_93eb357da0ea";
	
	String token = "Pj58233661";

	private static final Logger logger = LoggerFactory
			.getLogger(HomeController.class);

	@Autowired
	private UserService userService;

	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);

		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
				DateFormat.LONG, locale);

		String formattedDate = dateFormat.format(date);

		model.addAttribute("serverTime", formattedDate);

		return "home";
	}

	/**
	 * 接收消息入口
	 * 
	 * @param locale
	 * @param model
	 * @param response
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @param echostr
	 */
	@RequestMapping(value = "/receive", method = RequestMethod.POST)
	public void receive(
			Locale locale,
			Model model,
			HttpServletResponse response,
			HttpServletRequest request,
			@RequestParam(value = "signature", defaultValue = "", required = false) String signature,
			@RequestParam(value = "timestamp", defaultValue = "", required = false) String timestamp,
			@RequestParam(value = "nonce", defaultValue = "", required = false) String nonce,
			@RequestParam(value = "echostr", defaultValue = "", required = false) String echostr) {
		logger.info("receive msg is:signature=" + signature + ",timestamp="
				+ timestamp + ",nonce=" + nonce + ",echostr=" + echostr);

		//接收消息体的数据
		StringBuffer contentbuf = new StringBuffer();
		//回复的内容
		String resContent = ""; 
		
		try {
			InputStream in = request.getInputStream();
			byte[] buf = new byte[1024];
			int bytesRead;

			while ((bytesRead = in.read(buf)) != -1) {
				contentbuf.append(new String(buf, 0, bytesRead, "utf-8"));
			}
			logger.info(contentbuf.toString());
		} catch (IOException e1) {
			logger.error(e1.toString());
		}
		
		try {
			Document doc = DocumentHelper.parseText(contentbuf.toString());
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
			
			String msg = "<xml>"
					+ "<ToUserName><![CDATA[" + fromUserName + "]]></ToUserName>"
					+ "<FromUserName><![CDATA[" + openId + "]]></FromUserName>"
					+ "<CreateTime>" + new Date().getTime() + "</CreateTime>"
					+ "<MsgType><![CDATA[text]]></MsgType>"
					+ "<Content><![CDATA[still building...]]></Content>"
					+ "</xml>";
			
			resContent = msg;
			logger.info("resContent:" + resContent);
		} catch (Exception e) {
			logger.error(e.toString());
		}

		// 对参数进行排序
		String[] array = { token, timestamp, nonce };
		Arrays.sort(array);

		StringBuilder sb = new StringBuilder();

		for (String str : array) {
			sb.append(str);
		}

		// 本地加密的sign
		String localSign = Security.SHA1(sb.toString());
		String result = "";

		if (StringUtils.equals(localSign, signature)) {
			result = resContent;
		} else {
			logger.warn("signature error");
			result = "signature error";
		}

		response.setContentType("application/xml;charset=UTF-8");
		try {
			response.getWriter().write(result);
			response.getWriter().flush();
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}
}
