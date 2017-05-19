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
 * Altered by Michelle on 27.04.2017.
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

    // Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_CHILD = "cname";
    private static final String KEY_UID = "uid";
    private static final String KEY_CID = "cid";
    private static final String KEY_CREATED_AT = "created_at";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String  CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USERNAME + " TEXT,"
                + KEY_CHILD + " TEXT," + KEY_UID + " TEXT," + KEY_CID + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String uid, String username,  String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username); // UserName
        values.put(KEY_CHILD," "); // Child not selected by default
        values.put(KEY_UID, uid); // Userid
        values.put(KEY_CID," "); // Childid
        values.put(KEY_CREATED_AT, created_at); // Created At

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Setting User ID in database
     * */
    public String setUser(String user) {
        String selectQuery = "UPDATE user SET username='"+user+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(selectQuery);
        Log.d(TAG, "Setting username from Sqlite: " + user);
        return user;
    }

    /**
     * Setting User ID in database
     * */
    public String setUserID(String userID) {
        String selectQuery = "UPDATE user SET uid='"+userID+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(selectQuery);
        Log.d(TAG, "Setting uid from Sqlite: " + userID);
        return userID;
    }


    /**
     * Getting User ID from database
     * */
    public String getUserID() {
        String userID = "";
        String selectQuery = "SELECT uid FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            userID = cursor.getString(0);
            cursor.close();
        }
        Log.d(TAG, "Fetching userID from Sqlite: " + userID);
        return userID;
    }

    /**
     * Setting Child Name from database
     * */
    public void setChild(String child) {
        String selectQuery = "UPDATE user SET cname='"+child+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(selectQuery);
        Log.d(TAG, "Setting child from Sqlite: " + child);
    }

    /**
     * Getting Child Name from database
     * */
    public String getChild() {
        String childName = "";
        String selectQuery = "SELECT cname FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            childName = cursor.getString(0);
            cursor.close();
        }
        Log.d(TAG, "Fetching child from Sqlite: " + childName);
        return childName;
    }

    /**
     * Setting childID in database
     * */
    public void setChildID(String child) {
        String selectQuery = "UPDATE user SET cid='"+child+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL(selectQuery);
        Log.d(TAG, "Setting childID from Sqlite: " + child);
    }

    /**
     * Getting childID from SQLite database
     * */
    public String getChildID() {
        String childID = "";
        String selectQuery = "SELECT cid FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null) {
            cursor.moveToFirst();
            childID = cursor.getString(0);
            cursor.close();
        }
        Log.d(TAG, "Fetching childID from Sqlite: " + childID);
        return childID;
    }

    /**
     * Getting user data from database
     *
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            //user.put("cname", cursor.getString(2));
            //user.put("email", cursor.getString(3));
            user.put("uid", cursor.getString(2));
            user.put("created_at", cursor.getString(3));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }**/

    /**
     * Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }
}
