/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

import com.joyzl.network.Utility;

/**
 * Accept-Encoding
 * 
 * @author ZhangXi
 * @date 2021年10月20日
 */
public final class AcceptEncoding extends QualityValueHeader {

	public final static String NAME = HTTP1.Accept_Encoding;

	public final static String GZIP = TransferEncoding.GZIP;
	public final static String COMPRESS = TransferEncoding.COMPRESS;
	public final static String DEFLATE = TransferEncoding.DEFLATE;
	public final static String IDENTITY = TransferEncoding.IDENTITY;
	public final static String BR = "br";

	@Override
	public String getHeaderName() {
		return HTTP1.Accept_Encoding;
	}

	public final static AcceptEncoding parse(String value) {
		if (Utility.noEmpty(value)) {
			AcceptEncoding header = new AcceptEncoding();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}