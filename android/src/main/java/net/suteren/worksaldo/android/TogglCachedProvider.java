package net.suteren.worksaldo.android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import ch.simas.jtoggl.JToggl;
import ch.simas.jtoggl.TimeEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static net.suteren.worksaldo.android.DbHelper.*;
import static net.suteren.worksaldo.android.MainActivity.DashboardFragment.INSTANT;
import static net.suteren.worksaldo.android.MainActivity.MAIN;

/**
 * Created by vranikp on 24.2.16.
 *
 * @author vranikp
 */
public class TogglCachedProvider extends ContentProvider {

    public static final String ORDER_BY = "datetime(" + START_COL + ")";
    public static final String GROUP_BY = "date(" + START_COL + ")";
    public static final String WHERE = "date(" + START_COL + ") >= date(?) and date(" + START_COL + ") <= date(?)";

    public static final String DAY_START_NAME = "start";
    public static final String DATE_NAME = "date";
    public static final String DAY_END_NAME = "stop";
    public static final String DAY_TOTAL_NAME = "total";

    public static final String DATE_COMPOSITE = "date(start) " + DATE_NAME;
    public static final String DAY_START_COMPOSITE = "min(time(" + START_COL + ")) " + DAY_START_NAME;
    public static final String DAY_END_COMPOSITE = "max(time(" + STOP_COL + ")) " + DAY_END_NAME;
    public static final String DAY_TOTAL_COMPOSITE = "sum(" + DURATION_COL + ") " + DAY_TOTAL_NAME;
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(0);

    public static final String URI_BASE = "net.suteren.toggl.provider";
    public static final String USER_PATH = "user";
    public static final String TIMEENTRY_PATH = "timeentry";

    public static final String TIMESHEET_URI = URI_BASE + "/" + TIMEENTRY_PATH;
    public static final String API_KEY = "api_key";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        if (!instant) {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(MAIN, MODE_PRIVATE);
            String key = sharedPreferences.getString(API_KEY, null);
            try {
                Log.d(this.getClass().getName(), "Loading from toggl for api key: " + key);
                JToggl jt = new JToggl(key, API_KEY);
                List<TimeEntry> te = jt.getTimeEntries(startDate(), endDate());
                Log.d(this.getClass().getName(), "Loaded from toggl: " + te.size());
                SQLiteDatabase db = getDbHelper(getContext()).getWritableDatabase();
                for (TimeEntry e : te) {
                    ContentValues values = new ContentValues(12);
                    values.put(DbHelper.DESCRIPTION_COL, e.getDescription());
                    values.put(DbHelper.WID_COL, e.getWid());
                    values.put(DbHelper.PID_COL, e.getPid());
                    values.put(DbHelper.TID_COL, e.getTid());
                    values.put(DbHelper.START_COL, DATE_TIME_FORMAT.format(e.getStart()));
                    values.put(DbHelper.STOP_COL, DATE_TIME_FORMAT.format(e.getStop()));
                    values.put(DbHelper.DURATION_COL, e.getDuration());
                    //values.put(DbHelper.BILLABLE_COL, null);
                    values.put(DbHelper.CREATED_WITH_COL, e.getCreated_with());
                    values.put(DbHelper.TAGS_COL, TextUtils.join(";", e.getTag_names()));
                    values.put(DbHelper.DURONLY_COL, e.getDuronly());
                    db.insertWithOnConflict(TIME_ENTRY, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
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

    public static Date startDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -c.get(Calendar.DAY_OF_WEEK));
        return c.getTime();
    }

    public static Date endDate() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, 7 - c.get(Calendar.DAY_OF_WEEK));
        return c.getTime();
    }

}
