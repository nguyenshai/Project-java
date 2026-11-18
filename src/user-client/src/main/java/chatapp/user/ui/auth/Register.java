package chatapp.user.ui.auth;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views;
import chatapp.user.ui.shared.Form;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * F01 Register (Figma A4–A5). The server checks every field at once (unique username and email,
 * valid email and birth date, password length and match) and each failed field turns red.
 * A new account is signed in right away.
 */
public final class Register extends AuthCard {
    private final Form form = new Form();
    private final JLabel subtitle;

    public Register(PreviewServer server, Runnable onSignIn, Runnable onSignInPage) {
        super(720);
        brand();
        card.add(Ui.label("Create account", 26, true));
        gap(4);
        subtitle = Ui.muted("Address and date of birth are optional.");
        card.add(subtitle);
        gap(16);
        List<String> genders = new ArrayList<>(List.of("Select"));
        genders.addAll(Views.GENDERS);
        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 6));
        grid.setOpaque(false);
        grid.add(form.text("username", "Username", "Choose a username", null));
        grid.add(form.text("fullName", "Full name", "Your full name", null));
        grid.add(form.text("email", "Email", "name@example.com", null));
        grid.add(form.choice("gender", "Gender", genders.toArray(String[]::new)));
        grid.add(form.text("birthDate", "Date of birth (optional)", "YYYY-MM-DD", null));
        grid.add(form.text("address", "Address (optional)", "Street, city", null));
        grid.add(form.password("password", "Password", "At least " + PreviewServer.MIN_PASSWORD + " characters",
            "At least " + PreviewServer.MIN_PASSWORD + " characters."));
        grid.add(form.password("confirm", "Confirm password", "Re-enter the password", null));
        card.add(grid);
        gap(14);
        card.add(primary("Create account", null, () -> submit(server, onSignIn)));
        gap(10);
        card.add(prompt("Already have an account?", "Sign in", onSignInPage));
        finish();
    }

    private void submit(PreviewServer server, Runnable onSignIn) {
        try {
            server.register(form.value("username"), form.value("fullName"), form.value("email"), form.value("address"),
                form.value("birthDate"), form.value("gender"), form.value("password"), form.value("confirm"));
            form.clearPasswords();
            onSignIn.run();
        } catch (ServiceException e) {
            subtitle.setText(form.show(e) ? "Fix the highlighted fields." : e.getMessage());
            subtitle.setForeground(Ui.RED);
        }
        revalidate();
        repaint();
    }
}
