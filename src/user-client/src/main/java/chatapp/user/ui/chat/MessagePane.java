package chatapp.user.ui.chat;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.Views.Message;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Conversation history (F11c, Figma "Message bubble"): day separators, my bubbles on the right in
 * blue, theirs on the left with an avatar (and the sender's name in groups), times, and on my last
 * message its status: "Sent", "Waiting" or "Delivered" (F11a/b, from Message.deliveredAt). Clicking a
 * bubble selects it (F11e, F10); search matches are amber and the focused message is scrolled into view.
 */
final class MessagePane extends JScrollPane {
    /** How the history is drawn. {@code focus} is scrolled into view; null scrolls to the newest message. */
    record View(List<Message> messages, boolean group, boolean selecting, Set<Long> selected, Set<Long> matches,
                Long focus, String footer) {}

    private static final int MAX_BUBBLE = 440;
    private final Map<Long, JComponent> rows = new HashMap<>();
    private Long pendingFocus;
    private boolean pendingBottom = true;

    MessagePane(View view, Consumer<Message> onToggle) {
        JPanel content = new JPanel() {
            @Override public void doLayout() {
                super.doLayout();
                revealPending();
            }
        };
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Ui.CANVAS);
        content.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        LocalDate day = null;
        Message lastMine = null;
        for (Message m : view.messages()) if (m.mine()) lastMine = m;
        for (Message m : view.messages()) {
            LocalDate d = m.sentAt().toLocalDate();
            if (!d.equals(day)) {
                day = d;
                content.add(daySeparator(d));
            }
            String status = m == lastMine ? m.delivery().label() : null;
            JComponent row = row(m, view, status, onToggle);
            rows.put(m.id(), row);
            content.add(row);
            content.add(Box.createVerticalStrut(10));
        }
        if (view.messages().isEmpty()) {
            JLabel none = Ui.muted("No messages yet. Say hello!");
            none.setAlignmentX(CENTER_ALIGNMENT);
            content.add(Box.createVerticalStrut(40));
            content.add(none);
        }
        content.add(Box.createVerticalGlue());
        if (view.footer() != null) {
            JLabel footer = Ui.muted(view.footer());
            footer.setIcon(Ui.icon("info", 15, Ui.BLUE));
            footer.setForeground(Ui.BLUE);
            footer.setIconTextGap(8);
            footer.setAlignmentX(LEFT_ALIGNMENT);
            content.add(footer);
        }
        for (Component c : content.getComponents()) if (c instanceof JComponent j && j.getAlignmentX() != CENTER_ALIGNMENT) j.setAlignmentX(LEFT_ALIGNMENT);
        setViewportView(content);
        setBorder(null);
        getViewport().setBackground(Ui.CANVAS);
        getVerticalScrollBar().setUnitIncrement(24);
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
        pendingFocus = view.focus();
        pendingBottom = view.focus() == null;
    }

    /** Scrolls once, as soon as the rows have their final positions. */
    private void revealPending() {
        int extent = getViewport().getExtentSize().height;
        if (extent <= 0 || (pendingFocus == null && !pendingBottom)) return;
        JComponent view = (JComponent) getViewport().getView();
        int max = Math.max(0, view.getHeight() - extent);
        int y = max;
        if (pendingFocus != null && rows.containsKey(pendingFocus)) {
            Rectangle r = rows.get(pendingFocus).getBounds();
            y = Math.max(0, Math.min(max, r.y - (extent - r.height) / 2));
        }
        pendingFocus = null;
        pendingBottom = false;
        getViewport().setViewPosition(new Point(0, y));
    }

    private static JComponent daySeparator(LocalDate day) {
        LocalDate today = PreviewServer.NOW.toLocalDate();
        String text = day.equals(today) ? "Today" : day.equals(today.minusDays(1)) ? "Yesterday" : Ui.DAY.format(day);
        JLabel chip = new JLabel(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Ui.SOFT);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 6, 6);
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        chip.setFont(new Font(Ui.FONT, Font.BOLD, 11));
        chip.setForeground(Ui.MUTED);
        chip.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        JPanel line = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        line.setOpaque(false);
        line.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        line.add(chip);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, line.getPreferredSize().height));
        return line;
    }

    private static JComponent row(Message m, View view, String status, Consumer<Message> onToggle) {
        boolean selected = view.selected().contains(m.id());
        boolean match = view.matches().contains(m.id());
        boolean focused = match && m.id() == (view.focus() == null ? -1 : view.focus());
        JComponent bubble = bubble(m, selected, match, focused);
        bubble.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onToggle.accept(m); }
        });
        bubble.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "toggle");
        bubble.getActionMap().put("toggle", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onToggle.accept(m); }
        });

        JPanel stack = Ui.column();
        if (view.group() && !m.mine()) {
            JLabel name = Ui.label(m.senderName(), 12, true);
            name.setForeground(Ui.MUTED);
            stack.add(name);
            stack.add(Box.createVerticalStrut(4));
        }
        stack.add(bubble);
        stack.add(Box.createVerticalStrut(4));
        JLabel time = Ui.muted(timeText(m) + (status == null ? "" : " · " + status));
        time.setFont(time.getFont().deriveFont(12f));
        stack.add(time);
        float align = m.mine() ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT;
        for (Component c : stack.getComponents()) ((JComponent) c).setAlignmentX(align);

        JPanel line = new JPanel(new FlowLayout(m.mine() ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        line.setOpaque(false);
        if (view.selecting()) {
            JCheckBox box = Ui.checkbox("");
            box.setSelected(selected);
            box.getAccessibleContext().setAccessibleName("Select message: " + m.text());
            box.addActionListener(e -> onToggle.accept(m));
            JPanel holder = new JPanel(new BorderLayout());
            holder.setOpaque(false);
            holder.add(box, BorderLayout.SOUTH);
            holder.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 8));
            line.add(holder);
        }
        if (!m.mine()) {
            JPanel avatar = new JPanel(new BorderLayout());
            avatar.setOpaque(false);
            avatar.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 10));
            avatar.add(Ui.avatar(m.senderName(), false, 30), BorderLayout.SOUTH);
            line.add(avatar);
        }
        line.add(stack);
        if (view.selecting() || !m.mine()) {
            JPanel full = new JPanel(new BorderLayout());
            full.setOpaque(false);
            full.add(line, m.mine() ? BorderLayout.EAST : BorderLayout.WEST);
            full.setMaximumSize(new Dimension(Integer.MAX_VALUE, full.getPreferredSize().height));
            return full;
        }
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, line.getPreferredSize().height));
        return line;
    }

    private static String timeText(Message m) {
        LocalDate today = PreviewServer.NOW.toLocalDate();
        return m.sentAt().toLocalDate().equals(today) || m.sentAt().toLocalDate().equals(today.minusDays(1))
            ? Ui.TIME.format(m.sentAt()) : Ui.DAY.format(m.sentAt()) + " · " + Ui.TIME.format(m.sentAt());
    }

    /** A rounded bubble: mine blue, theirs white; selected gets a blue ring, a search match is amber. */
    private static JComponent bubble(Message m, boolean selected, boolean match, boolean focused) {
        Color fill = match ? Ui.AMBER : m.mine() ? (selected ? Ui.RAIL_ACTIVE : Ui.BLUE) : (selected ? Ui.SELECTED : Color.WHITE);
        Color text = match ? Ui.BROWN : m.mine() ? Color.WHITE : Ui.INK;
        Color ring = selected ? Ui.BLUE : match ? (focused ? Ui.BROWN : new Color(Ui.BROWN.getRed(), Ui.BROWN.getGreen(), Ui.BROWN.getBlue(), 90)) : m.mine() ? null : Ui.LINE;
        JLabel label = new JLabel() {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(fill);
                g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                if (ring != null) {
                    g.setColor(ring);
                    g.setStroke(new BasicStroke(selected || focused ? 2f : 1f));
                    g.drawRoundRect(selected || focused ? 1 : 0, selected || focused ? 1 : 0,
                        getWidth() - (selected || focused ? 3 : 1), getHeight() - (selected || focused ? 3 : 1), 16, 16);
                }
                if (isFocusOwner()) { g.setColor(Ui.BLUE); g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16); }
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        label.setFont(new Font(Ui.FONT, Font.PLAIN, 14));
        label.setForeground(text);
        FontMetrics fm = label.getFontMetrics(label.getFont());
        String safe = SearchResults.escape(m.text());
        label.setText(fm.stringWidth(m.text()) > MAX_BUBBLE
            ? "<html><div style='width:" + MAX_BUBBLE * 3 / 4 + "pt'>" + safe + "</div></html>" : "<html>" + safe + "</html>");
        label.setBorder(BorderFactory.createEmptyBorder(9, 14, 9, 14));
        label.setFocusable(true);
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.setToolTipText(selected ? "Click to unselect" : "Click to select");
        label.getAccessibleContext().setAccessibleName((m.mine() ? "You" : m.senderName()) + ": " + m.text());
        return label;
    }
}
