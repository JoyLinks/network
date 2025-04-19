package com.joyzl.network.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import com.joyzl.network.buffer.DataBuffer;

/**
 * 霍夫曼编解码 RFC7541
 * 
 * @author ZhangXi 2025年4月1日
 */
public class HuffmanCoder {

	/** 代码表(正查：ASCII-CODE) */
	private final static Code[] CODES = new Code[] {
			// 正查代码表以ASCII值为索引
			new Code(0, 13, 0x1ff8), //
			new Code(1, 23, 0x7fffd8), //
			new Code(2, 28, 0xfffffe2), //
			new Code(3, 28, 0xfffffe3), //
			new Code(4, 28, 0xfffffe4), //
			new Code(5, 28, 0xfffffe5), //
			new Code(6, 28, 0xfffffe6), //
			new Code(7, 28, 0xfffffe7), //
			new Code(8, 28, 0xfffffe8), //
			new Code(9, 24, 0xffffea), //
			new Code(10, 30, 0x3ffffffc), //
			new Code(11, 28, 0xfffffe9), //
			new Code(12, 28, 0xfffffea), //
			new Code(13, 30, 0x3ffffffd), //
			new Code(14, 28, 0xfffffeb), //
			new Code(15, 28, 0xfffffec), //
			new Code(16, 28, 0xfffffed), //
			new Code(17, 28, 0xfffffee), //
			new Code(18, 28, 0xfffffef), //
			new Code(19, 28, 0xffffff0), //
			new Code(20, 28, 0xffffff1), //
			new Code(21, 28, 0xffffff2), //
			new Code(22, 30, 0x3ffffffe), //
			new Code(23, 28, 0xffffff3), //
			new Code(24, 28, 0xffffff4), //
			new Code(25, 28, 0xffffff5), //
			new Code(26, 28, 0xffffff6), //
			new Code(27, 28, 0xffffff7), //
			new Code(28, 28, 0xffffff8), //
			new Code(29, 28, 0xffffff9), //
			new Code(30, 28, 0xffffffa), //
			new Code(31, 28, 0xffffffb), //
			new Code(32, 6, 0x14), //
			new Code(33, 10, 0x3f8), //
			new Code(34, 10, 0x3f9), //
			new Code(35, 12, 0xffa), //
			new Code(36, 13, 0x1ff9), //
			new Code(37, 6, 0x15), //
			new Code(38, 8, 0xf8), //
			new Code(39, 11, 0x7fa), //
			new Code(40, 10, 0x3fa), //
			new Code(41, 10, 0x3fb), //
			new Code(42, 8, 0xf9), //
			new Code(43, 11, 0x7fb), //
			new Code(44, 8, 0xfa), //
			new Code(45, 6, 0x16), //
			new Code(46, 6, 0x17), //
			new Code(47, 6, 0x18), //
			new Code(48, 5, 0x0), //
			new Code(49, 5, 0x1), //
			new Code(50, 5, 0x2), //
			new Code(51, 6, 0x19), //
			new Code(52, 6, 0x1a), //
			new Code(53, 6, 0x1b), //
			new Code(54, 6, 0x1c), //
			new Code(55, 6, 0x1d), //
			new Code(56, 6, 0x1e), //
			new Code(57, 6, 0x1f), //
			new Code(58, 7, 0x5c), //
			new Code(59, 8, 0xfb), //
			new Code(60, 15, 0x7ffc), //
			new Code(61, 6, 0x20), //
			new Code(62, 12, 0xffb), //
			new Code(63, 10, 0x3fc), //
			new Code(64, 13, 0x1ffa), //
			new Code(65, 6, 0x21), //
			new Code(66, 7, 0x5d), //
			new Code(67, 7, 0x5e), //
			new Code(68, 7, 0x5f), //
			new Code(69, 7, 0x60), //
			new Code(70, 7, 0x61), //
			new Code(71, 7, 0x62), //
			new Code(72, 7, 0x63), //
			new Code(73, 7, 0x64), //
			new Code(74, 7, 0x65), //
			new Code(75, 7, 0x66), //
			new Code(76, 7, 0x67), //
			new Code(77, 7, 0x68), //
			new Code(78, 7, 0x69), //
			new Code(79, 7, 0x6a), //
			new Code(80, 7, 0x6b), //
			new Code(81, 7, 0x6c), //
			new Code(82, 7, 0x6d), //
			new Code(83, 7, 0x6e), //
			new Code(84, 7, 0x6f), //
			new Code(85, 7, 0x70), //
			new Code(86, 7, 0x71), //
			new Code(87, 7, 0x72), //
			new Code(88, 8, 0xfc), //
			new Code(89, 7, 0x73), //
			new Code(90, 8, 0xfd), //
			new Code(91, 13, 0x1ffb), //
			new Code(92, 19, 0x7fff0), //
			new Code(93, 13, 0x1ffc), //
			new Code(94, 14, 0x3ffc), //
			new Code(95, 6, 0x22), //
			new Code(96, 15, 0x7ffd), //
			new Code(97, 5, 0x3), //
			new Code(98, 6, 0x23), //
			new Code(99, 5, 0x4), //
			new Code(100, 6, 0x24), //
			new Code(101, 5, 0x5), //
			new Code(102, 6, 0x25), //
			new Code(103, 6, 0x26), //
			new Code(104, 6, 0x27), //
			new Code(105, 5, 0x6), //
			new Code(106, 7, 0x74), //
			new Code(107, 7, 0x75), //
			new Code(108, 6, 0x28), //
			new Code(109, 6, 0x29), //
			new Code(110, 6, 0x2a), //
			new Code(111, 5, 0x7), //
			new Code(112, 6, 0x2b), //
			new Code(113, 7, 0x76), //
			new Code(114, 6, 0x2c), //
			new Code(115, 5, 0x8), //
			new Code(116, 5, 0x9), //
			new Code(117, 6, 0x2d), //
			new Code(118, 7, 0x77), //
			new Code(119, 7, 0x78), //
			new Code(120, 7, 0x79), //
			new Code(121, 7, 0x7a), //
			new Code(122, 7, 0x7b), //
			new Code(123, 15, 0x7ffe), //
			new Code(124, 11, 0x7fc), //
			new Code(125, 14, 0x3ffd), //
			new Code(126, 13, 0x1ffd), //
			new Code(127, 28, 0xffffffc), //
			new Code(128, 20, 0xfffe6), //
			new Code(129, 22, 0x3fffd2), //
			new Code(130, 20, 0xfffe7), //
			new Code(131, 20, 0xfffe8), //
			new Code(132, 22, 0x3fffd3), //
			new Code(133, 22, 0x3fffd4), //
			new Code(134, 22, 0x3fffd5), //
			new Code(135, 23, 0x7fffd9), //
			new Code(136, 22, 0x3fffd6), //
			new Code(137, 23, 0x7fffda), //
			new Code(138, 23, 0x7fffdb), //
			new Code(139, 23, 0x7fffdc), //
			new Code(140, 23, 0x7fffdd), //
			new Code(141, 23, 0x7fffde), //
			new Code(142, 24, 0xffffeb), //
			new Code(143, 23, 0x7fffdf), //
			new Code(144, 24, 0xffffec), //
			new Code(145, 24, 0xffffed), //
			new Code(146, 22, 0x3fffd7), //
			new Code(147, 23, 0x7fffe0), //
			new Code(148, 24, 0xffffee), //
			new Code(149, 23, 0x7fffe1), //
			new Code(150, 23, 0x7fffe2), //
			new Code(151, 23, 0x7fffe3), //
			new Code(152, 23, 0x7fffe4), //
			new Code(153, 21, 0x1fffdc), //
			new Code(154, 22, 0x3fffd8), //
			new Code(155, 23, 0x7fffe5), //
			new Code(156, 22, 0x3fffd9), //
			new Code(157, 23, 0x7fffe6), //
			new Code(158, 23, 0x7fffe7), //
			new Code(159, 24, 0xffffef), //
			new Code(160, 22, 0x3fffda), //
			new Code(161, 21, 0x1fffdd), //
			new Code(162, 20, 0xfffe9), //
			new Code(163, 22, 0x3fffdb), //
			new Code(164, 22, 0x3fffdc), //
			new Code(165, 23, 0x7fffe8), //
			new Code(166, 23, 0x7fffe9), //
			new Code(167, 21, 0x1fffde), //
			new Code(168, 23, 0x7fffea), //
			new Code(169, 22, 0x3fffdd), //
			new Code(170, 22, 0x3fffde), //
			new Code(171, 24, 0xfffff0), //
			new Code(172, 21, 0x1fffdf), //
			new Code(173, 22, 0x3fffdf), //
			new Code(174, 23, 0x7fffeb), //
			new Code(175, 23, 0x7fffec), //
			new Code(176, 21, 0x1fffe0), //
			new Code(177, 21, 0x1fffe1), //
			new Code(178, 22, 0x3fffe0), //
			new Code(179, 21, 0x1fffe2), //
			new Code(180, 23, 0x7fffed), //
			new Code(181, 22, 0x3fffe1), //
			new Code(182, 23, 0x7fffee), //
			new Code(183, 23, 0x7fffef), //
			new Code(184, 20, 0xfffea), //
			new Code(185, 22, 0x3fffe2), //
			new Code(186, 22, 0x3fffe3), //
			new Code(187, 22, 0x3fffe4), //
			new Code(188, 23, 0x7ffff0), //
			new Code(189, 22, 0x3fffe5), //
			new Code(190, 22, 0x3fffe6), //
			new Code(191, 23, 0x7ffff1), //
			new Code(192, 26, 0x3ffffe0), //
			new Code(193, 26, 0x3ffffe1), //
			new Code(194, 20, 0xfffeb), //
			new Code(195, 19, 0x7fff1), //
			new Code(196, 22, 0x3fffe7), //
			new Code(197, 23, 0x7ffff2), //
			new Code(198, 22, 0x3fffe8), //
			new Code(199, 25, 0x1ffffec), //
			new Code(200, 26, 0x3ffffe2), //
			new Code(201, 26, 0x3ffffe3), //
			new Code(202, 26, 0x3ffffe4), //
			new Code(203, 27, 0x7ffffde), //
			new Code(204, 27, 0x7ffffdf), //
			new Code(205, 26, 0x3ffffe5), //
			new Code(206, 24, 0xfffff1), //
			new Code(207, 25, 0x1ffffed), //
			new Code(208, 19, 0x7fff2), //
			new Code(209, 21, 0x1fffe3), //
			new Code(210, 26, 0x3ffffe6), //
			new Code(211, 27, 0x7ffffe0), //
			new Code(212, 27, 0x7ffffe1), //
			new Code(213, 26, 0x3ffffe7), //
			new Code(214, 27, 0x7ffffe2), //
			new Code(215, 24, 0xfffff2), //
			new Code(216, 21, 0x1fffe4), //
			new Code(217, 21, 0x1fffe5), //
			new Code(218, 26, 0x3ffffe8), //
			new Code(219, 26, 0x3ffffe9), //
			new Code(220, 28, 0xffffffd), //
			new Code(221, 27, 0x7ffffe3), //
			new Code(222, 27, 0x7ffffe4), //
			new Code(223, 27, 0x7ffffe5), //
			new Code(224, 20, 0xfffec), //
			new Code(225, 24, 0xfffff3), //
			new Code(226, 20, 0xfffed), //
			new Code(227, 21, 0x1fffe6), //
			new Code(228, 22, 0x3fffe9), //
			new Code(229, 21, 0x1fffe7), //
			new Code(230, 21, 0x1fffe8), //
			new Code(231, 23, 0x7ffff3), //
			new Code(232, 22, 0x3fffea), //
			new Code(233, 22, 0x3fffeb), //
			new Code(234, 25, 0x1ffffee), //
			new Code(235, 25, 0x1ffffef), //
			new Code(236, 24, 0xfffff4), //
			new Code(237, 24, 0xfffff5), //
			new Code(238, 26, 0x3ffffea), //
			new Code(239, 23, 0x7ffff4), //
			new Code(240, 26, 0x3ffffeb), //
			new Code(241, 27, 0x7ffffe6), //
			new Code(242, 26, 0x3ffffec), //
			new Code(243, 26, 0x3ffffed), //
			new Code(244, 27, 0x7ffffe7), //
			new Code(245, 27, 0x7ffffe8), //
			new Code(246, 27, 0x7ffffe9), //
			new Code(247, 27, 0x7ffffea), //
			new Code(248, 27, 0x7ffffeb), //
			new Code(249, 28, 0xffffffe), //
			new Code(250, 27, 0x7ffffec), //
			new Code(251, 27, 0x7ffffed), //
			new Code(252, 27, 0x7ffffee), //
			new Code(253, 27, 0x7ffffef), //
			new Code(254, 27, 0x7fffff0), //
			new Code(255, 26, 0x3ffffee), //
			new Code(256, 30, 0x3fffffff),//
	};

	private static class Code {
		final char ascii;
		final int length;
		final int code;

		Code(int ascii, int length, int code) {
			this.ascii = (char) ascii;
			this.length = length;
			this.code = code;
		}
	}

	/**
	 * 代码排序：长度，代码
	 */
	private final static Comparator<Code> CODE_ORDER = new Comparator<Code>() {
		@Override
		public int compare(Code a, Code b) {
			if (a.length == b.length) {
				return a.code - b.code;
			}
			return a.length - b.length;
		}
	};

	/** 反查代码表 */
	private final static Code[][] GROUPS;
	/** 反查代码表中的最少和最多位数 */
	private final static int MIN_LENGTH, MAX_LENGTH;
	private final static byte MIN_MASK;
	static {
		// 构建反查代码表
		// 相同位数代码合并为组
		// 组中代码从小到大排序

		// 复制所有代码用于排序
		final Code[] codes = Arrays.copyOf(CODES, CODES.length);

		// 排序：位长度、代码
		Arrays.sort(codes, CODE_ORDER);

		// 最多32组，所有代码最多4字节
		GROUPS = new Code[32][];

		// 分组：位长度相同为组
		final Code[] group = new Code[codes.length];

		int n = -1;
		for (int i = 0; i < codes.length; i++) {
			if (n < 0) {
				group[++n] = codes[i];
			} else if (group[n].length == codes[i].length) {
				group[++n] = codes[i];
			} else {
				GROUPS[group[n].length] = Arrays.copyOfRange(group, 0, n + 1);
				group[n = 0] = codes[i];
			}
		}
		if (n >= 0) {
			GROUPS[group[n].length] = Arrays.copyOfRange(group, 0, n + 1);
		}

		MIN_LENGTH = codes[0].length;
		MAX_LENGTH = codes[codes.length - 1].length;
		MIN_MASK = (byte) (0b11111111 >>> (8 - MIN_LENGTH));
		// System.out.println("min:" + MIN_LENGTH + ",max:" + MAX_LENGTH);
	}

	private HuffmanCoder() {
	}

	/** 计算编码后字节数量 */
	public static int byteSize(CharSequence value) {
		int size = 0;
		for (int i = 0; i < value.length(); i++) {
			size += CODES[value.charAt(i)].length;
		}
		return (size + 7) / 8;
	}

	/** 编码字符串为字节 */
	public static void encode(DataBuffer buffer, CharSequence value) throws IOException {
		int temp = 0, r = 8;
		Code code;
		for (int i = 0; i < value.length(); i++) {
			code = CODES[value.charAt(i)];

			// 判断之前是否有剩余位
			if (r < 8) {
				if (code.length < r) {
					// 补齐之前剩余空位，且还不足1字节
					r = r - code.length;
					temp = temp | code.code << r;
					continue;
				} else {
					// 补齐之前空余位，已满足1字节
					temp = temp | code.code >>> (code.length - r);
					buffer.writeByte(temp);
					// 当前还有剩余位
					r = code.length - r;
				}
			} else {
				r = code.length;
			}

			// 输出整8位
			while (r >= 8) {
				r -= 8;
				buffer.writeByte(code.code >>> r);
			}

			// 将剩余位左移对齐8位
			r = 8 - r;
			temp = code.code << r;
		}

		// 结尾填充
		if (r < 8) {
			code = CODES[CODES.length - 1];
			temp = temp | code.code >>> (code.length - r);
			buffer.writeByte(temp);
		}
	}

	/** 解码字节为字符串 */
	public static void decode(DataBuffer buffer, StringBuilder builder, int length) throws IOException {
		int temp = 0, l = 0, r = 0, i;
		byte value;

		length = buffer.readable() - length;
		while (buffer.readable() > length) {
			value = buffer.readByte();
			// r表示value可用位数
			r = 8;

			do {
				// l表示temp已有位数
				if (l > 0) {
					// 从value取1位合并到temp
					r--;
					temp = (temp << 1) + (value >>> r & 0b00000001);
					l++;
				} else if (r < MIN_LENGTH) {
					// 剩余位全部转移到temp
					// 因不足最少代码位，需求下一个字节
					l = r;
					r = 8 - r;
					temp = value & (0b11111111 >>> r);
					break;
				} else {
					// 转移最少代码位到temp
					r -= MIN_LENGTH;
					temp = (value >>> r) & MIN_MASK;
					l = MIN_LENGTH;
				}

				if (l > MAX_LENGTH) {
					// 超出最大位长度
					throw new IOException("无效的霍夫曼编码");
				}

				// 反查代码表
				if (GROUPS[l] != null) {
					if (temp > GROUPS[l][GROUPS[l].length - 1].code) {
						// System.out.println("continue");
						continue;
					}
					// if (temp < GROUPS[l][0].code) {
					// // System.out.println("continue");
					// continue;
					// }

					i = 0;
					do {
						if (GROUPS[l][i].code == temp) {
							builder.append(GROUPS[l][i].ascii);
							l = 0;
							break;
						}
					} while (++i < GROUPS[l].length);
					// System.out.println(i);
				}

			} while (r > 0);
		}
	}
}