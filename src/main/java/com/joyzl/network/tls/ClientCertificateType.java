package com.joyzl.network.tls;

/**
 * <pre>
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
 * @author ZhangXi 2024年12月19日
 */
class ClientCertificateType extends CertificateTypes {

	@Override
	public short type() {
		return CLIENT_CERTIFICATE_TYPE;
	}
}