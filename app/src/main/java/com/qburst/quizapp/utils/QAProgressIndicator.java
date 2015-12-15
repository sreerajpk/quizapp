package com.qburst.quizapp.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class QAProgressIndicator {
	public static ProgressDialog progress(Context context) {
		ProgressDialog progress = new ProgressDialog(context);
		progress.setCanceledOnTouchOutside(false);
		progress.setCancelable(false);
		return progress;
	}
}
