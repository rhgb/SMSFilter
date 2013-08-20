package org.monospace.smsfilter;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SMSListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>,AbsListView.MultiChoiceModeListener {
	SimpleCursorAdapter mAdapter;
	private ActionMode mActionMode;
	private boolean pendingReload = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mAdapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.sms_list_item,
				null,
				new String[]{DbVars.COL_SMS_SENDER, DbVars.COL_SMS_CONTENT, DbVars.COL_SMS_RECV_TIME},
				new int[]{R.id.sms_sender, R.id.sms_content, R.id.sms_recv_time},
				0
		);
		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				switch (cursor.getColumnName(columnIndex)) {
					case DbVars.COL_SMS_RECV_TIME:
						TextView tv = (TextView) view;
						Date recv = new Date(cursor.getLong(columnIndex));
						Calendar now = Calendar.getInstance();
						Calendar recvCal = Calendar.getInstance();
						recvCal.setTime(recv);
						DateFormat format;
						if (now.get(Calendar.YEAR) != recvCal.get(Calendar.YEAR) ||
								now.get(Calendar.DAY_OF_YEAR) != recvCal.get(Calendar.DAY_OF_YEAR)) {
							format = DateFormat.getDateInstance(DateFormat.SHORT);
						} else {
							format = DateFormat.getTimeInstance(DateFormat.SHORT);
						}
						tv.setText(format.format(recv));
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
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView tv = (TextView) view.findViewById(R.id.sms_content);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(R.string.title_dialog_sms_detail)
						.setMessage(tv.getText()).show();
			}
		});
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
		if (pendingReload) {
			reloadList();
		}
		markAllAsRead();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(),
				Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_SMS),
				new String[]{DbVars.COL_ID, DbVars.COL_SMS_SENDER, DbVars.COL_SMS_CONTENT, DbVars.COL_SMS_RECV_TIME},
				null,
				null,
				DbVars.COL_SMS_RECV_TIME+" ASC");
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

	private void reloadList() {
		getLoaderManager().restartLoader(0, null, this);
		pendingReload = false;
	}

	public void refresh() {
		if (isVisible()) {
			reloadList();
		} else {
			pendingReload = true;
		}
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
		int count = getListView().getCheckedItemCount();
		String title = String.format(getResources().getString(R.string.action_mode_title_sms), count);
		mode.setTitle(title);
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater = mode.getMenuInflater();
		inflater.inflate(R.menu.sms_list_choice, menu);
		mActionMode = mode;
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		AlertDialog.Builder builder;
		switch (item.getItemId()) {
			case R.id.menu_delete_sms:
				builder = new AlertDialog.Builder(getActivity());
				String message = getResources().getString(R.string.action_delete_sms);
				builder.setMessage(String.format(message, getListView().getCheckedItemCount()));
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteMessages(getListView().getCheckedItemIds());
						if (mActionMode != null) {
							mActionMode.finish();
						}
					}
				}).setNegativeButton(android.R.string.cancel, null);
				builder.show();
				return true;
			case R.id.menu_preclude_addr:
				builder = new AlertDialog.Builder(getActivity());
				message = getResources().getString(R.string.action_preclude_addr);
				builder.setMessage(String.format(message, getListView().getCheckedItemCount()));
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						int res = precludeAddr(getListView().getCheckedItemIds());
						if (mActionMode != null) {
							mActionMode.finish();
						}
						Toast.makeText(getActivity(),
								String.format(getResources().getString(R.string.msg_addr_precluded), res),
								Toast.LENGTH_LONG).show();
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

	public void deleteMessages(long[] ids){
		String idlist = String.valueOf(ids[0]);
		for (int i = 1; i < ids.length; ++i) {
			idlist = idlist + "," + String.valueOf(ids[i]);
		}
		getActivity().getContentResolver().delete(
				Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_SMS),
				DbVars.COL_ID + " IN (" + idlist + ")",
				null
		);
		reloadList();
	}

	public int precludeAddr(long[] ids) {
		String idlist = String.valueOf(ids[0]);
		for (int i = 1; i < ids.length; ++i) {
			idlist = idlist + "," + String.valueOf(ids[i]);
		}
		ContentResolver resolver = getActivity().getContentResolver();
		Cursor addrCur = resolver.query(
				Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_SMS),
				new String[]{DbVars.COL_SMS_SENDER},
				DbVars.COL_ID + " IN (" + idlist + ")",
				null, null);
		Set<String> addrs = new HashSet<>(ids.length);
		addrCur.moveToFirst();
		while (!addrCur.isAfterLast()) {
			String str = addrCur.getString(0);
			addrs.add(str);
			Log.d("SMSListFragment",str);
			addrCur.moveToNext();
		}
		addrCur.close();
		ContentValues values = new ContentValues(5);
		values.put(DbVars.COL_FIL_TARGET, DbVars.FilterType.TARGET_ADDR);
		values.put(DbVars.COL_FIL_TYPE, DbVars.FilterType.TYPE_RAW);
		values.put(DbVars.COL_FIL_STATE, DbVars.FilterState.PERMIT.toString());
		for (String a : addrs) {
			values.put(DbVars.COL_FIL_RULE, a);
			resolver.insert(
					Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_FILTER),
					values
			);
		}
		return addrs.size();
	}

	public void markAllAsRead() {
		ContentValues v = new ContentValues(1);
		v.put(DbVars.COL_SMS_STATE, DbVars.SMS_STATE_READ);
		getActivity().getContentResolver().update(Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_SMS),
				v,
				DbVars.COL_SMS_STATE + " = ?",
				new String[]{Integer.toString(DbVars.SMS_STATE_UNREAD)});
	}
}