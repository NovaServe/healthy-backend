package healthy.lifestyle.backend.exception;

public enum ErrorMessage {
    AUTHENTICATION_ERROR("Authentication error"),
    INVALID_TOKEN("Invalid token"),
    ALREADY_EXISTS("Already exists"),
    TITLE_DUPLICATE("Title duplicate"),
    HTTP_REF_NOT_FOUND("Http ref with id %d not found"),
    BODY_PART_NOT_FOUND("Body part with id %d not found"),
    COUNTRY_NOT_FOUND("Country with id %d not found"),
    EXERCISE_NOT_FOUND("Exercise with id %d not found"),
    WORKOUT_NOT_FOUND("Workout with id %d not found"),
    USER_NOT_FOUND("User with id %d not found"),
    NUTRITION_NOT_FOUND("Nutrition with id %d not found"),
    ROLE_NOT_FOUND("Role not found"),
    NOT_FOUND("Not found"),
    CUSTOM_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_DEFAULT("Custom resource has been requested instead of default"),
    DEFAULT_RESOURCE_HAS_BEEN_REQUESTED_INSTEAD_OF_CUSTOM("Default resource has been requested instead of custom"),
    DEFAULT_RESOURCE_IS_NOT_ALLOWED_TO_MODIFY("Default resource is not allowed to modify"),
    USER_HTTP_REF_MISMATCH("Http ref with id %d doesn't belong to the user"),
    USER_EXERCISE_MISMATCH("Exercise with id %d doesn't belong to the user"),
    USER_WORKOUT_MISMATCH("Workout with id %d doesn't belong to the user"),
    USER_NUTRITION_MISMATCH("Nutrition with id %d doesn't belong to the user"),
    USER_REQUESTED_ANOTHER_USER_PROFILE("User has been requested another user profile"),
    EMPTY_REQUEST("Empty request"),
    NO_UPDATES_REQUEST("No updates request"),
    TITLE_IS_NOT_DIFFERENT("Title is not different"),
    DESCRIPTION_IS_NOT_DIFFERENT("Description is not different"),
    NEEDS_EQUIPMENT_IS_NOT_DIFFERENT("Needs equipment is not different"),
    USERNAME_IS_NOT_DIFFERENT("Username is not different"),
    EMAIL_IS_NOT_DIFFERENT("Email is not different"),
    FULL_NAME_IS_NOT_DIFFERENT("Full name is not different"),
    AGE_IS_NOT_DIFFERENT("Age is not different"),
    PASSWORD_IS_NOT_DIFFERENT("Password is not different"),
    USER_MENTAL_MISMATCH("Mental with id %d doesn't belong to the user"),
    MENTAL_NOT_FOUND("Mental with id %d not found");

    private final String name;

    ErrorMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
