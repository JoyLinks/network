/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import com.joyzl.network.tls.CertificateV3.CertificateEntry;

/**
 * 提供终端可用与会话的证书
 * 
 * @author ZhangXi 2025年2月25日
 */
public class SessionCertificates {

	/** 信任机构证书 */
	private final static Map<X500Principal, X509Certificate> CAS = new HashMap<>();
	/** 本地证书 SNI,CPK */
	private final static Map<String, LocalCache> LOCALS = new HashMap<>();
	/** 远端证书 SNI,CPK */
	private final static Map<String, RemoteCache> REMOTES = new HashMap<>();

	/**
	 * 获取指定名称的本地证书
	 */
	public static LocalCache getLocal(String name) {
		return LOCALS.get(name);
	}

	/**
	 * 获取指定名称的远端证书
	 */
	public static RemoteCache getRemote(String name) {
		return REMOTES.get(name);
	}

	public static Collection<LocalCache> allLocals() {
		return Collections.unmodifiableCollection(LOCALS.values());
	}

	public static Collection<RemoteCache> allRemotes() {
		return Collections.unmodifiableCollection(REMOTES.values());
	}

	public static String checkString() {
		final StringBuilder b = new StringBuilder();
		b.append("CERTIFICATES:");
		for (Map.Entry<String, LocalCache> e : LOCALS.entrySet()) {
			b.append('\n');
			b.append('\t');
			b.append(e.getKey());
			b.append(':');
			b.append(' ');
			b.append(e.getValue());
		}
		b.append("\nAUTHORITIES:");
		for (X509Certificate p : CAS.values()) {
			b.append('\n');
			b.append('\t');
			b.append(p.getSubjectX500Principal());
		}
		return b.toString();
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 获取指定名称的本地证书
	 */
	static LocalCache getLocal(ServerName name) {
		return LOCALS.get(name.getNameString());
	}

	/**
	 * 获取指定名称集的本地证书
	 */
	static LocalCache getLocal(ServerNames names) {
		LocalCache cpk;
		for (int i = 0; i < names.size(); i++) {
			cpk = LOCALS.get(names.get(i).getNameString());
			if (cpk != null) {
				return cpk;
			}
		}
		return null;
	}

	/**
	 * 根据用户提供的证书构造信任证书签发机构扩展对象(CertificateAuthorities)
	 */
	static CertificateAuthorities makeCASExtension() {
		final CertificateAuthorities cas = new CertificateAuthorities();
		for (X509Certificate c : CAS.values()) {
			cas.add(c.getIssuerX500Principal().getEncoded());
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

	/**
	 * 根据指定扩展参数筛选本地证书
	 */
	static LocalCache filters(CertificateAuthorities cas, OIDFilters oid) {
		if (LOCALS.isEmpty()) {
			return null;
		}
		for (LocalCache c : LOCALS.values()) {
			if (cas != null) {
				if (c.check(cas)) {
					if (oid != null) {
						if (c.check(oid)) {
							return c;
						}
					} else {
						return c;
					}
				}
			}
		}
		return null;
	}

	////////////////////////////////////////////////////////////////////////////////
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

	static {
		// 加载默认信任机构证书
		final String JAVA_HOME = System.getProperty("java.home");
		if (JAVA_HOME != null) {
			final File file = new File(JAVA_HOME, "/lib/security/cacerts");
			if (file.exists()) {
				try {
					loadAuthorities(file, "changeit");
				} catch (Exception e) {
					// 如果用户更改了文件位置和密码，应手动加载
					// 缺失信任机构证书将导致证书链验证失败
					// 忽略此异常
				}
			}
		}
	}

	/**
	 * 加载指定信任证书签发机构的证书，默认位于JAVA-HOME/lib/security/cacerts，默认密码 changeit
	 */
	public static void loadAuthorities(File cacerts, String password) throws Exception {
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
					CAS.put(x509.getSubjectX500Principal(), x509);
				}
			}
		}
	}

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
						createLocalCache(key, certificates);
					} else {
						final Certificate certificate = store.getCertificate(alias);
						if (certificate != null) {
							createLocalCache(key, certificate);
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
			createLocalCache(key, certificates.toArray(new Certificate[0]));
		} else {
			throw new Exception("未包含证书" + c);
		}
	}

	/**
	 * 加载私钥文件(PKCS #8)；<br>
	 * 扩展名可能为 .key .pem
	 */
	public static PrivateKey loadPrivateKey(File k) throws Exception {
		final PEM pem = PEM.loadFile(k);
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
	 * 加载对端证书并缓存
	 */
	static RemoteCache loadCertificate(String name, CertificateV3 c) throws Exception {
		final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		final Certificate[] certificates = new Certificate[c.size()];
		for (int index = 0; index < c.size(); index++) {
			if (c.get(index).type() == CertificateV3.X509) {
				certificates[index] = certificateFactory.generateCertificate(new ByteArrayInputStream(c.get(index).getData()));
			}
		}
		return createRemoteCache(name, certificates);
	}

	/**
	 * 加载对端证书并缓存
	 */
	static RemoteCache loadCertificate(String name, CertificateV0 c) throws Exception {
		final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
		final Certificate[] certificates = new Certificate[c.size()];
		for (int index = 0; index < c.size(); index++) {
			certificates[index] = certificateFactory.generateCertificate(new ByteArrayInputStream(c.get(index)));
		}
		return createRemoteCache(name, certificates);
	}

	/**
	 * 创建并缓存证书集，以证书集中的第一项证书中包含的名称为键
	 */
	static void createLocalCache(PrivateKey key, Certificate... certificates) throws Exception {
		final X509Certificate[] x509s = new X509Certificate[certificates.length];
		for (int i = 0; i < certificates.length; i++) {
			x509s[i] = (X509Certificate) certificates[i];
			CAS.put(x509s[i].getSubjectX500Principal(), x509s[i]);
		}
		final X509Certificate x509 = (X509Certificate) certificates[0];
		final LocalCache local = new LocalCache(x509s, key);

		// 绑定证书名称，服务端和客户端通过名称获取证书
		final Collection<List<?>> names = x509.getSubjectAlternativeNames();
		if (names != null) {
			// SAN
			for (List<?> name : names) {
				// [0 Integer,1 String]
				LOCALS.put(name.get(1).toString(), local);
			}
		} else {
			// CN
			// CN=Simon, OU=JOYZL, O=JOYZL, L=Chongqing, ST=Chongqing, C=CN
			String cn = x509.getSubjectX500Principal().getName();
			int a = cn.indexOf("CN=");
			int b = cn.indexOf(',');
			if (a >= 0 && b > a) {
				cn = cn.substring(a + 3, b);
				if (cn != null && cn.length() > 0) {
					LOCALS.put(cn, local);
					return;
				}
			}
		}
	}

	/**
	 * 创建并缓存证书集，以证书集中的第一项证书中包含的名称为键
	 */
	static RemoteCache createRemoteCache(String name, Certificate... certificates) throws Exception {
		final X509Certificate[] x509s = new X509Certificate[certificates.length];
		for (int i = 0; i < certificates.length; i++) {
			x509s[i] = (X509Certificate) certificates[i];
		}
		final RemoteCache cache = new RemoteCache(x509s);
		if (name != null) {
			REMOTES.put(name, cache);
		}
		return cache;
	}

	/**
	 * 本地对应SNI的证书集(证书链)
	 * 
	 * @author ZhangXi 2025年2月25日
	 */
	public static class LocalCache {
		private final X509Certificate[] certificates;
		private final CertificateEntry[] entries;
		private final PrivateKey privateKey;
		private final short scheme;

		LocalCache(X509Certificate[] certificates, PrivateKey privateKey) throws CertificateException {
			this.certificates = certificates;
			this.privateKey = privateKey;
			scheme = Signaturer.scheme(certificates[0].getSigAlgName());
			entries = new CertificateEntry[certificates.length];
			for (int i = 0; i < certificates.length; i++) {
				entries[i] = new CertificateEntry(certificates[i].getEncoded());
			}
		}

		boolean check(OIDFilters filters) {
			final X509Certificate certificate = getCertificates()[0];
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

		boolean check(CertificateAuthorities cas) {
			X509Certificate certificate;
			for (int c = 0; c < getCertificates().length; c++) {
				certificate = getCertificates()[c];
				for (int a = 0; a < cas.size(); a++) {
					if (Arrays.equals(cas.get(a), certificate.getIssuerX500Principal().getEncoded())) {
						return true;
					}
				}
			}
			return false;
		}

		public X509Certificate[] getCertificates() {
			return certificates;
		}

		CertificateEntry[] getEntries() {
			return entries;
		}

		PrivateKey getPrivateKey() {
			return privateKey;
		}

		short getScheme() {
			return scheme;
		}

		@Override
		public String toString() {
			return SignatureScheme.name(scheme) + " " + certificates.length;
		}
	}

	/**
	 * 远端对应SNI的证书集
	 * 
	 * @author ZhangXi 2025年3月9日
	 */
	public static class RemoteCache {
		private final X509Certificate[] certificates;
		private final short scheme;

		RemoteCache(X509Certificate[] certificates) throws CertificateException {
			this.certificates = certificates;
			scheme = Signaturer.scheme(certificates[0].getSigAlgName());
		}

		public X509Certificate[] getCertificates() {
			return certificates;
		}

		PublicKey getPublicKey() {
			return certificates[0].getPublicKey();
		}

		short getScheme() {
			return scheme;
		}

		@Override
		public String toString() {
			return SignatureScheme.name(scheme) + " " + certificates.length;
		}
	}

	/**
	 * 检查证书是否有效
	 */
	public static void check(X509Certificate... certificates) throws Exception {
		X509Certificate c, previous = null;
		for (int i = 0; i < certificates.length; i++) {
			c = certificates[i];
			c.checkValidity();
			if (previous != null) {
				previous.verify(c.getPublicKey());
			}
			previous = c;
		}
		if (previous != null) {
			if (CAS.containsKey(previous.getSubjectX500Principal())) {
				return;
			} else {
				c = CAS.get(previous.getIssuerX500Principal());
				if (c != null) {
					previous.verify(c.getPublicKey());
					return;
				} else {
					throw new CertificateException("invalid CA");
				}
			}
		} else {
			throw new CertificateException("invalid Certificate");
		}
	}
}