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

	protected abstract void connected(ODBSClient chain) throws Exception;

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
				reader.clear();
				return null;
			}
			// 判断帧是否接收完
			if (reader.readable() >= length) {
				// TAG
				length = reader.readUnsignedByte();
				ODBSMessage message = null;
				if (length > 0) {
					// 获取实体实例
					message = ((ODBSClient) chain).take(length);
					if (message.tag() != length) {
						throw new IllegalStateException("Message TAG ERROR");
					}
				}
				message = (ODBSMessage) odbs.readEntity(message, reader);
				message.setChain(chain);
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
		received((ODBSClient) chain, (M) message);
		if (message != null) {
			chain.receive();
		}
	}

	public abstract void received(ODBSClient chain, M message) throws Exception;

	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final ODBSMessage o = (ODBSMessage) message;
		final DataBuffer writer = DataBuffer.instance();
		// 1Byte
		writer.write(HEAD);
		// 4Byte
		writer.writeInt(0);
		// TAG 1Byte
		writer.writeByte(o.tag());
		// Entity nByte
		odbs.writeEntity(o, writer);

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
		return writer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void sent(ChainChannel chain, Object message) throws Exception {
		sent((ODBSClient) chain, (M) message);
		if (message != null) {
			((ODBSClient) chain).sendNext();
		}
	}

	protected abstract void sent(ODBSClient chain, M message) throws Exception;

	public void beat(ChainChannel chain) throws Exception {
		beat((ODBSClient) chain);
	}

	protected abstract void beat(ODBSClient chain) throws Exception;

	@Override
	public void disconnected(ChainChannel chain) throws Exception {
		disconnected((ODBSClient) chain);
	}

	protected abstract void disconnected(ODBSClient chain) throws Exception;

	@Override
	public void error(ChainChannel chain, Throwable e) {
		chain.close();
		error((ODBSClient) chain, e);
	}

	protected abstract void error(ODBSClient chain, Throwable e);
}