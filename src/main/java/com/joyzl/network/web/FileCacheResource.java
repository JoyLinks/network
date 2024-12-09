package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.joyzl.network.http.Range.ByteRange;

/**
 * 缓存的文件资源
 * 
 * @author ZhangXi 2024年12月9日
 */
public class FileCacheResource extends FileResource {

	private ByteBuffer identity;

	public FileCacheResource(File root, File file, boolean weak) {
		super(root, file, weak);
	}

	ByteBuffer identity() throws IOException {
		if (identity == null) {
			synchronized (this) {
				if (identity == null) {
					identity = ByteBuffer.allocateDirect((int) getLength());
					try (FileInputStream input = new FileInputStream(getFile());
						FileChannel channel = input.getChannel();) {
						channel.read(identity);
						identity.flip();
					} catch (IOException e) {
						identity = null;
						throw e;
					}
				}
			}
		}
		return identity;
	}

	@Override
	public InputStream getData(String encoding) throws IOException {
		return new ByteBufferInputStream(identity());
	}

	@Override
	public InputStream getData(String encoding, ByteRange range) throws IOException {
		return new ByteBufferPartInputStream(identity(), range.getStart(), range.getSize());
	}

	/**
	 * 包装 ByteBuffer 为 InputStream <br>
	 * 可将同一个 ByteBuffer 同时包装给多个线程使用
	 */
	class ByteBufferInputStream extends InputStream {

		private final ByteBuffer buffer;
		private int index;

		ByteBufferInputStream(ByteBuffer buffer) {
			this.buffer = buffer;
		}

		@Override
		public int read() {
			if (index < buffer.limit()) {
				return buffer.get(index++) & 0xFF;
			}
			return -1;
		}

		@Override
		public int available() {
			return buffer.limit() - index;
		}

		@Override
		public long skip(long n) {
			if (n <= 0) {
				return 0;
			}
			if (n >= available()) {
				n = available();
				index = buffer.limit();
				return n;
			}
			index += n;
			return n;
		}
	}

	/**
	 * 包装 ByteBuffer 为 InputStream <br>
	 * 可将同一个 ByteBuffer 同时包装给多个线程使用
	 */
	class ByteBufferPartInputStream extends InputStream {

		private final ByteBuffer buffer;
		private final int length;
		private int index;

		ByteBufferPartInputStream(ByteBuffer buffer, long offset, long length) {
			if (offset + length > buffer.limit()) {
				throw new IndexOutOfBoundsException();
			}
			this.buffer = buffer;
			this.length = (int) (offset + length);
			index = (int) offset;
		}

		@Override
		public int read() {
			if (index < length) {
				return buffer.get(index++) & 0xFF;
			}
			return -1;
		}

		@Override
		public int available() {
			return buffer.limit() - index;
		}

		@Override
		public long skip(long n) {
			if (n <= 0) {
				return 0;
			}
			if (n >= available()) {
				n = available();
				index = length;
				return n;
			}
			index += n;
			return n;
		}
	}

	/**
	 * 简化 java.io.ByteArrayOutputStream <br>
	 * 取消方法强制同步，可直接获取缓存数组（避免复制）<br>
	 * 取消了数组自动增长，因此创建时必须指定足够数量
	 */
	class ByteArrayOutputStream extends OutputStream {

		private byte buffer[];
		private int count;

		public ByteArrayOutputStream(int size) {
			buffer = new byte[size];
		}

		public void write(int b) {
			buffer[count] = (byte) b;
			count += 1;
		}

		public void reset() {
			count = 0;
		}

		public int size() {
			return count;
		}

		public byte[] buffer() {
			return buffer;
		}
	}
}