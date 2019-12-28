package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class Event implements Serializable {

    private long offset;
    private String producer;
    private String creationDate;
    private int similarity;

    public Event() {}
    public long getOffset() {return offset;}
    public void setOffset(long offset) {this.offset = offset;}
    public int getSimilarity() {return similarity;}
    public void setSimilarity(int similarity) {this.similarity = similarity;}
    public String getProducer() {
        return producer;
    }
    public void setProducer(String producer) {
        this.producer = producer;
    }
    public String getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }
}
