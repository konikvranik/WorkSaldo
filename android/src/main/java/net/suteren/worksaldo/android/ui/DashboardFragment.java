package net.suteren.worksaldo.android.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import net.suteren.worksaldo.android.R;
import net.suteren.worksaldo.android.WorkEstimator;
import net.suteren.worksaldo.android.provider.TogglCachedProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;
import static net.suteren.worksaldo.android.ui.MainActivity.SALDO_LOADER;
import static net.suteren.worksaldo.android.ui.MainActivity.loaderBundle;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, ISharedPreferencesProvider {

    private static final DateFormat WEEKDAY_FORMAT = new SimpleDateFormat("E");
    private SimpleCursorAdapter mAdapter;
    private final DateFormat DATE_FORMAT_INSTANCE = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);

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


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ListView lv = (ListView) rootView.findViewById(R.id.listing);
        LoaderManager lm = getLoaderManager();
        mAdapter = new SimpleCursorAdapter(getCtx(), R.layout.row, null,
                new String[]{DATE_NAME, DAY_START_NAME, DAY_END_NAME, DAY_TOTAL_NAME, DAY_SALDO_NAME},
                new int[]{R.id.date, R.id.from, R.id.to, R.id.total, R.id.saldo}, 0);
        updateBinder();
        lv.setAdapter(mAdapter);

        lm.initLoader(MainActivity.INSTANT_DATABASE_LOADER, loaderBundle(true), this);

        lm.initLoader(MainActivity.REMOTE_SERVICE_LOADER, loaderBundle(false), this);

        lm.initLoader(MainActivity.SALDO_LOADER, loaderBundle(true), getSaldoLoaderCallback((TextView) rootView.findViewById(R.id.saldo), (TextView) rootView.findViewById(R.id.dailyAverage), (TextView) rootView.findViewById(R.id.dailyTotal)));

        return rootView;
    }

    private LoaderManager.LoaderCallbacks<Cursor> getSaldoLoaderCallback(final TextView saldo, final TextView dailyAverage, final TextView dailyTotal) {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            public float todayCount;

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return DashboardFragment.this.onCreateLoader(id, args);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                SharedPreferences sharedPreferences = ((MainActivity) getActivity()).getSharedPreferences();

                boolean rwt = sharedPreferences.getBoolean("real_worked_time", true);

                WorkEstimator we = new WorkEstimator(getPeriod(DashboardFragment.this), Calendar.getInstance(), getTotalHours(DashboardFragment.this));

                final float currentSaldo = we.getSaldo(countTotal(data, rwt));
                saldo.setText(String.format("%.1f", currentSaldo));

                final float todayRemains = todayCount - we.getHoursPerDay();
                dailyAverage.setText(String.format("%.1f", todayRemains));
                dailyTotal.setText(String.format("%.1f", todayRemains + currentSaldo));

                saldo.invalidate();
                dailyAverage.invalidate();
                dailyTotal.invalidate();

            }

            private float countTotal(Cursor data, boolean rwt) {
                data.moveToFirst();
                float cnt = 0;

                while (!data.isAfterLast()) {
                    float tdc = getCount(data.getFloat(4), data.getString(2), data.getString(3), rwt);
                    cnt += tdc;
                    try {
                        if (DateUtils.isToday(DATE_FORMAT.parse(data.getString(1)).getTime())) {
                            todayCount = tdc;
                        }
                    } catch (ParseException e) {
                        Log.e("DashboardFragment", "Unable parse date", e);
                    }
                    data.moveToNext();
                }

                return cnt;
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private int getTotalHours(ISharedPreferencesProvider sharedPreferences) {
        return Integer.parseInt(sharedPreferences.getSharedPreferences().getString("total_hours", "40"));
    }

    private void updateBinder() {
        mAdapter.setViewBinder(new DayBinder(((MainActivity) getActivity()).getSharedPreferences()
                .getBoolean("real_worked_time", true)));
    }

    @Override
    public void onResume() {
        updateBinder();
        super.onResume();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        boolean instant = args.getBoolean(MainActivity.INSTANT, false);
        Calendar d = Calendar.getInstance();
        String start = DATE_FORMAT.format(TogglCachedProvider.getPeriod(this).from(d).getTime());
        String stop = DATE_FORMAT.format(TogglCachedProvider.getPeriod(this).to(d).getTime());
        Log.d("DashboardFragment", String.format("start: %s, stop: %s", start, stop));
        return new CursorLoader(getCtx(), new Uri.Builder().scheme("content")
                .authority(TogglCachedProvider.URI_BASE)
                .appendPath(TogglCachedProvider.TIMEENTRY_PATH)
                .appendQueryParameter(MainActivity.INSTANT, String.valueOf(instant))
                .build(),
                new String[]{DATE_COMPOSITE, DAY_START_COMPOSITE, DAY_END_COMPOSITE, DAY_TOTAL_COMPOSITE, DAY_SALDO_COMPOSITE},
                WHERE,
                new String[]{start, stop},
                ORDER_BY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Cursor c = mAdapter.swapCursor(data);
        if (c != null) {
            c.close();
        }
        getLoaderManager().restartLoader(SALDO_LOADER, loaderBundle(true), getSaldoLoaderCallback((TextView) getView().getRootView().findViewById(R.id.saldo), (TextView) getView().getRootView().findViewById(R.id.dailyAverage), (TextView) getView().getRootView().findViewById(R.id.dailyTotal)));
        Log.d("LoaderCallbacks", "loader finished");
        Log.d("LoaderCallbacks", "loaded " + data.getCount() + " items.");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Cursor c = mAdapter.swapCursor(null);
        if (c != null) {
            c.close();
        }
        Log.d("LoaderCallbacks", "loader reset");
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        if (getActivity() instanceof ISharedPreferencesProvider)
            return ((ISharedPreferencesProvider) getActivity()).getSharedPreferences();
        return null;
    }


    private class DayBinder implements SimpleCursorAdapter.ViewBinder {

        private final boolean realHours;

        private DayBinder(boolean realHours) {
            this.realHours = realHours;
        }

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String value = null;

            switch (columnIndex) {
                case 1:
                    try {
                        final Date date = DATE_FORMAT.parse(cursor.getString(columnIndex));
                        value = String.format("%s %s", WEEKDAY_FORMAT.format(date), DATE_FORMAT_INSTANCE.format(date));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                case 2:
                case 3:
                    try {
                        value = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(TIME_FORMAT
                                .parse(cursor.getString(columnIndex)));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    break;

                case 4:
                    float count = getCount(cursor.getFloat(columnIndex), cursor.getString(2), cursor.getString(3), realHours);
                    try {
                        value = getResources().getQuantityString(Math.round(count), R.plurals.hour);
                    } catch (Resources.NotFoundException e) {
                        value = String.format("%.1f", count);
                    }
                    break;

                case 5:
                    Calendar c = Calendar.getInstance();
                    try {
                        c.setTime(DATE_FORMAT.parse(cursor.getString(1)));
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    WorkEstimator we = new WorkEstimator(getPeriod(DashboardFragment.this), c, getTotalHours(DashboardFragment.this));
                    float s = getCount(cursor.getFloat(4), cursor.getString(2), cursor.getString(3), realHours) - we.getHoursPerDay();
                    value = String.format("%.1f", s);
                    break;
            }

            if (value != null)

            {
                TextView tv = (TextView) view;
                tv.setText(value);
                return true;
            } else

            {
                return false;
            }
        }

    }

    private float getCount(Float total, String start, String stop, boolean realHours) {

        float count = 0;
        try {
            count = (realHours ? total :
                    (int) (TIME_FORMAT.parse(stop).getTime() - TIME_FORMAT.parse(start).getTime()) / 1000 - getPause()) / 3600;
        } catch (ParseException e) {
            Log.e("DashboardFragment", "Unable to parse date", e);
        }
        return count;
    }

    private float getPause() {
        return Integer.parseInt(getSharedPreferences().getString("pause", "30")) * 60;
    }
}
