package com.example.easyshop.Services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "posts.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_POSTS = "posts";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_PRICE = "price";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_OWNER_ID = "owner_id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_IS_PURCHASED = "is_purchased";
    public static final String COLUMN_BUYER_ID = "buyer_id";
    public static final String COLUMN_COMMENTS = "comments";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_POSTS + " ("
            + COLUMN_ID + " TEXT PRIMARY KEY, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_DESCRIPTION + " TEXT, "
            + COLUMN_IMAGE + " TEXT, "
            + COLUMN_PRICE + " REAL, "
            + COLUMN_LOCATION + " TEXT, "
            + COLUMN_OWNER_ID + " TEXT, "
            + COLUMN_TIMESTAMP + " INTEGER, "
            + COLUMN_IS_PURCHASED + " INTEGER, "
            + COLUMN_BUYER_ID + " TEXT, "
            + COLUMN_COMMENTS + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }
}
