/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.File;
import java.security.Security;

/**
 * 响应头是资源的特定版本的标识符。
 * 
 * <pre>
 * ETag: W/"<etag_value>"
 * ETag: "<etag_value>"
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年10月18日
 */
public final class ETag {

	public final static String NAME = "ETag";

	/**
	 * 生成弱算法值
	 * 
	 * @param file
	 * @return W/"[length][lastModified][name.ext]"
	 */
	public final static String makeWTag(File file) {
		return "W/\"" + Long.toString(file.length(), Character.MAX_RADIX) + Long.toString(file.lastModified(), Character.MAX_RADIX) + Security.Base64Encode(file.getName()) + "\"";
	}

	/**
	 * 生成可逆的弱算法值
	 * 
	 * @param file
	 * @param key
	 * @return length-lastModified-name.ext-key...
	 */
	public final static String makeMTag(File file, String... keys) {
		final StringBuilder sb = new StringBuilder();
		sb.append(Long.toString(file.length(), Character.MAX_RADIX));
		sb.append("-");
		sb.append(Long.toString(file.lastModified(), Character.MAX_RADIX));
		sb.append("-");
		sb.append(Security.Base64Encode(file.getName()));
		for (int index = 0; index < keys.length; index++) {
			sb.append("-");
			sb.append(keys[index]);
		}
		return sb.toString();
	}

	/**
	 * 拆分可逆的弱算法值
	 * 
	 * @param tag
	 * @return length,lastModified,name.ext,key...
	 */
	public final static String[] parseMTag(String tag) {
		if (tag == null || tag.length() < 3) {
			return null;
		}
		final String[] texts = tag.split("-");
		if (texts.length == 4) {
			texts[2] = Security.Base64Decode(texts[2]);
			return texts;
		}
		return null;
	}
}