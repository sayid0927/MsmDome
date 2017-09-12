package com.example.sayid.myapplication.common.db2;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public abstract class DBHelper extends SQLiteOpenHelper {
    public final static int DB_VERSION = 1;
    public final static String DB_NAME = "smspay.db";
    private static SQLiteDatabase db;

    public DBHelper(Context paramContext) {
        this(paramContext, DB_NAME, null, DB_VERSION);
    }

    public DBHelper(Context paramContext, String paramString, SQLiteDatabase.CursorFactory paramCursorFactory, int paramInt) {
        super(paramContext, paramString, paramCursorFactory, paramInt);
    }

    public void onCreate(SQLiteDatabase paramSQLiteDatabase) {
        paramSQLiteDatabase.execSQL(ReportDao.createTable());
        paramSQLiteDatabase.execSQL(BlockDao.createTable());
        //paramSQLiteDatabase.equals(ChannelsDao.createTable());

    }

    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2) {
    }

    protected void execSQL(String paramString) {
        getDB().execSQL(paramString);

        db.close();
    }

    protected void execSQL(String paramString, Object[] paramArrayOfObject) {
        getDB().execSQL(paramString, paramArrayOfObject);

        db.close();
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> getList(String paramString, String[] paramArrayOfString, Class<T> paramClass) {
        ArrayList<T> localArrayList = new ArrayList<T>();

        Cursor localCursor = getDB().rawQuery(paramString, paramArrayOfString);
        while (localCursor.moveToNext()) {
            localArrayList.add((T) get(localCursor));
        }
        localCursor.close();
        db.close();

        return localArrayList;
    }

    protected SQLiteDatabase getDB() {
        if ((db == null) || (!db.isOpen())) {
            db = getWritableDatabase();
        }
        return db;
    }

    protected abstract Object get(Cursor paramCursor);
}