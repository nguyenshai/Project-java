package chatapp.user.ui.account;

import chatapp.user.preview.PreviewServer;
import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views;
import chatapp.user.preview.Views.Account;
import chatapp.user.ui.Navigator;
import chatapp.user.ui.shared.Form;
import chatapp.user.ui.shared.Screen;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Account page (Figma AC1–AC5): profile card, Edit profile (F03, username read-only) and
 * Change password (F05). Signing out is in the sidebar (AC6).
 */
public final class AccountPage extends Screen {
    private final PreviewServer server;
    private final Navigator nav;

    public AccountPage(PreviewServer server, Navigator nav) {
        this.server = server;
        this.nav = nav;
        profile();
    }

    private void profile() {
        Account me = server.me();
        JPanel page = page("Account");
        JPanel who = new JPanel(new BorderLayout(14, 0));
        who.setOpaque(false);
        who.add(Ui.avatar(me.fullName(), false, 52), BorderLayout.WEST);
        JPanel names = Ui.column();
        names.add(Box.createVerticalStrut(5));
        names.add(Ui.label(me.fullName(), 18, true));
        names.add(Ui.muted("@" + me.username() + "   ·   Member since " + Ui.date(me.createdAt())));
        who.add(names);
        Ui.add(page, who);

        JPanel card = Ui.card(new BorderLayout(0, 14));
        card.add(Ui.label("Account details", 17, true), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(2, 3, 16, 14));
        fields.setOpaque(false);
        fields.add(info("Full name", me.fullName()));
        fields.add(info("Username", me.username()));
        fields.add(info("Email", me.email()));
        fields.add(info("Date of birth", me.birthDate() == null ? "" : Ui.DATE.format(me.birthDate())));
        fields.add(info("Gender", me.gender()));
        fields.add(info("Address", me.address()));
        card.add(fields);
        JPanel actions = Ui.row();
        actions.add(Ui.button("Edit profile", Ui.Style.PRIMARY, "edit", this::edit));
        actions.add(Ui.button("Change password", Ui.Style.SECONDARY, "key", this::password));
        card.add(actions, BorderLayout.SOUTH);
        Ui.add(page, card);
        display(page);
    }

    private static JPanel info(String name, String value) {
        JPanel panel = Ui.column();
        JLabel title = Ui.muted(name);
        title.setFont(title.getFont().deriveFont(12f));
        panel.add(title);
        panel.add(Box.createVerticalStrut(2));
        panel.add(Ui.label(value == null || value.isBlank() ? "—" : value, 14, false));
        return panel;
    }

    private void edit() {
        Account me = server.me();
        JPanel page = page("Edit profile");
        back(page, "Account", this::profile);
        JPanel errorSlot = new JPanel(new BorderLayout());
        errorSlot.setOpaque(false);
        Ui.add(page, errorSlot);
        Form form = new Form();
        List<String> genders = new ArrayList<>(List.of("Select"));
        genders.addAll(Views.GENDERS);
        JPanel grid = new JPanel(new GridLayout(0, 3, 16, 6));
        grid.setOpaque(false);
        grid.add(form.text("fullName", "Full name", "Your full name", null));
        Form.Field username = form.text("username", "Username", "Username", "Username can't be changed.");
        Ui.readOnly((JTextField) username.input);
        grid.add(username);
        grid.add(form.text("email", "Email", "name@example.com", null));
        grid.add(form.choice("gender", "Gender", genders.toArray(String[]::new)));
        grid.add(form.text("birthDate", "Date of birth (optional)", "YYYY-MM-DD", null));
        grid.add(form.text("address", "Address (optional)", "Street, city", null));
        form.get("fullName").set(me.fullName());
        username.set(me.username());
        form.get("email").set(me.email());
        form.get("gender").set(Views.GENDERS.contains(me.gender()) ? me.gender() : "Select");
        form.get("birthDate").set(me.birthDate() == null ? "" : me.birthDate().toString());
        form.get("address").set(me.address());

        JPanel card = Ui.card(new BorderLayout(0, 12));
        card.add(grid);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(Ui.button("Cancel", this::profile));
        actions.add(Ui.button("Save changes", Ui.Style.PRIMARY, "check", () -> {
            try {
                server.updateProfile(form.value("fullName"), form.value("email"), form.value("address"),
                    form.value("birthDate"), form.value("gender"));
                nav.profileChanged();
                notice(Ui.Banner.SUCCESS, "Profile updated.");
                profile();
            } catch (ServiceException e) {
                boolean fields = form.show(e);
                errorSlot.removeAll();
                errorSlot.add(Ui.banner(Ui.Banner.ERROR, fields ? "Fix the highlighted fields and save again." : e.getMessage()));
                page.revalidate();
                page.repaint();
            }
        }));
        card.add(actions, BorderLayout.SOUTH);
        Ui.add(page, card);
        display(page);
    }

    private void password() {
        JPanel page = page("Change password");
        back(page, "Account", this::profile);
        JPanel slot = new JPanel(new BorderLayout());
        slot.setOpaque(false);
        Form form = new Form();
        JPanel card = Ui.card(new BorderLayout(0, 12));
        JPanel fields = Ui.column();
        JPanel banner = new JPanel(new BorderLayout());
        banner.setOpaque(false);
        fields.add(banner);
        for (Form.Field f : List.of(
                form.password("current", "Current password", "Enter current password", null),
                form.password("password", "New password", "Enter new password", "At least " + PreviewServer.MIN_PASSWORD + " characters."),
                form.password("confirm", "Confirm new password", "Re-enter new password", null))) {
            fields.add(f);
            fields.add(Box.createVerticalStrut(6));
        }
        card.add(fields);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(Ui.button("Cancel", this::profile));
        actions.add(Ui.button("Update password", Ui.Style.PRIMARY, "key", () -> {
            banner.removeAll();
            try {
                server.changePassword(form.value("current"), form.value("password"), form.value("confirm"));
                form.clearErrors();
                banner.add(Ui.banner(Ui.Banner.SUCCESS, "Password updated. Use your new password next time you sign in."));
                banner.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
            } catch (ServiceException e) {
                if (!form.show(e)) banner.add(Ui.banner(Ui.Banner.ERROR, e.getMessage()));
            }
            form.clearPasswords();
            card.revalidate();
            card.repaint();
        }));
        card.add(actions, BorderLayout.SOUTH);
        slot.add(Ui.width(card, 440), BorderLayout.WEST);
        Ui.add(page, slot);
        display(page);
    }
}
