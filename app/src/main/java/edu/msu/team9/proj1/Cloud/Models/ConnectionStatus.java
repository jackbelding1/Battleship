package edu.msu.team9.proj1.Cloud.Models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(name = "battleship")
public class ConnectionStatus {
    @Attribute(name = "status", required = false)
    private String status;

    @Attribute(name = "msg", required = false)
    private String msg;

    @Attribute(name="connected", required = true)
    private String connected;

    public String getMessage() {
        return this.msg;
    }

    public void setMessage(String message) {
        this.msg = message;
    }

    public void setConnectionStatus(String state) {
        this.connected = state;
    }
    public String getConnectionStatus() {
        return this.connected;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ConnectionStatus() {}

    public ConnectionStatus(String status, String msg, String state) {
        this.status = status;
        this.msg = msg;
        this.connected = state;
    }
}
