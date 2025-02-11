package com.joyzl.network.webdav.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 多状态返回
 * 
 * @author ZhangXi 2025年2月9日
 */
public class MultiStatus implements ResponseDescription {
	/*-
	 * <!ELEMENT multistatus (response*, responsedescription?) >
	 */

	private final List<Response> responses = new ArrayList<>();
	private String description;

	public List<Response> getResponses() {
		return responses;
	}

	public void setResponses(List<Response> values) {
		if (responses != values) {
			responses.clear();
			responses.addAll(values);
		}
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String value) {
		description = value;
	}
}