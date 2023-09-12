package com.joyzl.network.web;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map.Entry;

import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;

/**
 * Content-Type: application/x-www-form-urlencoded
 * 
 * @author ZhangXi 2023年9月8日
 */
public class XWWWFormUrlencoded {

	/**
	 * 编码POST请求键值对数据
	 * <p>
	 * 必须设置Content-Type: application/x-www-form-urlencoded<br>
	 * 必须完整编码才能获知Content-Length
	 */
	public static void write(HTTPWriter writer, WEBRequest request) throws IOException {
		// name=value&name=value
		// POST的键值对参数 没有 [ ENTER LINE ] 结束标志
		// 必须编码完成才能提供Content-Length

		if (request.hasParameter()) {
			boolean more = false;
			for (Entry<String, String[]> item : request.getParametersMap().entrySet()) {
				for (int index = 0; index < item.getValue().length; index++) {
					if (more) {
						writer.write(HTTPCoder.AND);
					} else {
						more = true;
					}
					writer.write(item.getKey());
					writer.write(HTTPCoder.EQUAL);
					if (item.getValue()[index] == null) {
						// 忽略
					} else if (item.getValue()[index].length() <= 0) {
						// 忽略
					} else {
						writer.write(URLEncoder.encode(item.getValue()[index], HTTPCoder.URL_CHARSET));
					}
				}
			}
		}
	}

	/**
	 * 解码POST请求键值对数据
	 * <p>
	 * 必须获取Content-Type: application/x-www-form-urlencoded<br>
	 * 必须接收完整数据才能解码Content-Length
	 */
	public static void read(HTTPReader reader, WEBRequest request) throws IOException {
		// name=value&name=value
		// POST的键值对参数 没有 [ ENTER LINE ] 结束标志
		// 读取键值对参数，调用此方法之前应当判断数据流长度，
		// 因Content-Type=application/x-www-form-urlencoded提交的POST参数无结束标志
		// 因此在调用此方法读取POST参数之前应当通过Content-Length判断数据长度是否足够

		String name;
		while (reader.readTo(HTTPCoder.EQUAL)) {
			name = reader.string();
			reader.readTo(HTTPCoder.AND);
			if (reader.sequence().length() > 0) {
				request.addParameter(name, URLDecoder.decode(reader.string(), HTTPCoder.URL_CHARSET));
			} else {
				request.addParameter(name, null);
			}
		}
	}

	public static void write(HTTPWriter writer, WEBResponse response) throws IOException {
		throw new UnsupportedOperationException();
	}

	public static void read(HTTPReader reader, WEBResponse response) throws IOException {
		throw new UnsupportedOperationException();
	}
}