package com.qburst.quizapp.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.example.games.basegameutils.GameHelper;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.qburst.quizapp.R;
import com.qburst.quizapp.adapters.QASectionsListAdapter;
import com.qburst.quizapp.adapters.QASectionsListAdapter.ViewHolder;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.databases.QADatabaseHelper;
import com.qburst.quizapp.databases.QADatabaseOperations;
import com.qburst.quizapp.models.QASections;
import com.qburst.quizapp.receivers.QAParseReceiver;
import com.qburst.quizapp.utils.QAAlert;
import com.qburst.quizapp.utils.QAPreference;
import com.qburst.quizapp.utils.QAProgressIndicator;
import com.qburst.quizapp.utils.QAUtils;

public class QAStarterActivity extends QABaseActivity {

	private Button leaderboardButton;
	private Button achievementsButton;
	private ImageView sound;

	private boolean isSoundOn = true;
	private ImageView signInOrOut;
	private int numberOfSections;
	private List<QASections> sectionsListFromDatabase = new ArrayList<QASections>();

	private ImageView playGame;
	private String tableName;
	private ProgressDialog progressDialog;
	private GridView gridView;

	private FrameLayout starterLayout;
	private FrameLayout sectionsLayout;
	private QASectionsListAdapter sectionsListAdapter;

	private boolean isBackFromDifficultyLevel;
	private Animation slideUp;
	private boolean isPlayGameButtonClicked;
	private TextView sectionsLabel;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		createDatabase();
		setContentView(R.layout.qastarter_layout);

		Intent receiveIntent = getIntent();
		isBackFromDifficultyLevel = receiveIntent.getBooleanExtra(
				QAConstants.BACK_FROM_DIFFICULTY_LEVEL, false);

		setProgressDialog();
		initViews();

		if (QAPreference.getSignedInPreference(this)) {
			signInOrOut.setImageResource(R.drawable.sign_out);
		} else {
			signInOrOut.setImageResource(R.drawable.sign_in);
		}

		playMusic(getApplicationContext(), QAConstants.MAIN_BACKGROUND_MUSIC);
		Parse.initialize(this, QAConstants.PARSE_APP_ID,
				QAConstants.PARSE_CLIENT_KEY);

		defineParseReceiver();

		// Fetch sections of quiz from cloud
		sectionsFetch();
	}

	@Override
	protected void onStart() {

		slideUp = AnimationUtils.loadAnimation(QAStarterActivity.this,
				R.anim.slide_up);

		if (isBackFromDifficultyLevel) {
			sectionsLayout.startAnimation(slideUp);
			starterLayout.setVisibility(View.GONE);
			sectionsLayout.setVisibility(View.VISIBLE);
			isBackFromDifficultyLevel = false;
		} else {
			// Do Nothing
		}
		super.onStart();
	}

	@Override
	protected void onRestart() {

		super.onRestart();
		playMusic(getApplicationContext(), QAConstants.MAIN_BACKGROUND_MUSIC);
	}

	private void setProgressDialog() {
		progressDialog = QAProgressIndicator.progress(QAStarterActivity.this);
		progressDialog
				.setMessage(getString(R.string.loading_quiz_please_wait_));
		progressDialog.show();
	}

	private void initViews() {

		leaderboardButton = (Button) findViewById(R.id.leaderboard_button);
		achievementsButton = (Button) findViewById(R.id.achievements_button);
		gridView = (GridView) findViewById(R.id.grid_view);
		playGame = (ImageView) findViewById(R.id.play_game);
		starterLayout = (FrameLayout) findViewById(R.id.starter_layout);
		sectionsLayout = (FrameLayout) findViewById(R.id.sections_layout);
		sound = (ImageView) findViewById(R.id.sound);
		signInOrOut = (ImageView) findViewById(R.id.sign_in_or_out);
		sectionsLabel = (TextView) findViewById(R.id.sections_label);

		setListeners();
	}

	private void setListeners() {

		leaderboardButton.setOnClickListener(onClickListener);
		achievementsButton.setOnClickListener(onClickListener);
		playGame.setOnClickListener(onClickListener);
		sound.setOnClickListener(onClickListener);
		signInOrOut.setOnClickListener(onClickListener);
	}

	@Override
	public void playMusic(Context context, int musicType) {

		super.playMusic(context, musicType);
		if (QAPreference.getSoundPreference(this)) {
			isSoundOn = true;
			sound.setImageResource(R.drawable.sound_on);
		} else {
			isSoundOn = false;
			sound.setImageResource(R.drawable.sound_off);
		}
	}

	@Override
	public void stopMusic(Context context) {
		super.stopMusic(context);
	}

	private void defineParseReceiver() {
		Intent intent = new Intent(this, QASplashScreenActivity.class);
		QAParseReceiver receiver = new QAParseReceiver();
		receiver.getActivity(getApplicationContext(), intent);
	}

	// Fetch sections from Parse
	public void sectionsFetch() {

		numberOfSections = QADatabaseOperations.count(QAStarterActivity.this,
				QAConstants.FETCH_ALL + QAConstants.SECTIONS_TABLE);

		if (numberOfSections > QAConstants.DB_EMPTY) {
			sectionsListFromDatabase = QADatabaseOperations.fetchSectionsList(
					QAStarterActivity.this, QAConstants.FETCH_ALL
							+ QAConstants.SECTIONS_TABLE);
			populateSectionsView(sectionsListFromDatabase);
		} else {
			if (QAUtils.isNetworkAvailable(QAStarterActivity.this)) {

				ParseQuery<ParseObject> query = ParseQuery
						.getQuery(QAConstants.SECTIONS_TABLE);

				// Fetch sections from Parse
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> parse,
							ParseException parseException) {

						if (parseException == null) {
							QADatabaseOperations.insertSectionsIntoTable(
									QAStarterActivity.this, parse,
									QAConstants.SECTIONS_TABLE);
							sectionsListFromDatabase = QADatabaseOperations
									.fetchSectionsList(
											QAStarterActivity.this,
											QAConstants.FETCH_ALL
													+ QAConstants.SECTIONS_TABLE);
							populateSectionsView(sectionsListFromDatabase);
						} else {
							// Alert
							parseException.printStackTrace();
						}
					}
				});
			} else {
				Builder dialog = QAAlert.alert(QAStarterActivity.this);
				dialog.setTitle(R.string.network_error);
				dialog.setMessage(R.string.check_network_connection);
				dialog.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int which) {
								dialog.cancel();
								if (QAUtils
										.isNetworkAvailable(QAStarterActivity.this)) {
									sectionsFetch();
								} else {
									sectionsLabel
											.setText(R.string.no_sections_loaded);
									if (progressDialog.isShowing()) {
										progressDialog.dismiss();
									}
								}
							}
						});
				dialog.show();
			}
		}
	}

	private void populateSectionsView(List<QASections> sectionsList) {

		sectionsListAdapter = new QASectionsListAdapter(this, sectionsList);
		gridView.setAdapter(sectionsListAdapter);
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				tableName = ((ViewHolder) (view.getTag())).getSectionName()
						.getText().toString();
				setDifficultyLevel();
			}
		});

		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}

	public OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.leaderboard_button:
				if (QAUtils.isNetworkAvailable(QAStarterActivity.this)) {
					if (isSignedIn()) {
						startActivityForResult(
								Games.Leaderboards
										.getAllLeaderboardsIntent(getApiClient()),
								QAConstants.REQUEST_LEADERBOARD);
					} else {
						progressDialog = QAProgressIndicator
								.progress(QAStarterActivity.this);
						progressDialog
								.setMessage(getString(R.string.running_play_games_));
						progressDialog.show();
						beginUserInitiatedSignIn();
					}
				} else {
					Builder dialog = QAAlert.alert(QAStarterActivity.this);
					dialog.setTitle(R.string.network_error);
					dialog.setMessage(R.string.check_network_connection);
					dialog.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int which) {
									dialog.cancel();
								}
							});
					dialog.show();
				}
				break;
			case R.id.achievements_button:
				if (QAUtils.isNetworkAvailable(QAStarterActivity.this)) {
					if (isSignedIn()) {
						startActivityForResult(
								Games.Achievements
										.getAchievementsIntent(getApiClient()),
								QAConstants.REQUEST_ACHIEVEMENTS);
						Games.setViewForPopups(getApiClient(),
								findViewById(R.id.achievements_home_popup));
					} else {
						progressDialog = QAProgressIndicator
								.progress(QAStarterActivity.this);
						progressDialog
								.setMessage(getString(R.string.running_play_games_));
						progressDialog.show();
						beginUserInitiatedSignIn();
					}
				} else {
					Builder dialog = QAAlert.alert(QAStarterActivity.this);
					dialog.setTitle(R.string.network_error);
					dialog.setMessage(R.string.check_network_connection);
					dialog.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int which) {
									dialog.cancel();
								}
							});
					dialog.show();
				}
				break;
			case R.id.play_game:
				isPlayGameButtonClicked = true;
				sectionsLayout.startAnimation(slideUp);
				starterLayout.setVisibility(View.GONE);
				sectionsLayout.setVisibility(View.VISIBLE);
				break;
			case R.id.sound:
				if (isSoundOn == true) {
					sound.setImageResource(R.drawable.sound_off);
					isSoundOn = false;
					QAPreference.setSoundPreference(QAStarterActivity.this,
							false);
					stopMusic(getApplicationContext());
				} else {
					sound.setImageResource(R.drawable.sound_on);
					isSoundOn = true;
					QAPreference.setSoundPreference(QAStarterActivity.this,
							true);
					playMusic(getApplicationContext(),
							QAConstants.MAIN_BACKGROUND_MUSIC);
				}
				break;
			case R.id.sign_in_or_out:
				if (QAPreference.getSignedInPreference(QAStarterActivity.this)) {
					signOut();
					QAPreference.setSignedInPreference(QAStarterActivity.this,
							false);
					signInOrOut.setImageResource(R.drawable.sign_in);
				} else {
					progressDialog = QAProgressIndicator
							.progress(QAStarterActivity.this);
					progressDialog
							.setMessage(getString(R.string.running_play_games_));
					progressDialog.show();
					beginUserInitiatedSignIn();
				}
				break;
			}
		}
	};

	private void setDifficultyLevel() {
		stopMusic(getApplicationContext());
		Intent intent = new Intent(QAStarterActivity.this,
				QADifficultyLevelActivity.class);
		intent.putExtra(getString(R.string.section_name), tableName);
		startActivity(intent);
		overridePendingTransition(R.anim.pull_right_in, R.anim.push_left_out);
		finish();
	}

	private void createDatabase() {

		QADatabaseHelper helper = QADatabaseHelper.getInstance(this);
		try {
			helper.createDataBase();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {

		if (isPlayGameButtonClicked == false) {
			Builder dialog = QAAlert.alert(QAStarterActivity.this);
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
		} else {
			sectionsLayout.setVisibility(View.GONE);
			starterLayout.setVisibility(View.VISIBLE);
			isPlayGameButtonClicked = false;
		}
	}

	@Override
	public void onSignInFailed() {
		if (QAPreference.getSignedInPreference(this)) {
			beginUserInitiatedSignIn();
		} else {
			// Nothing
		}
	}

	@Override
	protected void onActivityResult(int req, int res, Intent data) {
		if (res == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
			updateUI();
			GameHelper gamehelper = getGameHelper();
			if (gamehelper != null) {
				gamehelper.disconnect();
			}
		} else if (res == Activity.RESULT_CANCELED) {
			if (progressDialog != null) {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();
				} else {
					// Nothing
				}
			} else {
				// Nothing
			}
		} else {
			// Do Nothing
		}
		super.onActivityResult(req, res, data);
	}

	private void updateUI() {
		signInOrOut.setImageResource(R.drawable.sign_in);
		QAPreference.setSignedInPreference(QAStarterActivity.this, false);
	}

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
		QAPreference.setSignedInPreference(QAStarterActivity.this, true);
		signInOrOut.setImageResource(R.drawable.sign_out);
	}
}