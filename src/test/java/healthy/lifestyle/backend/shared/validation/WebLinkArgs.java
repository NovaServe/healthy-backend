package healthy.lifestyle.backend.shared.validation;

public class WebLinkArgs {
    private static final String[] validWebLinks = new String[] {"https://web-link.com", " https://web-link.com "};

    private static final String[] invalidWebLinks = new String[] {"web-link.com", "web.link.com"};

    private static final Object[] nullOrBlankWebLinks = new Object[] {null, "", " ", "  "};

    public static String[] getValidWebLinks() {
        return validWebLinks;
    }

    public static String[] getInvalidWebLinks() {
        return invalidWebLinks;
    }

    public static Object[] getNullOrBlankWebLinks() {
        return nullOrBlankWebLinks;
    }
}
