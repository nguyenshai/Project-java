package chatapp.admin.ui;

import chatapp.admin.ui.accounts.Accounts;
import chatapp.admin.ui.analytics.Analytics;
import chatapp.admin.ui.moderation.Moderation;
import chatapp.admin.ui.preview.PreviewData;
import chatapp.admin.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AdminFrame extends JFrame {
    public AdminFrame() {
        super("ChatApp Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(createContent());
        setSize(1280, 800);
        setMinimumSize(new Dimension(1050, 660));
        setLocationRelativeTo(null);
    }

    public static JPanel createContent() {
        Ui.install();
        return new Workspace();
    }

    private static final class Workspace extends JPanel {
        private final PreviewData data = new PreviewData();
        private final JPanel content = new JPanel(new CardLayout());
        private final Map<String, JButton> buttons = new LinkedHashMap<>();
        private Component current;

        Workspace() {
            super(new BorderLayout());
            add(sidebar(), BorderLayout.WEST);
            add(content, BorderLayout.CENTER);
            showPage("Users");
        }

        private JPanel sidebar() {
            JPanel side = Ui.column();
            side.setOpaque(true);
            side.setBackground(Ui.RAIL);
            side.setPreferredSize(new Dimension(224, 0));
            side.setBorder(BorderFactory.createEmptyBorder(16, 16, 15, 16));
            JLabel brand = Ui.label("ChatApp Admin", 17, true);
            brand.setForeground(Color.WHITE);
            JLabel mark = new JLabel(Ui.icon("brand", 22), SwingConstants.CENTER);
            mark.setOpaque(true);
            mark.setBackground(Ui.BLUE);
            mark.setPreferredSize(new Dimension(34, 34));
            JPanel brandRow = Ui.row();
            brandRow.add(mark);
            brandRow.add(brand);
            side.add(brandRow);
            side.add(Box.createVerticalStrut(27));
            section(side, "Manage Accounts");
            link(side, "Users", "users");
            section(side, "Monitor & Moderate");
            link(side, "Account History", "clock");
            link(side, "Chat Groups", "group");
            link(side, "Spam Reports", "flag");
            section(side, "Analyze Usage");
            link(side, "Analytics", "chart");
            side.add(Box.createVerticalGlue());
            JSeparator divider = new JSeparator();
            divider.setForeground(Ui.RAIL_ACTIVE);
            divider.setMaximumSize(new Dimension(192, 1));
            side.add(divider);
            JLabel admin = Ui.label("   AM    Alex Morgan", 13, true);
            admin.setForeground(Color.WHITE);
            side.add(Box.createVerticalStrut(12));
            side.add(admin);
            side.add(Box.createVerticalStrut(6));
            JButton signOut = Ui.button("Sign Out", Ui.RAIL, Ui.RAIL_MUTED, () -> {
                if (JOptionPane.showConfirmDialog(this, "Close the Admin preview?", "Sign Out",
                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    Window window = SwingUtilities.getWindowAncestor(this);
                    if (window != null) window.dispose();
                }
            });
            signOut.setIcon(Ui.icon("exit", 17));
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
            button.setPreferredSize(new Dimension(192, 39));
            button.setMaximumSize(new Dimension(192, 39));
            button.addActionListener(e -> showPage(name));
            buttons.put(name, button);
            side.add(button);
        }

        private void showPage(String name) {
            for (var entry : buttons.entrySet()) {
                JButton b = entry.getValue();
                boolean active = entry.getKey().equals(name);
                b.setSelected(active);
                b.setForeground(active ? Color.WHITE : Ui.RAIL_MUTED);
                b.setIcon(Ui.icon(b.getClientProperty("navIcon") + (active ? "-active" : ""), 18));
            }
            if (current != null) content.remove(current);
            current = switch (name) {
                case "Users" -> new Accounts(data);
                case "Analytics" -> new Analytics(data);
                default -> new Moderation(data, name);
            };
            content.add(current, name);
            ((CardLayout) content.getLayout()).show(content, name);
            content.revalidate();
            content.repaint();
        }
    }
}
