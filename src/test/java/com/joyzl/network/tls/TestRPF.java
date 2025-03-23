package com.joyzl.network.tls;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import com.joyzl.network.Utility;

class TestRPF {

	// Seed = Client Random + Server Random

	@Test
	void testV12() throws Exception {
		final byte[] preMasterSecret = Utility.hex("f8938ecc9edebc5030c0c6a441e213cd24e6f770a50dda07876f8d55da062bcadb386b411fd4fe4313a604fce6c17fbc");
		final byte[] clientRandom1 = Utility.hex("36c129d01a3200894b9179faac589d9835d58775f9b5ea3587cb8fd0364cae8c");
		final byte[] serverRandom1 = Utility.hex("f6c9575ed7ddd73e1f7d16eca115415812a43c2b747daaaae043abfb50053fce");
		final byte[] masterSecret = Utility.hex("202c88c00f84a17a20027079604787461176455539e705be730890602c289a5001e34eeb3a043e5d52a65e66125188bf");

		final byte[] clientRandom2 = Utility.hex("62e1fd91f23f558a605f28478c58cf72637b89784d959df7e946d3f07bd1b616");
		final byte[] serverRandom2 = Utility.hex("ae6c806f8ad4d80784549dff28a4b58fd837681a51d928c3e30ee5ff14f39868");
		final byte[] keyBlock = Utility.hex("d06139889fffac1e3a71865f504aa5d0d2a2e89506c6f2279b670c3e1b74f531016a2530c51a3a0f7e1d6590d0f0566b2f387f8d11fd4f731cdd572d2eae927f6f2f81410b25e6960be68985add6c38445ad9f8c64bf8068bf9a6679485d966f1ad6f68b43495b10a683755ea2b858d70ccac7ec8b053c6bd41ca299d4e51928");

		final PRF rpf = new PRF("HmacSHA256");

		byte[] ms = rpf.prf(preMasterSecret, V2DeriveSecret.MASTER_SECRET, clientRandom1, serverRandom1, masterSecret.length);
		assertArrayEquals(masterSecret, ms);

		byte[] kb = rpf.prf(masterSecret, V2DeriveSecret.KEY_EXPANSION, serverRandom2, clientRandom2, keyBlock.length);
		assertArrayEquals(keyBlock, kb);
	}

}