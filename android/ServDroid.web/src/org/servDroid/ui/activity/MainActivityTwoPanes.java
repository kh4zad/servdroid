/*
 * Copyright (C) 2013 Joan Puig Sanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.servDroid.ui.activity;

import org.servDroid.ui.fragment.LogFragment;
import org.servDroid.ui.fragment.OptionsFragment;
import org.servDroid.ui.fragment.ServDroidBaseFragment;
import org.servDroid.ui.fragment.SettingsFragment;
import org.servDroid.ui.fragment.WebFragment;
import org.servDroid.ui.fragment.StartStopFragment.OnStartStopButtonPressed;
import org.servDroid.ui.option.IMainOptionsList;
import org.servDroid.ui.option.ServDroidOptions;
import org.servDroid.web.R;

import roboguice.inject.InjectFragment;
import roboguice.inject.InjectView;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.FragmentManager;
import android.widget.FrameLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.inject.Inject;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivityTwoPanes extends ServDroidBaseFragmentActivity implements OnStartStopButtonPressed{

	private LogFragment mLogFragment;
	private WebFragment mWebFragment;
	private PreferenceFragment mSettingsFragment;

	private ServDroidBaseFragment mCurrentSupportFragment;
	private String mCurrentFragmentTag;

	@InjectFragment(R.id.optionsFragment)
	private OptionsFragment mOptionsFragment;

	@InjectView(R.id.fillableFrameLayout)
	private FrameLayout mFillableLayout;

	@Inject
	private IMainOptionsList mOptions;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_twopanes);

		if (mCurrentFragmentTag == null & mCurrentSupportFragment == null) {
			setRightSuportFragment(getLogFragment());
		}
	}

	@Override
	protected void createMainMenus(Menu menu) {
		MenuItem menuItemHelp = appMenu.add(0, MENU_ID_HELP, 0, R.string.menu_help);
		menuItemHelp.setIcon(android.R.drawable.ic_menu_help);
		menuItemHelp.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		if (storeHelper != null && storeHelper.hasStoreInfo()) {
			MenuItem menuItemDonate = appMenu.add(0, MENU_ID_DONATE, 0, R.string.donate);
			menuItemDonate.setIcon(R.drawable.ic_menu_donate);
			menuItemDonate.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		super.createMainMenus(menu);
	}

	private WebFragment getWebFragment() {
		if (mWebFragment == null) {
			mWebFragment = new WebFragment();
		}
		return mWebFragment;
	}

	private LogFragment getLogFragment() {
		if (mLogFragment == null) {
			mLogFragment = new LogFragment();
			Bundle params = new Bundle();
			int padding = getResources().getDimensionPixelSize(R.dimen.padding_log_view);
			params.putInt(LogFragment.PARAM_PADDING_TOP, padding);
			params.putInt(LogFragment.PARAM_PADDING_BOTTOM, padding);
			params.putInt(LogFragment.PARAM_PADDING_LEFT, padding);
			params.putInt(LogFragment.PARAM_PADDING_RIGHT, padding);
			mLogFragment.setArguments(params);
		}
		return mLogFragment;
	}

	private android.app.Fragment getSettingsFragment() {
		if (mSettingsFragment == null) {
			mSettingsFragment = new SettingsFragment();
		}
		return mSettingsFragment;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		mCurrentSupportFragment.addSpecificMenu(appMenu);
		return result;
	}

	private synchronized void setRighFragment(android.app.Fragment fragment) {
		android.app.FragmentManager fManager = getFragmentManager();
		if (fragment == null && mCurrentFragmentTag != null) {
			fManager.beginTransaction().remove(fManager.findFragmentByTag(mCurrentFragmentTag))
					.commit();
			mCurrentFragmentTag = null;
			return;
		}
		if (mCurrentFragmentTag != null && mCurrentFragmentTag.equals(fragment.getTag())) {
			return;
		}
		if (fragment != null) {
			setRightSuportFragment(null);
		}
		if (mCurrentFragmentTag != null) {
			fManager.beginTransaction().remove(fManager.findFragmentByTag(mCurrentFragmentTag))
					.commit();
		} else if (fragment == null) {
			mCurrentFragmentTag = null;
			return;
		}
		fManager.beginTransaction()
				.add(R.id.fillableFrameLayout, fragment, fragment.getClass().getSimpleName())
				.commit();
		mCurrentFragmentTag = fragment.getClass().getSimpleName();
	}

	private synchronized void setRightSuportFragment(ServDroidBaseFragment fragment) {
		if (mCurrentSupportFragment == fragment) {
			return;
		}
		if (fragment != null) {
			setRighFragment(null);
		}

		FragmentManager fManager = getSupportFragmentManager();
		if (mCurrentSupportFragment != null) {
			fManager.beginTransaction().remove(mCurrentSupportFragment).commit();
			if (appMenu != null) {
				mCurrentSupportFragment.removeSpecificMenu(appMenu);
			}
		}
		if (fragment != null) {
			fManager.beginTransaction()
					.add(R.id.fillableFrameLayout, fragment, fragment.getClass().getSimpleName())
					.commit();
			fragment.addSpecificMenu(appMenu);
		}
		mCurrentSupportFragment = fragment;
	}

	@Override
	public void onOptionClick(int id) {
		switch (id) {
		case ServDroidOptions.OPTION_ID_LOG:
			setRightSuportFragment(getLogFragment());
			break;
		case ServDroidOptions.OPTION_ID_SETTINGS:
			setRighFragment(getSettingsFragment());
			break;
		case ServDroidOptions.OPTION_ID_WEB:
			setRightSuportFragment(getWebFragment());
			break;
		case ServDroidOptions.OPTION_ID_DUMP_LOG:
			getLogFragment().saveLog();
			break;
		case ServDroidOptions.OPTION_ID_DELETE_LOG:
			getLogFragment().deleteLog();
			break;
		case ServDroidOptions.OPTION_ID_REFRESH_LOG:
			getLogFragment().fillLogList();
			break;
		default:
			break;
		}
	}

	@Override
	public void onStartStopButtonPressed(boolean pressed) {
		if (mCurrentSupportFragment == getLogFragment()){
			getLogFragment().fillLogList();
		}
		
	}
}
