package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 压缩的文件资源
 * 
 * @author ZhangXi 2024年12月9日
 */
public class FileCompressResource extends FileResource {

	private final File cache;
	private File gzip, deflate;
	private long gzipLength, deflateLength;

	public FileCompressResource(String path, File file, File cache, boolean weak) {
		super(path, file, weak);
		this.cache = cache;
	}

	void deflate() throws IOException {
		if (deflate == null) {
			synchronized (this) {
				if (deflate == null) {
					deflate = File.createTempFile(getFile().getName(), ".dft", cache);
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
					gzip = File.createTempFile(getFile().getName(), ".gzp", cache);
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
		if (acceptEncoding != null && getLength() > 1024) {
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