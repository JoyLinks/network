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
 * ODBS 帧和包服务端编解码
 * 
 * @author ZhangXi
 * @date 2020年12月13日
 */
public abstract class ODBSServerHandler<M extends ODBSMessage> extends ODBSFrame implements ChainHandler {

	private final ODBSBinary odbs;

	public ODBSServerHandler(ODBS o) {
		odbs = new ODBSBinary(o);
	}

	@Override
	public void connected(ChainChannel chain) throws Exception {
		connected((ODBSSlave) chain);
		chain.receive();
	}

	protected abstract void connected(ODBSSlave slave) throws Exception;

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
			return null;
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
				final ODBSSlave slave = (ODBSSlave) chain;

				// TAG:FINISH|ID
				length = reader.readInt();
				final boolean finish = Binary.getBit(length, 31);
				length = Binary.setBit(length, false, 31);

				// 获取并解码消息
				// 消息可能须经历多次解码
				ODBSMessage message = null;
				if (length > 0) {
					if ((length & 1) == 1) {
						message = slave.receives().get(length);
						if (message == null) {
							message = odbs.readEntity(message, reader);
							message.tag(length);
							if (finish) {
								return message;
							} else {
								slave.receives().add(message, length);
							}
						} else {
							message = odbs.readEntity(message, reader);
							if (finish) {
								slave.receives().remove(length);
								return message;
							} else {
								return null;
							}
						}
					} else {
						// 客户端仅使用奇数标识
						reader.clear();
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
		final ODBSSlave slave = (ODBSSlave) chain;
		if (message == null) {
			slave.receives().clear();
		} else {
			((ODBSMessage) message).chain(chain);
			received(slave, (M) message);
		}
	}

	protected abstract void received(ODBSSlave slave, M message) throws Exception;

	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final ODBSSlave slave = (ODBSSlave) chain;
		final DataBuffer writer = DataBuffer.instance();
		final ODBSMessage om = (ODBSMessage) message;

		// HEAD 1Byte
		writer.write(HEAD);
		// LENGTH 4Byte 后补
		writer.writeInt(0);
		// TAG 4Byte 后补
		writer.writeInt(0);
		// DATA Entity
		odbs.writeEntity(om, writer);

		// 长度不包括 HEAD 和 LENGTH 本身
		int length = writer.readable() - MIN_FRAME;
		writer.set(1, (byte) (length >>> 24));
		writer.set(2, (byte) (length >>> 16));
		writer.set(3, (byte) (length >>> 8));
		writer.set(4, (byte) (length));

		// 消息标识
		length = slave.streams().id();
		length = Binary.setBit(length, true, 31);
		writer.set(5, (byte) (length >>> 24));
		writer.set(6, (byte) (length >>> 16));
		writer.set(7, (byte) (length >>> 8));
		writer.set(8, (byte) (length));

		// 标记当前消息已完成
		slave.streams().done();
		return writer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void sent(ChainChannel chain, Object message) throws Exception {
		final ODBSSlave slave = (ODBSSlave) chain;
		if (message == null) {
			slave.streams().clear();
			slave.pushes().clear();
		} else {
			// 消息是否发送完成
			// 消息可能须经历多次发送
			if (slave.streams().isDone()) {
				sent(slave, (M) message);
			}
			slave.sendNext();
		}
	}

	protected abstract void sent(ODBSSlave slave, M message) throws Exception;

	public void disconnected(ChainChannel chain) throws Exception {
		disconnected((ODBSSlave) chain);
	}

	protected abstract void disconnected(ODBSSlave slave) throws Exception;

	@Override
	public void error(ChainChannel chain, Throwable e) {
		error((ODBSSlave) chain, e);
	}

	protected abstract void error(ODBSSlave slave, Throwable e);
}