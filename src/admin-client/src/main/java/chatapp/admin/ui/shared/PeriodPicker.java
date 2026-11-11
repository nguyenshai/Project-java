package chatapp.admin.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.Consumer;

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

    public static final Range SAMPLE = new Range(LocalDate.of(2026, 11, 3), LocalDate.of(2026, 11, 9));
    private static final DateTimeFormatter LABEL = DateTimeFormatter.ofPattern("MMM d, uuuu", Locale.ENGLISH);

    private PeriodPicker() {}

    public static JButton range(Range initial, Consumer<Range> onChange) {
        return picker(initial, false, false, onChange);
    }

    public static JButton customDates(Range initial, Consumer<Range> onChange) {
        return picker(initial, false, true, onChange);
    }

    public static JButton year(int initial, Consumer<Integer> onChange) {
        return picker(Range.year(initial), true, false, r -> onChange.accept(r.from().getYear()));
    }

    private static JButton picker(Range initial, boolean yearOnly, boolean customOnly, Consumer<Range> change) {
        JButton button = Ui.button(title(initial, yearOnly), () -> {});
        button.setIcon(Ui.icon("down", 15));
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setIconTextGap(10);
        Range[] current = {initial};
        button.addActionListener(e -> {
            JPopupMenu popup = new JPopupMenu();
            JPanel content = Ui.card(new BorderLayout(0, 8));
            JPanel fields = new JPanel(new CardLayout());
            JPanel modes = Ui.row();
            JToggleButton monthMode = new JToggleButton("Month");
            JToggleButton yearMode = new JToggleButton("Year");
            JToggleButton datesMode = new JToggleButton("Custom dates", true);
            ButtonGroup group = new ButtonGroup();
            for (JToggleButton mode : new JToggleButton[]{monthMode, yearMode, datesMode}) {
                group.add(mode);
                modes.add(mode);
                mode.addActionListener(event -> ((CardLayout) fields.getLayout()).show(fields, mode.getText()));
            }
            var months = Ui.choice(Month.values());
            months.setSelectedItem(current[0].from().getMonth());
            SpinnerNumberModel years = new SpinnerNumberModel(current[0].from().getYear(), 1900, 2200, 1);
            JSpinner monthYear = new JSpinner(years);
            JSpinner year = new JSpinner(years);
            JTextField from = new JTextField(current[0].from().toString(), 10);
            JTextField to = new JTextField(current[0].to().toString(), 10);
            JPanel monthFields = Ui.row(), yearFields = Ui.row(), dateFields = Ui.row();
            monthFields.add(Ui.muted("Month"));
            monthFields.add(months);
            monthFields.add(Ui.muted("Year"));
            monthFields.add(monthYear);
            yearFields.add(Ui.muted("Year"));
            yearFields.add(year);
            dateFields.add(Ui.muted("From"));
            dateFields.add(from);
            dateFields.add(Ui.muted("To"));
            dateFields.add(to);
            fields.add(dateFields, "Custom dates");
            fields.add(monthFields, "Month");
            fields.add(yearFields, "Year");
            if (yearOnly) {
                yearMode.setSelected(true);
                ((CardLayout) fields.getLayout()).show(fields, "Year");
            } else if (!customOnly) {
                content.add(modes, BorderLayout.NORTH);
            }
            content.add(fields, BorderLayout.CENTER);
            JPanel actions = Ui.row();
            JLabel feedback = Ui.muted("");
            feedback.setForeground(Ui.RED);
            JPanel footer = Ui.column();
            footer.add(feedback);
            footer.add(actions);
            actions.add(Ui.button("Cancel", () -> popup.setVisible(false)));
            actions.add(Ui.button("Apply", Ui.BLUE, Color.WHITE, () -> {
                try {
                    Range result;
                    if (yearMode.isSelected()) {
                        year.commitEdit();
                        result = Range.year((Integer) year.getValue());
                    } else if (monthMode.isSelected()) {
                        monthYear.commitEdit();
                        result = Range.month((Integer) monthYear.getValue(), (Month) months.getSelectedItem());
                    } else {
                        result = new Range(LocalDate.parse(from.getText().trim()), LocalDate.parse(to.getText().trim()));
                    }
                    current[0] = result;
                    button.setText(title(result, yearOnly));
                    popup.setVisible(false);
                    change.accept(result);
                } catch (java.time.DateTimeException | java.text.ParseException ex) {
                    feedback.setText("Enter a valid year or date (YYYY-MM-DD).");
                    popup.pack();
                } catch (IllegalArgumentException ex) {
                    feedback.setText(ex.getMessage());
                    popup.pack();
                }
            }));
            content.add(footer, BorderLayout.SOUTH);
            popup.add(content);
            popup.show(button, 0, button.getHeight() + 4);
        });
        return button;
    }

    private static String title(Range range, boolean yearOnly) {
        return yearOnly ? "Period: " + range.from().getYear()
            : "Period: " + LABEL.format(range.from()) + " – " + LABEL.format(range.to());
    }
}
