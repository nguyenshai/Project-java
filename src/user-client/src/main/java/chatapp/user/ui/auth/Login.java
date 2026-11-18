package chatapp.user.ui.auth;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.ui.shared.Form;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;

/**
 * F02 Log in (Figma A1–A3). Separate from the Admin Login. Missing fields are marked under each
 * field; a wrong password or a locked account shows a red banner above the fields.
 */
public final class Login extends AuthCard {
    private final Form form = new Form();
    private final JPanel banner = new JPanel(new BorderLayout());

    public Login(PreviewServer server, Runnable onSignIn, Runnable onRegister, Runnable onForgot) {
        super(400);
        brand();
        heading("Sign in", "Chat with your friends and groups.");
        banner.setOpaque(false);
        card.add(banner);
        Form.Field username = form.text("username", "Username", "Enter your username", null);
        Form.Field password = form.password("password", "Password", "Enter your password", null);
        card.add(username);
        gap(6);
        card.add(password);
        JPanel forgot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        forgot.setOpaque(false);
        forgot.add(Ui.button("Forgot password?", Ui.Style.LINK, null, onForgot));
        forgot.setMaximumSize(new Dimension(Integer.MAX_VALUE, forgot.getPreferredSize().height));
        card.add(forgot);
        gap(10);
        Runnable submit = () -> submit(server, onSignIn);
        card.add(primary("Sign in", null, submit));
        gap(10);
        card.add(prompt("New to ChatApp?", "Create account", onRegister));
        ((JTextField) username.input).addActionListener(e -> submit.run());
        ((JTextField) password.input).addActionListener(e -> submit.run());
        finish();
    }

    /** Clears the form so the next person starts from an empty page. */
    public void reset() {
        form.get("username").set("");
        form.clearPasswords();
        form.clearErrors();
        banner.removeAll();
        revalidate();
    }

    private void submit(PreviewServer server, Runnable onSignIn) {
        banner.removeAll();
        try {
            server.signIn(form.value("username"), form.value("password"));
            reset();
            onSignIn.run();
        } catch (ServiceException e) {
            if (!form.show(e)) {
                form.clearPasswords();
                boolean locked = e.code() == ServiceException.Code.ACCOUNT_LOCKED;
                banner.add(Ui.banner(Ui.Banner.ERROR, locked ? "lock" : "alert", e.getMessage()));
                banner.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
            }
        }
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, banner.getPreferredSize().height));
        revalidate();
        repaint();
    }
}
