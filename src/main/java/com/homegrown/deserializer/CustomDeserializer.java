package com.homegrown.deserializer;

import java.util.Map;
import com.homegrown.pojo.CustomObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;


public class CustomDeserializer implements Deserializer<CustomObject> {
	@Override public void configure(Map<String, ?> configs, boolean isKey) {}
	@Override public void close() {}
	@Override public CustomObject deserialize(String topic, byte[] data) {
		ObjectMapper mapper = new ObjectMapper();
		CustomObject object = null;
		try {
			object = mapper.readValue(data, CustomObject.class);
		} catch (Exception exception) {
			System.out.println("Error in deserializing bytes " + exception);
		}
		return object;
	}
}