package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class SimilaritiesListResponseDto extends ResponseDto {

    private List<Similarity> similarities;

    public SimilaritiesListResponseDto(){}
    public SimilaritiesListResponseDto(String status, String message) {
        super(status,message);
    }

    public List<Similarity> getSimilarities() {
        return similarities;
    }

    public void setSimilarities(List<Similarity> similarities) {
        this.similarities = similarities;
    }
}
