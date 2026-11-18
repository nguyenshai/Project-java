package chatapp.user.ui.chat;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.Views.Access;
import chatapp.user.preview.Views.Chat;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Private and group conversations in one list (F11, F12f; Figma "Conversation item"): avatar with
 * an online dot for private chats, title, last message, time and a lock for encrypted groups.
 */
final class ConversationList extends JPanel {
    ConversationList(List<Chat> chats, Long selectedId, Consumer<Chat> onPick) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        if (chats.isEmpty()) {
            JLabel none = Ui.muted("No conversations match.");
            none.setBorder(BorderFactory.createEmptyBorder(12, 10, 0, 0));
            add(none);
        }
        for (Chat chat : chats) {
            add(item(chat, selectedId != null && chat.id() == selectedId, onPick));
            add(Box.createVerticalStrut(2));
        }
    }

    private static JPanel item(Chat chat, boolean selected, Consumer<Chat> onPick) {
        JPanel row = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean hover = Boolean.TRUE.equals(getClientProperty("hover"));
                if (selected || hover || isFocusOwner()) {
                    g.setColor(selected ? Ui.SELECTED : Ui.SOFT);
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        row.setFocusable(true);
        row.getAccessibleContext().setAccessibleName(chat.title());
        JPanel avatar = new JPanel(new GridBagLayout());
        avatar.setOpaque(false);
        avatar.add(Ui.avatar(chat.title(), !chat.group() && chat.online(), 34));
        row.add(avatar, BorderLayout.WEST);

        JPanel top = new JPanel(new BorderLayout(6, 0));
        top.setOpaque(false);
        JLabel title = Ui.label(chat.title(), 14, true);
        if (chat.access() == Access.LOCKED || chat.access() == Access.YOU_BLOCKED) {
            JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            titleRow.setOpaque(false);
            titleRow.add(title);
            titleRow.add(Box.createHorizontalStrut(6));
            titleRow.add(chat.access() == Access.LOCKED ? Ui.locked() : Ui.pill("Blocked", Ui.Tone.NEUTRAL));
            top.add(titleRow, BorderLayout.CENTER);
        } else {
            top.add(title, BorderLayout.CENTER);
        }
        JLabel time = Ui.muted(Ui.when(chat.lastActivity(), PreviewServer.NOW.toLocalDate()));
        time.setFont(time.getFont().deriveFont(12f));
        if (chat.encrypted()) {
            time.setIcon(Ui.icon("lock", 13, Ui.MUTED));
            time.setIconTextGap(6);
            time.setToolTipText("Encrypted group");
        }
        top.add(time, BorderLayout.EAST);
        JLabel preview = Ui.muted(chat.preview());
        JPanel text = new JPanel(new GridLayout(2, 1, 0, 1));
        text.setOpaque(false);
        text.add(top);
        text.add(preview);
        row.add(text, BorderLayout.CENTER);

        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.putClientProperty("hover", true); row.repaint(); }
            @Override public void mouseExited(MouseEvent e) { row.putClientProperty("hover", false); row.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { onPick.accept(chat); }
        });
        row.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
        row.getActionMap().put("open", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onPick.accept(chat); }
        });
        row.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { row.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { row.repaint(); }
        });
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        row.setPreferredSize(new Dimension(300, 56));
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }
}
