package edu.msu.team9.proj1;

import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "battleship")
public final class Catalog {
    @Attribute(name="status", required = false)
    private String status;

    @ElementList(name="gameItem", inline = true, required = false, type = GameItem.class)
    private List<GameItem> items;

    @Attribute(name= "msg", required = false)
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<GameItem> getItems() {
        return items;
    }

    public void setItems(List<GameItem> items) {
        this.items = items;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Catalog(String status, ArrayList<GameItem> items, @Nullable String msg) {
        this.status = status;
        this.items = items;
        this.message = msg;
    }

    public Catalog() {
    }
}
