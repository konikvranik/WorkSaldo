package net.suteren.worksaldo.android;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.suteren.worksaldo.android.DbHelper.*;

/**
 * Created by vranikp on 24.2.16.
 *
 * @author vranikp
 */
public class TogglCachedProvider extends ContentProvider {

    public static final String ORDER_BY = "datetime(" + START_COL + ")";
    public static final String GROUP_BY = "date(" + START_COL + ")";
    public static final String WHERE = "date(" + START_COL + ") = date(?)";

    public static final String DAY_START_NAME = "start";
    public static final String DAY_END_NAME = "stop";
    public static final String DAY_TOTAL_NAME = "total";

    public static final String DAY_START_COMPOSITE = "min(time(" + START_COL + ")) " + DAY_START_NAME;
    public static final String DAY_END_COMPOSITE = "max(time(" + STOP_COL + ")) " + DAY_END_NAME;
    public static final String DAY_TOTAL_COMPOSITE = "sum(" + DURATION_COL + ") " + DAY_TOTAL_NAME;
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(0);

    public static final String URI_BASE = "net.suteren.toggl.provider";
    public static final String USER_PATH = "user";
    public static final String TIMEENTRY_PATH = "timeentry";

    public static final String TIMESHEET_URI = URI_BASE + "/" + TIMEENTRY_PATH;


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

                    break;

                // If the incoming URI was for time sheet entries
                case 2:
                    return getDbHelper(getContext()).getReadableDatabase()
                            .query(TIME_ENTRY, projection, selection, selectionArgs, GROUP_BY, null, sortOrder);

                default:
                    // If the URI is not recognized, you should do some error handling here.
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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

}
