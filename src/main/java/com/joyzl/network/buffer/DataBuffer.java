/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.joyzl.codec.BigEndianDataInput;
import com.joyzl.codec.BigEndianDataOutput;
import com.joyzl.codec.DataInput;
import com.joyzl.codec.DataOutput;
import com.joyzl.network.verifies.EmptyVerifier;
import com.joyzl.network.verifies.Verifiable;
import com.joyzl.network.verifies.Verifier;

/**
 * 数据缓存对象，数据将写入多个ByteBuffer中，或者从多个ByteBuffer中读取数据，根据需要会自动扩展和回收ByteBuffer。
 * 实例不是多线程安全的，不能在多个线程同时访问实例。
 * 
 * @author ZhangXi
 * @date 2021年3月13日
 */
public final class DataBuffer implements Verifiable, DataInput, DataOutput, BigEndianDataInput, BigEndianDataOutput {

	// 对象实例缓存
	private final static ConcurrentLinkedQueue<DataBuffer> BYTE_BUFFERS = new ConcurrentLinkedQueue<>();

	public final static DataBuffer instance() {
		DataBuffer buffer = BYTE_BUFFERS.poll();
		if (buffer == null) {
			buffer = new DataBuffer();
		}
		return buffer;
	}

	/**
	 * 获取可用对象数量
	 * 
	 * @return 0~n
	 */
	public final static int freeCount() {
		return BYTE_BUFFERS.size();
	}

	////////////////////////////////////////////////////////////////////////////////

	private DataBufferUnit read;
	private DataBufferUnit mark;
	private DataBufferUnit write;

	private int length;
	private int bounds;
	private int units;

	// 必须至少保留一个ByteBufferUnit
	// 对于获取DataBuffer的程序而言，至少需要一个ByteBufferUnit
	// 默认保留一个可减少获取频率，减少不必要的null判断
	// 不能保证每个缓存单元都是写满的

	private DataBuffer() {
		read = write = DataBufferUnit.get();
		bounds = -1;
		length = 0;
		units = 1;
		mark = null;
	}

	/**
	 * 获取缓存持有的单元数量
	 */
	public final int units() {
		return units;
	}

	/**
	 * 获取缓存总容量
	 */
	public final int capacity() {
		return units * DataBufferUnit.UNIT_SIZE;
	}

	/**
	 * 获取当前剩余可写入字节数量，写入字节超过此数量将自动扩展
	 */
	public final int writeable() {
		return write.writeable();
	}

	/**
	 * 可读字节数量
	 */
	public final int readable() {
		return bounds < 0 ? length : bounds;
	}

	/**
	 * 写入字节到指定位置，写入位置不变，不参与校验
	 *
	 * @param index 0~{@link #readable()}-1
	 * @param value byte
	 * @see {{@link #set(int, byte)}
	 */
	@Deprecated
	public final void writeByte(int index, byte value) {
		set(index, value);
	}

	/**
	 * 设置指定位置字节，读写位置不变，不参与校验
	 *
	 * @param index 0~{@link #readable()}-1
	 * @param value byte
	 */
	public final void set(int index, byte value) {
		if (index >= 0 && index < length) {
			DataBufferUnit item = read;
			while (index >= item.readable()) {
				index -= item.readable();
				item = item.next();
			}
			item.writeByte(item.readIndex() + index, value);
		} else {
			throw new IndexOutOfBoundsException(index);
		}
	}

	/**
	 * 写入输入流中的所有字节，参与校验
	 * 
	 * @param input InputStream
	 * @throws IOException
	 */
	public final void write(InputStream input) throws IOException {
		int value;
		while (input.available() > 0) {
			while ((value = input.read()) >= 0) {
				write(value);
			}
		}
	}

	/**
	 * 写入缓存对象中所有字节，参与校验
	 * 
	 * @param buffer DataBufferLink
	 * @throws IOException
	 */
	public final void write(DataBuffer buffer) throws IOException {
		while (buffer.readable() > 0) {
			write(buffer.readUnsignedByte());
		}
	}

	/**
	 * 读取指定位置字节，读取位置不变，不参与校验
	 *
	 * @param index 0~{@link #readable()}-1
	 * @see {{@link #get(int)}
	 */
	@Deprecated
	public final byte readByte(int index) {
		return get(index);
	}

	/**
	 * 获取指定位置字节，读写位置不变，不参与校验
	 *
	 * @param index 0~{@link #readable()}-1
	 */
	public final byte get(int index) {
		if (index >= 0 && index < length) {
			DataBufferUnit item = read;
			while (index >= item.readable()) {
				index -= item.readable();
				item = item.next();
			}
			return item.readByte(item.readIndex() + index);
		} else {
			throw new IndexOutOfBoundsException(index);
		}
	}

	/**
	 * 读取所有字节到输出流，参与校验
	 * 
	 * @param output
	 * @throws IOException
	 */
	public final void read(OutputStream output) throws IOException {
		while (readable() > 0) {
			output.write(readUnsignedByte());
		}
	}

	/**
	 * 读取所有字节到缓存，参与校验
	 * 
	 * @param buffer DataBufferLink
	 * @throws IOException
	 */
	public final void read(DataBuffer buffer) throws IOException {
		while (readable() > 0) {
			buffer.write(readUnsignedByte());
		}
	}

	/**
	 * 标记读取位置，可通过{@link #reset()}恢复标记的位置
	 */
	public final void mark() {
		mark = read;
		while (mark != null) {
			mark.buffer().mark();
			mark = mark.next();
		}
		mark = read;
	}

	/**
	 * 恢复读取位置，可通过{@link #mark()}标记读取位置
	 */
	public final void reset() {
		if (mark == null) {
			throw new IllegalStateException("恢复读取位置之前未标记读取位置");
		}

		length = 0;
		read = mark;
		while (mark != null) {
			mark.buffer().reset();
			length += mark.readable();
			mark = mark.next();
		}
	}

	/**
	 * 设置读取限制，当读取超出范围时抛出异常
	 *
	 * @param size 1~readable()
	 */
	public final void bounds(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("读取限制参数错误" + size);
		}
		if (size > length) {
			throw new IllegalArgumentException("读取限制参数错误,超出可读范围" + size);
		}
		bounds = size;
	}

	/**
	 * 丢弃读取限制的数据并返回丢弃数量
	 *
	 * @return 0~n
	 */
	public final int discard() {
		if (bounds <= 0) {
			// 0 表示限制读完
			// -1 表示未设置限制
			bounds = -1;
			return 0;
		}

		int size = 0;
		while (bounds >= read.readable()) {
			bounds -= read.readable();
			size += read.readable();
			read = read.apart();
			units--;
		}
		read.readIndex(read.readIndex() + bounds);
		length -= size;
		bounds = -1;
		return size;
	}

	/**
	 * 获取范围之外的数据量,如果未设置读取范围,则返回0
	 */
	public final int residue() {
		return bounds > 0 ? length - bounds : 0;
	}

	/**
	 * 将当前缓存对象中读取限制之外的剩余数据转移到目标缓存对象中，边界内数据被保留，边界外数据被转移，不参与校验
	 */
	public final void residue(DataBuffer target) {
		// 源对象未标记边界则不会转移任何数据
		if (bounds > 0) {
			if (bounds < length) {
				int u = 1;
				int size = bounds;
				// 1.跳过被整个包含在读取限制中的单元
				DataBufferUnit unit = read;
				while (size > unit.readable()) {
					size -= unit.readable();
					unit = unit.next();
					u++;
				}

				// size此时表示当前单元可读部分署于读取限制的数量
				int index = unit.readIndex() + size;
				// 处理正好位于限制界限的单元
				while (index < unit.writeIndex()) {
					target.writeByte(unit.readByte(index));
					index++;
				}
				unit.writeIndex(size);
				write = unit;
				// 已转移的数量
				size = index - unit.readIndex() - size;

				// 注意：之后的单元将整体转移，当前单元可能并未全部写完

				// 整体转移后续单元
				unit = unit.braek();
				if (unit != null) {
					// 后续单元无须复制，进行整体转移
					target.write = target.write.link(unit);
					target.length += length - bounds;
					target.units += units - u;
					units = u;
				}
				length = bounds;
				bounds = -1;
			} else {
				// 设置了边界但是没有剩余数据需要转移
			}
		} else if (bounds == 0) {
			// 无数据需要转移
		} else {
			// 未设置边界
			// 转移残余数据，需要设置读取限制，否则此操作无意义
			throw new IllegalStateException("未设置读取限制");
		}
	}

	/**
	 * 将源缓存对象中的数据全部复制到当前缓存对象中，源对象数据被保留，不参与校验
	 * <p>
	 * 此方法是多线程安全的，可支持多个线程对一个数据源并发执行复制操作
	 */
	public final void replicate(DataBuffer source) {
		if (source.readable() > 0) {
			int index;
			DataBufferUnit unit = source.read;
			while (unit != null) {
				for (index = unit.readIndex(); index < unit.writeIndex(); index++) {
					writeByte(unit.readByte(index));
				}
				unit = unit.next();
			}
		}
	}

	/**
	 * 将源缓存对象中的数据全部复制到当前缓存对象中，源对象数据被保留，不参与校验
	 * <p>
	 * 此方法是多线程安全的，可支持多个线程对一个数据源并发执行复制操作
	 * 
	 * @param source 要复制的数据源
	 * @param start 0 ~ source.readable() - 1
	 * @param end start ~ source.readable() - 1
	 */
	public final void replicate(DataBuffer source, int start, int end) {
		if (start < 0 || start >= source.readable()) {
			throw new IndexOutOfBoundsException(start);
		}
		if (end < 0 || end >= source.readable()) {
			throw new IndexOutOfBoundsException(start);
		}
		if (end < start) {
			throw new IllegalArgumentException();
		}
		if (end == start || source.readable() == 0) {
			return;
		}

		// end 计算成长度
		end = end - start;

		DataBufferUnit temp = source.read;
		while (temp != null) {
			if (start >= temp.readable()) {
				start -= temp.readable();
				temp = temp.next();
			} else {
				for (start = temp.readIndex() + start; start < temp.writeIndex(); start++) {
					if (end > 0) {
						writeByte(temp.readByte(start));
						end--;
					} else {
						return;
					}
				}
				break;
			}
		}

		temp = temp.next();
		while (temp != null) {
			for (start = temp.readIndex(); start < temp.writeIndex(); start++) {
				if (end > 0) {
					writeByte(temp.readByte(start));
					end--;
				} else {
					return;
				}
			}
			temp = temp.next();
		}
	}

	/**
	 * 清除所有数据
	 */
	public final void clear() {
		length = 0;
		bounds = -1;

		if (mark != null) {
			read = mark;
			mark = null;
		}
		// 释放ByteBufferUnit只保留一个
		while (read.next() != null) {
			read = read.apart();
			units--;
		}
		write.clear();
	}

	public void release() {
		clear();
		BYTE_BUFFERS.offer(this);
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 获取缓存单元用于从通道(Channel)读取数据，通道数据写入后必须通过{@link #written(int)}设置数量；<br>
	 * 从通道读取数据写入到缓存单元。
	 * 
	 * @return ByteBuffer
	 * @see #written(int)
	 */
	public ByteBuffer write() {
		if (write.isFull()) {
			// 防止用已满的ByteBuffer接收数据,将导致读零
			write = write.link(DataBufferUnit.get());
		}
		// 调整ByteBuffer用于Channel接收数据
		write.buffer().mark();
		write.buffer().position(write.buffer().limit());
		write.buffer().limit(write.buffer().capacity());
		return write.buffer();
	}

	/**
	 * 获取缓存单元用于从通道(Channel)读取数据，通道数据写入后必须通过{@link #written(int)}设置数量；<br>
	 * 从通道读取数据写入到缓存单元，指定可能的数据长度以获取多个缓存单元。
	 * 
	 * @param size 将要写入的最大数据长度
	 * @return ByteBuffer[]
	 */
	public ByteBuffer[] writes(int size) {
		return null;
	}

	/**
	 * 设置缓存单元从通道读取的字节数量，必须事先调用{@link #write()}从通道读取数据；<br>
	 * 此方法将更新{@link #DataBuffer()}中的可读字节数量。
	 * 
	 * @param size 字节数量
	 * @see #write()
	 */
	public void written(int size) {
		if (size > 0) {
			length += size;
		}
		// 调整ByteBuffer用于DataBuffer读写数据
		if (write.buffer().position() > 0) {
			write.buffer().limit(write.buffer().position());
			write.buffer().reset();

			// 不能用flip()会将mark设置为-1
			// 设置limit时如果mark>limit则ByteBuffer内部重置mark=-1
			// mark=-1时执行ByteBuffer.reset()将抛出InvalidMarkException
			// Channel只要写入过数据ByteBuffer.position>0
		} else {
			write.clear();
		}
	}

	/**
	 * 获取缓存单元用于写入数据到通道(Channel)，写入数据到通道后必须通过{@link #read(int)}设置数量；<br>
	 * 将缓存单元的数据写入到通道。
	 * 
	 * @return DataBufferUnit
	 * @see #read(int)
	 * @see #read(long)
	 */
	public ByteBuffer read() {
		while (read.isEmpty()) {
			// 防止用已空的ByteBuffer发送数据,将导致零写
			read = read.apart();
		}
		return read.buffer();
	}

	/**
	 * @see #read()
	 * @return
	 */
	public ByteBuffer[] reads() {
		final ByteBuffer[] buffers = new ByteBuffer[units];
		DataBufferUnit unit = read;
		for (int index = 0; index < buffers.length; index++) {
			buffers[index] = unit.buffer();
			unit = unit.next();
		}
		return buffers;
	}

	/**
	 * 设置缓存单元写入到通道的字节数量，必须事先调用{@link #read()}写入数据到通道；<br>
	 * 此方法将更新{@link #DataBuffer()}中的可读字节数量，并视情况释放已读完的缓存单元。
	 * 
	 * @param size 字节数量
	 * @see #read()
	 */
	public void read(int size) {
		// Channel发送后ByteBuffer的position会改变
		if (size > 0) {
			length -= size;
			while (read.isEmpty()) {
				if (read == write) {
					break;
				} else {
					read = read.apart();
					units--;
				}
			}
		}
	}

	/**
	 * @see #read(int)
	 * @param size
	 */
	public void read(long size) {
		while (size > Integer.MAX_VALUE) {
			read(Integer.MAX_VALUE);
			size -= Integer.MAX_VALUE;
		}
		read((int) size);
	}

	////////////////////////////////////////////////////////////////////////////////

	private Verifier verifier = EmptyVerifier.INSTANCE;

	@Override
	public Verifier getVerifier() {
		return verifier;
	}

	@Override
	public void setVerifier(Verifier v) {
		// 确保字节校验器不为null
		verifier = v == null ? EmptyVerifier.INSTANCE : v;
	}

	////////////////////////////////////////////////////////////////////////////////

	@Override
	public void writeByte(byte value) {
		if (write.isFull()) {
			write = write.link(DataBufferUnit.get());
			units++;
		}
		length++;
		write.writeByte(getVerifier().check(value));
	}

	@Override
	public void writeByte(int b) throws IOException {
		writeByte((byte) b);
	}

	@Override
	public byte readByte() {
		if (length > 0 && bounds != 0) {
			// 读完后最后可能会残留一个 EMPTY 的单元
			// 此单元可能会在转移后混入链表中间导致读取抛出异常
			// 用while循环判断可排除异常,是否有更好的方案避免 EMPTY 单元
			while (read.isEmpty()) {
				if (mark == null) {
					read = read.apart();
					units--;
				} else {
					read = read.next();
				}
			}
			bounds--;
			length--;
			return getVerifier().check(read.readByte());
		}
		throw new IllegalStateException("无数据可读");
	}

	////////////////////////////////////////////////////////////////////////////////

	private final static String HEX_UPPERCASE = "0123456789ABCDEF";

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DataBuffer:");
		builder.append(length);
		if (length > 0) {
			DataBufferUnit unit = read;
			while (unit != null) {
				builder.append("\n");
				builder.append(unit.readIndex());
				builder.append("~");
				builder.append(unit.writeIndex());
				builder.append(" [");
				for (int index = unit.readIndex(); index < unit.writeIndex(); index++) {
					if (index > unit.readIndex()) {
						builder.append(' ');
					}
					int value = unit.readByte(index) & 0xFF;
					builder.append(HEX_UPPERCASE.charAt(value >>> 4));
					builder.append(HEX_UPPERCASE.charAt(value & 0x0F));
				}
				builder.append(']');
				unit = unit.next();
			}
		}
		return builder.toString();
	}
}