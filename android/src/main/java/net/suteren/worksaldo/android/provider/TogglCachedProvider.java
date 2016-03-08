package net.suteren.worksaldo.android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import ch.simas.jtoggl.JToggl;
import ch.simas.jtoggl.domain.TimeEntry;
import net.suteren.worksaldo.android.DbHelper;
import net.suteren.worksaldo.android.ui.ISharedPreferencesProvider;

import javax.ws.rs.client.Client;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;
import static net.suteren.worksaldo.android.DbHelper.*;
import static net.suteren.worksaldo.android.ui.MainActivity.*;

/**
 * Created by vranikp on 24.2.16.
 *
 * @author vranikp
 */
public class TogglCachedProvider extends ContentProvider implements ISharedPreferencesProvider {

    public static final String ORDER_BY = "datetime(" + START_COL + ")";
    public static final String GROUP_BY = "date(" + START_COL + ")";
    public static final String SELECT_WHERE = "date(" + START_COL + ") >= date(?) and date(" + START_COL + ") <= date" +
            "(?)";
    public static final String DELETE_WHERE = "date(" + START_COL + ") >= date(?) and date(" + START_COL + ") <= date" +
            "(?)";

    public static final String DAY_START_NAME = "start";
    public static final String DATE_NAME = "date";
    public static final String DAY_END_NAME = "stop";
    public static final String DAY_TOTAL_NAME = "total";
    public static final String DAY_SALDO_NAME = "saldo";

    public static final String DATE_COMPOSITE = "date(start) " + DATE_NAME;
    public static final String DAY_START_COMPOSITE = "min(time(" + START_COL + ", 'localtime')) " + DAY_START_NAME;
    public static final String DAY_END_COMPOSITE = "max(time(" + STOP_COL + ", 'localtime')) " + DAY_END_NAME;
    public static final String DAY_TOTAL_COMPOSITE = "sum(" + DURATION_COL + ") " + DAY_TOTAL_NAME;
    public static final String DAY_SALDO_COMPOSITE = "sum(" + DURATION_COL + ") " + DAY_SALDO_NAME;
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(0);

    public static final String URI_BASE = "net.suteren.toggl.provider";
    public static final String USER_PATH = "user";
    public static final String TIMEENTRY_PATH = "timeentry";

    public static final String TIMESHEET_URI = URI_BASE + "/" + TIMEENTRY_PATH;
    public static final String API_KEY = "api_token";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final int UNAUTHORIZED = 401;
    public static final String RESULT_CODE = "resultCode";

    {
        sUriMatcher.addURI(URI_BASE, USER_PATH, 1);
        sUriMatcher.addURI(URI_BASE, TIMEENTRY_PATH, 2);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        boolean instant = Boolean.parseBoolean(uri.getQueryParameter(INSTANT));
        Bundle report = null;
        if (!instant) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(MAIN, MODE_PRIVATE);
            String key = sharedPreferences.getString(API_KEY, null);
            if (key == null || "".equals(key.trim())) {
                report = new Bundle();
                report.putInt(RESULT_CODE, UNAUTHORIZED);
            } else {
                Log.d("TogglCachedProvider", String.format("KEY: %s", key));
                try {
                    Log.d("TogglCachedProvider", "Loading from toggl for api key: " + key);
                    JToggl jt = new JToggl(key, API_KEY) {
                        @Override
                        protected Client prepareClient() {
                            return super.prepareClient().register(AndroidFriendlyFeature.class);
                        }
                    };
                    Calendar start = Calendar.getInstance();
                    Calendar stop = Calendar.getInstance();
                    start.setTime(DATE_FORMAT.parse(selectionArgs[0]));
                    stop.setTime(DATE_FORMAT.parse(selectionArgs[1]));
                    Log.d("TogglCachedProvider", String.format("Get TEs from %s to %s",
                            DATE_TIME_FORMAT.format(start.getTime()), DATE_TIME_FORMAT.format(stop.getTime())));
                    List<TimeEntry> te = jt.getTimeEntries(start, stop);
                    Log.d("TogglCachedProvider", "Loaded from toggl: " + te.size());
                    SQLiteDatabase db = getDbHelper(getContext()).getWritableDatabase();

                    db.delete(TIME_ENTRY, DELETE_WHERE, selectionArgs);
                    for (TimeEntry e : te) {
                        Log.d("TogglCachedProvider", String.format("Persisting: %s", te));
                        ContentValues values = new ContentValues(12);
                        values.put(DbHelper.DESCRIPTION_COL, e.getDescription() == null ? "" : e.getDescription());
                        values.put(DbHelper.WID_COL, e.getWorkspaceId());
                        values.put(DbHelper.PID_COL, e.getProjectId());
                        values.put(DbHelper.TID_COL, e.getTaskId());

                        final SimpleDateFormat dateTimeFormat = DATE_TIME_FORMAT;
                        final Calendar teStart = e.getStart();
                        dateTimeFormat.setTimeZone(teStart.getTimeZone());
                        values.put(DbHelper.START_COL, dateTimeFormat.format(teStart.getTime()));

                        Calendar teStop = e.getStop();
                        if (teStop == null) {
                            teStop = Calendar.getInstance();
                            teStop.setTimeZone(TimeZone.getTimeZone("GMT"));
                        }
                        dateTimeFormat.setTimeZone(teStop.getTimeZone());
                        values.put(DbHelper.STOP_COL, dateTimeFormat.format(teStop.getTime()));

                        values.put(DbHelper.DURATION_COL, e.getDuration());
                        //values.put(DbHelper.BILLABLE_COL, null);
                        values.put(DbHelper.CREATED_WITH_COL, e.getCreatedWith());
                        if (e.getTags() != null)
                            values.put(DbHelper.TAGS_COL, TextUtils.join(";", e.getTags()));
                        values.put(DbHelper.DURONLY_COL, e.getDurationOnly());
                        long id = db.insertWithOnConflict(TIME_ENTRY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                        Log.d("TogglCachedProvider", String.format("Inserted timeentry with id: %d", id));
                    }
                    db.close();
                } catch (Exception e) {
                    Log.e("TogglCachedProvider", "unable to get time entries", e);
                }
            }

        }
        List<String> proj = Arrays.asList(projection);
        if (!proj.contains(ID_COLUMN_NAME)) {
            proj = new ArrayList<>(Arrays.asList(projection));
            proj.add(0, ID_COLUMN_NAME);
            projection = proj.toArray(new String[proj.size()]);
        }
        try {
            switch (sUriMatcher.match(uri)) {

                // If the incoming URI was for user
                case 1:

                    return null;

                // If the incoming URI was for time sheet entries
                case 2:
                    SQLiteDatabase readableDatabase = getDbHelper(getContext()).getReadableDatabase();
                    Cursor cursor = readableDatabase
                            .query(TIME_ENTRY, projection, selection, selectionArgs, GROUP_BY, null, sortOrder);
                    return cursor;

                default:
                    // If the URI is not recognized, you should do some error handling here.
                    return null;

            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TogglCachedProvider", "unable to get time entries", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }


    @Override
    public SharedPreferences getSharedPreferences() {
        return getContext().getSharedPreferences(MAIN, MODE_PRIVATE);
    }
}
