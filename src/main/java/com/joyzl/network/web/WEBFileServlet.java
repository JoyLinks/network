package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import com.joyzl.network.Utility;

/**
 * WEB 文件资源服务
 * 
 * @author ZhangXi 2023年9月15日
 */
public class WEBFileServlet extends FileServlet {

	/** 主目录 */
	private final String path;
	/** 缓存目录 */
	private final String cache;

	/** 默认文件名 */
	private String[] DEFAULTS = new String[] { "default.html", "index.html" };
	/** 建议压缩的文件扩展名 */
	private String[] EXTENSIONS = new String[] { ".html", ".htm", ".css", ".js", ".json", ".xml", ".svg" };

	public WEBFileServlet(String path) {
		this(path, null);
	}

	public WEBFileServlet(String path, String cache) {
		this.path = path;
		if (Utility.isEmpty(cache)) {
			cache = path + File.separatorChar + "caches";
		}
		this.cache = cache;

		final File p = new File(path);
		if (p.exists()) {
			final File c = new File(cache);
			if (!c.exists()) {
				c.mkdirs();
			}
		}
	}

	@Override
	protected File find(String uri) {
		if (uri == null || uri.length() == 1 && uri.equals("/")) {
			// URL "http://192.168.0.1"
			// URI "/"
			// 查找默认页面
			File file;
			for (int index = 0; index < DEFAULTS.length; index++) {
				file = new File(path, DEFAULTS[index]);
				if (file.exists()) {
					return file;
				}
			}
			return null;
		}
		// URL http://192.168.0.1/content/main.html
		// URI /content/main.html
		// DIR d:\\scm-server\content
		// FILE d:\\scm-server\content\main.html
		return new File(path, uri);
	}

	@Override
	protected boolean canCompress(File file) {
		for (int index = 0; index < EXTENSIONS.length; index++) {
			if (Utility.ends(file.getPath(), EXTENSIONS[index], true)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取用于缓存的文件<br>
	 * 
	 * "/usr/local/scm-server/web/mobile/default.html"<br>
	 * "[cache]/mobile_default.html.gzp"<br>
	 * 
	 * "D:\joylink\scm-server\web\mobile\default.html"<br>
	 * "[cache]\mobile_default.html.dft"<br>
	 * 
	 * @param file 源文件
	 * @param extension 缓存文件扩展名
	 * @return [cache][filename][extension]
	 */
	protected File cacheFile(File file, String extension) {
		if (file.getPath().length() - path.length() > 250) {
			// 生成的文件名超长，无法生成压缩文件
			// Linux文件名的长度限制是255个字符
			// windows文件名必须少于260个字符
			return null;
		}
		String name = file.getPath().substring(path.length() + 1);
		name = name.replace('/', '_');
		name = name.replace('\\', '_');
		return new File(cache + File.separatorChar + name + extension);
	}

	@Override
	protected File br(File source) throws IOException {
		return null;
	}

	@Override
	protected File gzip(File source) throws IOException {
		final File target = cacheFile(source, ".gzp");
		if (target == null) {
			return null;
		}
		if (target.exists()) {
			if (source.lastModified() < target.lastModified()) {
				return target;
			}
		}
		try (FileInputStream input = new FileInputStream(source);
			GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(target, false))) {
			while (input.available() > 0) {
				output.write(input.read());
			}
			output.flush();
			output.finish();
		}
		return target;
	}

	@Override
	protected File deflate(File source) throws IOException {
		final File target = cacheFile(source, ".dft");
		if (target == null) {
			return null;
		}
		if (target.exists()) {
			if (source.lastModified() < target.lastModified()) {
				return target;
			}
		}
		try (FileInputStream input = new FileInputStream(source);
			DeflaterOutputStream output = new DeflaterOutputStream(new FileOutputStream(target, false))) {
			while (input.available() > 0) {
				output.write(input.read());
			}
			output.flush();
			output.finish();
		}
		return target;
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
	public void setDefaults(String[] dEFAULTS) {
		DEFAULTS = dEFAULTS;
	}

	/**
	 * 获取应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public String[] getExtensions() {
		return EXTENSIONS;
	}

	/**
	 * 设置应压缩的文件扩展名，当浏览器支持内容压缩时，这些扩展名的文件将被压缩以减少字节数量
	 */
	public void setExtensions(String[] eXTENSIONS) {
		EXTENSIONS = eXTENSIONS;
	}
}