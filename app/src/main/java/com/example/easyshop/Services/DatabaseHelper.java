package com.example.easyshop.Services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "posts.db";
    private static final int DATABASE_VERSION = 2;

    private static final String CREATE_TABLE = "CREATE TABLE " + PostDao.TABLE_POSTS + " ("
            + PostDao.COLUMN_ID + " TEXT PRIMARY KEY, "
            + PostDao.COLUMN_TITLE + " TEXT, "
            + PostDao.COLUMN_DESCRIPTION + " TEXT, "
            + PostDao.COLUMN_IMAGE + " TEXT, "
            + PostDao.COLUMN_PRICE + " REAL, "
            + PostDao.COLUMN_LOCATION + " TEXT, "
            + PostDao.COLUMN_OWNER_ID + " TEXT, "
            + PostDao.COLUMN_TIMESTAMP + " INTEGER, "
            + PostDao.COLUMN_IS_PURCHASED + " INTEGER, "
            + PostDao.COLUMN_BUYER_ID + " TEXT, "
            + PostDao.COLUMN_COMMENTS + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "Creating table: " + CREATE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PostDao.TABLE_POSTS);
        onCreate(db);
    }
}
