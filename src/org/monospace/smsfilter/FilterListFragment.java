package org.monospace.smsfilter;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

public class FilterListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	SimpleCursorAdapter mAdapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.filter_list_item,
				null,
				new String[]{DatabaseHelper.COL_FIL_RULE, DatabaseHelper.COL_FIL_TARGET, DatabaseHelper.COL_FIL_DESC},
				new int[]{R.id.filter_rule, R.id.filter_type, R.id.filter_desc},
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
				DatabaseHelper.TABLE_FILTER,
				new String[]{"id AS _id", DatabaseHelper.COL_FIL_RULE, DatabaseHelper.COL_FIL_TARGET, DatabaseHelper.COL_FIL_DESC},
				null,
				null,
				DatabaseHelper.COL_FIL_TARGET +" ASC");
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
