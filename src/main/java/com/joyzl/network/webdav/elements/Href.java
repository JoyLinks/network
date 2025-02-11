package com.joyzl.network.webdav.elements;

/**
 * 绝对或者相对路径
 * 
 * @author ZhangXi 2025年2月9日
 */
public interface Href {
	/*-
	 * <!ELEMENT href (#PCDATA)>
	 * <href>http://www.example.com/file</href>
	 */

	String getHref();

	void setHref(String value);
}