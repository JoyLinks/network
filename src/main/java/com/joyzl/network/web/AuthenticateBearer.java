package com.joyzl.network.web;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.http.Authorization;
import com.joyzl.network.http.HTTP1Coder;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;
import com.joyzl.network.http.WWWAuthenticate;

/**
 * Bearer 身份验证 RFC 6750
 * 
 * @author ZhangXi 2024年11月26日
 */
public class AuthenticateBearer extends Authenticate {

	public final static String TYPE = "Bearer";

	private final Map<String, String> TOKENS = new ConcurrentHashMap<>();
	private String users;
	private String www;

	public AuthenticateBearer(String path) {
		super(path);
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean verify(Request request, Response response) {
		// Authorization: Bearer token

		String token = request.getHeader(Authorization.NAME);
		if (token != null && token.length() > 6) {
			if (token.startsWith(TYPE)) {
				token = token.substring(TYPE.length() + 1);
				if (TOKENS.containsKey(token)) {
					return true;
				}
				if (check(token)) {
					TOKENS.put(token, token);
					return true;
				}
			}
		}
		response.setStatus(HTTPStatus.UNAUTHORIZED);
		response.addHeader(WWWAuthenticate.NAME, www);
		return false;
	}

	public void setRealm(String vlaue) {
		super.setRealm(vlaue);
		www = TYPE + " realm=\"" + vlaue + HTTP1Coder.QUOTE;
	}

	public void setUsers(String value) throws IOException {
		users = value;
	}

	public String getUsers() {
		return users;
	}

	boolean check(String token) {
		// 向第三方请求验证令牌
		return false;
	}
}