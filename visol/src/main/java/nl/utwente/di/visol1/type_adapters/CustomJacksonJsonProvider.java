package nl.utwente.di.visol1.type_adapters;

import javax.annotation.Priority;
import javax.ws.rs.ext.Provider;
import java.sql.Time;
import java.sql.Timestamp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

@Provider
@Priority(1)
public class CustomJacksonJsonProvider extends JacksonJaxbJsonProvider {
	public static final ObjectMapper MAPPER = createMapper();

	public CustomJacksonJsonProvider() {
		super(MAPPER, DEFAULT_ANNOTATIONS);
	}

	private static ObjectMapper createMapper() {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule module = new SimpleModule();
		module.addSerializer(Time.class, new TimeAdapter.JSONSerializer());
		module.addDeserializer(Time.class, new TimeAdapter.JSONDeserializer());
		module.addSerializer(Timestamp.class, new TimestampAdapter.JSONSerializer());
		module.addDeserializer(Timestamp.class, new TimestampAdapter.JSONDeserializer());
		mapper.registerModule(module);
		return mapper;
	}
}
