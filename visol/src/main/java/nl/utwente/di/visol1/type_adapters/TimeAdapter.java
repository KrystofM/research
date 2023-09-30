package nl.utwente.di.visol1.type_adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.IOException;
import java.sql.Time;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class TimeAdapter {

	public static String adapt(Time time) {
		return time == null ? null : time.toString();
	}


	public static Time unadapt(String value) {
		return value == null ? null : Time.valueOf(value);
	}

	public static class XML extends XmlAdapter<String, Time> {

		@Override
		public String marshal(Time time) {
			return adapt(time);
		}

		@Override
		public Time unmarshal(String value) {
			return unadapt(value);
		}
	}

	public static class JSONSerializer extends StdSerializer<Time> {

		public JSONSerializer() {
			this(null);
		}

		public JSONSerializer(Class<Time> type) {
			super(type);
		}

		@Override
		public void serialize(Time value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeString(adapt(value));
		}
	}

	public static class JSONDeserializer extends StdDeserializer<Time> {
		public JSONDeserializer() {
			this(null);
		}

		public JSONDeserializer(Class<Time> type) {
			super(type);
		}

		@Override
		public Time deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
			return unadapt(jsonParser.getValueAsString());
		}
	}
}
