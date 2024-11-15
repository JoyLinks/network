package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
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
public class RAMFileServlet extends WEBResourceServlet {

	/** 主目录 */
	private final File root;

	/** 默认文件名 */
	private String[] DEFAULTS = new String[] { "default.html", "index.html" };
	/** 压缩的文件扩展名 */
	private String[] COMPRESSES = new String[] { ".html", ".htm", ".css", ".js", ".json", ".xml", ".svg" };
	/** 缓存的文件扩展名 */
	private String[] CACHES = new String[] { ".jpg", ".jpeg", ".png", ".gif" };
	/** 资源对象缓存 */
	private final Map<String, WEBResource> resources = new HashMap<>();

	public RAMFileServlet(String root) {
		this(new File(root));
	}

	public RAMFileServlet(File root) {
		this.root = root;
	}

	@Override
	protected WEBResource find(String uri) {
		if (uri == null || uri.length() == 0) {
			uri = "/";
		}
		WEBResource resource = resources.get(uri);
		if (resource == null) {
			File file = new File(root, uri);
			if (file.exists()) {
				if (file.isDirectory()) {
					// 查找默认页面
					file = findDefault(file);
					if (file != null) {
						if (canCompress(file)) {
							resource = new CompressResource(file);
						} else if (canCache(file)) {
							resource = new CacheResource(file);
						} else {
							resource = new FileResource(file);
						}
						resources.put(uri + file.getName(), resource);
						resources.put(uri, resource);
					}
				} else {
					if (canCompress(file)) {
						resource = new CompressResource(file);
					} else if (canCache(file)) {
						resource = new CacheResource(file);
					} else {
						resource = new FileResource(file);
					}
					resources.put(uri, resource);
				}
			}
		}
		return resource;
	}

	File findDefault(File path) {
		File file;
		for (int index = 0; index < DEFAULTS.length; index++) {
			file = new File(path, DEFAULTS[index]);
			if (file.exists() && file.isFile()) {
				return file;
			}
		}
		return null;
	}

	boolean canCache(File file) {
		if (file.length() < WEBContentCoder.MAX) {
			for (int index = 0; index < CACHES.length; index++) {
				if (Utility.ends(file.getPath(), CACHES[index], true)) {
					return true;
				}
			}
		}
		return false;
	}

	boolean canCompress(File file) {
		if (file.length() < WEBContentCoder.MAX) {
			for (int index = 0; index < COMPRESSES.length; index++) {
				if (Utility.ends(file.getPath(), COMPRESSES[index], true)) {
					return true;
				}
			}
		}
		return false;
	}

	class CacheResource extends FileResource {

		private ByteBuffer identity;

		public CacheResource(File file) {
			super(file);
		}

		ByteBuffer identity() throws IOException {
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
			return identity;
		}

		@Override
		public InputStream getData(String encoding) throws IOException {
			return new ByteBufferInputStream(identity());
		}

		@Override
		public InputStream getData(String encoding, ByteRange range) throws IOException {
			return new ByteBufferInputStream(identity(), range.getStart(), range.getSize());
		}
	}

	class CompressResource extends CacheResource {

		private ByteBuffer deflate, gzip;

		public CompressResource(File file) {
			super(file);
		}

		void deflate() throws IOException {
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

		void gzip() throws IOException {
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
					return new ByteBufferInputStream(gzip, range.getStart(), range.getSize());
				}
				if (AcceptEncoding.DEFLATE.equals(encoding)) {
					deflate();
					return new ByteBufferInputStream(deflate, range.getStart(), range.getSize());
				}
			}
			return new ByteBufferInputStream(identity(), range.getStart(), range.getSize());
		}
	}

	/**
	 * 获取默认文件名，当访问网站地址未指定文件名时按默认文件名顺序查询默认文件资源
	 */
	public String[] getDefaults() {
		return DEFAULTS;
	}

	/**
	 * 设置默认文件名，当访问网站地址未指定文件名时按默认文件名顺序查询默认文件资源
	 */
	public void setDefaults(String[] values) {
		if (values == null) {
			DEFAULTS = new String[0];
		} else {
			DEFAULTS = values;
		}
	}

	/**
	 * 获取应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public String[] getCompresses() {
		return COMPRESSES;
	}

	/**
	 * 设置应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public void setCompresses(String[] values) {
		if (values == null) {
			COMPRESSES = new String[0];
		} else {
			COMPRESSES = values;
		}
	}

	/**
	 * 获取应缓存的文件扩展名
	 */
	public String[] getCaches() {
		return CACHES;
	}

	/**
	 * 设置应缓存的文件扩展名
	 */
	public void setCaches(String[] values) {
		if (values == null) {
			CACHES = new String[0];
		} else {
			CACHES = values;
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

		ByteBufferInputStream(ByteBuffer buffer, long offset, long length) {
			this.buffer = buffer;
		}

		@Override
		public int read() {
			if (index < buffer.limit()) {
				return buffer.get(index++);
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