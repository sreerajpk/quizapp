package com.qburst.quizapp.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;

public class QAAlert {

	public static Builder alert(Context context) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
		alertDialog.setCancelable(false);
		return alertDialog;
	}
}
