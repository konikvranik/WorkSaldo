package net.suteren.worksaldo.android.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import net.suteren.worksaldo.android.Period;
import net.suteren.worksaldo.android.provider.TogglCachedProvider;

import java.util.Calendar;

import static net.suteren.worksaldo.android.provider.TogglCachedProvider.*;

/**
 * Created by hpa on 7.3.16.
 */
public abstract class AbstractDaysLoader implements LoaderManager.LoaderCallbacks<Cursor> {

    protected ISharedPreferencesProviderWithContext ctx;

    public AbstractDaysLoader(ISharedPreferencesProviderWithContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        boolean instant = args.getBoolean(MainActivity.INSTANT, false);
        Calendar d = Calendar.getInstance();
        String start = formatDate(getPeriod().from(d));
        String stop = formatDate(getPeriod().to(d));
        Log.d("DashboardFragment", String.format("start: %s, stop: %s", start, stop));
        return new CursorLoader(ctx.getContext(),
                new Uri.Builder().scheme("content")
                        .authority(TogglCachedProvider.URI_BASE)
                        .appendPath(TogglCachedProvider.TIMEENTRY_PATH)
                        .appendQueryParameter(MainActivity.INSTANT, String.valueOf(instant))
                        .build(),
                new String[]{DATE_COMPOSITE, DAY_START_COMPOSITE, DAY_END_COMPOSITE, DAY_TOTAL_COMPOSITE,
                        DAY_SALDO_COMPOSITE},

                SELECT_WHERE,
                new String[]{start, stop},
                ORDER_BY);
    }

    private static String formatDate(Calendar time) {
        return DATE_FORMAT.format(time.getTime());
    }

    protected int getTotalHours() {
        return Integer.parseInt(ctx.getSharedPreferences().getString("total_hours", "40"));
    }

    public Period getPeriod() {
        return Period.valueOf(ctx.getSharedPreferences().getString("period", "week").toUpperCase());
    }

}
