package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.TreeMap;

@XmlRootElement
public class SimilaritiesResponseDto extends ResponseDto {

    private TreeMap<String,double[]> trains;
    private TreeMap<String,double[]> tests;
    private TreeMap<String,double[]> trainFFTs;
    private TreeMap<String,double[]> testFFTs;
    private TreeMap<String,Date> timestamps;
    private TreeMap<String,Date> benchmarks;
    private TreeMap<String,Integer> similarities;
    private TreeMap<String,Float> frequencies;

    public SimilaritiesResponseDto(){}
    public SimilaritiesResponseDto(String status, String message) {
        super(status,message);
    }

    public TreeMap<String, double[]> getTrainFFTs() {
        return trainFFTs;
    }

    public void setTrainFFTs(TreeMap<String, double[]> trainFFTs) {
        this.trainFFTs = trainFFTs;
    }

    public TreeMap<String, double[]> getTestFFTs() {
        return testFFTs;
    }

    public void setTestFFTs(TreeMap<String, double[]> testFFTs) {
        this.testFFTs = testFFTs;
    }

    public TreeMap<String, Date> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(TreeMap<String,Date> timestamps) {
        this.timestamps = timestamps;
    }

    public TreeMap<String, Integer> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(TreeMap<String, Integer> similarities) {
        this.similarities = similarities;
    }

    public TreeMap<String, Date> getBenchmarks() {return benchmarks;}

    public void setBenchmarks(TreeMap<String, Date> benchmarks) {this.benchmarks = benchmarks;}

    public TreeMap<String, double[]> getTrains() {return trains;}

    public void setTrains(TreeMap<String, double[]> trains) {this.trains = trains;}

    public TreeMap<String, double[]> getTests() {return tests;}

    public void setTests(TreeMap<String, double[]> tests) {this.tests = tests;}

    public TreeMap<String, Float> getFrequencies() {return frequencies;}

    public void setFrequencies(TreeMap<String, Float> frequencies) {this.frequencies = frequencies;}
}
