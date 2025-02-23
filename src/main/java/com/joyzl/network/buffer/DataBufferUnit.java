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
 * <p>
 * 注意：此类实现的所有方法均不执行索引位置越界检查，如果索引越界由内部ByteBuffer抛出异常
 * </p>
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
		// TODO DataBufferUnit 缓存太多之后释放部分

		int size = BYTES;
		while (size-- > 0) {
			BYTE_BUFFER_UNITS.offer(new DataBufferUnit());
		}
	}

	public final static DataBufferUnit get() {
		DataBufferUnit unit = BYTE_BUFFER_UNITS.poll();
		if (unit == null) {
			return new DataBufferUnit();
		} else {
			// 取消特殊值
			unit.mark = 0;
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
	 * 获取缓存读取位置索引
	 */
	public final int readIndex() {
		return buffer.position();
	}

	/**
	 * 设置缓存读取位置索引
	 */
	public final void readIndex(int index) {
		buffer.position(index);
	}

	/**
	 * 从缓存读取位置读取值，读取位置自动推进
	 */
	public final byte readByte() {
		return buffer.get();
	}

	/**
	 * 从头部跳过（丢弃）指定数量数据，返回实际跳过（丢弃）的数量
	 */
	public int readSkip(int length) {
		if (buffer.position() + length > buffer.limit()) {
			length = buffer.limit() - buffer.position();
			buffer.position(buffer.limit());
		} else {
			buffer.position(buffer.position() + length);
		}
		return length;
	}

	/**
	 * 从缓存指定位置获取值，读取和写入位置不变
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
	 * 获取缓存写入位置索引
	 */
	public final int writeIndex() {
		return buffer.limit();
	}

	/**
	 * 设置缓存写入位置索引
	 */
	public final void writeIndex(int index) {
		buffer.limit(index);
	}

	/**
	 * 写入值到缓存写入位置,写入位置自动推进
	 */
	public final void writeByte(byte value) {
		buffer.limit(buffer.limit() + 1);
		buffer.put(buffer.limit() - 1, value);
	}

	/**
	 * 从缓存写入位置取出值，写入位置自动缩短
	 */
	public final byte backByte() {
		final byte value = buffer.get(buffer.limit() - 1);
		buffer.limit(buffer.limit() - 1);
		return value;
	}

	/**
	 * 从尾部回退（丢弃）指定数量数据，返回实际回退（丢弃）的数量
	 */
	public int backSkip(int length) {
		if (buffer.limit() - length < buffer.position()) {
			length = buffer.limit() - buffer.position();
			buffer.limit(buffer.position());
		} else {
			buffer.limit(buffer.limit() - length);
		}
		return length;
	}

	/**
	 * 设置缓存指定位置值，读取和写入位置不变
	 */
	public final void set(int index, byte value) {
		buffer.put(index, value);
	}

	/**
	 * 记录当前读写位置
	 */
	public final void mark() {
		// 缓存单元的空间大小超过特定值时0x8000(32768)
		// 将导致mark值有概率和单元回收标记0x80000000重叠
		// 当前缓存单元空间固定为1024，此情况永远不会出现
		mark = (buffer.limit() << 16) | buffer.position();
	}

	/**
	 * 恢复之前标记的读写位置
	 */
	public final void reset() {
		buffer.limit(mark >>> 16);
		buffer.position(mark & 0xFFFF);
	}

	/**
	 * 当前是否已标记
	 */
	public boolean marked() {
		return mark != 0;
	}

	/**
	 * 获取ByteBuffer实例
	 */
	public final ByteBuffer buffer() {
		return buffer;
	}

	/**
	 * 获取用于发送（读取）数据的ByteBuffer实例，之前的mark将失效
	 */
	public final ByteBuffer send() {
		mark();
		return buffer;
	}

	/**
	 * 获取用于发送（读取）数据的ByteBuffer实例，之前的mark将失效；<br>
	 * 允许读取数量如果超过已有数据量则没有任何作用
	 */
	public final ByteBuffer send(int length) {
		mark();
		// 设置读取限制
		if (length < buffer.remaining()) {
			length = buffer.remaining() - length;
			buffer.limit(buffer.limit() - length);
		}
		return buffer;
	}

	/**
	 * 数据发送完成，返回读取（减少）数据量
	 */
	public final int sent() {
		buffer.limit(mark >>> 16);
		return (mark >>> 16) - (mark & 0xFFFF) - buffer.remaining();
	}

	/**
	 * 获取用于接收（写入）数据的ByteBuffer实例，之前的mark将失效
	 */
	public final ByteBuffer receive() {
		// 调整ByteBuffer用于Channel接收写入数据
		// [-p---m---] -> [----p---m]

		mark();
		// 恢复写入位置 position=limit
		buffer.position(buffer.limit());
		// 恢复写入限制 limit=capacity
		buffer.limit(buffer.capacity());
		return buffer;
	}

	/**
	 * 获取用于接收（写入）数据的ByteBuffer实例，之前的mark将失效；<br>
	 * 允许写入数量如果超过可写空间则没有任何作用
	 */
	public final ByteBuffer receive(int length) {
		// 调整ByteBuffer用于Channel接收写入数据
		// [-p---m---] -> [----p---m]

		mark();
		// 恢复写入位置 position=limit
		buffer.position(buffer.limit());
		// 恢复写入限制 limit=capacity
		buffer.limit(buffer.capacity());
		// 设置写入限制
		if (length < buffer.remaining()) {
			length = buffer.remaining() - length;
			buffer.limit(buffer.limit() - length);
		}
		return buffer;
	}

	/**
	 * 数据接收完成恢复ByteBuffer状态，返回写入（增加）数据量
	 */
	public final int received() {
		// 调整ByteBuffer完成Channel接收写入数据
		// receive() .[-p---l---] -> [-m---p---l]
		// received() ...............[-m----p--l] -> [-p----l--]

		// 20250209
		// 因ByteBuffer.mark()方法会被意外调用，因此不在使用此方法
		// 不能用flip()会将mark设置为-1
		// 设置limit时如果mark>limit则ByteBuffer内部重置mark=-1
		// mark=-1时执行ByteBuffer.reset()将抛出InvalidMarkException
		// Channel只要写入过数据ByteBuffer.position>0

		buffer.limit(buffer.position());
		buffer.position(mark & 0xFFFF);
		return buffer.remaining() - (mark >>> 16) + (mark & 0xFFFF);
	}

	/**
	 * 缩减缓存单元，断开回收当前缓存单元，返回连接的缓存单元，如果没有连接的缓存单元则返回null
	 */
	public final DataBufferUnit curtail() {
		final DataBufferUnit unit = next;
		next = null;
		release();
		return unit;
	}

	/**
	 * 扩展缓存单元，连接新的缓存单元并返回
	 */
	public final DataBufferUnit extend() {
		if (next == null) {
			next = get();
		} else {
			throw new IllegalStateException("DataBufferUnit:已有连接单元");
		}
		return next;
	}

	/**
	 * 断开当前缓存单元，返回连接的缓存单元，如果没有连接的缓存单元则返回null
	 */
	public final DataBufferUnit braek() {
		final DataBufferUnit unit = next;
		next = null;
		return unit;
	}

	/**
	 * 连接缓存单元，如果被连接缓存单元也连接有其它缓存单元则返回最后一个
	 */
	public final DataBufferUnit link(DataBufferUnit unit) {
		if (next == null) {
			next = unit;
			while (unit.next() != null) {
				unit = unit.next();
			}
			return unit;
		} else {
			throw new IllegalStateException("DataBufferUnit:已有连接单元");
		}
	}

	/**
	 * 连接缓存单元，不检查后续单元
	 */
	public final void next(DataBufferUnit unit) {
		if (next == null) {
			next = unit;
		} else {
			throw new IllegalStateException("DataBufferUnit:已有连接单元");
		}
	}

	/**
	 * 下一个单元
	 * 
	 * @return null / ByteBufferUnit
	 */
	public final DataBufferUnit next() {
		return next;
	}

	/**
	 * 清空，读写位置归零，标记位置归零
	 */
	public final void clear() {
		// 重置读位置
		buffer.position(0);
		// 重置写位置
		buffer.limit(0);
		// 重置标记位置
		mark = 0;
	}

	/**
	 * 释放并回收缓存单元，包括连接的缓存单元
	 */
	public final void release() {
		// 特殊值标记是否已释放
		if (mark != Integer.MIN_VALUE) {
			mark = Integer.MIN_VALUE;
			buffer.position(0);
			buffer.limit(0);

			if (next != null) {
				next.release();
				next = null;
			}

			BYTE_BUFFER_UNITS.offer(this);
		} else {
			throw new IllegalStateException("重复释放");
		}
	}

	@Override
	public final String toString() {
		return buffer.toString();
	}
}