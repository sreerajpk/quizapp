package com.qburst.quizapp.utils;

import com.qburst.quizapp.constants.QAConstants;

import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class QAPreference {

	private static SharedPreferences settings;

	public static boolean getSignedInPreference(Context context) {
		settings = context.getSharedPreferences(QAConstants.SETTINGS_PREF_NAME,
				Context.MODE_PRIVATE);
		return settings.getBoolean("signedIn", false);
	}

	public static void setSignedInPreference(Context context, boolean signedIn) {
		settings = context.getSharedPreferences(QAConstants.SETTINGS_PREF_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("signedIn", signedIn);
		editor.commit();
	}

	public static boolean getSoundPreference(Context context) {
		settings = context.getSharedPreferences(QAConstants.SETTINGS_PREF_NAME,
				Context.MODE_PRIVATE);
		return settings.getBoolean("sound", true);
	}

	public static void setSoundPreference(Context context, boolean sound) {
		settings = context.getSharedPreferences(QAConstants.SETTINGS_PREF_NAME,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("sound", sound);
		editor.commit();
	}
	
	public static boolean getTwitterLoggedInPreference(Context context){
		settings = context.getSharedPreferences(QAConstants.SETTINGS_PREF_NAME, Context.MODE_PRIVATE);
		return settings.getBoolean(QAConstants.PREF_KEY_TWITTER_LOGIN, false);
	}
	public static void setTwitterLoggedInDetailsPreference(Context context, AccessToken accessToken, String username){
		Editor editor = settings.edit();
		editor.putString(QAConstants.PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
		editor.putString(QAConstants.PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
		editor.putBoolean(QAConstants.PREF_KEY_TWITTER_LOGIN, true);
		editor.putString(QAConstants.PREF_USER_NAME, username);
		editor.commit();
	}
}
