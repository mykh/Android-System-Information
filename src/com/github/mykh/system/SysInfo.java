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

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.github.mykh.common.Config;
import com.github.mykh.common.ConfigBase;
import com.github.mykh.common.ConfigList;
import com.github.mykh.common.Utils;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.Secure;
import android.util.Log;

public class SysInfo {
	private final IntentFilter batteryChargedFilter;
	private final BatteryReceiver batteryReceiver;

	private static int getVersionSdk() {
		final String clsVer = "android.os.Build$VERSION";
		int ver = Utils.getClassFieldInt(clsVer, "SDK_INT", -1);
		if (ver == -1)
			ver = Utils.getClassFieldInt(clsVer, "SDK", -1);
		return ver;
	}

	private static void getProcParams(String filePath, List<String> names, List<String> values) {
		String content;
		try {
			content = Utils.readFileAsString(filePath);
		} catch (IOException e) {
			Log.e(Utils.LOGGER_TAG, "Can't read file (" + filePath + ")");
			return;
		}
		Utils.parseConfString(content, names, values);
	}

	private static void fillNodeListFromProcFile(String filePath, ConfigList list) {
		List<String> names = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		getProcParams(filePath, names, values);
		assert (names.size() == values.size());
		for (int i = 0; i < names.size(); i++)
			list.getItems().add(new Config(names.get(i), values.get(i)));
	}

	private Context context;

	private Context getContext() {
		return context;
	}

	private ConfigList getOS() {
		String androidId = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
		ConfigList info = new ConfigList("OS");
		List<ConfigBase> items = info.getItems();
		items.add(new Config("*Browser UserAgent", null));
		items.add(new Config("Android ID", androidId,	"A 64-bit number (as a hex string) that is randomly generated on the device's first boot and should remain constant for the lifetime of the device. (The value may change if a factory reset is performed on the device.)"));
		items.add(new Config("*Uptime", null));
		items.add(new Config("*Uptime (without sleeps)", null));
		return info;
	}

	private ConfigList getBuildInfos() {
		int versionSdk = getVersionSdk();
		String versionCodeName = Utils.getClassFieldStrOrNull("android.os.Build$VERSION", "CODENAME");
		String versionIncremental = Utils.getClassFieldStrOrNull("android.os.Build$VERSION", "INCREMENTAL");
		String buildCpuAbi1 = Utils.getClassFieldStrOrNull("android.os.Build", "CPU_ABI");
		String buildCpuAbi2 = Utils.getClassFieldStrOrNull("android.os.Build", "CPU_ABI2");
		String buildManufacturer = Utils.getClassFieldStrOrNull("android.os.Build", "MANUFACTURER");
		String buildBootloader = Utils.getClassFieldStrOrNull("android.os.Build", "BOOTLOADER");
		String buildHardware = Utils.getClassFieldStrOrNull("android.os.Build", "HARDWARE");
		String buildRadio = Utils.invokeClassMethodStrOrNull("android.os.Build", "getRadioVersion");
		if ((buildRadio == null) || (buildRadio.length() == 0))
			buildRadio = Utils.getClassFieldStrOrNull("android.os.Build", "RADIO");
		String buildBoard = Utils.getClassFieldStrOrNull("android.os.Build", "BOARD");
		String buildBrand = Utils.getClassFieldStrOrNull("android.os.Build", "BRAND");
		String buildDevice = Utils.getClassFieldStrOrNull("android.os.Build", "DEVICE");
		String buildDisplay = Utils.getClassFieldStrOrNull("android.os.Build", "DISPLAY");
		String buildFingerprint = Utils.getClassFieldStrOrNull("android.os.Build", "FINGERPRINT");
		Date buildDate = new Date(android.os.Build.TIME);

		ConfigList info = new ConfigList("BuildInfos");
		List<ConfigBase> items = info.getItems();
		items.add(new Config("Android version", android.os.Build.VERSION.RELEASE));
		if (versionCodeName != null)
			items.add(new Config("Release Codename", versionCodeName));
		if (versionIncremental != null) // Show it ?
			items.add(new Config("Release version incremental", versionIncremental,
					"The internal value used by the underlying source control to represent this build. E.g., a perforce changelist number or a git hash."));
		items.add(new Config("API LEVEL", (versionSdk == -1) ? null : Integer.toString(versionSdk),
				"The user-visible SDK version of the framework."));
		if (buildCpuAbi1 != null)
			items.add(new Config("CPU ABI", buildCpuAbi1,
					"The name of the instruction set (CPU type + ABI convention) of native code."));
		if (buildCpuAbi2 != null)
			items.add(new Config("CPU ABI 2", buildCpuAbi2,
					"The name of the second instruction set (CPU type + ABI convention) of native code."));
		if (buildManufacturer != null)
			items.add(new Config("Manufacturer", buildManufacturer, "The manufacturer of the product/hardware."));
		if (buildBootloader != null)
			items.add(new Config("Bootloader", buildBootloader, "The system bootloader version number."));
		if (buildHardware != null)
			items.add(new Config("Hardware", buildHardware,
					"The name of the hardware (from the kernel command line or /proc)."));
		if (buildRadio != null)
			items.add(new Config("Radio", buildRadio, "The version string for the radio firmware."));
		if (buildBoard != null)
			items.add(new Config("Board", buildBoard, "The name of the underlying board."));
		if (buildBrand != null)
			items.add(new Config("Brand", buildBrand, "The brand (e.g., carrier) the software is customized for, if any."));
		if (buildDevice != null)
			items.add(new Config("Device", buildDevice, "The name of the industrial design."));
		if (buildDisplay != null)
			items.add(new Config("Display", buildDisplay, "A build ID string meant for displaying to the user."));
		if (buildFingerprint != null)
			items.add(new Config("Fingerprint", buildFingerprint, "A string that uniquely identifies this build."));
		items.add(new Config("Host", android.os.Build.HOST));
		items.add(new Config("ID", android.os.Build.ID));
		items.add(new Config("Model", android.os.Build.MODEL, "The end-user-visible name for the end product."));
		items.add(new Config("Product", android.os.Build.PRODUCT, "The name of the overall product."));
		items.add(new Config("Tags", android.os.Build.TAGS,
				"Comma-separated tags describing the build, like \"unsigned,debug\"."));
		items.add(new Config("Type", android.os.Build.TYPE, "The type of build."));
		items.add(new Config("User", android.os.Build.USER));
		items.add(new Config("*Time", buildDate.toString())); // TODO: show in correct format
		return info;
	}

	private ConfigList getBattery() {
		ConfigList bat = new ConfigList("Battery");
		List<ConfigBase> items = bat.getItems();
		items.add(new Config("Health", batteryReceiver.getHealthStr()));
		items.add(new Config("Level", batteryReceiver.getLevelStr()));
		items.add(new Config("Plugged", batteryReceiver.getPluggedStr()));
		items.add(new Config("Present", batteryReceiver.getPresentStr()));
		items.add(new Config("Status", batteryReceiver.getStatusStr()));
		items.add(new Config("Technology", batteryReceiver.getTechnology()));
		items.add(new Config("Temperature", batteryReceiver.getTemperatureStr()));
		items.add(new Config("Voltage", batteryReceiver.getVoltageStr()));
		return bat;
	}

	private ConfigList getMemory() {
		// Details: http://www.drakaz.com/2010/04/30/android-memory-thresholds/
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(memoryInfo);

		ConfigList mem = new ConfigList("Memory");
		List<ConfigBase> items = mem.getItems();
		items.add(new Config("*Download cache Max/Min", null));
		items.add(new Config("*Data Max/Free", null));
		items.add(new Config("*External storage Max/Free", null));
		items.add(new Config("*Total RAM", null));
		items.add(new Config("Free RAM", Double.toString(memoryInfo.availMem / Utils.MB)));
		items.add(new Config(
				"Threshold RAM",
				Double.toString(memoryInfo.threshold / Utils.MB),
				"The threshold of Free RAM at which we consider memory to be low and start killing background services and other non-extraneous processes."));
		return mem;
	}

	private ConfigList getLowMemoryKillerLevels() {
		String lmkl_minfree = null;
		final String lmklFileName = "/sys/module/lowmemorykiller/parameters/minfree";
		try {
			lmkl_minfree = Utils.readFileAsString(lmklFileName);
		} catch (IOException e) {
			Log.e(Utils.LOGGER_TAG, "Can not read file + " + lmklFileName);
		}
		String lmklFOREGROUND_APP = null;
		String lmklVISIBLE_APP = null;
		String lmklSECONDARY_SERVER = null;
		String lmklHIDDEN_APP = null;
		String lmklCONTENT_PROVIDER = null;
		String lmklEMPTY_APP = null;
		if (lmkl_minfree != null) {
			String[] lmklParams = lmkl_minfree.trim().split(",");
			if (lmklParams.length == 6) {
				try {
					for (int i = 0; i < lmklParams.length; i++) {
						lmklParams[i] = String.format(Utils.locale, "%5.3f MB", Double.parseDouble(lmklParams[i]) * 4.0
								/ Utils.KB);
					}
					lmklFOREGROUND_APP = lmklParams[0];
					lmklVISIBLE_APP = lmklParams[1];
					lmklSECONDARY_SERVER = lmklParams[2];
					lmklHIDDEN_APP = lmklParams[3];
					lmklCONTENT_PROVIDER = lmklParams[4];
					lmklEMPTY_APP = lmklParams[5];
				} catch (Exception e) {
					Log.e(Utils.LOGGER_TAG, "lmkl params parse error.");
				}
			}
			else
				Log.w(Utils.LOGGER_TAG, "Invalid Low Memory Killer Levels");
		}

		ConfigList lmkl = new ConfigList("Low Memory Killer Levels");
		List<ConfigBase> items = lmkl.getItems();
		items.add(new Config("FOREGROUND_APP", lmklFOREGROUND_APP,
				"This is the process running the current foreground app."));
		items.add(new Config("VISIBLE_APP", lmklVISIBLE_APP,
				"This is a process only hosting activities that are visible to the user."));
		items.add(new Config("SECONDARY_SERVER", lmklSECONDARY_SERVER, "This is a process holding a secondary server."));
		items.add(new Config("HIDDEN_APP", lmklHIDDEN_APP,
				"This is a process only hosting activities that are not visible."));
		items.add(new Config("CONTENT_PROVIDER", lmklCONTENT_PROVIDER,
				"This is a process with a content provider that does not have any clients attached to it."));
		items.add(new Config("EMPTY_APP", lmklEMPTY_APP, "This is a process without anything currently running in it."));
		return lmkl;
	}

	private ConfigList getEnvironment() {
		ConfigList env = new ConfigList("Environment");
		List<ConfigBase> items = env.getItems();
		// TODO: make directory clickable, do not show if null
		items.add(new Config("Root Directory", android.os.Environment.getRootDirectory().getPath())); // TODO: check if Directory is null
		items.add(new Config("Data Directory", android.os.Environment.getDataDirectory().getPath()));
		items.add(new Config("Download Cache Directory", android.os.Environment.getDownloadCacheDirectory().getPath()));
		items.add(new Config("External Storage State", android.os.Environment.getExternalStorageState()));
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
			items.add(new Config("External Storage Directory", android.os.Environment.getExternalStorageDirectory()
					.getPath()));
		Boolean Environment_isExternalStorageEmulated = Utils.invokeClassMethodBoolOrNull("android.os.Environment",
				"isExternalStorageEmulated");
		if (Environment_isExternalStorageEmulated != null)
			items.add(new Config("External Storage Is Emulated", Environment_isExternalStorageEmulated ? "yes" : "no"));
		Boolean Environment_isExternalStorageRemovable = Utils.invokeClassMethodBoolOrNull("android.os.Environment",
				"isExternalStorageRemovable");
		if (Environment_isExternalStorageRemovable != null)
			items.add(new Config("External Storage Is Removable", Environment_isExternalStorageRemovable ? "yes" : "no"));
		// TODO: add directory path (use getExternalStoragePublicDirectory(String type))
		items.add(new Config(
				"Alarms Directory",
				Utils.getClassFieldStrOrNull("android.os.Environment", "DIRECTORY_ALARMS"),
				"Standard directory in which to place any audio files that should be in the list of alarms that the user can select (not as regular music)."));
		items.add(new Config("DCIM Directory", Utils.getClassFieldStrOrNull("android.os.Environment", "DIRECTORY_DCIM"),
				"The traditional location for pictures and videos when mounting the device as a camera."));
		items.add(new Config("Downloads Directory", Utils.getClassFieldStrOrNull("android.os.Environment",
				"DIRECTORY_DOWNLOADS"),
				"Standard directory in which to place files that have been downloaded by the user."));
		items.add(new Config("Movies Directory", Utils.getClassFieldStrOrNull("android.os.Environment",
				"DIRECTORY_MOVIES"), "Standard directory in which to place movies that are available to the user."));
		items.add(new Config("Music Directory",
				Utils.getClassFieldStrOrNull("android.os.Environment", "DIRECTORY_MUSIC"),
				"Standard directory in which to place any audio files that should be in the regular list of music for the user."));
		items.add(new Config(
				"Notifications Directory",
				Utils.getClassFieldStrOrNull("android.os.Environment", "DIRECTORY_NOTIFICATIONS"),
				"Standard directory in which to place any audio files that should be in the list of notifications that the user can select (not as regular music)."));
		items.add(new Config("Pictures Directory", Utils.getClassFieldStrOrNull("android.os.Environment",
				"DIRECTORY_PICTURES"), "Standard directory in which to place pictures that are available to the user."));
		items.add(new Config(
				"Podcasts Directory",
				Utils.getClassFieldStrOrNull("android.os.Environment", "DIRECTORY_PODCASTS"),
				"Standard directory in which to place any audio files that should be in the list of podcasts that the user can select (not as regular music)."));
		items.add(new Config(
				"Ringtones Directory",
				Utils.getClassFieldStrOrNull("android.os.Environment", "DIRECTORY_RINGTONES"),
				"Standard directory in which to place any audio files that should be in the list of ringtones that the user can select (not as regular music)."));
		return env;
	}

	private ConfigList getFeatures() {
		ConfigList features = new ConfigList("Features");
		List<ConfigBase> items = features.getItems();
		Class<android.content.pm.PackageManager> pm = android.content.pm.PackageManager.class;
		try {
			Method method = pm.getMethod("getSystemAvailableFeatures", (Class[]) null);
			Object res = method.invoke(getContext().getPackageManager(), (Object[]) null);
			if (res != null)
				for (int i = 0; i < Array.getLength(res); i++) {
					Object feature = Array.get(res, i);
					Field filed = feature.getClass().getField("name");
					String name = (String) filed.get(feature);
					if (name == null) {
						String ver = (String) Utils.invokeObjectMethodObjectOrNull(feature, "getGlEsVersion");
						items.add(new Config("glEsVers", ver)); // TODO: check result 
					} else
						items.add(new Config("feature", name));
				}
		} catch (Exception e) {
			Log.e(Utils.LOGGER_TAG, "Can not call getSystemAvailableFeatures: " + e.getMessage());
			items.add(new Config("feature", "is not available in this version of Android"));
		}
		return features;
	}

	private ConfigList getJavaProperties() {
		ConfigList javaprop = new ConfigList("Java Properties");
		Properties properties = java.lang.System.getProperties();
		Enumeration<?> e = properties.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String Value = (String) properties.get(key);
			javaprop.getItems().add(new Config(key, Value));
		}
		return javaprop;
	}

	private ConfigList getMisc() {
		String procVersion = null;
		final String procVersionFileName = "/proc/version";
		try {
			procVersion = Utils.readFileAsString(procVersionFileName);
		} catch (IOException e) {
			Log.e(Utils.LOGGER_TAG, "Can not read file + " + procVersionFileName);
		}
		ConfigList msc = new ConfigList("Misc");
		List<ConfigBase> items = msc.getItems();
		items.add(new Config("*CacheDir", getContext().getCacheDir().getPath()));
		String ExternalCacheDirPath;
		try {
			Object obj = Utils.invokeObjectMethodObjectOrNull(getContext(), "getExternalCacheDir");
			ExternalCacheDirPath = (String) Utils.invokeObjectMethodObjectOrNull(obj, "getPath");
		} catch (Exception e) {
			ExternalCacheDirPath = null;
			Log.e(Utils.LOGGER_TAG, e.getMessage());
		}
		items.add(new Config("*ExternalCacheDir", ExternalCacheDirPath));
		String ExternalFilesDirPath;
		try {
			Object obj = Utils.invokeObjectMethodObjectOrNull(getContext(), "getExternalFilesDir");
			ExternalFilesDirPath = (String) Utils.invokeObjectMethodObjectOrNull(obj, "getPath");
		} catch (Exception e) {
			ExternalFilesDirPath = null;
			Log.e(Utils.LOGGER_TAG, e.getMessage());
		}
		items.add(new Config("*ExternalFilesDir", ExternalFilesDirPath));
		items.add(new Config("*Kernel version", procVersion));
		items.add(new Config("*Android input methods", null));
		Map<String, String> jenv = java.lang.System.getenv();
		for (String key : jenv.keySet())
			items.add(new Config(key, jenv.get(key)));
		return msc;
	}

	public void process(ConfigList config) {
        // http://etenclub.ru/pda/board/Optimizaciya-raboty-pamyati-shtatnymi-sredstvami-android-os-t30608.html#entry267212
        // http://forum.xda-developers.com/showpost.php?p=5442369&postcount=1
        // http://developer.android.com/reference/android/content/pm/PackageManager.html
        // http://developer.android.com/reference/android/webkit/WebSettings.htm
		// http://www.xinotes.org/notes/note/911/
		// /proc/stat
		// /proc/mounts

		List<ConfigBase> items = config.getItems();
		items.add(getOS());
		items.add(getBuildInfos());
		items.add(getBattery());
		items.add(getMemory());
		items.add(getLowMemoryKillerLevels());

		if (Utils.SHOW_UNIMPLEMENTED_ITEMS) {
			ConfigList tel = new ConfigList("Telephony");
			items.add(tel);
			ConfigList net = new ConfigList("Networks");
			items.add(net);
			ConfigList wifi = new ConfigList("Wifi");
			items.add(wifi);
		}

		ConfigList cpu = new ConfigList("CPU");
		fillNodeListFromProcFile("/proc/cpuinfo", cpu);
		items.add(new Config("*Frequency Stats (time)", null));
		items.add(cpu);

		if (Utils.SHOW_UNIMPLEMENTED_ITEMS) {
			ConfigList cam = new ConfigList("Camera");
			items.add(cam);
			ConfigList scr = new ConfigList("Screen");
			items.add(scr);
			ConfigList opengl = new ConfigList("OpenGL");
			items.add(opengl);
			ConfigList sensors = new ConfigList("Sensors");
			items.add(sensors);
		}

		items.add(getEnvironment());
		items.add(getFeatures());

		if (Utils.SHOW_UNIMPLEMENTED_ITEMS) {
			ConfigList mnt = new ConfigList("Mount points");
			items.add(mnt);
		}

		items.add(getJavaProperties());
		items.add(getMisc());
	}
	
	public SysInfo(Context context) {
		this.context = context;
		batteryChargedFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		batteryReceiver = new BatteryReceiver();
		getContext().registerReceiver(batteryReceiver, batteryChargedFilter);
	}
}
