package nl.utwente.di.visol1.type_adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TimestampAdapter {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static String adapt(Timestamp timestamp) {
		return timestamp == null ? null : formatter.format(timestamp.toLocalDateTime().atOffset(ZoneOffset.UTC));
	}

	public static Timestamp unadapt(String value) {
		return value == null ? null : Timestamp.from(formatter.parse(value, Instant::from));
	}

	public static Timestamp unadaptOrElse(String value, Timestamp defaultValue) {
		return Optional.ofNullable(unadapt(value)).orElse(defaultValue);
	}

	public static class XML extends XmlAdapter<String, Timestamp> {

		@Override
		public String marshal(Timestamp timestamp) {
			return adapt(timestamp);
		}

		@Override
		public Timestamp unmarshal(String value) {
			return unadapt(value);
		}
	}

	public static class JSONSerializer extends StdSerializer<Timestamp> {
		public JSONSerializer() {
			this(null);
		}

		public JSONSerializer(Class<Timestamp> type) {
			super(type);
		}

		@Override
		public void serialize(Timestamp value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeString(adapt(value));
		}
	}

	public static class JSONDeserializer extends StdDeserializer<Timestamp> {
		public JSONDeserializer() {
			this(null);
		}

		public JSONDeserializer(Class<Timestamp> type) {
			super(type);
		}

		@Override
		public Timestamp deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			return unadapt(jsonParser.getValueAsString());
		}
	}
}
