package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
public class RequestDto implements Serializable{

    protected String username;
    protected String password;

    protected String firstname;
    protected String lastname;

    protected String msisdn;
    protected String email;
    protected String region;

    protected String toActivate;
    protected String toDisable;

    protected String producer;
    protected Integer threshold;
    protected Integer limit;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {return firstname;}

    public void setFirstname(String firstname) {this.firstname = firstname;}

    public String getLastname() {return lastname;}

    public void setLastname(String lastname) {this.lastname = lastname;}

    public String getMsisdn() {return msisdn;}

    public void setMsisdn(String msisdn) {this.msisdn = msisdn;}

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}

    public String getToActivate() {return toActivate;}

    public void setToActivate(String toActivate) {this.toActivate = toActivate;}

    public String getToDisable() {return toDisable;}

    public void setToDisable(String toDisable) {this.toDisable = toDisable;}

    public String getRegion() {return region;}

    public void setRegion(String region) {this.region = region;}

    public String getProducer() {return producer;}

    public void setProducer(String producer) {this.producer = producer;}

    public Integer getThreshold() {return threshold;}

    public void setThreshold(Integer threshold) {this.threshold = threshold;}

    public Integer getLimit() {return limit;}

    public void setLimit(Integer limit) {this.limit = limit;}
}
