package com.sample.vidance.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by Danil on 27.03.2017.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "db676837084";

    // Login table name
    private static final String TABLE_USER = "user";

    // Child behaviour table name
    private static final String TABLE_BHTEST = "child_btest";

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_CNAME = "cname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_CNAME + " TEXT, "
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_BTEST_TABLE = "CREATE TABLE " + TABLE_BHTEST + "("
                + BehaviourHandler.TAG_BID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_CNAME + " TEXT, "
                + BehaviourHandler.TAG_BNAME + " TEXT," + BehaviourHandler.TAG_SEVER + " TEXT,"
                + KEY_UPDATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_BTEST_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BHTEST);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String cname, /*String email,*/ String uid, String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_CNAME, cname); // Child Name
        //values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    public void addBehaviour(String name, String cname, String bName, String role, String updated_at) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_CNAME, cname); // Child Name
        values.put(BehaviourHandler.TAG_BNAME, bName);
        values.put(BehaviourHandler.TAG_SEVER, role);
        values.put(KEY_UPDATED_AT, updated_at);

        long id = db.insert(TABLE_BHTEST, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "Behaviours inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("cname", cursor.getString(2));
            //user.put("email", cursor.getString(3));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    public HashMap<String, String> getBehaviourDetails() {
        HashMap<String, String> behaviour = new HashMap<String, String>();
        String selectQueryFromBTest = "SELECT * FROM " + TABLE_BHTEST;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQueryFromBTest, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            //behaviour.put("bc_id", cursor.getString(1));
            behaviour.put("name", cursor.getString(1));
            behaviour.put("cname", cursor.getString(2));
            behaviour.put("bName", cursor.getString(3));
            behaviour.put("severity", cursor.getString(4));
            behaviour.put("updated_at", cursor.getString(5));
        }

        cursor.close();
        db.close();
        // return behaviour
        Log.d(TAG, "Fetching btest from Sqlite: " + behaviour.toString());

        return behaviour;
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
