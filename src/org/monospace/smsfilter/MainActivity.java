package org.monospace.smsfilter;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements EditFilterDialogFragment.DialogListener {

	private static class TabListener<T extends Fragment> implements ActionBar.TabListener {
	    private Fragment mFragment;
	    private final Activity mActivity;
	    private final String mTag;
	    private final Class<T> mClass;

	    /** Constructor used each time a new tab is created.
	      * @param activity  The host Activity, used to instantiate the fragment
	      * @param tag  The identifier tag for the fragment
	      * @param clz  The fragment's Class, used to instantiate the fragment
	      */
	    public TabListener(Activity activity, String tag, Class<T> clz) {
	        mActivity = activity;
	        mTag = tag;
	        mClass = clz;
	    }
	    
	    @Override
	    public void onTabSelected(Tab tab, FragmentTransaction ft) {
	        // Check if the fragment is already initialized
	        if (mFragment == null) {
	            // If not, instantiate and add it to the activity
	            mFragment = Fragment.instantiate(mActivity, mClass.getName());
	            ft.add(android.R.id.content, mFragment, mTag);
	        } else {
	            // If it exists, simply attach it in order to show it
	            ft.attach(mFragment);
	        }
	    }
	    @Override
	    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	        if (mFragment != null) {
	            // Detach the fragment, because another one is being attached
	            ft.detach(mFragment);
	        }
	    }
	    @Override
	    public void onTabReselected(Tab tab, FragmentTransaction ft) {
	        // User selected the already selected tab. Usually do nothing.
	    }
	}

	private static final String TAB_SMS = "tab_sms_list";
	private static final String TAB_FILTER = "tab_filter_list";
	private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
	    super.onCreate(savedInstanceState);
	    // Notice that setContentView() is not used, because we use the root
	    // android.R.id.content as the container for each fragment
		// setup action bar for tabs
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		Tab tab = actionBar.newTab()
				.setText(R.string.tab_sms_list)
				.setTabListener(new TabListener<>(
						this, TAB_SMS, SMSListFragment.class));
		actionBar.addTab(tab);

		tab = actionBar.newTab()
				.setText(R.string.tab_filter_list)
				.setTabListener(new TabListener<>(
						this, TAB_FILTER, FilterListFragment.class));
		actionBar.addTab(tab);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				SMSListFragment fragment = (SMSListFragment) getFragmentManager().findFragmentByTag(TAB_SMS);
				fragment.refresh();
			}
		};

		registerReceiver(mReceiver, new IntentFilter("org.monospace.smsfilter.NEW_BLOCKED_SMS"));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			case R.id.menu_add:
				EditFilterDialogFragment dialog = new EditFilterDialogFragment();
				dialog.setDialogListener(this);
				dialog.show(getFragmentManager(), "EditFilterDialogFragment");
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onDialogPositiveClick(EditFilterDialogFragment.FilterType type, EditFilterDialogFragment.FilterState state, String content) {
		ContentValues values = new ContentValues(4);
		values.put(DatabaseHelper.COL_FIL_TARGET, type.getTarget());
		values.put(DatabaseHelper.COL_FIL_TYPE, type.getType());
		values.put(DatabaseHelper.COL_FIL_RULE, type.applyContent(content));
		values.put(DatabaseHelper.COL_FIL_STATE, state.toString());
		getContentResolver().insert(Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DatabaseHelper.TABLE_FILTER), values);
		FilterListFragment fragment = (FilterListFragment) getFragmentManager().findFragmentByTag(TAB_FILTER);
		if (fragment != null) {
			fragment.getLoaderManager().restartLoader(0, null, fragment);
		}
	}
}
