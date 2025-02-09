/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ByteBuffer 单元
 * 
 * @author ZhangXi
 * @date 2021年3月13日
 */
public final class DataBufferUnit {

	public final static int BYTES = 1024;
	private final static ConcurrentLinkedQueue<DataBufferUnit> BYTE_BUFFER_UNITS;
	static {
		BYTE_BUFFER_UNITS = new ConcurrentLinkedQueue<>();

		// -XX:MaxDirectMemorySize=512m

		int size = BYTES;
		while (size-- > 0) {
			BYTE_BUFFER_UNITS.offer(new DataBufferUnit());
		}
	}

	public final static DataBufferUnit get() {
		DataBufferUnit unit = BYTE_BUFFER_UNITS.poll();
		if (unit == null) {
			return new DataBufferUnit();
		}
		return unit;
	}

	public final static int freeCount() {
		return BYTE_BUFFER_UNITS.size();
	}

	////////////////////////////////////////////////////////////////////////////////

	private final ByteBuffer buffer;
	private DataBufferUnit next;
	/**
	 * 20250209<br>
	 * 由于javax.crypto.Cipher.update()方法在内部调用了ByteBuffer.mark()方法，
	 * 导致采用ByteBuffer.mark()方法记录并恢复读取位置的方案在特定情况下会失效；<br>
	 * 因此采用额外的mark字段实现数据单元的标记与恢复功能。
	 */
	private int mark = 0;

	private DataBufferUnit() {
		// 为了确保缓冲即可读亦可写必须确保 ByteBuffer的position < limit <= capacity
		// position表示缓存读位置，limit表示缓存写位置，capacity为容量
		buffer = ByteBuffer.allocateDirect(BYTES);
		// 新建ByteBufferUnit默认状态为java.nio.DirectByteBuffer[pos=0,lim=2048,cap=2048]
		buffer.limit(0);
	}

	/**
	 * 获取缓存容量
	 */
	public final int capacity() {
		return buffer.capacity();
	}

	/**
	 * 指示缓存已写满
	 */
	public final boolean isFull() {
		return buffer.limit() == buffer.capacity();
	}

	/**
	 * 指示缓存已读完
	 */
	public final boolean isEmpty() {
		return buffer.position() == buffer.limit();
	}

	/**
	 * 指示缓存无数据
	 */
	public final boolean isBlank() {
		return buffer.position() == 0 && buffer.limit() == 0;
	}

	/**
	 * 获取缓存可读字节数量
	 */
	public final int readable() {
		return buffer.limit() - buffer.position();
	}

	/**
	 * 获取缓存当前读取位置索引
	 */
	public final int readIndex() {
		return buffer.position();
	}

	/**
	 * 设置缓存当前读取位置索引
	 */
	public final void readIndex(int index) {
		buffer.position(index);
	}

	/**
	 * 从缓存当前位置读取值,读取位置自动推进
	 */
	public final byte readByte() {
		return buffer.get();
	}

	/**
	 * 从缓存指定位置获取值,读取位置不变
	 */
	public final byte get(int index) {
		return buffer.get(index);
	}

	/**
	 * 获取缓存可写字节数
	 */
	public final int writeable() {
		return buffer.capacity() - buffer.limit();
	}

	/**
	 * 获取缓存当前写入位置索引
	 */
	public final int writeIndex() {
		return buffer.limit();
	}

	/**
	 * 设置缓存当前写入位置索引
	 */
	public final void writeIndex(int index) {
		buffer.limit(index);
	}

	/**
	 * 写入值到缓存当前位置,写入位置自动推进
	 */
	public final void writeByte(byte value) {
		buffer.limit(buffer.limit() + 1);
		buffer.put(buffer.limit() - 1, value);
	}

	/**
	 * 设置缓存指定位置值,写入位置不变
	 */
	public final void set(int index, byte value) {
		buffer.put(index, value);
	}

	/**
	 * 获取ByteBuffer实例
	 */
	public final ByteBuffer buffer() {
		return buffer;
	}

	/**
	 * 记录当前读写位置
	 */
	public final void mark() {
		mark = buffer.position();
	}

	/**
	 * 恢复之前标记的读写位置
	 */
	public final void reset() {
		buffer.position(mark);
	}

	/**
	 * 获取用于接收数据的ByteBuffer实例
	 */
	public final ByteBuffer receive() {
		// 调整ByteBuffer用于Channel接收写入数据
		// [-p---m---] -> [----p---m]

		// 记录当前位置 mark=position
		mark = buffer.position();
		// 恢复写入位置 position=limit
		buffer.position(buffer.limit());
		// 恢复写入限制 limit=capacity
		buffer.limit(buffer.capacity());

		return buffer;
	}

	/**
	 * 数据接收完成恢复ByteBuffer状态，返回写入数据量
	 */
	public final int received() {
		// 调整ByteBuffer完成Channel接收写入数据
		// [----p---m] -> [-p---m---]

		// 20250209
		// 因ByteBuffer.mark()方法会被意外调用，因此不在使用此方法
		// 不能用flip()会将mark设置为-1
		// 设置limit时如果mark>limit则ByteBuffer内部重置mark=-1
		// mark=-1时执行ByteBuffer.reset()将抛出InvalidMarkException
		// Channel只要写入过数据ByteBuffer.position>0

		final int current = buffer.position();
		buffer.limit(buffer.position());
		buffer.position(mark);
		return current - buffer.position();
	}

	/**
	 * 释放当前缓存单元，当前缓存单元被回收，返回下一个缓存单元，如果没有下一个缓存单元则返回null
	 */
	public final DataBufferUnit apart() {
		final DataBufferUnit unit = next;
		next = null;
		release();
		return unit;
	}

	/**
	 * 断开当前缓存单元，返回下一个缓存单元，如果没有下一个缓存单元则返回null
	 */
	public final DataBufferUnit braek() {
		final DataBufferUnit unit = next;
		next = null;
		return unit;
	}

	/**
	 * 连接一个缓存单元，如果被连接缓存单元也连接有其它缓存单元则返回最后一个
	 */
	public final DataBufferUnit link(DataBufferUnit unit) {
		next = unit;
		while (unit.next() != null) {
			unit = unit.next();
		}
		return unit;
	}

	/**
	 * 下一个单元
	 * 
	 * @return null / ByteBufferUnit
	 */
	public final DataBufferUnit next() {
		return next;
	}

	public final void clear() {
		buffer.position(0);// 读位置
		buffer.limit(0);// 写位置
		mark = 0;
	}

	public final void release() {
		clear();
		// TODO DataBufferUnit 缓存太多之后释放部分

		BYTE_BUFFER_UNITS.offer(this);
	}

	@Override
	public final String toString() {
		return buffer.toString();
	}
}