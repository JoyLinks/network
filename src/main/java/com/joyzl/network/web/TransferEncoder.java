package com.joyzl.network.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.buffer.DataBufferInput;
import com.joyzl.network.buffer.DataBufferOutput;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.http.TransferEncoding;

/**
 * Transfer-Encoding: chunked
 * 
 * @author ZhangXi 2023年9月8日
 */
public class TransferEncoder extends WEBContentCoder {

	public static boolean write(HTTPWriter writer, WEBResponse response, String transferEncoding) throws IOException {
		if (TransferEncoding.CHUNKED.equalsIgnoreCase(transferEncoding)) {
			return writeChunked(writer, response);
		} else //
		if (TransferEncoding.COMPRESS.equalsIgnoreCase(transferEncoding)) {
			return writeCompress(writer, response);
		} else //
		if (TransferEncoding.DEFLATE.equalsIgnoreCase(transferEncoding)) {
			return writeDeflate(writer, response);
		} else //
		if (TransferEncoding.GZIP.equalsIgnoreCase(transferEncoding)) {
			return writeGZIP(writer, response);
		} else //
		if (TransferEncoding.IDENTITY.equalsIgnoreCase(transferEncoding)) {
			return writeIdentity(writer, response);
		} else {
			throw new IOException("无效值 Transfer-Encoding:" + transferEncoding);
		}
	}

	public static boolean read(HTTPReader reader, WEBResponse response, String transferEncoding) throws IOException {
		if (TransferEncoding.CHUNKED.equalsIgnoreCase(transferEncoding)) {
			return readChunked(reader, response);
		} else //
		if (TransferEncoding.COMPRESS.equalsIgnoreCase(transferEncoding)) {
			return readCompress(reader, response);
		} else //
		if (TransferEncoding.DEFLATE.equalsIgnoreCase(transferEncoding)) {
			return readDeflate(reader, response);
		} else //
		if (TransferEncoding.GZIP.equalsIgnoreCase(transferEncoding)) {
			return readGZIP(reader, response);
		} else //
		if (TransferEncoding.IDENTITY.equalsIgnoreCase(transferEncoding)) {
			return readIdentity(reader, response);
		} else {
			throw new IOException("无效值 Transfer-Encoding:" + transferEncoding);
		}
	}

	/**
	 * 输出分块传输数据
	 * 
	 * @param writer
	 * @param response
	 * @return true 全部完成 / false 单块输出
	 * @throws IOException
	 */
	static boolean writeChunked(HTTPWriter writer, WEBResponse response) throws IOException {
		// 分块的形式进行发送时无Content-Length值
		// 分块长度以十六进制的形式表示，后面紧跟着 '\r\n'
		// 之后是分块本身，后面也是'\r\n'
		// 终止块是一个常规的分块，不同之处在于其长度为0

		final InputStream input = input(response);

		// 当前块长度
		int length = input.available();
		if (length - BLOCK > 1024) {
			length = BLOCK;
		}

		// 长度
		writer.write(Integer.toHexString(length));
		writer.write(HTTPCoder.CRLF);

		// 内容块
		while (length-- > 0) {
			writer.buffer().write(input.read());
		}
		writer.write(HTTPCoder.CRLF);

		// 结束块
		if (input.available() > 0) {
			return false;
		} else {
			writer.write("0");
			writer.write(HTTPCoder.CRLF);
			writer.write(HTTPCoder.CRLF);
			return true;
		}
	}

	/**
	 * 读取分块传输数据
	 * 
	 * @param reader
	 * @param response
	 * @return true 全部完成 / false 数据不足
	 * @throws IOException
	 */
	static boolean readChunked(HTTPReader reader, WEBResponse response) throws IOException {
		reader.mark();
		if (reader.readTo(HTTPCoder.CRLF)) {
			int length = Integer.parseUnsignedInt(reader.sequence(), 0, reader.sequence().length(), 10);
			if (reader.buffer().readable() >= length + 2/* CRLF */) {
				if (length == 0) {
					if (reader.readTo(HTTPCoder.CRLF)) {
						return true;
					} else {
						return false;
					}
				} else {
					DataBuffer buffer = (DataBuffer) response.getContent();
					if (buffer == null) {
						response.setContent(buffer = DataBuffer.instance());
					}
					// reader.buffer().bounds(length);
					// buffer.residue(reader.buffer());
					// reader.buffer().discard();
					reader.buffer().transfer(buffer, length);
					reader.readTo(HTTPCoder.CRLF);
					return false;
				}
			}
		}
		reader.reset();
		return false;
	}

	static boolean writeCompress(HTTPWriter writer, WEBResponse response) {
		// TODO Auto-generated method stub
		return false;
	}

	static boolean readCompress(HTTPReader reader, WEBResponse response) {
		// TODO Auto-generated method stub
		return false;
	}

	static boolean writeDeflate(HTTPWriter writer, WEBResponse response) throws IOException {
		final InputStream input = input(response);
		try (final DeflaterOutputStream output = new DeflaterOutputStream(new DataBufferOutput(writer.buffer()))) {
			while (input.available() > 0) {
				output.write(input.read());
			}
			output.flush();
		}
		return true;
	}

	static boolean readDeflate(HTTPReader reader, WEBResponse response) throws IOException {
		final OutputStream output = output(response);
		try (final DeflaterInputStream input = new DeflaterInputStream(new DataBufferInput(reader.buffer()))) {
			while (input.available() > 0) {
				output.write(input.read());
			}
			output.flush();
		}
		return true;
	}

	static boolean writeGZIP(HTTPWriter writer, WEBResponse response) throws IOException {
		final InputStream input = input(response);
		try (final GZIPOutputStream output = new GZIPOutputStream(new DataBufferOutput(writer.buffer()))) {
			while (input.available() > 0) {
				output.write(input.read());
			}
			output.flush();
		}
		return true;
	}

	static boolean readGZIP(HTTPReader reader, WEBResponse response) throws IOException {
		final OutputStream output = output(response);
		try (final GZIPInputStream input = new GZIPInputStream(new DataBufferInput(reader.buffer()))) {
			while (input.available() > 0) {
				output.write(input.read());
			}
			output.flush();
		}
		return true;
	}

	static boolean writeIdentity(HTTPWriter writer, WEBResponse response) {
		// TODO Auto-generated method stub
		return false;
	}

	static boolean readIdentity(HTTPReader reader, WEBResponse response) {
		// TODO Auto-generated method stub
		return false;
	}
}