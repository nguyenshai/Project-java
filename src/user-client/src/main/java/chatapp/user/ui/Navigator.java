package chatapp.user.ui;

import java.util.List;

/**
 * Links between the sidebar pages. Each feature lives on one page; other pages only link to it
 * through here (e.g. Friend → "Chat" opens the Chat page on that conversation).
 */
public interface Navigator {
    /** Opens the Chat page with this conversation selected. */
    void openChat(long chatId);

    /** Opens the Group page on this group's detail. */
    void openGroup(long groupId);

    /** Opens the Group page's Create group form with these usernames already added. */
    void openCreateGroup(List<String> usernames);

    /** Opens the Friend page on a tab ("Friends", "Online", "Requests" or "Find people"). */
    void openFriends(String tab);

    /** Re-reads the signed-in user's name for the sidebar after a profile change. */
    void profileChanged();

    /** Asks to confirm, then signs out and returns to the Login page. */
    void signOut();
}
