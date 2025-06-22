/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.web;

import com.joyzl.network.http.HTTP1;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.Request;
import com.joyzl.network.http.Response;

/**
 * 资源校验
 * 
 * @author ZhangXi 2024年11月26日
 */
public abstract class Authenticate {

	private final String path;
	private String algorithm;
	private String[] methods;
	private String realm;

	public Authenticate(String path) {
		if (path == null) {
			this.path = "";
		} else {
			this.path = path;
		}
	}

	/**
	 * 请求方法是否允许
	 */
	public boolean allow(Request request, Response response) {
		if (methods == null || methods.length == 0) {
			return true;
		}
		for (int index = 0; index < methods.length; index++) {
			if (request.getMethod().equalsIgnoreCase(methods[index])) {
				return true;
			}
		}
		response.setStatus(HTTPStatus.METHOD_NOT_ALLOWED);
		response.addHeader(HTTP1.Allow, String.join(", ", methods));
		return false;
	}

	/**
	 * 验证是否允许请求资源
	 */
	public abstract boolean verify(Request request, Response response);

	public abstract String getType();

	/**
	 * 获取受保护的资源路径，相对于URL根路径
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 获取受保护资源的验证提示信息
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * 设置受保护资源的验证提示信息
	 */
	public void setRealm(String vlaue) {
		realm = vlaue;
	}

	/**
	 * 获取加密方式，如果密码采用加密存储，必须使用验证方式匹配的加密方式
	 */
	public String getAlgorithm() {
		return algorithm;
	}

	/**
	 * 设置加密方式，例如：MD5 SHA-1 SHA-256
	 */
	public void setAlgorithm(String value) {
		algorithm = value;
	}

	/**
	 * 获取允许的请求方法，如果未指定则默认允许所有
	 */
	public String[] getMethods() {
		return methods;
	}

	/**
	 * 设置允许的请求方法，如果未指定则默认允许所有
	 */
	public void setMethods(String... values) {
		methods = values;
	}
}