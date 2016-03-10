package net.suteren.worksaldo;


import org.joda.time.Days;
import org.joda.time.LocalDate;

/**
 * Created by hpa on 6.3.16.
 */
public enum Period {
    DAY, WEEK, MONTH, CUSTOM, period;

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
        Days daysInPeriod = Days.daysBetween(from(date), to(date)).plus(1);
        if (daysInPeriod.getDays() > 5) {
            daysInPeriod = Days.days(5);
        }
        return daysInPeriod;
    }

    public Days getDaysBefore(LocalDate date) {
        return Days.daysBetween(from(date), date);
    }

}
