/*
 * Copyright © 2017-2025 重庆骄智科技有限公司.
 * 本软件根据 Apache License 2.0 开源，详见 LICENSE 文件。
 */
package com.joyzl.network.http;

/**
 * Transfer-Encoding 传输编码,消息首部指明了将 entity 安全传递给用户所采用的编码形式。
 * 
 * Transfer-Encoding是一个逐跳传输消息首部，即仅应用于两个节点之间的消息传递，而不是所请求的资源本身。
 * 一个多节点连接中的每一段都可以应用不同的Transfer-Encoding值。
 * 如果你想要将压缩后的数据应用于整个连接，那么请使用端到端传输消息首部Content-Encoding。
 * 当这个消息首部出现在HEAD请求的响应中，而这样的响应没有消息体，那么它其实指的是应用在相应的GET请求的应答的值。
 * 
 * <pre>
 * Transfer-Encoding: chunked
 * Transfer-Encoding: compress
 * Transfer-Encoding: deflate
 * Transfer-Encoding: gzip
 * Transfer-Encoding: identity
 * 
 * // Several values can be listed, separated by a comma
 * Transfer-Encoding: gzip, chunked
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年1月11日
 */
public final class TransferEncoding {

	public final static String NAME = HTTP1.Transfer_Encoding;

	/**
	 * 用于指代自身（例如：未经过压缩和修改）。除非特别指明，这个标记始终可以被接受。
	 */
	public final static String IDENTITY = "identity";
	/**
	 * 数据以一系列分块的形式进行发送。Content-Length首部在这种情况下不被发送。
	 * 在每一个分块的开头需要添加当前分块的长度，以十六进制的形式表示，后面紧跟着'\r\n'，之后是分块本身，后面也是'\r\n'。
	 * 终止块是一个常规的分块，不同之处在于其长度为0。终止块后面是一个挂载（trailer），由一系列（或者为空）的实体消息首部构成。
	 */
	public final static String CHUNKED = "chunked";
	/**
	 * 采用Lempel-Ziv-Welch(LZW)压缩算法。这个名称来自UNIX系统的compress程序，该程序实现了前述算法。
	 * 与其同名程序已经在大部分UNIX发行版中消失一样，这种内容编码方式已经被大部分浏览器弃用，部分因为专利问题（这项专利在2003年到期）。
	 */
	public final static String COMPRESS = "compress";
	/**
	 * 采用zlib结构(在RFC-1950中规定)，和deflate压缩算法(在 RFC-1951中规定)。
	 */
	public final static String DEFLATE = "deflate";
	/**
	 * 表示采用Lempel-Ziv coding(LZ77)压缩算法，以及32位CRC校验的编码方式。
	 * 这个编码方式最初由UNIX平台上的gzip程序采用。处于兼容性的考虑，HTTP/1.1标准提议支持这种编码方式的服务器应该识别作为别名的x-gzip指令。
	 */
	public final static String GZIP = "gzip";

	public static String combine(String a) {
		if (GZIP.equals(a)) {
			return "gzip, chunked";
		}
		if (DEFLATE.equals(a)) {
			return "deflate, chunked";
		}
		if (IDENTITY.equals(a)) {
			return "chunked";
		}
		if (COMPRESS.equals(a)) {
			return "compress, chunked";
		}
		return "chunked";
	}
}