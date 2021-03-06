package net.suteren.worksaldo.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import net.suteren.worksaldo.android.IRefreshable;
import net.suteren.worksaldo.android.IReloadable;
import net.suteren.worksaldo.android.R;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.API_KEY;


public class MainActivity extends Activity implements ISharedPreferencesProvider {

    public static final int DAYS_LOADER = 1;
    public static final int DAYS_UPDATER = 2;

    public static final String MAIN = "main";
    private LoginDialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.container, new DashboardFragment()).commit();
        }
        checkLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment f = getFragmentManager().findFragmentById(R.id.container);
        if (f instanceof IReloadable) {
            ((IReloadable) f).reload();
        }
        if (f instanceof IRefreshable) {
            ((IRefreshable) f).refresh();
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

        switch (id){

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.action_login:
                getLoginDialog().show();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private LoginDialog getLoginDialog() {
        if (loginDialog == null) {
            loginDialog = new LoginDialog(this);
        }
        return loginDialog;
    }

    private void checkLogin() {
        if (getSharedPreferences().getString(API_KEY, null) == null) {
            getLoginDialog().show();
        }
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(MAIN, MODE_PRIVATE);
    }

}
