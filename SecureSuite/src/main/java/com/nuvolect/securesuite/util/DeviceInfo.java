/*
 * Copyright (c) 2011 - 2015, Nuvolect LLC. All Rights Reserved.
 * All intellectual property rights, including without limitation to
 * copyright and trademark of this work and its derivative works are
 * the property of, or are licensed to, Nuvolect LLC.
 * Any unauthorized use is strictly prohibited.
 */
package com.nuvolect.securesuite.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class DeviceInfo {

	/**
	 * Return a unique string for the device.  This string only changes when you wipe the device
	 * and reinstall Android.
	 * @param context
	 * @return unique device ID string
	 */
	public static String getUniqueInstallId(Context context) {

		String deviceId = Settings.Secure.getString( context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return deviceId;
	}

	/**
	 * Return a somewhat human readable set of device information, OS, etc.
	 * @param context
	 * @return
	 */
	public static JSONObject getDeviceInfo(Context context){

		JSONObject j = new JSONObject();
		try {
			j.put("os", System.getProperty("os.version"));
			j.put("incremental",  android.os.Build.VERSION.INCREMENTAL);
			j.put("sdk", android.os.Build.VERSION.SDK_INT);
			j.put("device", android.os.Build.DEVICE);
			j.put("model", android.os.Build.MODEL);
			j.put("product",android.os.Build.PRODUCT);
			j.put("unique_install_id", getUniqueInstallId(context));
		} catch (JSONException e) {
		}
		return j;
	}

	/**
	 * Retrieves phone model
	 * @return
	 */
	public static String getMakeModelName() {
		String manufacturer = android.os.Build.MANUFACTURER;
		String model = android.os.Build.MODEL;

		if (model.startsWith(manufacturer)) {
			return capitalize(model);
		} else {
			return capitalize(manufacturer) + " " + model;
		}
	}
	//Used for the phone model
	private static String capitalize(String s) {

		if (s == null || s.length() == 0) {
			return "";
		}
		char first = s.charAt(0);
		if (Character.isUpperCase(first)) {
			return s;
		} else {
			return Character.toUpperCase(first) + s.substring(1);
		}
	}

	/**
	 * Test if a specific intent can be run on this device.  They user may not have
	 * GoogleMaps installed, or an app for email.
	 * @param context
	 * @param intent
	 * @return
	 */
	public static boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list =
				packageManager.queryIntentActivities(intent,
						PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}
}
