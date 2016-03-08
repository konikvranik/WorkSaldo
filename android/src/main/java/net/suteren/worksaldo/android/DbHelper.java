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
        super(context, "main", null,
                context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
    }

    public synchronized static DbHelper getDbHelper(Context context) throws PackageManager.NameNotFoundException {
        if (dbHelper == null) {
            dbHelper = new DbHelper(context);
            //    addTodayRecords(dbHelper.getWritableDatabase());
        }
        return dbHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.compileStatement(TIME_ENTRIES_TABLE_CREATE).execute();
        db.compileStatement(CLIENTS_TABLE_CREATE).execute();
      /* db.compileStatement(String.format(INSERT_FAKE_DATA, "2016-02-25 19:23:00", "2016-02-25 19:50:00", 27 * 60))
                .execute();
        db.compileStatement(String.format(INSERT_FAKE_DATA, "2016-02-25 09:23:00", "2016-02-25 11:50:00",
                2 * 3600 + 27 * 60)).execute();*/
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
