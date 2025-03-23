package com.joyzl.network.tls;

import java.util.Arrays;

import com.joyzl.network.tls.CertificateStatusRequest.OCSPStatusRequest;

/**
 * 扩展：证书状态请求协商V2，1.3已废弃
 * 
 * <pre>
 * RFC 6961 
 * 
 * struct {
 *       CertificateStatusType status_type;
 *       uint16 request_length; // Length of request field in bytes
 *       select (status_type) {
 *             case ocsp: OCSPStatusRequest;
 *             case ocsp_multi: OCSPStatusRequest;
 *       } request;
 * } CertificateStatusRequestItemV2;
 * 
 * enum { ocsp(1), ocsp_multi(2), (255) } CertificateStatusType;
 * 
 * struct {
 *       ResponderID responder_id_list<0..2^16-1>;
 *       Extensions request_extensions;
 * } OCSPStatusRequest;
 * 
 * opaque ResponderID<1..2^16-1>;
 * opaque Extensions<0..2^16-1>;
 * 
 * struct {
 *       CertificateStatusRequestItemV2 certificate_status_req_list<1..2^16-1>;
 * } CertificateStatusRequestListV2;
 * </pre>
 * 
 * @author ZhangXi 2025年3月13日
 */
public class CertificateStatusRequestListV2 extends Extension {

	public final static byte OCSP_MULTI = 2;

	////////////////////////////////////////////////////////////////////////////////

	final static CertificateStatusRequestItemV2[] EMPTY = new CertificateStatusRequestItemV2[0];
	private CertificateStatusRequestItemV2[] items = EMPTY;

	@Override
	public short type() {
		return STATUS_REQUEST_V2;
	}

	public CertificateStatusRequestItemV2[] get() {
		return items;
	}

	public CertificateStatusRequestItemV2 get(int index) {
		return items[index];
	}

	public void add(CertificateStatusRequestItemV2 value) {
		if (items == EMPTY) {
			items = new CertificateStatusRequestItemV2[] { value };
		} else {
			items = Arrays.copyOf(items, items.length + 1);
			items[items.length - 1] = value;
		}
	}

	public void set(CertificateStatusRequestItemV2[] value) {
		if (value == null) {
			items = EMPTY;
		} else {
			items = value;
		}
	}

	public int size() {
		return items.length;
	}

	@Override
	public String toString() {
		return name() + ":" + size();
	}

	static class CertificateStatusRequestItemV2 extends OCSPStatusRequest {
		public CertificateStatusRequestItemV2() {
			setStatusType(OCSP_MULTI);
		}
	}
}