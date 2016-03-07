package net.suteren.worksaldo.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import net.suteren.worksaldo.android.IReloadable;
import net.suteren.worksaldo.android.R;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;


public class MainActivity extends Activity implements ISharedPreferencesProvider {

    public static final int INSTANT_DATABASE_LOADER = 1;
    public static final int REMOTE_SERVICE_LOADER = 2;
    public static final int SALDO_LOADER = 3;
    public static final String INSTANT = "instant";

    public static final String MAIN = "main";
    private Menu myMenu;

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
        myMenu = menu;
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
            Log.d("MainActivity", "refreshing...");
            Fragment f = getFragmentManager().findFragmentById(R.id.container);
            if (f instanceof IReloadable) {

                ((IReloadable) f).onReload(new Runnable() {
                    @Override
                    public void run() {
                        resetUpdating();
                    }
                });
                startUpdating();
                ((IReloadable) f).reload();

            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkLogin() {

        if (getSharedPreferences().getString(API_KEY, null) == null) {
            new LoginDialog(this).show();
        }


    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(MAIN, MODE_PRIVATE);
    }


    public static Bundle loaderBundle(boolean value) {
        Bundle instantBundle = new Bundle();
        instantBundle.putBoolean("instant", value);
        return instantBundle;
    }

    public void startUpdating() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        myMenu.findItem(R.id.action_refresh).setActionView(iv);
    }

    public void resetUpdating() {
        // Get our refresh item from the menu
        MenuItem m = myMenu.findItem(R.id.action_refresh);
        if (m.getActionView() != null) {
            // Remove the animation.
            m.getActionView().clearAnimation();
            m.setActionView(null);
        }
    }
}
