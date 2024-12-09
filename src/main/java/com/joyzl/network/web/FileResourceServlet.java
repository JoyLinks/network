package com.joyzl.network.web;

import java.io.File;
import java.util.Collection;

import com.joyzl.network.Utility;
import com.joyzl.network.http.HTTPStatus;

/**
 * 文件资源
 * 
 * @author ZhangXi 2024年11月26日
 */
public abstract class FileResourceServlet extends WEBResourceServlet {

	/** 文件大小阈值，超过此限制的文件无须压缩或缓存 */
	public final static int MAX = 1024 * 1024 * 16;

	/** 主目录 */
	private final File root;
	/** 错误目录 */
	private File error;

	/** 默认文件名 */
	private String[] defaults = new String[] { "default.html", "index.html" };
	/** 压缩的文件扩展名 */
	private String[] compresses = new String[] { ".html", ".htm", ".css", ".js", ".json", ".xml", ".svg" };
	/** 是否列示目录文件 */
	private boolean browse = false;
	/** 是否使用弱验证 */
	private boolean weak = true;

	public FileResourceServlet(String root) {
		this(new File(root));
	}

	public FileResourceServlet(File root) {
		this.root = root;
	}

	@Override
	protected WEBResource find(HTTPStatus status) {
		if (error != null) {
			File file = new File(error, status.code() + ".html");
			if (file.exists()) {
				return new FileResource(root, file, false);
			}
		}
		return null;
	}

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
	 * 获取文件资源主目录
	 */
	public File getRoot() {
		return root;
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
	 * 获取错误页面所在目录
	 */
	public File getErrorPages() {
		return error;
	}

	/**
	 * 设置错误页面所在目录，其中文件按 404.html 匹配
	 */
	public void setErrorPages(String value) {
		if (value == null) {
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