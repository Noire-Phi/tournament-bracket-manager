package algo;

import java.util.List;
import model.Player;

/** Standings ranks. Tied players share a rank (1, 1, 1, 4 ...). */
public final class Ranks {
    public static final int LEADERS = 1;
    public static final int NEXT = 2;
    public static final int REST = 0;

    private Ranks() {}

    /** Shared ranks for a list already sorted by points, highest first. */
    public static int[] of(List<Player> sorted) {
        int[] rank = new int[sorted.size()];
        for (int i = 0; i < sorted.size(); i++) {
            boolean tied = i > 0 && sorted.get(i).points == sorted.get(i - 1).points;
            rank[i] = tied ? rank[i - 1] : i + 1;
        }
        return rank;
    }

    /** LEADERS for the top points, NEXT for the next lower points, REST otherwise. No group without a point. */
    public static int group(List<Player> sorted, int i) {
        int points = sorted.get(i).points;
        if (points <= 0) return REST;
        int top = sorted.get(0).points;
        if (points == top) return LEADERS;
        for (Player p : sorted) {
            if (p.points < top) return p.points == points ? NEXT : REST;
        }
        return REST;
    }
}
