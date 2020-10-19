package com.aman.applocker;

public class Constants {
    //for table attributes..
    static final String ROW_ID="id";
    static final String NAME = "name";
    static final String PASS = "position";

    //for database creation and table name..
    static final String DB_NAME="PASS_DB";
    static final String TB_NAME="PASS_TB";
    static final int DB_VERSION='1';

    static final String CREATE_TB = "CREATE TABLE " + TB_NAME + "(" + ROW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + NAME + " TEXT NOT NULL," + PASS + " TEXT NOT NULL);";
}
