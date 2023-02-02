package edu.msu.team9.proj1.Cloud.Models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "battleship")
public class UserStatus {
    @Attribute(name = "status", required = false)
    private String status;

    @Attribute(name = "msg", required = false)
    private String msg;

    @Attribute(name="uid", required = false)
    private int userid;

    public String getMessage() {
        return msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    public void setUserId(Integer id) {
        this.userid = id;
    }

    public int getUserId() {
        return userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserStatus() {}

    public UserStatus(String status, String msg) {
        this.status = status;
        this.msg = msg;
        //this.userid = userid;
    }
}
