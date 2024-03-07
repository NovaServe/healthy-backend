package healthy.lifestyle.backend.notification.shared;

public enum ActivityType {
    WORKOUT("Workout"),
    NUTRITION("Nutrition activity"),
    MENTAL_ACTIVITY("Mental activity");

    private final String value;

    ActivityType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
