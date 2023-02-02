package edu.msu.team9.proj1.Cloud.Models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "battleshipuser")
public class UserAccount {
    @Attribute
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Attribute
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    @Attribute
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserAccount() {}

    public UserAccount(Integer id, String username, String password){
        this.id = id;
        this.username = username;
        this.password = password;
    }
}
