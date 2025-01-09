package com.joyzl.network.tls;

/**
 * 握手消息
 * 
 * <pre>
      struct {
          HandshakeType msg_type;    / handshake type /
          uint24 length;             / remaining bytes in message /
          select (Handshake.msg_type) {
              case client_hello:          ClientHello;
              case server_hello:          ServerHello;
              case end_of_early_data:     EndOfEarlyData;
              case encrypted_extensions:  EncryptedExtensions;
              case certificate_request:   CertificateRequest;
              case certificate:           Certificate;
              case certificate_verify:    CertificateVerify;
              case finished:              Finished;
              case new_session_ticket:    NewSessionTicket;
              case key_update:            KeyUpdate;
          };
      } Handshake;
 * </pre>
 * 
 * @author ZhangXi 2024年12月13日
 */
public abstract class Handshake extends TLSPlaintext {

	// HandshakeType MAX(255)

	/** TLS 1.0 */
	public final static byte HELLO_REQUEST = 0;
	/** TLS 1.0 1.3 */
	public final static byte CLIENT_HELLO = 1;
	/** TLS 1.0 1.3 */
	public final static byte SERVER_HELLO = 2;
	/** TLS 1.3 新会话票据(PSK) */
	public final static byte NEW_SESSION_TICKET = 4;
	/** TLS 1.3 早期数据结束 */
	public final static byte END_OF_EARLY_DATA = 5;
	/** TLS 1.3 被保护的扩展 */
	public final static byte ENCRYPTED_EXTENSIONS = 8;
	/** TLS 1.0 1.3 终端的证书 */
	public final static byte CERTIFICATE = 11;
	/** TLS 1.0 */
	public final static byte SERVER_KEY_EXCHANGE = 12;
	/** TLS 1.0 1.3 请求客户端证书 */
	public final static byte CERTIFICATE_REQUEST = 13;
	/** TLS 1.0 */
	public final static byte SERVER_HELLO_DONE = 14;
	/** TLS 1.0 1.3 使用证书消息对整个握手消息进行签名 */
	public final static byte CERTIFICATE_VERIFY = 15;
	/** TLS 1.0 */
	public final static byte CLIENT_KEY_EXCHANGE = 16;
	/** TLS 1.0 1.3 整个握手消息的密钥确认 */
	public final static byte FINISHED = 20;

	public final static byte CERTIFICATE_URL = 21;

	public final static byte CERTIFICATE_STATUS = 22;
	/** TLS 1.3 更新密钥 */
	public final static byte KEY_UPDATE = 24;
	/** TLS 1.3 */
	public final static byte MESSAGE_HASH = (byte) 254;

	////////////////////////////////////////////////////////////////////////////////

	public abstract byte msgType();

	public boolean hasExtensions() {
		return false;
	}

	@Override
	public byte contentType() {
		return HANDSHAKE;
	}

	@Override
	public String toString() {
		return name();
	}

	public String name() {
		return name(msgType());
	}

	public final static String name(byte code) {
		if (code == HELLO_REQUEST) {
			return "hello_request";
		} else if (code == CLIENT_HELLO) {
			return "client_hello";
		} else if (code == SERVER_HELLO) {
			return "server_hello";
		} else if (code == NEW_SESSION_TICKET) {
			return "new_session_ticket";
		} else if (code == END_OF_EARLY_DATA) {
			return "end_of_early_data";
		} else if (code == ENCRYPTED_EXTENSIONS) {
			return "encrypted_extensions";
		} else if (code == CERTIFICATE) {
			return "certificate";
		} else if (code == SERVER_KEY_EXCHANGE) {
			return "server_key_exchange";
		} else if (code == CERTIFICATE_REQUEST) {
			return "certificate_request";
		} else if (code == SERVER_HELLO_DONE) {
			return "server_hello_done";
		} else if (code == CERTIFICATE_VERIFY) {
			return "certificate_verify";
		} else if (code == CLIENT_KEY_EXCHANGE) {
			return "client_key_exchange";
		} else if (code == FINISHED) {
			return "finished";
		} else if (code == CERTIFICATE_URL) {
			return "certificate_url";
		} else if (code == CERTIFICATE_STATUS) {
			return "certificate_status";
		} else if (code == KEY_UPDATE) {
			return "key_update";
		} else if (code == MESSAGE_HASH) {
			return "message_hash";
		} else {
			return "unknown";
		}
	}
}