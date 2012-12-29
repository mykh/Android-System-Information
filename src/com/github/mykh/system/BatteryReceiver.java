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

package com.github.mykh.system;

import com.github.mykh.common.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BatteryReceiver extends BroadcastReceiver {
	private final static String extraHealth = getExtra("EXTRA_HEALTH", "health"); // integer containing the current health constant.
	private final static String extraIconSmall = getExtra("EXTRA_ICON_SMALL", "icon-small");// integer containing the resource ID of a small status bar icon indicating the current battery state.
	private final static String extraLevel = getExtra("EXTRA_LEVEL", "level"); // integer field containing the current battery level, from 0 to EXTRA_SCALE.
	private final static String extraPlugged = getExtra("EXTRA_PLUGGED", "plugged"); // integer indicating whether the device is plugged in to a power source; 0 means it is on battery, other constants are different types of power sources.
	private final static String extraPresent = getExtra("EXTRA_PRESENT", "present"); // boolean indicating whether a battery is present.
	private final static String extraScale = getExtra("EXTRA_SCALE", "scale"); // integer containing the maximum battery level.
	private final static String extraStatus = getExtra("EXTRA_STATUS", "status"); // integer containing the current status constant.
	private final static String extraTechnology = getExtra("EXTRA_TECHNOLOGY", "technology"); // String describing the technology of the current battery.
	private final static String extraTemperature = getExtra("EXTRA_TEMPERATURE", "temperature"); // integer containing the current battery temperature.
	private final static String extraVoltage = getExtra("EXTRA_VOLTAGE", "voltage"); // integer containing the current battery voltage level.

	private static final int BATTERY_HEALTH_COLD = Utils.getClassFieldInt("android.os.BatteryManager", "BATTERY_HEALTH_COLD", -1);

	private boolean dataReceived = false;
	private int health = -1;
	private int iconSmall = -1;
	private int level = -1;
	private int plugged = -1;
	private Boolean present = null;
	private int scale = -1;
	private int status = -1;
	private String technology = null;
	private int temperature = -1;
	private int voltage = -1;
	
	private static String getExtra(String propName, String defaultName) {
		String result = Utils.getClassFieldStrOrNull("android.os.BatteryManager", propName);
		if (result == null)
			result = defaultName;
		return result;
	}

	public String getHealthStr() {
		//android.os.BatteryManager.BATTERY_HEALTH
		switch (health) {
		case android.os.BatteryManager.BATTERY_HEALTH_DEAD:
			return "dead";
		case android.os.BatteryManager.BATTERY_HEALTH_GOOD:
			return "good";
		case android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT:
			return "overheat";
		case android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
			return "over voltage";
		case android.os.BatteryManager.BATTERY_HEALTH_UNKNOWN:
			return "unknown";
		case android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
			return "unspecified failure";
		default:
			if (health == BATTERY_HEALTH_COLD)
				return "cold";
			else {
				Log.w(getClass().getName(), "Unknown health. value = " + Integer.toString(health));
				return null;
			}
		}
	}

	public int getIconSmall() {
		return iconSmall;
	}

	public String getLevelStr() {
		if (level == -1)
			return null;
		if (scale <= 0)
			return Integer.toString(level);
		else {
			double value = 100.0 * (double) level / (double) scale;
			if ((scale == 10) || (scale == 100))
				return String.format(Utils.locale, "%3.0f%%", value);
			else
				return String.format(Utils.locale, "%5.2f%%", value);
		}
	}

	public String getPluggedStr() {
		switch (plugged) {
		case android.os.BatteryManager.BATTERY_PLUGGED_AC:
			return "AC"; // Power source is an AC charger.
		case android.os.BatteryManager.BATTERY_PLUGGED_USB:
			return "USB"; // Power source is a USB port.
		default:
			Log.w(getClass().getName(), "Unknown plugged. value = " + Integer.toString(plugged));
			return null;
		}
	}

	public String getPresentStr() {
		if (present == null)
			return null;
		else
			return present ? "yes" : "no";
	}

	public int getScale() {
		return scale;
	}

	public String getStatusStr() {
		switch (status) {
		case -1:
			return null;
		case android.os.BatteryManager.BATTERY_STATUS_CHARGING:
			return "charging";
		case android.os.BatteryManager.BATTERY_STATUS_DISCHARGING:
			return "discharging";
		case android.os.BatteryManager.BATTERY_STATUS_FULL:
			return "full";
		case android.os.BatteryManager.BATTERY_STATUS_NOT_CHARGING:
			return "not charging";
		case android.os.BatteryManager.BATTERY_STATUS_UNKNOWN:
			return "unknown";
		default:
			Log.w(getClass().getName(), "Unknown status. value = " + Integer.toString(status));
			return null;
		}
	}

	public String getTechnology() {
		return technology;
	}

	public String getTemperatureStr() {
		if (temperature == -1)
			return null;
		else {
			double c = temperature / 100.0;
			double f = (9.0 * c) / 5.0 + 32.0;
			return String.format(Utils.locale, "%4.1f°C (%4.1f°F)", c, f);
		}
	}

	public String getVoltageStr() {
		if (voltage == -1)
			return null;
		else
			return String.format(Utils.locale, "%5.4fV", voltage / 1000.0); // TODO: show in V, mV
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		level = intent.getIntExtra(extraLevel, -1);
		health = intent.getIntExtra(extraHealth, -1);
		iconSmall = intent.getIntExtra(extraIconSmall, -1);
		level = intent.getIntExtra(extraLevel, -1);
		plugged = intent.getIntExtra(extraPlugged, -1);
		Boolean present = intent.getBooleanExtra(extraPresent, false);
		scale = intent.getIntExtra(extraScale, -1);
		status = intent.getIntExtra(extraStatus, -1);
		technology = intent.getStringExtra(extraTechnology);
		temperature = intent.getIntExtra(extraTemperature, -1);
		voltage = intent.getIntExtra(extraVoltage, -1);
		
		dataReceived = true;

		String log = "health=" + Integer.toString(health) + ", " + "iconSmall=" + Integer.toString(iconSmall)
				+ ", " + "level=" + Integer.toString(level) + ", " + "plugged=" + Integer.toString(plugged) + ", "
				+ "present=" + Boolean.toString(present) + ", " + "scale=" + Integer.toString(scale) + ", "
				+ "status=" + Integer.toString(status) + ", " + "technology=" + technology + ", " + "temperature="
				+ Integer.toString(temperature) + ", " + "voltage=" + Integer.toString(voltage);

		Log.v(Utils.LOGGER_TAG, log);
	}
	
	public boolean isDataReceived() {
		return dataReceived;
	};
}