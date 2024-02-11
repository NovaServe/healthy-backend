package healthy.lifestyle.backend.shared.validation;

public enum ValidationMessage {
    LENGTH_RANGE("Length must be between %d and %d"),
    NOT_NULL_OR_BLANK("Must be not null or blank"),
    NOT_BLANK("Must be not blank");

    private final String name;

    ValidationMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
