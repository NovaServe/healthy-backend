package healthy.lifestyle.backend.shared.validation;

public class TitleArgs {
    private static final String[] validTitles = new String[] {"Title", " Title ", "Title1", "Title 1"};

    private static final String[] invalidTitles = new String[] {"Title!"};

    private static final Object[] nullOrBlankTitles = new Object[] {null, "", " ", "  "};

    public static String[] getValidTitles() {
        return validTitles;
    }

    public static String[] getInvalidTitles() {
        return invalidTitles;
    }

    public static Object[] getNullOrBlankTitles() {
        return nullOrBlankTitles;
    }
}
