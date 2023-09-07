/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * 支持校验的数据接口
 * 
 * @author ZhangXi
 * @date 2020年7月21日
 */
public interface Verifiable {

	/**
	 * 获取字节校验方式
	 */
	public Verifier getVerifier();

	/**
	 * 设置字节校验方式
	 */
	public void setVerifier(Verifier v);
}
