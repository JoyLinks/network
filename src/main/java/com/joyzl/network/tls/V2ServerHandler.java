/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.tls;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.codec.Binary;

/**
 * TLSServerHandler
 * 
 * @author ZhangXi 2025年2月14日
 */
public class V2ServerHandler extends ServerHandler {

	public V2ServerHandler(ChainHandler handler, TLSParameters parameters) {
		super(handler, parameters);
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.setContext(new TLSContext());
		chain.receive();
	}

	@Override
	public Object decode(ChainChannel chain, DataBuffer buffer) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
		final int type;
		try {
			type = RecordCoder.decode(context.cipher, buffer, context.data);
		} catch (Exception e) {
			if (e instanceof TLSException) {
				return new Alert((TLSException) e);
			} else {
				throw e;
			}
		}
		if (type < 0) {
			return null;
		} else if (type == Record.APPLICATION_DATA) {
			Object message = handler.decode(chain, context.data);
			while (message != null && context.data.readable() > 0) {
				handler.received(chain, message);
				message = handler.decode(chain, context.data);
			}
			return message;
		} else if (type == Record.HANDSHAKE) {
			// 解码后可能会保留数据用于消息哈希
			// 因此context.length表示保留的数据
			Handshake handshake = decode(context, context.data);
			while (handshake != null && context.data.readable() > context.length) {
				received(chain, handshake);
				handshake = decode(context, context.data);
			}
			return handshake;
		} else if (type == Record.CHANGE_CIPHER_SPEC) {
			return RecordCoder.decodeChangeCipherSpec(context.data);
		} else if (type == Record.HEARTBEAT) {
			return RecordCoder.decodeHeartbeat(context.data);
		} else if (type == Record.ALERT) {
			return RecordCoder.decodeAlert(context.data);
		} else {
			return new TLSException(Alert.UNEXPECTED_MESSAGE);
		}
	}

	private Handshake decode(TLSContext context, DataBuffer buffer) throws Exception {
		if (buffer.readable() < 4) {
			return null;
		}
		// 获取消息类型
		final byte type = buffer.get(0);
		// 获取消息字节数(type 1byte + length uint24 + data)
		final int length = Binary.join((byte) 0, buffer.get(1), buffer.get(2), buffer.get(3)) + 4;
		// 校验数据长度
		if (buffer.readable() < length) {
			return null;
		}
		if (type == Handshake.CLIENT_HELLO) {
			// 握手消息应保留数据用于TranscriptHash
			buffer.mark();
			final Handshake handshake = HandshakeCoder.decodeV2(buffer);
			// 待协商后执行消息哈希
			context.length = length;
			buffer.reset();
			return handshake;
		} else if (type == Handshake.FINISHED) {
			// 客户端完成消息校验码
			final byte[] local = context.secret.clientFinished();
			context.secret.hash(buffer, length);

			final Finished finished = (Finished) HandshakeCoder.decodeV2(buffer);
			finished.setLocalData(local);
			return finished;
		} else if (type == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			return HandshakeCoder.decodeV2(buffer);
		} else {
			context.secret.hash(buffer, length);
			return HandshakeCoder.decodeV2(buffer);
		}
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
		if (message instanceof Record record) {
			// TLS RECORD
			if (record.contentType() == Record.HANDSHAKE) {
				if (record instanceof Handshakes handshakes) {
					// 握手消息不与其他消息交错
					// 握手消息不能跨越密钥更改
					final DataBuffer data = DataBuffer.instance();
					final DataBuffer buffer = DataBuffer.instance();
					for (int i = 0; i < handshakes.size(); i++) {
						encode(context, handshakes.get(i), data);
						if (handshakes.get(i).msgType() == Handshake.SERVER_HELLO) {
							RecordCoder.encodePlaintextV2(record, data, buffer);
							RecordCoder.encodeV2(ChangeCipherSpec.INSTANCE, buffer);
						} else {
							RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
						}
					}
					data.release();
					return buffer;
				} else {
					final DataBuffer data = DataBuffer.instance();
					encode(context, (Handshake) record, data);
					final DataBuffer buffer = DataBuffer.instance();
					if (context.cipher.encryptReady()) {
						RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
					} else {
						RecordCoder.encodePlaintextV2(record, data, buffer);
					}
					data.release();
					return buffer;
				}
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeV0((ChangeCipherSpec) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.APPLICATION_DATA) {
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeV0((ApplicationData) record, buffer);
				return buffer;
			} else if (record.contentType() == Record.HEARTBEAT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder.encode((HeartbeatMessage) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
				data.release();
				return buffer;
			} else if (record.contentType() == Record.ALERT) {
				final DataBuffer data = DataBuffer.instance();
				RecordCoder.encode((Alert) record, data);
				final DataBuffer buffer = DataBuffer.instance();
				if (context.cipher.encryptReady()) {
					RecordCoder.encodeCiphertext(context.cipher, record, data, buffer);
				} else {
					RecordCoder.encodePlaintextV2(record, data, buffer);
				}
				data.release();
				return buffer;
			} else {
				throw new UnsupportedOperationException("TLS:意外的记录类型" + record.contentType());
			}
		} else {
			// APPLICATION DATA
			final DataBuffer data = handler.encode(chain, message);
			final DataBuffer buffer = DataBuffer.instance();
			RecordCoder.encodeCiphertext(context.cipher, ApplicationData.INSTANCE, data, buffer);
			data.release();
			return buffer;
		}
	}

	private void encode(TLSContext context, Handshake handshake, DataBuffer buffer) throws Exception {
		if (handshake.msgType() == Handshake.FINISHED) {
			// 握手完成消息编码之前构造验证码
			final Finished finished = (Finished) handshake;
			finished.setVerifyData(context.secret.serverFinished());
		} else if (handshake.msgType() == Handshake.HELLO_REQUEST) {
			// 此消息不包含在握手验证摘要中
			HandshakeCoder.encodeV2(handshake, buffer);
			return;
		}
		HandshakeCoder.encodeV2(handshake, buffer);
		context.secret.hash(buffer);
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
		if (message == null) {
			handler.received(chain, message);
		} else if (message instanceof Record) {
			System.out.println(message);
			final Record record = (Record) message;
			if (record.contentType() == Record.APPLICATION_DATA) {
				// 忽略空的应用消息
			} else if (record.contentType() == Record.CHANGE_CIPHER_SPEC) {
				// 忽略兼容性消息
			} else if (record.contentType() == Record.HANDSHAKE) {
				message = handshake(context, (Handshake) record);
				if (message != null) {
					chain.send(message);
				}
			} else if (record.contentType() == Record.HEARTBEAT) {
				heartbeat(chain, (HeartbeatMessage) record);
			} else if (record.contentType() == Record.ALERT) {
				chain.close();
			} else {
				chain.close();
			}
		} else {
			handler.received(chain, message);
		}
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		final TLSContext context = chain.getContext(TLSContext.class);
		if (message instanceof Record) {
			System.out.println(message);
			if (message instanceof Alert) {
				chain.close();
				return;
			} else if (message instanceof Handshakes handshakes) {
				if (handshakes.last().msgType() == Handshake.FINISHED) {

				}
			}
		} else {
			handler.sent(chain, message);
		}
	}

	/**
	 * 握手，本实现所有握手消息(Handshake)继承自记录层(Record)；<br>
	 * 收到的握手消息即便在记录层已合并，在解码后也逐消息调用此方法；
	 */
	private Record handshake(TLSContext context, Handshake record) throws Exception {
		if (record.msgType() == Handshake.CLIENT_HELLO) {
			final ClientHello hello = (ClientHello) record;
			if (hello.getVersion() != V12) {
				return new Alert(Alert.PROTOCOL_VERSION);
			}
			// 压缩模式
			if (hello.hasCompressionMethods()) {
				if (hello.getCompressionMethods()[0] == 0) {
					// 不支持任何压缩
				} else {
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
			} else {
				return new Alert(Alert.ILLEGAL_PARAMETER);
			}
			// 加密套件
			final short suite;
			if (hello.hasCipherSuites()) {
				suite = CipherSuiter.match(hello.getCipherSuites(), parameters.cipherSuites());
				if (suite > 0) {
					// 设置加密套件
					context.cipher.initialize(suite);
				} else {
					// 未能匹配加密套件
					return new Alert(Alert.ILLEGAL_PARAMETER);
				}
			} else {
				// 未指定加密套件
				return new Alert(Alert.ILLEGAL_PARAMETER);
			}

			// 使用增强型主密钥
			final ExtendedMasterSecret ems = hello.getExtension(Extension.EXTENDED_MASTER_SECRET);
			context.extendedMasterSecret = ems != null;

			final SessionTicket st = hello.getExtension(Extension.SESSION_TICKET);

			context.secret.clientRandom(hello.getRandom());
			context.secret.initialize(context.cipher.type());

			final ServerHello server = new ServerHello();
			server.setCipherSuite(suite);
			server.makeRandom(V12);

			if (hello.hasSessionId()) {

			} else {
				server.setSessionId(hello.getSessionId());
			}
			return null;
		} else if (record.msgType() == Handshake.FINISHED) {
			final Finished finished = (Finished) record;
			if (finished.validate()) {
				return null;
			} else {
				return new Alert(Alert.DECRYPT_ERROR);
			}
		} else {
			return new Alert(Alert.UNEXPECTED_MESSAGE);
		}
	}

	private class TLSContext extends TLSSlaveContext {
		final Signaturer signaturer;
		final V2CipherSuiter cipher;
		final V2SecretCache secret;
		final V2KeyExchange key;

		boolean extendedMasterSecret;

		public TLSContext() {
			signaturer = new Signaturer();
			cipher = new V2CipherSuiter();
			secret = new V2SecretCache();
			key = new V2KeyExchange();
		}
	}
}