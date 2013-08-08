package org.monospace.smsfilter;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

public class SMSStateReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
		String MSG_TYPE = intent.getAction();
		DatabaseHelper dbHelper = new DatabaseHelper(context);

		if (MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = intent.getExtras();
			Object messages[] = (Object[]) bundle.get("pdus");
			Log.i("smsreceiver", "Messages received l=" + messages.length);

			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// query filter
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) messages[0]);

			String blockSelection = String.format(Locale.US,
					"(%s = ?) AND (((%s = ?) AND (? LIKE %s)) OR ((%s = ?) AND (? LIKE %s)))",
					DbVars.COL_FIL_STATE,
					DbVars.COL_FIL_TARGET,
					DbVars.COL_FIL_RULE,
					DbVars.COL_FIL_TARGET,
					DbVars.COL_FIL_RULE);

			Cursor permitQuery = db.query(DbVars.TABLE_FILTER,
					new String[]{ DbVars.COL_FIL_STATE },
					blockSelection,
					new String[] {
							DbVars.FilterState.PERMIT.toString(),
							DbVars.FilterType.TARGET_ADDR,
							sms.getOriginatingAddress(),
							DbVars.FilterType.TARGET_CONTENT,
							sms.getMessageBody()
					}, null, null, null);
			if (permitQuery.getCount() != 0) {
				permitQuery.close();
				return;
			}

			Cursor blockQuery = db.query(DbVars.TABLE_FILTER,
					new String[]{DbVars.COL_FIL_STATE},
					blockSelection,
					new String[]{
							DbVars.FilterState.STATE_BLOCK,
							DbVars.FilterType.TARGET_ADDR,
							sms.getOriginatingAddress(),
							DbVars.FilterType.TARGET_CONTENT,
							sms.getMessageBody()
					}, null, null, null);
			if (blockQuery.getCount() != 0) {
				abortBroadcast();
				Toast.makeText(context, R.string.msg_sms_blocked, Toast.LENGTH_SHORT).show();
				ContentValues smsValues = new ContentValues();
				smsValues.put(DbVars.COL_SMS_SENDER, sms.getOriginatingAddress());
				smsValues.put(DbVars.COL_SMS_CONTENT, sms.getMessageBody());
				smsValues.put(DbVars.COL_SMS_RECV_TIME, sms.getTimestampMillis());
				smsValues.put(DbVars.COL_SMS_STATE, 0);
				db.insert(DbVars.TABLE_SMS, null, smsValues);

				Intent newBlocked = new Intent("org.monospace.smsfilter.NEW_BLOCKED_SMS");
				newBlocked.setPackage("org.monospace.smsfilter");
				context.sendBroadcast(newBlocked);
			}
			permitQuery.close();
			blockQuery.close();
		} else {
			Log.w("smsreceiver", "Something else received");
		}
	}
}