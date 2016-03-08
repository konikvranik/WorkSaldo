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
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis(0);
        d.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        d.set(Calendar.MONTH, date.get(Calendar.MONTH));
        d.set(Calendar.YEAR, date.get(Calendar.YEAR));

        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        d.set(Calendar.MILLISECOND, 0);

        switch (this) {
            case MONTH:
                d.set(Calendar.DAY_OF_MONTH, 1);
                break;
            case WEEK:
                d.add(Calendar.DAY_OF_MONTH, -((d.get(Calendar.DAY_OF_WEEK) + 5) % 7));
                break;
        }

        return d;
    }

    public Calendar to(Calendar date) {
       Calendar d = Calendar.getInstance();

        d.setTimeInMillis(0);
        d.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        d.set(Calendar.MONTH, date.get(Calendar.MONTH));
        d.set(Calendar.YEAR, date.get(Calendar.YEAR));

        //d.set(Calendar.HOUR_OF_DAY, 0);
        d.set(Calendar.MINUTE, 0);
        d.set(Calendar.SECOND, 0);
        d.set(Calendar.MILLISECOND, 0);

        switch (this) {
            case MONTH:
                d.set(Calendar.DAY_OF_MONTH, 1);
                d.add(Calendar.MONTH, 1);
                break;
            case WEEK:
                d.add(Calendar.DAY_OF_MONTH, 7 - ((d.get(Calendar.DAY_OF_WEEK) + 5) % 7) - 1);
                break;
            case DAY:
                d.add(Calendar.DAY_OF_MONTH, 1);
        }

        return d;
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
                return TimeUnit.DAYS.convert(to(date).getTimeInMillis() - from(date).getTimeInMillis(), TimeUnit
                        .MILLISECONDS);
            default:
                return 0;
        }
    }
}
