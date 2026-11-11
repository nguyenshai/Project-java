package chatapp.admin.ui.shared;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public final class Ui {
    public static final Color CANVAS = Color.decode("#F5F7FB");
    public static final Color RAIL = Color.decode("#102345");
    public static final Color RAIL_ACTIVE = Color.decode("#203E72");
    public static final Color RAIL_MUTED = Color.decode("#A6B9D7");
    public static final Color INK = Color.decode("#18263B");
    public static final Color MUTED = Color.decode("#4D5F78");
    public static final Color LINE = Color.decode("#DCE5F1");
    public static final Color SOFT = Color.decode("#EDF3FC");
    public static final Color BLUE = Color.decode("#1B54A8");
    public static final Color SELECTED = Color.decode("#DDEBFF");
    public static final Color AMBER = Color.decode("#F7EEDC");
    public static final Color BROWN = Color.decode("#724D17");
    public static final Color RED = Color.decode("#A92D35");

    private Ui() {}

    public static void install() {
        var defaults = UIManager.getDefaults();
        var keys = java.util.Collections.list(defaults.keys());
        for (Object key : keys) {
            if (defaults.get(key) instanceof javax.swing.plaf.FontUIResource) {
                defaults.put(key, new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 14));
            }
        }
        UIManager.put("OptionPane.background", CANVAS);
        UIManager.put("Panel.background", CANVAS);
        UIManager.put("TextField.selectionBackground", SELECTED);
        UIManager.put("ComboBox.selectionBackground", SELECTED);
    }

    public static JLabel label(String s, int size, boolean bold) {
        JLabel label = new JLabel(s);
        label.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(INK);
        return label;
    }

    public static JLabel muted(String s) {
        JLabel label = label(s, 13, false);
        label.setForeground(MUTED);
        return label;
    }

    public static JPanel row() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);
        return row;
    }

    public static JPanel column() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        return panel;
    }

    public static void add(JPanel column, JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(component);
        column.add(Box.createVerticalStrut(14));
    }

    public static JPanel card(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        return panel;
    }

    public static JButton button(String title, Runnable action) {
        return button(title, Color.WHITE, INK, action);
    }

    public static JButton button(String title, Color background, Color foreground, Runnable action) {
        JButton b = new JButton(title) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getModel().isPressed() ? background.darker() : background);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (background.equals(Color.WHITE) || isFocusOwner()) {
                    g.setColor(isFocusOwner() ? BLUE : LINE);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        b.setForeground(foreground);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    public static ImageIcon icon(String name, int size) {
        var url = Ui.class.getResource("/images/admin/" + name + ".png");
        return url == null ? null : new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    public static JTextField field(String hint, int columns) {
        JTextField f = new JTextField(columns) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    g.setColor(MUTED);
                    Insets insets = getInsets();
                    g.drawString(hint, insets.left, (getHeight() + g.getFontMetrics().getAscent()
                        - g.getFontMetrics().getDescent()) / 2);
                }
            }
        };
        f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)));
        f.setToolTipText(hint);
        f.getAccessibleContext().setAccessibleName(hint);
        return f;
    }

    public static JPanel search(JTextField field) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(BorderFactory.createLineBorder(LINE));
        JLabel icon = new JLabel(icon("search", 16));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        box.add(icon, BorderLayout.WEST);
        field.setBorder(BorderFactory.createEmptyBorder(7, 6, 7, 10));
        box.add(field, BorderLayout.CENTER);
        return box;
    }

    public static void onText(JTextField field, Runnable listener) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { listener.run(); }
            @Override public void removeUpdate(DocumentEvent e) { listener.run(); }
            @Override public void changedUpdate(DocumentEvent e) { listener.run(); }
        });
    }

    public static <T> JComboBox<T> choice(T[] options) {
        JComboBox<T> box = new JComboBox<>(options);
        box.setBackground(Color.WHITE);
        box.setForeground(INK);
        return box;
    }

    public static JTable table(String... columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
            @Override public Class<?> getColumnClass(int column) {
                for (int row = 0; row < getRowCount(); row++) {
                    Object value = getValueAt(row, column);
                    if (value != null) return value.getClass();
                }
                return Object.class;
            }
        });
        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(INK);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setBackground(SOFT);
        table.getTableHeader().setForeground(MUTED);
        var header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        header.setReorderingAllowed(false);
        var headerRenderer = header.getDefaultRenderer();
        header.setDefaultRenderer((grid, value, selected, focused, row, col) -> {
            Component cell = headerRenderer.getTableCellRendererComponent(grid, value, selected, focused, row, col);
            if (cell instanceof JLabel label && value != null && !value.toString().isBlank()) {
                label.setHorizontalTextPosition(SwingConstants.LEFT);
                if (label.getIcon() == null) label.setIcon(icon("sort", 12));
            }
            return cell;
        });
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable grid, Object value, boolean selected,
                    boolean focus, int row, int column) {
                super.getTableCellRendererComponent(grid, value, selected, focus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 4));
                if (!selected) {
                    setBackground(Color.WHITE);
                    setForeground(INK);
                    if ("Locked".equals(value)) {
                        setBackground(AMBER);
                        setForeground(BROWN);
                    } else if ("Active".equals(value)) {
                        setBackground(SOFT);
                        setForeground(BLUE);
                    }
                }
                if ("›".equals(value)) setIcon(icon("chevron", 16));
                else setIcon(null);
                if ("›".equals(value)) setText("");
                if (value instanceof java.time.LocalDate date) {
                    setText(date.format(java.time.format.DateTimeFormatter.ofPattern("MMM d, uuuu", java.util.Locale.ENGLISH)));
                }
                return this;
            }
        });
        return table;
    }

    public static void rows(JTable table, List<Object[]> rows) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (Object[] row : rows) model.addRow(row);
        JScrollPane scroll = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, table);
        if (scroll != null) sizeTable(scroll, table);
    }

    private static void sizeTable(JScrollPane scroll, JTable table) {
        int height = Math.max(88, Math.min(395, 38 + table.getRowHeight() * table.getRowCount()));
        scroll.setPreferredSize(new Dimension(table.getPreferredSize().width + 2, height));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        scroll.revalidate();
    }

    public static JScrollPane scrollTable(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        table.setFillsViewportHeight(true);
        sizeTable(scroll, table);
        return scroll;
    }

    public static void onRow(JTable table, Consumer<Integer> action) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                if (row >= 0) action.accept(table.convertRowIndexToModel(row));
            }
        });
        table.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "openRow");
        table.getActionMap().put("openRow", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (table.getSelectedRow() >= 0) action.accept(table.convertRowIndexToModel(table.getSelectedRow()));
            }
        });
    }

    public static JPanel tabs(String selected, String[] labels, Consumer<String> pick) {
        JPanel panel = row();
        for (String name : labels) panel.add(button(name, name.equals(selected) ? SELECTED : CANVAS,
                name.equals(selected) ? BLUE : MUTED, () -> pick.accept(name)));
        return panel;
    }

    public static boolean matches(String query, String... fields) {
        String q = query.trim().toLowerCase(java.util.Locale.ROOT);
        for (String field : fields) if (field.toLowerCase(java.util.Locale.ROOT).contains(q)) return true;
        return false;
    }
}
