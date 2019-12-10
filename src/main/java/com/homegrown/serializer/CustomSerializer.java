package com.homegrown.serializer;

import java.util.Map;
import com.homegrown.pojo.CustomObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;


public class CustomSerializer implements Serializer<CustomObject> {
	@Override public void configure(Map<String, ?> configs, boolean isKey) {}
	@Override public void close() {}
	@Override public byte[] serialize(String topic, CustomObject data) {
		byte[] retVal = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			retVal = objectMapper.writeValueAsString(data).getBytes();
		} catch (Exception exception) {
			System.out.println("Error in serializing object" + data);
		}
		return retVal;
	}
}