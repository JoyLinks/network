/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.codec.Binary;
import com.joyzl.odbs.ODBS;
import com.joyzl.odbs.ODBSBinary;

/**
 * ODBS 帧和包客户端编解码
 * 
 * @author ZhangXi
 * @date 2020年12月13日
 */
public abstract class ODBSClientHandler<M extends ODBSMessage> extends ODBSFrame implements ChainHandler {

	private final ODBSBinary odbs;

	public ODBSClientHandler(ODBS o) {
		odbs = new ODBSBinary(o);
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		connected((ODBSClient) chain);
		chain.receive();
	}

	protected abstract void connected(ODBSClient client) throws Exception;

	@Override
	public Object decode(ChainChannel chain, DataBuffer reader) throws Exception {
		int length = reader.readable();
		if (length < MIN_FRAME) {
			// 不足最小帧字节数,继续接收
			return null;
		}
		if (length > MAX_FRAME) {
			// 超过最长帧字节数,丢弃数据
			reader.clear();
			throw new IllegalStateException("数据长度" + length + "超过最大值" + MAX_FRAME);
		}

		reader.mark();
		if (HEAD == reader.readByte()) {
			// 数据长度
			length = reader.readInt();
			if (length < 0) {
				// 错误的长度
				reader.clear();
				return null;
			}
			// 判断帧是否接收完
			if (reader.readable() >= length) {
				final ODBSClient client = (ODBSClient) chain;

				// TAG:FINISH|ID
				length = reader.readInt();
				final boolean finish = Binary.getBit(length, 31);
				length = Binary.setBit(length, false, 31);

				// 获取并解码消息
				// 消息可能须经历多次解码
				ODBSMessage message = null;
				if (length > 0) {
					if ((length & 1) == 1) {
						message = client.sends().get(length);
						if (message == null) {
							// 错误的消息标识
							reader.clear();
						} else {
							message = odbs.readEntity(message, reader);
							if (finish) {
								// client.sends().remove(length);
								client.sendRemove(length);
								return message;
							} else {
								return null;
							}
						}
					} else {
						message = client.pushes().get(length);
						if (message == null) {
							message = odbs.readEntity(message, reader);
							message.tag(length);
							if (finish) {
								return message;
							} else {
								client.pushes().add(message, length);
							}
						} else {
							message = odbs.readEntity(message, reader);
							if (finish) {
								client.pushes().remove(length);
								return message;
							} else {
								return null;
							}
						}
					}
				} else {
					message = odbs.readEntity(message, reader);
					return message;
				}
			} else {
				// 未接收完,需要继续接收剩余字节
				reader.reset();
			}
		} else {
			// 不是头标记，丢弃数据
			reader.clear();
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void received(ChainChannel chain, Object message) throws Exception {
		final ODBSClient client = (ODBSClient) chain;
		if (message == null) {
			// 超时：消息已无法响应
			client.pushes().clear();

			ODBSMessage om;
			client.sends().iterator();
			while (client.sends().hasNext()) {
				om = client.sends().next();
				om.setStatus(ODBSMessage.TIMEOUT);
				client.sends().remove();
				received(client, om);
			}
		} else {
			((ODBSMessage) message).chain(chain);
			received(client, (M) message);
		}
	}

	public abstract void received(ODBSClient client, M message) throws Exception;

	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final ODBSClient client = (ODBSClient) chain;
		final DataBuffer writer = DataBuffer.instance();
		final ODBSMessage om = (ODBSMessage) message;

		// 1Byte
		writer.write(HEAD);
		// LENGTH 4Byte 后补
		writer.writeInt(0);
		// TAG 4Byte 后补
		writer.writeInt(0);
		// Entity nByte
		odbs.writeEntity(om, writer);

		int length = writer.readable();
		if (length > MAX_LENGTH) {
			// 超过最长数据字节数,丢弃数据
			writer.release();
			throw new IllegalStateException("数据长度" + length + "超过最大值" + MAX_LENGTH);
		}

		// 长度不包括 HEAD 和 LENGTH 本身
		length -= MIN_FRAME;
		writer.set(1, (byte) (length >>> 24));
		writer.set(2, (byte) (length >>> 16));
		writer.set(3, (byte) (length >>> 8));
		writer.set(4, (byte) (length));

		// 消息标识
		length = client.streams().id();
		length = Binary.setBit(length, true, 31);
		writer.set(5, (byte) (length >>> 24));
		writer.set(6, (byte) (length >>> 16));
		writer.set(7, (byte) (length >>> 8));
		writer.set(8, (byte) (length));

		// 标记当前消息已完成
		client.streams().done();
		return writer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void sent(ChainChannel chain, Object message) throws Exception {
		final ODBSClient client = (ODBSClient) chain;
		if (message == null) {
			// 超时：消息已无法送出
			client.streams().clear();

			ODBSMessage om;
			client.sends().iterator();
			while (client.sends().hasNext()) {
				om = client.sends().next();
				om.setStatus(ODBSMessage.TIMEOUT);
				client.sends().remove();
				sent(client, om);
			}
		} else {
			// 消息是否发送完成
			// 消息可能须经历多次发送
			if (client.streams().isDone()) {
				sent(client, (M) message);
			}
			client.sendNext();
		}
	}

	protected abstract void sent(ODBSClient client, M message) throws Exception;

	public void beat(ChainChannel chain) throws Exception {
		beat((ODBSClient) chain);
	}

	protected abstract void beat(ODBSClient client) throws Exception;

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		disconnected((ODBSClient) chain);
	}

	protected abstract void disconnected(ODBSClient client) throws Exception;

	@Override
	public void error(ChainChannel chain, Throwable e) {
		error((ODBSClient) chain, e);
	}

	protected abstract void error(ODBSClient client, Throwable e);
}