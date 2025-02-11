package com.joyzl.network.webdav.elements;

/**
 * 响应描述
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface ResponseDescription {
	/*-
	 * <!ELEMENT responsedescription (#PCDATA) >
	 * <responsedescription>There has been an access violation error.</responsedescription> 
	 */

	public String getDescription();

	public void setDescription(String value);
}