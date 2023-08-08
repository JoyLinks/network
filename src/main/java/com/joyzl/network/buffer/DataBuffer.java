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

import com.joyzl.codec.DataInput;
import com.joyzl.codec.DataOutput;
import com.joyzl.network.verifies.EmptyVerifier;
import com.joyzl.network.verifies.Verifiable;
import com.joyzl.network.verifies.Verifier;

/**
 * 数据缓存对象，数据将写入多个ByteBuffer中，或者从多个ByteBuffer中读取数据，根据需要会自动扩展和回收ByteBuffer。
 * 
 * @author ZhangXi
 * @date 2021年3月13日
 */
public final class DataBuffer implements Verifiable, DataInput, DataOutput {

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

	/**
	 * 获取可用缓存单元数量
	 * 
	 * @return 0~n
	 */
	public final static int freeUnitCount() {
		return ByteBufferUnit.freeCount();
	}

	////////////////////////////////////////////////////////////////////////////////

	private ByteBufferUnit read;
	private ByteBufferUnit mark;
	private ByteBufferUnit write;

	private int length;
	private int bounds;
	private int units;

	// 必须至少保留一个ByteBufferUnit
	// 对于获取DataBuffer的程序而言，至少需要一个ByteBufferUnit
	// 默认保留一个可减少获取频率，减少不必要的null判断
	// 不能保证每个缓存单元都是写满的

	private DataBuffer() {
		read = write = ByteBufferUnit.get();
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
		return units * ByteBufferUnit.UNIT_SIZE;
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
	 */
	public final void writeByte(int index, byte value) {
		ByteBufferUnit item = read;
		while (index >= item.readable()) {
			index -= item.readable();
			item = item.next();
		}
		item.writeByte(item.readIndex() + index, value);
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
	 */
	public final byte readByte(int index) {
		ByteBufferUnit item = read;
		while (index >= item.readable()) {
			index -= item.readable();
			item = item.next();
		}
		return item.readByte(item.readIndex() + index);
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
		while (bounds > read.readable()) {
			bounds -= read.readable();
			size += read.readable();
			read = read.apart();
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
				int size = bounds;
				ByteBufferUnit unit = read;
				// 跳过被整个包含在读取限制中的单元
				while (size > unit.readable()) {
					size -= unit.readable();
					unit = unit.next();
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
				unit = unit.next();
				if (unit != null) {
					// 后续单元无须复制，进行整体转移
					target.write = target.write.link(unit);
					target.length += length - bounds - size;
				}
				length = bounds;
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
	 * 删除指定位置指定数量字节,删除的字节不会参与校验,也无法通过reset()恢复
	 *
	 * @param index 0~{@link #readable()}-1
	 * @param len 字节数量
	 */
	public final void delete(int index, int len) {
		if (index >= 0) {
			if (len > 0) {
				if (index + len < readable()) {
					// 从缓存单元删除字节，不移动前后字节
					// 删除后为空的缓存单元被释放

					// 查找删除开始位置
					ByteBufferUnit prev = null, unit = read;
					while (index > unit.readable()) {
						prev = unit;
						index -= unit.readable();
						unit = unit.next();
					}

					// 总长度减
					length -= len;
					// 限制减
					if (bounds > 0) {
						bounds -= len;
					}

					// 情况1单元头部删除
					// 情况2单元中间删除,需要移动后部数据
					// 情况3单元尾部删除
					// 情况4单元全部删除
					do {
						if (index == 0) {
							if (len >= unit.readable()) {
								// 单元全部删除,当前单元需要释放
								len -= unit.readable();
								if (write == unit) {
									write = prev;
								}
								if (read == unit) {
									read = unit = unit.apart();
								} else {
									prev.link(unit = unit.apart());
								}
								units--;
							} else {
								// 单元头部删除,当前单元不能释放
								unit.readIndex(unit.readIndex() + len);
								len = 0;
							}
						} else {
							if (index + len >= unit.writeIndex()) {
								// 单元尾部删除
								len -= unit.writeIndex() - unit.readIndex() - index;
								unit.writeIndex(unit.readIndex() + index);
								index = 0;
							} else {
								// 单元中间删除,需要移动后部数据,当前单元不能释放
								/**
								 * <pre>
								 *       P                                         LIMIT
								 * [ 0 0 1 1 1 1 1 1 0 0 0 0 1 1 1 1 1 1 1 1 1 1 1 1 ]
								 *              LEFT |_______| RIGHT
								 * </pre>
								 */
								index = index + unit.readIndex();// LEFT
								while (index + len < unit.writeIndex()) {
									unit.writeByte(index, unit.readByte(index + len));
									index++;
								}
								unit.writeIndex(index);
								len = 0;
							}
						}
					} while (len > 0);
				} else {
					throw new IndexOutOfBoundsException(index + len);
				}
			} else {
				throw new IllegalArgumentException("长度无效,len:" + len);
			}
		} else {
			throw new IllegalArgumentException("索引位置无效,index:" + index);
		}
	}

	/**
	 * 将源缓存对象中的数据全部转移到当前缓存对象中，源对象数据被清空，不参与校验
	 * 
	 * <p>
	 * 如果设置了读取限制，则仅转移限制内的数据；如果设置了mark则标记将被取消
	 * 
	 * @param source 数据源
	 */
	public final void transmit(DataBuffer source) {
		if (source.readable() > 0) {
			if (length == 0) {
				// 当前缓存实例无数据

				if (source.bounds > 0) {
					// 源缓存实例有边界设置

					// 1 用mark变量临时记录缓存单元头
					mark = source.read;

					// 2 检查头是否可全部转移
					int len = source.bounds;
					if (len > mark.readable()) {
						len -= mark.readable();
						source.units--;
						// 检查后续可全部转移的的单元
						write = mark;
						while (write.next() != null) {
							if (len >= write.next().readable()) {
								len -= write.next().readable();
								write = write.next();
								source.units--;
								units++;
							} else {
								break;
							}
						}

						source.read = write.braek();
						if (source.read == null) {
							source.read = read;
							source.write = read;
						} else {
							read.release();
						}
						read = mark;
					}
					mark = null;

					// 3 转移边界单元残留数据,如果有
					while (len-- > 0) {
						writeByte(source.readByte());
					}

					length = source.bounds;
					source.length -= source.bounds;
					source.bounds = 0;
				} else {
					// 源缓存实例无边界设置
					// 交换两个实例的缓存单元

					mark = read;

					read = source.read;
					write = source.write;
					length = source.length;
					units = source.units;

					source.read = mark;
					source.write = mark;
					source.length = 0;
					source.bounds = -1;
					source.units = 1;
					source.mark = null;

					mark = null;
				}
			} else {
				// 当前缓存实例有数据

				if (source.bounds > 0) {
					// 源缓存实例有边界设置
					mark = source.read;
					int len = source.bounds;
					if (len > mark.readable()) {
						len -= mark.readable();
						source.units--;
						units++;

						ByteBufferUnit temp = mark;
						while (temp.next() != null) {
							if (len >= temp.next().readable()) {
								len -= temp.next().readable();
								temp = temp.next();
								source.units--;
								units++;
							} else {
								break;
							}
						}

						source.read = temp.braek();
						if (source.read == null) {
							source.read = ByteBufferUnit.get();
							source.write = source.read;
						}
						write = write.link(mark);
					}
					mark = null;

					// 3 转移边界单元残留数据,如果有
					while (len-- > 0) {
						writeByte(source.readByte());
					}

					length += source.bounds;
					source.length -= source.bounds;
					source.bounds = 0;
				} else {
					// 源缓存实例无边界设置
					// 将源实例的缓存单元连接到当前实例
					// 可能会出现未写完的缓存单元

					write = write.link(source.read);
					length += source.length;
					units += source.units;
					mark = null;

					source.read = ByteBufferUnit.get();
					source.write = source.read;
					source.length = 0;
					source.bounds = -1;
					source.units = 1;
					source.mark = null;
				}
			}
		} else {
			// 源缓存无可读数据，什么也不用做
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
			ByteBufferUnit temp = source.read;
			while (temp != null) {
				for (index = temp.readIndex(); index < temp.writeIndex(); index++) {
					writeByte(temp.readByte(index));
				}
				temp = temp.next();
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

		ByteBufferUnit temp = source.read;
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

	public ByteBuffer[] getByteBuffers() {
		return null;
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
		}
		write.clear();
		units = 1;
	}

	public void release() {
		clear();
		BYTE_BUFFERS.offer(this);
	}

	////////////////////////////////////////////////////////////////////////////////

	private final static String HEX_UPPERCASE = "0123456789ABCDEF";

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DataBuffer:");
		builder.append(length);
		if (length > 0) {
			ByteBufferUnit unit = read;
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
			write = write.link(ByteBufferUnit.get());
			units++;
		}
		length++;
		write.writeByte(getVerifier().check(value));
	}

	@Override
	public void writeByte(int b) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeShort(short value) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeInt(int value) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeLong(long value) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeFloat(float value) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void writeDouble(double value) throws IOException {
		// TODO Auto-generated method stub

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

	@Override
	public short readShort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int readInt() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long readLong() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float readFloat() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double readDouble() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
}