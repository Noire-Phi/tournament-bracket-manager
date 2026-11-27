package model;

/** Sport type. Changes labels only; records are kept when switching. */
public enum SportMode {
    ESPORTS("Esports", "Esports", "Player", "Player name"),
    PHYSICAL("Physical Sports", "Physical", "Team", "Team name");

    public final String label;
    /** Short form for the header switch. */
    public final String shortLabel;
    public final String entity;
    public final String nameLabel;

    SportMode(String label, String shortLabel, String entity, String nameLabel) {
        this.label = label;
        this.shortLabel = shortLabel;
        this.entity = entity;
        this.nameLabel = nameLabel;
    }

    @Override
    public String toString() {
        return label;
    }
}
