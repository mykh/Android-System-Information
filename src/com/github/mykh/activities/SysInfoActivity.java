/*
* Android System Information
* Copyright (C) 2010-2012 mykh
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.github.mykh.activities;

import com.github.mykh.R;
import com.github.mykh.common.ConfigList;
import com.github.mykh.system.SysInfo;
import com.github.mykh.views.IConfigFormatter;
import com.github.mykh.views.ConfigFormatter_PlainText;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class SysInfoActivity extends Activity {

	static final private int MENU_REFRESH = Menu.FIRST;

	private SysInfo info;
	private TextView textView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		textView = (TextView) findViewById(R.id.tvInfo);
		assert (textView != null);
		info = new SysInfo(getApplicationContext());
		refresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem miRefresh = menu.add(0, MENU_REFRESH, Menu.NONE, R.string.act_refresh);
		//miRefresh.setIcon(R.drawable.refresh);
		miRefresh.setShortcut('0', 'r');

		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		  super.onOptionsItemSelected(item);
		  switch (item.getItemId()) {
		  case (MENU_REFRESH):
			  refresh();
			  return true;
		  }
		  return false;
		}

	private void refresh() {
		ConfigList config = new ConfigList("");
		info.process(config);
		StringBuilder report = new StringBuilder();
		IConfigFormatter formatter = new ConfigFormatter_PlainText();
		formatter.process(config, report, 0);
		textView.setText(report.toString());
	}
}
