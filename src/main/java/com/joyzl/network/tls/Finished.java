/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * TLS 1.3
 * 
 * struct {
 *       opaque verify_data[Hash.length];
 * } Finished;
 * </pre>
 * 
 * <pre>
 * TLS 1.2
 * 
 * struct {
 *       opaque verify_data[verify_data_length];
 * } Finished;
 * </pre>
 * 
 * <pre>
 * TLS 1.0 1.1
 * struct {
 *       opaque verify_data[12];
 * } Finished;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
class Finished extends Handshake {

	/** 对端发送的验证码 */
	private byte[] verifyData = TLS.EMPTY_BYTES;
	/** 本地生成的验证码 */
	private byte[] localData = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return FINISHED;
	}

	public byte[] getVerifyData() {
		return verifyData;
	}

	public void setVerifyData(byte[] value) {
		if (value == null) {
			verifyData = TLS.EMPTY_BYTES;
		} else {
			verifyData = value;
		}
	}

	@Override
	public String toString() {
		return name() + ":verify=" + verifyData.length + "byte";
	}

	public byte[] getLocalData() {
		return localData;
	}

	public void setLocalData(byte[] value) {
		if (value == null) {
			localData = TLS.EMPTY_BYTES;
		} else {
			localData = value;
		}
	}

	/**
	 * 验证双方验证码是否相同
	 */
	public boolean validate() {
		if (localData != verifyData) {
			return Arrays.equals(localData, localData);
		}
		return false;
	}
}