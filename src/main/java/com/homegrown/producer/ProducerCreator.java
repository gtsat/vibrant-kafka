package com.homegrown.producer;

import java.util.Properties;

import com.homegrown.serializer.CustomSerializer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;

public class ProducerCreator {
	public static Producer<String,byte[]> createProducer(String brokers, String id) {
		Properties props = new Properties();
		props.put(ProducerConfig.CLIENT_ID_CONFIG, id);
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
		return new KafkaProducer<>(props);
	}
}
