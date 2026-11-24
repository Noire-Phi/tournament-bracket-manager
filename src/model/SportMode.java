package model;

/** Sport type. Changes labels only; records are kept when switching. */
public enum SportMode {
    ESPORTS("Esports", "Player", "Player name"),
    PHYSICAL("Physical Sports", "Team", "Team name");

    public final String label;
    public final String entity;
    public final String nameLabel;

    SportMode(String label, String entity, String nameLabel) {
        this.label = label;
        this.entity = entity;
        this.nameLabel = nameLabel;
    }

    @Override
    public String toString() {
        return label;
    }
}
