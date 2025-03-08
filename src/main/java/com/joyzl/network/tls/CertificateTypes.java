package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * <pre>
 * RFC7250
 * 
 * struct {
 *         select(ClientOrServerExtension) {
 *             case client:
 *               CertificateType server_certificate_types<1..2^8-1>;
 *             case server:
 *               CertificateType server_certificate_type;
 *         }
 * } ServerCertTypeExtension;
 * 
 * struct {
 *         select(ClientOrServerExtension) {
 *             case client:
 *               CertificateType client_certificate_types<1..2^8-1>;
 *             case server:
 *               CertificateType client_certificate_type;
 *         }
 * } ClientCertTypeExtension;
 * </pre>
 * 
 * @author ZhangXi 2025年3月6日
 */
abstract class CertificateTypes extends Extension {

	private byte[] types = TLS.EMPTY_BYTES;

	public CertificateTypes() {
	}

	public CertificateTypes(byte... types) {
		this.types = types;
	}

	public byte[] get() {
		return types;
	}

	public byte get(int index) {
		return types[index];
	}

	public void set(byte... value) {
		if (value == null) {
			types = TLS.EMPTY_BYTES;
		} else {
			types = value;
		}
	}

	public void add(byte value) {
		if (types == TLS.EMPTY_BYTES) {
			types = new byte[] { value };
		} else {
			types = Arrays.copyOf(types, types.length + 1);
			types[types.length - 1] = value;
		}
	}

	public int size() {
		return types.length;
	}

	static class ServerCertificateTypes extends CertificateTypes {

		@Override
		public short type() {
			return SERVER_CERTIFICATE_TYPE;
		}
	}

	static class ClientCertificateTypes extends CertificateTypes {

		@Override
		public short type() {
			return CLIENT_CERTIFICATE_TYPE;
		}
	}
}