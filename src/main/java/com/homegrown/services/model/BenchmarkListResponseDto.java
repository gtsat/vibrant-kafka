package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class BenchmarkListResponseDto extends ResponseDto {
    private List<BenchmarkSample> benchmarks;

    public BenchmarkListResponseDto(){}
    public BenchmarkListResponseDto(String status, String message) {super(status,message);}
    public void setBenchmarks(List<BenchmarkSample> benchmarks) {this.benchmarks = benchmarks;}
    public List<BenchmarkSample> getBenchmarks() {return benchmarks;}

}
