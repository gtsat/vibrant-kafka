package com.homegrown.services.model;

import java.sql.Timestamp;
import java.util.List;

public final class BenchmarkSample {
    private String producer;
    private float frequency;
    private List<Byte> signal;
    private Timestamp creationDate;

    public BenchmarkSample (String producer, float frequency, List<Byte> signal, Timestamp creationDate) {
        this.creationDate = creationDate;
        this.frequency = frequency;
        this.producer = producer;
        this.signal = signal;
    }
    public String getProducer() {return producer;}
    public List<Byte> getSignal() {return signal;}
    public float getFrequency() {
        return frequency;
    }
    public Timestamp getCreationDate() {return creationDate;}
}
