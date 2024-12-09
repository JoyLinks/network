package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import com.joyzl.network.Utility;
import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 磁盘缓存的文件资源服务
 * 
 * @author ZhangXi 2023年9月15日
 */
public class DiskFileServlet extends FileResourceServlet {

	/** 缓存目录 */
	private final File cache;
	private final Map<String, WEBResource> resources = new ConcurrentHashMap<>();

	public DiskFileServlet(String path) {
		this(new File(path), null);
	}

	public DiskFileServlet(String root, String cache) {
		this(new File(root), new File(cache));
	}

	public DiskFileServlet(File root, File cache) {
		super(root);
		if (cache != null) {
			if (!cache.exists()) {
				if (!cache.mkdirs()) {
					cache = null;
				}
			}
		}
		this.cache = cache;
	}

	@Override
	protected WEBResource find(String path) {
		// URL "http://192.168.0.1" URI "/"
		// URL http://192.168.0.1/content/main.html URI /content/main.html
		// URL http://192.168.0.1/eno URI /eno/index.html

		WEBResource resource = resources.get(path);
		if (resource == null) {
			File file = new File(getRoot(), path);
			if (file.exists()) {
				if (file.isDirectory()) {
					if (path.charAt(path.length()) == '/') {
						// 查找默认页面
						final File page = findDefault(file);
						if (page != null) {
							synchronized (this) {
								resource = resources.get(path);
								if (resource == null) {
									if (canCompress(page)) {
										resource = new CompressResource(getRoot(), page);
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
							} else {
								resource = new FileResource(getRoot(), file, isWeak());
							}
							resources.put(resource.getContentLocation(), resource);
							// resources.put(uri, resource);
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

	/**
	 * 获取用于缓存的文件；未指定缓存目录则创建临时文件；
	 * 有指定缓存目录则在指定目录生成缓存文件，缓存文件是对源文件经过压缩后的文件，无须压缩的文件也无须缓存。
	 */
	File cacheFile(File file, String extension) throws IOException {
		if (cache == null) {
			return File.createTempFile(file.getName(), extension);
		} else {
			// Linux文件名的长度限制是255个字符
			// windows文件名必须少于260个字符
			final long name = Utility.toLong(file.getPath(), getRoot().getPath().length(), file.getPath().length() - getRoot().getPath().length());
			return new File(cache, Long.toString(name, Character.MAX_RADIX) + extension);
		}
	}

	class CompressResource extends FileResource {

		private File gzip, deflate;
		private long gzipLength, deflateLength;

		public CompressResource(File root, File file) {
			super(root, file, isWeak());
		}

		void deflate() throws IOException {
			if (deflate == null) {
				synchronized (this) {
					if (deflate == null) {
						deflate = cacheFile(getFile(), ".dft");
						try (FileInputStream input = new FileInputStream(getFile());
							DeflaterOutputStream output = new DeflaterOutputStream(new FileOutputStream(deflate))) {
							input.transferTo(output);
							output.flush();
							output.finish();
						}
						deflateLength = deflate.length();
					}
				}
			}
		}

		void gzip() throws IOException {
			if (gzip == null) {
				synchronized (this) {
					if (gzip == null) {
						gzip = cacheFile(getFile(), ".gzp");
						try (FileInputStream input = new FileInputStream(getFile());
							GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(gzip))) {
							input.transferTo(output);
							output.flush();
							output.finish();
						}
						gzipLength = gzip.length();
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
					return gzipLength;
				}
				if (AcceptEncoding.DEFLATE.equals(encoding)) {
					deflate();
					return deflateLength;
				}
			}
			return getLength();
		}

		@Override
		public InputStream getData(String encoding) throws IOException {
			if (encoding != null) {
				if (AcceptEncoding.GZIP.equals(encoding)) {
					gzip();
					return new FileInputStream(gzip);
				}
				if (AcceptEncoding.DEFLATE.equals(encoding)) {
					deflate();
					return new FileInputStream(deflate);
				}
			}
			return new FileInputStream(getFile());
		}

		@Override
		public InputStream getData(String encoding, ByteRange range) throws IOException {
			if (encoding != null) {
				if (AcceptEncoding.GZIP.equals(encoding)) {
					gzip();
					return new FilePartInputStream(gzip, range.getStart(), range.getSize());
				}
				if (AcceptEncoding.DEFLATE.equals(encoding)) {
					deflate();
					return new FilePartInputStream(deflate, range.getStart(), range.getSize());
				}
			}
			return new FilePartInputStream(getFile(), range.getStart(), range.getSize());
		}
	}
}