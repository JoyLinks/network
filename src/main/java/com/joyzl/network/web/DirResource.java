package com.joyzl.network.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 目录资源
 * 
 * @author ZhangXi 2024年11月21日
 */
public class DirResource extends WEBResource {

	/** URI */
	private String contentLocation;

	private final File dir;
	private final boolean browse;
	private DataBuffer buffer;

	public DirResource(File root, File dir, boolean browse) {
		this.dir = dir;
		this.browse = browse;
		contentLocation = uri(root, dir);
	}

	String uri(File root, File file) {
		String uri = file.getPath().substring(root.getPath().length()).replace('\\', '/');
		if (uri.endsWith("/")) {
			return uri;
		}
		return uri + '/';
	}

	@Override
	public String getContentType() {
		if (browse) {
			return MIMEType.TEXT_HTML;
		}
		return null;
	}

	@Override
	public String getContentLanguage() {
		return null;
	}

	@Override
	public String getContentLocation() {
		return contentLocation;
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
		return AcceptEncoding.IDENTITY;
	}

	@Override
	public long getLength(String encoding) throws IOException {
		if (browse) {
			return list();
		}
		return 0;
	}

	@Override
	public InputStream getData(String encoding) throws IOException {
		if (browse) {
			list();
			return new DataBufferInput(buffer, true);
		}
		return null;
	}

	@Override
	public InputStream getData(String encoding, ByteRange range) throws IOException {
		if (browse) {
			list();
			return new DataBufferInput(buffer, true);
		}
		return null;
	}

	public File getDirectory() {
		return dir;
	}

	int list() {
		if (buffer == null) {
			final String[] items = dir.list();
			if (items != null) {
				buffer = DataBuffer.instance();
				try {
					buffer.writeASCIIs("<html>");
					buffer.writeASCIIs("<body>");
					buffer.writeASCIIs("<ul>");
					for (int index = 0; index < items.length; index++) {
						buffer.writeASCIIs("<li>");
						buffer.writeChars(items[index]);
						buffer.writeASCIIs("</li>");
					}
					buffer.writeASCIIs("</ul>");
					buffer.writeASCIIs("</body>");
					buffer.writeASCIIs("</html>");
				} catch (IOException e) {
					e.printStackTrace();
					buffer.release();
					buffer = null;
				}
			}
		}
		return buffer == null ? 0 : buffer.readable();
	}
}