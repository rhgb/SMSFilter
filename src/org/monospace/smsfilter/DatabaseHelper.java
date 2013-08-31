/*
 * Copyright 2013 Wang Mengzhe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.monospace.smsfilter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static final int DB_VERSION = 3;
	private static final String DB_NAME = "main_db";

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
		if (oldVersion < 2) {
			db.execSQL("DROP TABLE "+ DbVars.TABLE_FILTER+";");
			createFilterTable(db);
			genInitData(db);
		}
		if (oldVersion < 3) {
			//TODO alter table
			db.execSQL("ALTER TABLE " + DbVars.TABLE_SMS +
					" ADD " + DbVars.COL_SMS_PROTOCOL_ID + " INTEGER NULL");
		}
	}

	private void createFilterTable(SQLiteDatabase db) {
		String create_filter = "CREATE TABLE "+ DbVars.TABLE_FILTER +
				" ("+ DbVars.COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				DbVars.COL_FIL_TARGET + " TEXT NOT NULL, " +
				DbVars.COL_FIL_TYPE + " TEXT NOT NULL, " +
				DbVars.COL_FIL_RULE + " TEXT NOT NULL, " +
				DbVars.COL_FIL_STATE + " TEXT NOT NULL, " +
				DbVars.COL_FIL_DESC + " TEXT);";
		db.execSQL(create_filter);
	}

	private void createSMSTable(SQLiteDatabase db) {
		String create_sms = "CREATE TABLE "+ DbVars.TABLE_SMS +
				" ("+ DbVars.COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
				DbVars.COL_SMS_PROTOCOL_ID + " INTEGER NULL, " +
				DbVars.COL_SMS_SENDER + " TEXT NOT NULL, " +
				DbVars.COL_SMS_CONTENT + " TEXT, " +
				DbVars.COL_SMS_RECV_TIME + " NUMERIC NOT NULL, " +
				DbVars.COL_SMS_STATE + " INTEGER NOT NULL)";
		db.execSQL(create_sms);
	}

	private void genInitData(SQLiteDatabase db) {
		ContentValues values = new ContentValues(5);
		values.put(DbVars.COL_FIL_TARGET, DbVars.FilterType.TARGET_ADDR);
		values.put(DbVars.COL_FIL_TYPE, DbVars.FilterType.TYPE_RAW);
		values.put(DbVars.COL_FIL_RULE, "106%");
		values.put(DbVars.COL_FIL_STATE, DbVars.FilterState.STATE_BLOCK);
		values.put(DbVars.COL_FIL_DESC, "My favourite");
		db.insert(DbVars.TABLE_FILTER, null, values);
	}
}
