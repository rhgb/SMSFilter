package org.monospace.smsfilter;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class SMSListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
	SimpleCursorAdapter mAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.sms_list_item,
				null,
				new String[]{DatabaseHelper.COL_SMS_SENDER, DatabaseHelper.COL_SMS_CONTENT},
				new int[]{R.id.sms_sender, R.id.sms_content},
				0
		);
		setListAdapter(mAdapter);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);
	}
	/* (non-Javadoc)
	 * @see android.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new DatabaseCursorLoader(getActivity(),
				DatabaseHelper.TABLE_SMS,
				new String[]{"id AS _id", DatabaseHelper.COL_SMS_SENDER, DatabaseHelper.COL_SMS_CONTENT},
				null,
				null,
				DatabaseHelper.COL_SMS_RECV_TIME+" ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

}
