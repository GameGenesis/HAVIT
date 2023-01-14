package com.havit.app.ui.habit;

/**
 * Enumeration of the days of the week
 * Each enum constant has a name and an index value
 */
public enum Days {
    SUNDAY("Sunday", 0), MONDAY("Monday", 1), TUESDAY("Tuesday", 2),
    WEDNESDAY("Wednesday", 3), THURSDAY("Thursday", 4), FRIDAY("Friday", 5), SATURDAY("Saturday", 6);

    private final String name;
    private final int value;

    /**
     * Initializes the name and value fields for each enum constant
     *
     * @param name the name of the day
     * @param value the index value corresponding to the day
     */
    Days(String name, int value) {
        this.name = name;
        this.value = value;
    }

    /**
     * returns the name of the current day (getter)
     *
     * @return the name of the current day
     */
    public String getName() {
        return name;
    }

    /**
     * returns the index value of the current day (getter)
     *
     * @return the index value of the current day
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the corresponding day for the specified index value
     *
     * @param value the index value
     * @return the corresponding day for the specified index value
     * @throws IllegalArgumentException if the value is invalid
     */
    public static Days fromValue(int value) {
        for (Days day : Days.values()) {
            if (day.getValue() == value) {
                return day;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
