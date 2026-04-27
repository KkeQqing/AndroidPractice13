package com.example.androidpractice13;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyContentProvider extends ContentProvider {

    private MyDbHelper dbHelper;
    // UriMatcher用于匹配不同的URI请求
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int USER_DIR = 1; // 匹配多条数据
    private static final int USER_ITEM = 2; // 匹配单条数据

    static {
        // 添加匹配规则：content://com.example.androidpractice13.provider/user
        uriMatcher.addURI("com.example.androidpractice13.provider", "user", USER_DIR);
        // 添加匹配规则：content://com.example.androidpractice13.provider/user/#
        uriMatcher.addURI("com.example.androidpractice13.provider", "user/#", USER_ITEM);
    }

    @Override
    public boolean onCreate() {
        dbHelper = new MyDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case USER_DIR:
                // 查询所有
                cursor = db.query(MyDbHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case USER_ITEM:
                // 查询单条，根据ID
                String id = uri.getPathSegments().get(1);
                cursor = db.query(MyDbHelper.TABLE_NAME, projection, "_id=?", new String[]{id}, null, null, sortOrder);
                break;
            default:
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case USER_DIR:
                return "vnd.android.cursor.dir/vnd.com.example.androidpractice13.provider.user";
            case USER_ITEM:
                return "vnd.android.cursor.item/vnd.com.example.androidpractice13.provider.user";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri uriReturn = null;
        switch (uriMatcher.match(uri)) {
            case USER_DIR:
            case USER_ITEM:
                long newId = db.insert(MyDbHelper.TABLE_NAME, null, values);
                uriReturn = Uri.parse("content://com.example.androidpractice13.provider/user/" + newId);
                break;
        }
        return uriReturn;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // 类似实现 delete 逻辑
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // 类似实现 update 逻辑
        return 0;
    }
}