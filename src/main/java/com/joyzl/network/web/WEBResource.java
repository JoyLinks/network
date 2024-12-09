package com.joyzl.network.web;

import java.io.IOException;
import java.io.InputStream;

import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Range.ByteRange;

/**
 * WEB资源
 * 
 * @author ZhangXi 2024年11月13日
 */
public abstract class WEBResource {

	/**
	 * 获取资源类型
	 */
	public abstract String getContentType();

	/**
	 * 获取资源语言
	 */
	public abstract String getContentLanguage();

	/**
	 * 获取资源位置
	 */
	public abstract String getContentLocation();

	/**
	 * 获取最后修改日期
	 */
	public abstract String getLastModified();

	/**
	 * 获取版本的标识符
	 */
	public abstract String getETag();

	/**
	 * 根据客户端请求指定的期望编码方式适配最合适的编码标识
	 */
	public abstract String fitEncoding(AcceptEncoding acceptEncoding);

	/**
	 * 获取指定编码方式的资源大小，如果期望的编码方式不支持返回原始资源大小
	 */
	public abstract long getLength(String encoding) throws IOException;

	/**
	 * 获取指定编码方式的资源数据，如果期望的编码方式不支持返回原始资源数据
	 */
	public abstract InputStream getData(String encoding) throws IOException;

	/**
	 * 获取指定编码方式的资源数据，如果期望的编码方式不支持返回原始资源数据
	 */
	public abstract InputStream getData(String encoding, ByteRange range) throws IOException;

}