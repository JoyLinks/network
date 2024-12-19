package com.joyzl.network.tls;

/**
 * <pre>
 * finished_key =
 *       HKDF-Expand-Label(BaseKey, "finished", "", Hash.length)
 * 
 * Structure of this message:
 *      struct {
 *            opaque verify_data[Hash.length];
 *      } Finished;
 * 
 * The verify_data value is computed as follows:
 * 
 *      verify_data =
 *            HMAC(finished_key,
 *                     Transcript-Hash(Handshake Context,
 *                                               Certificate*, CertificateVerify*))
 * 
 *      * Only included if present.
 * </pre>
 * 
 * @author ZhangXi 2024年12月19日
 */
public class Finished extends Handshake {

	private byte[] data;

	@Override
	public HandshakeType getMsgType() {
		return HandshakeType.FINISHED;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] value) {
		data = value;
	}
}