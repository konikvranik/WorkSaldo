package net.suteren.worksaldo.android;

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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static net.suteren.worksaldo.android.MainActivity.SALDO_LOADER;
import static net.suteren.worksaldo.android.MainActivity.loaderBundle;
import static net.suteren.worksaldo.android.TogglCachedProvider.*;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mAdapter;

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
                new String[]{DATE_NAME, DAY_START_NAME, DAY_END_NAME, DAY_TOTAL_NAME},
                new int[]{R.id.date, R.id.from, R.id.to, R.id.total}, 0);
        updateBinder();
        lv.setAdapter(mAdapter);

        lm.initLoader(MainActivity.INSTANT_DATABASE_LOADER, loaderBundle(true), this);

        lm.initLoader(MainActivity.REMOTE_SERVICE_LOADER, loaderBundle(false), this);

        final TextView saldo = (TextView) rootView.findViewById(R.id.saldo);
        lm.initLoader(MainActivity.SALDO_LOADER, loaderBundle(true), getSaldoLoaderCallback(saldo));

        return rootView;
    }

    private LoaderManager.LoaderCallbacks<Cursor> getSaldoLoaderCallback(final TextView saldo) {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return DashboardFragment.this.onCreateLoader(id, args);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                SharedPreferences sharedPreferences = ((MainActivity) getActivity()).getSharedPreferences();
                boolean rwt = sharedPreferences.getBoolean("real_worked_time", true);
                int totalWanted = Integer.parseInt(sharedPreferences.getString("total_hours", "0"));
                long diff = Calendar.getInstance().getTimeInMillis() - startDate().getTimeInMillis();
                long days = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                Log.d("DashboardFragment", String.format("Days: %d", days));
                long est = totalWanted / 5 * days;
                Log.d("DashboardFragment", String.format("Est: %d", est));
                data.moveToFirst();
                int cnt = 0;
                while (!data.isAfterLast()) {
                    if (rwt) {
                        cnt += data.getInt(4);
                    } else {
                        try {
                            long start = TIME_FORMAT.parse(data.getString(2)).getTime();
                            long stop = TIME_FORMAT.parse(data.getString(3)).getTime();
                            cnt += (start - stop) / 1000;
                        } catch (ParseException e) {
                            Log.e("DashboardFragment", "Parse time", e);
                        }
                    }
                    data.moveToNext();
                }
                Log.d("DashboardFragment", String.format("Cnt: %d", cnt));
                saldo.setText(String.format("%d", est - (cnt / 3600)));
                saldo.invalidate();
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private void updateBinder() {
        mAdapter.setViewBinder(new Binder(((MainActivity) getActivity()).getSharedPreferences()
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
        String start = DATE_FORMAT.format(startDate().getTime());
        String stop = DATE_FORMAT.format(endDate().getTime());
        Log.d("DashboardFragment", String.format("start: %s, stop: %s", start, stop));
        return new CursorLoader(getCtx(), new Uri.Builder().scheme("content")
                .authority(TogglCachedProvider.URI_BASE)
                .appendPath(TogglCachedProvider.TIMEENTRY_PATH)
                .appendQueryParameter(MainActivity.INSTANT, String.valueOf(instant))
                .build(),
                new String[]{DATE_COMPOSITE, DAY_START_COMPOSITE, DAY_END_COMPOSITE, DAY_TOTAL_COMPOSITE},
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
        getLoaderManager().restartLoader(SALDO_LOADER, loaderBundle(true), getSaldoLoaderCallback((TextView) getView().getRootView().findViewById(R.id.saldo)));
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


    private class Binder implements SimpleCursorAdapter.ViewBinder {

        private final boolean realHours;

        private Binder(boolean realHours) {
            this.realHours = realHours;
        }

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            String value = null;
            switch (columnIndex) {
                case 1:
                    try {
                        value = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(DATE_FORMAT
                                .parse(cursor.getString(columnIndex)));
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
                    int count = 0;
                    try {
                        count = (realHours ? cursor.getInt(columnIndex) :
                                (int) (TIME_FORMAT.parse(cursor.getString(3)).getTime() - TIME_FORMAT.parse(cursor
                                        .getString(2)).getTime()) / 1000) / 3600;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    try {
                        value = getResources().getQuantityString(count, R.plurals.hour);
                    } catch (Resources.NotFoundException e) {
                        value = DecimalFormat.getNumberInstance().format(count);
                    }
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
}
