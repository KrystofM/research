package nl.utwente.di.visol1.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Base64;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import nl.utwente.di.visol1.dao.EmployeeDao;
import nl.utwente.di.visol1.models.Employee;
import nl.utwente.di.visol1.models.Role;

import static nl.utwente.di.visol1.util.TokenUtil.Token.*;

public class TokenUtil {
	private static final String SECURE_RANDOM_ALGORITHM = "SHA1PRNG";
	private static final String SECURE_RANDOM_PROVIDER = "SUN";
	private static final String SECRET_KEY_ALGORITHM = "HmacSHA512";
	private static final String SECRET_KEY_PROVIDER = "SunJCE";
	private static final JsonNodeFactory JSON = JsonNodeFactory.withExactBigDecimals(false);
	private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();
	private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
	private static final int SECRET_SIZE = 64;
	private static final String SECRET = generateSecret();

	private static String generateSecret() {
		SecureRandom random;
		try {
			random = SecureRandom.getInstance(SECURE_RANDOM_ALGORITHM, SECURE_RANDOM_PROVIDER);
		} catch (NoSuchAlgorithmException | NoSuchProviderException ignored) {
			random = new SecureRandom();
		}
		byte[] bytes = new byte[SECRET_SIZE];
		random.nextBytes(bytes);
		return BASE64_ENCODER.encodeToString(bytes);
	}

	public static String generateToken(Employee employee) {
		Instant current = Instant.now();
		Token token = new Token(
			new Header(
				"HS512",
				"JWT"
			),
			new Payload(
				current.getEpochSecond(),
				current.plus(1, ChronoUnit.DAYS).getEpochSecond(),
				// To account for clock skew.
				current.minus(3, ChronoUnit.MINUTES).getEpochSecond(),
				employee.getEmail()
			)
		);
		return token.toBase64() + "." + generateSignature(token);
	}

	public static void main(String[] args) {
		Employee employee = new Employee();
		employee.setEmail("tester@test.test");
		String generatedToken = generateToken(employee);
		System.out.println(generatedToken);
		System.out.println(validateToken(generatedToken));
	}

	private static Response validateToken(String encodedToken) {
		String[] chunks = encodedToken.split("\\.");
		if (chunks.length < 2) {
			return invalidToken("The access token is malformed");
		}

		Token token;
		try {
			token = fromBase64(chunks[0], chunks[1]);
		} catch (IllegalArgumentException exception) {
			return invalidToken("The access token is not valid base64");
		}
		if (token.header.type.equals("JWT")) {
			if (token.header.algorithm.equals("HS512")) {
				String expectedSignature = generateSignature(token);
				if (!expectedSignature.equals(chunks[2])) {
					return invalidToken("The access token has an invalid signature");
				}

				return Response.ok(token).build();
			} else {
				return invalidToken("The access token is encrypted with an unsupported algorithm");
			}
		} else {
			return invalidToken("The access token is not a JWT");
		}
	}

	private static Response invalidToken(String description) {
		return Response.status(Response.Status.UNAUTHORIZED)
			.header("WWW-Authenticate", "Bearer error=\"invalid_token\" error_description=\"" + description + "\"").build();
	}

	private static Response invalidRequest(String description) {
		return Response.status(Response.Status.BAD_REQUEST)
			.header("WWW-Authenticate", "Bearer error=\"invalid_request\" error_description=\"" + description + "\"").build();
	}

	private static String generateSignature(Token token) {
		try {
			SecretKeySpec keySpecification = new SecretKeySpec(SECRET.getBytes(), SECRET_KEY_ALGORITHM);
			Mac mac = Mac.getInstance(SECRET_KEY_ALGORITHM, SECRET_KEY_PROVIDER);
			mac.init(keySpecification);
			return BASE64_ENCODER.encodeToString(mac.doFinal(token.toBase64().getBytes()));
		} catch (NoSuchAlgorithmException | NoSuchProviderException exception) {
			throw new IllegalStateException("Could not find algorithms necessary to generate tokens", exception);
		} catch (InvalidKeyException exception) {
			throw new IllegalStateException("Could not generate token", exception);
		}
	}

	private static Response validate(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header == null) {
			return invalidRequest("The authorization header is missing");
		}
		String[] chunks = header.split(" ", 2);
		if (chunks[0].equals("Bearer")) {
			if (chunks.length != 2) {
				return invalidRequest("The authorization header does not contain exactly one Bearer token");
			}
			Response response = validateToken(chunks[1]);
			if (response.getStatus() == Response.Status.OK.getStatusCode()) {
				Token token = (Token) response.getEntity();
				Instant instant = Instant.now();
				if (instant.isAfter(Instant.ofEpochSecond(token.payload.notBefore)) &&
				    instant.isBefore(Instant.ofEpochSecond(token.payload.expirationTime))) {
					return Response.ok(token).build();
				} else {
					return invalidRequest("The access token has expired");
				}
			} else {
				return response;
			}
		} else {
			return invalidRequest("The authorization header is of an unsupported type");
		}
	}

	@SafeVarargs
	public static Response check(HttpServletRequest request, Function<Employee, Response> authorizedResponse, Supplier<Permission>... permissions) {
		if (Configuration.Authorization.BYPASS.getAsBoolean() && request.getHeader("Authorization") == null) {
			return authorizedResponse.apply(null);
		}

		Response response = validate(request);
		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			Token token = (Token) response.getEntity();
			Employee employee = EmployeeDao.getEmployee(token.payload.employee);
			try {
				if (Arrays.stream(permissions).map(Supplier::get).allMatch(permission -> permission.check(employee))) {
					return authorizedResponse.apply(employee);
				} else {
					return Response.status(Response.Status.FORBIDDEN).build();
				}
			} catch (NullPointerException exception) {
				return Response.status(Response.Status.NOT_FOUND).build();
			} catch (ClassCastException exception) {
				// Supplier "permission" was not actually a permission
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			}
		} else {
			return response;
		}
	}

	public static class Token {
		public final Header header;
		public final Payload payload;

		private Token(Header header, Payload payload) {
			this.header = header;
			this.payload = payload;
		}

		static Token fromBase64(String header, String payload) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				return new Token(
					Header.fromJson(mapper.readTree(BASE64_DECODER.decode(header))),
					Payload.fromJson(mapper.readTree(BASE64_DECODER.decode(payload)))
				);
			} catch (IOException exception) {
				throw new IllegalStateException("Could not parse token", exception);
			}
		}

		@Override
		public String toString() {
			return header.toJson() + "." + payload.toJson();
		}

		String toBase64() {
			return BASE64_ENCODER.encodeToString(header.toJson().getBytes()) + "." + BASE64_ENCODER.encodeToString(payload.toJson().getBytes());
		}

		public static class Header {
			public final String algorithm;
			public final String type;

			private Header(String algorithm, String type) {
				this.algorithm = algorithm;
				this.type = type;
			}

			static Header fromJson(JsonNode json) {
				return new Header(
					json.get("alg").asText(),
					json.get("typ").asText()
				);
			}

			String toJson() {
				return JSON.objectNode().put("alg", algorithm).put("typ", type).toString();
			}
		}

		public static class Payload {
			public final long issuedAt;
			public final long expirationTime;
			public final long notBefore;
			public final String employee;

			private Payload(long issuedAt, long expirationTime, long notBefore, String employee) {
				this.issuedAt = issuedAt;
				this.expirationTime = expirationTime;
				this.notBefore = notBefore;
				this.employee = employee;
			}

			static Payload fromJson(JsonNode json) {
				return new Payload(
					json.get("iat").asLong(),
					json.get("exp").asLong(),
					json.get("nbf").asLong(),
					json.get("employee").asText()
				);
			}

			String toJson() {
				return JSON.objectNode().put("iat", issuedAt).put("exp", expirationTime).put("nbf", notBefore).put("employee", employee).toString();
			}
		}
	}
}
