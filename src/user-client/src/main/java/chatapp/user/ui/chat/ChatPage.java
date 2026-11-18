package chatapp.user.ui.chat;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views.Access;
import chatapp.user.preview.Views.Chat;
import chatapp.user.preview.Views.Group;
import chatapp.user.preview.Views.Member;
import chatapp.user.preview.Views.Message;
import chatapp.user.preview.Views.SearchHit;
import chatapp.user.ui.Navigator;
import chatapp.user.ui.shared.Dialogs;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Chat page (Figma C1–C11): private and group conversations in one list (F11, F12f), history with
 * live / delivered-when-online status, multi-select → Delete for me (F11e) and Report spam (F10),
 * Clear history (F11d), search in one chat (F11f) or all chats (F11g) with jump, and the AI
 * Suggest / Improve panel (F11h). Group management itself is on the Group page.
 */
public final class ChatPage extends JPanel {
    private final PreviewServer server;
    private final Navigator nav;
    private Long chatId;

    // left column
    private final JTextField globalField = Ui.field("Search all messages", 20);
    private final JTextField filterField = Ui.field("Filter conversations by name", 20);
    private final JPanel filterBox;
    private final JPanel listSlot = new JPanel(new BorderLayout());
    private Long openedHit;

    // right pane
    private final JPanel right = new JPanel(new BorderLayout());
    private final JPanel messagesSlot = new JPanel(new BorderLayout());
    private final JPanel bottomSlot = new JPanel(new BorderLayout());
    private final JPanel noticeSlot = new JPanel(new BorderLayout());
    private Composer composer;
    private boolean selecting;
    private final Set<Long> selected = new LinkedHashSet<>();
    private boolean searching;
    private final JTextField inChatField = Ui.field("Search in this chat", 30);
    private final JLabel hitCount = Ui.muted("");
    private List<SearchHit> hits = List.of();
    private int hitIndex;
    private Long jumpTo;

    public ChatPage(PreviewServer server, Navigator nav, Long chatId) {
        super(new BorderLayout());
        this.server = server;
        this.nav = nav;
        this.chatId = chatId;
        setBackground(Ui.CANVAS);

        JPanel left = new JPanel(new BorderLayout(0, 12));
        left.setBackground(Color.WHITE);
        left.setPreferredSize(new Dimension(340, 0));
        left.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Ui.LINE),
            BorderFactory.createEmptyBorder(24, 16, 12, 16)));
        JPanel top = Ui.column();
        JLabel title = Ui.label("Chat", 32, true);
        title.setBorder(BorderFactory.createEmptyBorder(0, 2, 12, 0));
        top.add(title);
        JPanel globalBox = Ui.search(globalField);
        globalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, globalBox.getPreferredSize().height));
        top.add(globalBox);
        top.add(Box.createVerticalStrut(10));
        filterBox = Ui.search(filterField);
        filterBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, filterBox.getPreferredSize().height));
        top.add(filterBox);
        for (Component c : top.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        left.add(top, BorderLayout.NORTH);
        listSlot.setOpaque(false);
        JPanel listTop = new JPanel(new BorderLayout());
        listTop.setOpaque(false);
        listTop.add(listSlot, BorderLayout.NORTH);
        JScrollPane listScroll = new JScrollPane(listTop, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.setBorder(null);
        listScroll.getViewport().setBackground(Color.WHITE);
        listScroll.getVerticalScrollBar().setUnitIncrement(24);
        left.add(listScroll);
        add(left, BorderLayout.WEST);

        right.setBackground(Ui.CANVAS);
        messagesSlot.setOpaque(false);
        bottomSlot.setOpaque(false);
        noticeSlot.setOpaque(false);
        add(right, BorderLayout.CENTER);

        Ui.onText(filterField, this::renderList);
        Ui.onText(globalField, () -> { openedHit = null; renderList(); });
        Ui.onText(inChatField, () -> { hitIndex = 0; runSearch(); });
        inChatField.addActionListener(e -> step(1));
        renderList();
        renderRight();
    }

    // ------------------------------------------------------------------ left column

    private void renderList() {
        listSlot.removeAll();
        String global = globalField.getText();
        filterBox.setVisible(global.isBlank());
        if (!global.isBlank()) {
            listSlot.add(new SearchResults(server.searchAll(global), global, openedHit, this::openHit));
        } else {
            listSlot.add(new ConversationList(server.chats(filterField.getText()), chatId, chat -> open(chat.id(), null)));
        }
        listSlot.revalidate();
        listSlot.repaint();
    }

    private void openHit(SearchHit hit) {
        openedHit = hit.message().id();
        open(hit.conversationId(), hit.message().id());
    }

    /** Opens a conversation, optionally jumping to (and highlighting) one message. */
    private void open(long id, Long messageId) {
        chatId = id;
        selecting = false;
        selected.clear();
        searching = false;
        inChatField.setText("");
        jumpTo = messageId;
        composer = null;
        renderList();
        renderRight();
    }

    // ------------------------------------------------------------------ right pane

    private void renderRight() {
        right.removeAll();
        noticeSlot.removeAll();
        Chat chat = null;
        if (chatId != null) {
            try {
                chat = server.chat(chatId);
            } catch (ServiceException e) {
                chatId = null;
                notice(Ui.Banner.ERROR, e.getMessage());
            }
        }
        if (chat == null) {
            right.add(noticeSlot, BorderLayout.NORTH);
            right.add(emptyState());
            right.revalidate();
            right.repaint();
            return;
        }
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header(chat), BorderLayout.NORTH);
        if (searching) top.add(searchBar(), BorderLayout.CENTER);
        top.add(noticeSlot, BorderLayout.SOUTH);
        right.add(top, BorderLayout.NORTH);
        right.add(messagesSlot);
        right.add(bottomSlot, BorderLayout.SOUTH);
        if (composer == null) composer = new Composer("", composerActions());
        renderMessages(chat, true);
        right.revalidate();
        right.repaint();
    }

    private JComponent emptyState() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        JPanel inner = Ui.column();
        JLabel icon = new JLabel(Ui.icon("chat", 36, Ui.MUTED));
        JLabel title = Ui.label("Select a conversation", 18, true);
        JLabel hint = Ui.muted("Or start one from Friend → Chat");
        for (JComponent c : new JComponent[]{icon, title, hint}) {
            c.setAlignmentX(CENTER_ALIGNMENT);
            inner.add(c);
            inner.add(Box.createVerticalStrut(8));
        }
        JButton friends = Ui.button("Open Friend", Ui.Style.SECONDARY, "friend", () -> nav.openFriends("Friends"));
        friends.setAlignmentX(CENTER_ALIGNMENT);
        inner.add(friends);
        center.add(inner);
        return center;
    }

    private JComponent header(Chat chat) {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.LINE),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)));
        JPanel avatar = new JPanel(new GridBagLayout());
        avatar.setOpaque(false);
        avatar.add(Ui.avatar(chat.title(), !chat.group() && chat.online(), 36));
        header.add(avatar, BorderLayout.WEST);
        JPanel names = Ui.column();
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleRow.setOpaque(false);
        titleRow.add(Ui.label(chat.title(), 18, true));
        if (chat.encrypted()) {
            titleRow.add(Box.createHorizontalStrut(10));
            titleRow.add(Ui.pill("Encrypted", Ui.Tone.MEMBERSHIP));
        }
        if (chat.access() == Access.LOCKED || chat.access() == Access.YOU_BLOCKED) {
            titleRow.add(Box.createHorizontalStrut(10));
            titleRow.add(chat.access() == Access.LOCKED ? Ui.locked() : Ui.pill("Blocked", Ui.Tone.NEUTRAL));
        }
        names.add(titleRow);
        JLabel status;
        if (chat.group()) {
            Group group = server.group(chat.id());
            long online = group.members().stream().filter(Member::online).count();
            status = Ui.muted(group.members().size() + " members  ·  " + online + " online");
        } else if (!chat.access().canSend()) {
            status = Ui.muted(switch (chat.access()) {
                case LOCKED -> "Account locked by an administrator";
                case YOU_BLOCKED -> "You blocked this person";
                default -> "Unavailable";
            });
        } else if (chat.online()) {
            status = Ui.muted("●  Online · messages arrive instantly");
            status.setForeground(Ui.BLUE);
        } else {
            status = Ui.muted("Offline");
        }
        status.setFont(status.getFont().deriveFont(12f));
        names.add(status);
        for (Component c : names.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.weightx = 1;
        middle.add(names, gc);
        header.add(middle);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        if (!searching) buttons.add(Ui.button("Search in chat", Ui.Style.SECONDARY, "search", () -> {
            searching = true;
            selecting = false;
            selected.clear();
            renderRight();
            SwingUtilities.invokeLater(inChatField::requestFocusInWindow);
        }));
        buttons.add(Ui.button("Clear history", Ui.Style.SECONDARY, "history", () -> clearHistory(chat)));
        JPanel buttonBox = new JPanel(new GridBagLayout());
        buttonBox.setOpaque(false);
        buttonBox.add(buttons);
        header.add(buttonBox, BorderLayout.EAST);
        return header;
    }

    /** F11f: the in-chat search bar with "n of m", previous/next and close. */
    private JComponent searchBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(Ui.SOFT);
        bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Ui.LINE),
            BorderFactory.createEmptyBorder(8, 24, 8, 24)));
        JPanel box = Ui.search(inChatField);
        box.setMaximumSize(null);
        bar.add(box);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controls.setOpaque(false);
        controls.add(hitCount);
        controls.add(iconButton("up", "Previous match", () -> step(-1)));
        controls.add(iconButton("down", "Next match", () -> step(1)));
        controls.add(iconButton("close", "Close search", () -> {
            searching = false;
            inChatField.setText("");
            jumpTo = null;
            renderRight();
        }));
        bar.add(controls, BorderLayout.EAST);
        return bar;
    }

    private static JButton iconButton(String icon, String name, Runnable action) {
        JButton button = Ui.button("", Ui.Style.SECONDARY, icon, action);
        button.setToolTipText(name);
        button.getAccessibleContext().setAccessibleName(name);
        return button;
    }

    private void runSearch() {
        if (chatId == null) return;
        try {
            hits = server.search(chatId, inChatField.getText());
        } catch (ServiceException e) {
            lostChat(e);
            return;
        }
        if (hitIndex >= hits.size()) hitIndex = Math.max(0, hits.size() - 1);
        jumpTo = hits.isEmpty() ? null : hits.get(hitIndex).message().id();
        renderMessages(null, false);
    }

    private void step(int delta) {
        if (hits.isEmpty()) return;
        hitIndex = Math.floorMod(hitIndex + delta, hits.size());
        jumpTo = hits.get(hitIndex).message().id();
        renderMessages(null, false);
    }

    /** Rebuilds the history and the bottom bar after a change to the open conversation. */
    private void renderMessages() {
        renderMessages(null, true);
    }

    /**
     * Rebuilds the history and the bottom bar; the header and search field stay, so typing keeps focus.
     *
     * @param known       the open chat if the caller already fetched it, else null
     * @param searchAgain false when the caller has just run the in-chat search, so it is not run twice
     */
    private void renderMessages(Chat known, boolean searchAgain) {
        Chat chat = known;
        List<Message> messages;
        try {
            if (chat == null) chat = server.chat(chatId);
            messages = server.messages(chatId);
        } catch (ServiceException e) {
            lostChat(e);
            return;
        }
        Set<Long> matches = new HashSet<>();
        if (searching) {
            if (searchAgain) hits = server.search(chatId, inChatField.getText());
            if (hitIndex >= hits.size()) hitIndex = Math.max(0, hits.size() - 1);
            hits.forEach(h -> matches.add(h.message().id()));
            hitCount.setText(inChatField.getText().isBlank() ? "" : hits.isEmpty() ? "No matches"
                : (hitIndex + 1) + " of " + hits.size());
        } else if (jumpTo != null) {
            matches.add(jumpTo);
        }
        selected.retainAll(messages.stream().map(Message::id).toList());
        String footer = null;
        if (!chat.group() && chat.access().canSend() && !chat.online()) {
            String first = chat.title().split(" ")[0];
            footer = first + " is offline. Your messages will be waiting when " + first + " signs in.";
        }
        messagesSlot.removeAll();
        messagesSlot.add(new MessagePane(new MessagePane.View(messages, chat.group(), selecting, Set.copyOf(selected),
            matches, jumpTo, footer), this::toggle));
        bottomSlot.removeAll();
        bottomSlot.add(selecting ? selectionBar(messages) : chat.access().canSend() ? composer : closedBar(chat));
        messagesSlot.revalidate();
        messagesSlot.repaint();
        bottomSlot.revalidate();
        bottomSlot.repaint();
    }

    /** The open conversation can no longer be read (e.g. I was removed): close it and say why. */
    private void lostChat(ServiceException e) {
        chatId = null;
        renderRight();   // clears the notice area, so the error is posted after it
        notice(Ui.Banner.ERROR, e.getMessage());
        renderList();
    }

    private void notice(Ui.Banner kind, String text) {
        noticeSlot.removeAll();
        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.setBorder(BorderFactory.createEmptyBorder(10, 24, 0, 24));
        holder.add(Ui.banner(kind, text));
        noticeSlot.add(holder);
        noticeSlot.revalidate();
        noticeSlot.repaint();
    }

    /** Replaces the message box when I can't send here: I blocked them, they blocked me, or their account is locked. */
    private JComponent closedBar(Chat chat) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Ui.SOFT);
        bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.LINE),
            BorderFactory.createEmptyBorder(16, 24, 16, 24)));
        String text = switch (chat.access()) {
            case LOCKED -> chat.title() + "'s account is locked by an administrator. You can't send messages in this chat.";
            case YOU_BLOCKED -> "You blocked " + chat.title() + ". You can't send messages in this chat.";
            default -> "You can't message this person.";
        };
        JLabel label = Ui.muted(text);
        label.setIcon(Ui.icon(chat.access() == Access.LOCKED ? "lock" : "block", 16, Ui.MUTED));
        label.setIconTextGap(10);
        bar.add(label, BorderLayout.CENTER);
        return bar;
    }

    // ------------------------------------------------------------------ selection: delete for me (F11e), report spam (F10)

    private void toggle(Message m) {
        if (!selected.remove(m.id())) selected.add(m.id());
        selecting = !selected.isEmpty();
        if (selecting) searching = false;
        renderRight();
    }

    private JComponent selectionBar(List<Message> messages) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Ui.SOFT);
        bar.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.LINE),
            BorderFactory.createEmptyBorder(14, 24, 14, 24)));
        bar.add(Ui.label(selected.size() + (selected.size() == 1 ? " message selected" : " messages selected"), 14, true),
            BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(Ui.button("Delete for me", Ui.Style.DANGER_OUTLINE, "trash", this::deleteSelected));
        Message only = selected.size() == 1 ? messages.stream().filter(m -> selected.contains(m.id())).findFirst().orElse(null) : null;
        JButton report = Ui.button("Report spam", Ui.Style.DANGER, "flag", () -> reportSpam(only));
        if (only == null || only.mine() || only.reportedByMe()) {
            Ui.disabled(report, only == null ? "Select one message to report it."
                : only.mine() ? "You can only report messages from other people." : "You already reported this message.");
        }
        buttons.add(report);
        buttons.add(Ui.button("Cancel", () -> {
            selecting = false;
            selected.clear();
            renderRight();
        }));
        bar.add(buttons, BorderLayout.EAST);
        return bar;
    }

    private void deleteSelected() {
        int n = selected.size();
        if (!Dialogs.confirm(this, "Delete for me", n + (n == 1 ? " message" : " messages"),
                "The selected messages will be removed for you only. Others in this chat still see them.",
                "Delete", Ui.Style.DANGER, "trash")) return;
        try {
            server.delete(chatId, List.copyOf(selected));
            notice(Ui.Banner.SUCCESS, n + (n == 1 ? " message was" : " messages were") + " deleted for you.");
            selecting = false;
            selected.clear();
        } catch (ServiceException e) {
            notice(Ui.Banner.ERROR, e.getMessage());
        }
        renderList();
        renderMessages();
    }

    static final List<String> SPAM_REASONS = List.of("Unwanted promotions or ads", "Repeated unsolicited messages", "Other");

    private void reportSpam(Message m) {
        if (m == null) return;
        JPanel extra = Ui.column();
        JPanel quote = new JPanel(new BorderLayout(0, 3));
        quote.setBackground(Ui.SOFT);
        quote.setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
        JLabel from = Ui.label(m.senderName() + " · " + Ui.TIME.format(m.sentAt()), 12, true);
        from.setForeground(Ui.MUTED);
        quote.add(from, BorderLayout.NORTH);
        quote.add(Ui.paragraph(m.text(), 13, Ui.INK, 380));
        extra.add(quote);
        extra.add(Box.createVerticalStrut(12));
        extra.add(Ui.label("Reason", 13, true));
        extra.add(Box.createVerticalStrut(4));
        List<JCheckBox> boxes = new ArrayList<>();
        for (String reason : SPAM_REASONS) {
            JCheckBox box = Ui.checkbox(reason);
            box.setSelected(boxes.isEmpty());
            boxes.add(box);
            extra.add(box);
        }
        for (Component c : extra.getComponents()) ((JComponent) c).setAlignmentX(LEFT_ALIGNMENT);
        if (!Dialogs.confirm(this, new Dialogs.Ask("Report spam", m.senderName(),
                "Administrators will review this message and may lock the account.", extra, "Report", Ui.Style.DANGER, "flag"))) {
            return;
        }
        String reason = String.join("; ", boxes.stream().filter(JCheckBox::isSelected).map(JCheckBox::getText).toList());
        try {
            server.reportSpam(m.id(), reason);
            notice(Ui.Banner.SUCCESS, "Report sent. Administrators will review the message from " + m.senderName() + ".");
            selecting = false;
            selected.clear();
        } catch (ServiceException e) {
            notice(Ui.Banner.ERROR, e.getMessage());
        }
        renderMessages();
    }

    private void clearHistory(Chat chat) {
        String others = chat.group() ? "Other members" : chat.title().split(" ")[0];
        if (!Dialogs.confirm(this, "Clear chat history", chat.title(),
                "All messages in this conversation will be removed for you only. " + others
                    + " will still see them. This can't be undone.",
                "Clear history", Ui.Style.DANGER, "trash")) return;
        try {
            server.clearHistory(chat.id());
            notice(Ui.Banner.SUCCESS, "Chat history cleared for you.");
            selecting = false;
            selected.clear();
        } catch (ServiceException e) {
            notice(Ui.Banner.ERROR, e.getMessage());
        }
        renderList();
        renderMessages();
    }

    // ------------------------------------------------------------------ composer (F11a/b, F11h)

    private Composer.Actions composerActions() {
        return new Composer.Actions() {
            @Override public void send(String text) {
                try {
                    server.send(chatId, text);
                    composer.sent();
                    jumpTo = null;
                    noticeSlot.removeAll();
                    renderList();
                    renderMessages();
                    SwingUtilities.invokeLater(() -> composer.input().requestFocusInWindow());
                } catch (ServiceException e) {
                    composer.error(e.getMessage());
                }
            }

            @Override public void suggest() {
                try {
                    composer.showSuggestions(server.suggest(chatId));
                } catch (ServiceException e) {
                    composer.error(e.getMessage());
                }
            }

            @Override public void improve(String draft, String tone) {
                try {
                    composer.showImprove(draft.strip(), server.improve(chatId, draft, tone), tone);
                } catch (ServiceException e) {
                    composer.error(e.getMessage());
                }
            }
        };
    }
}
