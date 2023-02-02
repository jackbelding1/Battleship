package edu.msu.team9.proj1.Cloud.Models;

import androidx.annotation.Nullable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import java.util.ArrayList;

import edu.msu.team9.proj1.GameItem;
import edu.msu.team9.proj1.Grid;

@Root(name = "battleship")
public class GameResult {
    @Attribute
    private String status;

    @Attribute(name="gameid", required = false)
    private int gameid;

    @Attribute(name ="gamestate", required = false)
    private String gameState;

    public String getGameState()
    {
        return gameState;
    }

    public void setGameState(String state){
        gameState = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getGameID() {
        return gameid;
    }

    public void setGameID(int gameID) {
        this.gameid = gameID;
    }

    @Attribute(name = "msg", required = false)
    private String message;

    @Element(name = "gameItem", type = GameItem.class, required = false)
    private GameItem game;

    public GameItem getGame(){
        return game;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public GameResult() {}

    public GameResult(String status, @Nullable String msg, int gameId, String state) {
        this.status = status;
        this.message = msg;
        this.gameid = gameId;
        this.gameState = state;
    }
}
