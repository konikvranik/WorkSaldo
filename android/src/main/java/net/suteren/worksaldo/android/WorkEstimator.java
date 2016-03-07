package net.suteren.worksaldo.android;

import android.database.Cursor;
import android.text.format.DateUtils;
import android.util.Log;
import net.suteren.worksaldo.android.ui.ISharedPreferencesProvider;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.DATE_FORMAT;
import static net.suteren.worksaldo.android.provider.TogglCachedProvider.TIME_FORMAT;

/**
 * Created by hpa on 6.3.16.
 */
public class WorkEstimator {
    private final boolean closedDay;
    private Calendar from;
    private Calendar to;
    private float total;
    private Calendar date;
    private Period period;
    private int pause;

    public WorkEstimator(Period period, Calendar date, float total, boolean closedDay) {
        this(period, period.from(date), period.to(date), date, total, closedDay);
    }

    public WorkEstimator(Calendar from, Calendar to, Calendar date, float total, boolean closedDay) {
        this(Period.CUSTOM, from, to, date, total, closedDay);
    }

    private WorkEstimator(Period period, Calendar from, Calendar to, Calendar date, float total, boolean closedDay) {
        this.period = period;
        this.date = date;
        this.from = from;
        this.to = to;
        this.total = total;
        this.closedDay = closedDay;

    }

    public float getSaldo(float... cnt) {
        return cnt[0] + (closedDay && cnt.length > 1 ? cnt[1] : 0) - (getPastDayCount() * total / period.getDayCount(date));
    }


    public float getHoursPerDay() {
        return total / period.getDayCount(date);
    }

    public long getPastDayCount() {
        long diff = date.getTimeInMillis() - from.getTimeInMillis();
        return Math.min(TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS) + (closedDay ? 1 : 0), period.getDayCount(date));
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
            if (today) {
                try {
                    long stopDate = TIME_FORMAT.parse(stop).getTime();
                    Date now = new Date();
                    if (now.getTime() > stopDate && !closedDay) {
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

    public float getCount(Float total, String start, String stop, boolean realHours) {

        float count = 0;
        try {
            count = (realHours ? total :
                    (int) (TIME_FORMAT.parse(stop).getTime() - TIME_FORMAT.parse(start).getTime()) / 1000 - getPause()) / 3600;
        } catch (ParseException e) {
            Log.e("DashboardFragment", "Unable to parse date", e);
        }
        return count;
    }

    public int getPause() {
        return pause;
    }

    public void setPause(int pause) {
        this.pause = pause;
    }

    public float getSaldo(Cursor data, boolean rwt) {
        final float[] floats = countTotal(data, rwt);
        return getSaldo(floats[0] + (closedDay ? floats[1] : 0));
    }


    public float getTodayToAvg(float workedToday) {
        return workedToday - getHoursPerDay();
    }

    public float getTodayToWhole(float[] workedPast) {
        return getTodayToAvg(workedPast[1]) + getSaldo(workedPast[0], workedPast[1]);
    }
}
