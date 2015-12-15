package com.qburst.quizapp.application;

import android.app.Application;
import android.content.res.Configuration;
import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.utils.QAUtils;
import io.fabric.sdk.android.Fabric;

public class QAParseApplication extends Application {

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {

		super.onCreate();
		Fabric.with(this, new Crashlytics());
		if (QAUtils.isNetworkAvailable(QAParseApplication.this)) {
			Parse.initialize(getApplicationContext(), QAConstants.PARSE_APP_ID,
					QAConstants.PARSE_CLIENT_KEY);
			ParseInstallation.getCurrentInstallation().saveInBackground();
			ParsePush.subscribeInBackground("", new SaveCallback() {
				@Override
				public void done(ParseException exception) {
					if (exception == null) {

					} else {
						exception.printStackTrace();
					}
				}
			});
		} else {
			// Do Nothing
		}

	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

}