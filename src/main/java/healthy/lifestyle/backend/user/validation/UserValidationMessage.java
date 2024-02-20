package healthy.lifestyle.backend.user.validation;

public enum UserValidationMessage {
    USERNAME_LENGTH_RANGE("Username length must be between 5 and 20"),
    EMAIL_LENGTH_RANGE("Email length must be between 7 and 60"),
    FULL_NAME_LENGTH_RANGE("Full name length must be between 2 and 50"),
    PASSWORD_LENGTH_RANGE("Password length must be between 10 and 20");

    private final String name;

    UserValidationMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
