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
public class DirectoryResource extends WEBResource {

	/** URI */
	private final String contentLocation;
	private final String contentType;

	private final File dir;
	private DataBuffer buffer;

	public DirectoryResource(String path, File dir, boolean browse) {
		this.dir = dir;
		contentLocation = path;
		if (browse) {
			contentType = MIMEType.TEXT_HTML;
		} else {
			contentType = null;
		}
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
		if (contentType != null) {
			return list();
		}
		return 0;
	}

	@Override
	public InputStream getData(String encoding) throws IOException {
		if (contentType != null) {
			list();
			return new DataBufferInput(buffer, true);
		}
		return null;
	}

	@Override
	public InputStream getData(String encoding, ByteRange range) throws IOException {
		if (contentType != null) {
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