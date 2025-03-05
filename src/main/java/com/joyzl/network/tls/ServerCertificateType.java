package com.joyzl.network.tls;

/**
 * <pre>
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
 * @author ZhangXi 2024年12月19日
 */
class ServerCertificateType extends CertificateTypes {

	@Override
	public short type() {
		return SERVER_CERTIFICATE_TYPE;
	}
}