package org.monospace.smsfilter;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

/**
 * SMSFilter
 * Author: rhgb
 */
public class DatabaseCursorLoader extends AsyncTaskLoader<Cursor> {
	final ForceLoadContentObserver mObserver;
	String mTable;
	String[] mColumns;
	String mSelection;
	String[] mSelectionArgs;
	String mOrderBy;
	DatabaseHelper mDatabaseHelper;
	Cursor mCursor;

	public DatabaseCursorLoader(Context context, String table, String[] columns, String selection,
	                    String[] selectionArgs, String orderBy) {
		super(context);
		mObserver = new ForceLoadContentObserver();
		mTable = table;
		mColumns = columns;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mOrderBy = orderBy;
		mDatabaseHelper = new DatabaseHelper(context);
	}
	/* Runs on a worker thread */
	@Override
	public Cursor loadInBackground() {
        Cursor cursor = mDatabaseHelper.getWritableDatabase().query(false, mTable, mColumns, mSelection, mSelectionArgs, null, null, mOrderBy, null);
        if (cursor != null) {
            // Ensure the cursor window is filled
            cursor.getCount();
            cursor.registerContentObserver(mObserver);
        }
        return cursor;
	}

	/* Runs on the UI thread */
	@Override
	public void deliverResult(Cursor cursor) {
		if (isReset()) {
			// An async query came in while the loader is stopped
			if (cursor != null) {
				cursor.close();
			}
			return;
		}
		Cursor oldCursor = mCursor;
		mCursor = cursor;

		if (isStarted()) {
			super.deliverResult(cursor);
		}

		if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
			oldCursor.close();
		}
	}

	/**
	 * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
	 * will be called on the UI thread. If a previous load has been completed and is still valid
	 * the result may be passed to the callbacks immediately.
	 *
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);
		}
		if (takeContentChanged() || mCursor == null) {
			forceLoad();
		}
	}

	/**
	 * Must be called from the UI thread
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	@Override
	public void onCanceled(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
		mCursor = null;
	}

}
