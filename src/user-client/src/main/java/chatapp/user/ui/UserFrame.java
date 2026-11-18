package chatapp.user.ui;

import chatapp.user.preview.PreviewServer;
import chatapp.user.ui.account.AccountPage;
import chatapp.user.ui.auth.ForgotPassword;
import chatapp.user.ui.auth.Login;
import chatapp.user.ui.auth.Register;
import chatapp.user.ui.chat.ChatPage;
import chatapp.user.ui.friend.FriendPage;
import chatapp.user.ui.group.GroupPage;
import chatapp.user.ui.shared.Dialogs;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Window of the User client: the pre-login flow (Login, Register, Forgot password) and, after
 * sign-in, the workspace with the sidebar pages Chat, Group, Friend and Account. One
 * {@link PreviewServer} lives as long as the window, so an account registered here can sign in again.
 */
public final class UserFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final PreviewServer server = new PreviewServer();
    private final Auth auth;
    private JPanel workspace;

    public UserFrame() {
        super("ChatApp");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Ui.install();
        auth = new Auth(server, this::showWorkspace);
        root.add(auth, "Auth");
        setContentPane(root);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1100, 680));
        setLocationRelativeTo(null);
        var icon = Ui.icon("chat", 32, Ui.BLUE);
        if (icon != null) setIconImage(icon.getImage());
    }

    /** Opens a fresh workspace for the signed-in user. */
    private void showWorkspace() {
        if (workspace != null) root.remove(workspace);
        workspace = createContent(server, this::showLogin);
        root.add(workspace, "Workspace");
        cards.show(root, "Workspace");
    }

    private void showLogin() {
        auth.show("Login");
        cards.show(root, "Auth");
    }

    /** The workspace signed in as the sample user Maya Chen; used directly by tests. */
    public static JPanel createContent() {
        PreviewServer server = new PreviewServer();
        server.signIn(PreviewServer.SAMPLE_USERNAME, PreviewServer.SAMPLE_PASSWORD);
        return createContent(server, () -> {});
    }

    /** The workspace for whoever is signed in on {@code server}; {@code onSignOut} runs after signing out. */
    public static JPanel createContent(PreviewServer server, Runnable onSignOut) {
        Ui.install();
        return new Workspace(server, onSignOut);
    }

    /** The pre-login flow; {@code onSignIn} runs after a successful sign-in or registration. */
    public static JPanel createAuth(PreviewServer server, Runnable onSignIn) {
        Ui.install();
        return new Auth(server, onSignIn);
    }

    /** Login, Register and Forgot password, switched in place. */
    private static final class Auth extends JPanel {
        private final CardLayout pages = new CardLayout();
        private final Login login;
        private final PreviewServer server;
        private final Runnable onSignIn;

        Auth(PreviewServer server, Runnable onSignIn) {
            this.server = server;
            this.onSignIn = onSignIn;
            setLayout(pages);
            login = new Login(server, onSignIn, () -> show("Register"), () -> show("Forgot"));
            add(login, "Login");
            show("Login");
        }

        /** Register and Forgot password are rebuilt each time so no typed data stays behind. */
        void show(String name) {
            for (Component c : getComponents()) if (c != login) remove(c);
            switch (name) {
                case "Register" -> add(new Register(server, onSignIn, () -> show("Login")), name);
                case "Forgot" -> add(new ForgotPassword(server, () -> show("Login")), name);
                default -> login.reset();
            }
            pages.show(this, name);
            revalidate();
            repaint();
        }
    }

    private static final class Workspace extends JPanel implements Navigator {
        private final PreviewServer server;
        private final JPanel content = new JPanel(new CardLayout());
        private final Map<String, JButton> buttons = new LinkedHashMap<>();
        private final JPanel identity = new JPanel(new BorderLayout(10, 0));
        private final Runnable onSignOut;
        private Component current;

        Workspace(PreviewServer server, Runnable onSignOut) {
            super(new BorderLayout());
            this.server = server;
            this.onSignOut = onSignOut;
            add(sidebar(), BorderLayout.WEST);
            add(content, BorderLayout.CENTER);
            show("Chat", new ChatPage(server, this, null));
        }

        private JPanel sidebar() {
            JPanel side = Ui.column();
            side.setOpaque(true);
            side.setBackground(Ui.RAIL);
            side.setPreferredSize(new Dimension(224, 0));
            side.setBorder(BorderFactory.createEmptyBorder(16, 16, 15, 16));
            JLabel brand = Ui.label("ChatApp", 17, true);
            brand.setForeground(Color.WHITE);
            JLabel mark = new JLabel(Ui.icon("brand", 20), SwingConstants.CENTER);
            mark.setOpaque(true);
            mark.setBackground(Ui.BLUE);
            mark.setPreferredSize(new Dimension(34, 34));
            JPanel brandRow = Ui.row();
            brandRow.add(mark);
            brandRow.add(brand);
            side.add(brandRow);
            side.add(Box.createVerticalStrut(27));
            section(side, "Messages");
            link(side, "Chat", "chat");
            link(side, "Group", "group");
            section(side, "People");
            link(side, "Friend", "friend");
            section(side, "Settings");
            link(side, "Account", "account");
            side.add(Box.createVerticalGlue());
            JSeparator divider = new JSeparator();
            divider.setForeground(Ui.RAIL_ACTIVE);
            divider.setMaximumSize(new Dimension(192, 1));
            side.add(divider);
            side.add(Box.createVerticalStrut(12));
            identity.setOpaque(false);
            identity.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
            identity.setMaximumSize(new Dimension(192, 30));
            profileChanged();
            side.add(identity);
            side.add(Box.createVerticalStrut(6));
            JButton signOut = Ui.button("Sign Out", Ui.Style.LINK, null, this::signOut);
            signOut.setForeground(Ui.RAIL_MUTED);
            signOut.setFont(signOut.getFont().deriveFont(Font.PLAIN, 14f));
            signOut.setIcon(Ui.icon("exit", 17));
            signOut.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            signOut.setHorizontalAlignment(SwingConstants.LEFT);
            side.add(signOut);
            brandRow.setMaximumSize(new Dimension(192, brandRow.getPreferredSize().height));
            for (Component child : side.getComponents()) {
                if (child instanceof JComponent item) item.setAlignmentX(Component.LEFT_ALIGNMENT);
            }
            return side;
        }

        private void section(JPanel side, String text) {
            JLabel label = Ui.label("   " + text, 12, true);
            label.setForeground(Ui.RAIL_MUTED);
            side.add(Box.createVerticalStrut(12));
            side.add(label);
            side.add(Box.createVerticalStrut(4));
        }

        private void link(JPanel side, String name, String icon) {
            JButton button = new JButton(name) {
                @Override protected void paintComponent(Graphics graphics) {
                    Graphics2D g = (Graphics2D) graphics.create();
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g.setColor(isSelected() || getModel().isRollover() ? Ui.RAIL_ACTIVE : Ui.RAIL);
                    g.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g.dispose();
                    super.paintComponent(graphics);
                }
            };
            button.putClientProperty("navIcon", icon);
            button.setContentAreaFilled(false);
            button.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            button.setHorizontalAlignment(SwingConstants.LEFT);
            button.setIconTextGap(10);
            button.setFocusPainted(false);
            button.setPreferredSize(new Dimension(192, 39));
            button.setMaximumSize(new Dimension(192, 39));
            button.addActionListener(e -> showPage(name));
            buttons.put(name, button);
            side.add(button);
        }

        private void showPage(String name) {
            show(name, switch (name) {
                case "Group" -> new GroupPage(server, this);
                case "Friend" -> new FriendPage(server, this, "Friends");
                case "Account" -> new AccountPage(server, this);
                default -> new ChatPage(server, this, null);
            });
        }

        private void show(String name, Component page) {
            for (var entry : buttons.entrySet()) {
                JButton b = entry.getValue();
                boolean active = entry.getKey().equals(name);
                b.setSelected(active);
                b.setForeground(active ? Color.WHITE : Ui.RAIL_MUTED);
                b.setFont(new Font(Ui.FONT, active ? Font.BOLD : Font.PLAIN, 14));
                b.setIcon(Ui.icon(b.getClientProperty("navIcon") + (active ? "-active" : ""), 18));
            }
            if (current != null) content.remove(current);
            current = page;
            content.add(current, name);
            ((CardLayout) content.getLayout()).show(content, name);
            content.revalidate();
            content.repaint();
        }

        @Override public void openChat(long chatId) { show("Chat", new ChatPage(server, this, chatId)); }

        @Override public void openGroup(long groupId) { show("Group", GroupPage.detail(server, this, groupId)); }

        @Override public void openCreateGroup(List<String> usernames) {
            show("Group", GroupPage.create(server, this, usernames));
        }

        @Override public void openFriends(String tab) { show("Friend", new FriendPage(server, this, tab)); }

        @Override public void profileChanged() {
            identity.removeAll();
            var me = server.me();
            JComponent avatar = Ui.avatar(me.fullName(), false, 26);
            identity.add(avatar, BorderLayout.WEST);
            JLabel name = Ui.label(me.fullName(), 13, true);
            name.setForeground(Color.WHITE);
            name.setToolTipText("@" + me.username());
            identity.add(name, BorderLayout.CENTER);
            identity.revalidate();
            identity.repaint();
        }

        @Override public void signOut() {
            var me = server.me();
            if (Dialogs.confirm(this, "Sign out", me.fullName() + " (@" + me.username() + ")",
                    "You'll need your username and password to sign in again. Friends will see you as offline.",
                    "Sign out", Ui.Style.PRIMARY, "exit")) {
                server.signOut();
                onSignOut.run();
            }
        }
    }
}
