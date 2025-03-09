package com.joyzl.network.tls;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;

class TestRPF {

	final byte[] PreMasterSecret = Utility.hex("000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F202122232425262728292A2B2C2D2E2F");
	final byte[] ClientRandom = Utility.hex("CDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCDCD");
	final byte[] ServerRandom = Utility.hex("ABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABABAB");
	final byte[] ExpectedMasterSecret = Utility.hex("04184C43456353483C7A3636646377746563356336323736383237363536363763373636373636363637363637363637");

	// Seed = Client Random + Server Random

	@Test
	void test() throws Exception {
		final PRF rpf = new PRF("HmacSHA256");
		byte[] masterSecret = rpf.expandLabel(PreMasterSecret, MasterSecret.MASTER_SECRET, ClientRandom, ServerRandom, 48);
		System.out.println(Utility.hex(masterSecret));
		// 398badace9342411657098a4c11491c8af1097eff18f47403b4d7dc492efe0a780136fa6b06a274b36c11216a46465b5

	}

}