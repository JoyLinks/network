/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.joyzl.network.Utility;

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

	public final static String NAME = HTTP1.ETag;

	/**
	 * 生成弱算法值
	 * 
	 * @return W/"[length][lastModified]"
	 */
	public final static String makeWeak(File file) {
		return makeWeak(file.length(), file.lastModified());
	}

	/**
	 * 生成弱算法值
	 * 
	 * @return W/"[length][lastModified]"
	 */
	public final static String makeWeak(long length, long lastModified) {
		return "W/\"" + Long.toString(length, Character.MAX_RADIX) + Long.toString(lastModified, Character.MAX_RADIX) + "\"";
	}

	/**
	 * 生成强算法值 MD5
	 * 
	 * @param file
	 * @return "******"
	 */
	public static String makeStorng(File file) {
		final byte[] buffer = new byte[2048];
		final MessageDigest md;
		int i;
		try (FileInputStream input = new FileInputStream(file)) {
			md = MessageDigest.getInstance("MD5");
			do {
				i = input.read(buffer);
				if (i > 0) {
					md.update(buffer, 0, i);
				}
			} while (i > 0);
		} catch (IOException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		return Utility.hex("\"", md.digest(), "\"");
	}
}