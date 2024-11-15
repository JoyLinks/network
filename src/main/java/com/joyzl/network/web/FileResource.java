package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Date;
import com.joyzl.network.http.ETag;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 文件资源
 * 
 * @author ZhangXi 2024年11月14日
 */
public class FileResource extends WEBResource {

	/** "Wed, 21 Oct 2015 07:28:00 GMT" */
	private String lastModified;
	/** MIME Type */
	private String contentType;
	/** W/KIJNHYGFRE/ */
	private String eTag;

	private final File file;
	private long modified;
	private long length;

	public FileResource(File file) {
		this.file = file;
		length = file.length();
		modified = file.lastModified();

		lastModified = Date.toText(modified);
		contentType = MIMEType.getMIMEType(file);
		eTag = ETag.makeWTag(length, modified);
	}

	@Override
	public String fitEncoding(AcceptEncoding acceptEncoding) {
		return AcceptEncoding.IDENTITY;
	}

	@Override
	public long getLength(String encoding) throws IOException {
		return length;
	}

	@Override
	public InputStream getData(String encoding) throws IOException {
		return new FileInputStream(getFile());
	}

	@Override
	public InputStream getData(String encoding, ByteRange byterange) throws IOException {
		return new FilePartInputStream(getFile(), byterange.getStart(), byterange.getSize());
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String getLastModified() {
		return lastModified;
	}

	@Override
	public String getETag() {
		return eTag;
	}

	public File getFile() {
		return file;
	}

	public long getLength() {
		return length;
	}

	public long getModified() {
		return modified;
	}
}