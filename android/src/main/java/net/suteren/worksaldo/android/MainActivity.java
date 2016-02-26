package net.suteren.worksaldo.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static net.suteren.worksaldo.android.TogglCachedProvider.*;


public class MainActivity extends Activity {

    public static final String MAIN = "main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DashboardFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DashboardFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        public static final int INSTANT_DATABASE_LOADER = 1;
        public static final int REMOTE_SERVICE_LOADER = 2;
        public static final String INSTANT = "instant";
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
            mAdapter.setViewBinder(new Binder());
            lv.setAdapter(mAdapter);

            Bundle instantBundle = new Bundle();
            instantBundle.putBoolean("instant", true);
            lm.initLoader(INSTANT_DATABASE_LOADER, instantBundle, this);

            Bundle delayedBundle = new Bundle();
            delayedBundle.putBoolean("instant", false);
            // lm.initLoader(REMOTE_SERVICE_LOADER, delayedBundle, this);

            return rootView;
        }


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            boolean instant = args.getBoolean(INSTANT, false);
            return new CursorLoader(getCtx(), new Uri.Builder().scheme("content")
                    .authority(TogglCachedProvider.URI_BASE)
                    .appendPath(TogglCachedProvider.TIMEENTRY_PATH)
                    .appendQueryParameter(INSTANT, String.valueOf(instant))
                    .build(),
                    new String[]{DATE_COMPOSITE, DAY_START_COMPOSITE, DAY_END_COMPOSITE, DAY_TOTAL_COMPOSITE},
                    WHERE,
                    new String[]{DATE_FORMAT.format(startDate()), DATE_FORMAT.format(endDate())},
                    ORDER_BY);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Cursor c = mAdapter.swapCursor(data);
            if (c != null) {
                c.close();
            }
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
                        int count = cursor.getInt(columnIndex) / 3600;
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
}
