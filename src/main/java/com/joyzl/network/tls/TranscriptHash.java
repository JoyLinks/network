package com.joyzl.network.tls;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import com.joyzl.network.buffer.DataBuffer;

public class TranscriptHash extends CipherSuiter {

	public TranscriptHash(short code) throws NoSuchAlgorithmException {
		super(code);
	}

	/** Transcript-Hash */
	public void hash(DataBuffer message) {
		if (message.readable() > 0) {
			final ByteBuffer[] buffers = message.reads();
			for (int index = 0; index < buffers.length; index++) {
				buffers[index].mark();
				digest.update(buffers[index]);
				buffers[index].reset();
			}
		}
	}

	/** Transcript-Hash */
	public byte[] digest() {
		return digest.digest();
	}

}