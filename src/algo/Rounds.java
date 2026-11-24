package algo;

/** Bracket size rules. */
public final class Rounds {
    private Rounds() {}

    /** Recursive: rounds(n) = 0 if n <= 1, else 1 + rounds(n / 2). rounds(16) = 4. */
    public static int rounds(int n) {
        return n <= 1 ? 0 : 1 + rounds(n / 2);
    }

    public static boolean isBracketSize(int n) {
        return n == 4 || n == 8 || n == 16;
    }
}
