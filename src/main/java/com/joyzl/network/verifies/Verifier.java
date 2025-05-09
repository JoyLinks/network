/*-
 * www.joyzl.net
 * 重庆骄智科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.verifies;

/**
 * 字节校验接口
 * 
 * @author ZhangXi
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