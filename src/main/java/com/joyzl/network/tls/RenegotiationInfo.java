/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：重协商，用于判断重协商的链接是不是原有链接的对端。
 * 
 * <pre>
 * RFC 5746 Transport Layer Security (TLS) Renegotiation Indication Extension
 * 
 * struct {
 *     opaque renegotiated_connection<0..255>;
 * } RenegotiationInfo;
 * 
 * 初始时：EMPTY/SCSV
 * 重协商时：
 * ClientHellos:client_verify_data
 * ServerHellos:client_verify_data + server_verify_data
 * </pre>
 * 
 * @author ZhangXi 2024年12月21日
 */
class RenegotiationInfo extends Extension {

	/** 向后兼容SSLv3 TLS 1.0 TLS 1.1 */
	public final static byte[] TLS_EMPTY_RENEGOTIATION_INFO_SCSV = new byte[] { 0x00, (byte) 0xFF };

	private byte[] value = TLS.EMPTY_BYTES;

	@Override
	public short type() {
		return RENEGOTIATION_INFO;
	}

	public byte[] get() {
		return value;
	}

	public void set(byte[] value) {
		if (value == null) {
			this.value = TLS.EMPTY_BYTES;
		} else {
			this.value = value;
		}
	}

	public boolean isEmpty() {
		return value.length == 0;
	}

	public boolean isSCSV() {
		return Arrays.equals(value, TLS_EMPTY_RENEGOTIATION_INFO_SCSV);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return name() + ":EMPTY";
		}
		if (isSCSV()) {
			return name() + ":SCSV";
		}
		return name() + ":" + value.length + "byte";
	}
}