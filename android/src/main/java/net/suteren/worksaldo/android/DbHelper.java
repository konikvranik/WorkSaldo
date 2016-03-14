package net.suteren.worksaldo.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Database helper which creates, updates database tables and provides database instance.
 *
 * @author vranikp
 */
public class DbHelper extends SQLiteOpenHelper {

    /**
     * Name of time entries table.
     */
    public static final String TIME_ENTRY = "TIME_ENTRY";
    /**
     * Name of clients table.
     */
    public static final String CLIENT_TABLE = "CLIENT";
    /**
     * Name of column with start time.
     */
    public static final String START_COL = "start";
    /**
     * Name of column with end time.
     */
    public static final String STOP_COL = "stop";
    public static final String AT_COL = "at";
    /**
     * Name of column work duration.
     */
    public static final String DURATION_COL = "duration";
    /**
     * Name of default column with row id.
     */
    public static final String ID_COLUMN_NAME = "ROWID _id";
    /**
     * Name of column with time entry description.
     */
    public static final String DESCRIPTION_COL = "description";
    /**
     * Name of column with workspace id.
     */
    public static final String WID_COL = "wid";
    /**
     * Name of column with project id.
     */
    public static final String PID_COL = "pid";
    /**
     * Name of column with task id.
     */
    public static final String TID_COL = "tid";
    /**
     * Name of column with billable flag.
     */
    public static final String BILLABLE_COL = "billable";
    /**
     * Name of column with "agent" name.
     */
    public static final String CREATED_WITH_COL = "created_with";
    /**
     * Name of column with tags.
     */
    public static final String TAGS_COL = "tags";
    /**
     * Name of column with duration only flag.
     */
    public static final String DURONLY_COL = "duronly";
    /**
     * Integer type.
     */
    public static final String INTEGER_TYPE = "INTEGER";

    /**
     * Text type.
     */
    public static final String TEXT_TYPE = "TEXT";

    private static final String TIME_ENTRIES_TABLE_CREATE = "CREATE TABLE " + TIME_ENTRY + " (" +
            DESCRIPTION_COL + " " + TEXT_TYPE + " NOT NULL," +
            WID_COL + " " + INTEGER_TYPE + " DEFAULT -1," +
            PID_COL + " " + INTEGER_TYPE + " DEFAULT -1," +
            TID_COL + " " + INTEGER_TYPE + " DEFAULT -1," +
            BILLABLE_COL + " " + INTEGER_TYPE + "," +
            START_COL + " " + TEXT_TYPE + " NOT NULL," +
            STOP_COL + " " + TEXT_TYPE + " NOT NULL," +
            DURATION_COL + " " + INTEGER_TYPE + " NOT NULL," +
            CREATED_WITH_COL + " " + TEXT_TYPE + ", " +
            TAGS_COL + " " + TEXT_TYPE + "," +
            DURONLY_COL + " " + INTEGER_TYPE + "," +
            AT_COL + " " + TEXT_TYPE + "," +
            "UNIQUE (" +
            START_COL + "," +
            STOP_COL + "," +
            DESCRIPTION_COL + "," +
            WID_COL + "," +
            TID_COL + "," +
            PID_COL + ")" +
            ");";
    private static final String CLIENTS_TABLE_CREATE = "CREATE TABLE " + CLIENT_TABLE + " (" +
            "name " + TEXT_TYPE + "," +
            "default_hourly " + INTEGER_TYPE + "," +
            "currency " + TEXT_TYPE + "," +
            WID_COL + " " + INTEGER_TYPE + "," +
            "notes " + TEXT_TYPE +
            ");";
    private static DbHelper dbHelper;

    private DbHelper(Context context) throws PackageManager.NameNotFoundException {
        super(context, "main", null, context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
    }

    public synchronized static DbHelper getDbHelper(Context context) {
        if (dbHelper == null) {
            try {
                dbHelper = new DbHelper(context);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("DbHelper", "Unable to create DB helper", e);
                throw new RuntimeException(e);
            }
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.compileStatement(TIME_ENTRIES_TABLE_CREATE).execute();
        db.compileStatement(CLIENTS_TABLE_CREATE).execute();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
