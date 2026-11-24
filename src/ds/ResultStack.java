package ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import model.Match;
import model.Player;

/** Results in the order they were entered. The top entry is the newest, and Undo pops it. */
public class ResultStack {

    /** Everything Undo needs to reverse one result. */
    public static class Entry {
        public final Match match;
        public final Match.Status prevStatus;
        public final Player winner;
        public final Player loser;

        public Entry(Match match, Match.Status prevStatus, Player winner, Player loser) {
            this.match = match;
            this.prevStatus = prevStatus;
            this.winner = winner;
            this.loser = loser;
        }
    }

    private final Stack<Entry> stack = new Stack<>();

    public void push(Entry e) {
        stack.push(e);
    }

    public Entry peek() {
        return stack.peek();
    }

    public Entry pop() {
        return stack.pop();
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public int size() {
        return stack.size();
    }

    /** Newest first, for the History page. */
    public List<Entry> newestFirst() {
        List<Entry> copy = new ArrayList<>(stack);
        Collections.reverse(copy);
        return copy;
    }
}
