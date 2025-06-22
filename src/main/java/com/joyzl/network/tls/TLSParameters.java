/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.nio.charset.StandardCharsets;

/**
 * 提供运行时参数
 * 
 * @author ZhangXi 2025年3月12日
 */
public class TLSParameters {

	/** 可用的协议 */
	private byte[][] alpns;
	/** 可用的密钥算法 */
	private short[] namedGroups = V3KeyExchange.AVAILABLES;
	/** 可用的加密套件 */
	private short[] cipherSuites = CipherSuiter.AVAILABLES;
	/** 可用的签名算法 */
	private short[] signatureSchemes = Signaturer.AVAILABLES;
	/** 可用的版本 */
	private short[] versions = new short[] { TLS.V13, TLS.V12 };
	/** 忽略证书有效性检查 */
	private boolean ignoreCertificate = false;

	/**
	 * 获取可用的版本
	 * 
	 * @see TLS
	 */
	public String[] getVersions() {
		final String[] values = new String[versions.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = TLS.version(versions[i]);
		}
		return values;
	}

	/**
	 * 设置可用的版本
	 * 
	 * @see TLS
	 */
	public void setVersions(String... values) {
		versions = new short[values.length];
		for (int i = 0; i < values.length; i++) {
			versions[i] = TLS.version(values[i]);
		}
	}

	/**
	 * 设置可用的版本
	 * 
	 * @see TLS
	 */
	public void setVersions(short... values) {
		versions = values;
	}

	/**
	 * 获取可用的版本代码组
	 */
	public short[] versions() {
		return versions;
	}

	/**
	 * 获取可用的密钥算法
	 * 
	 * @see SignatureScheme
	 */
	public String[] getSignatureSchemes() {
		final String[] values = new String[signatureSchemes.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = SignatureScheme.name(signatureSchemes[i]);
		}
		return values;
	}

	/**
	 * 设置可用的密钥算法
	 * 
	 * @see SignatureScheme
	 */
	public void setSignatureSchemes(String... values) {
		signatureSchemes = new short[values.length];
		for (int i = 0; i < values.length; i++) {
			signatureSchemes[i] = SignatureScheme.name(values[i]);
		}
	}

	/**
	 * 设置可用的密钥算法
	 * 
	 * @see SignatureScheme
	 */
	public void setSignatureSchemes(short... values) {
		signatureSchemes = values;
	}

	/**
	 * 获取可用的密钥算法代码组
	 */
	short[] signatureSchemes() {
		return signatureSchemes;
	}

	/**
	 * 获取可用的加密套件
	 * 
	 * @see CipherSuite
	 */
	public String[] getCipherSuites() {
		final String[] values = new String[namedGroups.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = CipherSuite.name(namedGroups[i]);
		}
		return values;
	}

	/**
	 * 设置可用的加密套件
	 * 
	 * @see CipherSuite
	 */
	public void setCipherSuites(String... values) {
		cipherSuites = new short[values.length];
		for (int i = 0; i < values.length; i++) {
			cipherSuites[i] = CipherSuite.name(values[i]);
		}
	}

	/**
	 * 设置可用的加密套件
	 * 
	 * @see CipherSuite
	 */
	public void setCipherSuites(short... values) {
		cipherSuites = values;
	}

	/**
	 * 获取可用的加密套件代码组
	 */
	short[] cipherSuites() {
		return cipherSuites;
	}

	/**
	 * 获取可用的密钥算法
	 * 
	 * @see NamedGroup
	 */
	public String[] getNamedGroups() {
		final String[] values = new String[namedGroups.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = NamedGroup.named(namedGroups[i]);
		}
		return values;
	}

	/**
	 * 设置可用的密钥算法
	 * 
	 * @see NamedGroup
	 */
	public void setNamedGroups(short... values) {
		namedGroups = values;
	}

	/**
	 * 设置可用的密钥算法
	 * 
	 * @see NamedGroup
	 */
	public void setNamedGroups(String... values) {
		namedGroups = new short[values.length];
		for (int i = 0; i < values.length; i++) {
			namedGroups[i] = NamedGroup.name(values[i]);
		}
	}

	/**
	 * 获取可用的密钥算法代码组
	 */
	short[] namedGroups() {
		return namedGroups;
	}

	/**
	 * 获取可用应用协议
	 * 
	 * @see ApplicationLayerProtocolNegotiation
	 */
	public String[] getAlpns() {
		final String[] values = new String[alpns.length];
		for (int i = 0; i < values.length; i++) {
			values[i] = new String(alpns[i], StandardCharsets.US_ASCII);
		}
		return values;
	}

	/**
	 * 设置可用应用协议
	 * 
	 * @see ApplicationLayerProtocolNegotiation
	 */
	public void setAlpns(String... values) {
		alpns = new byte[values.length][];
		for (int i = 0; i < values.length; i++) {
			alpns[i] = values[i].toLowerCase().getBytes(StandardCharsets.US_ASCII);
		}
	}

	/**
	 * 获取可用应用协议编码组
	 */
	byte[][] alpns() {
		return alpns;
	}

	static short[] join(short[] a, short[] b) {
		short[] c = new short[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}

	/**
	 * 是否忽略证书有效性检查
	 */
	public boolean isIgnoreCertificate() {
		return ignoreCertificate;
	}

	/**
	 * 是否忽略证书有效性检查
	 */
	public void setIgnoreCertificate(boolean value) {
		ignoreCertificate = value;
	}
}