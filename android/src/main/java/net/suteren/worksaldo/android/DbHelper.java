package net.suteren.worksaldo.android;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final String TIME_ENTRIES_TABLE_CREATE = "CREATE TABLE " + TIME_ENTRY + " (" +
            "id INTEGER," +
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
            ")";

    public static final String CLIENT_TABLE = "CLIENT";
    private static final String CLIENTS_TABLE_CREATE = "CREATE TABLE " + CLIENT_TABLE + " (" +
            "id INTEGER," +
            "name TEXT," +
            "default_hourly INTEGER," +
            "currency TEXT," +
            "wid INTEGER," +
            "notes TEXT" +
            ")";
    private static final String INSERT_FAKE_DATA = "insert into TIME_ENTRY (id, description, wid, pid, tid, billable," +
            " start, stop, duration, created_with, tags, duronly) values (%d, 'Desc', 1, 1, 1, 1, '%s', '%s', %d, " +
            "'me', '', 0);";

    public DbHelper(Context context) throws PackageManager.NameNotFoundException {
        super(context, "main", null,
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.beginTransaction();
        db.compileStatement(TIME_ENTRIES_TABLE_CREATE).execute();
        db.compileStatement(CLIENTS_TABLE_CREATE).execute();
        db.compileStatement(String.format(INSERT_FAKE_DATA, 1, "2016-02-25 19:23:00", "2016-02-25 19:50:00", 27 * 60))
                .execute();
        db.compileStatement(String.format(INSERT_FAKE_DATA, 2, "2016-02-25 09:23:00", "2016-02-25 11:50:00", 2 * 3600
                + 27 * 60))
                .execute();
        db.endTransaction();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
