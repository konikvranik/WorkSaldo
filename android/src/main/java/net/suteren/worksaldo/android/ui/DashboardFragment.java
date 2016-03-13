package net.suteren.worksaldo.android.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.caverock.androidsvg.SVGImageView;
import net.suteren.worksaldo.IWorkEstimator;
import net.suteren.worksaldo.Period;
import net.suteren.worksaldo.StandardWorkEstimator;
import net.suteren.worksaldo.android.IRefreshable;
import net.suteren.worksaldo.android.R;
import org.joda.time.*;
import org.joda.time.format.*;

import java.io.IOException;
import java.io.Writer;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static net.suteren.worksaldo.StandardWorkEstimator.chunkOfWork;
import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;
import static net.suteren.worksaldo.android.ui.MainActivity.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardFragment extends Fragment implements ISharedPreferencesProviderWithContext, IRefreshable {

    public static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .printZeroIfSupported()
            .appendHours()
            .appendSeparator(":")
            .minimumPrintedDigits(2)
            .append(new PeriodPrinter() {
                @Override
                public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
                    return 2;
                }

                @Override
                public int countFieldsToPrint(ReadablePeriod period, int stopAt, Locale locale) {
                    return 1;
                }

                @Override
                public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
                    buf.append(String.format("%02d", Math.abs(period.get(DurationFieldType.minutes()))));
                }

                @Override
                public void printTo(Writer out, ReadablePeriod period, Locale locale) throws IOException {
                    StringBuffer sb = new StringBuffer();
                    printTo(sb, period, locale);
                    out.write(sb.toString());
                }
            }, new PeriodParser() {
                @Override
                public int parseInto(ReadWritablePeriod period, String periodStr, int position, Locale locale) {
                    throw new UnsupportedOperationException();
                }
            })
            .toFormatter();
    private static final DateTimeFormatter WEEKDAY_FORMAT = DateTimeFormat.forPattern("E");
    public static final String DAY_CLOSED_TIMESTAMP = "day_closed_timestamp";
    private SimpleCursorAdapter mAdapter;
    private DayBinder dayBinder;
    private Runnable onReload;
    private ListView lv;
    private SwipeRefreshLayout refresh;

    private Context getCtx() {
        return getContext();
    }

    @Override
    public Context getContext() {
        if (Build.VERSION.SDK_INT < 23) {
            return getActivity();
        } else {
            return super.getContext();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        lv = (ListView) rootView.findViewById(R.id.listing);
        refresh = (SwipeRefreshLayout) rootView.findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reload();
                onReload(new Runnable() {
                    @Override
                    public void run() {
                        refresh.setRefreshing(false);
                    }
                });
            }
        });
        final LoaderManager lm = getLoaderManager();
        mAdapter = new SimpleCursorAdapter(getCtx(), R.layout.row, null,
                new String[]{DATE_NAME, DAY_START_NAME, DAY_END_NAME, DAY_TOTAL_NAME, DAY_SALDO_NAME},
                new int[]{R.id.date, R.id.from, R.id.to, R.id.total, R.id.saldo}, 0);
        dayBinder = new DayBinder();
        dayBinder.setMode(getRealWorkedTime());
        mAdapter.setViewBinder(dayBinder);
        lv.setAdapter(mAdapter);

        lm.initLoader(DAYS_LOADER, null, getDaysLoaderCallback());

        rootView.findViewById(R.id.counters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchClosedDay();
                Log.d("DashboardFragment", "Day is now " + (isDayClosed() ? "closed" : "open"));
            }
        });

        updateCountersColor(rootView);

        return rootView;
    }

    LoaderManager.LoaderCallbacks<Cursor> getReloadCallback() {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                LocalDate d = LocalDate.now();
                String start = DATE_FORMAT.print(getPeriod().from(d));
                String stop = DATE_FORMAT.print(getPeriod().to(d));
                return new CursorLoader(getActivity(), RELOAD_URI, null, null, new String[]{start, stop}, null);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (onReload != null) {
                    onReload.run();
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                if (onReload != null) {
                    onReload.run();
                }
            }
        };
    }

    private Period getPeriod() {
        return Period.valueOf(getSharedPreferences().getString("period", "week").toUpperCase());
    }

    private void switchClosedDay() {
        if (isDayClosed()) {
            openDay();
        } else {
            closeDay();
        }
        refresh();
    }

    private void closeDay() {
        getSharedPreferences().edit().putLong(DAY_CLOSED_TIMESTAMP, DateTime.now().withTimeAtStartOfDay().plus(Days.days(1)).getMillis()).apply();
        updateCountersColor(getView());
    }

    private void updateCountersColor(View view) {
        //view.findViewById(R.id.counters).setBackgroundColor(getColor(isDayClosed() ? R.color.closed : R.color.open));
    }

    public int getColor(int colorId) {
        if (Build.VERSION.SDK_INT >= 23) {
            return getResources().getColor(colorId, getActivity().getTheme());
        } else {
            return getResources().getColor(colorId);
        }
    }


    private void openDay() {
        getSharedPreferences().edit().putLong(DAY_CLOSED_TIMESTAMP, 0).apply();
        updateCountersColor(getView());
    }

    private AbstractDaysLoader getDaysLoaderCallback() {
        return new AbstractDaysLoader(this) {
            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                Cursor c = mAdapter.swapCursor(data);
                if (c != null) {
                    c.close();
                }

                refreshSaldo(data);
                Log.d("LoaderCallbacks", "loader finished");
                Log.d("LoaderCallbacks", "loaded " + data.getCount() + " items.");
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                Cursor c = mAdapter.swapCursor(null);
                if (c != null) {
                    //c.close();
                }
                Log.d("LoaderCallbacks", "loader reset");
            }
        };
    }

    private int getNumberColor(float currentSaldo) {
        final int[] intArray = getResources().getIntArray(R.array.numbercolors);
        return intArray[(int) Math.signum(currentSaldo) + 1];
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        if (getActivity() instanceof ISharedPreferencesProvider) {
            return ((ISharedPreferencesProvider) getActivity()).getSharedPreferences();
        } else {
            return getActivity().getSharedPreferences(MAIN, MODE_PRIVATE);
        }
    }

    public void reload() {
        getLoaderManager().restartLoader(DAYS_UPDATER, null, getReloadCallback());
    }

    public void onReload(Runnable action) {
        this.onReload = action;
    }

    @Override
    public void refresh() {
        Log.d("DashboardFragment", "Refreshing");
        getActivity().getContentResolver().notifyChange(TIMEENTRIES_URI, null);
        getLoaderManager().restartLoader(DAYS_LOADER, null, getDaysLoaderCallback());
    }

    @Override
    public void onRefresh(Runnable action) {

    }

    private class DayBinder implements SimpleCursorAdapter.ViewBinder {

        private boolean realHours;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String value = null;
            LocalTime stop = TIME_FORMAT.parseLocalTime(cursor.getString(3));
            LocalDateTime day = DATE_FORMAT.parseLocalDate(cursor.getString(1)).toLocalDateTime(stop);
            IWorkEstimator we = new StandardWorkEstimator(getPeriod(), day, Duration.standardHours(getTotalHours()));
            boolean isToday = day.toLocalDate().isEqual(LocalDate.now());

            final LocalTime to = getEndTime(stop, isToday);
            LocalTime time = TIME_FORMAT.parseLocalTime(cursor.getString(2));
            if (getRealWorkedTime()) {
                long duration = cursor.getLong(4);
                if (duration > 0) {
                    we.addHours(chunkOfWork(Duration.standardSeconds(duration), true));
                }
            } else {
                we.addHours(chunkOfWork(time, to, Duration.standardMinutes(getPause()), true));
            }
            Integer color = null;

            String string = cursor.getString(columnIndex);


            switch (columnIndex) {
                case 1:
                    final LocalDate date = DATE_FORMAT.parseLocalDate(string);
                    value = String.format("%s %s", WEEKDAY_FORMAT.print(date), DateTimeFormat.mediumDate().print(date));
                    break;

                case 3:
                    time = to;
                case 2:
                    value = string == null ? "Ø" : DateTimeFormat.shortTime().print(time);
                    break;

                case 4:
                    value = PERIOD_FORMATTER.print(we.getWorkedHoursToday().toPeriod());
                    break;

                case 5:
                    final Duration saldoToday = we.getSaldoToday();
                    color = getNumberColor(saldoToday.getStandardSeconds());
                    value = PERIOD_FORMATTER.print(saldoToday.toPeriod());
                    break;
            }

            if (value != null) {
                TextView tv = (TextView) view;
                tv.setText(value);
                if (color != null) {
                    tv.setTextColor(color);
                }
                return true;
            } else

            {
                return false;
            }
        }

        public void setMode(boolean mode) {
            this.realHours = mode;
        }

    }

    LocalTime getEndTime(LocalTime stop, boolean isToday) {
        final LocalTime now = LocalTime.now();
        return !isDayClosed() && isToday && now.isAfter(stop) ? now : stop;
    }


    public boolean isDayClosed() {
        return new DateTime(getSharedPreferences().getLong(DAY_CLOSED_TIMESTAMP, 0)).isAfter(DateTime.now());
    }

    void refreshSaldo(Cursor data) {
        StandardWorkEstimator we = getSaldoWorkEstimator(data);

        final Duration currentSaldo = we.getSaldo();
        final Duration saldoToday = we.getSaldoToday();
        final Duration remainingToday = we.getRemainingToday();
        if (isDayClosed()) {
            currentSaldo.plus(we.getSaldoToday());
        }

        final View rootView = getView().getRootView();
        if (rootView != null) {
            TextView saldo = (TextView) rootView.findViewById(R.id.saldo);
            TextView dailyAverage = (TextView) rootView.findViewById(R.id.dailyAverage);
            TextView dailyTotal = (TextView) rootView.findViewById(R.id.dailyTotal);
            AnalogClock clock = (AnalogClock) rootView.findViewById(R.id.clock);
            ImageView gears = (SVGImageView) rootView.findViewById(R.id.gears);

            if (isDayClosed()) {
                clock.setVisibility(View.GONE);
                saldo.setVisibility(View.VISIBLE);
                gears.setVisibility(View.GONE);
                gears.clearAnimation();
            } else {
                clock.setVisibility(View.VISIBLE);
                saldo.setVisibility(View.GONE);
                gears.setVisibility(View.VISIBLE);
                gears.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_gear));
            }


            saldo.setText(PERIOD_FORMATTER.print(currentSaldo.toPeriod()));
            saldo.setTextColor(getNumberColor(currentSaldo.getStandardSeconds()));

            dailyAverage.setText(PERIOD_FORMATTER.print(saldoToday.toPeriod()));
            dailyAverage.setTextColor(getNumberColor(saldoToday.getStandardSeconds()));

            dailyTotal.setText(PERIOD_FORMATTER.print(remainingToday.toPeriod()));
            dailyTotal.setTextColor(getNumberColor(remainingToday.getStandardSeconds()));

            saldo.invalidate();
            dailyAverage.invalidate();
            dailyTotal.invalidate();

            Log.d("DashboardFragment", "Saldo reloaded");
        }
    }

    private StandardWorkEstimator getSaldoWorkEstimator(Cursor data) {

        LocalDateTime now = LocalDateTime.now();
        StandardWorkEstimator we = new StandardWorkEstimator(getPeriod(), now, Duration.standardHours(getTotalHours()));

        data.moveToFirst();

        while (!data.isAfterLast()) {
            final boolean isToday = LocalDate.now().isEqual(DATE_FORMAT.parseLocalDate(data.getString(1)));

            if (getRealWorkedTime()) {
                long duration = data.getLong(4);
                if (duration > 0) {
                    we.addHours(chunkOfWork(Duration.standardSeconds(duration), isToday));
                }
            } else {
                we.addHours(chunkOfWork(
                        TIME_FORMAT.parseLocalTime(data.getString(2)),
                        getEndTime(TIME_FORMAT.parseLocalTime(data.getString(3)), isToday),
                        Duration.standardMinutes(getPause()),
                        isToday));
            }
            data.moveToNext();
        }
        return we;
    }

    private boolean getRealWorkedTime() {
        return getSharedPreferences().getBoolean("real_worked_time", false);
    }

    public int getPause() {
        return Integer.parseInt(getSharedPreferences().getString("pause", "30").toUpperCase());
    }

    protected int getTotalHours() {
        return Integer.parseInt(getSharedPreferences().getString("total_hours", "40"));
    }

    public abstract class AbstractDaysLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        protected ISharedPreferencesProviderWithContext ctx;

        public AbstractDaysLoader(ISharedPreferencesProviderWithContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            LocalDate d = LocalDate.now();
            String start = DATE_FORMAT.print(getPeriod().from(d));
            String stop = DATE_FORMAT.print(getPeriod().to(d));
            Log.d("DashboardFragment", String.format("start: %s, stop: %s", start, stop));
            return new CursorLoader(ctx.getContext(), TIMEENTRIES_URI,
                    new String[]{DATE_COMPOSITE, DAY_START_COMPOSITE, DAY_END_COMPOSITE, DAY_TOTAL_COMPOSITE,
                            DAY_SALDO_COMPOSITE},
                    SELECT_WHERE,
                    new String[]{start, stop},
                    ORDER_BY);
        }
    }
}
