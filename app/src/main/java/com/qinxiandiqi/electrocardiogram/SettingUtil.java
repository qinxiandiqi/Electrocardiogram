package com.qinxiandiqi.electrocardiogram;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;

public class SettingUtil {
	
	static final String PREFERENCES = "preferences";
	static final String SENSITIVITY = "sensitivity"; 
	static final String TABLESIZE = "tableSize";
	static final String FOLDERPATH = "folderPath";
	
	public static int getSensitivity(Activity activity){
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE);
		return preferences.getInt(SENSITIVITY, 50);
	}
	
	public static boolean setSensitivity(Activity activity, int sensitivity){
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE);
		return preferences.edit().putInt(SENSITIVITY, sensitivity).commit();
	}
	
	public static int getTableSize(Activity activity){
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE);
		return preferences.getInt(TABLESIZE, 50);
	}
	
	public static boolean setTableSize(Activity activity, int tableSize){
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE);
		return preferences.edit().putInt(TABLESIZE, tableSize).commit();
	}
	
	public static String getFolderPath(Activity activity){
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE);
		String path = preferences.getString(FOLDERPATH, null);
		if(path == null || path.length() == 0){
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()+"/Electrocardiogram";
		}else return path;
	}
	
	public static boolean setFolderPath(Activity activity, String path){
		SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, Activity.MODE_PRIVATE);
		return preferences.edit().putString(FOLDERPATH, path).commit();
	}
}
