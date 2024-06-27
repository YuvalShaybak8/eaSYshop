package com.example.easyshop.Services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.easyshop.Model.CommentModel;
import com.example.easyshop.Model.PostModel;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class PostDao extends SQLiteOpenHelper {

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

    public PostDao(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("PostDao", "Creating table: " + CREATE_TABLE);
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    public void insertPost(PostModel post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, post.getPostID());
        values.put(COLUMN_TITLE, post.getTitle());
        values.put(COLUMN_DESCRIPTION, post.getDescription());
        values.put(COLUMN_IMAGE, post.getImage());
        values.put(COLUMN_PRICE, post.getPrice());
        values.put(COLUMN_LOCATION, post.getLocation());
        values.put(COLUMN_OWNER_ID, post.getOwnerID());
        values.put(COLUMN_TIMESTAMP, post.getTimestamp().toDate().getTime());
        values.put(COLUMN_IS_PURCHASED, post.isPurchased() ? 1 : 0);
        values.put(COLUMN_BUYER_ID, post.getBuyerID());
        values.put(COLUMN_COMMENTS, commentsToString(post.getComments()));

        long result = db.insert(TABLE_POSTS, null, values);
        Log.d("PostDao", "Inserted post with ID: " + post.getPostID() + ", Result: " + result);
        db.close();
    }

    public List<PostModel> getAllPosts() {
        List<PostModel> posts = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_POSTS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                List<CommentModel> commentsList = stringToComments(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS)));

                // Ensure the timestamp is within a valid range
                long timestampSeconds = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));
                long timestampMillis = timestampSeconds * 1000;
                if (timestampMillis < -62135596800000L || timestampMillis > 253402300799000L) {
                    timestampMillis = System.currentTimeMillis();
                }
                com.google.firebase.Timestamp timestamp = new com.google.firebase.Timestamp(timestampMillis / 1000, 0);

                PostModel post = new PostModel(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OWNER_ID)),
                        timestamp,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_PURCHASED)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUYER_ID)),
                        commentsList
                );
                posts.add(post);
            } while (cursor.moveToNext());
        } else {
            Log.d("PostDao", "No posts found in the database.");
        }
        cursor.close();
        db.close();
        return posts;
    }

    public void deletePost(PostModel post) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POSTS, COLUMN_ID + " = ?", new String[]{post.getPostID()});
        db.close();
    }

    private String commentsToString(List<CommentModel> comments) {
        StringBuilder commentsString = new StringBuilder();
        for (CommentModel comment : comments) {
            commentsString.append(commentToString(comment)).append(",");
        }
        return commentsString.length() > 0 ? commentsString.substring(0, commentsString.length() - 1) : "";
    }

    private String commentToString(CommentModel comment) {
        return comment.getUserID() + "|" + comment.getCommentText();
    }

    private List<CommentModel> stringToComments(String commentsString) {
        List<CommentModel> comments = new ArrayList<>();
        if (commentsString != null && !commentsString.isEmpty()) {
            String[] commentArray = commentsString.split(",");
            for (String comment : commentArray) {
                String[] parts = comment.split("\\|");
                if (parts.length == 2) {
                    comments.add(new CommentModel(parts[1], parts[0], ""));
                }
            }
        }
        return comments;
    }

    public void deleteAllPosts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_POSTS);
        db.close();
    }
}
