package com.qburst.quizapp.activities;

import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.utils.QAAlert;

public class QADifficultyLevelActivity extends QABaseActivity {

	private Button amateurButton;
	private Button expertButton;
	private Button masterButton;
	private ImageButton backButton;
	private String difficultyLevel;
	private String tableName;
	SharedPreferences backFromDifficultyLevel;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		tableName = intent.getStringExtra(getString(R.string.section_name));

		setContentView(R.layout.difficulty_level_layout);
		playMusic(getApplicationContext(), QAConstants.MAIN_BACKGROUND_MUSIC);
		initViews();
	}

	@Override
	protected void onRestart() {

		super.onRestart();
		playMusic(getApplicationContext(), QAConstants.MAIN_BACKGROUND_MUSIC);
	}

	// Initialize views
	private void initViews() {

		amateurButton = (Button) findViewById(R.id.amateur_button);
		expertButton = (Button) findViewById(R.id.expert_button);
		masterButton = (Button) findViewById(R.id.master_button);
		backButton = (ImageButton) findViewById(R.id.back_button);

		setListeners();
	}

	// Set click listeners for buttons
	private void setListeners() {

		amateurButton.setOnClickListener(onClickListener);
		expertButton.setOnClickListener(onClickListener);
		masterButton.setOnClickListener(onClickListener);
		backButton.setOnClickListener(onClickListener);
	}

	@Override
	public void onBackPressed() {

		Builder dialog = QAAlert.alert(QADifficultyLevelActivity.this);
		dialog.setTitle(R.string.exit_game);
		dialog.setMessage(R.string.exit_game_dialog);
		dialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int which) {
						stopMusic(getApplicationContext());
						finish();
					}
				});
		dialog.setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int which) {
						dialog.cancel();
					}
				});
		dialog.show();
	}

	// Selecting Difficulty Level for the game
	public OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.amateur_button:
				difficultyLevel = getString(R.string.amateur);
				startQuiz();
				break;
			case R.id.expert_button:
				difficultyLevel = getString(R.string.expert);
				startQuiz();
				break;
			case R.id.master_button:
				difficultyLevel = getString(R.string.master);
				startQuiz();
				break;
			case R.id.back_button:
				// Go to home page
				stopMusic(getApplicationContext());
				Intent intent = new Intent(QADifficultyLevelActivity.this,
						QAStarterActivity.class);
				intent.putExtra(QAConstants.BACK_FROM_DIFFICULTY_LEVEL, true);
				startActivity(intent);
				overridePendingTransition(R.anim.pull_left_in,
						R.anim.push_right_out);
				finish();
			}
		}
	};

	// Start Quiz
	private void startQuiz() {

		stopMusic(getApplicationContext());
		Bundle bundle = new Bundle();
		Intent intent = new Intent(QADifficultyLevelActivity.this, QAStartQuizActivity.class);
		bundle.putString(getString(R.string.section_name), tableName);
		bundle.putString(getString(R.string.difficulty_level), difficultyLevel);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.pull_right_in, R.anim.push_left_out);
		finish();
	}
}
