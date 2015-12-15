package com.qburst.quizapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.example.games.basegameutils.BaseGameActivity;
import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;

public class QASplashScreenActivity extends BaseGameActivity {

	private ImageView appLogo;
	private TextView appName;
	private Animation imageAnimation;
	private Animation appNameAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.splashscreen_layout);

		initViews();

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// This method will be executed once the timer is over
				// Start your app main activity
				Intent intent = new Intent(QASplashScreenActivity.this,
						QASignInActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.pull_right_in,
						R.anim.push_left_out);
				finish();
			}
		}, QAConstants.SPLASH_TIME_OUT);
	}

	private void initViews() {

		appLogo = (ImageView) this.findViewById(R.id.logo);
		appName = (TextView) this.findViewById(R.id.application_name);

		setAnimation();
	}

	private void setAnimation() {
		imageAnimation = AnimationUtils.loadAnimation(this,
				R.anim.rotate_around_center);
		appLogo.startAnimation(imageAnimation);

		appNameAnimation = AnimationUtils.loadAnimation(this,
				R.anim.text_animation);
		appName.startAnimation(appNameAnimation);
	}

	@Override
	public void onSignInFailed() {

	}

	@Override
	public void onSignInSucceeded() {

	}
}