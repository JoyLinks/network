package com.joyzl.network.ftp;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;

/**
 * 文件列表
 * 
 * @author ZhangXi 2024年7月11日
 */
public class ListUNIXHandler extends ListHandler {

	/*-
	 * UNIX
	 * drwxrwxrwx 1 ftp ftp               0 Jun 24 05:29 Games
	 * drwxrwxrwx 1 ftp ftp               0 Jul 04 01:29 Movies
	 * 
	 * 类型权限 链接数 所有者 组 大小 月 年 时间 名称
	 */

	final static ListUNIXHandler INSTANCES = new ListUNIXHandler();

	@Override
	public FTPMessage decode(ChainChannel<FTPMessage> chain, DataBuffer reader) throws Exception {
		final ListClient client = (ListClient) chain;

		FTPFile file;
		while (reader.readable() > 20) {
			reader.mark();
			file = new FTPFile();

			// 类型权限
			file.setDirectory(reader.readASCII() == 'd');
			// 权限
			file.setPermissions(reader.readASCIIs(9));

			skipSpaces(reader);

			// 链接数
			file.setLinks(readInteger(reader));
			// 所有者
			file.setOwner(readString(reader));
			// 组
			file.setOwner(readString(reader));

			skipSpaces(reader);

			// 大小
			file.setSize(readInteger(reader));
			// 月 年 时间
			file.setLastModified(readDateTime(reader));
			// 名称
			file.setName(readString(reader));

			if (reader.readASCII() == '\n') {
				reader.erase();
				client.getLIST().getFiles().add(file);
			} else {
				reader.reset();
			}
		}
		return null;
	}

	LocalDateTime readDateTime(DataBuffer reader) throws IOException {
		// Jun 24 05:29
		// Jul 24 2013
		final Month month = LIST.parseMonth(reader.readASCII(), reader.readASCII(), reader.readASCII());
		skipSpaces(reader);
		int day = readInteger(reader);
		int hour = readInteger(reader);
		if (hour > 1000) {
			return LocalDateTime.of(hour, month, day, 0, 0);
		} else {
			int minute = readInteger(reader);
			final LocalDate NOW = LocalDate.now();
			if (month.getValue() > NOW.getMonthValue()) {
				return LocalDateTime.of(NOW.getYear() - 1, month, day, hour, minute);
			} else {
				return LocalDateTime.of(NOW.getYear(), month, day, hour, minute);
			}
		}
	}
}