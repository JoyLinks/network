package com.joyzl.network.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 文件匹配 Multiple Choices
 * 
 * @author ZhangXi 2024年11月25日
 */
public final class FileMultiple extends WEBResource {

	public static WEBResource find(File path) {
		// /a3-test/fairlane
		// /a3-test/fairlane.gif
		// /a3-test/fairlane.jpg
		// /a3-test/fairlane.png

		// Alternates 未得到广泛应用
		// Alternates: {"index.html" 1.0 {type text/html} {language en}},{}

		// Accept: image/*; q=1.0
		// Accept-Encoding: compress; q=0.0, gzip; q=0.0, deflate; q=0.5
		// TODO 根据Accept/Accept-Encoding优选资源

		final String name = path.getName();
		if (name != null && name.length() > 0) {
			final StringBuilder builder = new StringBuilder();
			final String[] files = path.getParentFile().list();
			String type;
			if (files != null && files.length > 0) {
				for (int index = 0; index < files.length; index++) {
					if (files[index].startsWith(name)) {
						type = MIMEType.getByFilename(files[index]);
						if (type != null) {
							if (builder.length() > 0) {
								builder.append(',');
							}
							builder.append('{');
							builder.append(files[index]);
							builder.append(' ');
							builder.append('{');
							builder.append(type);
							builder.append('}');
							builder.append('}');
						}
					}
				}
			}
			if (builder.length() > 0) {
				return new FileMultiple(builder.toString());
			}
		}
		return null;
	}

	private String contentType;

	FileMultiple(String types) {
		contentType = types;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getContentLanguage() {
		return null;
	}

	@Override
	public String getContentLocation() {
		return null;
	}

	@Override
	public String getLastModified() {
		return null;
	}

	@Override
	public String getETag() {
		return null;
	}

	@Override
	public String fitEncoding(AcceptEncoding acceptEncoding) {
		return null;
	}

	@Override
	public long getLength(String encoding) throws IOException {
		return 0;
	}

	@Override
	public InputStream getData(String encoding) throws IOException {
		return null;
	}

	@Override
	public InputStream getData(String encoding, ByteRange range) throws IOException {
		return null;
	}
}