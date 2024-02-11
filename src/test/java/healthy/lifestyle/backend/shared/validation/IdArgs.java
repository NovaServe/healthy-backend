package healthy.lifestyle.backend.shared.validation;

public class IdArgs {
    private static final long[] validIds = new long[] {0, 1};

    private static final long[] invalidIds = new long[] {-1};

    public static long[] getValidIds() {
        return validIds;
    }

    public static long[] getInvalidIds() {
        return invalidIds;
    }
}
