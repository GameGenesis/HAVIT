package com.havit.app.ui.habit;

public enum Days {
    SUNDAY("Sunday", 0), MONDAY("Monday", 1), TUESDAY("Tuesday", 2),
    WEDNESDAY("Wednesday", 3), THURSDAY("Thursday", 4), FRIDAY("Friday", 5), SATURDAY("Saturday", 6);

    private final String name;
    private final int value;

    Days(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static Days fromValue(int value) {
        for (Days day : Days.values()) {
            if (day.getValue() == value) {
                return day;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
