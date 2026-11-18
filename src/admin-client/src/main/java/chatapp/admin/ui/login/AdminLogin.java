package chatapp.admin.ui.login;

import chatapp.admin.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Sign-in page of the Admin client, separate from the User client's login.
 * The V1 preview only checks that both fields are filled; the server will verify
 * the credentials and that the account has the ADMIN system role.
 */
public final class AdminLogin extends JPanel {
    private final JTextField username = Ui.field("Enter your username", 22);
    private final JPasswordField password = Ui.password("Enter your password", 22);
    private final JLabel error = Ui.muted(" ");

    public AdminLogin(Runnable onSignIn) {
        super(new GridBagLayout());
        setBackground(Ui.CANVAS);

        JPanel card = Ui.card(new BorderLayout(0, 18));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.LINE),
            BorderFactory.createEmptyBorder(28, 32, 28, 32)));

        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        brand.setOpaque(false);
        JLabel mark = new JLabel(Ui.icon("brand", 22), SwingConstants.CENTER);
        mark.setOpaque(true);
        mark.setBackground(Ui.BLUE);
        mark.setPreferredSize(new Dimension(34, 34));
        brand.add(mark);
        brand.add(Box.createHorizontalStrut(10));
        brand.add(Ui.label("ChatApp Admin", 17, true));
        JPanel heading = Ui.column();
        heading.add(brand);
        heading.add(Box.createVerticalStrut(18));
        heading.add(Ui.label("Sign in", 26, true));
        heading.add(Box.createVerticalStrut(4));
        heading.add(Ui.muted("For system administrators only."));
        for (Component child : heading.getComponents()) ((JComponent) child).setAlignmentX(LEFT_ALIGNMENT);
        card.add(heading, BorderLayout.NORTH);

        JPanel fields = Ui.column();
        Ui.add(fields, labeled("Username", username));
        Ui.add(fields, labeled("Password", password));
        error.setForeground(Ui.RED);
        error.setAlignmentX(LEFT_ALIGNMENT);
        fields.add(error);
        card.add(fields);

        JButton signIn = Ui.button("Sign in", Ui.BLUE, Color.WHITE, () -> submit(onSignIn));
        signIn.setPreferredSize(new Dimension(0, 40));
        card.add(signIn, BorderLayout.SOUTH);
        username.addActionListener(e -> submit(onSignIn));
        password.addActionListener(e -> submit(onSignIn));

        card.setPreferredSize(new Dimension(400, card.getPreferredSize().height));
        add(card);
    }

    /** Clears the form so the next administrator starts from an empty page. */
    public void reset() {
        username.setText("");
        password.setText("");
        error.setText(" ");
    }

    private void submit(Runnable onSignIn) {
        char[] secret = password.getPassword();
        try {
            if (username.getText().isBlank() || secret.length == 0) {
                error.setText("Enter your username and password.");
                return;
            }
        } finally {
            Arrays.fill(secret, '\0');
        }
        reset();
        onSignIn.run();
    }

    private static JPanel labeled(String title, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        JLabel label = Ui.label(title, 13, true);
        label.setLabelFor(field);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }
}
