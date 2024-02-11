package healthy.lifestyle.backend.user.validation;

public class AgeArgs {
    private static final int[] validAge = new int[] {
        16, 17, 119, 120,
    };

    private static final int[] invalidAge = new int[] {15, 121};

    public static int[] getValidAge() {
        return validAge;
    }

    public static int[] getInvalidAge() {
        return invalidAge;
    }
}
