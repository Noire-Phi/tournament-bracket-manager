package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * The UI_GUIDE table: it sits in a rounded inner box (PANEL_DEEP, radius 16) inside its tile.
 * Muted bold header, 1px LINE row dividers, selected row tinted amber with a 4px amber bar.
 */
public final class TableStyle {
    private static final int BOX_RADIUS = 16;

    private TableStyle() {}

    public static void apply(JTable table) {
        table.setRowHeight(Theme.CONTROL_H + 2);
        // 1px LINE divider under each row: the grid shows through a 1px row gap.
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(Theme.LINE);
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setBackground(Theme.PANEL_DEEP);
        table.setForeground(Theme.INK);
        table.setSelectionBackground(Theme.SELECTION);
        table.setSelectionForeground(Theme.INK);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(false); // the group's own sorts feed the model

        Cell body = new Cell(Theme.body(Theme.SIZE_M), v -> Theme.INK);
        table.setDefaultRenderer(Object.class, body);
        table.setDefaultRenderer(Number.class, body);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderCell());
        header.setPreferredSize(new java.awt.Dimension(0, Theme.CONTROL_H));
    }

    /** An ID column (M013, P012): DM Sans, small, muted. */
    public static void idColumn(JTable table, int column) {
        column(table, column, Theme.body(Theme.SIZE_S), v -> Theme.INK_MUTED);
    }

    /** One column with its own font, and a colour chosen from the cell's value. */
    public static void column(JTable table, int column, Font font, Function<Object, Color> ink) {
        table.getColumnModel().getColumn(column).setCellRenderer(new Cell(font, ink));
    }

    /** The table in a scroll pane (vertical only) on the inner surface. */
    public static JScrollPane scroll(JTable table) {
        JScrollPane sp = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setBackground(Theme.PANEL_DEEP);
        // Ask for five rows, not the whole list: the table fills its tile and scrolls inside it, and the page
        // scrolls only when even five rows do not fit (1000 x 640).
        table.setPreferredScrollableViewportSize(new java.awt.Dimension(0, 5 * table.getRowHeight()));
        return sp;
    }

    /** The scroll pane inside a rounded PANEL_DEEP box, padded so the square table corners stay inside the curve. */
    public static JPanel box(JScrollPane scroll) {
        JPanel box = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PANEL_DEEP);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 2 * BOX_RADIUS, 2 * BOX_RADIUS);
                g2.dispose();
            }
        };
        box.setOpaque(false);
        box.setBorder(BorderFactory.createEmptyBorder(Theme.SPACE_XS, Theme.SPACE_XS, Theme.SPACE_XS, Theme.SPACE_XS));
        box.add(scroll, BorderLayout.CENTER);
        return box;
    }

    /** Row background and selection bar. Shared with StatusPill so a pill cell matches its row. */
    static void paintRow(Graphics g, int width, int height, boolean selected, boolean firstColumn) {
        g.setColor(selected ? Theme.SELECTION : Theme.PANEL_DEEP);
        g.fillRect(0, 0, width, height);
        if (selected && firstColumn) {
            g.setColor(Theme.PRIMARY);
            g.fillRect(0, 0, Theme.SELECTION_BAR, height);
        }
    }

    private static final class Cell extends DefaultTableCellRenderer {
        private final Font font;
        private final Function<Object, Color> ink;
        private boolean selected, first;

        Cell(Font font, Function<Object, Color> ink) {
            this.font = font;
            this.ink = ink;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            selected = isSelected;
            first = column == 0;
            setFont(font);
            setForeground(ink.apply(value));
            setBackground(isSelected ? Theme.SELECTION : Theme.PANEL_DEEP);
            setHorizontalAlignment(value instanceof Number ? SwingConstants.RIGHT : SwingConstants.LEFT);
            // Keyboard focus shows as a 2px FOCUS outline on the focused cell.
            Color edge = hasFocus && table.isFocusOwner() ? Theme.FOCUS : null;
            setBorder(edge != null
                    ? BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(2, 2, 2, 2, edge),
                            BorderFactory.createEmptyBorder(0, Theme.SPACE_L - 2, 0, Theme.SPACE_L - 2))
                    : BorderFactory.createEmptyBorder(0, Theme.SPACE_L, 0, Theme.SPACE_L));
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
            setText(value == null ? "" : value.toString());
            setHorizontalAlignment(Number.class.isAssignableFrom(table.getColumnClass(column))
                    ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setFont(Theme.body(Theme.SIZE_S).deriveFont(Font.BOLD));
            setForeground(Theme.INK_MUTED);
            setBackground(Theme.PANEL_DEEP);
            setOpaque(true);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, Theme.BORDER, 0, Theme.LINE),
                    BorderFactory.createEmptyBorder(0, Theme.SPACE_L, 0, Theme.SPACE_L)));
            return this;
        }
    }
}
