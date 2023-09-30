package nl.utwente.di.visol1.util;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

public class EncryptionUtil {
	private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
	private static final String SECURE_RANDOM_PROVIDER = "SUN";
	private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA512";
	private static final String SECRET_KEY_PROVIDER = "SunJCE";
	private static final int SALT_SIZE = 32;
	public static final int PBE_KEY_ITERATION_COUNT = 65536;
	public static final int PBE_KEY_LENGTH = 512;

	public static final MessageDigest MD5;

	static {
		try {
			MD5 = MessageDigest.getInstance("MD5", "SUN");
		} catch (NoSuchAlgorithmException | NoSuchProviderException exception) {
			throw new IllegalStateException("MD5 not supported", exception);
		}
	}

	public static String bytesToHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	private static byte[] generateSalt() {
		SecureRandom random;
		try {
			random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, SECURE_RANDOM_PROVIDER);
		} catch (NoSuchAlgorithmException | NoSuchProviderException ignored) {
			random = new SecureRandom();
		}
		byte[] bytes = new byte[SALT_SIZE];
		random.nextBytes(bytes);
		return bytes;
	}

	public static SaltedKey generateKey(String password) {
		byte[] salt = generateSalt();
		byte[] hash = encrypt(password, salt);
		return new SaltedKey(hash, salt);
	}

	public static byte[] encrypt(String password, byte[] salt) {
		try {
			SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM, SECRET_KEY_PROVIDER);
			// TODO determine how high we want the iteration count to be
			KeySpec keySpecification = new PBEKeySpec(password.toCharArray(), salt, PBE_KEY_ITERATION_COUNT, PBE_KEY_LENGTH);
			SecretKey secretKey = factory.generateSecret(keySpecification);
			return secretKey.getEncoded();
		} catch (NoSuchAlgorithmException | NoSuchProviderException exception) {
			throw new RuntimeException("Could not find algorithms necessary to generate keys", exception);
		} catch (InvalidKeySpecException exception) {
			throw new RuntimeException("Could not generate key", exception);
		}
	}

	public static boolean validate(String password, SaltedKey key) {
		byte[] hash = encrypt(password, key.salt);
		return Arrays.equals(hash, key.hash);
	}

	public static void main(String[] args) {
		String OUTPUT_FORMAT = "%-20s: %s%n";
		BiConsumer<String, byte[]> print = (s, b) -> System.out.printf(OUTPUT_FORMAT, s, bytesToHex(b));

		String password = "password";
		SaltedKey key = generateKey(password);
		byte[] check = encrypt(password, key.salt);

		print.accept("hash", key.hash);
		print.accept("salt", key.salt);
		print.accept("check", check);
		System.out.printf(OUTPUT_FORMAT, "equals", Arrays.equals(key.hash, check));
	}

	public static void outputProviders() {
		Map<String, Map<Provider, List<Service>>> servicesMap = new HashMap<>();

		Arrays.stream(Security.getProviders()).forEach(provider -> {
//			System.out.printf("%s (%s): %s %n", provider.getName(), provider.getVersionStr(), provider.getInfo());
			provider.getServices().forEach(service -> {
//				System.out.printf("\t%s: %s in %s%n", service.getType(), service.getAlgorithm(), service.getClassName());
				servicesMap.putIfAbsent(service.getType(), new HashMap<>());
				servicesMap.get(service.getType()).putIfAbsent(provider, new ArrayList<>());
				servicesMap.get(service.getType()).get(provider).add(service);
			});
		});

//		System.out.println("\n\n");

		/*servicesMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			System.out.printf("%s: %n", entry.getKey());
			entry.getValue().entrySet().stream().sorted(Comparator.comparing(o -> o.getKey().getName())).forEach(entry2 -> {
				System.out.printf(
					"\t%s: %s%n",
					entry2.getKey(),
					entry2.getValue().stream().map(Service::getAlgorithm).collect(Collectors.joining(", "))
				);
			});
		});*/

		servicesMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
			System.out.printf("%s: %n", entry.getKey());
			entry.getValue().values().stream().flatMap(List::stream).sorted(Comparator.comparing(Service::getAlgorithm)).forEach(
				service -> System.out.printf("\t%s from %s%n", service.getAlgorithm(), service.getProvider().getName())
			);
		});
	}

	public static class SaltedKey {
		public final byte[] hash;
		public final byte[] salt;

		public SaltedKey(byte[] hash, byte[] salt) {
			this.hash = hash;
			this.salt = salt;
		}

		@Override
		public String toString() {
			return String.format("SaltedKey{hash=%s, salt=%s}", bytesToHex(hash), bytesToHex(salt));
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			SaltedKey other = (SaltedKey) o;
			return Arrays.equals(hash, other.hash) && Arrays.equals(salt, other.salt);
		}

		@Override
		public int hashCode() {
			return Objects.hash(Arrays.hashCode(hash), Arrays.hashCode(salt));
		}
	}
}
