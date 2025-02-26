package com.joyzl.network.tls;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供终端可用与会话的证书
 * 
 * @author ZhangXi 2025年2月25日
 */
public class SessionCertificates {

	/** SNI,Entry */
	private final static Map<String, CPK> ENTRIES = new HashMap<>();

	public static CPK get(String name) {
		return ENTRIES.get(name);
	}

	public static String checkString() {
		final StringBuilder b = new StringBuilder();
		for (Map.Entry<String, CPK> e : ENTRIES.entrySet()) {
			b.append(e.getKey());
			b.append(':');
			b.append(e.getValue());
			b.append('\n');
		}
		return b.toString();
	}

	// TLS 1.3 证书类型必须是X.509v3[RFC5280]，除非另有明确协商[RFC5081]

	// DER: Distinguished Encoding Rules 证书的二进制编码
	// PEM: Privacy Enhanced Mail [RFC7468] 证书的Base64编码
	// PKCS #10: Certification Request Syntax [RFC2986]
	// PKCS #7 Cryptographic Message Syntax [RFC2315]
	// PKCS #8 Private-Key Information Syntax [RFC5208][RFC5958]
	// CMS: Cryptographic Message Syntax [RFC5652]
	// CRLs: Certificate Revocation Lists [RFC5280]

	// 证书文件的扩展名
	// PFX: 多证书和私钥，密码保护
	// PEM: 证书或私钥
	// CER: 二进制证书，不含私钥
	// DER: 二进制证书，不含私钥
	// CRT: 二进制证书，不含私钥
	// KEY: 证书对应的私钥
	// JKS: Java KeyStore，多证书和私钥，密码保护
	// P12: 同PFX

	/**
	 * 加载包含证书链和私钥的证书文件，通常需要密码用于读取并解锁文件内容; <br>
	 * 扩展名可能为 .pfx .p12 .jks
	 */
	public static void loadKeyStore(File c, String pwd) throws Exception {
		final char[] password = pwd == null ? null : pwd.toCharArray();
		final KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
		try (FileInputStream input = new FileInputStream(c)) {
			store.load(input, password);
		}
		if (store.size() > 0) {
			final Enumeration<String> aliases = store.aliases();
			while (aliases.hasMoreElements()) {
				final String alias = aliases.nextElement();
				final PrivateKey key = (PrivateKey) store.getKey(alias, password);
				if (key != null) {
					final Certificate[] certificates = store.getCertificateChain(alias);
					if (certificates != null) {
						entry(key, certificates);
					} else {
						final Certificate certificate = store.getCertificate(alias);
						if (certificate != null) {
							entry(key, certificate);
						} else {
							throw new Exception("未能获取证书" + c);
						}
					}
				} else {
					throw new Exception("未能获取私钥" + c);
				}
			}
		} else {
			throw new Exception("未包含证书" + c);
		}
	}

	/**
	 * 加载证书文件和对应的私钥文件；<br>
	 * 证书文件扩展名可能为 .pem .cer .der.crt，私钥文件扩展名可能为 .key
	 */
	public static void loadKeyCertificate(File c, File k) throws Exception {
		final PrivateKey key = loadPrivateKey(k);
		final CertificateFactory factory = CertificateFactory.getInstance("X.509");
		final Collection<? extends Certificate> certificates;
		try (FileInputStream input = new FileInputStream(c)) {
			certificates = factory.generateCertificates(input);
		}
		if (certificates.size() > 0) {
			entry(key, certificates.toArray(new Certificate[0]));
		} else {
			throw new Exception("未包含证书" + c);
		}
	}

	/**
	 * 加载私钥文件(PKCS #8)；<br>
	 * 扩展名可能为 .key .pem
	 */
	public static PrivateKey loadPrivateKey(File k) throws Exception {
		final PEM pem = PEM.load(k);
		if (pem.getLabel().endsWith(" PRIVATE KEY")) {
			final String algorithm = pem.getLabel().substring(0, pem.getLabel().length() - 12);
			final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pem.getData());
			final KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
			return keyFactory.generatePrivate(keySpec);
		} else {
			throw new Exception("无效私钥文件" + k);
		}
	}

	/**
	 * 创建并缓存证书集，以证书集中的第一项证书中包含的名称为键
	 */
	static void entry(PrivateKey key, Certificate... certificates) throws Exception {
		final CertificateEntry[] entries = new CertificateEntry[certificates.length];
		X509Certificate x509;
		for (int index = 0; index < certificates.length; index++) {
			x509 = (X509Certificate) certificates[index];
			entries[index] = new CertificateEntry(x509.getEncoded());
		}
		x509 = (X509Certificate) certificates[0];
		final short scheme = Signaturer.scheme(x509.getSigAlgName());
		final CPK entry = new CPK(entries, key, scheme);
		final Collection<List<?>> names = x509.getSubjectAlternativeNames();
		for (List<?> name : names) {
			// [0 Integer,1 String]
			ENTRIES.put(name.get(1).toString(), entry);
		}
		x509.getIssuerX500Principal();
		x509.getExtendedKeyUsage();
		x509.getExtensionValue(null);
	}

	/**
	 * 对应SNI的证书集(证书链)
	 * 
	 * @author ZhangXi 2025年2月25日
	 */
	public static class CPK {
		private final CertificateEntry[] entries;
		private final PrivateKey privateKey;
		private final short scheme;

		public CPK(CertificateEntry[] entries, PrivateKey key, short scheme) {
			this.entries = entries;
			this.scheme = scheme;
			privateKey = key;
		}

		public CertificateEntry[] getEntries() {
			return entries;
		}

		public PrivateKey getPrivateKey() {
			return privateKey;
		}

		public short getScheme() {
			return scheme;
		}

		@Override
		public String toString() {
			return SignatureScheme.named(scheme) + " " + entries.length;
		}
	}
}