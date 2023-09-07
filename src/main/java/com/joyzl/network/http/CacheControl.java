/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Cache-Control通用消息头字段，被用于在http请求和响应中，通过指定指令来实现缓存机制。
 * 缓存指令是单向的，这意味着在请求中设置的指令，不一定被包含在响应中。
 * 
 * <pre>
 * 客户端可以在HTTP请求中使用的标准 Cache-Control 指令。
 * Cache-Control: max-age=<seconds>
 * Cache-Control: max-stale[=<seconds>]
 * Cache-Control: min-fresh=<seconds>
 * Cache-control: no-cache
 * Cache-control: no-store
 * Cache-control: no-transform
 * Cache-control: only-if-cached
 * </pre>
 * 
 * <pre>
 * 服务器可以在响应中使用的标准 Cache-Control 指令。
 * Cache-control: must-revalidate
 * Cache-control: no-cache
 * Cache-control: no-store
 * Cache-control: no-transform
 * Cache-control: public
 * Cache-control: private
 * Cache-control: proxy-revalidate
 * Cache-Control: max-age=<seconds>
 * Cache-control: s-maxage=<seconds>
 * 
 * Cache-Control:public, max-age=31536000
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年1月13日
 */
public final class CacheControl extends Header {

	public final static String NAME = "Cache-Control";

	public final static String MAX_AGE = "max-age";
	public final static String MAX_STALE = "max-stale";
	public final static String MIN_FRESH = "min-fresh";
	public final static String NO_CACHE = "no-cache";
	public final static String NO_STORE = "no-store";
	public final static String NO_TRANSFORM = "no-transform";
	public final static String ONLY_IF_CACHED = "only-if-cached";
	public final static String MUST_REVALIDATE = "must-revalidate";
	public final static String PUBLIC = "public";
	public final static String PRIVATE = "private";
	public final static String PROXY_REVALIDATE = "proxy-revalidate";
	public final static String S_MAXAGE = "s-maxage";

	private String control;
	private int seconds;

	public CacheControl() {
	}

	public CacheControl(String control) {
		this(control, 0);
	}

	public CacheControl(String control, int seconds) {
		this.control = control;
		this.seconds = seconds;
	}

	@Override
	public String getHeaderName() {
		return NAME;
	}

	@Override
	public String getHeaderValue() {
		if (MAX_AGE.equalsIgnoreCase(control) || MIN_FRESH.equalsIgnoreCase(control) || S_MAXAGE.equalsIgnoreCase(control)) {
			return control + HTTPCoder.EQUAL + seconds;
		}
		if (MAX_STALE.equalsIgnoreCase(control) && seconds > 0) {
			return control + HTTPCoder.EQUAL + seconds;
		}
		return control;
	}

	@Override
	public void setHeaderValue(String value) {
		int index;
		if ((index = value.indexOf(HTTPCoder.EQUAL)) > 0) {
			control = value.substring(0, index);
			seconds = Integer.parseInt(value, index + 1, value.length(), 10);
		} else {
			control = value;
		}
	}

	public final static CacheControl parse(String value) {
		if (Utility.noEmpty(value)) {
			CacheControl header = new CacheControl();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}

	public String getControl() {
		return control;
	}

	public void setControl(String value) {
		control = value;
	}

	public int getSeconds() {
		return seconds;
	}

	public void setSeconds(int value) {
		seconds = value;
	}
}
