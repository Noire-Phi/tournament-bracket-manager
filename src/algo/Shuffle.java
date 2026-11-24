package algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Fisher-Yates shuffle, used to seed round one. */
public final class Shuffle {
    private Shuffle() {}

    public static <T> List<T> shuffled(List<T> items, Random rnd) {
        List<T> out = new ArrayList<>(items);
        for (int i = out.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            T tmp = out.get(i);
            out.set(i, out.get(j));
            out.set(j, tmp);
        }
        return out;
    }
}
