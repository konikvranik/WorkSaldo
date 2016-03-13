package net.suteren.worksaldo;


import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by hpa on 6.3.16.
 */
public enum Period {
    DAY, WEEK, MONTH, CUSTOM, period;

    public static final int MAX_DAYS = 5;
    private double dayCount;

    public LocalDate from(LocalDate date) {

        switch (this) {
            case MONTH:
                return date.dayOfMonth().withMinimumValue();
            case WEEK:
                return date.dayOfWeek().withMinimumValue();
        }
        return date;
    }

    public LocalDate to(LocalDate date) {
        switch (this) {
            case MONTH:
                return date.dayOfMonth().withMaximumValue();
            case WEEK:
                return date.dayOfWeek().withMaximumValue();
        }
        return date;
    }


    public Days getDayCount(LocalDate date) {
        return cutOff(Days.daysBetween(from(date), to(date)).plus(1));
    }

    Days cutOff(Days daysInPeriod) {
        if (daysInPeriod.getDays() > MAX_DAYS) {
            daysInPeriod = Days.days(MAX_DAYS);
        }
        return daysInPeriod;
    }

    public Days getDaysBefore(LocalDate date) {
        return cutOff(Days.daysBetween(from(date), date));
    }

}
