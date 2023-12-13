package com.joyzl.network.codec.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.joyzl.codec.BigEndianDataInput;
import com.joyzl.codec.BigEndianDataOutput;
import com.joyzl.codec.DataInput;
import com.joyzl.codec.DataOutput;
import com.joyzl.codec.LittleEndianDataInput;
import com.joyzl.codec.LittleEndianDataOutput;
import com.joyzl.network.codec.BigEndianBCDInput;
import com.joyzl.network.codec.BigEndianBCDOutput;
import com.joyzl.network.codec.LittleEndianBCDInput;
import com.joyzl.network.codec.LittleEndianBCDOutput;

/**
 * 示例各种DataInput实现方式
 * 
 * @author ZhangXi 2023年12月12日
 */
public class StreamSamples {

	InputStream input;
	OutputStream output;

	final DataInput beInput = new BigEndianDataInput() {
		@Override
		public byte readByte() throws IOException {
			return (byte) input.read();
		}
	};
	final DataInput leInput = new LittleEndianDataInput() {
		@Override
		public byte readByte() throws IOException {
			return (byte) input.read();
		}
	};
	final DataOutput beOutput = new BigEndianDataOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			output.write(b);
		}
	};
	final DataOutput leOutput = new LittleEndianDataOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			output.write(b);
		}
	};

	final BigEndianBCDOutput beBCDOutput = new BigEndianBCDOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			output.write(b);
		}
	};
	final BigEndianBCDInput beBCDInput = new BigEndianBCDInput() {
		@Override
		public byte readByte() throws IOException {
			return (byte) input.read();
		}
	};
	final LittleEndianBCDOutput leBCDOutput = new LittleEndianBCDOutput() {
		@Override
		public void writeByte(int b) throws IOException {
			output.write(b);
		}
	};
	final LittleEndianBCDInput leBCDInput = new LittleEndianBCDInput() {
		@Override
		public byte readByte() throws IOException {
			return (byte) input.read();
		}
	};
}