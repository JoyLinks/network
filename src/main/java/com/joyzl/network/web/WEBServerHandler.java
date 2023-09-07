/*-
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.network.Utility;
import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;
import com.joyzl.network.http.Connection;
import com.joyzl.network.http.ContentLength;
import com.joyzl.network.http.ContentType;
import com.joyzl.network.http.HTTPCoder;
import com.joyzl.network.http.HTTPReader;
import com.joyzl.network.http.HTTPStatus;
import com.joyzl.network.http.HTTPWriter;
import com.joyzl.network.http.Message;
import com.joyzl.network.http.TransferEncoding;

/**
 * HTTP SERVER
 *
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class WEBServerHandler implements ChainHandler<Message> {

	// RFC 2616 HTTP/1.1
	// RFC 7540 HTTP/2 暂不支持
	// RFC 1867 multipart/form-data
	// 超大文件上传通过分片方式接收，由专门的Servlet处理

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		chain.receive();
	}

	@Override
	public final Message decode(ChainChannel<Message> chain, DataBuffer buffer) throws Exception {
		// 获取暂存消息,通常是数据接收不足解码未完成的消息
		final WEBSlave slave = (WEBSlave) chain;
		WEBRequest request = slave.getRequest();
		if (request == null) {
			slave.setRequest(request = new WEBRequest());
		}
		// 阻止超过最大限制的数据帧
		if (buffer.readable() > WEBCoder.FRAME_MAX) {
			slave.setRequest(null);
			buffer.clear();
			return null;
		}
		// 消息解码
		final HTTPReader reader = new HTTPReader(buffer);
		switch (request.state()) {
			case Message.COMMAND:
				if (HTTPCoder.readCommand(reader, request)) {
					request.state(Message.HEADERS);
				} else {
					return null;
				}
			case Message.HEADERS:
				if (HTTPCoder.readHeaders(reader, request)) {
					request.state(Message.CONTENT);
				} else {
					return null;
				}
			case Message.CONTENT:
				final ContentLength content_length = ContentLength.parse(request.getHeader(ContentLength.NAME));
				if (content_length == null) {
					// 无请求实体内容
					buffer.clear();
					request.state(Message.COMPLETE);
				} else if (content_length.getLength() <= 0) {
					buffer.clear();
					request.state(Message.COMPLETE);
				} else if (content_length.getLength() <= buffer.readable()) {
					final ContentType content_type = ContentType.parse(request.getHeader(ContentType.NAME));
					if (content_type == null) {
						// 缺省为"application/octet-stream"
						WEBCoder.readRAW(buffer, request);
						request.state(Message.COMPLETE);
					} else if (WEBCoder.X_WWW_FORM_URLENCODED.equalsIgnoreCase(content_type.getType())) {
						// POST x-www-form-urlencoded
						WEBCoder.readWWWForm(reader, request);
						request.state(Message.COMPLETE);
					} else if (WEBCoder.MULTIPART_FORMDATA.equalsIgnoreCase(content_type.getType())) {
						// POST multipart
						WEBCoder.readMultipartFormData(reader, request, content_type);
						request.state(Message.COMPLETE);
					} else {
						// RAW / BINARY
						WEBCoder.readRAW(buffer, request);
						request.state(Message.COMPLETE);
					}
				} else {
					return null;
				}
			case Message.COMPLETE:
				slave.setRequest(null);
				return request;
			default:
				reader.close();
				return null;
		}
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		if (Utility.equals(Connection.CLOSE, message.getHeader(Connection.NAME), false)) {
			chain.setType(HTTPStatus.CLOSE.code());
		}
	}

	@Override
	public final void beat(ChainChannel<Message> chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public final DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		if (message instanceof WEBResponse) {
			final WEBResponse response = (WEBResponse) message;
			final DataBuffer buffer = DataBuffer.instance();
			final HTTPWriter writer = new HTTPWriter(buffer);
			switch (response.state()) {
				case Message.COMMAND:
					HTTPCoder.writeCommand(writer, response);
					response.state(Message.HEADERS);
				case Message.HEADERS:
					// 输出头部之前检查消息实体内容并设置消息长度"Content-Length"
					ContentType content_type = ContentType.parse(response.getHeader(ContentType.NAME));
					String transfer_encoding = response.getHeader(TransferEncoding.NAME);
					if (content_type == null) {
						// RFC7231 缺省为"application/octet-stream"
					} else if (WEBCoder.MULTIPART_BYTERANGES.equalsIgnoreCase(content_type.getType())) {
						final HTTPWriter content = new HTTPWriter(DataBuffer.instance());
						WEBCoder.writeMultipart(content, response, content_type);
						response.setContent(content.getDataBuffer());
					}
					if (transfer_encoding == null) {
						response.addHeader(ContentLength.NAME, Integer.toString(WEBCoder.checkContent(response.getContent())));
					}
					HTTPCoder.writeHeaders(writer, response);
					response.state(Message.CONTENT);
				case Message.CONTENT:
					content_type = ContentType.parse(response.getHeader(ContentType.NAME));
					transfer_encoding = response.getHeader(TransferEncoding.NAME);
					if (transfer_encoding == null) {
						if (WEBCoder.writeRAW(buffer, response)) {
							response.state(Message.COMPLETE);
						}
					} else {
						if (WEBCoder.writeChunked(writer, response)) {
							response.state(Message.COMPLETE);
						}
					}
					return buffer;
				case Message.COMPLETE:
				default:
					writer.close();
					return null;
			}
		} else {
			throw new IllegalArgumentException("不支持的消息类型:" + message);
		}
	}

	@Override
	public void sent(ChainChannel<Message> chain, Message message) throws Exception {
		if (message.state() == Message.COMPLETE) {
			// 消息发送完成
			if (chain.getType() == HTTPStatus.CLOSE.code()) {
				// 链路标记为须关闭
				chain.close();
			} else {
				// 链路保持，继续接收消息
				chain.receive();
			}
		} else {
			// 再次发送当前消息直至完成
			chain.send(message);
		}
	}

	@Override
	public void disconnected(ChainChannel<Message> chain) throws Exception {
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		chain.close();
	}
}