package org.monospace.smsfilter;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSStateReceiver extends BroadcastReceiver {
	public void onReceive(Context context, Intent intent) {
		String MSG_TYPE = intent.getAction();
		DatabaseHelper dbHelper = new DatabaseHelper(context);

		if (MSG_TYPE.equals("android.provider.Telephony.SMS_RECEIVED")) {
			Bundle bundle = intent.getExtras();
			Object messages[] = (Object[]) bundle.get("pdus");
			Log.i("smsreceiver", "Messages received l=" + messages.length);

			SQLiteDatabase db = dbHelper.getWritableDatabase();
			String query = "SELECT id FROM "+DatabaseHelper.TABLE_FILTER +" WHERE ("+DatabaseHelper.COL_FIL_TARGET +"='address') AND (? LIKE "+DatabaseHelper.COL_FIL_RULE+");";
			SmsMessage sms = SmsMessage.createFromPdu((byte[]) messages[0]);
			String[] initAddr = { sms.getOriginatingAddress() };
			Cursor res = db.rawQuery(query, initAddr);
			if (res.getCount() != 0) {
				abortBroadcast();
				Toast.makeText(context, "Message blocked", Toast.LENGTH_SHORT).show();
				ContentValues smsValues = new ContentValues();
				smsValues.put(DatabaseHelper.COL_SMS_SENDER, sms.getOriginatingAddress());
				smsValues.put(DatabaseHelper.COL_SMS_CONTENT, sms.getMessageBody());
				smsValues.put(DatabaseHelper.COL_SMS_RECV_TIME, sms.getTimestampMillis());
				smsValues.put(DatabaseHelper.COL_SMS_STATE, 0);
				db.insert(DatabaseHelper.TABLE_SMS, null, smsValues);

				Intent newBlocked = new Intent("org.monospace.smsfilter.NEW_BLOCKED_SMS");
				newBlocked.setPackage("org.monospace.smsfilter");
				context.sendBroadcast(newBlocked);
			}
		} else {
			Log.w("smsreceiver", "Something else received");
		}
	}
}