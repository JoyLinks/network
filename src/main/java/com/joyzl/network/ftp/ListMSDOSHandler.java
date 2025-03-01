package com.joyzl.network.ftp;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.joyzl.network.buffer.DataBuffer;
import com.joyzl.network.chain.ChainChannel;

/**
 * 文件列表
 * 
 * @author ZhangXi 2024年7月11日
 */
public class ListMSDOSHandler extends ListHandler {

	/*-
	 * MSDOS
	 * 02-23-06     10:21AM    <DIR> src
	 * 02-23-05     09:22AM    2246 readme.txt
	 * 05-25-04     08:56AM    987642 VC.h
	 * 
	 * 日期 时间 大小 名称
	 * 日期和时间通常以 MM DD YYYY HH:MM [AM|PM] 的格式显示
	 */

	final static ListMSDOSHandler INSTANCES = new ListMSDOSHandler();

	@Override
	public FTPMessage decode(ChainChannel chain, DataBuffer reader) throws Exception {
		final ListClient client = (ListClient) chain;

		FTPFile file;
		LocalDate date;
		LocalTime time;
		while (reader.readable() > 10) {
			reader.mark();
			file = new FTPFile();

			// 日期
			date = readDate(reader);

			skipSpaces(reader);

			// 时间
			time = readTime(reader);

			skipSpaces(reader);

			// 大小/DIR
			file.setOwner(readString(reader));
			if ("<DIR>".equalsIgnoreCase(file.getOwner())) {
				file.setDirectory(true);
			} else {
				file.setSize(Long.parseLong(file.getOwner()));
			}

			// 名称
			file.setName(readString(reader));

			if (reader.readASCII() == '\n') {
				reader.erase();
				file.setLastModified(LocalDateTime.of(date, time));
				client.getLIST().getFiles().add(file);
			} else {
				reader.reset();
			}
		}

		return null;
	}

	LocalDate readDate(DataBuffer reader) throws IOException {
		// MM DD YYYY
		// 02-23-06

		int month = readInteger(reader);
		int day = readInteger(reader);
		int year = readInteger(reader);
		if (year > 1000) {
			return LocalDate.of(year, month, day);
		} else {
			return LocalDate.of(2000 + year, month, day);
		}
	}

	LocalTime readTime(DataBuffer reader) throws IOException {
		// HH:MM [AM|PM]
		// 10:21AM

		int hour = Character.digit(reader.readASCII(), 10) * 10;
		hour += Character.digit(reader.readASCII(), 10);
		reader.readASCII();
		int minute = Character.digit(reader.readASCII(), 10) * 10;
		minute += Character.digit(reader.readASCII(), 10);
		if (reader.readASCII() == 'P') {
			hour += 12;
		}
		reader.readASCII();
		return LocalTime.of(hour, minute, 0);
	}
}