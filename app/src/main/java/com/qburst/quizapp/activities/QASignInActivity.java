package com.qburst.quizapp.activities;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.utils.QAAlert;
import com.qburst.quizapp.utils.QAPreference;
import com.qburst.quizapp.utils.QAProgressIndicator;
import com.qburst.quizapp.utils.QAUtils;

public class QASignInActivity extends QABaseActivity {
	private SignInButton signInButton;
	private Button noSignInButton;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signin_layout);

		if (QAPreference.getSignedInPreference(this)) {
			setProgressDialog();
		} else {
			// Nothing
		}
		playMusic(getApplicationContext(), QAConstants.MAIN_BACKGROUND_MUSIC);
		initViews();
	}

	@Override
	protected void onRestart() {

		super.onRestart();
		playMusic(getApplicationContext(), QAConstants.MAIN_BACKGROUND_MUSIC);
	}

	private void setProgressDialog() {

		progressDialog = QAProgressIndicator.progress(QASignInActivity.this);
		progressDialog.setMessage(getString(R.string.checking_user));
		progressDialog.show();
		beginUserInitiatedSignIn();
	}

	private void initViews() {

		signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		noSignInButton = (Button) findViewById(R.id.no_sign_in_button);
		
		setListeners();
	}

	private void setListeners() {

		signInButton.setOnClickListener(onClickListener);
		noSignInButton.setOnClickListener(onClickListener);
	}

	public OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.sign_in_button:
				progressDialog = QAProgressIndicator
						.progress(QASignInActivity.this);
				progressDialog
						.setMessage(getString(R.string.running_play_games_));
				progressDialog.show();
				if (QAUtils.isNetworkAvailable(QASignInActivity.this)) {
					beginUserInitiatedSignIn();
				} else {
					Builder dialog = QAAlert.alert(QASignInActivity.this);
					dialog.setTitle(R.string.network_error);
					dialog.setMessage(R.string.check_network_connection);
					dialog.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int which) {
									dialog.cancel();
									if (progressDialog != null) {
										if (progressDialog.isShowing()) {
											progressDialog.dismiss();
										} else {
											// Nothing
										}
									} else {
										// Nothing
									}
								}
							});
					dialog.show();
				}
				break;
			case R.id.no_sign_in_button:
				stopMusic(getApplicationContext());
				Intent intent = new Intent(QASignInActivity.this,
						QAStarterActivity.class);
				intent.putExtra(QAConstants.BACK_FROM_DIFFICULTY_LEVEL, false);
				startActivity(intent);
				overridePendingTransition(R.anim.pull_right_in,
						R.anim.push_left_out);
				finish();
				break;
			}
		}
	};

	@Override
	public void onSignInSucceeded() {

		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			} else {
				// Nothing
			}
		} else {
			// Nothing
		}
		QAPreference.setSignedInPreference(this, true);
		Intent intent = new Intent(QASignInActivity.this,
				QAStarterActivity.class);
		intent.putExtra(QAConstants.BACK_FROM_DIFFICULTY_LEVEL, false);
		startActivity(intent);
		overridePendingTransition(R.anim.pull_right_in, R.anim.push_left_out);
		finish();
	}

	@Override
	public void onSignInFailed() {

	}

	@Override
	protected void onActivityResult(int request, int response, Intent data) {

		if (response == Activity.RESULT_CANCELED) {
			if (progressDialog != null) {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				} else {
					// Nothing
				}
			} else {
				// Nothing
			}
		} else if (response == GamesActivityResultCodes.RESULT_SIGN_IN_FAILED) {
			if (progressDialog != null) {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				} else {
					// Nothing
				}
			} else {
				// Nothing
			}
		}
		super.onActivityResult(request, response, data);
	}
}
