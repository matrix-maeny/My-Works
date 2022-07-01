package com.matrix_maeny.myworks.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class WorkDataBaseHelper extends SQLiteOpenHelper {
    public WorkDataBaseHelper(@Nullable Context context) {
        super(context, "WorkDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create Table Work(name TEXT primary key,state INT,code INT)");
        db.execSQL("Create Table MainNotification(name TEXT primary key,workName TEXT,time INT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("Drop Table if exists Work");
        db.execSQL("Drop Table if exists MainNotification");
    }

    public boolean insertData(String name,int state,int code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("name",name);
        cv.put("state",state);
        cv.put("code",code);

        long result = db.insert("Work",null,cv);

        return result != -1;
    }// insertData
    public boolean insertNotification(String name, String workName,int time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("workName",workName);
        cv.put("name",name);
        cv.put("time",time);

        long result = db.insert("MainNotification",null,cv);

        return result != -1;
    }// insertData

    public boolean updateData(String name,int state){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("state",state);
        long result = db.update("Work",cv,"name=?",new String[]{name});
        return result != -1;
    }
    public boolean updateNotification(String workName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("workName",workName);
        long result = db.update("MainNotification",cv,"name=?",new String[]{"work"});
        return result != -1;
    }
    public boolean updateNotificationTime(int time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put("time",time);
        long result = db.update("MainNotification",cv,"name=?",new String[]{"work"});
        return result != -1;
    }

    public boolean deleteData(String name){
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete("Work","name=?",new String[]{name});

        return result != -1;
    }// deleteData

    public boolean deleteAll(){
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete("Work",null,null);

        return result != -1;


    }

    public Cursor getData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("Select * from Work",null);
    }
    public Cursor getNotificationData(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.rawQuery("Select * from MainNotification",null);
    }

}
