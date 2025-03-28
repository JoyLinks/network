package com.joyzl.network.tls;

import java.util.Arrays;

/**
 * 扩展：协商证书状态请求
 * 
 * <pre>
 * RFC6066
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
 * opaque ResponderID<1..2^16-1>; // DER-encoded ASN.1
 * opaque Extensions<0..2^16-1>; // DER-encoded ASN.1 of OCSP request extensions
 * 
 * RFC2560 X.509 Internet Public Key Infrastructure Online Certificate Status Protocol - OCSP
 * </pre>
 * 
 * @author ZhangXi 2025年3月6日
 */
abstract class CertificateStatusRequest extends Extension {

	// CertificateStatusType MAX(255)

	public final static byte OCSP = 1;

	////////////////////////////////////////////////////////////////////////////////

	private byte statusType = OCSP;

	@Override
	public short type() {
		return STATUS_REQUEST;
	}

	public byte getStatusType() {
		return statusType;
	}

	public void setStatusType(byte value) {
		statusType = value;
	}

	static class OCSPStatusRequest extends CertificateStatusRequest {

		/** DER-encoded ASN.1 */
		private byte[][] responderIDs = TLS.EMPTY_BYTES_BYTES;
		/** DER-encoded ASN.1 */
		private byte[] requestExtensions = TLS.EMPTY_BYTES;

		public byte[][] getResponderIDs() {
			return responderIDs;
		}

		public byte[] getResponderID(int index) {
			return responderIDs[index];
		}

		public void addResponderID(byte[] value) {
			if (responderIDs == EMPTY_BYTES_BYTES) {
				responderIDs = new byte[][] { value };
			} else {
				responderIDs = Arrays.copyOf(responderIDs, responderIDs.length + 1);
				responderIDs[responderIDs.length - 1] = value;
			}
		}

		public void setResponderIDs(byte[][] value) {
			if (value == null) {
				responderIDs = TLS.EMPTY_BYTES_BYTES;
			} else {
				responderIDs = value;
			}
		}

		public int size() {
			return responderIDs.length;
		}

		public byte[] getRequestExtensions() {
			return requestExtensions;
		}

		public void setRequestExtensions(byte[] value) {
			if (value == null) {
				requestExtensions = TLS.EMPTY_BYTES;
			} else {
				requestExtensions = value;
			}
		}

		@Override
		public String toString() {
			return "status_request:OSCP";
		}
	}

	/**
	 * 特殊情况1.3结构为 CertificateStatus RFC6066
	 */
	static class OCSPResponse extends CertificateStatusRequest {

		private byte[] response;

		public byte[] get() {
			return response;
		}

		public void set(byte[] value) {
			response = value;
		}

		@Override
		public String toString() {
			return "status_response:OSCP";
		}
	}
}