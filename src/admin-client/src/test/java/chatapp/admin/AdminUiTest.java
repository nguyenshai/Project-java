package chatapp.admin;

import chatapp.admin.ui.AdminFrame;
import chatapp.admin.ui.preview.PreviewData;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AdminUiTest {
    @Test
    void mainNavigationAndAnalyticsExposeTheRequiredViews() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = AdminFrame.createContent();
            assertColumns(shell, "Full name", "Status", "Created");
            button(shell, "Account History").doClick();
            assertColumns(shell, "Timestamp", "Username", "Full name");
            button(shell, "New Accounts").doClick();
            assertColumns(shell, "Created", "Username", "Full name");
            button(shell, "Chat Groups").doClick();
            assertColumns(shell, "Group name", "Members", "Group admins");
            button(shell, "Spam Reports").doClick();
            assertColumns(shell, "Reported username", "Account status");
            button(shell, "Analytics").doClick();
            assertNotNull(button(shell, "Friend Counts"));
            button(shell, "Friend Counts").doClick();
            assertColumns(shell, "Direct friends", "Total network");
            button(shell, "Activity").doClick();
            assertColumns(shell, "Sign-ins", "App opens", "People chatted with", "Groups chatted in");
        });
    }

    @Test
    void friendCountDetailShowsAllRelationshipColumns() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JPanel shell = AdminFrame.createContent();
            button(shell, "Analytics").doClick(0);
            button(shell, "Friend Counts").doClick(0);
            JTable table = components(shell).stream().filter(JTable.class::isInstance).map(JTable.class::cast)
                .findFirst().orElseThrow();
            table.setRowSelectionInterval(0, 0);
            table.getActionMap().get("openRow").actionPerformed(null);
            assertColumns(shell, "Full name", "Username", "Friends since");
            assertColumns(shell, "Group name", "Group role", "Joined");
            assertColumns(shell, "Full name", "Username", "Mutual friends");
        });
    }

    @Test
    void lockAndUnlockShareOneAccountStateAndHistory() {
        PreviewData data = new PreviewData();
        String userId = data.users.stream().filter(user -> !user.locked()).findFirst().orElseThrow().id();
        int historySize = data.locks.size();
        data.lock(userId, true);
        data.lock(userId, true);
        assertEquals("Locked", data.status(userId));
        assertEquals(historySize + 1, data.locks.size());
        data.lock(userId, false);
        assertEquals("Active", data.status(userId));
        assertEquals(historySize + 2, data.locks.size());
    }

    private static JButton button(Container root, String label) {
        return components(root).stream().filter(JButton.class::isInstance).map(JButton.class::cast)
            .filter(b -> label.equals(b.getText())).findFirst()
            .orElseThrow(() -> new AssertionError("Missing button: " + label));
    }

    private static void assertColumns(Container root, String... columns) {
        assertTrue(components(root).stream().filter(JTable.class::isInstance)
            .map(JTable.class::cast).anyMatch(table -> {
                List<String> headers = new ArrayList<>();
                for (int i = 0; i < table.getColumnCount(); i++) headers.add(table.getColumnName(i));
                return headers.containsAll(List.of(columns));
            }), "Missing columns: " + List.of(columns));
    }

    private static List<Component> components(Container root) {
        List<Component> result = new ArrayList<>();
        for (Component child : root.getComponents()) {
            if (!child.isVisible()) continue;
            result.add(child);
            if (child instanceof Container container) result.addAll(components(container));
        }
        return result;
    }
}
