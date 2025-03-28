package com.joyzl.network.ftp;

import java.io.IOException;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;
import com.joyzl.network.chain.ChainHandler;

/**
 * FTP Client Handler
 * 
 * @author ZhangXi 2024年7月9日
 */
public class FTPClientHandler implements ChainHandler {

	// 访问控制：USER PASS ACCT CWD CDUP SMNT QUIT REIN
	// 传输参数：PORT PASV TYPE STRU MODE
	// 服务：RETR STOR STOU APPE ALLO REST RNFR RNTO ABOR
	// DELE RMD MKD PWD LIST NLST SITE SYST STAT HELP NOOP
	// 扩展：SIZE MDTM MLST MLSD

	final static FTPClientHandler INSTANCE = new FTPClientHandler();

	@Override
	public void connected(ChainChannel chain) throws Exception {
		chain.receive();
	}

	@Override
	public DataBuffer encode(ChainChannel chain, Object message) throws Exception {
		final DataBuffer buffer = DataBuffer.instance();
		final FTPMessage command = (FTPMessage) message;
		// COMMAND
		buffer.writeASCIIs(command.getCommand().name());
		switch (command.getCommand()) {
			case USER:
				// 用户 USER SP username CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case PASS:
				// 密码 PASS SP password CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case ACCT:
				// 账户 ACCT SP account-information CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case CWD:
				// 改变工作目录 CWD SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case CDUP:
				// 返回上层目录 CDUP CRLF */
				break;
			case SMNT:
				// 结构装备 SMNT SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case QUIT:
				// 注销 QUIT CRLF */
				break;
			case REIN:
				// 重新初始化 REIN CRLF */
				break;
			case PORT:
				// 数据端口 PORT SP host-port CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case PASV:
				// 被动 PASV CRLF */
				break;
			case TYPE:
				// 表示类型 TYPE SP type-code CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case STRU:
				// 文件结构 STRU SP structure-code CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case MODE:
				// 传输模式 MODE SP mode-code CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case RETR:
				// 获得 RETR SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case STOR:
				// 保存 STOR SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case STOU:
				// 唯一保存 STOU CRLF */
				break;
			case APPE:
				// 追加 APPE SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case ALLO:
				// 分配 ALLO SP decimal-integer [SP R SP decimal-integer] CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case REST:
				// 重新开始 REST SP marker CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case RNFR:
				// 重命名开始 RNFR SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case RNTO:
				// 重命名为 RNTO SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case ABOR:
				// 放弃 ABOR CRLF */
				break;
			case DELE:
				// 删除 DELE SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case RMD:
				// 删除目录 RMD SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case MKD:
				// 新建目录 MKD SP pathname CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case PWD:
				// 打印工作目录 PWD CRLF */
				break;
			case LIST:
				// 列表 LIST [SP pathname] CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case NLST:
				// 名字列表 NLST [SP pathname] CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case SITE:
				// 站点参数 SITE SP string CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case SYST:
				// 系统 SYST CRLF */
				break;
			case STAT:
				// 状态 STAT [SP pathname] CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case HELP:
				// 帮助 HELP [SP string] CRLF */
				buffer.writeASCII(FTP.SPACE);
				if (command.getParameter() != null) {
					buffer.writeASCIIs(command.getParameter());
				}
				break;
			case NOOP:
				// 空操作 NOOP CRLF */
				break;
			default:
				//
				break;
		}
		buffer.writeASCIIs(FTP.CRLF);
		return buffer;
	}

	@Override
	public void sent(ChainChannel chain, Object message) throws Exception {
		final FTPClient link = (FTPClient) chain;
		FTPMessage command = (FTPMessage) message;
		if (command == null) {
			command = link.getCurrent();
			command.setCode(999);
			command.finish();
		} else {
			chain.receive();
		}
	}

	@Override
	public FTPMessage decode(ChainChannel chain, DataBuffer reader) throws Exception {
		if (reader.readable() < 5) {
			return null;
		}

		// 单行
		// CODE<SP>TEXT<CRLF>

		// 多行
		// CODE<->TEXT<CRLF>
		// ...CRLF
		// CODE<SP>TEXT<CRLF>

		final FTPClient link = (FTPClient) chain;
		FTPMessage command = link.getCurrent();
		if (command == null) {
			command = new NOOP();
		}
		int value;
		if (command.getCode() < 1000) {
			// CODE 3
			command.setCode(readCode(reader));

			value = reader.readASCII();
			if (value == FTP.SPACE) {
				value = checkCRLF(reader);
				if (value >= 0) {
					command.setText(new String(reader.readASCIIs(value)));
					reader.skipBytes(2);
					return command;
				} else {
					return null;
				}
			} else //
			if (value == FTP.HYPHEN) {
				value = checkCRLF(reader);
				if (value >= 0) {
					command.setText(new String(reader.readASCIIs(value)));
					reader.skipBytes(2);
					// NEXT
				} else {
					return null;
				}
			} else {
				throw new IllegalStateException("无法识别的报文格式");
			}
		} else {
			// 还原代码
			command.setCode(command.getCode() - 1000);
		}

		// NEXT ROW
		while (reader.readable() > 0) {
			value = checkCRLF(reader);
			if (value >= 0) {
				if (reader.get(3) == FTP.SPACE) {
					if (readCode(reader) == command.getCode()) {
						reader.readByte();
						value -= 4;

						command.setText(new String(reader.readASCIIs(value)));
						reader.skipBytes(2);
						return command;
					} else {
						// 读取了CODE，但不是首行相同值
						// 已经读取了CODE，如何恢复这三个字符呢？
						value -= 3;
					}
				}
				command.setText(new String(reader.readASCIIs(value)));
				reader.skipBytes(2);
			} else {
				break;
			}
		}
		// 通过code标记多行继续接收
		command.setCode(command.getCode() + 1000);
		return null;
	}

	@Override
	public void received(ChainChannel chain, Object message) throws Exception {
		final FTPClient link = (FTPClient) chain;
		FTPMessage command = (FTPMessage) message;
		if (command == null) {
			command = link.getCurrent();
			command.setCode(999);
			command.finish();
		} else {
			if (link.getCurrent() == command) {
				command.finish();
			} else {
				link.getListener().finish(command);
			}
			// 1**状态很可能还有后续状态反馈
			if (command.getCode() < 200) {
				chain.receive();
			}
		}
	}

	@Override
	public void disconnected(ChainChannel chain) throws Exception {

	}

	@Override
	public void error(ChainChannel chain, Throwable e) {
		e.printStackTrace();
	}

	private int readCode(DataBuffer reader) throws IOException {
		int code = Character.digit(reader.readASCII(), 10) * 100;
		code += Character.digit(reader.readASCII(), 10) * 10;
		code += Character.digit(reader.readASCII(), 10);
		return code;
	}

	private int checkCRLF(DataBuffer reader) {
		for (int index = 0; index < reader.readable(); index++) {
			if (reader.get(index) == FTP.CRLF.charAt(0)) {
				if (reader.get(index + 1) == FTP.CRLF.charAt(1)) {
					return index;
				}
			}
		}
		return -1;
	}
}