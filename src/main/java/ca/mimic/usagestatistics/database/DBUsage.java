package ca.mimic.usagestatistics.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBUsage extends SQLiteOpenHelper {

    private static final String TABLE_USAGE = "usage";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_CLASSNAME = "classname";
    private static final String COLUMN_PACKAGENAME = "packagename";
    private static final String COLUMN_LAUNCHES = "launches";
    private static final String COLUMN_TIME_USED = "time_used";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_DAY_USED = "dayused";

    private static final String DATABASE_NAME = "usage.db";
    private static final int DATABASE_VERSION = 7;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_USAGE + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_NAME
            + " text not null, " + COLUMN_CLASSNAME
            + " text not null, " + COLUMN_PACKAGENAME
            + " text not null, " + COLUMN_DAY_USED
            + " text not null, " + COLUMN_TIME_USED
            + " integer not null default 0, " + COLUMN_LAUNCHES
            + " integer not null default 1, " + COLUMN_TIMESTAMP
            + " datetime not null default current_timestamp);";

    public DBUsage(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(Tasks.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USAGE);
        onCreate(db);
    }
    public void QueryData(String sql){
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }
    public Cursor GetData(String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql,null);
    }


}
