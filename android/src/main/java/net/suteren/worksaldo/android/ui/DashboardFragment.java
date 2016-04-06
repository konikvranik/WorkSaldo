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
import android.widget.AnalogClock;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import com.caverock.androidsvg.SVGImageView;
import net.suteren.worksaldo.IWorkEstimator;
import net.suteren.worksaldo.Period;
import net.suteren.worksaldo.StandardWorkEstimator;
import net.suteren.worksaldo.android.IRefreshable;
import net.suteren.worksaldo.android.IReloadable;
import net.suteren.worksaldo.android.R;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Duration;
import org.joda.time.DurationFieldType;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadWritablePeriod;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.joda.time.format.PeriodParser;
import org.joda.time.format.PeriodPrinter;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static net.suteren.worksaldo.StandardWorkEstimator.chunkOfWork;
import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;
import static net.suteren.worksaldo.android.ui.MainActivity.*;

/**
 * Fragment of main dashboard. Contains counters of work progress and list of days of work in actual period.
 */
public class DashboardFragment extends Fragment implements ISharedPreferencesProviderWithContext, IRefreshable,
        IReloadable {

    /**
     * formatting duration of work in H:mm format with possible sign before hours.
     */
    public static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .append(new PeriodPrinter() {
                @Override
                public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
                    return period.toPeriod().getMillis() < 0 ? 1 : 0;
                }

                @Override
                public int countFieldsToPrint(ReadablePeriod period, int stopAt, Locale locale) {
                    return 0;
                }

                @Override
                public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
                    if (period.toPeriod().getMillis() < 0) {
                        buf.append("-");
                    }
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
            .printZeroIfSupported()
            .append(new PeriodPrinter() {
                @Override
                public int calculatePrintedLength(ReadablePeriod period, Locale locale) {
                    return DecimalFormat.getIntegerInstance().format(period.get(DurationFieldType.hours())).length();
                }

                @Override
                public int countFieldsToPrint(ReadablePeriod period, int stopAt, Locale locale) {
                    return 1;
                }

                @Override
                public void printTo(StringBuffer buf, ReadablePeriod period, Locale locale) {
                    buf.append(String.format("%02d", Math.abs(period.get(DurationFieldType.hours()))));
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

    private LoaderManager.LoaderCallbacks<Cursor> DAYS_LOADER_CALLBACK = new AbstractDaysLoader(this) {
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            Cursor c = mAdapter.swapCursor(data);
            if (c != null) {
                c.close();
            }

            refreshSaldo(data);
            Log.d("LoaderCallbacks", "loaded " + data.getCount() + " items.");
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            Log.d("LoaderCallbacks", "loader reset");
        }
    };

    private LoaderManager.LoaderCallbacks<Cursor> RELOAD_CALLBACK = new AbstractDaysLoader(this) {
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
    private static final DateTimeFormatter WEEKDAY_FORMAT = DateTimeFormat.forPattern("E");
    private static final String DAY_CLOSED_TIMESTAMP = "day_closed_timestamp";
    private static final int MINUTE = 60000;
    private SimpleCursorAdapter mAdapter;
    private DayBinder dayBinder;
    private Runnable onReload;
    private ListView lv;
    private SwipeRefreshLayout refresh;
    private Timer refreshTimer;

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

        // Prepare list of days and set adapter on it.
        lv = (ListView) rootView.findViewById(R.id.listing);
        dayBinder = new DayBinder();
        dayBinder.setMode(getRealWorkedTime());
        mAdapter = new SimpleCursorAdapter(getCtx(), R.layout.row, null,
                new String[]{DATE_NAME, DAY_START_NAME, DAY_END_NAME, DAY_TOTAL_NAME, DAY_SALDO_NAME},
                new int[]{R.id.date, R.id.from, R.id.to, R.id.total, R.id.saldo}, 0);
        mAdapter.setViewBinder(dayBinder);
        lv.setAdapter(mAdapter);

        // reload data from DB.
        getLoaderManager().initLoader(DAYS_LOADER, null, DAYS_LOADER_CALLBACK);

        // set refresh action when swipe down list of days.
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

        rootView.findViewById(R.id.counters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchClosedDay();
            }
        });

        // Set timer to refresh data every minute.
        setupRefreshTimer();

        return rootView;
    }

    void setupRefreshTimer() {
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                refresh();
            }
        }, MINUTE, MINUTE);
    }

    @Override
    public void onResume() {
        super.onResume();
        dayBinder.setMode(getRealWorkedTime());
        refresh();
        setupRefreshTimer();
    }

    @Override
    public void onPause() {
        refreshTimer.cancel();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        refreshTimer.cancel();
        super.onDestroy();
    }

    private Period getPeriod() {
        return Period.valueOf(getSharedPreferences().getString("period", "week").toUpperCase(Locale.ENGLISH));
    }

    private void switchClosedDay() {
        if (isDayClosed()) {
            openDay();
        } else {
            closeDay();
        }
        Log.d("DashboardFragment", "Day is now " + (isDayClosed() ? "closed" : "open"));
        refresh();
    }

    private void closeDay() {
        getSharedPreferences().edit().putLong(DAY_CLOSED_TIMESTAMP, DateTime.now().withTimeAtStartOfDay().plus(Days
                .days(1)).getMillis()).apply();
    }

    private void openDay() {
        getSharedPreferences().edit().putLong(DAY_CLOSED_TIMESTAMP, 0).apply();
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

    /**
     * Causes reloading data from backend.
     */
    public void reload() {
        getLoaderManager().restartLoader(DAYS_UPDATER, null, RELOAD_CALLBACK);
    }

    /**
     * Sets action to be executed when reload is finished.
     *
     * @param action Action to execute when reload is finished.
     */
    public void onReload(Runnable action) {
        this.onReload = action;
    }

    @Override
    public void refresh() {
        Log.d("DashboardFragment", "Refreshing");
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().getContentResolver().notifyChange(TIMEENTRIES_URI, null);
                    getLoaderManager().restartLoader(DAYS_LOADER, null, DAYS_LOADER_CALLBACK);
                }
            });
        }
    }

    @Override
    public void onRefresh(Runnable action) {

    }

    private class DayBinder implements SimpleCursorAdapter.ViewBinder {

        private boolean realHours;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            TextView tv = (TextView) view;

            LocalTime time = TIME_FORMAT.parseLocalTime(cursor.getString(2));
            if (columnIndex == 2) { // time from
                tv.setText(DateTimeFormat.shortTime().print(TIME_FORMAT.parseLocalTime(cursor.getString(2))));
                return true;
            }

            final LocalDate day = DATE_FORMAT.parseLocalDate(cursor.getString(1));
            if (columnIndex == 1) { // date
                tv.setText(String.format("%s %s", WEEKDAY_FORMAT.print(day), DateTimeFormat.mediumDate().print(day)));
                return true;
            }

            boolean isToday = day.isEqual(LocalDate.now());
            final LocalTime to = getEndTime(TIME_FORMAT.parseLocalTime(cursor.getString(3)), isToday && !realHours);
            if (columnIndex == 3) { // time to
                tv.setText(to == null ? "Ø" : DateTimeFormat.shortTime().print(to));
                return true;
            }

            // prepare work estimator for both remaining columns: worked and bilance.
            IWorkEstimator we = new StandardWorkEstimator(getPeriod(), day.toLocalDateTime(to), Duration
                    .standardHours(getTotalHours()));
            if (realHours) {
                long duration = cursor.getLong(4);
                if (duration > 0) {
                    we.addHours(chunkOfWork(Duration.standardSeconds(duration), true));
                }
            } else {
                we.addHours(chunkOfWork(time, to, Duration.standardMinutes(getPause()), true));
            }

            switch (columnIndex) {

                case 4: // worked
                    tv.setText(PERIOD_FORMATTER.print(we.getWorkedHoursToday().toPeriod()));
                    return true;

                case 5: // balance
                    final Duration saldoToday = we.getSaldoToday();
                    tv.setText(PERIOD_FORMATTER.print(saldoToday.toPeriod()));
                    tv.setTextColor(getNumberColor(saldoToday.getStandardSeconds()));
                    return true;
            }
            return false;
        }

        public void setMode(boolean mode) {
            this.realHours = mode;
        }

    }

    private LocalTime getEndTime(LocalTime stop, boolean isToday) {
        final LocalTime now = LocalTime.now();
        return !isDayClosed() && isToday && now.isAfter(stop) ? now : stop;
    }


    private boolean isDayClosed() {
        return new DateTime(getSharedPreferences().getLong(DAY_CLOSED_TIMESTAMP, 0)).isAfter(DateTime.now());
    }

    private void refreshSaldo(Cursor data) {
        View rootView = getView();
        if (rootView == null || (rootView = getView().getRootView()) == null) {
            return;
        }

        StandardWorkEstimator we = getSaldoWorkEstimator(data);

        TextView mainCounter = (TextView) rootView.findViewById(R.id.saldo);
        TextView upperCounter = (TextView) rootView.findViewById(R.id.upperCounter);
        TextView lowerCounter = (TextView) rootView.findViewById(R.id.lowerCounter);
        @SuppressWarnings("deprecation")
        AnalogClock clock = (AnalogClock) rootView.findViewById(R.id.clock);
        ImageView gears = (SVGImageView) rootView.findViewById(R.id.gears);

        if (isDayClosed()) {
            mainCounter.setVisibility(View.VISIBLE);
            clock.setVisibility(View.GONE);
            gears.setVisibility(View.GONE);
            gears.clearAnimation();

            // main counter
            Duration currentSaldo = we.getSaldo().plus(we.getSaldoToday());
            mainCounter.setTextColor(getNumberColor(currentSaldo.getStandardSeconds()));
            mainCounter.setText(PERIOD_FORMATTER.print(currentSaldo.toPeriod()));

            // upper counter
            upperCounter.setText(PERIOD_FORMATTER.print(we.getWorkedHours().toPeriod()));

            // lower counter
            lowerCounter.setText(PERIOD_FORMATTER.print(we.getRemainingTotal().toPeriod()));
        } else {
            mainCounter.setVisibility(View.GONE);
            clock.setVisibility(View.VISIBLE);
            gears.setVisibility(View.VISIBLE);
            gears.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_gear));

            // upper counter
            upperCounter.setText(TIME_FORMAT.print(LocalTime.now().plus(we.getSaldoToday()
                    .toPeriodFrom(DateTime.now()))));

            // lower counter
            lowerCounter.setText(
                    TIME_FORMAT.print(LocalTime.now().plus(we.getSaldo().plus(we.getSaldoToday())
                            .toPeriodFrom(DateTime.now()))));
        }

        Log.d("DashboardFragment", "Saldo reloaded");
    }

    private void resetColor(TextView lowerCounter) {
        if (Build.VERSION.SDK_INT < 23) {
            lowerCounter.setTextColor(getResources().getColor(android.R.color.primary_text_dark));
        } else {
            lowerCounter.setTextColor(getCtx().getColor(android.R.color.primary_text_dark));
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

    /**
     * Return value from shared preferences.
     *
     * @return duration of mandatory work pause in minutes.
     */
    public int getPause() {
        return Integer.parseInt(getSharedPreferences().getString("pause", "30"));
    }

    /**
     * Return value from shared preferences.
     *
     * @return demanded worked hours per period.
     */
    public int getTotalHours() {
        return Integer.parseInt(getSharedPreferences().getString("total_hours", "40"));
    }

    private abstract class AbstractDaysLoader implements LoaderManager.LoaderCallbacks<Cursor> {

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
