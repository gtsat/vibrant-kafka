package com.homegrown.partitioner;

import java.util.Map;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.clients.producer.Partitioner;

public class CustomPartitioner implements Partitioner{
	private static final int PARTITION_COUNT=50;
	@Override public void configure(Map<String,?> configs) {}
	@Override public void close() {}
	@Override public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
		//Integer keyInt = Integer.parseInt(key.toString());
		Integer keyInt = key.toString().hashCode();
		return keyInt % PARTITION_COUNT;
	}
}
