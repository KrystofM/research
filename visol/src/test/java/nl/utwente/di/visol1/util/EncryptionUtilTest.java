package nl.utwente.di.visol1.util;

import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class EncryptionUtilTest {
	private static final int ITERATION_COUNT = 10;
	private static final Random RANDOM = new Random();

	private static String random() {
		int length = RANDOM.nextInt(64) + 1;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			sb.append((char) (RANDOM.nextInt(26) + 'a'));
		}
		return sb.toString();
	}

	@Test
	public void fuzzTest() {
		for (int i = 0; i < ITERATION_COUNT; i++) {
			String input = random();
			EncryptionUtil.SaltedKey key = EncryptionUtil.generateKey(input);
			byte[] rehash = EncryptionUtil.encrypt(input, key.salt);
			assertArrayEquals(key.hash, rehash);
		}
	}
}
