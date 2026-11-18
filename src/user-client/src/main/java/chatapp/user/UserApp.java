package chatapp.user;

import chatapp.user.ui.UserFrame;
import javax.swing.SwingUtilities;

/**
 * Starts the User client (Version 1 preview). Sign in with any sample account, e.g. {@code mayachen}
 * / {@code chatapp123}, or create a new account on the Register page.
 */
public final class UserApp {
    private UserApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserFrame().setVisible(true));
    }
}
