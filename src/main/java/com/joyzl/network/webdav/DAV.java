package com.joyzl.network.webdav;

import com.joyzl.network.http.Header;

/**
 * OPTIONS响应中表示该资源支持本规范所指定的DAV模式和协议
 * 
 * @author ZhangXi 2025年2月7日
 */
public class DAV extends Header {

	@Override
	public String getHeaderName() {
		return "DAV";
	}

	@Override
	public String getHeaderValue() {
		return "3,2,1";
	}

	@Override
	public void setHeaderValue(String value) {
		// TODO Auto-generated method stub

	}
}