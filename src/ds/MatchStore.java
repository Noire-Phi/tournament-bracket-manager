package ds;

import algo.Searches;
import algo.Sorts;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import model.Match;

/** All matches, in a LinkedList kept in bracket order. */
public class MatchStore {
    private final LinkedList<Match> list = new LinkedList<>();

    public void add(Match m) {
        list.add(m);
    }

    public boolean remove(Match m) {
        return list.remove(m);
    }

    /** Binary search by ID. Sorts a copy by ID first, since the store keeps bracket order. */
    public Match findById(int id) {
        // shortcut: sorts a copy on every lookup, fine for 15 matches, upgrade to a cached copy for larger brackets
        List<Match> byId = Sorts.mergeSort(new ArrayList<>(list), (x, y) -> Integer.compare(x.id, y.id));
        return Searches.binaryById(byId, id);
    }

    public List<Match> list() {
        return Collections.unmodifiableList(list);
    }

    public int size() {
        return list.size();
    }
}
