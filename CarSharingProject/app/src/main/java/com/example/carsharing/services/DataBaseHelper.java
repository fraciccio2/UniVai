package com.example.carsharing.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Patterns;

import androidx.annotation.Nullable;

import java.util.HashMap;

public class DataBaseHelper extends SQLiteOpenHelper {

    public static final String databaseName = "LogIn.db";

    public DataBaseHelper(@Nullable Context context) {
        super(context, databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table if not exists user(email text primary key, password text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists user");
    }

    public void insertData(String email, String password) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("password", password);
        sqLiteDatabase.insert("user", null, contentValues);
    }

    public HashMap<String, String> getEmailAndPassword() {
        HashMap<String, String> map = new HashMap<>();
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("select * from user", null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            map.put("email", cursor.getString(0));
            map.put("password", cursor.getString(1));
            return map;
        }
        return null;
    }

    public void dropTable() {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.execSQL("drop table if exists user");
    }

    public Boolean isCorrectEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.contains("studium");
    }
}
