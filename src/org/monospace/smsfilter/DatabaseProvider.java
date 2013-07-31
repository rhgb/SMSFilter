/**
 * @author rhgb
 * 13-7-8
 */

package org.monospace.smsfilter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class DatabaseProvider extends ContentProvider {
	DatabaseHelper mHelper;

	private static final class URI_PATTERN {
		static final int SMS_LIST = 1;
		static final int SMS_ROW = 2;
		static final int FILTER_LIST = 3;
		static final int FILTER_ROW = 4;
	}

	private class UriParser {
		private boolean generated;
		private final Uri mUri;
		private int mMatch;
		private final String mSelection;
		private String mTable;
		private String mSelGen;

		public UriParser(Uri uri, String sel) {
			generated = false;
			mUri = uri;
			if (sel != null) {
				mSelection = sel;
			} else {
				mSelection = "";
			}
		}

		public UriParser(Uri uri) {
			this(uri, "");
		}

		private void generate() {
			if (mUri == null || mSelection == null) throw new NullPointerException();
			mMatch = sMatcher.match(mUri);
			switch (mMatch) {
				case URI_PATTERN.SMS_LIST:
					mTable = DbVars.TABLE_SMS;
					mSelGen = mSelection;
					break;
				case URI_PATTERN.SMS_ROW:
					mTable = DbVars.TABLE_SMS;
					mSelGen = mSelection.isEmpty() ?
							DbVars.COL_ID + "=" + mUri.getLastPathSegment() :
							mSelection + " AND " + DbVars.COL_ID + "=" + mUri.getLastPathSegment();
					break;
				case URI_PATTERN.FILTER_LIST:
					mTable = DbVars.TABLE_FILTER;
					mSelGen = mSelection;
					break;
				case URI_PATTERN.FILTER_ROW:
					mTable = DbVars.TABLE_FILTER;
					mSelGen = mSelection.isEmpty() ?
							DbVars.COL_ID + "=" + mUri.getLastPathSegment() :
							mSelection + " AND " + DbVars.COL_ID + "=" + mUri.getLastPathSegment();
					break;
				default:
					throw new IllegalArgumentException();
			}
			generated = true;
		}

		public String getTable() {
			if (!generated) {
				generate();
			}
			return mTable;
		}

		public String getSelection() {
			if (!generated) {
				generate();
			}
			return mSelGen;
		}

		public int getMatch() {
			if (!generated) {
				generate();
			}
			return mMatch;
		}
	}

	public static final String AUTHORITY = "org.monospace.smsfilter.provider";
	private static final UriMatcher sMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sMatcher.addURI(AUTHORITY, DbVars.TABLE_SMS, URI_PATTERN.SMS_LIST);
		sMatcher.addURI(AUTHORITY, DbVars.TABLE_SMS + "/#", URI_PATTERN.SMS_ROW);
		sMatcher.addURI(AUTHORITY, DbVars.TABLE_FILTER, URI_PATTERN.FILTER_LIST);
		sMatcher.addURI(AUTHORITY, DbVars.TABLE_FILTER + "/#", URI_PATTERN.FILTER_ROW);
	}

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	@Override
	public boolean onCreate() {
		mHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		UriParser parser = new UriParser(uri, selection);
		String table = parser.getTable();
		String selMod = parser.getSelection();
		SQLiteDatabase db = mHelper.getReadableDatabase();
		return db.query(table, projection, selMod, selectionArgs, null, null, sortOrder);
	}

	@Override
	public String getType(Uri uri) {
		switch (sMatcher.match(uri)) {
			case URI_PATTERN.SMS_LIST:
				return "vnd.android.cursor.dir/vnd.org.monospace.smsfilter.provider.sms";
			case URI_PATTERN.SMS_ROW:
				return "vnd.android.cursor.item/vnd.org.monospace.smsfilter.provider.sms";
			case URI_PATTERN.FILTER_LIST:
				return "vnd.android.cursor.dir/vnd.org.monospace.smsfilter.provider.filter";
			case URI_PATTERN.FILTER_ROW:
				return "vnd.android.cursor.item/vnd.org.monospace.smsfilter.provider.filter";
			default:
				return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		UriParser parser = new UriParser(uri);
		int match = parser.getMatch();
		if (match == URI_PATTERN.SMS_ROW || match == URI_PATTERN.FILTER_ROW) throw new IllegalArgumentException();
		String table = parser.getTable();
		SQLiteDatabase db = mHelper.getWritableDatabase();
		long value = db.insert(table, null, values);
		return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		UriParser parser = new UriParser(uri, selection);
		String table = parser.getTable();
		String selMod = parser.getSelection();
		SQLiteDatabase db = mHelper.getWritableDatabase();
		return db.delete(table, selMod, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		UriParser parser = new UriParser(uri, selection);
		String table = parser.getTable();
		String selMod = parser.getSelection();
		SQLiteDatabase db = mHelper.getWritableDatabase();
		return db.update(table, values, selMod, selectionArgs);
	}
}
