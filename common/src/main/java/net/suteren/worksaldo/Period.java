package net.suteren.worksaldo;


import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Enum representing period for counting required work.
 */
public enum Period {
    DAY, WEEK, MONTH, CUSTOM, period;

    private static final int MAX_DAYS_IN_WEEK = 5; // TODO: provide correct dynamic method to recognize working days.
    private double dayCount;

    /**
     * Determines first day of period based on date inside a period.
     *
     * @param date date inside a period.
     * @return first day of the period.
     */
    public LocalDate from(LocalDate date) {

        switch (this) {
            case MONTH:
                return date.dayOfMonth().withMinimumValue();
            case WEEK:
                return date.dayOfWeek().withMinimumValue();
        }
        return date;
    }

    /**
     * Determines last day of period based on date inside a period.
     *
     * @param date date inside a period.
     * @return last day of the period.
     */
    public LocalDate to(LocalDate date) {
        switch (this) {
            case MONTH:
                return date.dayOfMonth().withMaximumValue();
            case WEEK:
                return date.dayOfWeek().withMaximumValue();
        }
        return date;
    }

    /**
     * Determines count of all days of a period based on date inside a period.
     *
     * @param date date inside a period.
     * @return count of days in the period.
     */
    public Days getDayCount(LocalDate date) {
        return cutOff(Days.daysBetween(from(date), to(date)).plus(1));
    }

    private Days cutOff(Days daysInPeriod) {
        if (this == WEEK && daysInPeriod.getDays() > MAX_DAYS_IN_WEEK) {
            daysInPeriod = Days.days(MAX_DAYS_IN_WEEK);
        }
        return daysInPeriod;
    }

    /**
     * Determines count of days in a period before a particular date.
     *
     * @param date date inside a period to find days before.
     * @return number of days before date.
     */
    public Days getDaysBefore(LocalDate date) {
        return cutOff(Days.daysBetween(from(date), date));
    }

}
