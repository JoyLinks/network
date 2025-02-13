package com.joyzl.network.tls;

import java.util.List;

/**
 * 扩展
 * 
 * @author ZhangXi 2025年2月12日
 */
public interface Extensions {

	byte msgType();

	boolean isHelloRetryRequest();

	boolean hasExtensions();

	List<Extension> getExtensions();

	void setExtensions(List<Extension> value);
}