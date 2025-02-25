package com.joyzl.network.tls;

/**
 * 扩展：证书中签名算法
 * 
 * @author ZhangXi 2025年1月24日
 */
public class SignatureAlgorithmsCert extends SignatureAlgorithms {

	@Override
	public short type() {
		return SIGNATURE_ALGORITHMS_CERT;
	}
}