package chatapp.admin.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;

public final class PeriodPicker {
    public record Range(LocalDate from, LocalDate to) {
        public Range {
            if (from.isAfter(to)) throw new IllegalArgumentException("From must be on or before To.");
            if (from.getYear() < 1900 || to.getYear() > 2200) {
                throw new IllegalArgumentException("Choose dates from 1900 to 2200.");
            }
        }
        public boolean includes(LocalDate date) {
            return !date.isBefore(from) && !date.isAfter(to);
        }
        public static Range month(int year, Month month) {
            YearMonth m = YearMonth.of(year, month);
            return new Range(m.atDay(1), m.atEndOfMonth());
        }
        public static Range year(int year) {
            return new Range(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
        }
    }

    private int year;

    public PeriodPicker(int year) {
        this.year = year;
    }

    public int year() {
        return year;
    }

    public Range range() {
        return Range.year(year);
    }

    public JButton button(Runnable onChange) {
        JButton button = Ui.button("Period: " + year, () -> {});
        button.setIcon(Ui.icon("down", 15));
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setIconTextGap(10);

        JPopupMenu popup = new JPopupMenu();
        JPanel content = Ui.card(new BorderLayout(0, 8));
        JSpinner input = new JSpinner(new SpinnerNumberModel(year, 1900, 2200, 1));
        var editor = new JSpinner.NumberEditor(input, "0");
        input.setEditor(editor);
        JPanel fields = Ui.row();
        fields.add(Ui.muted("Year"));
        fields.add(input);
        content.add(fields, BorderLayout.NORTH);

        JLabel feedback = Ui.muted("");
        feedback.setForeground(Ui.RED);
        content.add(feedback, BorderLayout.CENTER);
        JPanel actions = Ui.row();
        actions.add(Ui.button("Cancel", () -> popup.setVisible(false)));
        actions.add(Ui.button("Apply", Ui.BLUE, Color.WHITE, () -> {
            try {
                input.commitEdit();
            } catch (java.text.ParseException e) {
                feedback.setText("Enter a year from 1900 to 2200.");
                popup.pack();
                return;
            }
            int selected = (Integer) input.getValue();
            popup.setVisible(false);
            if (year != selected) {
                year = selected;
                button.setText("Period: " + year);
                onChange.run();
            }
        }));
        content.add(actions, BorderLayout.SOUTH);
        popup.add(content);
        popup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                editor.getTextField().setValue(year);
                feedback.setText("");
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });
        button.setComponentPopupMenu(popup);
        button.addActionListener(e -> popup.show(button, 0, button.getHeight() + 4));
        return button;
    }
}
