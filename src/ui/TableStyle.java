package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Applies the UI_GUIDE table look: RAISE header, 44px rows, selection tint plus a 5px PRIMARY bar. */
public final class TableStyle {
    private TableStyle() {}

    public static void apply(JTable table) {
        table.setRowHeight(Theme.CONTROL_H);
        table.setShowGrid(false);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.setBackground(Theme.PANEL);
        table.setForeground(Theme.INK);
        table.setSelectionBackground(Theme.SELECTION);
        table.setSelectionForeground(Theme.INK);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false); // the group's own sorts feed the model

        Cell body = new Cell(Theme.body(14));
        table.setDefaultRenderer(Object.class, body);
        table.setDefaultRenderer(Number.class, body);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderCell());
        header.setPreferredSize(new java.awt.Dimension(0, Theme.CONTROL_H));
    }

    /** Shows one column (for example the ID column) in the monospaced face. */
    public static void monoColumn(JTable table, int column) {
        table.getColumnModel().getColumn(column).setCellRenderer(new Cell(Theme.mono(13)));
    }

    /** Wraps the table in a scroll pane with a LINE border and a PANEL viewport. */
    public static JScrollPane scroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createMatteBorder(Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.LINE));
        sp.getViewport().setBackground(Theme.PANEL);
        return sp;
    }

    /** Row background and selection bar. Shared with StatusPill so a pill cell matches its row. */
    static void paintRow(Graphics g, int width, int height, boolean selected, boolean firstColumn) {
        g.setColor(selected ? Theme.SELECTION : Theme.PANEL);
        g.fillRect(0, 0, width, height);
        if (selected && firstColumn) {
            g.setColor(Theme.PRIMARY);
            g.fillRect(0, 0, Theme.SELECTION_BAR, height);
        }
    }

    private static final class Cell extends DefaultTableCellRenderer {
        private final Font font;
        private boolean selected, first;

        Cell(Font font) {
            this.font = font;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            selected = isSelected;
            first = column == 0;
            setFont(font);
            setForeground(Theme.INK);
            setBackground(isSelected ? Theme.SELECTION : Theme.PANEL);
            setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            // Keyboard focus shows as a 2px FOCUS outline on the focused cell.
            Color edge = hasFocus && table.isFocusOwner() ? Theme.FOCUS : null;
            setBorder(edge != null
                    ? BorderFactory.createMatteBorder(Theme.BORDER, Theme.BORDER, Theme.BORDER, Theme.BORDER, edge)
                    : BorderFactory.createEmptyBorder(Theme.BORDER, Theme.SPACE_M, Theme.BORDER, Theme.SPACE_M));
            if (edge != null) {
                setBorder(BorderFactory.createCompoundBorder(getBorder(),
                        BorderFactory.createEmptyBorder(0, Theme.SPACE_M - Theme.BORDER, 0, Theme.SPACE_M - Theme.BORDER)));
            }
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (selected && first) {
                g.setColor(Theme.PRIMARY);
                g.fillRect(0, 0, Theme.SELECTION_BAR, getHeight());
            }
        }
    }

    private static final class HeaderCell extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, false, false, row, column);
            setText(value == null ? "" : value.toString().toUpperCase());
            setFont(Theme.display(17));
            setForeground(Theme.INK);
            setBackground(Theme.RAISE);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, Theme.BORDER, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(0, Theme.SPACE_M, 0, Theme.SPACE_M)));
            return this;
        }
    }
}
