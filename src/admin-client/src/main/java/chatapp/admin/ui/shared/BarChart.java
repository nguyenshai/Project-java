package chatapp.admin.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Arrays;

public final class BarChart extends JPanel {
    private String[] labels;
    private int[] values;

    public BarChart(String[] labels, int[] values) {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 178));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 178));
        setSeries(labels, values);
    }

    public void setSeries(String[] labels, int[] values) {
        this.labels = labels.clone();
        this.values = values.clone();
        setToolTipText(values.length == 0 ? null : Arrays.toString(values));
        repaint();
    }

    public static BarChart forDates(PeriodPicker.Range period, List<LocalDate> dates) {
        long days = ChronoUnit.DAYS.between(period.from(), period.to());
        if (days <= 30) {
            var buckets = period.from().datesUntil(period.to().plusDays(1)).toList();
            return new BarChart(buckets.stream().map(d -> buckets.size() <= 14 ? d.getMonthValue() + "/" + d.getDayOfMonth() : "").toArray(String[]::new),
                buckets.stream().mapToInt(d -> (int) dates.stream().filter(d::equals).count()).toArray());
        }
        YearMonth start = YearMonth.from(period.from()), end = YearMonth.from(period.to());
        if (ChronoUnit.MONTHS.between(start, end) >= 36) {
            int first = start.getYear(), last = end.getYear();
            return new BarChart(java.util.stream.IntStream.rangeClosed(first, last).mapToObj(y -> last - first <= 14 ? String.valueOf(y) : "").toArray(String[]::new),
                java.util.stream.IntStream.rangeClosed(first, last).map(y -> (int) dates.stream().filter(d -> d.getYear() == y).count()).toArray());
        }
        var months = java.util.stream.Stream.iterate(start, d -> !d.isAfter(end), d -> d.plusMonths(1)).toList();
        return new BarChart(months.stream().map(d -> months.size() <= 14 ? d.getMonthValue() + "/" + d.getYear() % 100 : "").toArray(String[]::new),
            months.stream().mapToInt(m -> (int) dates.stream().filter(d -> YearMonth.from(d).equals(m)).count()).toArray());
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setFont(Ui.muted("").getFont().deriveFont(11f));
        if (values.length == 0 || Arrays.stream(values).noneMatch(value -> value > 0)) {
            g.setColor(Ui.MUTED);
            g.drawString("No records for this period", 34, getHeight() / 2);
            g.dispose();
            return;
        }
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
            g.drawString(labels[i], mid - g.getFontMetrics().stringWidth(labels[i]) / 2, base + 17);
        }
        g.dispose();
    }
}
