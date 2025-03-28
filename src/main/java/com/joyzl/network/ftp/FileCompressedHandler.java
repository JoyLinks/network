package com.joyzl.network.ftp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;

/**
 * 文件压缩传输
 * 
 * @author ZhangXi 2024年7月13日
 */
public class FileCompressedHandler extends FileHandler {

	final static byte ASCII_SPACE = 32;
	final static byte EBCDIC_SPACE = 64;
	final static byte IMAGE_SPACE = 0;

	/*-
	 * 常规数据
	 * +------+------+
	 * | HEAD | DATA |
	 * +------+------+
	 * HEAD:	1Byte 最高位0，其余位表示字节数n，最大127
	 *  7             0
	 * +-+-+-+-+-+-+-+-+
	 * |0|       n     |
	 * +-+-+-+-+-+-+-+-+
	 * 
	 * 压缩数据
	 * 重复数据压缩 2Byte
	 * 2       6               8
	 * +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+
	 * |1 0|     n     | |       d       |
	 * +-+-+-+-+-+-+-+-+ +-+-+-+-+-+-+-+-+
	 * 
	 * 填充数据压缩 1Byte
	 * 2       6
	 * +-+-+-+-+-+-+-+-+
	 * |1 1|     n     |
	 * +-+-+-+-+-+-+-+-+
	 * ASCII <SP> 32
	 * EBCDIC <SP> 64
	 * IMAGE 0
	 * 
	 * 转义序列
	 * +---+-------+
	 * | 0 | BLOCK |
	 * +---+-------+
	 * BLOCK:	块传输报文
	 */

	final static FileCompressedHandler INSTANCE = new FileCompressedHandler();

	// decode() received() 用于接收文件数据

	@Override
	public FileMessage decode(ChainChannel chain, DataBuffer reader) throws Exception {
		final FileClient client = (FileClient) chain;
		final FileMessage message = client.getCommand();
		byte tag;
		while (reader.readable() > 0) {
			reader.mark();
			tag = reader.readByte();
			if (tag > 0 && tag <= 127) {
				// 常规数据
				if (reader.readable() >= tag) {
					reader.transfer(client.getChannel(), tag);
					message.setTransferred(message.getTransferred() + tag);
				} else {
					reader.reset();
					return null;
				}
			} else //
			if ((tag >>> 6) == 2) {
				// 压缩的重复数据
				if (reader.readable() > 0) {
					tag &= 0b00111111;
					final byte value = reader.readByte();
					writeBytes(client.getChannel(), value, tag);
					message.setTransferred(message.getTransferred() + tag);
				} else {
					reader.reset();
					return null;
				}
			} else //
			if ((tag >>> 6) == 3) {
				// 压缩的填充数据
				tag &= 0b00111111;
				writeBytes(client.getChannel(), IMAGE_SPACE, tag);
				message.setTransferred(message.getTransferred() + tag);
			}
			reader.erase();
		}
		return message;
	}

	@Override
	protected void received(ChainChannel chain, FileMessage message) throws Exception {
		final FileClient client = (FileClient) chain;
		if (message == null) {
			message = client.getCommand();
			message.setCode(999);
			message.finish();
		} else {
			chain.receive();
		}
	}

	// encode() sent() 用于发送文件数据

	@Override
	protected DataBuffer encode(ChainChannel chain, FileMessage message) throws Exception {
		final FileClient client = (FileClient) chain;
		final FileChannel channel = client.getChannel();

		final DataBuffer buffer = DataBuffer.instance();
		long length = channel.size() - channel.position();
		if (length > BUFFER_SIZE) {
			length = BUFFER_SIZE;

			int l;
			while (length > 0) {
				l = channel.read(buffer.write());
				buffer.written(l);
				length -= l;
				message.setTransferred(message.getTransferred() + l);
			}
		} else {
			int l;
			while (length > 0) {
				l = channel.read(buffer.write());
				buffer.written(l);
				length -= l;
				message.setTransferred(message.getTransferred() + l);
			}
			// CLOSE
			client.closeChannel();
		}
		return buffer;
	}

	@Override
	protected void sent(ChainChannel chain, FileMessage message) throws Exception {
		final FileClient client = (FileClient) chain;
		if (message == null) {
			message = client.getCommand();
			message.setCode(999);
			message.finish();
		} else {
			if (client.getChannel() == null) {
				client.close();
			} else {
				client.send(client.getCommand());
			}
		}
	}

	int writeBytes(FileChannel channel, byte value, int length) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocate(length);
		while (length-- > 0) {
			buffer.put(value);
		}
		buffer.flip();
		return channel.write(buffer);
	}
}