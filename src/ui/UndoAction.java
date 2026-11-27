package ui;

import java.awt.Component;
import ds.ResultStack;
import model.Tournament;

/** Undo with a confirmation. Shared by the Dashboard and the History page. */
final class UndoAction {
    private UndoAction() {}

    /** Asks first, then reverses the newest result. Returns true when something was undone. */
    static boolean run(Component parent, Tournament tournament, Page.Host host) {
        if (tournament.results().isEmpty()) {
            host.notify("Nothing to undo.", true);
            return false;
        }
        ResultStack.Entry top = tournament.results().peek();
        String id = Tournament.matchId(top.match);
        String message = id + ": " + top.winner.name + " no longer beats " + top.loser.name
                + ". The point is removed and the match is back in play.";
        if (!Confirm.ask(parent, "Undo last result", message, "Undo")) return false;
        try {
            tournament.undo();
        } catch (IllegalStateException ex) {
            host.notify(ex.getMessage() + ".", true);
            return false;
        }
        host.notify("Undid " + id + ".", false);
        return true;
    }
}
