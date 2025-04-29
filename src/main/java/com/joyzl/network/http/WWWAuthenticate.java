/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * WWW-Authenticate
 * <p>
 * HTTP WWW-Authenticate 响应头定义了使用何种验证方式去获取对资源的连接。 WWW-Authenticate
 * header通常会和一个401 Unauthorized 的响应一同被发送。
 * 
 * <pre>
 * WWW-Authenticate: <type> realm=<realm>
 * WWW-Authenticate: Basic
 * WWW-Authenticate: Basic realm="Access to the staging site"
 * WWW-Authenticate: Digest qop="auth", realm="DS-2CD2310FD-I", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d"
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年12月7日
 */
public final class WWWAuthenticate extends Header {

	public final static String NAME = HTTP1.WWW_Authenticate;

	private String value;

	@Override
	public String getHeaderName() {
		return HTTP1.WWW_Authenticate;
	}

	@Override
	public String getHeaderValue() {
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}

	public final static WWWAuthenticate parse(String value) {
		if (Utility.noEmpty(value)) {
			WWWAuthenticate header = new WWWAuthenticate();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}