package net.suteren.worksaldo.android.ui;

import android.content.SharedPreferences;

/**
 * Interface which guarantees to return default shared preferences.
 */
public interface ISharedPreferencesProvider {

    /**
     * @return default shared preferences.
     */
    SharedPreferences getSharedPreferences();
}
