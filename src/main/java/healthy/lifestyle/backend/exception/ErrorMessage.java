package healthy.lifestyle.backend.exception;

public enum ErrorMessage {
    TITLE_DUPLICATE("Title Duplicate"),
    INVALID_SYMBOLS("Invalid symbols"),
    INVALID_NESTED_OBJECT("Invalid nested object"),
    ALREADY_EXISTS("Already exists"),
    SERVER_ERROR("Server error"),
    INVALID_TOKEN("Invalid token"),
    AUTHENTICATION_ERROR("Authentication error"),
    USER_NOT_FOUND("User not found"),
    COUNTRY_NOT_FOUND("Country not found"),
    NOT_FOUND("Not found"),
    UNAUTHORIZED_FOR_THIS_RESOURCE("Unauthorized for this resource"),
    DEFAULT_MEDIA_IS_NOT_ALLOWED_TO_MODIFY("Default media is not allowed to modify"),
    CUSTOM_WORKOUT_REQUIRED("Custom workout required"),
    DEFAULT_MEDIA_REQUESTED("Default media requested"),
    DEFAULT_RESOURCE_IF_NOT_ALLOWED_TO_MODIFY("Default is not allowed to modify"),
    USER_RESOURCE_MISMATCH("User-resource mismatch"),
    EMPTY_REQUEST("Empty request"),
    NO_UPDATES_REQUEST("No updates request"),
    DEFAULT_CUSTOM_MISMATCH("Default-custom mismatch"),
    TITLES_ARE_NOT_DIFFERENT("Title are not different"),
    DESCRIPTIONS_ARE_NOT_DIFFERENT("Descriptions are not different"),
    NEEDS_EQUIPMENT_ARE_NOT_DIFFERENT("Needs equipment are not different"),
    USERNAME_IS_NOT_DIFFERENT("Username is not different"),
    EMAIL_IS_NOT_DIFFERENT("Email is not different"),
    FULLNAME_IS_NOT_DIFFERENT("Full name is not different"),
    AGE_IS_NOT_DIFFERENT("Age is not different"),
    PASSWORD_IS_NOT_DIFFERENT("Password is not different");

    private final String name;

    ErrorMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
