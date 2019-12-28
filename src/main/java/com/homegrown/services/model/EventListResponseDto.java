package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class EventListResponseDto extends ResponseDto {

    private List<Event> events;

    public EventListResponseDto(){}
    public EventListResponseDto(String status, String message) {
        super(status,message);
    }
    public void setEvents(List<Event> events) {this.events = events;}
    public List<Event> getEvents() {return events;}
}
