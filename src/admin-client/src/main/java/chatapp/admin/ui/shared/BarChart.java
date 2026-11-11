package chatapp.admin.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.List;
import java.util.Arrays;

public final class BarChart extends JPanel {
    private static final String[] MONTHS = Arrays.stream(Month.values())
        .map(month -> month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)).toArray(String[]::new);
    private int[] values;

    public BarChart(int[] values) {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 178));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 178));
        getAccessibleContext().setAccessibleName("Monthly chart, January through December");
        setSeries(values);
    }

    public void setSeries(int[] values) {
        if (values.length != MONTHS.length) throw new IllegalArgumentException("Expected twelve monthly values.");
        this.values = values.clone();
        setToolTipText(Arrays.toString(values));
        repaint();
    }

    public static BarChart forDates(int year, List<LocalDate> dates) {
        int[] counts = new int[MONTHS.length];
        for (LocalDate date : dates) {
            if (date.getYear() == year) counts[date.getMonthValue() - 1]++;
        }
        return new BarChart(counts);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(Ui.muted("").getFont().deriveFont(11f));
        int left = 34, top = 12, base = getHeight() - 28, height = base - top;
        int plotWidth = Math.max(1, getWidth() - left - 12);
        int step = Math.max(1, (int) Math.ceil(Arrays.stream(values).max().orElse(0) / 4.0));
        int max = step * 4;
        for (int i = 0; i <= 4; i++) {
            int y = base - i * height / 4;
            g.setColor(Ui.LINE);
            g.drawLine(left, y, left + plotWidth, y);
            g.setColor(Ui.MUTED);
            g.drawString(String.valueOf(i * step), 2, y + 4);
        }
        for (int i = 0; i < values.length; i++) {
            int mid = left + (int) ((i + .5) * plotWidth / values.length);
            int width = Math.min(36, Math.max(3, plotWidth / values.length / 2));
            int bar = values[i] * height / max;
            g.setColor(Ui.BLUE);
            if (bar > 0) g.fillRoundRect(mid - width / 2, base - bar, width, bar, 4, 4);
            g.setColor(Ui.MUTED);
            g.drawString(MONTHS[i], mid - g.getFontMetrics().stringWidth(MONTHS[i]) / 2, base + 17);
        }
        if (Arrays.stream(values).allMatch(value -> value == 0)) {
            String message = "No records for this year";
            g.drawString(message, left + (plotWidth - g.getFontMetrics().stringWidth(message)) / 2, top + height / 2);
        }
        g.dispose();
    }
}
