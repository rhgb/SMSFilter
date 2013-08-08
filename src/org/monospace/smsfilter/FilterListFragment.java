package org.monospace.smsfilter;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class FilterListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.MultiChoiceModeListener {

	private SimpleCursorAdapter mAdapter;
	private ActionMode mActionMode;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.filter_list_item,
				null,
				new String[]{DbVars.COL_FIL_RULE, DbVars.COL_FIL_STATE, DbVars.COL_FIL_DESC},
				new int[]{R.id.filter_rule, R.id.filter_status, R.id.filter_desc},
				0
		);
		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				switch (cursor.getColumnName(columnIndex)) {
					case DbVars.COL_FIL_STATE:
						TextView tv = (TextView) view;
						switch (cursor.getString(columnIndex)) {
							case DbVars.FilterState.STATE_PERMIT:
								tv.setTextColor(Color.parseColor("#669900"));
								tv.setText(R.string.filter_state_permit);
								break;
							case DbVars.FilterState.STATE_BLOCK:
								tv.setTextColor(Color.parseColor("#CC0000"));
								tv.setText(R.string.filter_state_block);
								break;
						}
						return true;
					default:
						return false;
				}
			}
		});
		setListAdapter(mAdapter);
		setListShown(false);
		ListView lv = getListView();
		lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
		lv.setMultiChoiceModeListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	/* (non-Javadoc)
	 * @see android.app.Fragment#onPause()
	 */
	@Override
	public void onResume() {
		super.onResume();
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(),
				Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_FILTER),
				new String[]{DbVars.COL_ID, DbVars.COL_FIL_RULE, DbVars.COL_FIL_STATE, DbVars.COL_FIL_DESC},
				null,
				null,
				DbVars.COL_ID +" ASC");
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

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.filter_list_choice, menu);
		mActionMode = mode;
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_delete_filter:
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				String message = getResources().getString(R.string.action_delete_filters);
				builder.setMessage(String.format(message, getListView().getCheckedItemCount()));
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteFilters(getListView().getCheckedItemIds());
						if (mActionMode != null) {
							mActionMode.finish();
						}
					}
				}).setNegativeButton(android.R.string.cancel, null);
				builder.show();
				return true;
			default:
				return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mActionMode = null;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		int count = getListView().getCheckedItemCount();
		String title = String.format(getResources().getString(R.string.action_mode_title_filter), count);
		mode.setTitle(title);
	}

	public void deleteFilters(long[] ids) {
		String idlist = String.valueOf(ids[0]);
		for (int i = 1; i < ids.length; ++i) {
			idlist = idlist + "," + String.valueOf(ids[i]);
		}
		getActivity().getContentResolver().delete(
				Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_FILTER),
				DbVars.COL_ID + " IN (" + idlist + ")",
				null);
		getLoaderManager().restartLoader(0, null, this);
	}
}
