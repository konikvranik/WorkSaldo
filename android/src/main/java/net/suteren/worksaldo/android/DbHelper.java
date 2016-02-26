package net.suteren.worksaldo.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by vranikp on 24.2.16.
 *
 * @author vranikp
 */
public class DbHelper extends SQLiteOpenHelper {

    public static final String TIME_ENTRY = "TIME_ENTRY";
    public static final String START_COL = "start";
    public static final String STOP_COL = "stop";
    public static final String DURATION_COL = "duration";
    public static final String ID_COLUMN_NAME = "ROWID _id";
    private static final String TIME_ENTRIES_TABLE_CREATE = "CREATE TABLE " + TIME_ENTRY + " (" +
            "description TEXT," +
            "wid INTEGER," +
            "pid INTEGER," +
            "tid INTEGER," +
            "billable INTEGER," +
            START_COL + " TEXT," +
            STOP_COL + " TEXT," +
            DURATION_COL + " INTEGER," +
            "created_with TEXT, " +
            "tags TEXT," +
            "duronly INTEGER," +
            "at TEXT" +
            ");";

    public static final String CLIENT_TABLE = "CLIENT";
    private static final String CLIENTS_TABLE_CREATE = "CREATE TABLE " + CLIENT_TABLE + " (" +
            "name TEXT," +
            "default_hourly INTEGER," +
            "currency TEXT," +
            "wid INTEGER," +
            "notes TEXT" +
            ");";
    private static final String INSERT_FAKE_DATA = "insert into TIME_ENTRY ( description, wid, pid, tid, billable," +
            " start, stop, duration, created_with, tags, duronly) values ( 'Desc', 1, 1, 1, 1, '%s', '%s', %d, " +
            "'me', '', 0);";
    private static DbHelper dbHelper;

    private DbHelper(Context context) throws PackageManager.NameNotFoundException {
        super(context, "main", null,
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
    }

    synchronized static DbHelper getDbHelper(Context context) throws PackageManager.NameNotFoundException {
        if (dbHelper == null) {
            dbHelper = new DbHelper(context);
            addTodayRecords(dbHelper.getWritableDatabase());
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.compileStatement(TIME_ENTRIES_TABLE_CREATE).execute();
        db.compileStatement(CLIENTS_TABLE_CREATE).execute();
        db.compileStatement(String.format(INSERT_FAKE_DATA, "2016-02-25 19:23:00", "2016-02-25 19:50:00", 27 * 60))
                .execute();
        db.compileStatement(String.format(INSERT_FAKE_DATA, "2016-02-25 09:23:00", "2016-02-25 11:50:00",
                2 * 3600 + 27 * 60)).execute();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);


    }

    private static void addTodayRecords(SQLiteDatabase db) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        db.compileStatement(String.format(INSERT_FAKE_DATA,
                today + " 19:23:00", today + " 19:50:00", 27 * 60)).execute();
        db.compileStatement(String.format(INSERT_FAKE_DATA,
                today + " 09:23:00", today + " 11:50:00", 2 * 3600 + 27 * 60)).execute();
    }
}
