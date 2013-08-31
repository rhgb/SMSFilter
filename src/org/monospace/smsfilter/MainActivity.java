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

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class MainActivity extends Activity implements EditFilterDialogFragment.DialogListener {

	private class TabHelper extends FragmentPagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

		private Activity mActivity;
		private ViewPager mPager;

		public TabHelper(Activity activity, ViewPager pager) {
			super(activity.getFragmentManager());
			mActivity = activity;
			mPager = pager;
		}

		@Override
		public Fragment getItem(int position) {
			TabSet.Tab tab = mTabs.get(position);
			Fragment fragment = Fragment.instantiate(mActivity, tab.itemClass.getName());
			tab.setItem(fragment);
			return fragment;
		}

		@Override
		public int getCount() {
			return NUM_TABS;
		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			mPager.setCurrentItem(tab.getPosition());
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
		}

		@Override
		public void onPageScrolled(int i, float v, int i2) {
		}

		@Override
		public void onPageSelected(int i) {
			mActivity.getActionBar().setSelectedNavigationItem(i);
		}

		@Override
		public void onPageScrollStateChanged(int i) {
		}
	}

	private class TabSet extends ArrayList<TabSet.Tab> {
		public class Tab {
			final public Class itemClass;
			final public String tag;
			final public int textId;
			private Fragment item;

			public Tab(String tag, int txt, Class clz) {
				this.itemClass = clz;
				this.tag = tag;
				this.textId = txt;
			}
			@Override
			public boolean equals(Object o) {
				return Tab.class.isInstance(o) && ((Tab) o).tag.equals(this.tag);
			}

			public void setItem(Fragment item) {
				this.item = item;
			}
		}

		public TabSet(int capacity) {
			super(capacity);
		}

		public void add(String tag, int txt, Class clz) {
			Tab tab = new Tab(tag, txt, clz);
			if (!this.contains(tab)) {
				this.add(tab);
			}
		}

		public Fragment getItem(String tag) {
			for (Tab tab : this) {
				if (tab.tag.equals(tag)) {
					return tab.item;
				}
			}
			return null;
		}
	}

	private static final int NUM_TABS = 2;
	private static final String TAB_SMS = "tab_sms_list";
	private static final String TAB_FILTER = "tab_filter_list";
	private BroadcastReceiver mReceiver;
	private TabSet mTabs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

		mTabs = new TabSet(NUM_TABS);
		mTabs.add(TAB_SMS, R.string.tab_sms_list, SMSListFragment.class);
		mTabs.add(TAB_FILTER, R.string.tab_filter_list, FilterListFragment.class);

		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);

		ViewPager pager = new ViewPager(this);
		pager.setId(R.id.main_pager);

		TabHelper helper = new TabHelper(this, pager);
		pager.setAdapter(helper);
		pager.setOnPageChangeListener(helper);

		for (TabSet.Tab t : mTabs) {
			Tab tab = actionBar.newTab()
					.setText(t.textId)
					.setTabListener(helper);
			actionBar.addTab(tab);
		}

		setContentView(pager);

		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				SMSListFragment fragment = (SMSListFragment) mTabs.getItem(TAB_SMS);
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
	public void onDialogPositiveClick(DbVars.FilterType type, DbVars.FilterState state, String rule, String desc) {
		ContentValues values = new ContentValues(4);
		values.put(DbVars.COL_FIL_TARGET, type.getTarget());
		values.put(DbVars.COL_FIL_TYPE, type.getType());
		values.put(DbVars.COL_FIL_RULE, type.applyContent(rule));
		values.put(DbVars.COL_FIL_STATE, state.toString());
		values.put(DbVars.COL_FIL_DESC, desc);
		getContentResolver().insert(Uri.withAppendedPath(DatabaseProvider.CONTENT_URI, DbVars.TABLE_FILTER), values);
		FilterListFragment fragment = (FilterListFragment) mTabs.getItem(TAB_FILTER);
		if (fragment != null) {
			fragment.getLoaderManager().restartLoader(0, null, fragment);
		}
	}
}
