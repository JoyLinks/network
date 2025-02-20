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
import com.joyzl.codec.DataInput;
import com.joyzl.codec.DataOutput;
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
		} else {
			// 取消特殊值
			buffer.length = 0;
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

	// 实现注意：
	// 不能保证每个缓存单元都是写满的，应尽量避免较多未满单元出现；
	// 必须至少保留一个ByteBufferUnit，对于获取DataBuffer的程序而言，至少需要一个ByteBufferUnit；
	// 默认保留一个可减少获取频率，减少不必要的null判断；
	// 如果read==write则表示仅一个单元，且首尾均指向这个单元；
	// 内部实现均保证read和write绝不为null。

	private DataBuffer() {
		read = write = DataBufferUnit.get();
		mark = null;
		length = 0;
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
	 * 可读字节数量
	 */
	public final int readable() {
		return length;
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
		DataBufferUnit unit = read;
		while (index >= unit.readable()) {
			index -= unit.readable();
			unit = unit.next();
		}
		unit.set(unit.readIndex() + index, value);
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
	public final int write(final InputStream input, final int len) throws IOException {
		int length = 0, value;
		while (len > length && (value = input.read()) >= 0) {
			writeByte(value);
			length++;
		}
		return length;
	}

	/**
	 * 写入通道中的所有字节(FileChannel > DataBuffer)，不参与校验
	 * 
	 * @param channel
	 * @return 写入数量
	 */
	public final int write(FileChannel channel) throws IOException {
		int l, wrotes = 0;
		while (channel.size() - channel.position() > 0) {
			if (write.isFull()) {
				// 防止用已满的ByteBuffer接收数据,将导致读零
				write = write.link(DataBufferUnit.get());
			}
			l = channel.read(write.receive());
			length += write.received();
			if (l > 0) {
				wrotes += l;
			} else {
				break;
			}
		}
		return wrotes;
	}

	/**
	 * 写入通道中的指定字节(FileChannel > DataBuffer)，不参与校验
	 * 
	 * @param channel 打开的文件通道
	 * @param length 最多可写入数量
	 * @return 写入字节数量
	 */
	public final int write(FileChannel channel, int len) throws IOException {
		int l, wrotes = 0;
		while (len > 0 && channel.size() - channel.position() > 0) {
			if (write.isFull()) {
				// 防止用已满的ByteBuffer接收数据,将导致读零
				write = write.link(DataBufferUnit.get());
			}
			if (len >= write.writeable()) {
				l = channel.read(write.receive());
				length += write.received();
			} else {
				write.receive();
				// 这是特殊处理，通过设置ByteBuffer.limit值减少数据读入
				write.braek().writeIndex(write.readIndex() - write.writeable() - len);
				l = channel.read(write.buffer());
				length += write.received();
			}
			if (l > 0) {
				wrotes += l;
				len -= l;
			} else {
				break;
			}
		}
		return wrotes;
	}

	/**
	 * 写入指定编码的字符，不参与校验
	 * 
	 * @param chars 要写入的字符
	 * @param charset 字符编码
	 * @return 写入字节数量
	 */
	public final int write(final CharBuffer chars, Charset charset) {
		final CharsetEncoder encoder = charset.newEncoder();
		int wrote = 0;
		CoderResult result;
		while (chars.hasRemaining()) {
			if (write.isFull()) {
				// 防止用已满的ByteBuffer接收数据,将导致读零
				write = write.link(DataBufferUnit.get());
			}
			result = encoder.encode(chars, write.receive(), true);
			wrote += write.received();
			length += wrote;
			if (result == CoderResult.UNDERFLOW) {
				break;
			}
			if (result == CoderResult.OVERFLOW) {
				continue;
			}
		}
		return wrote;
	}

	/**
	 * 写入指定编码的字符，限制字节数量有可能导致字符错误的截断，不参与校验
	 * 
	 * @param chars 要写入的字符
	 * @param charset 字符编码
	 * @param len 最多可写入字节数
	 * @return 写入字节数量
	 */
	public final int write(final CharBuffer chars, Charset charset, int len) {
		final CharsetEncoder encoder = charset.newEncoder();
		int wrote = 0;
		CoderResult result;
		while (wrote < len) {
			if (write.isFull()) {
				// 防止用已满的ByteBuffer接收数据,将导致读零
				write = write.link(DataBufferUnit.get());
			}
			result = encoder.encode(chars, write.receive(), true);
			wrote += write.received();
			length += wrote;
			if (result == CoderResult.UNDERFLOW) {
				break;
			}
			if (result == CoderResult.OVERFLOW) {
				continue;
			}
		}
		return wrote;
	}

	/**
	 * 写入缓存对象中所有字节，参与校验
	 */
	public final void write(DataBuffer source) throws IOException {
		while (source.readable() > 0) {
			writeByte(source.readByte());
		}
	}

	/**
	 * 写入缓存对象中所有字节，参与校验
	 */
	public final void write(ByteBuffer source) throws IOException {
		while (source.remaining() > 0) {
			writeByte(source.get());
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
		DataBufferUnit item = read;
		while (index >= item.readable()) {
			index -= item.readable();
			item = item.next();
		}
		return item.get(item.readIndex() + index);
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
	public final void read(OutputStream output, int length) throws IOException {
		while (length > 0) {
			output.write(readUnsignedByte());
			length--;
		}
	}

	/**
	 * 读取所有字节到通道(DataBuffer > FileChannel)，不参与校验
	 * 
	 * @param channel 打开的文件通道
	 */
	public final void read(FileChannel channel) throws IOException {
		int length;
		while (readable() > 0) {
			length = channel.write(read());
			read(length);
		}
	}

	/**
	 * 读取指定字节到通道(DataBuffer > FileChannel)，不参与校验
	 * 
	 * @param channel 打开的文件通道
	 * @param length 字节数量
	 */
	public final void read(FileChannel channel, int length) throws IOException {
		int l;
		while (length > 0 && readable() > 0) {
			l = channel.write(read());
			read(l);
			length -= l;
		}
	}

	/**
	 * 读取指定编码字符，不参与校验
	 * 
	 * @param builder 字符序列
	 * @param chaeset 字符编码
	 * @param length 字节数量
	 */
	public final void read(CharBuffer buffer, Charset chaeset, int length) {
		final CharsetDecoder decoder = chaeset.newDecoder();
		CoderResult result;
		while (length > 0) {
			result = decoder.decode(read(), buffer, true);
			read(result.length());
			if (result == CoderResult.UNDERFLOW) {
				break;
			} else if (result == CoderResult.OVERFLOW) {
				continue;
			} else {

			}
		}
	}

	/**
	 * 读取所有字节到缓存，参与校验
	 * 
	 * @param target DataBufferLink
	 * @throws IOException
	 */
	public final void read(DataBuffer target) throws IOException {
		while (readable() > 0) {
			target.write(readUnsignedByte());
		}
	}

	/**
	 * 标记读取位置，可通过{@link #reset()}恢复标记的位置，可通过{@link #erase()}擦除标记的位置
	 */
	public final void mark() {
		mark = read;
		while (mark != null) {
			mark.mark();
			mark = mark.next();
		}
		mark = read;
	}

	/**
	 * 恢复读取位置，可通过{@link #mark()}标记读取位置，如果之前未执行{@link #mark()}将抛出异常
	 */
	public final void reset() {
		if (mark != null) {
			length = 0;
			read = mark;
			while (mark != null) {
				mark.reset();
				length += mark.readable();
				mark = mark.next();
			}
		}
		// TODO 尾部处理
	}

	/**
	 * 擦除标记的读取位置，擦除后将无法通过{@link #reset()}恢复，如果之前未执行{@link #mark()}将抛出异常
	 */
	public final void erase() {
		// 释放已读完的单元

		if (mark != null) {
			while (mark != read) {
				mark = mark.apart();
			}
			mark = null;
		}
	}

	/**
	 * 将源缓存对象中的数据全部复制到当前缓存对象中尾部，源对象数据保留，不参与校验
	 * <p>
	 * 此方法是多线程安全的，可支持多个线程对一个数据源并发执行复制操作
	 */
	public final void replicate(DataBuffer source) {
		if (source.readable() > 0) {
			int index;
			DataBufferUnit unit = source.read;
			while (unit != null) {
				for (index = unit.readIndex(); index < unit.writeIndex(); index++) {
					writeByte(unit.get(index));
				}
				unit = unit.next();
			}
		}
	}

	/**
	 * 将源缓存对象中的数据全部复制到当前缓存对象中尾部，源对象数据保留，不参与校验
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
						writeByte(temp.get(start));
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
					writeByte(temp.get(start));
					end--;
				} else {
					return;
				}
			}
			temp = temp.next();
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
			if (target.mark != null) {
				mark();
			}
			target.write = target.write.link(read);
			target.length += length;

			read = write = DataBufferUnit.get();
			mark = null;
			length = 0;
		}
	}

	/**
	 * 转移指定数量的数据到目标缓存对象中尾部，转移数据不参与校验，此方法执行数据单元转移；
	 * 读取标记会被擦除，如果要避免擦除应在调用此方法之前执行{@link #reset()}
	 * 
	 * @param target 目标缓存对象
	 * @param size 要转移的数据量
	 */
	public void transfer(DataBuffer target, int size) {
		if (size <= 0 || size > length) {
			throw new IndexOutOfBoundsException(size);
		}
		erase();
		if (target.mark == null && target.length == 0) {
			if (size == length) {
				target.length = size;
				target.read = read;
				read = target.write;
				target.write = write;
				write = read;
				length = 0;
			} else {
				length -= size;
				target.length += size;
				if (size >= read.readable()) {
					DataBufferUnit empty = target.read;
					target.read = target.write = read;
					size -= read.readable();
					read = read.braek();

					while (size >= read.readable()) {
						size -= read.readable();
						mark = read;
						read = read.braek();
						target.write = target.write.link(mark);
					}
					mark = null;

					if (size > 0) {
						empty.clear();
						target.write = target.write.link(empty);
						while (size-- > 0) {
							target.write.writeByte(read.readByte());
						}
					} else {
						empty.release();
					}
				} else {
					target.read.clear();
					while (size-- > 0) {
						target.write.writeByte(read.readByte());
					}
				}
			}
		} else {
			if (size == length) {
				if (target.mark != null) {
					mark();
				}
				target.length += size;
				target.write = target.write.link(read);
				write = read = DataBufferUnit.get();
				length = 0;
			} else {
				length -= size;
				target.length += size;
				while (size >= read.readable()) {
					size -= read.readable();
					mark = read;
					read = read.braek();
					if (target.mark != null) {
						mark.buffer().mark();
					}
					target.write = target.write.link(mark);
				}
				mark = null;

				if (size > 0) {
					target.write = target.write.link(DataBufferUnit.get());
					while (size-- > 0) {
						target.write.writeByte(read.readByte());
					}
					if (target.mark != null) {
						target.write.buffer().mark();
					}
				}
			}
		}
	}

	/**
	 * 转入源缓存对象的数据到当前对象中尾部，转入数据不参与校验，此方法执行数据单元转入；
	 * 读取标记会被擦除，如果要避免擦除应在调用此方法之前执行{@link #reset()}
	 * 
	 * @param source 源缓存对象
	 */
	public void append(DataBuffer source) {
		source.erase();
		if (length == 0) {
			read = source.read;
			source.read = write;
			source.write = write;
			write = read;
		} else if (write.isFull()) {
			write.next(source.read);
			write = source.write;
			source.read = DataBufferUnit.get();
			source.write = source.read;
		} else if (write.isBlank()) {
			// LAST - 1
			DataBufferUnit unit = read;
			while (unit.next() != write) {
				unit = unit.next();
			}

			unit.next(source.read);
			source.read = write;
			write = source.write;
			source.write = source.read;
		} else {
			// 将产生未满单元
			write.next(source.read);
			write = source.write;
			source.read = DataBufferUnit.get();
			source.write = source.read;
		}
		length += source.length;
		source.length = 0;
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
		if (read != write) {
			if (read != null) {
				// 释放ByteBufferUnit只保留一个
				while (read.next() != null) {
					read = read.apart();
				}
			} else {
				if (write != null) {
					read = write;
				}
			}
		}
		write.clear();
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
			throw new IllegalStateException("重复释放");
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
			write = write.link(DataBufferUnit.get());
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
			// 防止用已满的ByteBuffer接收数据,将导致读零
			write = write.link(DataBufferUnit.get());
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
			unit = unit.link(DataBufferUnit.get());
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
		if (size == 0) {
			write.received();
		} else if (size > 0) {
			length += size;
			size -= write.received();
			while (size > 0) {
				write = write.next();
				size -= write.received();
			}
		} else {
			write.received();
			// 特殊处理
			if (size == Integer.MIN_VALUE) {
				write = write.link(DataBufferUnit.get());
			}
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
			// 防止用已空的ByteBuffer发送数据,将导致零写
			read = read.apart();
		}
		return read.buffer();
	}

	/**
	 * 获取所有缓存单元用于将编码的数据写入到其它通道(Channel)，写入数据到通道后必须通过{@link #read(int)}设置数量；
	 * 完成写入并设置其它通道读取的数量，缓存单元的数据已全部写入将自动被释放。
	 * 
	 * @see #read()
	 * @return
	 */
	public ByteBuffer[] reads() {
		final ByteBuffer[] buffers = new ByteBuffer[units()];
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
	 * 取出首部单元
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

	@Override
	public byte readByte() {
		// 读完后最后可能会残留一个 EMPTY 的单元
		// 此单元可能会在转移后混入链表中间导致读取抛出异常
		// 用while循环判断可排除异常,是否有更好的方案避免 EMPTY 单元
		while (read.isEmpty()) {
			if (mark == null) {
				read = read.apart();
			} else {
				read = read.next();
			}
		}
		length--;
		return getVerifier().check(read.readByte());
	}

	@Override
	public void writeByte(int b) {
		writeByte((byte) b);
	}

	@Override
	public void writeByte(byte value) {
		if (write.isFull()) {
			write = write.link(DataBufferUnit.get());
		}
		length++;
		write.writeByte(getVerifier().check(value));
	}

	private void back() {
		if (read == write) {
			throw new IllegalStateException("DataBuffer EMPTY");
		} else {
			// LAST - 1
			DataBufferUnit unit = read;
			while (unit.next() != write) {
				unit = unit.next();
			}
			if (mark == null) {
				unit.next(null);
				write.release();
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
					int value = unit.get(index) & 0xFF;
					builder.append(Character.forDigit(value >>> 4, 16));
					builder.append(Character.forDigit(value & 0x0F, 16));
				}
				builder.append(']');
				unit = unit.next();
			}
		}
		return builder.toString();
	}
}