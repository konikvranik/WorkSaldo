package net.suteren.worksaldo.android;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by hpa on 6.3.16.
 */
public enum Period {
    DAY, WEEK, MONTH, CUSTOM;

    private double dayCount;

    public Calendar from(Calendar date) {
        date = (Calendar) date.clone();
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        switch (this) {
            case MONTH:
                date.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEK:
                date.add(Calendar.DAY_OF_MONTH, -((date.get(Calendar.DAY_OF_WEEK) + 5) % 7));
                break;
        }
        return date;
    }

    public Calendar to(Calendar date) {
        date = (Calendar) date.clone();
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        switch (this) {
            case MONTH:
                date.set(Calendar.DAY_OF_MONTH, 1);
                date.add(Calendar.MONTH, 1);
                break;
            case WEEK:
                date.add(Calendar.DAY_OF_MONTH, 7 - ((date.get(Calendar.DAY_OF_WEEK) + 5) % 7) + 1);
                break;
            case DAY:
                date.add(Calendar.DAY_OF_MONTH, 1);
        }
        date.add(Calendar.MILLISECOND, -1);
        return date;
    }


    public long getDayCount(Calendar date) {
        switch (this) {
            case DAY:
                return 1;
            case WEEK:
                return 5;
            case MONTH:
                return date.getActualMaximum(Calendar.DAY_OF_MONTH);
            case CUSTOM:
                return TimeUnit.DAYS.convert(to(date).getTimeInMillis() - from(date).getTimeInMillis(), TimeUnit.MILLISECONDS);
            default:
                return 0;
        }
    }
}
