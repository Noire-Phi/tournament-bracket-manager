package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/** Design tokens from UI_GUIDE.md. Screens take every color, font and duration from here. */
public final class Theme {
    private Theme() {}

    public static final Color BG = new Color(0x070A1C);
    public static final Color PANEL = new Color(0x11163A);
    public static final Color PANEL_DEEP = new Color(0x0B0F2E);
    public static final Color RAISE = new Color(0x1B2256);
    public static final Color INK = Color.WHITE;
    public static final Color INK_MUTED = new Color(0xAEB7E6);
    public static final Color LINE = new Color(0x2B3170);
    public static final Color PRIMARY = new Color(0xFF3B5C);
    public static final Color ON_PRIMARY = new Color(0x0A0F2C);
    public static final Color WIN = new Color(0x2DE2E6);
    public static final Color GOLD = new Color(0xFFC53D);
    public static final Color DANGER_TEXT = new Color(0xFF8A9C);
    public static final Color FOCUS = new Color(0xFFC53D);
    /** Primary button hover (preview: #FF5A76). */
    public static final Color PRIMARY_HOVER = new Color(0xFF5A76);
    /** PRIMARY at 16% over PANEL, kept opaque so table renderers repaint cleanly. */
    public static final Color SELECTION = mix(PRIMARY, PANEL, 0.16);

    /** Control and table row height, and the spacing scale. */
    public static final int CONTROL_H = 44;
    public static final int SPACE_XS = 4;
    public static final int SPACE_S = 8;
    public static final int SPACE_M = 12;
    public static final int SPACE_L = 16;
    public static final int SPACE_XL = 24;
    /** Card padding, border width and the selection bar width. */
    public static final int CARD_PAD = 18;
    public static final int BORDER = 2;
    public static final int SELECTION_BAR = 5;

    /** Motion durations in milliseconds. */
    public static final int MOTION_FAST_MS = 150;
    public static final int MOTION_MS = 260;
    public static final int MOTION_SLOW_MS = 420;

    private static final String DISPLAY_FAMILY = pick("Barlow Condensed", "Arial Narrow", Font.SANS_SERIF);

    /** Condensed display face for titles, numbers, buttons and table headers. */
    public static Font display(float size) {
        return new Font(DISPLAY_FAMILY, Font.BOLD, 1).deriveFont(size);
    }

    public static Font body(float size) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, 1).deriveFont(size);
    }

    public static Font mono(float size) {
        return new Font(Font.MONOSPACED, Font.PLAIN, 1).deriveFont(size);
    }

    private static Color mix(Color top, Color under, double amount) {
        return new Color(
                (int) Math.round(top.getRed() * amount + under.getRed() * (1 - amount)),
                (int) Math.round(top.getGreen() * amount + under.getGreen() * (1 - amount)),
                (int) Math.round(top.getBlue() * amount + under.getBlue() * (1 - amount)));
    }

    private static String pick(String... families) {
        String[] have = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String f : families) {
            if (Arrays.asList(have).contains(f)) return f;
        }
        return Font.SANS_SERIF;
    }
}
