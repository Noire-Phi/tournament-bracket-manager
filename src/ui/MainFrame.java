package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Tournament;

/** The window after login: nav WEST, page deck CENTER (CardLayout), status label SOUTH. */
public class MainFrame extends JFrame implements Page.Host {
    private static final String[] KEYS = {"Dashboard", "Players", "Bracket", "Matches", "Standings", "History"};
    private static final int NAV_WIDTH = 240;

    private final Tournament tournament;
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);
    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final Map<String, NavButton> navs = new LinkedHashMap<>();
    private final JLabel brandName = new JLabel();
    private final JLabel brandSub = new JLabel();
    private final JLabel status = new JLabel();
    private String current = KEYS[0];

    public MainFrame(Tournament tournament) {
        super("Tournament Bracket Manager");
        this.tournament = tournament;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitApp();
            }
        });
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        deck.setOpaque(false);
        for (String key : KEYS) {
            Page page = createPage(key);
            pages.put(key, page);
            deck.add(page, key);
        }

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL, Theme.SPACE_XL));
        center.add(deck, BorderLayout.CENTER);

        status.setOpaque(true);
        status.setBackground(Theme.PANEL_DEEP);
        status.setForeground(Theme.INK_MUTED);
        status.setFont(Theme.body(13));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(Theme.BORDER, 0, 0, 0, Theme.LINE),
                BorderFactory.createEmptyBorder(Theme.SPACE_S, Theme.SPACE_XL, Theme.SPACE_S, Theme.SPACE_XL)));
        status.getAccessibleContext().setAccessibleName("Status");

        add(buildNav(), BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(1000, 640));
        setSize(1100, 700);
        setLocationRelativeTo(null);
        refreshAll();
        show(KEYS[0]);
    }

    /** Later steps swap each stub for the real page here. */
    private Page createPage(String key) {
        return new StubPage(key, tournament, this);
    }

    private JPanel buildNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(Theme.PANEL_DEEP);
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, Theme.BORDER, Theme.LINE),
                BorderFactory.createEmptyBorder(Theme.SPACE_XL, 0, Theme.SPACE_L, 0)));
        nav.setPreferredSize(new Dimension(NAV_WIDTH, 0));
        nav.getAccessibleContext().setAccessibleName("Modules");

        brandName.setFont(Theme.display(24));
        brandName.setForeground(Theme.INK);
        brandSub.setFont(Theme.body(13));
        brandSub.setForeground(Theme.INK_MUTED);
        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(0, Theme.SPACE_L, Theme.SPACE_L, Theme.SPACE_L));
        brand.setAlignmentX(LEFT_ALIGNMENT);
        brandName.setAlignmentX(LEFT_ALIGNMENT);
        brandSub.setAlignmentX(LEFT_ALIGNMENT);
        brand.add(brandName);
        brand.add(brandSub);
        nav.add(brand);

        for (String key : KEYS) {
            NavButton b = new NavButton(key);
            b.setAlignmentX(LEFT_ALIGNMENT);
            b.addActionListener(e -> show(key));
            navs.put(key, b);
            nav.add(b);
            nav.add(Box.createVerticalStrut(Theme.SPACE_XS));
        }
        nav.add(Box.createVerticalGlue());

        NavButton exit = new NavButton("Exit");
        exit.setAlignmentX(LEFT_ALIGNMENT);
        exit.addActionListener(e -> exitApp());
        nav.add(exit);
        return nav;
    }

    private void show(String key) {
        current = key;
        cards.show(deck, key);
        navs.forEach((k, b) -> b.setActive(k.equals(key)));
        pages.get(key).refresh();
        updateStatus();
    }

    /** Re-reads labels and data everywhere. Used when the sport mode changes. */
    private void refreshAll() {
        brandName.setText(tournament.name);
        brandSub.setText("Single elimination · " + tournament.sport.label);
        for (String key : KEYS) {
            Page p = pages.get(key);
            navs.get(key).setText(p.title());
            p.refresh();
        }
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
    public void changed() {
        refreshAll();
    }

    @Override
    public void notify(String message, boolean error) {
        Toast.show(getRootPane(), message, error);
        status.setText(message);
    }
}
