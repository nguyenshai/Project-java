package chatapp.user.ui.shared;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Colors, fonts and widget factories of the User client. Copied from the Admin client's
 * {@code chatapp.admin.ui.shared.Ui} (the two clients must not depend on each other) and extended
 * with the widgets of the Figma "00 · Shared user components" frame: button styles with icons,
 * avatars, pills, banners, a toggle and field states.
 */
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
    public static final Color TEAL_SOFT = Color.decode("#DDF4F7");
    public static final Color TEAL = Color.decode("#1D5D6A");
    public static final String FONT = "Segoe UI";

    public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH);
    public static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("MMM d, uuuu · HH:mm", Locale.ENGLISH);
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    public static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    /** Button looks from the Figma "User / Button" component (Admin button spec + optional icon). */
    public enum Style { PRIMARY, SECONDARY, DANGER, DANGER_OUTLINE, LINK, TAB, TAB_SELECTED }

    /** Pill tones from the Figma "Pill" component. */
    public enum Tone { INFO, WARNING, NEUTRAL, MEMBERSHIP, DANGER }

    /** Banner tones: info and success are blue, error is red, lock is teal. */
    public enum Banner { INFO, SUCCESS, ERROR, LOCK }

    private static final Map<String, ImageIcon> ICONS = new HashMap<>();

    private Ui() {}

    public static void install() {
        var defaults = UIManager.getDefaults();
        var keys = java.util.Collections.list(defaults.keys());
        for (Object key : keys) {
            if (defaults.get(key) instanceof javax.swing.plaf.FontUIResource) {
                defaults.put(key, new javax.swing.plaf.FontUIResource(FONT, Font.PLAIN, 14));
            }
        }
        UIManager.put("OptionPane.background", CANVAS);
        UIManager.put("Panel.background", CANVAS);
        UIManager.put("TextField.selectionBackground", SELECTED);
        UIManager.put("PasswordField.selectionBackground", SELECTED);
        UIManager.put("ComboBox.selectionBackground", SELECTED);
        UIManager.put("ToolTip.background", Color.WHITE);
    }

    // ------------------------------------------------------------------ text and layout

    /**
     * Shows a component's text literally. Swing renders any label or button text that starts with
     * "<html>" as HTML, so text other people control (names, messages) must never reach it unguarded.
     * Set before the text: the HTML renderer is only built when the text changes.
     */
    public static <T extends JComponent> T plain(T c) {
        c.putClientProperty("html.disable", Boolean.TRUE);
        return c;
    }

    public static JLabel label(String s, int size, boolean bold) {
        JLabel label = plain(new JLabel());
        label.setText(s);
        label.setFont(new Font(FONT, bold ? Font.BOLD : Font.PLAIN, size));
        label.setForeground(INK);
        return label;
    }

    public static JLabel muted(String s) {
        JLabel label = label(s, 13, false);
        label.setForeground(MUTED);
        return label;
    }

    public static JLabel colored(JLabel label, Color color) {
        label.setForeground(color);
        return label;
    }

    /**
     * Read-only text that wraps to the width it is laid out at. Before layout, {@code width} pixels
     * is used to size it (e.g. in a packed dialog); once its real width is known it re-measures.
     */
    public static JTextArea paragraph(String text, int size, Color color, int width) {
        JTextArea area = new JTextArea(text) {
            @Override public Dimension getPreferredSize() {
                int w = getWidth() > 0 ? getWidth() : width;
                javax.swing.text.View view = getUI().getRootView(this);
                view.setSize(w, Integer.MAX_VALUE);
                return new Dimension(width, (int) Math.ceil(view.getPreferredSpan(javax.swing.text.View.Y_AXIS)));
            }
            @Override public void setBounds(int x, int y, int w, int h) {
                boolean resized = w != getWidth();
                super.setBounds(x, y, w, h);
                if (resized && getPreferredSize().height != h) SwingUtilities.invokeLater(this::revalidate);
            }
        };
        area.setFont(new Font(FONT, Font.PLAIN, size));
        area.setForeground(color);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setBorder(BorderFactory.createEmptyBorder());
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        return area;
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

    /** Wraps a component so its preferred width is fixed while its height still follows the content. */
    public static JPanel width(JComponent content, int width) {
        JPanel box = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(width, super.getPreferredSize().height);
            }
        };
        box.setOpaque(false);
        box.add(content);
        return box;
    }

    /** A left title with components pinned to the right (page headers, section headers). */
    public static JPanel header(JComponent left, JComponent... right) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(left, BorderLayout.WEST);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        for (JComponent c : right) actions.add(c);
        header.add(actions, BorderLayout.EAST);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, header.getPreferredSize().height));
        return header;
    }

    // ------------------------------------------------------------------ buttons

    public static JButton button(String title, Runnable action) {
        return button(title, Style.SECONDARY, null, action);
    }

    public static JButton button(String title, Style style, String icon, Runnable action) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Style s = (Style) getClientProperty("style");
                Color fill = switch (s) {
                    case PRIMARY -> BLUE;
                    case DANGER -> RED;
                    case TAB_SELECTED -> SELECTED;
                    case TAB, LINK -> null;
                    default -> Color.WHITE;
                };
                Color border = s == Style.SECONDARY ? LINE : s == Style.DANGER_OUTLINE ? RED : null;
                if (!isEnabled() && fill != null) { fill = CANVAS; border = LINE; }
                if (fill != null) {
                    g.setColor(getModel().isPressed() ? fill.darker() : fill);
                    g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                if (isFocusOwner()) border = BLUE;
                if (border != null) {
                    g.setColor(border);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        plain(b).setText(title);
        b.putClientProperty("style", style);
        Color fg = switch (style) {
            case PRIMARY, DANGER -> Color.WHITE;
            case DANGER_OUTLINE -> RED;
            case LINK, TAB_SELECTED -> BLUE;
            case TAB -> MUTED;
            default -> INK;
        };
        b.setForeground(fg);
        b.setFont(new Font(FONT, style == Style.TAB ? Font.PLAIN : Font.BOLD, 13));
        if (icon != null) {
            b.setIcon(icon(icon, 16, fg));
            b.setDisabledIcon(icon(icon, 16, MUTED));
            b.setIconTextGap(7);
        }
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, style == Style.LINK ? 4 : 12, 8, style == Style.LINK ? 4 : 12));
        b.setFocusPainted(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> action.run());
        return b;
    }

    /** Disables a button and explains why in its tooltip. */
    public static JButton disabled(JButton button, String reason) {
        button.setEnabled(false);
        button.setToolTipText(reason);
        button.setForeground(MUTED);
        return button;
    }

    public static JPanel tabs(String selected, String[] labels, Consumer<String> pick) {
        JPanel panel = row();
        for (String name : labels) {
            panel.add(button(name, name.equals(selected) ? Style.TAB_SELECTED : Style.TAB, null, () -> pick.accept(name)));
        }
        return panel;
    }

    // ------------------------------------------------------------------ icons

    /** The PNG from {@code images/user/}, scaled; null if it is missing. Cached, used on the EDT only. */
    public static ImageIcon icon(String name, int size) {
        String key = name + "@" + size;
        ImageIcon icon = ICONS.get(key);
        if (icon == null) {
            var url = Ui.class.getResource("/images/user/" + name + ".png");
            if (url == null) return null;
            icon = new ImageIcon(new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
            ICONS.put(key, icon);
        }
        return icon;
    }

    /** The icon re-colored, so one PNG serves every button style. */
    public static ImageIcon icon(String name, int size, Color color) {
        String key = name + "@" + size + "#" + Integer.toHexString(color.getRGB());
        ImageIcon tinted = ICONS.get(key);
        if (tinted == null) {
            ImageIcon base = icon(name, size);
            if (base == null) return null;
            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(base.getImage(), 0, 0, null);
            g.setComposite(AlphaComposite.SrcIn);
            g.setColor(color);
            g.fillRect(0, 0, size, size);
            g.dispose();
            tinted = new ImageIcon(image);
            ICONS.put(key, tinted);
        }
        return tinted;
    }

    // ------------------------------------------------------------------ inputs

    /** Field border: line, blue when focused, red when marked invalid with {@link #invalid}. */
    private static final class FieldBorder extends AbstractBorder {
        private final Insets insets;
        FieldBorder(Insets insets) { this.insets = insets; }
        @Override public void paintBorder(Component c, Graphics graphics, int x, int y, int width, int height) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean bad = Boolean.TRUE.equals(((JComponent) c).getClientProperty("invalid"));
            boolean focus = c.hasFocus() || Boolean.TRUE.equals(((JComponent) c).getClientProperty("focused"));
            g.setColor(bad ? RED : focus ? BLUE : LINE);
            g.drawRoundRect(x, y, width - 1, height - 1, 8, 8);
            if (focus && !bad) g.drawRoundRect(x + 1, y + 1, width - 3, height - 3, 7, 7);
            g.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return (Insets) insets.clone(); }
    }

    private static <T extends JTextField> T styleField(T f, String hint) {
        f.setBorder(new FieldBorder(new Insets(8, 11, 8, 11)));
        f.setToolTipText(hint);
        f.getAccessibleContext().setAccessibleName(hint);
        f.setForeground(INK);
        f.setCaretColor(INK);
        f.setDisabledTextColor(MUTED);
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.repaint(); }
            @Override public void focusLost(FocusEvent e) { f.repaint(); }
        });
        return f;
    }

    private static void paintHint(JTextField field, Graphics g, String hint) {
        if (field.getDocument().getLength() == 0 && !field.isFocusOwner()) {
            g.setColor(MUTED);
            Insets insets = field.getInsets();
            g.drawString(hint, insets.left, (field.getHeight() + g.getFontMetrics().getAscent()
                - g.getFontMetrics().getDescent()) / 2);
        }
    }

    public static JTextField field(String hint, int columns) {
        JTextField f = new JTextField(columns) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintHint(this, g, hint);
            }
        };
        return styleField(f, hint);
    }

    public static JPasswordField password(String hint, int columns) {
        JPasswordField f = new JPasswordField(columns) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintHint(this, g, hint);
            }
        };
        return styleField(f, hint);
    }

    /** A read-only field shown greyed out, e.g. the username on Edit profile. */
    public static JTextField readOnly(JTextField field) {
        field.setEditable(false);
        field.setFocusable(false);
        field.setBackground(CANVAS);
        field.setForeground(MUTED);
        return field;
    }

    /** Marks a field (or a search box) red; false restores it. */
    public static void invalid(JComponent field, boolean bad) {
        field.putClientProperty("invalid", bad);
        field.repaint();
    }

    /** Search box: magnifier icon + borderless field inside one focus-aware frame. */
    public static JPanel search(JTextField field) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(Color.WHITE);
        box.setBorder(new FieldBorder(new Insets(1, 1, 1, 1)));
        JLabel icon = new JLabel(icon("search", 16));
        icon.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        box.add(icon, BorderLayout.WEST);
        field.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 10));
        field.setOpaque(false);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { box.putClientProperty("focused", true); box.repaint(); }
            @Override public void focusLost(FocusEvent e) { box.putClientProperty("focused", false); box.repaint(); }
        });
        box.add(field, BorderLayout.CENTER);
        box.setMaximumSize(new Dimension(box.getPreferredSize().width, box.getPreferredSize().height));
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

    /** The Figma toggle: a blue pill when on, grey when off. */
    public static JToggleButton toggle(boolean on, String name) {
        JToggleButton t = new JToggleButton() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = 36, h = 20, y = (getHeight() - h) / 2;
                Color on = isEnabled() ? BLUE : new Color(BLUE.getRed(), BLUE.getGreen(), BLUE.getBlue(), 90);
                g.setColor(isSelected() ? on : LINE);
                g.fillRoundRect(0, y, w, h, h, h);
                g.setColor(Color.WHITE);
                int knob = h - 6;
                g.fillOval(isSelected() ? w - knob - 3 : 3, y + 3, knob, knob);
                if (isFocusOwner()) { g.setColor(BLUE); g.drawRoundRect(0, y, w - 1, h - 1, h, h); }
                g.dispose();
            }
        };
        t.setSelected(on);
        t.setPreferredSize(new Dimension(36, 24));
        t.setBorder(BorderFactory.createEmptyBorder());
        t.setContentAreaFilled(false);
        t.setFocusPainted(false);
        t.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        t.getAccessibleContext().setAccessibleName(name);
        return t;
    }

    public static JCheckBox checkbox(String text) {
        JCheckBox box = plain(new JCheckBox());
        box.setText(text);
        box.setOpaque(false);
        box.setForeground(INK);
        box.setFocusPainted(false);
        return box;
    }

    // ------------------------------------------------------------------ people and status

    public static String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].isEmpty() ? "?" : parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase(Locale.ROOT);
    }

    /** Round avatar with initials and an optional online dot (Figma "User / Avatar"). */
    public static JComponent avatar(String fullName, boolean online, int size) {
        String text = initials(fullName);
        JPanel avatar = new JPanel(null) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setColor(SOFT);
                g.fillOval(0, 0, size - 1, size - 1);
                g.setColor(LINE);
                g.drawOval(0, 0, size - 1, size - 1);
                g.setColor(BLUE);
                g.setFont(new Font(FONT, Font.BOLD, Math.max(10, size * 11 / 32)));
                FontMetrics fm = g.getFontMetrics();
                g.drawString(text, (size - fm.stringWidth(text)) / 2, (size + fm.getAscent() - fm.getDescent()) / 2);
                if (online) {
                    int dot = Math.max(8, size / 4);
                    g.setColor(Color.WHITE);
                    g.fillOval(size - dot - 1, size - dot - 1, dot + 1, dot + 1);
                    g.setColor(BLUE);
                    g.fillOval(size - dot, size - dot, dot - 1, dot - 1);
                }
                g.dispose();
            }
        };
        avatar.setOpaque(false);
        Dimension d = new Dimension(size, size);
        avatar.setPreferredSize(d);
        avatar.setMinimumSize(d);
        avatar.setMaximumSize(d);
        avatar.getAccessibleContext().setAccessibleName(fullName + (online ? ", online" : ""));
        return avatar;
    }

    /** Avatar, full name and @username stacked (the "Member"/"Person" table cell). */
    public static JPanel person(String fullName, String username, boolean online) {
        return person(fullName, username, online, false);
    }

    /** As {@link #person(String, String, boolean)}, with a "Locked" pill after the name for a locked account. */
    public static JPanel person(String fullName, String username, boolean online, boolean locked) {
        JPanel cell = new JPanel(new BorderLayout(10, 0));
        cell.setOpaque(false);
        JPanel avatarBox = new JPanel(new GridBagLayout());
        avatarBox.setOpaque(false);
        avatarBox.add(avatar(fullName, online && !locked, 30));
        cell.add(avatarBox, BorderLayout.WEST);
        JPanel text = column();
        if (locked) {
            JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            nameRow.setOpaque(false);
            nameRow.add(label(fullName, 14, true));
            nameRow.add(Box.createHorizontalStrut(8));
            nameRow.add(locked());
            nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            text.add(nameRow);
        } else {
            text.add(label(fullName, 14, true));
        }
        JLabel handle = muted("@" + username);
        handle.setFont(handle.getFont().deriveFont(12f));
        text.add(handle);
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.WEST;
        left.weightx = 1;
        middle.add(text, left);
        cell.add(middle, BorderLayout.CENTER);
        return cell;
    }

    /** Rounded status label with a dot (Figma "Pill"). */
    public static JLabel pill(String text, Tone tone) {
        Color bg = switch (tone) {
            case INFO -> SOFT; case WARNING -> AMBER; case MEMBERSHIP -> TEAL_SOFT; case DANGER -> Color.WHITE; default -> CANVAS;
        };
        Color fg = switch (tone) {
            case INFO -> BLUE; case WARNING -> BROWN; case MEMBERSHIP -> TEAL; case DANGER -> RED; default -> MUTED;
        };
        JLabel pill = new JLabel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(bg);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (tone == Tone.DANGER || tone == Tone.NEUTRAL) {
                    g.setColor(tone == Tone.DANGER ? RED : LINE);
                    g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        plain(pill).setText("•  " + text);
        pill.setFont(new Font(FONT, Font.BOLD, 11));
        pill.setForeground(fg);
        pill.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return pill;
    }

    public static JLabel status(boolean online) {
        return pill(online ? "Online" : "Offline", online ? Tone.INFO : Tone.NEUTRAL);
    }

    /** Status pill that says "Locked" for an account an administrator locked. */
    public static JLabel status(boolean online, boolean locked) {
        return locked ? locked() : status(online);
    }

    /** The red "Locked" pill shown wherever a locked account appears (Admin calls the same state "Locked"). */
    public static JLabel locked() {
        JLabel pill = pill("Locked", Tone.DANGER);
        pill.setToolTipText("An administrator locked this account.");
        return pill;
    }

    /** A full-width notice, e.g. "Friend request sent to Ana Lopez." */
    public static JPanel banner(Banner kind, String text) {
        return banner(kind, switch (kind) { case SUCCESS -> "check"; case ERROR -> "alert"; case LOCK -> "lock"; default -> "info"; }, text);
    }

    public static JPanel banner(Banner kind, String icon, String text) {
        Color bg = switch (kind) { case SUCCESS -> SELECTED; case ERROR -> Color.WHITE; case LOCK -> TEAL_SOFT; default -> SOFT; };
        Color fg = switch (kind) { case ERROR -> RED; case LOCK -> TEAL; default -> BLUE; };
        JPanel banner = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(bg);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                if (kind == Banner.ERROR) { g.setColor(RED); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8); }
                g.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        JLabel mark = new JLabel(icon(icon, 16, fg));
        mark.setVerticalAlignment(SwingConstants.TOP);
        banner.add(mark, BorderLayout.WEST);
        banner.add(paragraph(text, 13, fg, 300), BorderLayout.CENTER);
        banner.getAccessibleContext().setAccessibleName(text);
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        return banner;
    }

    /** Centered empty state in a white card (Figma "No friends match …"). */
    public static JPanel empty(String icon, String title, String text, JButton action) {
        JPanel card = card(new GridBagLayout());
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE),
            BorderFactory.createEmptyBorder(40, 16, 40, 16)));
        JPanel inner = column();
        for (JComponent c : new JComponent[]{new JLabel(icon(icon, 34, MUTED)), label(title, 18, true), muted(text)}) {
            c.setAlignmentX(Component.CENTER_ALIGNMENT);
            inner.add(c);
            inner.add(Box.createVerticalStrut(8));
        }
        if (action != null) { action.setAlignmentX(Component.CENTER_ALIGNMENT); inner.add(action); }
        card.add(inner);
        return card;
    }

    // ------------------------------------------------------------------ formatting

    /** Short time as in the conversation list: "10:05", "Yesterday" or "Nov 8". */
    public static String when(LocalDateTime time, LocalDate today) {
        LocalDate day = time.toLocalDate();
        if (day.equals(today)) return TIME.format(time);
        if (day.equals(today.minusDays(1))) return "Yesterday";
        return DAY.format(time);
    }

    public static String date(LocalDateTime time) {
        return time == null ? "—" : DATE.format(time);
    }
}
