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
import net.suteren.worksaldo.android.IReloadable;
import net.suteren.worksaldo.android.R;
import net.suteren.worksaldo.android.WorkEstimator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;
import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;
import static net.suteren.worksaldo.android.ui.MainActivity.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardFragment extends Fragment implements ISharedPreferencesProviderWithContext, IReloadable {

    private static final DateFormat WEEKDAY_FORMAT = new SimpleDateFormat("E");
    public static final String DAY_CLOSED_TIMESTAMP = "day_closed_timestamp";
    private SimpleCursorAdapter mAdapter;
    private final DateFormat DATE_FORMAT_INSTANCE = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
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
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.DAY_OF_WEEK, 1);
        getSharedPreferences().edit().putLong(DAY_CLOSED_TIMESTAMP, c.getTimeInMillis()).apply();
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

                WorkEstimator we = new WorkEstimator(getPeriod(), Calendar.getInstance(), getTotalHours(), getPause(),
                        isDayClosed());

                final float[] workedHours = we.countTotal(data, rwt);
                final float currentSaldo = we.getSaldo(workedHours);

                TextView saldo = (TextView) rootView.findViewById(R.id.saldo);
                TextView dailyAverage = (TextView) rootView.findViewById(R.id.dailyAverage);
                TextView dailyTotal = (TextView) rootView.findViewById(R.id.dailyTotal);

                saldo.setText(formatHour(currentSaldo));
                saldo.setTextColor(getNumberColor(currentSaldo));

                final float todayToAvg = we.getTodayToAvg(workedHours[1]);
                dailyAverage.setText(formatHour(todayToAvg));
                dailyAverage.setTextColor(getNumberColor(todayToAvg));

                final float todayToWhole = we.getTodayToWhole(workedHours);
                dailyTotal.setText(formatHour(todayToWhole));
                dailyTotal.setTextColor(getNumberColor(todayToWhole));

                saldo.invalidate();
                dailyAverage.invalidate();
                dailyTotal.invalidate();

                Log.d("DashboardFragment", "Salgo reloaded");

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
        if (lv == null)
            return;
        lv.invalidate();
        lv.invalidateViews();

    }

    private class DayBinder implements SimpleCursorAdapter.ViewBinder {

        private boolean realHours;

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String value = null;
            WorkEstimator we = new WorkEstimator(getDaysLoaderCallback().getPeriod(), Calendar.getInstance(),
                    getDaysLoaderCallback().getTotalHours(), getDaysLoaderCallback().getPause(), isDayClosed());
            Integer color = null;

            String string = cursor.getString(columnIndex);
            SimpleDateFormat dateFormat = DATE_FORMAT;

            Calendar c = Calendar.getInstance();
            dateFormat.setTimeZone(c.getTimeZone());

            switch (columnIndex) {
                case 1:
                    try {
                        final Date date = dateFormat.parse(string);
                        value = String.format("%s %s", WEEKDAY_FORMAT.format(date), DATE_FORMAT_INSTANCE.format(date));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                case 2:
                case 3:
                    try {
                        value = string == null ? "Ø" : SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format
                                (TIME_FORMAT
                                        .parse(string));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                case 4:
                    float count = we.getCount(cursor.getFloat(columnIndex), cursor.getString(2), cursor.getString(3),
                            realHours);
                    value = formatHour(count);
                    break;

                case 5:

                    try {

                        c.setTime(dateFormat.parse(cursor.getString(1)));
                        float s = we.getCount(cursor.getFloat(4), cursor.getString(2), cursor.getString(3), realHours)
                                - we.getHoursPerDay();
                        color = getNumberColor(s);
                        value = formatHour(s);
                    } catch (ParseException e) {
                        value = "Ø";
                    }
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

    private String formatHour(float s) {
        return String.format("%.0f:%02.0f", Math.floor(s), Math.abs((s - (int) s) * 60));
    }

    public boolean isDayClosed() {
        return getSharedPreferences().getLong(DAY_CLOSED_TIMESTAMP, 0) > System.currentTimeMillis();
    }
}
