/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * 具有权重的值 "Accept-Encoding: br;q=1.0, gzip;q=0.8, *;q=0.1"
 * 
 * @author ZhangXi
 * @date 2021年10月20日
 */
public final class QualityValue {

	private String value;
	private float quality = 1;

	public QualityValue() {
		this(null, 1);
	}

	public QualityValue(String v, float q) {
		setQuality(q);
		value = v;
	}

	/**
	 * 获取值
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 设置值
	 */
	public void setValue(String v) {
		value = v;
	}

	/**
	 * 获取权重
	 */
	public float getQuality() {
		return quality;
	}

	/**
	 * 设置权重
	 */
	public void setQuality(float q) {
		if (q > 1 || q < 0) {
			throw new IllegalArgumentException("权重值不合法，应为0.0~1.0");
		}
		quality = q;
	}
}