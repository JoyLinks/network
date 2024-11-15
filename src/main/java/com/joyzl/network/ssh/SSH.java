package com.joyzl.network.ssh;

/**
 * 安全Shell传输层协议 The Secure Shell (SSH) Transport Layer Protocol
 * <p>
 * https://datatracker.ietf.org/wg/secsh/documents/
 * </p>
 * 
 * @author ZhangXi 2024年9月1日
 */
public class SSH {

	final static String PROTOCOL_VERSION = "2.0";
	final static String SOFTWARE_VERSION = "JOYZL_SSH_2.0";

	// SSH MSG

	final static byte DISCONNECT = 1;
	final static byte IGNORE = 2;
	final static byte UNIMPLEMENTED = 3;
	final static byte DEBUG = 4;
	final static byte SERVICE_REQUEST = 5;
	final static byte SERVICE_ACCEPT = 6;
	final static byte KEXINIT = 20;
	final static byte NEWKEYS = 21;

	/*-
	 * TCP/IP 默认端口22
	 * 
	 * 协议版本交换(Protocol Version Exchange)
	 * +---+--------+
	 * |***|<CR><LF>|
	 * +---+--------+
	 * +--------------------------------+----+--------+--------+
	 * |SSH-protoversion-softwareversion|<SP>|comments|<CR><LF>|
	 * +--------------------------------+----+--------+--------+
	 * "SSH-2.0-billsSSH_3.6.3q3<CR><LF>"
	 * 字符编码:			UTF-8[ISO-10646 RFC3629]
	 * SSH:				固定头
	 * protoversion:	协议版本（必须）1.x / 2.0
	 * softwareversion:	软件版本（必须）
	 * comments:		注释（可选）
	 * 最大长度255(含<CR><LF>)
	 * 
	 * 二进制数据包(Binary Packet Protocol)
	 * +-------------+-------------+-------+------+---+
	 * |PACKET_LENGTH|RANDOM_LENGTH|PAYLOAD|RANDOM|MAC|
	 * +-------------+-------------+-------+------+---+
	 * packet_length:	UInt32 4Byte
	 * random_length:	0~255 1Byte
	 * payload:			负载数据（可能压缩）
	 * random:			随机填充字节，使得总长度为密码块倍数或8
	 * MAC:				身份验证码 (Message Authentication Code - MAC)
	 * 数据包最小长度16字节
	 * 数据包最大载荷32768字节
	 * 数据包最大长度35000字节
	 * 
	 * 
	 */

}