package edu.msu.team9.proj1.Cloud.Models;

import androidx.annotation.Nullable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "battleshipgame")
public class PlayerTurn {
    @Attribute
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Attribute(name = "msg", required = false)
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Attribute(name = "turn", required = false)
    private int turn;

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public PlayerTurn() {}

    public PlayerTurn(String status, @Nullable String msg, int turn) {
        this.status = status;
        this.message = msg;
        this.turn = turn;
    }
}
