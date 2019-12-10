package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public final class AuthenticationResponseDto extends ResponseDto {

    private User user;

    public User getUser() {return user;}

    public void setUser(User user) {this.user = user;}

    public AuthenticationResponseDto () {}
    public AuthenticationResponseDto(String username, String password) {
        super(username,password);
    }
}
