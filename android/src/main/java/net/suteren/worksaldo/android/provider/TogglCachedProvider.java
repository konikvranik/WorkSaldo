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
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import javax.ws.rs.client.Client;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static net.suteren.worksaldo.android.DbHelper.*;
import static net.suteren.worksaldo.android.ui.MainActivity.MAIN;

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
    public static final Uri TIMEENTRIES_URI = new Uri.Builder().scheme("content")
            .authority(TogglCachedProvider.URI_BASE)
            .appendPath(TogglCachedProvider.TIMEENTRY_PATH).build();

    public static final String RELOAD_PATH = "reload";
    public static final Uri RELOAD_URI = new Uri.Builder().scheme("content")
            .authority(TogglCachedProvider.URI_BASE)
            .appendPath(TogglCachedProvider.RELOAD_PATH).build();

    public static final int MATCHED_RELOAD = 3;
    public static final int MATCHED_USER = 1;
    public static final int MATCHED_TIMEENTRY = 2;


    public static final String TIMESHEET_URI = URI_BASE + "/" + TIMEENTRY_PATH;
    public static final String API_KEY = "api_token";

    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd").withZoneUTC();
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss").withZoneUTC();
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS").withZoneUTC();
    public static final int UNAUTHORIZED = 401;
    public static final String RESULT_CODE = "resultCode";

    {
        sUriMatcher.addURI(URI_BASE, USER_PATH, MATCHED_USER);
        sUriMatcher.addURI(URI_BASE, TIMEENTRY_PATH, MATCHED_TIMEENTRY);
        sUriMatcher.addURI(URI_BASE, RELOAD_PATH, MATCHED_RELOAD);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("TogglCachedProvider", "Querying data for uri " + uri.toString());


        switch (sUriMatcher.match(uri)) {

            // If the incoming URI was for user
            case MATCHED_RELOAD:
                Log.d("TogglCachedProvider", "Querying Toggl");
                reloadTimeentriesFromToggl(selectionArgs);
                Log.d("TogglCachedProvider", "Notifying uri " + TIMEENTRIES_URI.toString());
                getContext().getContentResolver().notifyChange(TIMEENTRIES_URI, null);
                return null;

            // If the incoming URI was for time sheet entries
            case MATCHED_TIMEENTRY:
                Log.d("TogglCachedProvider", "Querying DB");
                final Cursor cursor = reloadTimeentiresFromDb(projection, selection, selectionArgs, sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), TIMEENTRIES_URI);
                return cursor;

            default:
                // If the URI is not recognized, you should do some error handling here.
                return null;

        }

    }

    Cursor reloadTimeentiresFromDb(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            List<String> proj = Arrays.asList(projection);
            if (!proj.contains(ID_COLUMN_NAME)) {
                proj = new ArrayList<>(Arrays.asList(projection));
                proj.add(0, ID_COLUMN_NAME);
                projection = proj.toArray(new String[proj.size()]);
            }
            SQLiteDatabase readableDatabase = getDbHelper(getContext()).getReadableDatabase();
            Cursor cursor = readableDatabase
                    .query(TIME_ENTRY, projection, selection, selectionArgs, GROUP_BY, null, sortOrder);
            return cursor;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("TogglCachedProvider", "unable to get time entries", e);
            throw new RuntimeException(e);
        }
    }

    void reloadTimeentriesFromToggl(String[] selectionArgs) {
        Bundle report;
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
                Log.d("TogglCachedProvider", String.format("Get TEs from %s to %s", selectionArgs[0], selectionArgs[1]));
                List<TimeEntry> te = jt.getTimeEntries(ISODateTimeFormat.date().parseLocalDate(selectionArgs[0]), ISODateTimeFormat.date().parseLocalDate(selectionArgs[1]));
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

                    final DateTimeFormatter dateTimeFormat = DATE_TIME_FORMAT;

                    values.put(DbHelper.START_COL, dateTimeFormat.print(e.getStart()));

                    DateTime teStop = e.getStop();
                    values.put(DbHelper.STOP_COL, dateTimeFormat.print(teStop));

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
