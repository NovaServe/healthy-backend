package healthy.lifestyle.backend.user.validation;

public class EmailArgs {
    private static final String[] validEmails = new String[] {
        "a@cd.ef", " a@cd.ef ", "a.b@cd.ef", "a-b@c-d.ef", "a_b@c_d.ef", "ab1@cd.ef", "ab@cd1.ef", "ab1@cd1.ef"
    };

    private static final String[] invalidEmails = new String[] {
        "a@b.c",
        "ab@d.ef",
        "ab@cd.e",
        "a.b@c.d.ef",
        "ab@cd.e.f",
        "ab@cd.e_f",
        "ab@cd.ef1",
        "ab@@cd.ef1",
        "ab@cd.ef@",
        "ab@cd@.ef",
        "a b@cd.ef",
        "ab@c d.ef",
        "ab@cd.e f"
    };

    private static final Object[] nullOrBlankEmails = new Object[] {null, "", " ", "  "};

    public static String[] getValidEmails() {
        return validEmails;
    }

    public static String[] getInvalidEmails() {
        return invalidEmails;
    }

    public static Object[] getNullOrBlankEmails() {
        return nullOrBlankEmails;
    }
}
