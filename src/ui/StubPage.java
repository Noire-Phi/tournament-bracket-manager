package ui;

import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Match;
import model.SportMode;
import model.Tournament;

/**
 * Placeholder for a page that is not built yet. Shows live tournament facts so the wiring is visible.
 * The sport-mode button is temporary: the Dashboard gets the real switch.
 */
public class StubPage extends Page {
    private final String key;
    private final JLabel heading = new JLabel();
    private final JLabel name = line();
    private final JLabel mode = line();
    private final JLabel counts = line();
    private final JLabel champion = line();

    public StubPage(String key, Tournament tournament, Host host) {
        super(tournament, host);
        this.key = key;
        setLayout(new BorderLayout(0, Theme.SPACE_L));

        heading.setFont(Theme.display(48));
        heading.setForeground(Theme.INK);
        add(heading, BorderLayout.NORTH);

        Card card = new Card("Tournament (stub page)");
        JPanel lines = new JPanel();
        lines.setOpaque(false);
        lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
        lines.add(name);
        lines.add(mode);
        lines.add(counts);
        lines.add(champion);
        card.body().add(lines, BorderLayout.CENTER);

        StyledButton toggle = new StyledButton("Switch sport mode");
        toggle.addActionListener(e -> {
            tournament.sport = tournament.sport == SportMode.ESPORTS ? SportMode.PHYSICAL : SportMode.ESPORTS;
            host.changed();
            host.notify("Sport mode: " + tournament.sport.label, false);
        });
        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(toggle, BorderLayout.WEST);
        card.body().add(south, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        add(top, BorderLayout.CENTER);
        refresh();
    }

    private static JLabel line() {
        JLabel l = new JLabel();
        l.setFont(Theme.body(14));
        l.setForeground(Theme.INK);
        l.setBorder(javax.swing.BorderFactory.createEmptyBorder(Theme.SPACE_XS, 0, Theme.SPACE_XS, 0));
        return l;
    }

    @Override
    public String title() {
        return key.equals("Players") ? tournament.sport.entity + "s" : key;
    }

    @Override
    public void refresh() {
        heading.setText(title().toUpperCase());
        name.setText("Tournament: " + tournament.name);
        mode.setText("Sport mode: " + tournament.sport.label);
        int played = 0;
        for (Match m : tournament.matches().list()) {
            if (m.status == Match.Status.PLAYED) played++;
        }
        counts.setText(tournament.players().size() + " " + tournament.sport.entity.toLowerCase() + "s, "
                + tournament.matches().size() + " matches, " + played + " played");
        champion.setText("Champion: " + (tournament.champion() == null ? "none yet" : tournament.champion().name));
    }
}
