package com.joyzl.network.tls;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

/**
 * 提供终端可用与会话的证书
 * 
 * @author ZhangXi 2025年2月25日
 */
public class SessionCertificates {

	/** SNI,CPK */
	private final static Map<String, CPK> ENTRIES = new HashMap<>();
	/** CA */
	private final static Set<X500Principal> CAS = new HashSet<>();

	public static CPK get(String name) {
		return ENTRIES.get(name);
	}

	public static CPK get(ServerName name) {
		return ENTRIES.get(name.getNameString());
	}

	public static CPK get(ServerName... names) {
		CPK cpk;
		for (int i = 0; i < names.length; i++) {
			cpk = ENTRIES.get(names[i].getNameString());
			if (cpk != null) {
				return cpk;
			}
		}
		return null;
	}

	public static Collection<CPK> all() {
		return Collections.unmodifiableCollection(ENTRIES.values());
	}

	/**
	 * 根据用户提供的证书构造信任证书签发机构扩展对象(CertificateAuthorities)
	 */
	static CertificateAuthorities makeCASExtension() {
		final CertificateAuthorities cas = new CertificateAuthorities();
		for (X500Principal p : CAS) {
			cas.add(p.getEncoded());
		}
		return cas;
	}

	/**
	 * 根据用户指定构造客户端证书筛选扩展对象(OIDFilters)
	 */
	static OIDFilters makeOIDFiltersExtension() {
		final OIDFilters filters = new OIDFilters();
		// TODO 须进一步实现
		return filters;
	}

	public static String checkString() {
		final StringBuilder b = new StringBuilder();
		b.append("CERTIFICATES:");
		for (Map.Entry<String, CPK> e : ENTRIES.entrySet()) {
			b.append('\n');
			b.append('\t');
			b.append(e.getKey());
			b.append(':');
			b.append(' ');
			b.append(e.getValue());
		}
		b.append("\nAUTHORITIES:");
		for (X500Principal p : CAS) {
			b.append('\n');
			b.append('\t');
			b.append(p.getName());
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
						create(key, certificates);
					} else {
						final Certificate certificate = store.getCertificate(alias);
						if (certificate != null) {
							create(key, certificate);
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
			create(key, certificates.toArray(new Certificate[0]));
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
	static void create(PrivateKey key, Certificate... certificates) throws Exception {
		final X509Certificate[] x509s = new X509Certificate[certificates.length];
		for (int i = 0; i < certificates.length; i++) {
			x509s[i] = (X509Certificate) certificates[i];
			CAS.add(x509s[i].getIssuerX500Principal());
		}
		final X509Certificate x509 = (X509Certificate) certificates[0];
		final CPK cpk = new CPK(x509s, key);
		final Collection<List<?>> names = x509.getSubjectAlternativeNames();
		for (List<?> name : names) {
			// [0 Integer,1 String]
			ENTRIES.put(name.get(1).toString(), cpk);
		}
	}

	/**
	 * 对应SNI的证书集(证书链)
	 * 
	 * @author ZhangXi 2025年2月25日
	 */
	static class CPK {
		private final X509Certificate[] certificates;
		private final CertificateEntry[] entries;
		private final PrivateKey privateKey;
		private final short scheme;

		public CPK(X509Certificate[] certificates, PrivateKey privateKey) throws CertificateException {
			this.certificates = certificates;
			this.privateKey = privateKey;
			scheme = Signaturer.scheme(certificates[0].getSigAlgName());
			entries = new CertificateEntry[certificates.length];
			for (int i = 0; i < certificates.length; i++) {
				entries[i] = new CertificateEntry(certificates[i].getEncoded());
			}
		}

		public boolean check(OIDFilters filters) {
			final X509Certificate certificate = certificates[0];
			OIDFilter filter;
			Set<String> oids;
			byte[] value;
			for (int f = 0; f < filters.size(); f++) {
				filter = filters.get(f);
				oids = certificate.getCriticalExtensionOIDs();
				if (oids == null || !oids.contains(filter.getOIDString())) {
					oids = certificate.getNonCriticalExtensionOIDs();
					if (oids == null || !oids.contains(filter.getOIDString())) {
						return false;
					}
				}
				for (int v = 0; v < filter.valueSize(); v++) {
					value = certificate.getExtensionValue(filter.getOIDString());
					if (value == null) {
						return false;
					}
					if (Arrays.equals(filter.getValues(v), value)) {
						continue;
					}
				}
			}
			return true;
		}

		public boolean check(CertificateAuthorities cas) {
			X509Certificate certificate;
			for (int c = 0; c < certificates.length; c++) {
				certificate = certificates[c];
				for (int a = 0; a < cas.size(); a++) {
					if (Arrays.equals(cas.get(a), certificate.getIssuerX500Principal().getEncoded())) {
						return true;
					}
				}
			}
			return false;
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

	/**
	 * 加载指定信任证书签发机构的证书，默认位于JAVA-HOME/lib/security/cacerts，默认密码 changeit
	 */
	public static void load(File cacerts, String password) throws Exception {
		final KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
		try (FileInputStream input = new FileInputStream(cacerts)) {
			store.load(input, password.toCharArray());
		}

		Certificate cert;
		final Enumeration<String> aliases = store.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			if (store.isCertificateEntry(alias)) {
				cert = store.getCertificate(alias);
				if (cert instanceof X509Certificate x509) {
					CAS.add(x509.getIssuerX500Principal());
				}
			}
		}
	}
}