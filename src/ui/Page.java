package ui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import model.Tournament;

/** One screen in the MainFrame card deck. refresh() re-reads the tournament and redraws. */
public abstract class Page extends JPanel {

    /** What a page can ask of the window around it. */
    public interface Host {
        /** The tournament changed in a way every page must show (for example the sport mode). */
        void changed();

        /** Shows a toast and writes the message to the status label. */
        void notify(String message, boolean error);
    }

    protected final Tournament tournament;
    protected final Host host;

    protected Page(Tournament tournament, Host host) {
        super(new BorderLayout());
        this.tournament = tournament;
        this.host = host;
        setOpaque(false);
    }

    /** Nav label and page title. Players reads Teams in physical-sports mode. */
    public abstract String title();

    public abstract void refresh();
}
