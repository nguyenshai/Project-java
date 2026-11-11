package chatapp.admin;

import chatapp.admin.ui.AdminFrame;
import javax.swing.SwingUtilities;

public final class AdminApp {
    private AdminApp() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminFrame().setVisible(true));
    }
}
