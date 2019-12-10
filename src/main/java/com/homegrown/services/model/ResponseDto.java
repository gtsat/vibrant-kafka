package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResponseDto{

    protected String status;
    protected String message;

    public ResponseDto () {}

    public ResponseDto (String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
