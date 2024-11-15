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
public class DiskFileServlet extends WEBResourceServlet {

	/** 资源目录 */
	private final File root;
	/** 缓存目录 */
	private final File cache;

	/** 默认文件名 */
	private String[] DEFAULTS = new String[] { "default.html", "index.html" };
	/** 压缩的文件扩展名 */
	private String[] COMPRESSES = new String[] { ".html", ".htm", ".css", ".js", ".json", ".xml", ".svg" };
	/** 资源对象缓存 */
	private final Map<String, WEBResource> resources = new ConcurrentHashMap<>();

	public DiskFileServlet(String path) {
		this(new File(path), null);
	}

	public DiskFileServlet(String root, String cache) {
		this(new File(root), new File(cache));
	}

	public DiskFileServlet(File root, File cache) {
		this.root = root;
		this.cache = cache;
	}

	@Override
	protected WEBResource find(String uri) {
		// URL "http://192.168.0.1" URI "/"
		// URL http://192.168.0.1/content/main.html URI /content/main.html

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
						synchronized (this) {
							resource = resources.get(uri);
							if (resource == null) {
								if (canCompress(file)) {
									resource = new CompressResource(file);
								} else {
									resource = new FileResource(file);
								}
								resources.put(uri + file.getName(), resource);
								resources.put(uri, resource);
							}
						}
					}
				} else {
					synchronized (this) {
						resource = resources.get(uri);
						if (resource == null) {
							if (canCompress(file)) {
								resource = new CompressResource(file);
							} else {
								resource = new FileResource(file);
							}
							resources.put(uri, resource);
						}
					}
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

	/**
	 * 检查文件是否应压缩；<br>
	 * jpg和png图像文件本身已经过压缩，再次压缩已难以缩小，因此无须再压缩；<br>
	 * 音频和视频这类太大的文件也没有必要压缩，这会占用过多磁盘空间和运算资源，客户端应分块获取；<br>
	 * zip等已经压缩的文件当然也没有必要再次压缩。
	 */
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
			final long name = Utility.toLong(file.getPath(), root.getPath().length(), file.getPath().length() - root.getPath().length());
			return new File(cache, Long.toString(name, Character.MAX_RADIX) + extension);
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

	class CompressResource extends FileResource {

		private File gzip, deflate;
		private long gzipLength, deflateLength;

		public CompressResource(File file) {
			super(file);
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