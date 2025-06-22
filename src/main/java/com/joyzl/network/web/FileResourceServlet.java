/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.web;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTPStatus;

/**
 * 文件资源
 * 
 * @author ZhangXi 2024年11月26日
 */
public class FileResourceServlet extends WEBResourceServlet {

	/** 文件大小阈值，超过此限制的文件无须压缩或缓存 */
	public final static int MAX = 1024 * 1024 * 16;

	/** 资源对象缓存 */
	private final Map<String, WEBResource> resources = new ConcurrentHashMap<>();
	/** 资源基础路径（不含通配符） */
	private final String base;
	/** 主目录 */
	private final File root;
	/** 缓存目录 */
	private final File cache;
	/** 错误页目录 */
	private File error;

	/** 默认文件名 */
	private String[] defaults = new String[] { "index.html", "default.html" };
	/** 压缩的文件扩展名 */
	private String[] compresses = new String[] { ".html", ".htm", ".css", ".js", ".json", ".svg", ".xml" };
	/** 缓存的文件扩展名 */
	private String[] caches = new String[] { ".html", ".htm", ".css", ".js", ".json", ".svg", ".jpg", ".jpeg", ".png", ".gif", ".ttf", ".woff", ".woff2" };
	/** 是否列示目录文件 */
	private boolean browse = false;
	/** 是否使用弱验证 */
	private boolean weak = true;

	public FileResourceServlet(File root) {
		this(null, root, null);
	}

	public FileResourceServlet(String base, String root) {
		this(base, new File(root), null);
	}

	public FileResourceServlet(String base, String root, String cache) {
		this(base, new File(root), new File(cache));
	}

	public FileResourceServlet(String base, File root, File cache) {
		this.base = correctBase(base);
		this.root = root;
		if (cache != null) {
			// 构建缓存目录，如果不存在
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
		WEBResource resource = resources.get(path);
		if (resource == null) {
			final File file = resolveFile(path);
			if (file.exists()) {
				if (file.isDirectory()) {
					// DIR
					if (path.charAt(path.length() - 1) == '/') {
						// 查找默认页面
						final File page = findDefault(file);
						if (page != null) {
							synchronized (this) {
								resource = resources.get(path);
								if (resource == null) {
									resource = makeResource(page);
									resources.put(resource.getContentLocation(), resource);
									resources.put(path, resource);
								}
							}
						} else {
							// 返回目录资源
							// 可用于重定向或返回目录列表
							resource = new DirectoryResource(path, file, isBrowse());
						}
					} else {
						// 返回目录资源
						// 可用于重定向或返回目录列表
						resource = new DirectoryResource(path + '/', file, isBrowse());
					}
				} else {
					// FILE
					synchronized (this) {
						resource = resources.get(path);
						if (resource == null) {
							resource = makeResource(file);
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

	@Override
	protected WEBResource find(HTTPStatus status) {
		// 错误页面不进行压缩和缓存
		// 这不是应当经常出现的请求
		if (error != null) {
			// 查找指定目录
			final File file = new File(error, status.code() + ".html");
			if (file.exists()) {
				return new FileResource(resolvePath(file), file, false);
			}
		}
		// 查找根目录
		final File file = new File(root, status.code() + ".html");
		if (file.exists()) {
			return new FileResource(resolvePath(file), file, false);
		}
		return null;
	}

	/**
	 * 构建文件为资源对象，资源对象提供WEB所需的标头和内容
	 */
	protected WEBResource makeResource(File file) {
		// 不缓存 不压缩
		// 不缓存 要压缩
		// 要缓存 不压缩
		// 要缓存 要压缩
		if (canCache(file)) {
			if (canCompress(file)) {
				return new FileCacheCompressResource(resolvePath(file), file, isWeak());
			} else {
				return new FileCacheResource(resolvePath(file), file, isWeak());
			}
		} else {
			if (canCompress(file)) {
				return new FileCompressResource(resolvePath(file), file, cache, isWeak());
			} else {
				return new FileResource(resolvePath(file), file, isWeak());
			}
		}
	}

	/**
	 * 修正并移除基础路径通配符
	 */
	protected String correctBase(String base) {
		if (base != null) {
			if (base.length() == 0 || "*".equals(base) || "/".equals(base) || "/*".equals(base)) {
				return null;
			}
			if (base.charAt(0) == '*') {
				return null;
			}
			if (base.charAt(base.length() - 1) == '*') {
				return base.substring(0, base.length() - 1);
			}
			final int star = base.indexOf('*');
			if (star > 0) {
				return base.substring(star);
			}
		}
		return base;
	}

	/**
	 * 请求路径转换为实际文件（或目录）；<br>
	 * 调用此方法之前意味着path已经与含有通配符的base匹配，此方法不会重复检查path是否匹配base。
	 * 
	 * @param root 资源根目录
	 * @param path 请求的完整路径
	 * @param base 请求的资源路径前缀
	 */
	protected File resolveFile(String path) {
		// "/" *
		// URL "http://192.168.0.1"
		// - URI "/"
		// - PTH "/"
		// URL "http://192.168.0.1/content/main.html"
		// - URI /content/main.html
		// - PTH /content/main.html
		// URL "http://192.168.0.1/eno"
		// - URI "/eno/index.html"
		// - PTH "/eno/index.html"

		if (base == null || base.length() == 0) {
			if (path.length() > 1) {
				return new File(root, path);
			} else {
				return root;
			}
		}

		// "/xx" *
		// "/xx/" *
		// URL "http://192.168.0.1/xx/"
		// - URI "/xx/"
		// - PTH "/"
		// URL "http://192.168.0.1/xx/content/main.html"
		// - URI "/xx/content/main.html"
		// - PTH "/content/main.html"

		// "/.well-known/access.log"

		if (path.length() == base.length()) {
			return root;
		}
		if (path.length() - base.length() == 1) {
			if (path.charAt(path.length() - 1) == '/') {
				return root;
			}
		}
		return new File(root, path.substring(base.length()));

		// * ".png"
		// * "/image.png"
		// 视为无base全path定位

		// "/image/" * ".png"
		// 视为"/image/" *
	}

	/**
	 * 资源文件转换为资源路径<br>
	 * 调用此方法意味着已经在资源根目录定位到文件，此方法不会检查file是否位于root目录中
	 * 
	 * @param file 资源文件
	 * @return 资源路径 URL 的 PATH 部
	 */
	protected String resolvePath(File file) {
		String path = file.getPath().substring(root.getPath().length()).replace('\\', '/');
		if (base == null || base.length() == 0) {
			return path;
		}
		if (base.charAt(base.length() - 1) == '/') {
			return base + path;
		}
		return base + '/' + path;
	}

	/**
	 * 查找目录下的默认页面文件
	 */
	protected File findDefault(File path) {
		File file;
		for (int index = 0; index < defaults.length; index++) {
			file = new File(path, defaults[index]);
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
	protected boolean canCompress(File file) {
		if (file.length() < MAX) {
			for (int index = 0; index < compresses.length; index++) {
				if (Utility.ends(file.getPath(), compresses[index], true)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 检查文件是否应在内存缓存；
	 */
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
	 * 获取用于缓存的文件；未指定缓存目录则创建临时文件；
	 * 有指定缓存目录则在指定目录生成缓存文件，缓存文件是对源文件经过压缩后的文件，无须压缩的文件也无须缓存。
	 */
	protected File cacheFile(File file, String extension) throws IOException {
		// Linux文件名的长度限制是255个字符
		// windows文件名必须少于260个字符
		return File.createTempFile(file.getName(), extension, cache);
	}

	/**
	 * 获取文件资源主目录
	 */
	public File getRoot() {
		return root;
	}

	/**
	 * 获取已压缩文件资源临时目录
	 */
	public File getCache() {
		return cache;
	}

	/**
	 * 获取默认文件名，当访问网站地址未指定文件名时按默认文件名顺序查询默认文件资源
	 */
	public String[] getDefaults() {
		return defaults;
	}

	/**
	 * 设置默认文件名，当访问网站地址未指定文件名时按默认文件名顺序查询默认文件资源
	 */
	public void setDefaults(String[] values) {
		if (values == null) {
			defaults = new String[0];
		} else {
			defaults = values;
		}
	}

	/**
	 * @see #setDefaults(String[])
	 */
	public void setDefaults(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			defaults = new String[0];
		} else {
			defaults = new String[values.size()];
			int index = 0;
			for (String value : values) {
				defaults[index++] = value;
			}
		}
	}

	/**
	 * 获取应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public String[] getCompresses() {
		return compresses;
	}

	/**
	 * 设置应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public void setCompresses(String[] values) {
		if (values == null) {
			compresses = new String[0];
		} else {
			compresses = values;
		}
	}

	/**
	 * @see #setCompresses(String[])
	 */
	public void setCompresses(Collection<String> values) {
		if (values == null || values.isEmpty()) {
			compresses = new String[0];
		} else {
			compresses = new String[values.size()];
			int index = 0;
			for (String value : values) {
				compresses[index++] = value;
			}
		}
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

	/**
	 * 获取错误页面所在目录
	 */
	public File getErrorPages() {
		return error;
	}

	/**
	 * 设置错误页面所在目录，其中文件按 404.html 匹配
	 */
	public void setErrorPages(String value) {
		if (value == null || value.isEmpty() || value.isBlank()) {
			error = null;
		} else {
			error = new File(value);
		}
	}

	/**
	 * 设置错误页面所在目录，其中文件按 404.html 匹配
	 */
	public void setErrorPages(File value) {
		error = value;
	}

	/**
	 * 获取是否可列出目录中的文件
	 */
	public boolean isBrowse() {
		return browse;
	}

	/**
	 * 设置是否可列出目录中的文件
	 */
	public void setBrowse(boolean value) {
		browse = value;
	}

	/**
	 * 获取是否使用弱验证器
	 */
	public boolean isWeak() {
		return weak;
	}

	/**
	 * 设置是否使用弱验证器
	 */
	public void setWeak(boolean value) {
		weak = value;
	}
}