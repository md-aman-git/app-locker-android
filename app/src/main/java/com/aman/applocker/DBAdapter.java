package com.aman.applocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {
    Context mContext;
    SQLiteDatabase db;
    DBHelper dbHelper;

    public DBAdapter(Context mContext) {
        this.mContext = mContext;
        dbHelper = new DBHelper(mContext);
    }
    //open database..
    public DBAdapter openDB()
    {
        try{
            db = dbHelper.getWritableDatabase();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return this;
    }

    //close database ..
    public void closeDB()
    {
        try {
            dbHelper.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        finally {
            dbHelper.close();
        }

    }
    //delete specific column
    public void deleteAll()
    {
        db.execSQL("delete from "+ Constants.TB_NAME);
    }
    //reseed id
    public void resetRow()
    {
        db.execSQL("reindex "+ Constants.TB_NAME);
    }
    //insert data into database..
    public long insertData(String path, String position)
    {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.NAME, path);
            contentValues.put(Constants.PASS, position);
            return db.insert(Constants.TB_NAME, Constants.ROW_ID, contentValues);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return 0;
    }

    //retrieve data from database..
    public Cursor getPass()
    {
        String[] column = new String[]{Constants.ROW_ID, Constants.NAME, Constants.PASS};
        return db.query(Constants.TB_NAME, column, null, null,
                null, null, null);
    }

}
