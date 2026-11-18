package chatapp.user.preview;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A rule violation the user can fix; the message is shown to the user as-is.
 * Form checks may also name the fields that failed, so a page can mark each one. Pages that react to a
 * particular failure check its {@link Code}, never the wording of the message.
 */
public final class ServiceException extends RuntimeException {
    /** What kind of failure this is, for pages that show some failures differently. */
    public enum Code { RULE, ACCOUNT_LOCKED }

    private final Map<String, String> fieldErrors;
    private final Code code;

    public ServiceException(String message) {
        this(Code.RULE, message);
    }

    public ServiceException(Code code, String message) {
        super(message);
        this.code = code;
        this.fieldErrors = Map.of();
    }

    /** One failed form field, e.g. {@code ("email", "This email is already in use.")}. */
    public ServiceException(String field, String message) {
        super(message);
        this.code = Code.RULE;
        this.fieldErrors = Map.of(field, message);
    }

    /** Several failed form fields, in form order. The first message is the exception message. */
    public ServiceException(Map<String, String> fieldErrors) {
        super(fieldErrors.values().stream().findFirst().orElse("Check the form and try again."));
        this.code = Code.RULE;
        this.fieldErrors = Collections.unmodifiableMap(new LinkedHashMap<>(fieldErrors));
    }

    public Code code() {
        return code;
    }

    /** Field name → message; empty when the error is not tied to a form field. */
    public Map<String, String> fieldErrors() {
        return fieldErrors;
    }
}
