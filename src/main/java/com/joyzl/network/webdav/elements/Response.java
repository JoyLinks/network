package com.joyzl.network.webdav.elements;

import java.util.ArrayList;
import java.util.List;

/**
 * 响应结果
 * 
 * @author ZhangXi 2025年2月9日
 */
public class Response implements Href, ResponseDescription {
	/*-
	 * <!ELEMENT response (href, ((href*, status)|(propstat+)), error?, responsedescription? , location?) >
	 */

	private String href;
	private List<PropStat> propstats = new ArrayList<>();
	private Location location;
	private String description;
	private Error error;

	@Override
	public String getHref() {
		return href;
	}

	@Override
	public void setHref(String value) {
		href = value;
	}

	public List<PropStat> getPropstats() {
		return propstats;
	}

	public void setPropstats(List<PropStat> values) {
		if (propstats != values) {
			propstats.clear();
			propstats.addAll(values);
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