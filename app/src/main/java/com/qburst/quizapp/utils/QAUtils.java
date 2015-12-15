package com.qburst.quizapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class QAUtils {
	
	private static ConnectivityManager connectivityManager;
	private static NetworkInfo networkInfo;

	public static boolean isNetworkAvailable(Context context) {
		connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}
}
