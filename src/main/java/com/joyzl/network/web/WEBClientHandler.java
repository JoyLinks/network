/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.web;

import com.joyzl.common.Assist;
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
 * HTTP CLIENT
 * 
 * @author ZhangXi
 * @date 2020年6月26日
 */
public abstract class WEBClientHandler extends WEBCoder implements ChainHandler<Message> {

	@Override
	public void connected(ChainChannel<Message> chain) throws Exception {
		// 客户端连接后不能立即启动接收
		// HTTP 必须由客户端主动发起请求后才能接收
	}

	@Override
	public final Message decode(ChainChannel<Message> chain, DataBuffer buffer) throws Exception {
		final WEBClient client = (WEBClient) chain;
		WEBResponse response = client.getResponse();
		if (response == null) {
			client.setResponse(response = new WEBResponse());
		}
		// 阻止超过最大限制的数据帧
		if (buffer.readable() > WEBCoder.FRAME_MAX) {
			client.setResponse(null);
			buffer.clear();
			return null;
		}
		// 消息解码
		final HTTPReader reader = new HTTPReader(buffer);
		switch (response.state()) {
			case Message.COMMAND:
				if (HTTPCoder.readCommand(reader, response)) {
					response.state(Message.HEADERS);
				} else {
					return null;
				}
			case Message.HEADERS:
				if (HTTPCoder.readHeaders(reader, response)) {
					response.state(Message.CONTENT);
				} else {
					return null;
				}
			case Message.CONTENT:
				final ContentLength content_length = ContentLength.parse(response.getHeader(ContentLength.NAME));
				if (content_length == null) {
					final String transfer_encoding = response.getHeader(TransferEncoding.NAME);
					if (TransferEncoding.CHUNKED.equalsIgnoreCase(transfer_encoding)) {
						if (WEBCoder.readChunked(reader, response)) {
							response.state(Message.FINISH);
						} else {
							return null;
						}
					} else {
						throw new UnsupportedOperationException("不支持的Transfer-Encoding值:" + transfer_encoding);
					}
				} else if (content_length.getLength() <= 0) {
					response.state(Message.FINISH);
				} else if (content_length.getLength() <= buffer.readable()) {
					final ContentType content_type = ContentType.parse(response.getHeader(ContentType.NAME));
					if (content_type == null) {
						WEBCoder.readRAW(buffer, response);
						response.state(Message.FINISH);
					} else if (WEBCoder.MULTIPART_BYTERANGES.equalsIgnoreCase(content_type.getType())) {
						WEBCoder.readMultipart(reader, response, content_type);
						response.state(Message.FINISH);
					} else {
						WEBCoder.readRAW(buffer, response);
						response.state(Message.FINISH);
					}
				} else {
					return null;
				}
			case Message.FINISH:
				client.setResponse(null);
				return response;
			default:
				reader.close();
				return null;
		}
	}

	@Override
	public final void beat(ChainChannel<Message> chain) throws Exception {
		// HTTP 无心跳
	}

	@Override
	public void received(ChainChannel<Message> chain, Message message) throws Exception {
		if (Assist.equals(Connection.CLOSE, message.getHeader(Connection.NAME), false)) {
			chain.setType(HTTPStatus.CLOSE.code());
			chain.close();
		}
	}

	@Override
	public final DataBuffer encode(ChainChannel<Message> chain, Message message) throws Exception {
		if (message instanceof WEBRequest) {
			final WEBRequest request = (WEBRequest) message;
			final DataBuffer buffer = DataBuffer.getB2048();
			final HTTPWriter writer = new HTTPWriter(buffer);
			// COMMAND:
			HTTPCoder.writeCommand(writer, request);

			// HEADERS:
			// 输出头部之前检查消息实体内容并设置消息长度"Content-Length"
			ContentType content_type = ContentType.parse(request.getHeader(ContentType.NAME));
			if (content_type == null) {
				// RFC7231 缺省为"application/octet-stream"
			} else if (WEBCoder.X_WWW_FORM_URLENCODED.equalsIgnoreCase(content_type.getType())) {
				final HTTPWriter content = new HTTPWriter(DataBuffer.getB2048());
				WEBCoder.writeWWWForm(writer, request);
				request.setContent(content.getDataBuffer());
			} else if (WEBCoder.MULTIPART_FORMDATA.equalsIgnoreCase(content_type.getType())) {
				final HTTPWriter content = new HTTPWriter(DataBuffer.getB2048());
				WEBCoder.writeMultipartFormData(writer, request, content_type);
				request.setContent(content.getDataBuffer());
			} else {
				// WEBCoder.writeRAW(buffer, request);
			}
			request.addHeader(ContentLength.NAME, Integer.toString(WEBCoder.checkContent(request.getContent())));
			HTTPCoder.writeHeaders(writer, request);

			// CONTENT:
			WEBCoder.writeRAW(buffer, request);
			writer.close();
			return buffer;
		} else {
			throw new IllegalArgumentException("不支持的消息类型:" + message);
		}
	}

	@Override
	public final void sent(ChainChannel<Message> chain, Message message) throws Exception {
		chain.receive();
	}

	@Override
	public void disconnected(ChainChannel<Message> chain) throws Exception {
	}

	@Override
	public void error(ChainChannel<Message> chain, Throwable e) {
		chain.close();
	}
}