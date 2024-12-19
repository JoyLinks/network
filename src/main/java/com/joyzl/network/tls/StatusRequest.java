package com.joyzl.network.tls;

/**
 * 
 * <pre>
 * RFC 6066
 * 
 * struct {
 *     CertificateStatusType status_type;
 *     select (status_type) {
 *           case ocsp: OCSPStatusRequest;
 *     } request;
 * } CertificateStatusRequest;
 * 
 * enum { ocsp(1), (255) } CertificateStatusType;
 * 
 * struct {
 *     ResponderID responder_id_list<0..2^16-1>;
 *     Extensions  request_extensions;
 * } OCSPStatusRequest;
 * 
 * opaque ResponderID<1..2^16-1>;
 * opaque Extensions<0..2^16-1>;
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class StatusRequest extends Extension {

	private CertificateStatusRequest request;

	@Override
	public ExtensionType type() {
		return ExtensionType.STATUS_REQUEST;
	}

	public CertificateStatusRequest getRequest() {
		return request;
	}

	public void setRequest(CertificateStatusRequest value) {
		request = value;
	}

	@Override
	public String toString() {
		return "status_request:" + request;
	}
}