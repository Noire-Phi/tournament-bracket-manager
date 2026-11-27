package algo;

import java.util.List;
import model.Match;
import model.Player;

/** Hand-written searches. */
public final class Searches {
    private Searches() {}

    /** Linear search by name, ignoring case. Returns null when nothing matches. */
    public static Player linearByName(List<Player> list, String name) {
        for (Player p : list) {
            if (p.name.equalsIgnoreCase(name.trim())) return p;
        }
        return null;
    }

    /** Linear search that keeps every player whose name contains the text, ignoring case. Empty text keeps all. */
    public static List<Player> linearContains(List<Player> list, String text) {
        String needle = text.trim().toLowerCase();
        List<Player> found = new java.util.ArrayList<>();
        for (Player p : list) {
            if (p.name.toLowerCase().contains(needle)) found.add(p);
        }
        return found;
    }

    /** Binary search by ID. The list must already be sorted by ID. */
    public static Match binaryById(List<Match> sorted, int id) {
        int lo = 0, hi = sorted.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int v = sorted.get(mid).id;
            if (v == id) return sorted.get(mid);
            if (v < id) lo = mid + 1;
            else hi = mid - 1;
        }
        return null;
    }
}
