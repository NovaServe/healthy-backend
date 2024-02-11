package healthy.lifestyle.backend.user.validation;

public class UsernameArgs {
    private static final String[] validUsernames = new String[] {
        "username", " username ", "Username", "username1", "username-1", "username_1", "username.1",
    };

    private static final String[] invalidUsernames = new String[] {"user name", "username!"};

    private static final Object[] nullOrBlankUsernames = new Object[] {null, "", " ", "  "};

    public static String[] getValidUsernames() {
        return validUsernames;
    }

    public static String[] getInvalidUsernames() {
        return invalidUsernames;
    }

    public static Object[] getNullOrBlankUsernames() {
        return nullOrBlankUsernames;
    }
}
