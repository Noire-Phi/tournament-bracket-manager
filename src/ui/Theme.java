package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/** Design tokens from UI_GUIDE.md. Screens take every color, font and duration from here. */
public final class Theme {
    private Theme() {}

    // Bento: one gray scale (BG, PANEL, PANEL_DEEP, RAISE/LINE) and amber as the only accent.
    public static final Color BG = new Color(0x141516);
    public static final Color PANEL = new Color(0x1F2123);
    public static final Color PANEL_DEEP = new Color(0x2A2D30);
    public static final Color RAISE = new Color(0x383C41);
    public static final Color INK = new Color(0xECEBE6);
    public static final Color INK_MUTED = new Color(0xA4A8AC);
    public static final Color LINE = new Color(0x34383C);
    public static final Color PRIMARY = new Color(0xFFB000);
    public static final Color ON_PRIMARY = new Color(0x141516);
    public static final Color WIN = new Color(0xFFB000);
    public static final Color GOLD = new Color(0xFFB000);
    public static final Color DANGER_TEXT = new Color(0xFF8A7E);
    public static final Color FOCUS = new Color(0xFFB000);
    /** Fully transparent: the clear corners around a rounded, undecorated window. */
    public static final Color CLEAR = new Color(0, 0, 0, 0);
    /** Match in progress: the soft red, kept apart from amber so it never reads as a winner. */
    public static final Color LIVE = new Color(0xFF8A7E);
    /** Ready pill and ready bracket border: off-white with ON_PRIMARY text. */
    public static final Color READY_FILL = INK;
    /** Pill tints: Played (amber) and In progress (LIVE) at 16% over PANEL. */
    public static final Color PLAYED_TINT = mix(WIN, PANEL, 0.16);
    public static final Color LIVE_TINT = mix(LIVE, PANEL, 0.16);
    /** Primary button hover. */
    public static final Color PRIMARY_HOVER = new Color(0xFFC23D);
    /** PRIMARY at 10% over PANEL_DEEP (muted text on it stays above 4.5:1) (tables sit on the inner surface), kept opaque so renderers repaint cleanly. */
    public static final Color SELECTION = mix(PRIMARY, PANEL_DEEP, 0.10);
    /** WIN at 10% over PANEL_DEEP: the winner row on a bracket card. */
    public static final Color WIN_TINT = mix(WIN, PANEL_DEEP, 0.10);

    /** Type scale (UI_GUIDE section 3). Screens pass one of these to body() or display(), never a raw number. */
    public static final float SIZE_S = 14;    // labels, IDs, captions, helper text
    public static final float SIZE_M = 16;    // body, tables, fields, buttons
    public static final float SIZE_L = 20;    // tile titles, names in cards
    public static final float SIZE_XL = 32;   // section numbers, KPI values
    public static final float SIZE_XXL = 44;  // page titles, the names in the hero tile
    public static final float SIZE_BIG = 112; // the round number

    /** Control and table row height, and the spacing scale. */
    public static final int CONTROL_H = 44;
    public static final int SPACE_XS = 4;
    public static final int SPACE_S = 8;
    public static final int SPACE_M = 12;
    public static final int SPACE_L = 16;
    public static final int SPACE_XL = 24;
    /** Card padding, border width and the selection bar width. */
    public static final int CARD_PAD = 24;
    public static final int BORDER = 1;
    public static final int SELECTION_BAR = 4;

    /** Motion durations in milliseconds. */
    public static final int MOTION_FAST_MS = 150;
    public static final int MOTION_MS = 260;
    public static final int MOTION_SLOW_MS = 420;

    private static final String DISPLAY_FAMILY = pick("Bricolage Grotesque", "Segoe UI", Font.SANS_SERIF);
    private static final String BODY_FAMILY = pick("DM Sans", "Segoe UI", Font.SANS_SERIF);

    // Fonts are built once per size: renderers and paint code ask for the same few sizes over and over.
    private static final java.util.Map<Float, Font> DISPLAY = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<Float, Font> BODY = new java.util.concurrent.ConcurrentHashMap<>();

    /** Display face for titles, big numbers, tile titles and buttons. */
    public static Font display(float size) {
        return DISPLAY.computeIfAbsent(size, s -> new Font(DISPLAY_FAMILY, Font.BOLD, 1).deriveFont(s));
    }

    public static Font body(float size) {
        return BODY.computeIfAbsent(size, s -> new Font(BODY_FAMILY, Font.PLAIN, 1).deriveFont(s));
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
