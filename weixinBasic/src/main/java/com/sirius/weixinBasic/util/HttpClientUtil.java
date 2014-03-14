package com.sirius.weixinBasic.util;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.netease.libs.http.meta.ApplicationType;

/**
 * 带连接池的HTTP
 */

public class HttpClientUtil {

	private static final String TAG_CHARSET = "charset=";

	private final static Logger logger = Logger.getLogger(HttpClientUtil.class);

	private static final int CONNECTION_TIMEOUT = 3000;// 连接超时时间
	private static final int SO_TIMEOUT = 5000;// 等待数据超时时间
	private PoolingClientConnectionManager pool = null;
	private int maxConnection = 32;
	private static final String DEFAULT_CHARSET = "UTF-8";
	private int conntimeout = CONNECTION_TIMEOUT;
	private int sotimeout = SO_TIMEOUT;
	private String reqCharset = DEFAULT_CHARSET;
	private String resCharset = DEFAULT_CHARSET;
	private String agentHeader = "Netease/0.1";

	public HttpClientUtil() {

	}

	public HttpClientUtil(int conntimeout, int sotimeout) {
		this.sotimeout = sotimeout;
		this.conntimeout = conntimeout;
	}

	public HttpClientUtil(int maxConnection, int conntimeout, int sotimeout) {
		this(conntimeout, sotimeout);
		this.maxConnection = maxConnection;
	}

	public HttpClientUtil(int maxConnection, String charset, int conntimeout, int sotimeout) {
		this(conntimeout, sotimeout);
		this.maxConnection = maxConnection;
		this.reqCharset = charset;
	}

	public HttpClientUtil(int maxConnection) {
		this.maxConnection = maxConnection;
	}

	public HttpClientUtil(int maxConnection, String charset) {
		this.maxConnection = maxConnection;
		this.reqCharset = charset;
	}

	public HttpClientUtil(int maxConnection, String charset, String resCharset) {
		this.maxConnection = maxConnection;
		this.reqCharset = charset;
		this.resCharset = resCharset;
	}

	private HttpParams getParams() {
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, sotimeout);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, conntimeout);
		return params;
	}

	public HttpClient getHttpClient() {
		return new DefaultHttpClient(pool, getParams());
	}

	@PreDestroy
	public void destroy() throws Exception {
		logger.info("Http connection pool will destory...");
		if (pool != null) {
			pool.shutdown();
		}
		logger.info("Http connection pool destroyed!");
	}

	@PostConstruct
	public void afterPropertiesSet() throws Exception {
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https", 443, PlainSocketFactory.getSocketFactory()));
		pool = new PoolingClientConnectionManager(schemeRegistry);
		pool.setMaxTotal(maxConnection);
		pool.setDefaultMaxPerRoute(maxConnection);
	}

	/**
	 * 直接返回字符
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public String getData(String url, List<NameValuePair> params) throws IOException {
		return fetchData(createGetMethod(url, params, null));
	}

	public String getData(String url) throws IOException {
		return fetchData(createGetMethod(url, Collections.EMPTY_LIST, null));
	}

	public String getData(String url, ApplicationType type) throws IOException {
		return fetchData(createGetMethod(url, Collections.EMPTY_LIST, type));
	}

	public String getData(String url, List<NameValuePair> params, ApplicationType type) throws IOException {
		return fetchData(createGetMethod(url, params, type));
	}

	public String putData(String url, List<NameValuePair> params) throws IOException {
		return fetchData(createPutMethod(url, params, null));
	}

	public String putData(String url, List<NameValuePair> params, ApplicationType type) throws IOException {
		return fetchData(createPutMethod(url, params, type));
	}

	public String deleteData(String url, List<NameValuePair> params) throws IOException {
		return fetchData(createDeleteMethod(url, params, null));
	}

	public String deleteData(String url, List<NameValuePair> params, ApplicationType type) throws IOException {
		return fetchData(createDeleteMethod(url, params, type));
	}

	public String postData(String url, final HttpEntity entity, ApplicationType type) throws IOException {
		return fetchData(this.createPostMethod(url, entity, type));
	}

	public String postData(String url, final HttpEntity entity) throws IOException {
		return fetchData(this.createPostMethod(url, entity, null));
	}

	public String postData(String url, final List<NameValuePair> params) throws IOException {
		return fetchData(this.createPostMethod(url, params, null));
	}

	public String postData(String url, final List<NameValuePair> params, ApplicationType type) throws IOException {
		return fetchData(this.createPostMethod(url, params, type));
	}

	private String fetchData(HttpRequestBase request) {
		String result = null;
		if (request == null)
			return result;
		HttpClient client = null;
		long watch = System.nanoTime();
		try {
			client = getHttpClient();
			if (reqCharset != null)
				request.addHeader("Content-Encoding", reqCharset);
			request.addHeader("User-Agent", agentHeader);
			HttpResponse response = client.execute(request);
			HttpEntity rsentity = response.getEntity();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				Charset rescharset = ContentType.getOrDefault(rsentity).getCharset();
				if (rescharset != null && rescharset.name().equals(HTTP.DEFAULT_CONTENT_CHARSET)) {
					result = EntityUtils.toString(rsentity);
					if (resCharset == null) {
						result = new String(result.getBytes(rescharset), DEFAULT_CHARSET);
					} else {
						result = new String(result.getBytes(rescharset), resCharset);
					}
				} else {
					if (resCharset != null) {
						result = EntityUtils.toString(rsentity, resCharset);
					} else {
						result = EntityUtils.toString(rsentity);
					}
				}
				if (logger.isDebugEnabled()) {
					logger.debug(" fetch request " + result);
				}
			} else {
				logger.error("fetch request return error status:" + request.getURI()
						+ response.getStatusLine().getStatusCode());
			}
		} catch (ClientProtocolException e) {
			logger.error("fetch request error " + request.getURI() + e.getMessage());
		} catch (ParseException e) {
			logger.error("fetch request error " + request.getURI() + e.getMessage());
		} catch (IOException e) {
			logger.error("fetch request error " + request.getURI() + e.getMessage());
		} finally {
			request.releaseConnection();
			if (logger.isDebugEnabled())
				logger.debug("fetch url " + request.getURI() + ",consume: " + (System.nanoTime() - watch) / 1000);
		}
		return result;
	}

	/**
	 * 创建post请求
	 * 
	 * @param path
	 *            路径
	 * @return 请求
	 * @throws UnsupportedEncodingException
	 */
	private HttpPost createPostMethod(String url, final List<NameValuePair> params, ApplicationType type)
			throws UnsupportedEncodingException {
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, reqCharset);
		return createPostMethod(url, entity, type);
	}

	private HttpPost createPostMethod(String url, HttpEntity entity, ApplicationType accept) {
		HttpPost method = new HttpPost(url);
		if (null != accept) {
			method.addHeader("accept", accept.val());
		}
		method.setEntity(entity);
		return method;
	}

	private HttpGet createGetMethod(String url, final List<NameValuePair> params, ApplicationType accept)
			throws IOException {
		HttpGet method = new HttpGet(urlEncode(url, params));
		if (null != accept) {
			method.addHeader("accept", accept.val());
		}
		return method;
	}

	private String urlEncode(String url, final List<NameValuePair> params) {
		String param = URLEncodedUtils.format(params, reqCharset);
		if (url.indexOf("?") == -1) {
			url += "?" + param;
		} else {
			url += param;
		}
		return url;
	}

	private HttpPut createPutMethod(String url, final List<NameValuePair> params, ApplicationType accept)
			throws IOException {
		HttpPut method = new HttpPut(url);
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, reqCharset);
		method.setEntity(entity);
		if (null != accept) {
			method.addHeader("accept", accept.val());
		}
		return method;
	}

	private HttpDelete createDeleteMethod(String url, final List<NameValuePair> params, ApplicationType accept)
			throws IOException {
		HttpDelete method = new HttpDelete(urlEncode(url, params));
		if (null != accept) {
			method.addHeader("accept", accept.val());
		}
		return method;
	}

	public String getAgentHeader() {
		return agentHeader;
	}

	public void setAgentHeader(String agentHeader) {
		this.agentHeader = agentHeader;
	}

}
