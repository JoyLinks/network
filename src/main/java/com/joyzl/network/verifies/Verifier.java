/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 * 2018年9月18日
 */
package com.joyzl.network.verifies;

/**
 * 字节校验接口
 * 
 * @author simon(ZhangXi TEL:13883833982)
 *
 */
public abstract class Verifier {

	/**
	 * 计算并校验字节
	 * 
	 * @return value 原样返回
	 */
	public abstract byte check(byte value);

	/**
	 * 校验结果值
	 */
	public abstract int value();

	/**
	 * 重置校验
	 */
	public abstract void reset();
}