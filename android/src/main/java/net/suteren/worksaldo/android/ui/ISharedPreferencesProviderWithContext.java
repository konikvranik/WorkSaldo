package net.suteren.worksaldo.android.ui;

import android.content.Context;

/**
 * Created by hpa on 7.3.16.
 */
public interface ISharedPreferencesProviderWithContext extends ISharedPreferencesProvider {

    Context getContext();
}
