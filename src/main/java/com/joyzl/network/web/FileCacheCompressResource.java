package com.joyzl.network.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import com.joyzl.network.http.AcceptEncoding;
import com.joyzl.network.http.Range.ByteRange;

/**
 * 压缩并缓存的文件资源
 * 
 * @author ZhangXi 2024年12月9日
 */
public class FileCacheCompressResource extends FileCacheResource {

	private ByteBuffer deflate, gzip;

	public FileCacheCompressResource(File root, File file, boolean weak) {
		super(root, file, weak);
	}

	void deflate() throws IOException {
		if (deflate == null) {
			synchronized (this) {
				if (deflate == null) {
					final ByteArrayOutputStream buffer = new ByteArrayOutputStream((int) getLength());
					try (FileInputStream input = new FileInputStream(getFile());
						DeflaterOutputStream output = new DeflaterOutputStream(buffer);) {
						input.transferTo(output);
						output.flush();
						output.finish();
					}
					deflate = ByteBuffer.allocateDirect(buffer.size());
					deflate.put(buffer.buffer(), 0, buffer.size());
					deflate.flip();
				}
			}
		}
	}

	void gzip() throws IOException {
		if (gzip == null) {
			synchronized (this) {
				if (gzip == null) {
					final ByteArrayOutputStream buffer = new ByteArrayOutputStream((int) getLength());
					try (FileInputStream input = new FileInputStream(getFile());
						GZIPOutputStream output = new GZIPOutputStream(buffer)) {
						input.transferTo(output);
						output.flush();
						output.finish();
					}
					gzip = ByteBuffer.allocateDirect(buffer.size());
					gzip.put(buffer.buffer(), 0, buffer.size());
					gzip.flip();
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
				return gzip.limit();
			}
			if (AcceptEncoding.DEFLATE.equals(encoding)) {
				deflate();
				return deflate.limit();
			}
		}
		return getLength();
	}

	@Override
	public InputStream getData(String encoding) throws IOException {
		if (encoding != null) {
			if (AcceptEncoding.GZIP.equals(encoding)) {
				gzip();
				return new ByteBufferInputStream(gzip);
			}
			if (AcceptEncoding.DEFLATE.equals(encoding)) {
				deflate();
				return new ByteBufferInputStream(deflate);
			}
		}
		return new ByteBufferInputStream(identity());
	}

	@Override
	public InputStream getData(String encoding, ByteRange range) throws IOException {
		if (encoding != null) {
			if (AcceptEncoding.GZIP.equals(encoding)) {
				gzip();
				return new ByteBufferPartInputStream(gzip, range.getStart(), range.getSize());
			}
			if (AcceptEncoding.DEFLATE.equals(encoding)) {
				deflate();
				return new ByteBufferPartInputStream(deflate, range.getStart(), range.getSize());
			}
		}
		return new ByteBufferPartInputStream(identity(), range.getStart(), range.getSize());
	}
}