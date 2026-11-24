package ds;

import algo.Searches;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.Player;

/** All players, in an ArrayList. */
public class PlayerStore {
    private final ArrayList<Player> list = new ArrayList<>();
    private int nextId = 1;

    public Player add(String name) {
        Player p = new Player(nextId++, name);
        list.add(p);
        return p;
    }

    public boolean remove(Player p) {
        return list.remove(p);
    }

    public Player findByName(String name) {
        return Searches.linearByName(list, name);
    }

    public Player findById(int id) {
        for (Player p : list) {
            if (p.id == id) return p;
        }
        return null;
    }

    public List<Player> list() {
        return Collections.unmodifiableList(list);
    }

    public int size() {
        return list.size();
    }
}
