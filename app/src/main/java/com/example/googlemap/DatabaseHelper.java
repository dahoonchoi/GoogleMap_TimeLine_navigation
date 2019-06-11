package com.example.googlemap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "user1.db";
    public static final String TABLE_NAME = "users_data"; //위도, 경도, 데이터값
    public static final String TABLE_NAME1 = "users_data1"; //
    public static final String COL1 = "ID";
    public static final String COL2 = "FIRSTNAME";
    public static final String COL3 = "LASTNAME";
    public static final String COL4 = "FAVFOOD";
    public static final String COL5 = "PATHNUM";
    public static final String COL6 = "TIMENUM";
    public static final String COL7 = "TIMENUM1";


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                " FIRSTNAME TEXT, LASTNAME TEXT, FAVFOOD TEXT, PATHNUM TEXT )";
        db.execSQL(createTable);
        String createTable12 = "CREATE table " + TABLE_NAME1 + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                " TIMENUM TEXT, TIMENUM1 TEXT )";
        db.execSQL(createTable12);
        Log.d("DB", "생성완료");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME1);
        onCreate(db);
    }

    public boolean addData(String fName, String lName, String fFood, String pnum) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL2, fName);
        contentValues.put(COL3, lName);
        contentValues.put(COL4, fFood);
        contentValues.put(COL5, pnum);


        long result = db.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        } else {
            return true;
        }
    }

    public boolean addData1(String pnum, String pnum1) {
        SQLiteDatabase db4 = this.getWritableDatabase();
        ContentValues contentValues3 = new ContentValues();
        contentValues3.put(COL6, pnum);
        contentValues3.put(COL7, pnum1);



        long result = db4.insert(TABLE_NAME1, null, contentValues3);

        if(result == -1){
            return false;
        } else {
            return true;
        }

    }

    public Cursor getListContents3(String Disnum) {
        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME , null);
//        Cursor data = db.rawQuery("SELECT * FROM users_data where PATHNUM='1';", null);
        Cursor data = db.rawQuery("SELECT * FROM users_data where PATHNUM='"+Disnum+"' ORDER BY ID DESC;", null);
//        Cursor data = db.rawQuery("SELECT * FROM users_data where PATHNUM='"+Disnum+"';", null);
        return data;
    }

    public Cursor getListContents2(String Disnum) {
        SQLiteDatabase db = this.getWritableDatabase();
//        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME , null);
//        Cursor data = db.rawQuery("SELECT * FROM users_data where PATHNUM='1';", null);
        Cursor data = db.rawQuery("SELECT * FROM users_data where PATHNUM='"+Disnum+"';", null);
        return data;
    }



    public Cursor getListContents1() {
        SQLiteDatabase db1 = this.getWritableDatabase();
//        Cursor data = db.rawQuery("SELECT * FROM " + TABLE_NAME + "WHERE PATHNUM = 1", null);
        Cursor data = db1.rawQuery("SELECT * FROM " + TABLE_NAME1 , null);
//        Cursor data = db.rawQuery("SELECT * FROM users_data where PATHNUM='"+pnum+"';", null);
        return data;
    }
}
