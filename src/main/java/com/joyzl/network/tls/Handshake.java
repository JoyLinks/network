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

	public abstract HandshakeType getMsgType();

	@Override
	public ContentType contentType() {
		return ContentType.HANDSHAKE;
	}

	@Override
	public String toString() {
		return getMsgType().name();
	}
}