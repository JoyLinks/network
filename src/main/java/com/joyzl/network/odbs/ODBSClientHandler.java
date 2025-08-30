/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.odbs;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainGenericsHandler;
import com.joyzl.network.codec.Binary;
import com.joyzl.odbs.ODBS;
import com.joyzl.odbs.ODBSBinary;

/**
 * ODBS 帧和包客户端编解码
 * 
 * @author ZhangXi
 * @date 2020年12月13日
 */
public abstract class ODBSClientHandler<M extends ODBSMessage> extends ODBSFrame
		implements ChainGenericsHandler<ODBSClient, M> {

	/** 状态：成功 */
	public final static int SUCCESS = 0;
	/** 状态：网络 */
	public final static int NETWORK = 1;
	/** 状态：超时 */
	public final static int TIMEOUT = 2;

	private final ODBSBinary odbs;

	public ODBSClientHandler(ODBS o) {
		odbs = new ODBSBinary(o);
	}

	@Override
	public void connected(ODBSClient client) throws Exception {
		client.receive();
	}

	@Override
	public Object decode(ODBSClient client, DataBuffer reader) throws Exception {
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
			if (length <= 0) {
				// 错误的长度
				reader.clear();
				return null;
			}
			// 判断帧是否接收完
			if (reader.readable() >= length) {
				// TAG:FINISH|ID
				length = reader.readInt();
				final boolean finish = Binary.getBit(length, 31);
				length = Binary.setBit(length, false, 31);

				// 获取并解码消息
				// 消息可能须经历多次解码
				ODBSMessage message = null;
				if (length > 0) {
					message = client.receiveGet(length);
					if (message == null) {
						message = odbs.readEntity(message, reader);
						message.tag(length);
						if (finish) {
							return message;
						} else {
							client.receivePut(length, message);
						}
					} else {
						message = odbs.readEntity(message, reader);
						if (finish) {
							// client.receives().remove(length);
							client.receiveRemove(length);
							return message;
						} else {
							return null;
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
	public void received(ODBSClient client, M message) throws Exception {
		if (message == null) {
			fail(client, TIMEOUT);
		} else {
			message.chain(client);
			execute(client, (M) message, SUCCESS);
		}
	}

	protected abstract void execute(ODBSClient client, M message, int state) throws Exception;

	@Override
	public DataBuffer encode(ODBSClient client, M message) throws Exception {
		final DataBuffer writer = DataBuffer.instance();

		// 1Byte
		writer.write(HEAD);
		// LENGTH 4Byte 后补
		writer.writeInt(0);
		// TAG 4Byte 后补
		writer.writeInt(0);
		// Entity nByte
		odbs.writeEntity(message, writer);

		int length = writer.readable();
		if (length > MAX_LENGTH) {
			// 超过最长数据字节数,丢弃数据
			writer.release();
			throw new IllegalStateException("数据长度" + length + "超过最大值" + MAX_LENGTH);
		}

		// 长度不包括 HEAD 和 LENGTH 本身
		length -= 5;
		writer.set(1, (byte) (length >>> 24));
		writer.set(2, (byte) (length >>> 16));
		writer.set(3, (byte) (length >>> 8));
		writer.set(4, (byte) (length));

		// 消息标识
		length = client.sendId();
		length = Binary.setBit(length, true, 31);
		writer.set(5, (byte) (length >>> 24));
		writer.set(6, (byte) (length >>> 16));
		writer.set(7, (byte) (length >>> 8));
		writer.set(8, (byte) (length));

		// 标记当前消息已完成
		client.sendDone();
		return writer;
	}

	@Override
	public void sent(ODBSClient client, M message) throws Exception {
		if (message == null) {
			client.sendClear();
		} else {
			client.sendNext();
		}
	}

	@Override
	public void disconnected(ODBSClient client) throws Exception {
		client.sendClear();
		fail(client, NETWORK);
	}

	@SuppressWarnings("unchecked")
	protected void fail(ODBSClient client, int state) throws Exception {
		ODBSMessage om;
		client.receives().iterator();
		while (client.receives().hasNext()) {
			om = client.receives().next().value();
			client.receives().remove();
			execute(client, (M) om, state);
		}
	}
}