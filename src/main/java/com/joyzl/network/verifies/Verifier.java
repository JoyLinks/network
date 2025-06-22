/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
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