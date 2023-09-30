package nl.utwente.di.visol1.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public class Configuration {
	private static boolean inTestEnvironment = false;

	private Configuration() {}

	private static String getProperty(String key) {
		String environmentVariable = System.getenv("VISOL_CONFIG_" + key.toUpperCase().replace('.', '_'));
		if (environmentVariable != null) {
			return environmentVariable;
		} else {
			String propertiesFile = System.getenv().getOrDefault("VISOL_CONFIG_LOCATION", "config.properties");
			Properties properties = new Properties();
			try (InputStream input = new FileInputStream(propertiesFile)) {
				properties.load(input);
			} catch (IOException exception) {
				System.err.println("Properties file " + new File(propertiesFile).getAbsolutePath() + " not found");
				throw new IllegalStateException("Could not find property " + key);
			}
			return properties.getProperty(key, null);
		}
	}

	public static void useTestEnvironment(boolean useTestEnvironment) {
		inTestEnvironment = useTestEnvironment;
	}

	public static class Database {
		public static final Supplier<String> URL = () -> getProperty("database.url");
		public static final Supplier<String> NAME = () -> getProperty("database.name");
		public static final Supplier<String> SCHEMA = () -> getProperty(
			inTestEnvironment ? "database.test_schema" : "database.production_schema"
		);
		public static final Supplier<String> USERNAME = () -> getProperty("database.username");
		public static final Supplier<String> PASSWORD = () -> getProperty("database.password");

		private Database() {}
	}

	public static class Authorization {
		public static final BooleanSupplier BYPASS = () -> "true".equalsIgnoreCase(getProperty("authorization.bypass"));

		private Authorization() {}
	}
}
