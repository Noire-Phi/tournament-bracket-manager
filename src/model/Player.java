package model;

public class Player {
    public final int id;
    public String name;
    public int points, wins, losses;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
