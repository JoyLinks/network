/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.buffer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.joyzl.codec.BigEndianDataInput;
import com.joyzl.codec.BigEndianDataOutput;
import com.joyzl.codec.LittleEndianDataInput;
import com.joyzl.codec.LittleEndianDataOutput;
import com.joyzl.network.codec.BigEndianBCDInput;
import com.joyzl.network.codec.BigEndianBCDOutput;
import com.joyzl.network.codec.DataInput;
import com.joyzl.network.codec.DataOutput;
import com.joyzl.network.codec.LittleEndianBCDInput;
import com.joyzl.network.codec.LittleEndianBCDOutput;
import com.joyzl.network.verifies.EmptyVerifier;
import com.joyzl.network.verifies.Verifiable;
import com.joyzl.network.verifies.Verifier;

/**
 * 数据缓存对象，数据将写入多个ByteBuffer中，或者从多个ByteBuffer中读取数据，根据需要会自动扩展和回收ByteBuffer。
 * 实例不是多线程安全的，不能在多个线程同时访问实例。
 * <p>
 * 数据缓存对象内部通过链表连接多个数据单元，默认情况下缓存单元写满数据后会自动从单元池获取单元并连接尾部。
 * 数据缓存对象之间的转移或复制基于内部单元，因此无法保证每个单元正好写满数据，允许中间出现未填满的单元。
 * </p>
 * <p>
 * 支持大端和小端编码及解码，默认为大端序，可切换为小端序；每次获取的实例将被重置为大端序。
 * </p>
 * 
 * @author ZhangXi
 * @date 2021年3月13日
 */
public class DataBuffer implements Verifiable, DataInput, DataOutput, //
		BigEndianDataInput, BigEndianDataOutput, LittleEndianDataInput, LittleEndianDataOutput, //
		BigEndianBCDInput, BigEndianBCDOutput, LittleEndianBCDInput, LittleEndianBCDOutput {

	// 对象实例缓存
	private final static ConcurrentLinkedQueue<DataBuffer> BYTE_BUFFERS = new ConcurrentLinkedQueue<>();

	public final static DataBuffer instance() {
		DataBuffer buffer = BYTE_BUFFERS.poll();
		if (buffer == null) {
			buffer = new DataBuffer();
		} else {
			// 取消特殊值
			buffer.length = 0;
			buffer.bigEndian();
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

	private DataBufferUnit mark = null;
	private DataBufferUnit read;
	private DataBufferUnit write;

	private int length = 0;

	// 实现注意：
	// 不能保证每个缓存单元都是写满的，应尽量避免较多未满单元出现；
	// 必须至少保留一个ByteBufferUnit，对于获取DataBuffer的程序而言，至少需要一个ByteBufferUnit；
	// 默认保留一个可减少获取频率，减少不必要的null判断；
	// 如果read==write则表示仅一个单元，且首尾均指向这个单元；
	// 内部实现均保证read和write绝不为null。

	private DataBuffer() {
		read = write = DataBufferUnit.get();
	}

	/**
	 * 获取缓存持有的单元数量（将遍历所有单元以计算数量）
	 */
	public final int units() {
		int size = 0;
		DataBufferUnit unit = read;
		while (unit != null) {
			unit = unit.next();
			size++;
		}
		return size;
	}

	/**
	 * 获取缓存总容量（将遍历所有单元以计算数量）
	 */
	public final int capacity() {
		int size = 0;
		DataBufferUnit unit = read;
		while (unit != null) {
			size += unit.capacity();
			unit = unit.next();
		}
		return size;
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 获取当前剩余可写入字节数量，写入字节超过此数量将自动扩展
	 * <p>
	 * 此方法仅返回当前写入单元的剩余数量，不包括其它单元的空闲空间。
	 * </p>
	 */
	public final int writeable() {
		return write.writeable();
	}

	/**
	 * 设置指定位置字节，读写位置不变，不参与校验
	 *
	 * @param index 0~{@link #readable()}-1
	 * @param value byte
	 */
	public final void set(int index, byte value) {
		DataBufferUnit unit = read;
		while (index >= unit.readable()) {
			index -= unit.readable();
			unit = unit.next();
		}
		unit.set(unit.readIndex() + index, value);
	}

	@Override
	public void writeByte(int b) {
		writeByte((byte) b);
	}

	@Override
	public void writeByte(byte value) {
		getVerifier().check(value);
		if (write.isFull()) {
			write = write.extend();
		}
		write.writeByte(value);
		length++;
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
	 * 写入输入流中的全部字节(InputStream > DataBuffer)，参与校验
	 * 
	 * @param input InputStream
	 * @return 写入数量
	 */
	public final int write(final InputStream input) throws IOException {
		int length = 0, value;
		while ((value = input.read()) >= 0) {
			writeByte(value);
			length++;
		}
		return length;
	}

	/**
	 * 写入输入流中的指定字节(InputStream > DataBuffer)，参与校验
	 * 
	 * @param input InputStream
	 * @param len 最多可写入数量
	 * @return 写入数量
	 */
	public final int write(final InputStream input, int len) throws IOException {
		int size = 0, value;
		while (size < len && (value = input.read()) >= 0) {
			writeByte(value);
			size++;
		}
		return size;
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 可读字节数量
	 */
	public final int readable() {
		return length;
	}

	/**
	 * 获取指定位置字节，读写位置不变，不参与校验
	 *
	 * @param index 0~{@link #readable()}-1
	 */
	public final byte get(int index) {
		DataBufferUnit unit = read;
		while (index >= unit.readable()) {
			index -= unit.readable();
			unit = unit.next();
		}
		return unit.get(unit.readIndex() + index);
	}

	@Override
	public byte readByte() {
		if (read.isEmpty()) {
			if (read.next() != null) {
				if (mark == null) {
					read = read.curtail();
				} else {
					read = read.next();
				}
			} else {
				throw new IllegalStateException("DataBuffer:EMPTY");
			}
		}
		length--;
		return getVerifier().check(read.readByte());
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
	 * 读取所有字节到输出流(DataBuffer > OutputStream)，参与校验
	 * 
	 * @param output
	 */
	public final void read(OutputStream output) throws IOException {
		while (readable() > 0) {
			output.write(readUnsignedByte());
		}
	}

	/**
	 * 读取指定字节到输出流(DataBuffer > OutputStream)，参与校验
	 * 
	 * @param output
	 */
	public final void read(OutputStream output, int len) throws IOException {
		while (len > 0) {
			output.write(readUnsignedByte());
			len--;
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 将源缓存对象中的数据全部复制到当前缓存对象中尾部，源对象数据保留，不参与校验
	 * <p>
	 * 此方法相对于源缓存是多线程安全的，可支持多个线程对一个数据源并发执行复制操作；<br>
	 * 复制完成后，源缓存对象的读写位置和标记不会发生任何变化。
	 */
	public final void replicate(DataBuffer source) {
		DataBufferUnit unit = source.read;
		int size, p = -1;
		while (unit != null) {
			if (p < 0) {
				p = unit.readIndex();
			}
			size = unit.writeIndex() - p;
			if (size <= 0) {
				unit = unit.next();
				p = -1;
				continue;
			}
			if (write.isFull()) {
				write = write.extend();
			}
			if (write.writeable() < size) {
				size = write.writeable();
			}
			write.receive().put(write.readIndex(), unit.buffer(), p, size);
			write.readIndex(write.readIndex() + size);
			length += write.received();
			p += size;
		}
	}

	/**
	 * 将源缓存对象中的数据全部复制到当前缓存对象中尾部，源对象数据保留，不参与校验
	 * <p>
	 * 此方法相对于源缓存是多线程安全的，可支持多个线程对一个数据源并发执行复制操作；<br>
	 * 复制完成后，源缓存对象的读写位置和标记不会发生任何变化。
	 * 
	 * @param source 要复制的数据源
	 * @param offset 0 ~ source.readable()
	 * @param len offset ~ source.readable()
	 */
	public final void replicate(DataBuffer source, int offset, int len) {
		if (offset < 0 || len <= 0 || source.readable() < offset + len) {
			throw new IllegalArgumentException("DataBuffer:Argument OVERFLOW");
		}

		DataBufferUnit unit = source.read;
		int size, p = -1;
		// SKIP offset
		while (offset > 0 && unit != null) {
			if (offset < unit.readable()) {
				p = unit.readIndex() + offset;
				offset = 0;
				break;
			} else {
				offset -= unit.readable();
				unit = unit.next();
			}
		}
		// COPY
		while (len > 0 && unit != null) {
			if (p < 0) {
				p = unit.readIndex();
			}
			size = unit.writeIndex() - p;
			if (size <= 0) {
				unit = unit.next();
				p = -1;
				continue;
			}
			if (size > len) {
				size = len;
			}
			if (write.isFull()) {
				write = write.extend();
			}
			if (write.writeable() < size) {
				size = write.writeable();
			}
			write.receive().put(write.readIndex(), unit.buffer(), p, size);
			write.readIndex(write.readIndex() + size);
			length += write.received();
			len -= size;
			p += size;
		}
	}

	/**
	 * 转移所有数据到目标缓存对象中尾部，转移数据不参与校验，此方法执行数据单元转移；
	 * 读取标记会被擦除，如果要避免擦除应在调用此方法之前执行{@link #reset()}
	 * 
	 * @param target 目标缓存对象
	 */
	public void transfer(DataBuffer target) {
		erase();
		if (length > 0) {
			if (target.mark == null) {
				if (target.length == 0) {
					// 直接交换
					target.length = length;
					length = 0;
					mark = target.read;
					target.read = read;
					read = mark;
					mark = target.write;
					target.write = write;
					write = mark;
					mark = null;
					return;
				}
			}
			target.write = target.write.link(read);
			read = write = DataBufferUnit.get();
			target.length += length;
			length = 0;
		}
	}

	/**
	 * 转移指定数量的数据到目标缓存对象中尾部，转移数据不参与校验，此方法执行数据单元转移；
	 * 读取标记会被擦除，如果要避免擦除应在调用此方法之前执行{@link #reset()}
	 * 
	 * @param target 目标缓存对象
	 * @param len 要转移的数据量
	 */
	public void transfer(DataBuffer target, int len) {
		if (len <= 0 || len > length) {
			throw new IllegalArgumentException("DataBuffer:Argument OVERFLOW");
		}

		erase();
		if (length > 0) {
			if (target.mark == null) {
				if (target.length == 0) {
					if (len == length) {
						// 直接交换
						target.length = len;
						length = 0;
						mark = target.read;
						target.read = read;
						read = mark;
						mark = target.write;
						target.write = write;
						write = mark;
						mark = null;
						return;
					} else {
						target.length = len;
						length -= len;

						if (len > read.readable()) {
							mark = target.read;

							// 转移单元
							target.read = read;
							do {
								len -= read.readable();
								target.write = read;
								read = read.next();
							} while (len >= read.readable());
							target.write.braek();

							// 检查空闲单元
							if (len > target.write.writeable()) {
								target.write = target.write.link(mark);
							} else {
								mark.release();
							}
							mark = null;
						} else {
							// 单元恢复原始位置
							target.write.clear();
						}

						if (len > 0) {
							// 转移单元剩余数据
							target.write.receive().put(read.send(len));
							target.write.received();
							read.sent();
						}
						return;
					}
				}
			}

			if (len == length) {
				// 直接连接
				target.length += len;
				length = 0;

				target.write = target.write.link(read);
				read = write = DataBufferUnit.get();
			} else {
				target.length += len;
				length -= len;

				while (len >= read.readable()) {
					len -= read.readable();
					target.write.next(read);
					read = read.braek();
				}

				if (len > 0) {
					if (len > target.write.writeable()) {
						target.write = target.write.extend();
					}
					target.write.receive().put(read.send(len));
					target.write.received();
					read.sent();
				}
			}
		}
	}

	/**
	 * 转移所有字节到通道(DataBuffer > FileChannel)，不参与校验
	 * 
	 * @param target 打开的文件通道
	 */
	public final void transfer(FileChannel target) throws IOException {
		erase();
		while (length > 0) {
			if (read.isEmpty()) {
				read = read.curtail();
			}
			target.write(read.send());
			length -= read.sent();
		}
	}

	/**
	 * 转移指定字节到通道(DataBuffer > FileChannel)，不参与校验
	 * 
	 * @param target 打开的文件通道
	 * @param len 字节数量
	 */
	public final void transfer(FileChannel target, int len) throws IOException {
		if (len <= 0 || len > length) {
			throw new IllegalArgumentException("DataBuffer:Argument OVERFLOW");
		}
		erase();
		while (len > 0) {
			if (read.isEmpty()) {
				read = read.curtail();
			}
			len -= target.write(read.send(len));
			length -= read.sent();
		}
	}

	/**
	 * 读取指定编码字符，不参与校验
	 * 
	 * @param builder 字符序列
	 * @param chaeset 字符编码
	 * @param length 字节数量
	 */
	public final void transfer(CharBuffer target, Charset chaeset) {
		final CharsetDecoder decoder = chaeset.newDecoder();
		CoderResult result;
		while (length > 0) {
			if (read.isEmpty()) {
				read = read.curtail();
			}
			result = decoder.decode(read.send(), target, false);
			length -= read.sent();
			if (result == CoderResult.UNDERFLOW) {
				continue;
			} else if (result == CoderResult.OVERFLOW) {
				continue;
			} else {
				throw new IllegalArgumentException("DataBuffer:" + result.toString());
			}
		}
	}

	/**
	 * @see {@link #transfer(DataBuffer)}
	 */
	public void append(DataBuffer source) {
		source.transfer(this);
	}

	/**
	 * @see {@link #transfer(DataBuffer, int)}
	 */
	public void append(DataBuffer source, int length) {
		source.transfer(this);
	}

	/**
	 * 转入通道中的所有字节(FileChannel > DataBuffer)，不参与校验
	 * 
	 * @param channel
	 * @return 写入数量
	 */
	public final int append(FileChannel channel) throws IOException {
		int size = 0;
		while (channel.position() < channel.size()) {
			if (write.isFull()) {
				write = write.extend();
			}
			if (channel.read(write.receive()) > 0) {
				size += write.received();
			} else {
				break;
			}
		}
		length += size;
		return size;
	}

	/**
	 * 转入通道中的指定字节(FileChannel > DataBuffer)，不参与校验
	 * 
	 * @param channel 打开的文件通道
	 * @param length 最多可写入数量
	 * @return 写入字节数量
	 */
	public final int append(FileChannel channel, int len) throws IOException {
		if (len <= 0 || len > channel.size()) {
			throw new IllegalArgumentException("DataBuffer:Argument OVERFLOW");
		}

		int l, size = 0;
		while (len > 0 && channel.position() < channel.size()) {
			if (write.isFull()) {
				write = write.extend();
			}
			if ((l = channel.read(write.receive(len))) > 0) {
				size += write.received();
				len -= l;
			} else {
				break;
			}
		}
		length += size;
		return size;
	}

	/**
	 * 写入指定编码的字符，不参与校验
	 * 
	 * @param chars 要写入的字符
	 * @param charset 字符编码
	 * @return 写入字节数量
	 */
	public final int append(final CharBuffer chars, Charset charset) {
		int size = 0;
		CoderResult result;
		final CharsetEncoder encoder = charset.newEncoder();
		while (chars.hasRemaining()) {
			if (write.isFull()) {
				write = write.extend();
			}
			result = encoder.encode(chars, write.receive(), true);
			size += write.received();
			if (result == CoderResult.UNDERFLOW) {
				break;
			} else if (result == CoderResult.OVERFLOW) {
				write = write.extend();
				continue;
			}
		}
		length += size;
		return size;
	}

	/**
	 * 写入缓存对象中所有字节，参与校验
	 */
	public final void append(ByteBuffer source) throws IOException {
		int limit = source.limit();
		while (source.hasRemaining()) {
			if (write.isFull()) {
				write = write.extend();
			}
			if (write.writeable() < source.remaining()) {
				source.limit(source.position() + write.writeable());
				write.receive().put(source);
				length += write.received();
				source.limit(limit);
			} else {
				write.receive().put(source);
				length += write.received();
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	// 以下方法提供从尾部回退操作支持

	/**
	 * 回退尾部单元，此方法回收尾部单元，此方法既不判断数据量也不更新数据量
	 */
	private void back() {
		if (read == write) {
			throw new IllegalStateException("DataBuffer:EMPTY");
		} else {
			// LAST - 1
			DataBufferUnit unit = read;
			while (unit.next() != write) {
				unit = unit.next();
			}
			if (mark == null) {
				unit.braek().release();
			}
			write = unit;
		}
	}

	/**
	 * 从缓存尾部获取数据
	 */
	public byte backByte() {
		if (write.readable() <= 0) {
			back();
		}

		length--;
		return write.backByte();
	}

	/**
	 * 从缓存尾部丢弃数据
	 */
	public void backSkip(int size) {
		while (size > 0) {
			if (write.readable() <= 0) {
				back();
			} else if (write.readable() <= size) {
				size -= write.readable();
				length -= write.readable();
				if (read == write) {
					write.readIndex(0);
					write.writeIndex(0);
				} else {
					back();
				}
			} else {
				write.writeIndex(write.writeIndex() - size);
				length -= size;
				size = 0;
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 标记读取位置，可通过{@link #reset()}恢复标记的位置，可通过{@link #erase()}擦除标记的位置
	 */
	public final void mark() {
		write = read;
		write.mark();
		while (write.next() != null) {
			write = write.next();
			write.mark();
		}
		mark = read;
	}

	/**
	 * 恢复读取位置，可通过{@link #mark()}标记读取位置，如果之前未执行{@link #mark()}将没有任何效果
	 */
	public final void reset() {
		if (mark != null) {
			read = mark;
			read.reset();
			length = read.readable();

			while (read.next() != null) {
				if (read.next().marked()) {
					read = read.next();
					read.reset();
					length += read.readable();
					write = read;
				} else {
					read = read.braek();
					read.release();
					break;
				}
			}
			read = mark;
		}
	}

	/**
	 * 擦除标记的读取位置，擦除后将无法通过{@link #reset()}恢复，如果之前未执行{@link #mark()}将没有任何效果
	 */
	public final void erase() {
		if (mark != null) {
			while (mark != read) {
				mark = mark.curtail();
			}
			while (read.isEmpty()) {
				if (read.next() != null) {
					read = read.curtail();
				} else {
					read.clear();
					return;
				}
			}
			write = read;
			while (write.next() != null) {
				if (write.next().isEmpty()) {
					mark = write.braek();
					write.next(mark.curtail());
				} else {
					write = write.next();
				}
			}
			mark = null;
		}
	}

	/**
	 * 清除所有数据
	 */
	public final void clear() {
		length = 0;
		if (mark != null) {
			read = mark;
			mark = null;
		}
		if (read != null) {
			while (read.next() != null) {
				read = read.curtail();
			}
			write = read;
		} else {
			if (write != null) {
				read = write;
			} else {
				read = write = DataBufferUnit.get();
			}
		}
		read.clear();
	}

	public void release() {
		// 特殊值标记是否已释放
		if (length != Integer.MIN_VALUE) {
			clear();
			verifier = EmptyVerifier.INSTANCE;
			length = Integer.MIN_VALUE;
			BYTE_BUFFERS.offer(this);
		} else {
			// 重复释放抛出异常
			throw new IllegalStateException("DataBuffer:RELEASED");
		}
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
			// 防止用已满的ByteBuffer接收数据
			// 网络读取时将导致读零
			write = write.extend();
		}
		return write.receive();
	}

	/**
	 * 获取缓存单元用于从通道(Channel)读取数据，通道数据写入后必须通过{@link #written(int)}设置数量；<br>
	 * 从通道读取数据写入到缓存单元，指定可能的数据长度以获取多个缓存单元。
	 * 
	 * @param size 将要写入的最大数据长度
	 * @return ByteBuffer[]
	 */
	public ByteBuffer[] writes(int size) {
		if (write.isFull()) {
			// 防止用已满的ByteBuffer接收数据
			// 网络读取时将导致读零
			write = write.extend();
		}
		// 计算所需的ByteBuffer数量
		if (size <= write.writeable()) {
			size = 1;
		} else {
			size -= write.writeable();
			if (size % DataBufferUnit.BYTES > 0) {
				size = size / DataBufferUnit.BYTES + 2;
			} else {
				size = size / DataBufferUnit.BYTES + 1;
			}
		}
		// 构建ByteBuffer数组
		final ByteBuffer[] buffers = new ByteBuffer[size];
		DataBufferUnit unit = write;
		buffers[size = 0] = unit.receive();
		while (++size < buffers.length) {
			unit = unit.extend();
			buffers[size] = unit.receive();
		}
		return buffers;
	}

	/**
	 * 设置缓存单元从通道读取的字节数量，必须事先调用{@link #write()}从通道读取数据；<br>
	 * 此方法将更新{@link #DataBuffer()}中的可读字节数量。
	 * 
	 * @param size 字节数量
	 * @see #write()
	 */
	public void written(int size) {
		// 无论是否写入数据必须调整用于写入的单元

		while (write.next() != null) {
			length += write.received();
			if (write.isBlank()) {
				write = write.curtail();
			} else {
				write = write.next();
			}
		}
		length += write.received();

		// 特殊处理
		if (size == Integer.MIN_VALUE) {
			// TLS编码时剩余极少的空间不足以继续处理
			write = write.extend();
		}
	}

	/**
	 * 获取首部缓存单元用于将编码好的数据写入到其它通道(Channel)，写入数据到通道后必须通过{@link #read(int)}设置数量；
	 * 完成写入并设置其它通道读取的数量，如果当前缓存单元的数据已全部写入将自动被释放。
	 * 
	 * @return DataBufferUnit
	 * @see #read(int)
	 * @see #read(long)
	 */
	public ByteBuffer read() {
		while (read.isEmpty()) {
			// 防止用已空的ByteBuffer发送数据
			// 网络读取时将导致读零
			if (read.next() != null) {
				read = read.curtail();
			} else {
				throw new IllegalStateException("DataBuffer:EMPTY");
			}
		}
		return read.send();
	}

	/**
	 * 获取所有缓存单元用于将编码的数据写入到其它通道(Channel)，写入数据到通道后必须通过{@link #read(int)}设置数量；
	 * 完成写入并设置其它通道读取的数量，缓存单元的数据已全部写入将自动被释放。
	 * 
	 * @see #read()
	 * @return
	 */
	public ByteBuffer[] reads() {
		while (read.isEmpty()) {
			if (read.next() != null) {
				read = read.curtail();
			} else {
				throw new IllegalStateException("DataBuffer:EMPTY");
			}
		}

		int size = 1;
		DataBufferUnit unit = read;
		while (unit.next() != null) {
			if (unit.next().isEmpty()) {
				unit.next(unit.next().curtail());
			} else {
				unit = unit.next();
				size++;
			}
		}

		unit = read;
		final ByteBuffer[] buffers = new ByteBuffer[size];
		for (int index = 0; index < size; index++) {
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
		if (size >= 0) {
			length -= size;
			while (read.isEmpty()) {
				if (read.next() != null) {
					read = read.curtail();
				} else {
					break;
				}
			}
		}
	}

	/**
	 * @see #read(int)
	 * @param size
	 */
	public void read(long size) {
		if (size >= 0) {
			length -= size;
			while (read.isEmpty()) {
				if (read.next() != null) {
					read = read.curtail();
				} else {
					break;
				}
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////////
	// 这些方法提供对内部数据单元的操作能力
	// 数据单元只能从首部取出，取出后当前(DataBuffer)实例的可读数据将相应减少
	// 数据单元只能从尾部连接，连接后当前(DataBuffer)实例的可读数据将相应增加

	/**
	 * 获取首部单元
	 */
	public DataBufferUnit head() {
		return read;
	}

	/**
	 * 取出首部单元，取出的单元将于当前缓存对象断开联系
	 */
	public DataBufferUnit take() {
		if (readable() > 0) {
			final DataBufferUnit unit = read;
			length -= unit.readable();
			if (read == write) {
				read = write = DataBufferUnit.get();
			} else {
				read = unit.braek();
			}
			return unit;
		}
		return null;
	}

	/**
	 * 连接单元到尾部
	 */
	public void link(DataBufferUnit unit) {
		if (read == write) {
			if (read.isEmpty() || read.isBlank()) {
				read.release();
				read = unit;
			}
		}
		length += unit.readable();
		while (unit.next() != null) {
			unit = unit.next();
			length += unit.readable();
		}
		write = unit;
	}

	////////////////////////////////////////////////////////////////////////////////
	// 数据校验支持

	private Verifier verifier = EmptyVerifier.INSTANCE;

	@Override
	public Verifier getVerifier() {
		return verifier;
	}

	@Override
	public void setVerifier(Verifier v) {
		// 确保字节校验器不为null
		// 以此减少大量空判断
		verifier = v == null ? EmptyVerifier.INSTANCE : v;
	}

	////////////////////////////////////////////////////////////////////////////////

	/**
	 * 将执行逐字节比较，允许内部缓存单元差异
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o instanceof DataBuffer) {
			final DataBuffer other = (DataBuffer) o;
			if (other.readable() == this.readable()) {
				// 执行逐字节比较
				// 允许单元结构差异
				for (int index = 0; index < this.readable(); index++) {
					if (other.get(index) != this.get(index)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("DataBuffer:");
		builder.append(length);
		DataBufferUnit unit = read;
		while (unit != null) {
			builder.append("\n\t");
			builder.append(unit.readIndex());
			builder.append("~");
			builder.append(unit.writeIndex());
			builder.append(" [");
			for (int index = unit.readIndex(); index < unit.writeIndex(); index++) {
				if (index > unit.readIndex()) {
					builder.append(' ');
				}
				int value = unit.get(index) & 0xFF;
				builder.append(Character.forDigit(value >>> 4, 16));
				builder.append(Character.forDigit(value & 0x0F, 16));
			}
			builder.append(']');
			unit = unit.next();
		}
		return builder.toString();
	}

	////////////////////////////////////////////////////////////////////////////////
	// 大小端编码切换支持 ///////////////////////////////////////////////////////////

	private boolean be = true;

	/** 切换为大端编码格式 */
	public void bigEndian() {
		be = true;
	}

	/** 切换为小端编码格式 */
	public void littleEndian() {
		be = false;
	}

	@Override
	public int readUnsignedShort() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readUnsignedShort();
		} else {
			return LittleEndianBCDInput.super.readUnsignedShort();
		}
	}

	@Override
	public int readInt() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readInt();
		} else {
			return LittleEndianBCDInput.super.readInt();
		}
	}

	@Override
	public short readShort() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readShort();
		} else {
			return LittleEndianBCDInput.super.readShort();
		}
	}

	@Override
	public float readFloat() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readFloat();
		} else {
			return LittleEndianBCDInput.super.readFloat();
		}
	}

	@Override
	public long readLong() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readLong();
		} else {
			return LittleEndianBCDInput.super.readLong();
		}
	}

	@Override
	public double readDouble() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readDouble();
		} else {
			return LittleEndianBCDInput.super.readDouble();
		}
	}

	@Override
	public int readUnsignedMedium() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readUnsignedMedium();
		} else {
			return LittleEndianBCDInput.super.readUnsignedMedium();
		}
	}

	@Override
	public long readUnsignedInt() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readUnsignedInt();
		} else {
			return LittleEndianBCDInput.super.readUnsignedInt();
		}
	}

	@Override
	public int readMedium() throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readMedium();
		} else {
			return LittleEndianBCDInput.super.readMedium();
		}
	}

	@Override
	public void writeFloat(float value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeFloat(value);
		} else {
			LittleEndianBCDOutput.super.writeFloat(value);
		}
	}

	@Override
	public void writeMedium(int value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeMedium(value);
		} else {
			LittleEndianBCDOutput.super.writeMedium(value);
		}
	}

	@Override
	public void writeDouble(double value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeDouble(value);
		} else {
			LittleEndianBCDOutput.super.writeDouble(value);
		}
	}

	@Override
	public void writeInt(int value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeInt(value);
		} else {
			LittleEndianBCDOutput.super.writeInt(value);
		}
	}

	@Override
	public void writeLong(long value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeLong(value);
		} else {
			LittleEndianBCDOutput.super.writeLong(value);
		}
	}

	@Override
	public void writeShort(short value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeShort(value);
		} else {
			LittleEndianBCDOutput.super.writeShort(value);
		}
	}

	@Override
	public void writeBCD8421s(int value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD8421s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD8421s(value);
		}
	}

	@Override
	public void writeBCD8421s(CharSequence value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD8421s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD8421s(value);
		}
	}

	@Override
	public void writeBCD8421s(CharSequence value, int offset, int length) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD8421s(value, offset, length);
		} else {
			LittleEndianBCDOutput.super.writeBCD8421s(value, offset, length);
		}
	}

	@Override
	public void writeBCDs(int value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCDs(value);
		} else {
			LittleEndianBCDOutput.super.writeBCDs(value);
		}
	}

	@Override
	public void writeBCDs(CharSequence value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCDs(value);
		} else {
			LittleEndianBCDOutput.super.writeBCDs(value);
		}
	}

	@Override
	public void writeBCDs(CharSequence value, int offset, int length) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCDs(value, offset, length);
		} else {
			LittleEndianBCDOutput.super.writeBCDs(value, offset, length);
		}
	}

	@Override
	public void writeBCD3s(int value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD3s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD3s(value);
		}
	}

	@Override
	public void writeBCD3s(CharSequence value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD3s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD3s(value);
		}
	}

	@Override
	public void writeBCD3s(CharSequence value, int offset, int length) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD3s(value, offset, length);
		} else {
			LittleEndianBCDOutput.super.writeBCD3s(value, offset, length);
		}
	}

	@Override
	public void writeBCD2421s(int value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD2421s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD2421s(value);
		}
	}

	@Override
	public void writeBCD2421s(CharSequence value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD2421s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD2421s(value);
		}
	}

	@Override
	public void writeBCD2421s(CharSequence value, int offset, int length) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD2421s(value, offset, length);
		} else {
			LittleEndianBCDOutput.super.writeBCD2421s(value, offset, length);
		}
	}

	@Override
	public String readBCD5421String(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD5421String(size);
		} else {
			return LittleEndianBCDInput.super.readBCD5421String(size);
		}
	}

	@Override
	public void writeBCD5421s(int value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD5421s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD5421s(value);
		}
	}

	@Override
	public void writeBCD5421s(CharSequence value) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD5421s(value);
		} else {
			LittleEndianBCDOutput.super.writeBCD5421s(value);
		}
	}

	@Override
	public void writeBCD5421s(CharSequence value, int offset, int length) throws IOException {
		if (be) {
			BigEndianBCDOutput.super.writeBCD5421s(value, offset, length);
		} else {
			LittleEndianBCDOutput.super.writeBCD5421s(value, offset, length);
		}
	}

	@Override
	public String readBCDString(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCDString(size);
		} else {
			return LittleEndianBCDInput.super.readBCDString(size);
		}
	}

	@Override
	public int readBCD8421s(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD8421s(size);
		} else {
			return LittleEndianBCDInput.super.readBCD8421s(size);
		}
	}

	@Override
	public String readBCD8421String(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD8421String(size);
		} else {
			return LittleEndianBCDInput.super.readBCD8421String(size);
		}
	}

	@Override
	public int readBCD2421s(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD2421s(size);
		} else {
			return LittleEndianBCDInput.super.readBCD2421s(size);
		}
	}

	@Override
	public String readBCD2421String(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD2421String(size);
		} else {
			return LittleEndianBCDInput.super.readBCD2421String(size);
		}
	}

	@Override
	public int readBCD5421s(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD5421s(size);
		} else {
			return LittleEndianBCDInput.super.readBCD5421s(size);
		}
	}

	@Override
	public int readBCD3s(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD3s(size);
		} else {
			return LittleEndianBCDInput.super.readBCD3s(size);
		}
	}

	@Override
	public String readBCD3String(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCD3String(size);
		} else {
			return LittleEndianBCDInput.super.readBCD3String(size);
		}
	}

	@Override
	public int readBCDs(int size) throws IOException {
		if (be) {
			return BigEndianBCDInput.super.readBCDs(size);
		} else {
			return LittleEndianBCDInput.super.readBCDs(size);
		}
	}
}