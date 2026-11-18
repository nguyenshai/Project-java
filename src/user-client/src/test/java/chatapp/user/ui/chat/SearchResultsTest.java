package chatapp.user.ui.chat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchResultsTest {
    private static final String ON = "<b><font color='#1B54A8'>", OFF = "</font></b>";

    @Test
    void highlightsEveryMatchIgnoringCaseAndEscapesTheRest() {
        assertEquals("a " + ON + "Draft" + OFF + " &amp; " + ON + "draft" + OFF + " &lt;b&gt;",
            SearchResults.highlight("a Draft & draft <b>", " draft "));
    }

    @Test
    void charactersThatGrowWhenLowerCasedDoNotShiftTheHighlight() {
        // "İ".toLowerCase() is two chars long; positions found in a lower-cased copy would drift by one.
        assertEquals("İİ " + ON + "report" + OFF, SearchResults.highlight("İİ report", "report"));
        assertEquals("İ", SearchResults.highlight("İ", "xx"));
    }
}
