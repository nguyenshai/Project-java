package chatapp.admin.ui.shared;

import javax.swing.*;
import java.awt.*;

public abstract class Screen extends JPanel {
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

    protected void display(JPanel page) {
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
