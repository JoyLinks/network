/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import java.util.HashMap;
import java.util.Map;

import com.joyzl.network.codec.Binary;

/**
 * Request和Response的父类 提供HTTP Header支持
 * 
 * @author ZhangXi
 * @date 2021年10月8日
 */
public class HTTPMessage extends Message {

	private String version = HTTP1.V11;
	private final Map<String, String> headers = new HashMap<>();

	// HTTP 2
	private int dependency;
	private int weight;

	public HTTPMessage() {
	}

	public HTTPMessage(int id, String version) {
		super(id);
		this.version = version;
	}

	@Override
	public void reset() throws Exception {
		super.reset();
		headers.clear();
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String value) {
		version = value;
	}

	public void addHeader(String name, String value) {
		headers.put(name, value);
	}

	public void addHeader(Header value) {
		headers.put(value.getHeaderName(), value.getHeaderValue());
	}

	public boolean hasHeader(String name) {
		return headers.containsKey(name);
	}

	public String getHeader(String name) {
		return headers.get(name);
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void clearHeaders() {
		headers.clear();
	}

	boolean isExclusive() {
		return Binary.getBit(dependency, 31);
	}

	void setExclusive(boolean value) {
		dependency = Binary.setBit(dependency, value, 31);
	}

	int getDependency() {
		return dependency;
	}

	void setDependency(int value) {
		dependency = value;
	}

	int getWeight() {
		return weight;
	}

	void setWeight(int value) {
		weight = value;
	}
}