package org.monospace.smsfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "main_db";
	public static final String TABLE_FILTER = "filter";
	public static final String COL_FIL_TARGET = "target";
	public static final String COL_FIL_TYPE = "type";
	public static final String COL_FIL_RULE = "rule";
	public static final String COL_FIL_STATE = "state";
	public static final String COL_FIL_DESC = "description";
	public static final String TABLE_SMS = "sms";
	public static final String COL_SMS_SENDER = "address";
	public static final String COL_SMS_CONTENT = "message";
	public static final String COL_SMS_RECV_TIME = "receive_time";
	public static final String COL_SMS_STATE = "state";
	
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String create_filter = "CREATE TABLE "+ TABLE_FILTER +
				" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				COL_FIL_TARGET + " TEXT NOT NULL, " +
				COL_FIL_TYPE + " TEXT NOT NULL, " +
				COL_FIL_RULE + " TEXT NOT NULL, " +
				COL_FIL_STATE + " INTEGER NOT NULL, " +
				COL_FIL_DESC + " TEXT);";
		String create_sms = "CREATE TABLE "+ TABLE_SMS +
				" (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				COL_SMS_SENDER + " TEXT NOT NULL, " +
				COL_SMS_CONTENT + " TEXT, " +
				COL_SMS_RECV_TIME + " NUMERIC NOT NULL, " +
				COL_SMS_STATE + " INTEGER NOT NULL)";
		db.execSQL(create_filter);
		db.execSQL(create_sms);
		// test data
		ContentValues test1 = new ContentValues();
		test1.put(COL_SMS_SENDER, "+1234567");
		test1.put(COL_SMS_CONTENT, "This is a test message.");
		test1.put(COL_SMS_RECV_TIME, 12345);
		test1.put(COL_SMS_STATE, 0);
		db.insert(TABLE_SMS, null, test1);
		ContentValues test2 = new ContentValues();
		test2.put(COL_FIL_TARGET, "address");
		test2.put(COL_FIL_TYPE, "raw");
		test2.put(COL_FIL_RULE, "106%");
		test2.put(COL_FIL_STATE, 0);
		test2.put(COL_FIL_DESC, "My favourite");
		db.insert(TABLE_FILTER, null, test2);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
