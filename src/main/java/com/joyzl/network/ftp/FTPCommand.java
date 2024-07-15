package com.joyzl.network.ftp;

/**
 * FTP 命令
 * <p>
 * RFC5797 FTP Command and Extension Registry
 * 
 * @author ZhangXi 2024年7月5日
 */
public enum FTPCommand {

	// 访问控制

	/** 用户(User Name) USER SP username CRLF */
	USER,
	/** 密码(Password) PASS SP password CRLF */
	PASS,
	/** 账户(Account) ACCT SP account-information CRLF */
	ACCT,
	/** 改变工作目录(Change Working Directory) CWD SP pathname CRLF */
	CWD,
	/** 返回上层目录(Change to Parent Directory) CDUP CRLF */
	CDUP,
	/** 结构装备(Structure Mount) SMNT SP pathname CRLF */
	SMNT,
	/** 注销(Logout) QUIT CRLF */
	QUIT,
	/** 重新初始化(Reinitialize) REIN CRLF */
	REIN,

	// 传输参数

	/** 主动模式（Data Port 服务器连接本地端口），设定数据端口 PORT SP host-port CRLF */
	PORT,
	/** 被动模式（Passive Mode 本地连接服务器端口），获取数据端口 PASV CRLF */
	PASV,
	/** 表示类型(Representation Type) TYPE SP type-code CRLF */
	TYPE,
	/** 文件结构(File Structure) STRU SP structure-code CRLF */
	STRU,
	/** 传输模式(Transfer Mode) MODE SP mode-code CRLF */
	MODE,

	// 服务

	/** 获得(Retrieve) RETR SP pathname CRLF */
	RETR,
	/** 保存(Store) STOR SP pathname CRLF */
	STOR,
	/** 保存唯一(Store Unique) STOU CRLF */
	STOU,
	/** 追加(Append) APPE SP pathname CRLF */
	APPE,
	/** 分配(Allocate) ALLO SP decimal-integer [SP R SP decimal-integer] CRLF */
	ALLO,
	/** 重新开始(Restart) REST SP marker CRLF */
	REST,
	/** 重命名开始(Rename From) RNFR SP pathname CRLF */
	RNFR,
	/** 重命名为(Rename To) RNTO SP pathname CRLF */
	RNTO,
	/** 放弃(Abort) ABOR CRLF */
	ABOR,
	/** 删除文件(Delete File) DELE SP pathname CRLF */
	DELE,
	/** 删除目录(Remove Directory) RMD SP pathname CRLF */
	RMD,
	/** 新建目录(Make Directory) MKD SP pathname CRLF */
	MKD,
	/** 打印工作目录(Print Directory) PWD CRLF */
	PWD,
	/** 列表(List) LIST [SP pathname] CRLF */
	LIST,
	/** 名字列表(Name List) NLST [SP pathname] CRLF */
	NLST,
	/** 站点参数(Site Parameters) SITE SP string CRLF */
	SITE,
	/** 系统(System) SYST CRLF */
	SYST,
	/** 状态(Status) STAT [SP pathname] CRLF */
	STAT,
	/** 帮助(Help) HELP [SP string] CRLF */
	HELP,
	/** 空操作(No-Op) NOOP CRLF */
	NOOP,

	// 安全

	/** Authentication/Security Data */
	ADAT,
	/** Authentication/Security Mechanism */
	AUTH,
	/** Clear Command Channel */
	CCC,
	/** Confidentiality Protected Command */
	CONF,
	/** Privacy Protected Command */
	ENC,
	/** Integrity Protected Command */
	MIC,
	/** Protection Buffer Size */
	PBSZ,
	/** Data Channel Protection Level */
	PROT,

	// IPv6

	/** Extended Port */
	EPRT,
	/** Extended Passive Mode */
	EPSV,

	// 其它

	/** 功能协商(Feature Negotiation) */
	FEAT,
	/** Language */
	LANG,
	/** File Modification Time */
	MDTM,
	/** List Directory */
	MLSD,
	/** List Single Object */
	MLST,
	/** Options */
	OPTS,
	/** File Size */
	SIZE,
}
