package healthy.lifestyle.backend.validation;

public enum ValidationMessage {

    private final String name;

    ValidationMessage(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
