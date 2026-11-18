package chatapp.user.ui.shared;

import chatapp.user.preview.ServiceException;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Labeled form fields with an error line under each (Figma "User / Field": default, focused,
 * error and disabled states). A form keeps its fields by name, so a {@link ServiceException} with
 * field errors marks every failed field at once.
 */
public final class Form {
    /** One labeled input with its helper/error line. */
    public static final class Field extends JPanel {
        public final JComponent input;
        private final JLabel note;
        private final String helper;

        Field(String label, JComponent input, String helper) {
            super(new BorderLayout(0, 5));
            this.input = input;
            this.helper = helper;
            setOpaque(false);
            JLabel title = Ui.label(label, 13, true);
            title.setLabelFor(input);
            add(title, BorderLayout.NORTH);
            add(input, BorderLayout.CENTER);
            note = Ui.muted(helper == null ? " " : helper);
            note.setFont(note.getFont().deriveFont(12f));
            add(note, BorderLayout.SOUTH);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        public void error(String message) {
            Ui.invalid(input, message != null);
            note.setForeground(message != null ? Ui.RED : Ui.MUTED);
            note.setText(message != null ? message : helper == null ? " " : helper);
        }

        public String text() {
            if (input instanceof JPasswordField p) {
                char[] chars = p.getPassword();
                try { return new String(chars); } finally { Arrays.fill(chars, '\0'); }
            }
            if (input instanceof JTextField t) return t.getText();
            if (input instanceof JComboBox<?> box) return String.valueOf(box.getSelectedItem());
            return "";
        }

        public void set(String value) {
            if (input instanceof JTextField t) t.setText(value == null ? "" : value);
            else if (input instanceof JComboBox<?> box) box.setSelectedItem(value);
        }
    }

    private final Map<String, Field> fields = new LinkedHashMap<>();

    public Field text(String name, String label, String hint, String helper) {
        return put(name, new Field(label, Ui.field(hint, 18), helper));
    }

    public Field password(String name, String label, String hint, String helper) {
        return put(name, new Field(label, Ui.password(hint, 18), helper));
    }

    public Field choice(String name, String label, String[] options) {
        JComboBox<String> box = Ui.choice(options);
        box.setPreferredSize(new Dimension(box.getPreferredSize().width, Ui.field("", 1).getPreferredSize().height));
        return put(name, new Field(label, box, null));
    }

    private Field put(String name, Field field) {
        fields.put(name, field);
        return field;
    }

    public Field get(String name) {
        return fields.get(name);
    }

    public String value(String name) {
        return fields.get(name).text();
    }

    /** Clears every error, then marks the fields named in the exception. Returns true if any field matched. */
    public boolean show(ServiceException error) {
        clearErrors();
        boolean matched = false;
        for (var entry : error.fieldErrors().entrySet()) {
            Field field = fields.get(entry.getKey());
            if (field != null) { field.error(entry.getValue()); matched = true; }
        }
        return matched;
    }

    public void clearErrors() {
        for (Field field : fields.values()) field.error(null);
    }

    /** Empties password fields so secrets don't stay in memory after submit. */
    public void clearPasswords() {
        for (Field field : fields.values()) if (field.input instanceof JPasswordField p) p.setText("");
    }
}
