package net.suteren.worksaldo.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by vranikp on 24.2.16.
 *
 * @author vranikp
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String TIME_ENTRY = "TIME_ENTRY";
    public static final String START_COL = "start";
    public static final String STOP_COL = "stop";
    public static final String AT_COL = "at";
    public static final String DURATION_COL = "duration";
    public static final String ID_COLUMN_NAME = "ROWID _id";
    public static final String DESCRIPTION_COL = "description";
    public static final String WID_COL = "wid";
    public static final String PID_COL = "pid";
    public static final String TID_COL = "tid";
    public static final String BILLABLE_COL = "billable";
    public static final String CREATED_WITH_COL = "created_with";
    public static final String TAGS_COL = "tags";
    public static final String DURONLY_COL = "duronly";
    public static final String INTEGER_TYPE = "INTEGER";
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

    public static final String CLIENT_TABLE = "CLIENT";
    private static final String CLIENTS_TABLE_CREATE = "CREATE TABLE " + CLIENT_TABLE + " (" +
            "name " + TEXT_TYPE + "," +
            "default_hourly " + INTEGER_TYPE + "," +
            "currency " + TEXT_TYPE + "," +
            WID_COL + " " + INTEGER_TYPE + "," +
            "notes " + TEXT_TYPE +
            ");";
    private static final String INSERT_FAKE_DATA = "insert into TIME_ENTRY ( " + DESCRIPTION_COL + ", " + WID_COL +
            ", " + PID_COL + ", " + TID_COL + ", " +
            BILLABLE_COL + "," +
            " start, stop, duration, " + CREATED_WITH_COL + ", " + TAGS_COL + ", " + DURONLY_COL + ") values ( " +
            "'Desc', 1, 1, 1, " +
            "1, '%s', '%s', %d, " +
            "'me', '', 0);";
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
