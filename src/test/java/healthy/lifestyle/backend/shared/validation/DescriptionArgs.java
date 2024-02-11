package healthy.lifestyle.backend.shared.validation;

public class DescriptionArgs {
    private static final String[] validDescriptions =
            new String[] {"Description", "Description 1", " Description ", "Description . , - : ; ! ? ' \" # % ( ) + ="
            };

    private static final String[] invalidDescriptions = new String[] {"Description @"};

    private static final Object[] nullOrBlankDescriptions = new Object[] {null, "", " ", "  "};

    public static String[] getValidDescriptions() {
        return validDescriptions;
    }

    public static String[] getInvalidDescriptions() {
        return invalidDescriptions;
    }

    public static Object[] getNullOrBlankDescriptions() {
        return nullOrBlankDescriptions;
    }
}
