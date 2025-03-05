package com.joyzl.network.codec;

import java.io.IOException;

import com.joyzl.codec.BigEndianDataInput;
import com.joyzl.codec.BigEndianDataOutput;
import com.joyzl.codec.DataInput;
import com.joyzl.codec.DataOutput;
import com.joyzl.codec.LittleEndianDataInput;
import com.joyzl.codec.LittleEndianDataOutput;
import com.joyzl.network.buffer.DataBuffer;

/**
 * 示例DataBuffer包装为DataInput/DataOutput实现方式
 * 
 * @author ZhangXi 2023年12月13日
 */
public class DataBufferSamples {

	DataBuffer buffer;

	final DataInput beInput = new BigEndianDataInput() {
		@Override
		public byte readByte() throws IOException {
			return buffer.readByte();
		}
	};
	final DataInput leInput = new LittleEndianDataInput() {
		@Override
		public byte readByte() throws IOException {
			return buffer.readByte();
		}
	};
	final DataOutput beOutput = new BigEndianDataOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			buffer.writeByte(b);
		}
	};
	final DataOutput leOutput = new LittleEndianDataOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			buffer.writeByte(b);
		}
	};

	final BigEndianBCDInput beBCDInput = new BigEndianBCDInput() {
		@Override
		public byte readByte() throws IOException {
			return buffer.readByte();
		}
	};
	final LittleEndianBCDInput leBCDInput = new LittleEndianBCDInput() {
		@Override
		public byte readByte() throws IOException {
			return buffer.readByte();
		}
	};
	final BigEndianBCDOutput beBCDOutput = new BigEndianBCDOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			buffer.writeByte(b);
		}
	};
	final LittleEndianBCDOutput leBCDOutput = new LittleEndianBCDOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			buffer.writeByte(b);
		}
	};
}
