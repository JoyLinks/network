/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Authorization
 * <p>
 * HTTP协议中的 Authorization 请求消息头含有服务器用于验证用户代理身份的凭证，通常会在服务器返回401 Unauthorized
 * 状态码以及WWW-Authenticate消息头之后在后续请求中发送此消息头。
 * 
 * <pre>
 * Authorization: <type> <credentials>
 * Authorization: Basic YWxhZGRpbjpvcGVuc2VzYW1l
 * Authorization: Digest username="admin", realm="DS-2CD2310FD-I", qop="auth", algorithm="MD5", uri="/onvif/device_service", nonce="4e555130516a6b304e546f784e7a49334e7a417a59513d3d", nc=00000001, cnonce="0EE3ED23BFD9A00B2AB542E3BAB85BDB", response="518fd6d1666f9f00a5c5097359188c4e"
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年12月7日
 */
public final class Authorization extends Header {

	public final static String NAME = HTTP.Authorization;

	private String value;

	public Authorization() {
	}

	public Authorization(String value) {
		this.value = value;
	}

	@Override
	public String getHeaderName() {
		return HTTP.Authorization;
	}

	@Override
	public String getHeaderValue() {
		return value;
	}

	@Override
	public void setHeaderValue(String value) {
		this.value = value;
	}

	public final static Authorization parse(String value) {
		if (Utility.noEmpty(value)) {
			Authorization header = new Authorization();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}