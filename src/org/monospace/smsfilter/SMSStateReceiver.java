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

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.Locale;

public class SMSStateReceiver extends BroadcastReceiver {

	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
			DatabaseHelper dbHelper = new DatabaseHelper(context);
			SQLiteDatabase db = dbHelper.getWritableDatabase();

//			int error = intent.getIntExtra("errorCode", 0);
//			String format = intent.getStringExtra("format");
			Object[] pdus = (Object[]) intent.getExtras().get("pdus");
			SmsMessage[] msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < pdus.length; ++i) {
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			}
			SmsMessage sms = msgs[0];
			String address = sms.getOriginatingAddress();
			int protocolId = sms.getProtocolIdentifier();
			long timestamp = sms.getTimestampMillis();
			String msgBody;
			if (msgs.length == 1) {
				msgBody = sms.getDisplayMessageBody();
			} else {
				StringBuilder bodyBuilder = new StringBuilder();
				for (SmsMessage msg : msgs) {
					String body = msg.getDisplayMessageBody();
					if (body != null) {
						bodyBuilder.append(body);
					}
				}
				msgBody = bodyBuilder.toString();
			}
			if (applyFilter(db, address, msgBody)) {
				abortBroadcast();
				if (sms.isReplace()) {
					replaceMessage(db, address, msgBody, protocolId, timestamp);
				} else {
					storeMessage(db, address, msgBody, protocolId, timestamp);
				}
				Toast.makeText(context, R.string.msg_sms_blocked, Toast.LENGTH_SHORT).show();

				Intent newBlocked = new Intent("org.monospace.smsfilter.NEW_BLOCKED_SMS");
				newBlocked.setPackage("org.monospace.smsfilter");
				context.sendBroadcast(newBlocked);
			}
		}
	}
	public boolean applyFilter(SQLiteDatabase db, String address, String body) {

		String selection = String.format(Locale.US,
				"(%s = ?) AND (((%s = ?) AND (? LIKE %s)) OR ((%s = ?) AND (? LIKE %s)))",
				DbVars.COL_FIL_STATE,
				DbVars.COL_FIL_TARGET,
				DbVars.COL_FIL_RULE,
				DbVars.COL_FIL_TARGET,
				DbVars.COL_FIL_RULE);

		Cursor permitQuery = db.query(DbVars.TABLE_FILTER,
				new String[]{ DbVars.COL_FIL_STATE },
				selection,
				new String[] {
						DbVars.FilterState.PERMIT.toString(),
						DbVars.FilterType.TARGET_ADDR,
						address,
						DbVars.FilterType.TARGET_CONTENT,
						body
				}, null, null, null);
		try {
			if (permitQuery.getCount() != 0) {
				return false;
			}
		} finally {
			permitQuery.close();
		}

		Cursor blockQuery = db.query(DbVars.TABLE_FILTER,
				new String[]{DbVars.COL_FIL_STATE},
				selection,
				new String[]{
						DbVars.FilterState.STATE_BLOCK,
						DbVars.FilterType.TARGET_ADDR,
						address,
						DbVars.FilterType.TARGET_CONTENT,
						body
				}, null, null, null);
		try {
			return blockQuery.getCount() != 0;
		} finally {
			blockQuery.close();
		}
	}
	public void storeMessage(SQLiteDatabase db, String address, String body, int protocolId, long timestamp) {
		ContentValues smsValues = new ContentValues(5);

		smsValues.put(DbVars.COL_SMS_SENDER, address);
		smsValues.put(DbVars.COL_SMS_CONTENT, body);
		smsValues.put(DbVars.COL_SMS_RECV_TIME, timestamp);
		smsValues.put(DbVars.COL_SMS_PROTOCOL_ID, protocolId);
		smsValues.put(DbVars.COL_SMS_STATE, DbVars.SMS_STATE_UNREAD);

		db.insert(DbVars.TABLE_SMS, null, smsValues);
	}
	public void replaceMessage(SQLiteDatabase db, String address, String body, int protocolId, long timestamp) {
		Cursor cursor = db.query(DbVars.TABLE_SMS,
				new String[]{DbVars.COL_ID},
				DbVars.COL_SMS_SENDER + " = ? AND " + DbVars.COL_SMS_PROTOCOL_ID + " = ?",
				new String[]{address, Integer.toString(protocolId)},
				null, null, null);
		if (cursor.moveToFirst()) {
			int id = cursor.getInt(0);

			ContentValues values = new ContentValues(3);

			values.put(DbVars.COL_SMS_CONTENT, body);
			values.put(DbVars.COL_SMS_RECV_TIME, timestamp);
			values.put(DbVars.COL_SMS_STATE, DbVars.SMS_STATE_UNREAD);

			db.update(DbVars.TABLE_SMS,
					values,
					DbVars.COL_ID + " = ?", new String[]{Integer.toString(id)});
		} else {
			storeMessage(db, address, body, protocolId, timestamp);
		}
		cursor.close();
	}
}