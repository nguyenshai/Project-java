package chatapp.user.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * A table in the Admin table look (soft header, white rows with bottom borders) whose cells are
 * real components, so rows can hold avatars, pills, checkboxes and action buttons as in the Figma
 * User screens. A column with a fixed width keeps it; the other columns share the rest by weight.
 * Rows given an {@code onOpen} action open on click or Enter, like the Admin {@code Ui.onRow}.
 */
public final class RowTable extends JPanel {
    /** {@code width > 0} fixes the column width; otherwise it takes {@code weight} of the free space. */
    public record Column(String name, double weight, int width) {
        public static Column grow(String name, double weight) { return new Column(name, weight, 0); }
        public static Column fixed(String name, int width) { return new Column(name, 0, width); }
    }

    private final Column[] columns;
    private final JPanel body = Ui.column();
    private final List<Runnable> openers = new ArrayList<>();
    private final int rowHeight;

    public RowTable(int rowHeight, Column... columns) {
        super(new BorderLayout());
        this.columns = columns;
        this.rowHeight = rowHeight;
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(Ui.LINE));
        JComponent[] titles = new JComponent[columns.length];
        for (int i = 0; i < columns.length; i++) {
            JLabel title = Ui.label(columns[i].name(), 12, true);
            title.setForeground(Ui.MUTED);
            titles[i] = title;
        }
        JPanel header = line(titles, 36);
        header.setBackground(Ui.SOFT);
        header.setOpaque(true);
        add(header, BorderLayout.NORTH);
        body.setOpaque(true);
        body.setBackground(Color.WHITE);
        add(body, BorderLayout.CENTER);
        setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public List<String> columnNames() {
        List<String> names = new ArrayList<>();
        for (Column c : columns) names.add(c.name());
        return names;
    }

    public void clear() {
        body.removeAll();
        openers.clear();
        revalidate();
        repaint();
    }

    public int rowCount() {
        return body.getComponentCount();
    }

    /** Adds a row; {@code onOpen} may be null for rows that only have buttons. */
    public JPanel addRow(Runnable onOpen, JComponent... cells) {
        JPanel row = line(cells, rowHeight);
        row.setOpaque(true);
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.LINE),
            row.getBorder()));
        openers.add(onOpen);
        if (onOpen != null) {
            row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            row.setFocusable(true);
            row.addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { row.setBackground(Ui.SOFT); }
                @Override public void mouseExited(MouseEvent e) { row.setBackground(Color.WHITE); }
                @Override public void mouseClicked(MouseEvent e) { onOpen.run(); }
            });
            row.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "openRow");
            row.getActionMap().put("openRow", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) { onOpen.run(); }
            });
        }
        body.add(row);
        revalidate();
        return row;
    }

    /** Opens row {@code index} as a click would; used by keyboard users and tests. */
    public void open(int index) {
        Runnable opener = openers.get(index);
        if (opener != null) opener.run();
    }

    /** Highlights a row as selected (e.g. checked rows on the Online tab). */
    public void select(JPanel row, boolean selected) {
        row.setBackground(selected ? Ui.SELECTED : Color.WHITE);
    }

    private JPanel line(JComponent[] cells, int height) {
        JPanel line = new JPanel(new GridBagLayout());
        line.setOpaque(false);
        line.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < columns.length; i++) {
            Column column = columns[i];
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(false);
            JComponent content = i < cells.length && cells[i] != null ? cells[i] : new JLabel();
            JPanel holder = new JPanel(new GridBagLayout());
            holder.setOpaque(false);
            holder.add(content);
            boolean right = column.width() > 0 && i == columns.length - 1;
            cell.add(holder, right ? BorderLayout.EAST : BorderLayout.WEST);
            int w = column.width() > 0 ? column.width() : 0;
            Dimension size = new Dimension(w, height);
            cell.setPreferredSize(size);
            cell.setMinimumSize(new Dimension(w, height));
            c.gridx = i;
            c.weightx = column.width() > 0 ? 0 : column.weight();
            c.insets = new Insets(0, i == 0 ? 0 : 8, 0, 0);
            line.add(cell, c);
        }
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        return line;
    }
}
