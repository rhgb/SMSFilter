package org.monospace.smsfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 2;
	private static final String DB_NAME = "main_db";
    public static final String COL_ID = "_id";
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
		createFilterTable(db);
		createSMSTable(db);
		genInitData(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1) {
			db.execSQL("DROP TABLE "+TABLE_FILTER+";");
			createFilterTable(db);
		}
		genInitData(db);
	}

	private void createFilterTable(SQLiteDatabase db) {
		String create_filter = "CREATE TABLE "+ TABLE_FILTER +
				" ("+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				COL_FIL_TARGET + " TEXT NOT NULL, " +
				COL_FIL_TYPE + " TEXT NOT NULL, " +
				COL_FIL_RULE + " TEXT NOT NULL, " +
				COL_FIL_STATE + " TEXT NOT NULL, " +
				COL_FIL_DESC + " TEXT);";
		db.execSQL(create_filter);
	}

	private void createSMSTable(SQLiteDatabase db) {
		String create_sms = "CREATE TABLE "+ TABLE_SMS +
				" ("+COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				COL_SMS_SENDER + " TEXT NOT NULL, " +
				COL_SMS_CONTENT + " TEXT, " +
				COL_SMS_RECV_TIME + " NUMERIC NOT NULL, " +
				COL_SMS_STATE + " INTEGER NOT NULL)";
		db.execSQL(create_sms);
	}

	private void genInitData(SQLiteDatabase db) {
		ContentValues values = new ContentValues(5);
		values.put(COL_FIL_TARGET, EditFilterDialogFragment.FilterType.TARGET_ADDR);
		values.put(COL_FIL_TYPE, EditFilterDialogFragment.FilterType.TYPE_RAW);
		values.put(COL_FIL_RULE, "106%");
		values.put(COL_FIL_STATE, EditFilterDialogFragment.FilterState.BLOCK.toString());
		values.put(COL_FIL_DESC, "My favourite");
		db.insert(TABLE_FILTER, null, values);
	}
}
