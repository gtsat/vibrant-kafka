package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.Timestamp;
import java.util.TreeMap;

@XmlRootElement
public class KafkaConsumerResponseDto extends ResponseDto {

    private TreeMap<String,double[]> trains;
    private TreeMap<String,double[]> tests;
    private TreeMap<String,double[]> trainFFTs;
    private TreeMap<String,double[]> testFFTs;
    private TreeMap<String,String> timestamps;
    private TreeMap<String,String> benchmarks;
    private TreeMap<String,Integer> similarities;
    private TreeMap<String,String> categories;
    private TreeMap<String,Float> frequencies;
    private TreeMap<String,String> motionUrls;

    public KafkaConsumerResponseDto(){}
    public KafkaConsumerResponseDto(String status, String message) {
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

    public TreeMap<String,String> getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(TreeMap<String,String> timestamps) {
        this.timestamps = timestamps;
    }

    public TreeMap<String, Integer> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(TreeMap<String, Integer> similarities) {
        this.similarities = similarities;
    }

    public TreeMap<String,String> getBenchmarks() {return benchmarks;}

    public void setBenchmarks(TreeMap<String,String> benchmarks) {this.benchmarks = benchmarks;}

    public TreeMap<String, double[]> getTrains() {return trains;}

    public void setTrains(TreeMap<String, double[]> trains) {this.trains = trains;}

    public TreeMap<String, double[]> getTests() {return tests;}

    public void setTests(TreeMap<String, double[]> tests) {this.tests = tests;}

    public TreeMap<String, Float> getFrequencies() {return frequencies;}

    public void setFrequencies(TreeMap<String, Float> frequencies) {this.frequencies = frequencies;}

    public TreeMap<String, String> getCategories() {return categories;}

    public void setCategories(TreeMap<String, String> categories) {this.categories = categories;}

    public TreeMap<String, String> getMotionUrls() {return motionUrls;}

    public void setMotionUrls(TreeMap<String, String> motionUrls) {this.motionUrls = motionUrls;}
}
