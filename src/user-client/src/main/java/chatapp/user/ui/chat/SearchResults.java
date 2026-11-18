package chatapp.user.ui.chat;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.Views.SearchHit;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

/**
 * Results of "Search all messages" (F11g, Figma C8): conversation, sender, snippet with the match
 * in bold blue, and time. Opening one jumps to that message in its conversation.
 */
final class SearchResults extends JPanel {
    SearchResults(List<SearchHit> hits, String query, Long openMessageId, Consumer<SearchHit> onOpen) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        long chats = hits.stream().mapToLong(SearchHit::conversationId).distinct().count();
        JLabel count = Ui.label(hits.isEmpty() ? "No messages match “" + query.trim() + "”."
            : hits.size() + (hits.size() == 1 ? " result" : " results") + " in " + chats
            + (chats == 1 ? " conversation" : " conversations"), 12, true);
        count.setForeground(Ui.MUTED);
        count.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
        count.setAlignmentX(LEFT_ALIGNMENT);
        add(count);
        for (SearchHit hit : hits) {
            add(item(hit, query, openMessageId != null && hit.message().id() == openMessageId, onOpen));
            add(Box.createVerticalStrut(2));
        }
    }

    private static JPanel item(SearchHit hit, String query, boolean selected, Consumer<SearchHit> onOpen) {
        JPanel row = new JPanel(new BorderLayout(0, 2)) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selected || Boolean.TRUE.equals(getClientProperty("hover")) || isFocusOwner()) {
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
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(Ui.label(hit.conversationTitle(), 14, true), BorderLayout.CENTER);
        JLabel time = Ui.muted(Ui.when(hit.message().sentAt(), PreviewServer.NOW.toLocalDate()));
        time.setFont(time.getFont().deriveFont(12f));
        top.add(time, BorderLayout.EAST);
        row.add(top, BorderLayout.NORTH);
        String who = hit.message().mine() ? "You" : hit.message().senderName().split(" ")[0];
        JLabel snippet = new JLabel("<html>" + escape(who + ": ") + highlight(hit.message().text(), query) + "</html>");
        snippet.setFont(new Font(Ui.FONT, Font.PLAIN, 13));
        snippet.setForeground(Ui.MUTED);
        row.add(snippet, BorderLayout.CENTER);
        row.getAccessibleContext().setAccessibleName(hit.conversationTitle() + ": " + hit.message().text());
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { row.putClientProperty("hover", true); row.repaint(); }
            @Override public void mouseExited(MouseEvent e) { row.putClientProperty("hover", false); row.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { onOpen.accept(hit); }
        });
        row.getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "open");
        row.getActionMap().put("open", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onOpen.accept(hit); }
        });
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        row.setPreferredSize(new Dimension(300, 58));
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }

    /**
     * HTML-escapes the message text and wraps each match of the query in bold blue. Only the text is
     * passed in, never the "You: " prefix, because the server only matched the text.
     */
    static String highlight(String text, String query) {
        String q = query.trim();
        StringBuilder out = new StringBuilder();
        int from = 0;
        while (true) {
            int at = PreviewServer.indexOfIgnoreCase(text, q, from);
            if (at < 0) break;
            out.append(escape(text.substring(from, at))).append("<b><font color='#1B54A8'>")
                .append(escape(text.substring(at, at + q.length()))).append("</font></b>");
            from = at + q.length();
        }
        return out.append(escape(text.substring(from))).toString();
    }

    static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
