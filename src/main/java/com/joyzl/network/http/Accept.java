/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Accept 请求头用来告知（服务器）客户端可以处理的内容类型，这种内容类型用MIME类型来表示。
 * 借助内容协商机制,服务器可以从诸多备选项中选择一项进行应用，并使用Content-Type应答头通知客户端它的选择。
 * 浏览器会基于请求的上下文来为这个请求头设置合适的值，比如获取一个CSS层叠样式表时值与获取图片、视频或脚本文件时的值是不同的。
 * 
 * <pre>
 * Accept: <MIME_type>/<MIME_subtype>
 * Accept: <MIME_type>/＊
 * Accept: ＊/＊
 * Accept: text/html, application/xhtml+xml, application/xml;q=0.9, ＊/＊;q=0.8
 * Accept: text/html
 * Accept: image/＊
 * </pre>
 * 
 * ;q= (q因子权重)
 * 
 * @author ZhangXi
 * @date 2021年1月7日
 */
public final class Accept extends QualityValueHeader {

	public final static String NAME = HTTP1.Accept;

	public Accept() {
	}

	public Accept(String value) {
		setHeaderValue(value);
	}

	@Override
	public String getHeaderName() {
		return HTTP1.Accept;
	}

	public final static Accept parse(String value) {
		if (Utility.noEmpty(value)) {
			Accept header = new Accept();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}