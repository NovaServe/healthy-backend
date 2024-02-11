package healthy.lifestyle.backend.user.validation;

public class PasswordArgs {
    private static final String[] validPasswords =
            new String[] {"password", " password ", "Password", "aA0123456789.,-_<>:;!?#$%^&*()+="};

    private static final String[] invalidPasswords = new String[] {"pass word", "password`", "password~"};

    private static final Object[] nullOrBlankPasswords = new Object[] {null, "", " ", "  "};

    public static String[] getValidPasswords() {
        return validPasswords;
    }

    public static String[] getInvalidPasswords() {
        return invalidPasswords;
    }

    public static Object[] getNullOrBlankPasswords() {
        return nullOrBlankPasswords;
    }
}
