package net.suteren.worksaldo;

import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;

/**
 * Created by hpa on 6.3.16.
 */
public class WorkEstimator implements IWorkEstimator {
    private final boolean closedDay;
    private Calendar from;
    private Calendar to;
    private float total;
    private Calendar date;
    private Period period;
    private int pause;

    public WorkEstimator(Period period, Calendar date, float total, int pause, boolean closedDay) {
        this(period, period.from(date), period.to(date), date, total, pause, closedDay);
    }

    public WorkEstimator(Calendar from, Calendar to, Calendar date, float total, int pause, boolean closedDay) {
        this(Period.CUSTOM, from, to, date, total, pause, closedDay);
    }

    private WorkEstimator(Period period, Calendar from, Calendar to, Calendar date, float total, int pause, boolean
            closedDay) {
        this.period = period;
        this.date = date;
        this.from = from;
        this.to = to;
        this.total = total;
        this.closedDay = closedDay;
        setPause(pause);

    }

    @Override
    public float getSaldo(float... cnt) {
        return cnt[0] + (closedDay && cnt.length > 1 ? cnt[1] : 0) - (getPastDayCount() * total / period.getDayCount
                (date));
    }


    @Override
    public float getHoursPerDay() {
        return total / period.getDayCount(date);
    }

    public long getPastDayCount() {
        long diff = date.getTimeInMillis() - from.getTimeInMillis();
        return Math.min(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + (closedDay ? 1 : 0), period.getDayCount
                (date));
    }


    public float[] countTotal(Cursor data, boolean rwt) {
        data.moveToFirst();
        float cnt = 0;
        float todayCount = 0;
        while (!data.isAfterLast()) {

            boolean today = false;
            try {
                today = DateUtils.isToday(DATE_FORMAT.parse(data.getString(1)).getTime());
            } catch (ParseException e) {
                Log.e("DashboardFragment", "Unable parse date", e);
            }


            String stop = data.getString(3);
            assert stop != null;

            if (today) {
                try {
                    Date now = new Date();
                    if (stop != null && now.getTime() > TIME_FORMAT.parse(stop).getTime() && !closedDay) {
                        stop = TIME_FORMAT.format(now);
                    }
                } catch (ParseException e) {
                    Log.e("WorkEstimator", String.format("Unparseable time %s", stop), e);
                }
            }
            float tdc = getCount(data.getFloat(4), data.getString(2), stop, rwt);
            if (today) {
                todayCount = tdc;
            } else {
                cnt += tdc;
            }
            data.moveToNext();
        }

        return new float[]{cnt, todayCount};
    }

    private static String getStopNow() {
        return TIME_FORMAT.format(Calendar.getInstance().getTime());
    }

    public float getCount(Float worked, String start, String stop, boolean realHours) {

        assert start != null;
        assert stop != null;

        float count = 0;
        try {
            if (stop == null) {
                stop = getStopNow();
            }
            long stopTime = TIME_FORMAT.parse(stop).getTime();
            count = (realHours ? worked :
                    (int) (stopTime - TIME_FORMAT.parse(start).getTime()) / 1000 - (getPause() * 60)) / 3600;
        } catch (ParseException e) {
            Log.e("DashboardFragment", "Unable to parse date", e);
        }
        return count;
    }

    @Override
    public int getPause() {
        return pause;
    }

    @Override
    public void setPause(int pause) {
        this.pause = pause;
    }

    public float getSaldo(Cursor data, boolean rwt) {
        final float[] floats = countTotal(data, rwt);
        return getSaldo(floats[0] + (closedDay ? floats[1] : 0));
    }


    @Override
    public float getTodayToAvg(float workedToday) {
        return workedToday - getHoursPerDay();
    }

    @Override
    public float getTodayToWhole(float[] workedPast) {
        return getTodayToAvg(workedPast[1]) + getSaldo(workedPast[0], workedPast[1]);
    }
}
