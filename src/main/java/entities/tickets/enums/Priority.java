package entities.tickets.enums;

public enum Priority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4);

    private final int value;

    Priority(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
