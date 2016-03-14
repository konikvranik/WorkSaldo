package net.suteren.worksaldo.android.ui;

import android.content.Context;

/**
 * Interface which guarantees to provide application context and default shared preferences.
 */
public interface ISharedPreferencesProviderWithContext extends ISharedPreferencesProvider {

    /**
     * @return application context.
     */
    Context getContext();
}
