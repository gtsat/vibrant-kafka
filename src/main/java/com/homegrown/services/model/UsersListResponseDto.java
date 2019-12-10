package com.homegrown.services.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class UsersListResponseDto extends ResponseDto {

    private List<User> users;

    public UsersListResponseDto(){}
    public UsersListResponseDto(String status, String message) {
        super(status,message);
    }

    public List<User> getUsers() {return users;}
    public void setUsers(List<User> users) {this.users = users;}
}
