/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.buffer;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ByteBuffer封装
 * 
 * @author ZhangXi
 * @date 2021年3月13日
 */
public final class DataBufferUnit {

	public final static int UNIT_SIZE = 1024;
	private final static ConcurrentLinkedQueue<DataBufferUnit> BYTE_BUFFER_UNITS = new ConcurrentLinkedQueue<>();

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

	private DataBufferUnit() {
		next = null;

		// 为了确保缓冲即可读亦可写必须确保 ByteBuffer的position < limit <= capacity
		// position表示缓存读位置,limit表示缓存写位置，capacity为容量
		buffer = ByteBuffer.allocateDirect(UNIT_SIZE);
		// 新建ByteBufferUnit默认状态为java.nio.DirectByteBuffer[pos=0,lim=2048,cap=2048]
		buffer.limit(0);
	}

	/**
	 * 获取缓存大小
	 */
	final int size() {
		return buffer.capacity();
	}

	/**
	 * 指示缓存已写满
	 */
	final boolean isFull() {
		return buffer.limit() == buffer.capacity();
	}

	/**
	 * 指示缓存已读完
	 */
	final boolean isEmpty() {
		return buffer.position() == buffer.limit();
	}

	/**
	 * 指示缓存无数据
	 */
	final boolean isBlank() {
		return buffer.position() == 0 && buffer.limit() == 0;
	}

	/**
	 * 获取缓存可读字节数量
	 */
	final int readable() {
		return buffer.limit() - buffer.position();
	}

	/**
	 * 获取缓存当前读取位置索引
	 */
	final int readIndex() {
		return buffer.position();
	}

	/**
	 * 设置缓存当前读取位置索引
	 */
	final void readIndex(int index) {
		buffer.position(index);
	}

	/**
	 * 从缓存当前位置读取值,读取位置自动推进
	 */
	final byte readByte() {
		return buffer.get();
	}

	/**
	 * 从缓存指定位置读取值,读取位置不变
	 */
	final byte readByte(int index) {
		return buffer.get(index);
	}

	final int writeable() {
		return buffer.capacity() - buffer.limit();
	}

	final int writeIndex() {
		return buffer.limit();
	}

	final void writeIndex(int index) {
		buffer.limit(index);
	}

	final void writeByte(byte value) {
		buffer.limit(buffer.limit() + 1);
		buffer.put(buffer.limit() - 1, value);
	}

	final void writeByte(int index, byte value) {
		buffer.put(index, value);
	}

	final ByteBuffer buffer() {
		return buffer;
	}

	/**
	 * 释放当前缓存单元，当前缓存单元被回收，返回下一个缓存单元，如果没有下一个缓存单元则返回null
	 */
	final DataBufferUnit apart() {
		final DataBufferUnit unit = next;
		next = null;
		release();
		return unit;
	}

	/**
	 * 断开当前缓存单元，返回下一个缓存单元，如果没有下一个缓存单元则返回null
	 */
	final DataBufferUnit braek() {
		final DataBufferUnit unit = next;
		next = null;
		return unit;
	}

	/**
	 * 连接一个缓存单元，如果被连接缓存单元也连接有其它缓存单元则返回最后一个
	 */
	final DataBufferUnit link(DataBufferUnit unit) {
		next = unit;
		while (unit.next() != null) {
			unit = unit.next();
		}
		return unit;
	}

	/**
	 * 下一个节点
	 * 
	 * @return null / ByteBufferUnit
	 */
	final DataBufferUnit next() {
		return next;
	}

	final void clear() {
		buffer.position(0);// 读位置
		buffer.limit(0);// 写位置
	}

	final void release() {
		clear();
		BYTE_BUFFER_UNITS.offer(this);
	}

	@Override
	public final String toString() {
		return buffer.toString();
	}
}