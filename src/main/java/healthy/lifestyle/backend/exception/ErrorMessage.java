package healthy.lifestyle.backend.exception;

public enum ErrorMessage {
    TITLE_DUPLICATE("Title Duplicate"),
    INVALID_SYMBOLS("Invalid symbols"),
    INVALID_NESTED_OBJECT("Invalid nested object");

    private final String name;

    ErrorMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
