package algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Player;

/** Hand-written sorts. None of them calls Collections.sort. */
public final class Sorts {
    private Sorts() {}

    /**
     * Points, highest first. Selection sort on points, then insertion sort by name
     * inside each block of equal points.
     */
    public static void byPoints(List<Player> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int best = i;
            for (int j = i + 1; j < n; j++) {
                if (list.get(j).points > list.get(best).points) best = j;
            }
            Player tmp = list.get(i);
            list.set(i, list.get(best));
            list.set(best, tmp);
        }
        int start = 0;
        while (start < n) {
            int end = start;
            while (end + 1 < n && list.get(end + 1).points == list.get(start).points) end++;
            insertionByName(list, start, end);
            start = end + 1;
        }
    }

    private static void insertionByName(List<Player> list, int from, int to) {
        for (int i = from + 1; i <= to; i++) {
            Player key = list.get(i);
            int j = i - 1;
            while (j >= from && list.get(j).name.compareToIgnoreCase(key.name) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    /** Merge sort. Returns a new sorted list and leaves the input alone. Stable. */
    public static <T> List<T> mergeSort(List<T> items, Comparator<? super T> cmp) {
        if (items.size() <= 1) return items;
        int mid = items.size() / 2;
        List<T> left = mergeSort(new ArrayList<>(items.subList(0, mid)), cmp);
        List<T> right = mergeSort(new ArrayList<>(items.subList(mid, items.size())), cmp);
        List<T> out = new ArrayList<>(items.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) out.add(left.get(i++));
            else out.add(right.get(j++));
        }
        while (i < left.size()) out.add(left.get(i++));
        while (j < right.size()) out.add(right.get(j++));
        return out;
    }
}
