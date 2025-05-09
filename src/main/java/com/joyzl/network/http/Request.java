/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.joyzl.network.http.MultipartFile.MultipartFiles;

/**
 * HTTP 请求
 * 
 * @author ZhangXi
 * @date 2021年9月30日
 */
public class Request extends HTTPMessage {

	private String method = HTTP1.GET;
	private String url;
	private int uriHost = -1, uriPort, uriPath = -1, uriQuery, uriAnchor;

	public Request() {
	}

	public Request(int id, String version) {
		super(id, version);
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String value) {
		method = value;
	}

	@Override
	public String toString() {
		return method + HTTPCoder.SPACE + getVersion() + HTTPCoder.SPACE + url;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public String getURL() {
		return url;
	}

	/**
	 * 设置URL并解析各个字段位置 SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public void setURL(String value) {
		url = value;
		uriHost = uriPath = -1;
		uriPort = uriQuery = uriAnchor = 0;
		if (value != null) {
			uriHost = value.indexOf("://");
			if (uriHost >= 0) {
				uriHost += 3;
				uriPort = value.indexOf(':', uriHost);
				uriQuery = value.indexOf('?', uriHost);
				uriAnchor = value.indexOf('#', uriHost);
			} else {
				uriQuery = value.indexOf('?');
				uriAnchor = value.indexOf('#');
			}
		}
	}

	/**
	 * SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public String getScheme() {
		if (uriHost > 0) {
			return url.substring(0, uriHost - 3);
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public String getHost() {
		if (uriHost >= 0) {
			if (uriPort > 0) {
				return url.substring(uriHost, uriPort - 1);
			} else //
			if (uriPath > 0) {
				return url.substring(uriHost, uriPath);
			}
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public int getPort() {
		if (uriPort > 0) {
			if (uriPath > 0) {
				return Integer.parseUnsignedInt(url, uriPort, uriPath, 10);
			}
			return Integer.parseUnsignedInt(url, uriPort, url.length(), 10);
		}
		return 0;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public String getPath() {
		if (uriPath == 0) {
			if (uriQuery > 0) {
				return url.substring(uriPath, uriQuery);
			}
			if (uriAnchor > 0) {
				return url.substring(uriPath, uriAnchor);
			}
			return url;
		}
		if (uriPath > 0) {
			if (uriQuery > 0) {
				return url.substring(uriPath, uriQuery);
			}
			if (uriAnchor > 0) {
				return url.substring(uriPath, uriAnchor);
			}
			return url.substring(uriPath);
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public String getQuery() {
		if (uriQuery > 0) {
			if (uriAnchor > 0) {
				return url.substring(uriQuery, uriAnchor);
			}
			return url.substring(uriQuery);
		}
		return null;
	}

	/**
	 * SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR
	 */
	public String getAnchor() {
		if (uriAnchor > 0) {
			return url.substring(uriAnchor);
		}
		return null;
	}

	/**
	 * 检查URL中的path部分是否与指定的路径匹配，此方法用于避免前缀匹配时创建新字符串对象
	 */
	public boolean pathStart(String path) {
		return url.startsWith(path, uriPath);
	}

	/**
	 * 获取URL中的path部分字符数量（长度），此方法用于长度判断时避免创建新字符串对象
	 */
	public int pathLength() {
		if (uriPath == 0) {
			if (uriQuery > 0) {
				return uriQuery;
			}
			if (uriAnchor > 0) {
				return uriAnchor;
			}
			return url.length();
		}
		if (uriPath > 0) {
			if (uriQuery > 0) {
				return uriQuery - uriPath;
			}
			if (uriAnchor > 0) {
				return uriAnchor - uriPath;
			}
			return url.length() - uriPath;
		}
		return 0;
	}

	/**
	 * 设置URL并不会执行任何字段解析
	 */
	protected void setUrl(String value) {
		url = value;
	}

	protected void setAnchor(int value) {
		uriAnchor = value;
	}

	protected void setQuery(int value) {
		uriQuery = value;
	}

	protected void setPath(int value) {
		uriPath = value;
	}

	protected void setPort(int value) {
		uriPort = value;
	}

	protected void setHost(int value) {
		uriHost = value;
	}

	protected int getHostIndex() {
		return uriHost;
	}

	protected int getQueryIndex() {
		return uriQuery;
	}

	protected int getAnchorIndex() {
		return uriAnchor;
	}

	/** HTTP2 SCHEME://HOST:PORT/PATH?PARAMETERS#ANCHOR */
	protected void setScheme(String value) {

	}

	/** HTTP2 SCHEME://HOST(Authority):PORT/PATH?PARAMETERS#ANCHOR */
	protected void setAuthority(String value) {

	}

	protected String getAuthority() {
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////

	private Map<String, String[]> parameters;

	/**
	 * 添加参数
	 */
	public void addParameter(String name, String value) {
		if (parameters == null) {
			parameters = new HashMap<>();
		}
		String[] values = parameters.get(name);
		if (values == null) {
			parameters.put(name, new String[] { value });
		} else {
			values = Arrays.copyOf(values, values.length + 1);
			values[values.length - 1] = value;
			parameters.replace(name, values);
		}
	}

	/**
	 * 设置参数
	 */
	public void setParameter(String name, String value) {
		if (parameters == null) {
			parameters = new HashMap<>();
		}
		String[] values = parameters.get(name);
		if (values == null) {
			parameters.put(name, new String[] { value });
		} else if (values.length == 1) {
			values[0] = value;
			parameters.replace(name, values);
		} else {
			parameters.put(name, new String[] { value });
		}
	}

	/**
	 * 设置多个参数值
	 */
	public void setParameter(String name, String[] values) {
		parameters.put(name, values);
	}

	/**
	 * 获取参数值
	 */
	public String getParameter(String name) {
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		String[] values = parameters.get(name);
		if (values == null || values.length == 0) {
			return null;
		} else {
			return values[0];
		}
	}

	/**
	 * 获取参数值
	 */
	public String[] getParameterValues(String name) {
		if (parameters == null || parameters.isEmpty()) {
			return null;
		}
		return parameters.get(name);
	}

	public Map<String, String[]> getParametersMap() {
		return parameters;
	}

	public boolean hasParameter(String name) {
		if (parameters == null || parameters.isEmpty()) {
			return false;
		}
		return parameters.containsKey(name);
	}

	public boolean hasParameters() {
		if (parameters == null || parameters.isEmpty()) {
			return false;
		}
		return true;
	}

	public void clearParameters() {
		if (parameters != null) {
			parameters.clear();
		}
	}

	/**
	 * 获取请求的文件，如果请不包含文件将返回空集合
	 * 
	 * @return 包含文件的集合或空的集合
	 */
	public MultipartFiles getMultipartFiles() {
		if (getContent() != null) {
			if (getContent() instanceof MultipartFiles) {
				return (MultipartFiles) getContent();
			}
		}
		return MultipartFiles.EMPTY;
	}
}