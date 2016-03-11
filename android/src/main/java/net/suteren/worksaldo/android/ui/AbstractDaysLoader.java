package net.suteren.worksaldo.android.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import net.suteren.worksaldo.Period;
import net.suteren.worksaldo.android.provider.TogglCachedProvider;
import org.joda.time.LocalDate;

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
        LocalDate d = LocalDate.now();
        String start = formatDate(getPeriod().from(d));
        String stop = formatDate(getPeriod().to(d));
        Log.d("DashboardFragment", String.format("start: %s, stop: %s", start, stop));
        return new CursorLoader(ctx.getContext(), TIMEENTRIES_URI,
                new String[]{DATE_COMPOSITE, DAY_START_COMPOSITE, DAY_END_COMPOSITE, DAY_TOTAL_COMPOSITE,
                        DAY_SALDO_COMPOSITE},
                SELECT_WHERE,
                new String[]{start, stop},
                ORDER_BY);
    }

    private static String formatDate(LocalDate time) {
        return DATE_FORMAT.print(time);
    }

    protected int getTotalHours() {
        return Integer.parseInt(ctx.getSharedPreferences().getString("total_hours", "40"));
    }

    public Period getPeriod() {
        return Period.valueOf(ctx.getSharedPreferences().getString("period", "week").toUpperCase());
    }

    public int getPause() {
        return Integer.parseInt(ctx.getSharedPreferences().getString("pause", "20").toUpperCase());
    }

}
