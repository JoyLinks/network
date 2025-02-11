package com.joyzl.network.webdav.elements;

/**
 * PROPFIND方法返回的属性
 * 
 * @author ZhangXi 2025年2月9日
 */
public class PropFind {
	/*-
	 * <!ELEMENT propfind ( propname | (allprop, include?) | prop ) >
	 */

	private boolean propname;
	private boolean allprop;
	private Prop prop;
}