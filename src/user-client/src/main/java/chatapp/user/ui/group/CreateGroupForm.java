package chatapp.user.ui.group;

import chatapp.user.preview.ServiceException;
import chatapp.user.preview.Views.Group;
import chatapp.user.preview.Views.Person;
import chatapp.user.preview.Views.Relation;
import chatapp.user.ui.shared.Form;
import chatapp.user.ui.shared.Ui;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * F12a Create group (Figma G2–G3): a name and at least one other member, picked by username or
 * name from suggestions and shown as chips. The server enforces the "at least one other member" rule.
 */
final class CreateGroupForm {
    private final GroupPage host;
    /** Username → full name, in the order they were added. */
    private final Map<String, String> members = new LinkedHashMap<>();
    private final Form form = new Form();
    private final JPanel chips = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JPanel suggestions = Ui.column();
    private Form.Field picker;

    CreateGroupForm(GroupPage host, List<String> usernames) {
        this.host = host;
        for (String username : usernames) {
            List<Person> found = host.server.findPeople(username).stream().filter(p -> p.username().equals(username)).toList();
            members.put(username, found.isEmpty() ? username : found.getFirst().fullName());
        }
    }

    void show() {
        JPanel page = host.newPage("Create group");
        host.backTo(page);
        JPanel card = Ui.card(new BorderLayout(0, 12));
        card.add(Ui.label("New group", 17, true), BorderLayout.NORTH);
        JPanel fields = Ui.column();
        Form.Field name = form.text("name", "Group name", "e.g. Final presentation", null);
        fields.add(name);
        fields.add(Box.createVerticalStrut(4));
        picker = form.text("members", "Members", "Add by username or name", null);
        fields.add(picker);
        chips.setOpaque(false);
        chips.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.add(chips);
        fields.add(Box.createVerticalStrut(8));
        suggestions.setAlignmentX(Component.LEFT_ALIGNMENT);
        fields.add(suggestions);
        card.add(fields);
        JTextField search = (JTextField) picker.input;
        Ui.onText(search, this::refresh);
        search.addActionListener(e -> {
            List<Person> found = candidates(search.getText());
            if (found.size() == 1 && !found.getFirst().locked()) add(found.getFirst());
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(Ui.button("Cancel", host::showList));
        actions.add(Ui.button("Create group", Ui.Style.PRIMARY, null, () -> {
            try {
                Group group = host.server.createGroup(form.value("name"), List.copyOf(members.keySet()));
                host.banner(Ui.Banner.SUCCESS, "Group “" + group.name() + "” created with " + (group.members().size() - 1)
                    + (group.members().size() == 2 ? " other member." : " other members."));
                host.showDetail(group.id());
            } catch (ServiceException e) {
                if (!form.show(e)) picker.error(e.getMessage());
            }
        }));
        card.add(actions, BorderLayout.SOUTH);
        JPanel slot = new JPanel(new BorderLayout());
        slot.setOpaque(false);
        slot.add(Ui.width(card, 560), BorderLayout.WEST);
        Ui.add(page, slot);
        refresh();
        host.show(page);
    }

    /** People I may add: never someone who blocked me (the server hides them) or someone I blocked. */
    private List<Person> candidates(String query) {
        return host.server.findPeople(query).stream().filter(p -> p.relation() != Relation.BLOCKED).limit(6).toList();
    }

    private void add(Person p) {
        members.put(p.username(), p.fullName());
        picker.error(null);
        ((JTextField) picker.input).setText("");
        refresh();
    }

    private void refresh() {
        chips.removeAll();
        for (var entry : new ArrayList<>(members.entrySet())) {
            JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            chip.setBackground(Ui.SELECTED);
            chip.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));
            JLabel text = Ui.label(entry.getValue(), 12, true);
            text.setForeground(Ui.BLUE);
            chip.add(text);
            JButton remove = Ui.button("", Ui.Style.LINK, "close", () -> {
                members.remove(entry.getKey());
                refresh();
            });
            remove.setIcon(Ui.icon("close", 12, Ui.BLUE));
            remove.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 4));
            remove.setToolTipText("Remove @" + entry.getKey());
            remove.getAccessibleContext().setAccessibleName("Remove " + entry.getValue());
            chip.add(remove);
            chips.add(chip);
        }
        chips.setBorder(members.isEmpty() ? null : BorderFactory.createEmptyBorder(4, 0, 0, 0));

        suggestions.removeAll();
        String query = ((JTextField) picker.input).getText();
        if (!query.isBlank()) {
            JPanel box = new JPanel(new BorderLayout());
            box.setBackground(Color.WHITE);
            box.setBorder(BorderFactory.createLineBorder(Ui.LINE));
            JLabel title = Ui.label("Suggestions", 12, true);
            title.setForeground(Ui.MUTED);
            title.setOpaque(true);
            title.setBackground(Ui.SOFT);
            title.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
            box.add(title, BorderLayout.NORTH);
            JPanel rows = Ui.column();
            List<Person> found = candidates(query);
            if (found.isEmpty()) {
                JLabel none = Ui.muted("No people match “" + query.trim() + "”.");
                none.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
                rows.add(none);
            }
            for (Person p : found) {
                JPanel row = new JPanel(new BorderLayout(10, 0));
                row.setOpaque(false);
                row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Ui.LINE),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)));
                row.add(Ui.person(p.fullName(), p.username(), p.online(), p.locked()), BorderLayout.CENTER);
                row.add(members.containsKey(p.username()) ? Ui.muted("Added")
                    : p.locked() ? Ui.muted("Account locked")
                    : Ui.button("Add", Ui.Style.SECONDARY, "plus", () -> add(p)), BorderLayout.EAST);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                rows.add(row);
            }
            box.add(rows);
            suggestions.add(box);
        }
        chips.revalidate();
        chips.repaint();
        suggestions.revalidate();
        suggestions.repaint();
    }
}
