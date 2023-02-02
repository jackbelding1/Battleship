package edu.msu.team9.proj1;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(name = "gameItem")
public final class GameItem {
    @Attribute
    private int id;

    @Attribute(name="username")
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GameItem(int id, String name) {
        this.name = name;
        this.id = id;
    }

    public GameItem() {}

}
