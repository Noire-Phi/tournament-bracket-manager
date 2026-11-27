package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Tournament;

/**
 * The window after login (UI_GUIDE section 4): a header row with the brand on the left, the menu pill bar
 * in the middle and the sport switch and Exit on the right; the page deck (CardLayout) below; status label at the bottom.
 */
public class MainFrame extends JFrame implements Page.Host {
    private static final String[] KEYS = {"Dashboard", "Players", "Bracket", "Matches", "Standings", "History"};
    private static final int SIDE_PAD = 40;
    private static final int TOP_PAD = 24;
    private static final int BAR_PAD = 4;

    private final Tournament tournament;
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, NavButton> navs = new LinkedHashMap<>();
    private final JLabel brandName = new JLabel("Bracket Manager");
    private final JLabel brandSub = new JLabel();
    private final JLabel status = new JLabel();
    private final SportSwitch sportSwitch;
    private final JPanel header = new JPanel(new BorderLayout(Theme.SPACE_XL, Theme.SPACE_M));
    private final JPanel brand = new JPanel();
    private final JPanel navHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    private final JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.SPACE_M, 0));
    private String current = KEYS[0];
    private boolean navOnOwnRow;

    public MainFrame(Tournament tournament) {
        super("Tournament Bracket Manager");
        this.tournament = tournament;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApp();
            }

            @Override
            public void windowOpened(WindowEvent e) {
                pages.get(current).opened();
            }
        });
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        sportSwitch = new SportSwitch(tournament, mode -> {
            refreshAll();
            notify("Sport mode: " + mode.label, false);
        });

        deck.setOpaque(false);
        for (String key : KEYS) {
            Page page = createPage(key);
            pages.put(key, page);
            deck.add(page, key);
        }

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XL, SIDE_PAD, Theme.SPACE_XL, SIDE_PAD));
        center.add(deck, BorderLayout.CENTER);

        status.setOpaque(true);
        status.setBackground(Theme.PANEL);
        status.setForeground(Theme.INK_MUTED);
        status.setFont(Theme.body(Theme.SIZE_S));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(Theme.BORDER, 0, 0, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(Theme.SPACE_S, SIDE_PAD, Theme.SPACE_S, SIDE_PAD)));
        // No fixed accessible name: a label's name is its text, so a screen reader reads each new message.
        status.getAccessibleContext().setAccessibleDescription("Status");

        add(buildHeader(), BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(1000, 640));
        setSize(1440, 900);
        setLocationRelativeTo(null);
        // UI_GUIDE section 5: the frame tells every page its width, and pages re-flow (Dashboard: tiles or one column).
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                placeNav();
                pages.values().forEach(p -> p.resized(getWidth()));
            }
        });
        pages.values().forEach(p -> p.resized(getWidth()));
        FocusReveal.install(this);
        refreshAll();
        placeNav();
        select(KEYS[0]);
    }

    private Page createPage(String key) {
        if (key.equals("Dashboard")) return new DashboardPanel(tournament, this);
        if (key.equals("Players")) return new PlayersPanel(tournament, this);
        if (key.equals("Matches")) return new MatchesPanel(tournament, this);
        if (key.equals("Bracket")) return new BracketPanel(tournament, this);
        if (key.equals("Standings")) return new StandingsPanel(tournament, this);
        if (key.equals("History")) return new HistoryPanel(tournament, this);
        throw new IllegalArgumentException("No page called " + key);
    }

    private JPanel buildHeader() {
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(TOP_PAD, SIDE_PAD, 0, SIDE_PAD));

        brandName.setFont(Theme.display(Theme.SIZE_L));
        brandName.setForeground(Theme.INK);
        // A few pixels of slack: antialiased glyphs can paint a little past the measured width.
        brandName.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, Theme.SPACE_XS));
        brandSub.setFont(Theme.body(Theme.SIZE_S));
        brandSub.setForeground(Theme.INK_MUTED);
        JPanel words = new JPanel(new BorderLayout());
        words.setOpaque(false);
        words.add(brandName, BorderLayout.NORTH);
        words.add(brandSub, BorderLayout.SOUTH);
        brand.setOpaque(false);
        brand.setLayout(new BorderLayout(Theme.SPACE_M, 0));
        brand.add(new Glyphs.Logo(Theme.CONTROL_H), BorderLayout.WEST);
        brand.add(words, BorderLayout.CENTER);

        JPanel bar = new PillBar();
        bar.getAccessibleContext().setAccessibleName("Modules");
        for (String key : KEYS) {
            NavButton b = new NavButton(key);
            b.addActionListener(e -> show(key));
            navs.put(key, b);
            bar.add(b);
        }
        navHolder.setOpaque(false);
        navHolder.add(bar);

        StyledButton exit = new StyledButton("Exit", StyledButton.Kind.DANGER);
        exit.addActionListener(e -> exitApp());
        right.setOpaque(false);
        right.add(sportSwitch);
        right.add(exit);

        header.add(brand, BorderLayout.WEST);
        header.add(navHolder, BorderLayout.CENTER);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /** One row when the brand, the menu and the right-hand controls fit; otherwise the menu moves to its own row. */
    private void placeNav() {
        int needed = brand.getPreferredSize().width + navHolder.getPreferredSize().width
                + right.getPreferredSize().width + 2 * Theme.SPACE_XL + 2 * SIDE_PAD;
        boolean ownRow = needed > getWidth();
        if (ownRow == navOnOwnRow) return;
        navOnOwnRow = ownRow;
        header.remove(navHolder);
        header.add(navHolder, ownRow ? BorderLayout.SOUTH : BorderLayout.CENTER);
        header.revalidate();
    }

    /** Opens a page from the menu: switches to it, then plays its entrance. */
    private void show(String key) {
        select(key);
        pages.get(key).opened();
    }

    /** Switches to a page without its entrance (the window-opened event plays that for the first page). */
    private void select(String key) {
        current = key;
        cards.show(deck, key);
        navs.forEach((k, b) -> b.setActive(k.equals(key)));
        pages.get(key).refresh();
        updateStatus();
    }

    /** Re-reads labels and data everywhere. Used when the sport mode changes. */
    private void refreshAll() {
        brandSub.setText(tournament.name);
        header.revalidate();
        sportSwitch.sync();
        for (String key : KEYS) {
            Page p = pages.get(key);
            navs.get(key).setText(p.title());
            p.refresh();
        }
        placeNav();
        updateStatus();
    }

    private void updateStatus() {
        status.setText(tournament.name + "  ·  " + tournament.sport.label + "  ·  "
                + tournament.players().size() + " " + tournament.sport.entity.toLowerCase() + "s  ·  "
                + tournament.matches().size() + " matches");
    }

    private void exitApp() {
        if (Confirm.ask(this, "Exit", "Exit the application? Records live only in memory, so they are lost.", "Exit")) {
            dispose();
            System.exit(0);
        }
    }

    @Override
    public void exit() {
        exitApp();
    }

    @Override
    public void notify(String message, boolean error) {
        Toast.show(getRootPane(), message, error);
        status.setText(message);
    }

    /** The menu: a PANEL pill with a 1px LINE outline holding the nav pills. */
    private static final class PillBar extends JPanel {
        PillBar() {
            super(new FlowLayout(FlowLayout.CENTER, 2, 0));
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(BAR_PAD, BAR_PAD, BAR_PAD, BAR_PAD));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            g2.setColor(Theme.PANEL);
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);
            g2.setColor(Theme.LINE);
            g2.drawRoundRect(0, 0, getWidth() - 1, h - 1, h, h);
            g2.dispose();
        }
    }
}
