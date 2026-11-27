package ui;

import model.Match;
import model.Tournament;

/** Start Next, shared by the Dashboard and the Bracket page. One match is in progress at a time. */
final class StartNextAction {
    private StartNextAction() {}

    /** Starts the first ready match and says so. Returns true when a match was started. */
    static boolean run(Tournament tournament, Page.Host host) {
        Match live = tournament.inProgress();
        if (live != null) {
            host.notify("Finish " + Tournament.matchId(live) + " first.", true);
            return false;
        }
        Match m = tournament.startNext();
        if (m == null) {
            host.notify(tournament.isGenerated() ? "No match is ready." : "Generate the bracket first.", true);
            return false;
        }
        host.notify("Started " + Tournament.matchId(m) + ": " + m.a.name + " vs " + m.b.name + ".", false);
        return true;
    }
}
