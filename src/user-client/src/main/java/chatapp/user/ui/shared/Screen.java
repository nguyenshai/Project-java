package chatapp.user.ui.shared;

import javax.swing.*;
import java.awt.*;

/**
 * Base class for the scrolling User pages (Group, Friend, Account). Copied from the Admin client's
 * {@code Screen}: {@code page(title)} starts a page and {@code display(page)} rebuilds the body.
 * Adds a one-shot notice (success or error banner shown under the title on the next render) and the
 * Figma "← Back" link.
 */
public abstract class Screen extends JPanel {
    private Ui.Banner noticeKind;
    private String noticeText;

    protected Screen() {
        super(new BorderLayout());
        setBackground(Ui.CANVAS);
    }

    protected JPanel page(String title) {
        JPanel page = Ui.column();
        page.setBorder(BorderFactory.createEmptyBorder(30, 32, 20, 32));
        Ui.add(page, Ui.label(title, 32, true));
        return page;
    }

    /** A page whose title has buttons on the right, e.g. "Group" + "Create group". */
    protected JPanel page(String title, JComponent... actions) {
        JPanel page = Ui.column();
        page.setBorder(BorderFactory.createEmptyBorder(30, 32, 20, 32));
        Ui.add(page, Ui.header(Ui.label(title, 32, true), actions));
        return page;
    }

    /** Adds the blue "← target" link above the title. */
    protected void back(JPanel page, String target, Runnable action) {
        JButton link = Ui.button(target, Ui.Style.LINK, "back", action);
        link.setAlignmentX(Component.LEFT_ALIGNMENT);
        page.add(link, 0);
        page.add(Box.createVerticalStrut(4), 1);
    }

    /** Shows a banner under the title on the next {@link #display}. */
    protected void notice(Ui.Banner kind, String text) {
        noticeKind = kind;
        noticeText = text;
    }

    protected void display(JPanel page) {
        if (noticeText != null) {
            int index = Math.min(page.getComponentCount(), indexAfterTitle(page));
            page.add(Ui.banner(noticeKind, noticeText), index);
            page.add(Box.createVerticalStrut(14), index + 1);
            noticeText = null;
        }
        removeAll();
        JPanel body = new PageBody();
        body.setBackground(Ui.CANVAS);
        body.add(page, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(body, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Ui.CANVAS);
        scroll.getVerticalScrollBar().setUnitIncrement(24);
        add(scroll, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /** Index just after the title (and its spacer), skipping a back link if there is one. */
    private static int indexAfterTitle(JPanel page) {
        for (int i = 0; i < page.getComponentCount(); i++) {
            Component c = page.getComponent(i);
            if (c instanceof JLabel || c instanceof JPanel) return i + 2;
        }
        return page.getComponentCount();
    }

    private static final class PageBody extends JPanel implements Scrollable {
        PageBody() {
            super(new BorderLayout());
        }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 24; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) {
            return Math.max(24, visible.height - 24);
        }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
