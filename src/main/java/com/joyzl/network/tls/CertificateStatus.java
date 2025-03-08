package com.joyzl.network.tls;

/**
 * 证书状态消息，由status_request(CertificateStatusRequest)扩展协商
 * 
 * <pre>
 * RFC 6066
 * 
 * struct {
 *        CertificateStatusType status_type;
 *        select (status_type) {
 *            case ocsp: OCSPResponse;
 *        } response;
 *    } CertificateStatus;
 * 
 * opaque OCSPResponse<1..2^24-1>;
 * </pre>
 * 
 * @author ZhangXi 2025年3月6日
 */
class CertificateStatus extends Handshake {

	/** CertificateStatusType */
	private byte type = CertificateStatusRequest.OCSP;
	/** OCSPResponse */
	private byte[] response = TLS.EMPTY_BYTES;

	@Override
	public byte msgType() {
		return CERTIFICATE_STATUS;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte value) {
		type = value;
	}

	public byte[] getResponse() {
		return response;
	}

	public void setResponse(byte[] value) {
		if (value == null) {
			response = TLS.EMPTY_BYTES;
		} else {
			response = value;
		}
	}
}