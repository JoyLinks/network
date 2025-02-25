package com.joyzl.network.tls;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 终端支持的证书颁发机构CA(Certificate Authority)
 * 
 * <pre>
 * opaque DistinguishedName<1..2^16-1>;
 * 
 * struct {
 *     DistinguishedName authorities<3..2^16-1>;
 * } CertificateAuthoritiesExtension;
 * 
 * X501 DER X690
 * https://www.itu.int/rec/T-REC-X.501/en
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class CertificateAuthorities extends Extension {

	private final static byte[][] EMPTY = new byte[0][];
	private byte[][] authorities;

	@Override
	public short type() {
		return CERTIFICATE_AUTHORITIES;
	}

	public byte[][] get() {
		return authorities;
	}

	public byte[] get(int index) {
		return authorities[index];
	}

	public void set(byte[]... value) {
		if (value == null) {
			authorities = EMPTY;
		} else {
			authorities = value;
		}
	}

	public void add(byte[] value) {
		if (authorities == EMPTY) {
			authorities = new byte[][] { value };
		} else {
			authorities = Arrays.copyOf(authorities, authorities.length + 1);
			authorities[authorities.length - 1] = value;
		}
	}

	public int size() {
		return authorities.length;
	}

	@Override
	public String toString() {
		return "certificate_authorities:" + authorities.length;
	}

	//////////

	/**
	 * 加载默认信任CA，位于JAVA-HOME/lib/security/cacerts
	 */
	public static List<X509Certificate> load() throws Exception {
		final TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init((KeyStore) null);

		final List<X509Certificate> certificates = new ArrayList<>();
		final TrustManager[] managers = factory.getTrustManagers();
		for (TrustManager manager : managers) {
			if (manager instanceof X509TrustManager) {
				final X509TrustManager m = (X509TrustManager) manager;
				final X509Certificate[] issuers = m.getAcceptedIssuers();
				for (X509Certificate issuer : issuers) {
					certificates.add(issuer);
				}
			}
		}
		return certificates;
	}

	/**
	 * 加载指定信任CA
	 */
	public static List<X509Certificate> load(File cacerts, String password) throws Exception {
		final KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
		try (FileInputStream input = new FileInputStream(cacerts)) {
			store.load(input, password.toCharArray());
		}

		java.security.cert.Certificate cert;
		final List<X509Certificate> certificates = new ArrayList<>();
		final Enumeration<String> aliases = store.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			if (store.isCertificateEntry(alias)) {
				cert = store.getCertificate(alias);
				if (cert instanceof X509Certificate) {
					certificates.add((X509Certificate) cert);
				}
			}
		}
		return certificates;
	}

	public byte[][] names(List<X509Certificate> certificates) {
		final byte[][] dns = new byte[certificates.size()][];
		for (int index = 0; index < certificates.size(); index++) {
			dns[index] = certificates.get(index).getIssuerX500Principal().getEncoded();
		}
		return dns;
	}
}