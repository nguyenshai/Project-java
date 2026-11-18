package chatapp.user.ui.shared;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;

/**
 * Confirm dialogs in the Figma layout: title, a bold subject line, a short explanation, optional
 * extra content (e.g. the spam reason choices) and Cancel + an action button. Tests can answer
 * dialogs without showing a window through {@link #answerForTests}.
 */
public final class Dialogs {
    /** What a dialog asks; {@code extra} may be null. */
    public record Ask(String title, String subject, String body, JComponent extra, String action, Ui.Style style,
                      String icon) {}

    private static Predicate<Ask> testAnswer;

    private Dialogs() {}

    /** Answers every following dialog with the predicate instead of showing it; null restores real dialogs. */
    public static void answerForTests(Predicate<Ask> answer) {
        testAnswer = answer;
    }

    /** Shows the dialog; true when the user chose the action button. */
    public static boolean confirm(Component parent, Ask ask) {
        if (testAnswer != null) return testAnswer.test(ask);
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner, ask.title(), Dialog.ModalityType.APPLICATION_MODAL);
        boolean[] chosen = {false};
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));
        JPanel text = Ui.column();
        text.add(Ui.label(ask.title(), 20, true));
        if (ask.subject() != null) {
            text.add(Box.createVerticalStrut(8));
            JLabel subject = Ui.label(ask.subject(), 13, true);
            subject.setForeground(Ui.MUTED);
            text.add(subject);
        }
        if (ask.body() != null) {
            text.add(Box.createVerticalStrut(8));
            text.add(Ui.paragraph(ask.body(), 13, Ui.INK, 400));
        }
        if (ask.extra() != null) {
            text.add(Box.createVerticalStrut(12));
            text.add(ask.extra());
        }
        for (Component c : text.getComponents()) ((JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(text, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        JButton cancel = Ui.button("Cancel", dialog::dispose);
        JButton action = Ui.button(ask.action(), ask.style(), ask.icon(), () -> { chosen[0] = true; dialog.dispose(); });
        buttons.add(cancel);
        buttons.add(action);
        content.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(action);
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(), KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(cancel::requestFocusInWindow);
        dialog.setVisible(true);
        return chosen[0];
    }

    public static boolean confirm(Component parent, String title, String subject, String body, String action,
                                  Ui.Style style, String icon) {
        return confirm(parent, new Ask(title, subject, body, null, action, style, icon));
    }
}
