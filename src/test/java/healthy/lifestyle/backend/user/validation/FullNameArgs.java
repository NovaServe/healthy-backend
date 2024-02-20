package healthy.lifestyle.backend.user.validation;

public class FullNameArgs {
    private static final String[] validFullNames = new String[] {
        "FullName", "Full Name", " Full Name ",
    };

    private static final String[] invalidFullNames =
            new String[] {"FullName1", "FullName 1", "FullName!", "FullName !"};

    private static final Object[] nullOrBlankFullNames = new String[] {null, "", " ", "  "};

    public static String[] getValidFullNames() {
        return validFullNames;
    }

    public static String[] getInvalidFullNames() {
        return invalidFullNames;
    }

    public static Object[] getNullOrBlankFullNames() {
        return nullOrBlankFullNames;
    }
}
