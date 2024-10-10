package com.unipi.ppapakostas.braketracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


/**
 * Database class for managing the braking point history table.
 * This class is responsible for creating, upgrading, and interacting
 * with the SQLite database.
 */
public class Database extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "brTrack.db";

    private static final String TABLE_NAME = "history_table";

    public static final String ID =         "id";
    public static final String LONGITUDE =  "lon";
    public static final String LATITUDE =   "lat";
    public static final String TIMESTAMP =  "time";
    public static final String ACCELERATION =  "acceleration";


    private static Database databaseInstance;

    /**
     * Private constructor to prevent direct instantiation.
     * Use the {@link #getInstance(Context)} method to get the singleton instance.
     *
     * @param context The application context
     */
    public Database(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    /**
     * Retrieves the singleton instance of the Database class.
     *
     * @param context The application context
     * @return The singleton instance of the Database class
     */
    public static synchronized Database getInstance(Context context) {
        if (databaseInstance == null) {
            databaseInstance = new Database(context.getApplicationContext());
        }
        return databaseInstance;
    }

    private void createTable(SQLiteDatabase db){
        db.execSQL(" CREATE TABLE " + TABLE_NAME + " (" +
                ID +          " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                LONGITUDE +   " TEXT, " +
                LATITUDE +    " TEXT, " +
                TIMESTAMP +   " TEXT, " +
                ACCELERATION +   " TEXT );"
        );
    }


    /**
     * Called when the database is created for the first time.
     * This method will create the history table.
     *
     * @param sqLiteDatabase The database instance
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTable(sqLiteDatabase);
    }

    /**
     * Called when the database needs to be upgraded. This method will drop the existing
     * table and recreate it.
     *
     * @param sqLiteDatabase The database instance
     * @param oldVersion     The old database version
     * @param newVersion     The new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME+ ";");
        onCreate(sqLiteDatabase); // Recreate table after dropping it
    }

    public long insert(double lon, double lat, String timeStamp, float acceleration){
        ContentValues contentValues = new ContentValues();

        contentValues.put(LONGITUDE, String.valueOf(lon));
        contentValues.put(LATITUDE, String.valueOf(lat));
        contentValues.put(TIMESTAMP, timeStamp);
        contentValues.put(ACCELERATION, String.valueOf(acceleration));

        return getDatabaseInstance().insert(TABLE_NAME, null, contentValues);
    }

    public Cursor getAll(){
        return getDatabaseInstance().rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }

    public SQLiteDatabase getDatabaseInstance(){
        return this.getWritableDatabase();
    }

}
