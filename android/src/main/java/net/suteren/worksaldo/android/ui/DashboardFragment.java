package net.suteren.worksaldo.android.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import net.suteren.worksaldo.IWorkEstimator;
import net.suteren.worksaldo.StandardWorkEstimator;
import net.suteren.worksaldo.android.IReloadable;
import net.suteren.worksaldo.android.R;
import org.joda.time.*;
import org.joda.time.format.*;

import static android.content.Context.MODE_PRIVATE;
import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;
import static net.suteren.worksaldo.android.ui.MainActivity.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardFragment extends Fragment implements ISharedPreferencesProviderWithContext, IReloadable {

    public static final PeriodFormatter PERIOD_FORMATTER = new PeriodFormatterBuilder()
            .printZeroIfSupported()
            .appendHours()
            .appendSeparator(":")
            .minimumPrintedDigits(2)
            .rejectSignedValues(false)
            .appendMinutes()
            .toFormatter();
    private static final DateTimeFormatter WEEKDAY_FORMAT = DateTimeFormat.forPattern("E");
    public static final String DAY_CLOSED_TIMESTAMP = "day_closed_timestamp";
    private SimpleCursorAdapter mAdapter;
    private DayBinder dayBinder;
    private Runnable onReload;
    private ListView lv;

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
        final LoaderManager lm = getLoaderManager();
        mAdapter = new SimpleCursorAdapter(getCtx(), R.layout.row, null,
                new String[]{DATE_NAME, DAY_START_NAME, DAY_END_NAME, DAY_TOTAL_NAME, DAY_SALDO_NAME},
                new int[]{R.id.date, R.id.from, R.id.to, R.id.total, R.id.saldo}, 0);
        dayBinder = new DayBinder();
        dayBinder.setMode(((MainActivity) getActivity()).getSharedPreferences().getBoolean("real_worked_time", true));
        mAdapter.setViewBinder(dayBinder);
        lv.setAdapter(mAdapter);

        lm.initLoader(INSTANT_DATABASE_LOADER, loaderBundle(true), getDaysLoaderCallback());
        lm.initLoader(SALDO_LOADER, loaderBundle(true), getSaldoLoaderCallback(rootView));
        getLoaderManager().initLoader(REMOTE_SERVICE_LOADER, loaderBundle(false), getDaysLoaderCallback());

        rootView.findViewById(R.id.counters).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchClosedDay();
                lm.restartLoader(SALDO_LOADER, loaderBundle(true), getSaldoLoaderCallback(rootView));
                Log.d("DashboardFragment", "Day is now " + (isDayClosed() ? "closed" : "open"));
            }
        });

        updateCountersColor(rootView);

        reload();

        return rootView;
    }

    private void switchClosedDay() {
        if (isDayClosed()) {
            openDay();
        } else {
            closeDay();
        }
    }

    private void closeDay() {
        getSharedPreferences().edit().putLong(DAY_CLOSED_TIMESTAMP, DateTime.now().plus(Days.days(1)).getMillis()).apply();
        updateCountersColor(getView());
    }

    private void updateCountersColor(View view) {
        view.findViewById(R.id.counters).setBackgroundColor(getColor(isDayClosed() ? R.color.closed : R.color.open));
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
                mAdapter.notifyDataSetChanged();
                if (c != null) {
                    //c.close();
                }
                getLoaderManager().restartLoader(SALDO_LOADER, loaderBundle(true),
                        getSaldoLoaderCallback(getView().getRootView()));
                Log.d("LoaderCallbacks", "loader finished");
                Log.d("LoaderCallbacks", "loaded " + data.getCount() + " items.");
                if (onReload != null) {
                    onReload.run();
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                Cursor c = mAdapter.swapCursor(null);
                if (c != null) {
                    //c.close();
                }
                Log.d("LoaderCallbacks", "loader reset");
                if (onReload != null) {
                    onReload.run();
                }
            }
        };
    }

    private AbstractDaysLoader getSaldoLoaderCallback(final View rootView) {
        return new AbstractDaysLoader(this) {
            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                SharedPreferences sharedPreferences = ctx.getSharedPreferences();

                boolean rwt = sharedPreferences.getBoolean("real_worked_time", true);

                LocalDateTime now = LocalDateTime.now();
                if (isDayClosed()) {
                    now = now.plus(Duration.standardDays(1));
                }
                StandardWorkEstimator we = new StandardWorkEstimator(getPeriod(), now, Duration.standardHours(getTotalHours()));

                data.moveToFirst();

                int cnt = 0;

                while (!data.isAfterLast()) {
                    final boolean isToday = !isDayClosed() && LocalDate.now().isEqual(DATE_FORMAT.parseLocalDate(data.getString(1)));
                    Log.d("DashboardFragment", String.format("%s - %s", data.getString(2), data.getString(3)));
                    final IWorkEstimator.ChunkOfWork day = StandardWorkEstimator.chunkOfWork(
                            TIME_FORMAT.parseLocalTime(data.getString(2)),
                            isToday ? LocalTime.now() : TIME_FORMAT.parseLocalTime(data.getString(3)),
                            Duration.standardMinutes(getPause()),
                            isToday);
                    Log.d("DashboardFragment", String.format("%d", day.getHours().getStandardHours()));
                    Log.d("DashboardFragment", String.format("%s", day.isToday()));
                    we.addHours(day);
                    Log.d("DashboardFragment", String.format("%d - Worked: %s", ++cnt, PERIOD_FORMATTER.print(we.getWorkedHours().toPeriod())));
                    data.moveToNext();
                }
                Log.d("DashboardFragment", String.format("Expected: %s", PERIOD_FORMATTER.print(we.getExpected().toPeriod())));
                Log.d("DashboardFragment", String.format("Worked: %s", PERIOD_FORMATTER.print(we.getWorkedHours().toPeriod())));
                Log.d("DashboardFragment", String.format("Hours Per Day: %s", PERIOD_FORMATTER.print(we.getHoursPerDay().toPeriod())));

                final Duration currentSaldo = we.getSaldo();

                TextView saldo = (TextView) rootView.findViewById(R.id.saldo);
                TextView dailyAverage = (TextView) rootView.findViewById(R.id.dailyAverage);
                TextView dailyTotal = (TextView) rootView.findViewById(R.id.dailyTotal);

                saldo.setText(PERIOD_FORMATTER.print(currentSaldo.toPeriod()));
                saldo.setTextColor(getNumberColor(currentSaldo.getStandardSeconds()));

                final Duration saldoToday = we.getSaldoToday();
                dailyAverage.setText(PERIOD_FORMATTER.print(saldoToday.toPeriod()));
                dailyAverage.setTextColor(getNumberColor(saldoToday.getStandardSeconds()));

                final Duration remainingToday = we.getRemainingToday();
                dailyTotal.setText(PERIOD_FORMATTER.print(remainingToday.toPeriod()));
                dailyTotal.setTextColor(getNumberColor(remainingToday.getStandardSeconds()));

                saldo.invalidate();
                dailyAverage.invalidate();
                dailyTotal.invalidate();

                Log.d("DashboardFragment", "Saldo reloaded");

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

    @Override
    public void reload() {
        getLoaderManager().restartLoader(REMOTE_SERVICE_LOADER, loaderBundle(false), getDaysLoaderCallback());
    }

    @Override
    public void onReload(Runnable action) {
        this.onReload = action;
    }

    private class DayBinder implements SimpleCursorAdapter.ViewBinder {

        private boolean realHours;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String value = null;
            LocalTime stop = TIME_FORMAT.parseLocalTime(cursor.getString(3));
            LocalDateTime day = DATE_FORMAT.parseLocalDate(cursor.getString(1)).toLocalDateTime(stop);
            IWorkEstimator we = new StandardWorkEstimator(getDaysLoaderCallback().getPeriod(), day,
                    Duration.standardHours(getDaysLoaderCallback().getTotalHours()));

            final LocalTime start = TIME_FORMAT.parseLocalTime(cursor.getString(2));
            we.addHours(StandardWorkEstimator.chunkOfWork(start, TIME_FORMAT.parseLocalTime(cursor.getString(3)), Duration.standardMinutes(getDaysLoaderCallback().getPause()), true));

            Integer color = null;

            String string = cursor.getString(columnIndex);


            switch (columnIndex) {
                case 1:
                    final LocalDate date = DATE_FORMAT.parseLocalDate(string);
                    value = String.format("%s %s", WEEKDAY_FORMAT.print(date), DateTimeFormat.mediumDate().print(date));
                    break;

                case 2:
                case 3:
                    value = string == null ? "Ø" : DateTimeFormat.shortTime().print(TIME_FORMAT.parseDateTime(string));
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


    public boolean isDayClosed() {
        return getSharedPreferences().getLong(DAY_CLOSED_TIMESTAMP, 0) > System.currentTimeMillis();
    }
}
