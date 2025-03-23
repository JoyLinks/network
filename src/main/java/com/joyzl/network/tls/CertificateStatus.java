package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 证书状态消息，由status_request(CertificateStatusRequest)扩展协商
 * 
 * <pre>
 * RFC 6066
 * 
 * struct {
 *       CertificateStatusType status_type;
 *       select (status_type) {
 *             case ocsp: OCSPResponse;
 *       } response;
 * } CertificateStatus;
 * 
 * opaque OCSPResponse<1..2^24-1>;
 * </pre>
 * 
 * <pre>
 * RFC 6961
 * 
 * struct {
 *       CertificateStatusType status_type;
 *       select (status_type) {
 *             case ocsp: OCSPResponse;
 *             case ocsp_multi: OCSPResponseList;
 *       } response;
 * } CertificateStatus;
 * 
 * opaque OCSPResponse<0..2^24-1>;
 * 
 * struct {
 *       OCSPResponse ocsp_response_list<1..2^24-1>;
 * } OCSPResponseList;
 * </pre>
 * 
 * @author ZhangXi 2025年3月6日
 */
class CertificateStatus extends Handshake {

	/** CertificateStatusType */
	private byte statusType = CertificateStatusRequest.OCSP;
	/** OCSPResponse/OCSPResponseList */
	private byte[][] responses = TLS.EMPTY_STRINGS;

	@Override
	public byte msgType() {
		return CERTIFICATE_STATUS;
	}

	public byte getStatusType() {
		return statusType;
	}

	public void setStatusType(byte value) {
		statusType = value;
	}

	public byte[] getResponse() {
		return responses[0];
	}

	public byte[] getResponse(int index) {
		return responses[index];
	}

	public void addResponse(byte[] value) {
		if (responses == TLS.EMPTY_STRINGS) {
			responses = new byte[][] { value };
		} else {
			responses = Arrays.copyOf(responses, responses.length + 1);
			responses[responses.length - 1] = value;
		}
	}

	public void setResponse(byte[]... values) {
		if (values == null) {
			responses = TLS.EMPTY_STRINGS;
		} else {
			responses = values;
		}
	}

	public int size() {
		return responses.length;
	}
}