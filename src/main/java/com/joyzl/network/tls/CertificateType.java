package com.joyzl.network.tls;

/**
 * <pre>
 * RFC7250
 * 
 * struct {
 *         select(ClientOrServerExtension) {
 *             case client:
 *               CertificateType client_certificate_types<1..2^8-1>;
 *             case server:
 *               CertificateType client_certificate_type;
 *         }
 * } ClientCertTypeExtension;
 * 
 * struct {
 *         select(ClientOrServerExtension) {
 *             case client:
 *               CertificateType server_certificate_types<1..2^8-1>;
 *             case server:
 *               CertificateType server_certificate_type;
 *         }
 * } ServerCertTypeExtension;
 * </pre>
 * 
 * @author ZhangXi 2025年3月6日
 */
abstract class CertificateType extends Extension {

	private byte type = CertificateV3.X509;

	public CertificateType() {
	}

	public CertificateType(byte type) {
		this.type = type;
	}

	public byte get() {
		return type;
	}

	public void set(byte value) {
		type = value;
	}

	static class ServerCertificateType extends CertificateType {
		@Override
		public short type() {
			return SERVER_CERTIFICATE_TYPE;
		}
	}

	static class ClientCertificateType extends CertificateType {
		@Override
		public short type() {
			return CLIENT_CERTIFICATE_TYPE;
		}
	}
}