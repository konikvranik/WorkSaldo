package net.suteren.worksaldo.android;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Created by hpa on 6.3.16.
 */
public class WorkEstimator {
    private Calendar from;
    private Calendar to;
    private float total;
    private Calendar date;
    private Period period;

    public WorkEstimator(Period period, Calendar date, float total) {
        this(period, period.from(date), period.to(date), date, total);
    }

    public WorkEstimator(Calendar from, Calendar to, Calendar date, float total) {
        this(Period.CUSTOM, from, to, date, total);
    }

    private WorkEstimator(Period period, Calendar from, Calendar to, Calendar date, float total) {
        this.period = period;
        this.date = date;
        this.from = from;
        this.to = to;
        this.total = total;

    }

    public float getSaldo(float cnt) {
        return cnt - getPastDayCount() * total / period.getDayCount(date);
    }


    public float getHoursPerDay() {
        return total / period.getDayCount(date);
    }

    public long getPastDayCount() {
        long diff = date.getTimeInMillis() - from.getTimeInMillis();
        return Math.min(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + 1, period.getDayCount(date));
    }
}
