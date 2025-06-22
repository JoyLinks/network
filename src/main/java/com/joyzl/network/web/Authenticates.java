/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 身份验证集合
 * 
 * @author ZhangXi 2024年11月29日
 */
public class Authenticates {

	private final List<Authenticate> AUTHENTICATES = new ArrayList<>();

	public boolean check(Request request, Response response) {
		if (AUTHENTICATES.isEmpty()) {
			return true;
		}
		Authenticate authenticate;
		for (int index = 0; index < AUTHENTICATES.size(); index++) {
			authenticate = AUTHENTICATES.get(index);
			if (request.pathStart(authenticate.getPath())) {
				if (authenticate.allow(request, response)) {
					return authenticate.verify(request, response);
				}
				return false;
			}
		}
		return true;
	}

	public List<Authenticate> getAuthenticates() {
		return Collections.unmodifiableList(AUTHENTICATES);
	}

	public void setAuthenticates(List<Authenticate> values) {
		if (AUTHENTICATES != values) {
			AUTHENTICATES.clear();
			AUTHENTICATES.addAll(values);
		}
	}

	public void addAuthenticate(Authenticate value) {
		AUTHENTICATES.add(value);
	}

	public void removeAuthenticate(Authenticate value) {
		AUTHENTICATES.remove(value);
	}

	public void clear() {
		AUTHENTICATES.clear();
	}
}