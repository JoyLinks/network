package com.joyzl.network.ftp;

import java.nio.channels.FileChannel;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;

/**
 * 文件块传输
 * 
 * @author ZhangXi 2024年7月13日
 */
public class FileBlockHandler extends FileHandler {

	// 记录 数据块结束是 EOR
	final static byte EOR = (byte) 128;
	// 文件 数据块结束是 EOF
	final static byte EOF = 64;
	// 怀疑数据块有错
	final static byte ERR = 32;
	// 数据块是重开始标志
	final static byte RST = 16;

	/*-
	 * 数据传输
	 * +-----+--------+------+
	 * | TAG | LENGTH | DATA |
	 * +-----+--------+------+
	 * 重开标志
	 * +-----+--------+--------+
	 * | RST | LENGTH | MARKER |
	 * +-----+--------+--------+
	 * TAG:		1Byte EOR/EOF/ERR/RST
	 * LENGTH:	2Byte
	 * MARKER:	ASCII 不含空格
	 */

	/** 每块大小(12=TAG+LENGTH+TAG+LENGTH+MARK) */
	final static int BLOCK_SIZE = BUFFER_SIZE - 12;
	/** 每标记大小 */
	final static int MARKE_SIZE = BLOCK_SIZE * 8;

	// decode() received() 用于接收文件数据

	@Override
	public FileMessage decode(ChainChannel chain, DataBuffer reader) throws Exception {
		if (reader.readable() < 3) {
			return null;
		}

		reader.mark();
		// 检查块是否接收完整
		int tag = reader.readByte();
		int length = reader.readUnsignedShort();
		if (length > reader.readable()) {
			reader.reset();
			return null;
		}

		final FileClient client = (FileClient) chain;
		final FileMessage message = client.getCommand();
		if ((tag & EOR) > 0) {
			reader.transfer(client.getChannel(), length);
			message.setTransferred(message.getTransferred() + length);
			client.closeChannel();
		} else//
		if ((tag & EOF) > 0) {
			reader.transfer(client.getChannel(), length);
			message.setTransferred(message.getTransferred() + length);
			client.closeChannel();
		} else//
		if ((tag & RST) > 0) {
			// TODO 如何保存这个
			reader.readASCIIs(length);
		} else {
			reader.transfer(client.getChannel(), length);
			message.setTransferred(message.getTransferred() + length);

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
			if (client.getChannel() == null) {
				client.close();
			} else {
				chain.receive();
			}
		}
	}

	// encode() sent() 用于发送文件数据

	@Override
	protected DataBuffer encode(ChainChannel chain, FileMessage message) throws Exception {
		final FileClient client = (FileClient) chain;
		final FileChannel channel = client.getChannel();
		final DataBuffer buffer = DataBuffer.instance();
		// 1G(1024M)/8M=128 MARKE
		long length = channel.size() - channel.position();
		if (length > 0) {
			if (length > BLOCK_SIZE) {
				length = BLOCK_SIZE;
				// TAG 1B
				buffer.writeByte(0);
				// LENGTH 2B
				buffer.writeShort((int) length);
				// DATA
				length = buffer.append(channel, (int) length);
				message.setTransferred(message.getTransferred() + length);

				// RST 1B
				if (message.getTransferred() % MARKE_SIZE == 0) {
					int m = (int) (message.getTransferred() / MARKE_SIZE);
					final String mark = Integer.toUnsignedString(m, Character.MAX_RADIX);
					buffer.writeByte(RST);
					buffer.writeShort(mark.length());
					buffer.writeASCIIs(mark);
				}
			} else {
				// TAG 1B
				buffer.writeByte(EOF);
				// LENGTH 2B
				buffer.writeShort((int) length);
				// DATA
				length = buffer.append(channel, (int) length);
				message.setTransferred(message.getTransferred() + length);
			}
		} else {
			client.closeChannel();
		}
		return buffer;
	}

	@Override
	public void sent(ChainChannel chain, FileMessage message) throws Exception {
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
}