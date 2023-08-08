/*
 * www.joyzl.net
 * 中翌智联（重庆）科技有限公司
 * Copyright © JOY-Links Company. All rights reserved.
 */
package com.joyzl.network.http;

/**
 * Content-Encoding 内容编码，是一个实体消息首部，用于对特定媒体类型的数据进行压缩。
 * 
 * 当这个首部出现的时候，它的值表示消息主体进行了何种方式的内容编码转换。
 * 这个消息首部用来告知客户端应该怎样解码才能获取在Content-Type中标示的媒体类型内容。
 * 
 * 一般建议对数据尽可能地进行压缩，因此才有了这个消息首部的出现。 不过对于特定类型的文件来说，比如jpeg图片文件，已经是进行过压缩的了。
 * 有时候再次进行额外的压缩无助于负载体积的减小，反而有可能会使其增大。
 * 
 * <pre>
 * Content-Encoding: gzip
 * Content-Encoding: compress
 * Content-Encoding: deflate
 * Content-Encoding: identity
 * Content-Encoding: br
 * </pre>
 * 
 * @author ZhangXi
 * @date 2021年1月11日
 */
public final class ContentEncoding {

	public final static String NAME = "Content-Encoding";

	/**
	 * 表示采用Lempel-Ziv coding(LZ77)压缩算法，以及32位CRC校验的编码方式。
	 * 这个编码方式最初由UNIX平台上的gzip程序采用。处于兼容性的考虑，HTTP/1.1标准提议支持这种编码方式的服务器应该识别作为别名的x-gzip指令。
	 */
	public final static String GZIP = AcceptEncoding.GZIP;
	/**
	 * 采用Lempel-Ziv-Welch(LZW)压缩算法。这个名称来自UNIX系统的compress程序，该程序实现了前述算法。
	 * 与其同名程序已经在大部分UNIX发行版中消失一样，这种内容编码方式已经被大部分浏览器弃用，部分因为专利问题（这项专利在2003年到期）。
	 */
	public final static String COMPRESS = AcceptEncoding.COMPRESS;
	/**
	 * 采用zlib结构(在RFC-1950中规定)，和deflate压缩算法(在 RFC-1951中规定)。
	 */
	public final static String DEFLATE = AcceptEncoding.DEFLATE;
	/**
	 * 用于指代自身（例如：未经过压缩和修改）。除非特别指明，这个标记始终可以被接受。
	 */
	public final static String IDENTITY = AcceptEncoding.IDENTITY;
	/**
	 * 表示采用Brotli算法的编码方式。
	 */
	public final static String BR = AcceptEncoding.BR;
}