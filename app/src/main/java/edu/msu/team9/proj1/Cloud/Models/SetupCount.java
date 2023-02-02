package edu.msu.team9.proj1.Cloud.Models;

import androidx.annotation.Nullable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "battleship")
public class SetupCount {
    @Attribute
    private String status;

    @Attribute(required = false)
    private int first;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPosition() {
        return first;
    }

    public void setPosition(int position) {
        this.first = position;
    }

    @Attribute(name = "msg", required = false)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Attribute(name = "gameready", required = false)
    private int gameready;

    public int getSetupCount() {
        return gameready;
    }

    public void setSetupCount(int gameready) {
        this.gameready = gameready;
    }

    public SetupCount() {}

    public SetupCount(String status, @Nullable String msg, int gameready, int first) {
        this.status = status;
        this.message = msg;
        this.gameready = gameready;
        this.first = first;
    }
}
