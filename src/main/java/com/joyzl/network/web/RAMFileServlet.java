package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import com.joyzl.network.Utility;
import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 内存缓存的文件资源服务
 * 
 * @author ZhangXi 2023年9月15日
 */
public class RAMFileServlet extends FileResourceServlet {

	/** 缓存的文件扩展名 */
	private String[] caches = new String[] { ".jpg", ".jpeg", ".png", ".gif" };
	/** 资源对象缓存 */
	private final Map<String, WEBResource> resources = new ConcurrentHashMap<>();

	public RAMFileServlet(String root) {
		super(new File(root));
	}

	public RAMFileServlet(File root) {
		super(root);
	}

	@Override
	protected WEBResource find(String path) {
		WEBResource resource = resources.get(path);
		if (resource == null) {
			File file = new File(getRoot(), path);
			if (file.exists()) {
				if (file.isDirectory()) {
					if (path.endsWith("/")) {
						// 查找默认页面
						final File page = findDefault(file);
						if (page != null) {
							synchronized (this) {
								resource = resources.get(path);
								if (resource == null) {
									if (canCompress(page)) {
										resource = new CompressResource(getRoot(), page);
									} else if (canCache(page)) {
										resource = new CacheResource(getRoot(), page);
									} else {
										resource = new FileResource(getRoot(), page, isWeak());
									}
									resources.put(resource.getContentLocation(), resource);
									resources.put(path, resource);
								}
							}
						} else {
							// 返回目录资源
							// 可用于重定向或返回目录列表
							resource = new DirResource(getRoot(), file, isBrowse());
						}
					} else {
						// 返回目录资源
						// 可用于重定向或返回目录列表
						resource = new DirResource(getRoot(), file, isBrowse());
					}
				} else {
					synchronized (this) {
						resource = resources.get(path);
						if (resource == null) {
							if (canCompress(file)) {
								resource = new CompressResource(getRoot(), file);
							} else if (canCache(file)) {
								resource = new CacheResource(getRoot(), file);
							} else {
								resource = new FileResource(getRoot(), file, isWeak());
							}
							resources.put(path, resource);
						}
					}
				}
			} else {
				// 尝试查找
				resource = FileMultiple.find(file);
			}
		}
		return resource;
	}

	protected boolean canCache(File file) {
		if (file.length() < MAX) {
			for (int index = 0; index < caches.length; index++) {
				if (Utility.ends(file.getPath(), caches[index], true)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取应缓存的文件扩展名
	 */
	public String[] getCaches() {
		return caches;
	}

	/**
	 * 设置应缓存的文件扩展名
	 */
	public void setCaches(String[] values) {
		if (values == null) {
			caches = new String[0];
		} else {
			caches = values;
		}
	}

	/**
	 * @see #setCaches(String[])
	 */
	public void setCaches(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			caches = new String[0];
		} else {
			caches = new String[values.size()];
			int index = 0;
			for (String value : values) {
				caches[index++] = value;
			}
		}
	}

	class CacheResource extends FileResource {

		private ByteBuffer identity;

		public CacheResource(File root, File file) {
			super(root, file, isWeak());
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
	}

	class CompressResource extends CacheResource {

		private ByteBuffer deflate, gzip;

		public CompressResource(File root, File file) {
			super(root, file);
		}

		void deflate() throws IOException {
			if (deflate == null) {
				synchronized (this) {
					if (deflate == null) {
						final ByteArrayOutputStream buffer = new ByteArrayOutputStream((int) getLength());
						try (FileInputStream input = new FileInputStream(getFile());
							DeflaterOutputStream output = new DeflaterOutputStream(buffer);) {
							input.transferTo(output);
							output.flush();
							output.finish();
						}
						deflate = ByteBuffer.allocateDirect(buffer.size());
						deflate.put(buffer.buffer(), 0, buffer.size());
						deflate.flip();
					}
				}
			}
		}

		void gzip() throws IOException {
			if (gzip == null) {
				synchronized (this) {
					if (gzip == null) {
						final ByteArrayOutputStream buffer = new ByteArrayOutputStream((int) getLength());
						try (FileInputStream input = new FileInputStream(getFile());
							GZIPOutputStream output = new GZIPOutputStream(buffer)) {
							input.transferTo(output);
							output.flush();
							output.finish();
						}
						gzip = ByteBuffer.allocateDirect(buffer.size());
						gzip.put(buffer.buffer(), 0, buffer.size());
						gzip.flip();
					}
				}
			}
		}

		@Override
		public String fitEncoding(AcceptEncoding acceptEncoding) {
			if (acceptEncoding != null) {
				for (int index = 0; index < acceptEncoding.size(); index++) {
					if (AcceptEncoding.GZIP.equals(acceptEncoding.getValue(index))) {
						return AcceptEncoding.GZIP;
					}
					if (AcceptEncoding.DEFLATE.equals(acceptEncoding.getValue())) {
						return AcceptEncoding.DEFLATE;
					}
				}
			}
			return AcceptEncoding.IDENTITY;
		}

		@Override
		public long getLength(String encoding) throws IOException {
			if (encoding != null) {
				if (AcceptEncoding.GZIP.equals(encoding)) {
					gzip();
					return gzip.limit();
				}
				if (AcceptEncoding.DEFLATE.equals(encoding)) {
					deflate();
					return deflate.limit();
				}
			}
			return getLength();
		}

		@Override
		public InputStream getData(String encoding) throws IOException {
			if (encoding != null) {
				if (AcceptEncoding.GZIP.equals(encoding)) {
					gzip();
					return new ByteBufferInputStream(gzip);
				}
				if (AcceptEncoding.DEFLATE.equals(encoding)) {
					deflate();
					return new ByteBufferInputStream(deflate);
				}
			}
			return new ByteBufferInputStream(identity());
		}

		@Override
		public InputStream getData(String encoding, ByteRange range) throws IOException {
			if (encoding != null) {
				if (AcceptEncoding.GZIP.equals(encoding)) {
					gzip();
					return new ByteBufferPartInputStream(gzip, range.getStart(), range.getSize());
				}
				if (AcceptEncoding.DEFLATE.equals(encoding)) {
					deflate();
					return new ByteBufferPartInputStream(deflate, range.getStart(), range.getSize());
				}
			}
			return new ByteBufferPartInputStream(identity(), range.getStart(), range.getSize());
		}
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