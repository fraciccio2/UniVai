package com.example.carsharing;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class HelperDataBase extends SQLiteOpenHelper {
    public static final String databaseName = "LogIn.db";

    public HelperDataBase(@Nullable Context context){
        super(context, databaseName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table users(email text primary key, password text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists users");
    }

    public Boolean insertData (String email, String password) {
        SQLiteDatabase myDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("password", password);
        long result = myDatabase.insert("users", null, contentValues);

        return result != -1;
    }

    public Boolean checkEmail (String email){
        SQLiteDatabase myDatabase = this.getWritableDatabase();
        Cursor cursor = myDatabase.rawQuery("select * from users where email = ?", new String[]{email});

        return cursor.getCount() > 0;
    }

    public Boolean checkEmailAndPassword (String email, String password) {
        SQLiteDatabase myDatabase = this.getWritableDatabase();
        Cursor cursor = myDatabase.rawQuery("select * from users where email = ? and password = ?", new String[]{email, password});

        return cursor.getCount() > 0;
    }
}
