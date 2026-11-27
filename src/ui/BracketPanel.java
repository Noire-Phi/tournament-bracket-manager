package ui;

import algo.Rounds;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import model.Tournament;

/** Bracket page: generate the bracket, start the next match, and watch winners move on. */
public class BracketPanel extends Page {
    private final PageHeader header = new PageHeader();
    private final BracketCanvas canvas;

    public BracketPanel(Tournament tournament, Host host) {
        super(tournament, host);
        setLayout(new BorderLayout(0, Theme.SPACE_L));
        canvas = new BracketCanvas(tournament);

        StyledButton generate = new StyledButton("Generate bracket");
        StyledButton startNext = new StyledButton("Start next match", StyledButton.Kind.PRIMARY);
        generate.addActionListener(e -> generate());
        startNext.addActionListener(e -> startNext());
        header.setActions(StyledButton.row(generate, startNext));

        JScrollPane scroll = new JScrollPane(canvas);
        scroll.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        scroll.setOpaque(false);
        scroll.getViewport().setBackground(Theme.PANEL);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        scroll.getAccessibleContext().setAccessibleName("Bracket");
        // The bracket takes keyboard focus, so show it: a 2px FOCUS frame around the canvas.
        canvas.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                scroll.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Theme.FOCUS));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                scroll.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
            }
        });

        Card tile = new Card("");
        tile.body().add(scroll, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(tile, BorderLayout.CENTER);
        refresh();
    }

    private void generate() {
        int n = tournament.players().size();
        if (tournament.isGenerated()) {
            host.notify("The bracket already exists.", true);
            return;
        }
        if (!Rounds.isBracketSize(n)) {
            host.notify("A bracket needs 4, 8 or 16 " + plural() + ". You have " + n + ".", true);
            return;
        }
        String message = "Generate the bracket for " + n + " " + plural() + "? The " + plural()
                + " are shuffled into round one, and the list is locked: no " + tournament.sport.entity.toLowerCase()
                + " can be added, renamed or removed afterwards.";
        if (!Confirm.ask(this, "Generate bracket", message, "Generate", false)) return;
        try {
            tournament.generateBracket();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            host.notify(ex.getMessage() + ".", true);
            return;
        }
        host.notify("Bracket generated: " + n + " " + plural() + ", " + Rounds.rounds(n) + " rounds, "
                + tournament.matches().size() + " matches.", false);
        refresh();
    }

    private void startNext() {
        if (StartNextAction.run(tournament, host)) refresh();
    }

    private String plural() {
        return tournament.sport.entity.toLowerCase() + "s";
    }

    @Override
    public String title() {
        return "Bracket";
    }

    @Override
    public void refresh() {
        int n = tournament.players().size();
        String caption = n + " " + plural() + (tournament.isGenerated()
                ? " · " + Rounds.rounds(n) + " rounds" : " · needs 4, 8 or 16");
        header.set(title(), caption);
        canvas.sync();
    }
}
