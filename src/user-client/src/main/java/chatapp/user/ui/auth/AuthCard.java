package chatapp.user.ui.auth;

import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;

/** The centered white card shared by Login, Register and Forgot password (Figma "Auth card"). */
abstract class AuthCard extends JPanel {
    protected final JPanel card;
    private final int width;

    protected AuthCard(int width) {
        super(new GridBagLayout());
        this.width = width;
        card = new JPanel() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(width - 64, super.getPreferredSize().height);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        setBackground(Ui.CANVAS);
        JPanel frame = new JPanel(new BorderLayout());
        frame.setBackground(Color.WHITE);
        frame.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.LINE),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)));
        frame.add(card);
        add(frame);
    }

    /** Left-aligns everything once the content is added (BoxLayout needs one alignment). */
    protected void finish() {
        for (Component c : card.getComponents()) if (c instanceof JComponent j) j.setAlignmentX(LEFT_ALIGNMENT);
        card.revalidate();
    }

    protected void brand() {
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel(Ui.icon("brand", 20), SwingConstants.CENTER);
        mark.setOpaque(true);
        mark.setBackground(Ui.BLUE);
        mark.setPreferredSize(new Dimension(34, 34));
        brand.add(mark);
        brand.add(Box.createHorizontalStrut(10));
        brand.add(Ui.label("ChatApp", 17, true));
        card.add(brand);
        card.add(Box.createVerticalStrut(16));
    }

    protected void heading(String title, String subtitle) {
        card.add(Ui.label(title, 26, true));
        if (subtitle != null) {
            card.add(Box.createVerticalStrut(4));
            card.add(Ui.paragraph(subtitle, 13, Ui.MUTED, width - 64));
        }
        card.add(Box.createVerticalStrut(16));
    }

    protected void gap(int height) {
        card.add(Box.createVerticalStrut(height));
    }

    /** A full-width primary button. */
    protected JButton primary(String title, String icon, Runnable action) {
        JButton button = Ui.button(title, Ui.Style.PRIMARY, icon, action);
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setPreferredSize(new Dimension(width - 64, 38));
        return button;
    }

    /** "New to ChatApp?  Create account" style line, centered. */
    protected JPanel prompt(String text, String link, Runnable action) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        line.setOpaque(false);
        line.add(Ui.muted(text));
        line.add(Ui.button(link, Ui.Style.LINK, null, action));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, line.getPreferredSize().height));
        return line;
    }
}
