package net.suteren.worksaldo.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

import static net.suteren.worksaldo.android.TogglCachedProvider.*;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
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
    public static class PlaceholderFragment extends Fragment {

        private SimpleCursorAdapter mAdapter;

        public PlaceholderFragment() {
        }

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
            final String[] perspective = {DAY_START, DAY_END, DAY_TOTAL};
            lv.setAdapter(mAdapter = new SimpleCursorAdapter(getCtx(), R.layout.row, null, perspective,
                    new int[]{R.id.date, R.id.from, R.id.to}, 0));
            lm.initLoader(1, null, new LoaderManager.LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    return new CursorLoader(getCtx(), new Uri.Builder().scheme("content")
                            .authority(TogglCachedProvider.URI_BASE)
                            .appendPath(TogglCachedProvider.TIMEENTRY_PATH).build(),
                            perspective, WHERE,
                            new String[]{String.format("date(%s)",
                                    new SimpleDateFormat("yyyy-MM-dd").format(new Date()))}, ORDER_BY);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                    mAdapter.swapCursor(data);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    mAdapter.swapCursor(null);
                }
            });
            return rootView;
        }
    }
}
