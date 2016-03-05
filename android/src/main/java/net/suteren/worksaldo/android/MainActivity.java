package net.suteren.worksaldo.android;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import static net.suteren.worksaldo.android.TogglCachedProvider.*;


public class MainActivity extends Activity {

    public static final int INSTANT_DATABASE_LOADER = 1;
    public static final int REMOTE_SERVICE_LOADER = 2;
    public static final int SALDO_LOADER = 3;
    public static final String INSTANT = "instant";

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
        checkLogin();
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_login) {
            new LoginDialog(this).show();
        } else if (id == R.id.action_refresh) {
            Fragment f = getFragmentManager().findFragmentById(android.R.id.content);
            if (f instanceof LoaderManager.LoaderCallbacks) {
                getLoaderManager().restartLoader(REMOTE_SERVICE_LOADER, loaderBundle(false), (LoaderManager.LoaderCallbacks<Cursor>) f);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkLogin() {

        if (getSharedPreferences().getString(API_KEY, null) == null) {
            new LoginDialog(this).show();
        }


    }

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(MAIN, MODE_PRIVATE);
    }


    public static Bundle loaderBundle(boolean value) {
        Bundle instantBundle = new Bundle();
        instantBundle.putBoolean("instant", value);
        return instantBundle;
    }

}
