/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * Accept-Encoding
 * 
 * @author ZhangXi
 * @date 2021年10月20日
 */
public final class AcceptEncoding extends QualityValueHeader {

	public final static String NAME = "Accept-Encoding";

	public final static String GZIP = TransferEncoding.GZIP;
	public final static String COMPRESS = TransferEncoding.COMPRESS;
	public final static String DEFLATE = TransferEncoding.DEFLATE;
	public final static String IDENTITY = TransferEncoding.IDENTITY;
	public final static String BR = "br";

	@Override
	public String getHeaderName() {
		return NAME;
	}

	public final static AcceptEncoding parse(String value) {
		if (noEmpty(value)) {
			AcceptEncoding header = new AcceptEncoding();
			header.setHeaderValue(value);
			return header;
		}
		return null;
	}
}