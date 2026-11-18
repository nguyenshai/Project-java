package chatapp.user.ui.auth;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views.Email;
import chatapp.user.ui.shared.Form;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;

/**
 * F04 Reset password (Figma A6–A7). The server sets a random password and emails it; the preview
 * shows that email in a card ("Check your email"). A real SMTP send comes in Version 2.
 */
public final class ForgotPassword extends JPanel {
    private final PreviewServer server;
    private final Runnable onBack;

    public ForgotPassword(PreviewServer server, Runnable onBack) {
        super(new BorderLayout());
        this.server = server;
        this.onBack = onBack;
        request();
    }

    private void request() {
        Form form = new Form();
        AuthCard page = new AuthCard(400) {{
            JButton back = Ui.button("Back to sign in", Ui.Style.LINK, "back", onBack);
            card.add(back);
            gap(12);
            brand();
            heading("Reset password", "Enter the email on your account. We'll generate a new random password and send it to you.");
            Form.Field email = form.text("email", "Email", "name@example.com", null);
            card.add(email);
            gap(6);
            Runnable submit = () -> {
                try {
                    sent(server.resetPassword(form.value("email")));
                } catch (ServiceException e) {
                    if (!form.show(e)) email.error(e.getMessage());
                }
            };
            ((JTextField) email.input).addActionListener(e -> submit.run());
            card.add(primary("Send new password", "mail", submit));
            finish();
        }};
        show(page);
    }

    private void sent(Email mail) {
        AuthCard page = new AuthCard(480) {{
            brand();
            heading("Check your email", null);
            card.add(Ui.banner(Ui.Banner.SUCCESS, "A new password was sent to " + mail.to() + "."));
            gap(14);
            JPanel preview = new JPanel();
            preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));
            preview.setBackground(Ui.SOFT);
            preview.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
            JLabel from = Ui.muted("From: ChatApp <no-reply@chatapp.local>");
            JLabel to = Ui.muted("To: " + mail.to());
            JLabel subject = Ui.label("Subject: " + mail.subject(), 13, true);
            JTextArea body = Ui.paragraph(mail.body(), 13, Ui.INK, 360);
            for (JComponent c : new JComponent[]{from, to, subject, body}) {
                c.setAlignmentX(LEFT_ALIGNMENT);
                preview.add(c);
                preview.add(Box.createVerticalStrut(5));
            }
            preview.getAccessibleContext().setAccessibleName("Email preview");
            card.add(preview);
            gap(14);
            card.add(primary("Back to sign in", null, onBack));
            finish();
        }};
        show(page);
    }

    private void show(JComponent page) {
        removeAll();
        add(page);
        revalidate();
        repaint();
    }
}
