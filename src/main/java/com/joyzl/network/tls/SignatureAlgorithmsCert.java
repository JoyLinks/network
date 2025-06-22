/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

/**
 * 扩展：证书中签名算法
 * 
 * @author ZhangXi 2025年1月24日
 */
class SignatureAlgorithmsCert extends SignatureAlgorithms {

	// 结构与SignatureAlgorithms完全相同

	public SignatureAlgorithmsCert() {
	}

	public SignatureAlgorithmsCert(short... value) {
		set(value);
	}

	@Override
	public short type() {
		return SIGNATURE_ALGORITHMS_CERT;
	}
}