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

	/** URI */
	private String contentLocation;
	/** "Wed, 21 Oct 2015 07:28:00 GMT" */
	private String lastModified;
	/** zh */
	private String contentLanguage;
	/** MIME Type */
	private String contentType;
	/** W/KIJNHYGFRE/ */
	private String eTag;

	private final File file;
	private long modified;
	private long length;

	public FileResource(File root, File file, boolean weak) {
		this.file = file;
		length = file.length();
		modified = file.lastModified();

		contentLocation = uri(root, file);
		lastModified = Date.toText(modified);

		// index.html.en
		// index.en.html
		String extension, name = file.getName();
		int index = name.lastIndexOf('.');
		if (index > 0) {
			int end = name.length();
			do {
				extension = name.substring(index + 1, end);
				if (contentType == null) {
					contentType = MIMEType.getByExtension(extension);
				}
				if (contentLanguage == null) {
					contentLanguage = LanguageCodes.SEEKER.take(extension);
				}
				index = name.lastIndexOf('.', end = index - 1);
			} while (index > 0);
		} else {
			contentType = MIMEType.APPLICATION_OCTET_STREAM;
		}

		if (weak) {
			eTag = ETag.makeWeak(length, modified);
		} else {
			eTag = ETag.makeStorng(file);
		}
	}

	String uri(File root, File file) {
		return file.getPath().substring(root.getPath().length()).replace('\\', '/');
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
	public String getContentLocation() {
		return contentLocation;
	}

	@Override
	public String getContentLanguage() {
		return contentLanguage;
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