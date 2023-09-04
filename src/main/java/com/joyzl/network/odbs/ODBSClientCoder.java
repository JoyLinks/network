/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 *
 */
package com.joyzl.network.odbs;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.odbs.ODBS;
import com.joyzl.odbs.ODBSBinary;

/**
 * ODBS 帧和包客户端编解码
 *
 * <pre>
 * 帧结构
 * +--------+----------+-------+-------------+
 * | HEAD 1 | LENGTH 2 | TAG 1 | DATA ODBS n |
 * +--------+----------+-------+-------------+
 * </pre>
 *
 * @author ZhangXi 2019年7月15日
 *
 */
public abstract class ODBSClientCoder<M extends ODBSMessage> extends ODBSFrame<M> {

	private final ODBSBinary odbs;

	public ODBSClientCoder(ODBS o) {
		odbs = new ODBSBinary(o);
	}

	@Override
	public final DataBuffer encode(ChainChannel<M> chain, M message) throws Exception {
		final DataBuffer writer = DataBuffer.instance();
		// 1Byte
		writer.write(HEAD);
		// 3Byte
		writer.writeMedium(0);
		// TAG 1Byte
		writer.writeByte(message.tag());
		// Entity nByte
		odbs.writeEntity(message, writer);

		int length = writer.readable();
		if (length > MAX_LENGTH) {
			// 超过最长数据字节数,丢弃数据
			writer.release();
			throw new IllegalStateException("数据长度" + length + "超过最大值" + MAX_LENGTH);
		}
		// 长度不包括 HEAD 和 LENGTH 本身
		length -= 4;
		writer.set(1, (byte) (length >>> 16));
		writer.set(2, (byte) (length >>> 8));
		writer.set(3, (byte) (length));
		return writer;
	}

	@Override
	@SuppressWarnings("unchecked")
	public final M decode(ChainChannel<M> chain, DataBuffer reader) throws Exception {
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
			length = reader.readUnsignedMedium();
			// 判断帧是否接收完
			if (reader.readable() >= length) {
				// 解包，设置读取限制
				reader.bounds(length);
				// TAG
				length = reader.readByte();
				M message = null;
				if (length >= 0) {
					// 获取实体实例
					message = take(chain, length);
					// if (message == null) {
					// System.out.println("TAG NULL " + length);
					// }
				}
				message = (M) odbs.readEntity(message, reader);
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

	public abstract M take(ChainChannel<M> chain, int tag);
}
