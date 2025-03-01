/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.odbs;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
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

	protected abstract void connected(ODBSSlave chain) throws Exception;

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
				reader.clear();
				return null;
			}
			// 判断帧是否接收完
			if (reader.readable() >= length) {
				// TAG
				length = reader.readUnsignedByte();
				// ENTITY
				final ODBSMessage message = (ODBSMessage) odbs.readEntity(null, reader);
				// System.out.println(length);
				message.tag(length);
				return message;
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
		received((ODBSSlave) chain, (M) message);
		if (message != null) {
			chain.receive();
		} else {
			chain.close();
		}
	}

	protected abstract void received(ODBSSlave chain, M message) throws Exception;

	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final ODBSMessage o = (ODBSMessage) message;
		final DataBuffer writer = DataBuffer.instance();
		// HEAD 1Byte
		writer.write(HEAD);
		// LENGTH 4Byte
		writer.writeInt(0);
		// TAG 1Byte
		writer.writeByte(o.tag());
		// DATA Entity
		odbs.writeEntity(o, writer);
		// 长度不包括 HEAD 和 LENGTH 本身
		final int length = writer.readable() - 5;
		writer.set(1, (byte) (length >>> 24));
		writer.set(2, (byte) (length >>> 16));
		writer.set(3, (byte) (length >>> 8));
		writer.set(4, (byte) (length));
		return writer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void sent(ChainChannel chain, Object message) throws Exception {
		sent((ODBSSlave) chain, (M) message);
		if (message != null) {
			((ODBSSlave) chain).sendNext();
		} else {
			chain.close();
		}
	}

	protected abstract void sent(ODBSSlave chain, M message) throws Exception;

	public void disconnected(ChainChannel chain) throws Exception {
		disconnected((ODBSSlave) chain);
	}

	protected abstract void disconnected(ODBSSlave chain) throws Exception;

	@Override
	public void error(ChainChannel chain, Throwable e) {
		chain.close();
		error((ODBSSlave) chain, e);
	}

	protected abstract void error(ODBSSlave chain, Throwable e);
}