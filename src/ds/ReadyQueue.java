package ds;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import model.Match;

/** Matches that are ready to play, first in first out. Start Next takes the head. */
public class ReadyQueue {
    private final Queue<Match> queue = new LinkedList<>();

    public void offer(Match m) {
        queue.offer(m);
    }

    public Match poll() {
        return queue.poll();
    }

    public Match peek() {
        return queue.peek();
    }

    public boolean remove(Match m) {
        return queue.remove(m);
    }

    public int size() {
        return queue.size();
    }

    public List<Match> list() {
        return new ArrayList<>(queue);
    }
}
