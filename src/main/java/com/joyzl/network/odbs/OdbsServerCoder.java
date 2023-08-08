/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
/**
 *
 */
package com.joyzl.network.odbs;

import com.joyzl.cache.Pendable;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.codec.Binary;
import com.joyzl.odbs.ODBSBinary;

/**
 * ODBS 帧和包服务端编解码
 *
 * <p>
 * 服务端不支持Cacher.pend()对象自动取出,仅读取并设置pend标志
 * </p>
 *
 * @author simon(ZhangXi TEL:13883833982) 2019年7月15日
 *
 */
public abstract class OdbsServerCoder<M extends ODBSMessage> extends OdbsFrame<M> {

	private final ODBSBinary odbs;

	public OdbsServerCoder(ODBSBinary ob) {
		odbs = ob;
	}

	@Override
	public void beat(ChainChannel<M> chain) throws Exception {
		// SERVER 端默认不执行心跳检测
	}

	@Override
	public final M decode(ChainChannel<M> chain, DataBuffer reader) throws Exception {
		// 检查帧完整性,并剥离帧标记(仅剩数据部分)
		// +--------+----------+-------+--------+--------+
		// | HEAD 1 | LENGTH 3 | TAG 1 | DATA n | FOOT 1 |
		// +--------+----------+-------+--------+--------+

		int length = reader.readable();

		if (length < MIN_FRAME) {
			// 不足最小帧字节数,继续接收
			return null;
		}
		if (length > MAX_FRAME) {
			// 超过最长帧字节数,丢弃数据
			reader.clear();
			error(chain, new IllegalStateException("数据被丢弃，数据长度" + length + "超过最大值" + MAX_FRAME));
			return null;
		}
		while (reader.readable() > 0) {
			reader.mark();
			if (HEAD == reader.readByte()) {
				// 数据长度
				length = reader.readUnsignedMedium();
				// 判断数据长度是否超出范围
				if (length <= MAX_LENGTH) {
					// 判断帧是否接收完
					if (reader.readable() > length) {
						// 判断帧尾是否为预期位置
						if (FOOT == reader.readByte(length)) {
							try {
								// 解包，设置读取限制
								reader.bounds(length);
								final M action = decode(reader);
								length = reader.discard();
								if (length == 0 && FOOT == reader.readByte()) {
									action.setChain(chain);
									return action;
								} else {
									// 数据没读完,解包错误
									error(chain, new IllegalStateException("数据帧解析未到达末尾，剩余" + length));
								}
							} catch (Exception e) {
								error(chain, e);
							}
							// 继续尝试查找帧,如果还有数据可读则有可能是粘包
						} else {
							// 尾标记不正确,继续检查
						}
					} else {
						// 未接收完,需要继续接收剩余字节
						reader.reset();
						break;
					}
				} else {
					// 长度不正确,继续检查
				}
			} else {
				// 不是头标记，继续检查
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public final M decode(DataBuffer reader) {
		byte tags = reader.readByte();
		int pend = 0;
		if (Binary.getBit(tags, 7)) {
			// 实体具有额外的pending状态
			pend = reader.readVarint();
		}

		final M entity = (M) odbs.readEntity(null, reader);

		if (Binary.getBit(tags, 7)) {
			((Pendable) entity).pend(pend);
		}
		return entity;
	}

	@Override
	public final DataBuffer encode(ChainChannel<M> chain, M message) throws Exception {
		// +--------+----------+-------+--------+--------+
		// | HEAD 1 | LENGTH 3 | TAG 1 | DATA n | FOOT 1 |
		// +--------+----------+-------+--------+--------+

		final DataBuffer writer = DataBuffer.getB65536();
		// HEAD 1
		writer.write(HEAD);
		// LENGTH 3
		writer.writeMedium(0);
		// TAG+ID 1+n
		if (message.getChain() == chain) {
			// 消息标识仅对发起链路有效
			byte tags = 0;
			int pending = 0;

			if (message instanceof Pendable) {
				tags = Binary.setBit(tags, true, 7);
				pending = ((Pendable) message).pend();
			}

			// 输出状态标志
			writer.writeByte(tags);
			if (Binary.getBit(tags, 7)) {
				writer.writeVarint(pending);
			}
		} else {
			// 输出状态标志(默认)
			writer.write(0);
		}

		// DATA Entity
		odbs.writeEntity(message, writer);

		int length = writer.readable();
		if (length > MAX_LENGTH) {
			// 超过最长数据字节数,丢弃数据
			writer.release();
			throw new IllegalStateException("数据被丢弃，数据长度" + length + "超过最大值" + MAX_LENGTH);
		}

		length -= 4;// 长度不包括 HEAD 和 LENGTH 本身
		writer.writeByte(1, (byte) (length >>> 16));
		writer.writeByte(2, (byte) (length >>> 8));
		writer.writeByte(3, (byte) (length));

		// FOOT 1
		writer.write(FOOT);
		return writer;
	}
}
