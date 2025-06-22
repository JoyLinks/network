/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
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
