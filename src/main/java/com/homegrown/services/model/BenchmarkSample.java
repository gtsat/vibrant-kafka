package com.homegrown.services.model;

import java.sql.Timestamp;
import java.util.List;

public final class BenchmarkSample {
    private long offset;
    private String producer;
    private float frequency;
    private List<Byte> signal;
    private String creationDate;

    public BenchmarkSample (String producer, float frequency, List<Byte> signal, long offset, String creationDate) {
        this.creationDate = creationDate;
        this.frequency = frequency;
        this.producer = producer;
        this.offset = offset;
        this.signal = signal;
    }
    public BenchmarkSample (){}
    public long getOffset() {return offset;}
    public String getProducer() {return producer;}
    public List<Byte> getSignal() {return signal;}
    public float getFrequency() {return frequency;}
    public String getCreationDate() {return creationDate;}
}
