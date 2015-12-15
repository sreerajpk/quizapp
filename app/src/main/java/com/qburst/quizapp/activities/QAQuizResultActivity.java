package com.qburst.quizapp.activities;

import java.io.InputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.games.Games;
import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.databases.QADatabaseOperations;
import com.qburst.quizapp.fragments.QATwitterShareDialogFragment;
import com.qburst.quizapp.fragments.QATwitterShareDialogFragment.ShareDialogListener;
import com.qburst.quizapp.listeners.QATwitterAccessTokenUpdatedListener;
import com.qburst.quizapp.utils.QAAlert;
import com.qburst.quizapp.utils.QAPreference;
import com.qburst.quizapp.utils.QAProgressIndicator;
import com.qburst.quizapp.utils.QAUtils;

public class QAQuizResultActivity extends QABaseActivity implements
		ShareDialogListener, AnimationListener,
		QATwitterAccessTokenUpdatedListener {

	private static final int SHOW_NOT_SIGNED_IN = 1;
	private static final int NOT_SIGNED_IN_ALREADY_SHOWN = 2;
	private static final int SHOW_PLAY_GAMES_SUBMIT_ERROR = 1;
	private static final int PLAY_GAMES_SUBMIT_ERROR_ALREADY_SHOWN = 2;
	private static final int PLAY_GAMES_SUBMIT_SUCCESS = -1;
	private static final String PERMISSION = "email";
	private static final int WEBVIEW_REQUEST_CODE = 100;

	private static final String SHARE_FRAGMENT_TAG = "QATwitterShareDialogFragment";
	private static final String POST_ID = "post_id";
	private static final String FEED_DIALOG_NAME = "name";
	private static final String FEED_DIALOG_CAPTION = "caption";

	private static final String FEED_DIALOG_DESCRIPTION = "description";
	private static final String FEED_DIALOG_LINK = "link";
	private static final String FEED_DIALOG_PICTURE = "picture";
	private TextView scoreTextView;

	private TextView pointsScored;
	private TextView sectionNameTextView;
	private Button playAgainButton;
	private Button facebookShareButton;

	private Button twitterShareButton;
	private UiLifecycleHelper uiHelper;
	private boolean canPresentShareDialog;
	private String sectionName;

	private int score;
	private String difficultyLevel;
	private int animationCount = 0;
	private ProgressDialog progressDialog;

	private Twitter twitter;
	private RequestToken requestToken;
	private static SharedPreferences sharedPreferences;
	private String appDefaultStatus;

	private boolean isShareToFacebookButtonClicked = false;
	private boolean isShareToTwitterButtonClicked = false;
	private ImageView trophy;
	private Session session;

	private Session.StatusCallback statusCallback = new SessionStatusCallback();
	private int showingNotSignedInForFirstTime;
	private int showingPlayGamesSubmitErrorForFirstTime;
	private QATwitterAccessTokenUpdatedListener listener;

	private Animation sectionNameAnimation;
	private Animation scoreTextViewAnimation;
	private Animation pointsScoredAnimation;
	private Animation facebookAnimation;
	private Animation twitterAnimation;
	private Animation playAgainAnimation;
	private Animation trophyAnimation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showingNotSignedInForFirstTime = SHOW_NOT_SIGNED_IN;
		showingPlayGamesSubmitErrorForFirstTime = SHOW_PLAY_GAMES_SUBMIT_ERROR;

		uiHelper = new UiLifecycleHelper(this, statusCallback);
		uiHelper.onCreate(savedInstanceState);

		setContentView(R.layout.quiz_result_layout);
		getDataFromBundle();
		initViews();
		playMusic(getApplicationContext(), QAConstants.QUIZ_RESULT_MUSIC);
		createAppDefaultStatus();
		setAnimationForFields();

		// Set the isFetched field for all questions of the attended section to
		// 0
		QADatabaseOperations.setIsFetchedToFalse(QAQuizResultActivity.this,
				sectionName);
		checkIfTwitterLoggedIn();
		handleFacebookSession(savedInstanceState);
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
	}

	@Override
	protected void onStart() {

		sharedPreferences = getSharedPreferences(
				QAConstants.SETTINGS_PREF_NAME, Context.MODE_PRIVATE);
		super.onStart();
		Session.getActiveSession().addCallback(statusCallback);
	}

	@Override
	protected void onResume() {

		super.onResume();
		AppEventsLogger.activateApp(this);
	}

	@Override
	public void onPause() {

		super.onPause();
		AppEventsLogger.deactivateApp(this);
	}

	@Override
	protected void onStop() {

		super.onStop();
		Session.getActiveSession().removeCallback(statusCallback);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);
		Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// Get data from QAStartQuizActivity
	private void getDataFromBundle() {

		Bundle bundle = getIntent().getExtras();
		score = bundle.getInt(getString(R.string.score));
		sectionName = bundle.getString(getString(R.string.section_name));
		difficultyLevel = bundle
				.getString(getString(R.string.difficulty_level));
	}

	private void initViews() {

		sectionNameTextView = (TextView) findViewById(R.id.section_name);
		pointsScored = (TextView) findViewById(R.id.points_scored_label);
		scoreTextView = (TextView) findViewById(R.id.score_result);
		facebookShareButton = (Button) findViewById(R.id.facebook_share_button);
		twitterShareButton = (Button) findViewById(R.id.twitter_share_button);
		playAgainButton = (Button) findViewById(R.id.play_again_button);
		trophy = (ImageView) findViewById(R.id.trophy);

		// Set views invisible
		sectionNameTextView.setVisibility(View.GONE);
		pointsScored.setVisibility(View.GONE);
		scoreTextView.setVisibility(View.GONE);
		facebookShareButton.setVisibility(View.GONE);
		twitterShareButton.setVisibility(View.GONE);
		playAgainButton.setVisibility(View.GONE);
		trophy.setVisibility(View.GONE);

		sectionNameTextView.setText(sectionName);
		scoreTextView.setText(String.valueOf(score));

		playAgainButton.setOnClickListener(onClickListener);
		twitterShareButton.setOnClickListener(onClickListener);
		facebookShareButton.setOnClickListener(onClickListener);
	}

	private void createAppDefaultStatus() {
		StringBuilder stringBuilder = new StringBuilder(100);
		stringBuilder.append(getString(R.string.app_default_status_part1))
				.append(score)
				.append(getString(R.string.app_default_status_part2));
		appDefaultStatus = stringBuilder.toString();
	}

	private void checkIfTwitterLoggedIn() {

		if (QAPreference.getTwitterLoggedInPreference(this)) {
			// Nothing
		} else {
			Uri uri = getIntent().getData();
			if (uri != null
					&& uri.toString().startsWith(
							QAConstants.TWITTER_CALLBACK_URL)) {
				String verifier = uri
						.getQueryParameter(QAConstants.TWITTER_OAUTH_VERIFIER);
				try {
					// Getting oAuth authentication token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);
					// Save updated token
					saveTwitterInfo(accessToken);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	private void handleFacebookSession(Bundle savedInstanceState) {

		session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, statusCallback,
						savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			session.addCallback(statusCallback);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(
						statusCallback).setPermissions(PERMISSION));
			}
		} else {
			// Nothing
		}
	}

	// Play again, Twitter and Facebook button click listeners
	public OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {
			switch (view.getId()) {
			case R.id.play_again_button:
				stopMusic(getApplicationContext());
				Intent intent = new Intent(QAQuizResultActivity.this,
						QAStarterActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.pull_left_in,
						R.anim.push_right_out);
				finish();
				break;
			case R.id.twitter_share_button:
				progressDialog = QAProgressIndicator
						.progress(QAQuizResultActivity.this);
				progressDialog.setMessage(getString(R.string.starting_twitter));
				progressDialog.show();
				isShareToTwitterButtonClicked = true;
				isShareToFacebookButtonClicked = false;
				if (QAUtils.isNetworkAvailable(QAQuizResultActivity.this)) {
					if (!QAPreference
							.getTwitterLoggedInPreference(QAQuizResultActivity.this)) {
						new LoginToTwitter().execute();
					} else {
						Bundle arguments = new Bundle();
						arguments.putString(QAConstants.DEFAULT_STATUS,
								appDefaultStatus);
						QATwitterShareDialogFragment dialog = QATwitterShareDialogFragment
								.newInstance();
						dialog.setArguments(arguments);
						dialog.show(
								QAQuizResultActivity.this.getFragmentManager(),
								SHARE_FRAGMENT_TAG);
					}
				} else {
					Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
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
											// Do Nothing
										}
									} else {
										// Do Nothing
									}
								}
							});
					dialog.show();
				}
				break;
			case R.id.facebook_share_button:
				progressDialog = QAProgressIndicator
						.progress(QAQuizResultActivity.this);
				progressDialog
						.setMessage(getString(R.string.starting_facebook));
				progressDialog.show();
				isShareToFacebookButtonClicked = true;
				isShareToTwitterButtonClicked = false;
				if (QAUtils.isNetworkAvailable(QAQuizResultActivity.this)) {
					session = Session.getActiveSession();
					Session.openActiveSession(QAQuizResultActivity.this, true,
							statusCallback);
				} else {
					Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
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
											// Do Nothing
										}
									} else {
										// Do Nothing
									}
								}
							});
					dialog.show();
				}
				break;
			}
		}
	};

	// Initialize animations and call start animation
	private void setAnimationForFields() {

		sectionNameAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_left);
		pointsScoredAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_left);
		scoreTextViewAnimation = AnimationUtils.loadAnimation(this,
				R.anim.bounce_animation);
		facebookAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_left);
		twitterAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_left);
		playAgainAnimation = AnimationUtils.loadAnimation(this,
				R.anim.slide_in_from_left);
		trophyAnimation = AnimationUtils.loadAnimation(this,
				R.anim.bounce_trophy);

		sectionNameAnimation.setAnimationListener(this);
		pointsScoredAnimation.setAnimationListener(this);
		scoreTextViewAnimation.setAnimationListener(this);
		facebookAnimation.setAnimationListener(this);
		twitterAnimation.setAnimationListener(this);
		playAgainAnimation.setAnimationListener(this);
		trophyAnimation.setAnimationListener(this);

		animationCount = 1;
		sectionNameTextView.startAnimation(sectionNameAnimation);
		sectionNameTextView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onBackPressed() {

		Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
		dialog.setMessage(R.string.exit_the_game);
		dialog.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int which) {
						stopMusic(getApplicationContext());
						finish();
					}
				});
		dialog.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int which) {
						dialog.cancel();
					}
				});
		dialog.show();
	}

	// Handle session changes
	private class SessionStatusCallback implements Session.StatusCallback {

		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			// Check if Session is Opened or not
			processSessionStatus(session, state, exception);
		}
	}

	// Handle Facebook session change
	private void processSessionStatus(Session session, SessionState state,
			Exception exception) {

		if (session != null && session.isOpened()) {

			if (session.getPermissions().contains(PERMISSION)) {
				if (progressDialog != null) {
					if (progressDialog.isShowing()) {
						progressDialog.dismiss();
					} else {
						// Do Nothing
					}
				} else {
					// Do Nothing
				}
				progressDialog = QAProgressIndicator
						.progress(QAQuizResultActivity.this);
				progressDialog
						.setMessage(getString(R.string.sharing_to_facebook));
				progressDialog.show();
				// Show Progress Dialog
				if (canPresentShareDialog) {
					FacebookDialog shareDialog = createShareDialogBuilderForLink()
							.build();
					uiHelper.trackPendingDialogCall(shareDialog.present());
					if (progressDialog != null) {
						if (progressDialog.isShowing()) {
							progressDialog.dismiss();
						} else {
							// Do Nothing
						}
					} else {
						// Do Nothing
					}
				} else {
					publishFeedDialog();
					if (progressDialog != null) {
						if (progressDialog.isShowing()) {
							progressDialog.dismiss();
						} else {
							// Do Nothing
						}
					} else {
						// Do Nothing
					}
				}
			} else {
				session.requestNewReadPermissions(new Session.NewPermissionsRequest(
						QAQuizResultActivity.this, PERMISSION));
			}
		} else {
			// Do Nothing
		}
	}

	// Save twitter login info
	private void saveTwitterInfo(AccessToken accessToken) {

		long userID = accessToken.getUserId();
		User user;
		try {
			user = twitter.showUser(userID);
			String username = user.getName();
			// Storing oAuth tokens to shared preferences
			QAPreference.setTwitterLoggedInDetailsPreference(this, accessToken,
					username);
		} catch (TwitterException twitterException) {
			twitterException.printStackTrace();
		}
	}

	// Login to twitter
	class LoginToTwitter extends AsyncTask<String, String, RequestToken> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			} else {
				// Nothing
			}
			progressDialog = QAProgressIndicator
					.progress(QAQuizResultActivity.this);
			progressDialog
					.setMessage(getString(R.string.logging_in_to_twitter));
			progressDialog.show();
		}

		@Override
		protected RequestToken doInBackground(String... message) {

			final ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(QAConstants.TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(QAConstants.TWITTER_CONSUMER_SECRET);
			final Configuration configuration = builder.build();
			final TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();
			try {
				requestToken = twitter
						.getOAuthRequestToken(QAConstants.TWITTER_CALLBACK_URL);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return requestToken;
		}

		@Override
		protected void onPostExecute(RequestToken requestToken) {

			try {
				// Loading twitter login page on webview
				final Intent intent = new Intent(QAQuizResultActivity.this,
						QATwitterShareWebViewActivity.class);
				intent.putExtra(QAConstants.TWITTER_AUTHENTICATION_URL,
						requestToken.getAuthenticationURL());
				startActivityForResult(intent, WEBVIEW_REQUEST_CODE);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	// Update status in twitter
	class UpdateTwitterStatus extends AsyncTask<String, String, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			} else {
				// Do Nothing
			}
			progressDialog = QAProgressIndicator
					.progress(QAQuizResultActivity.this);
			progressDialog.setMessage(getString(R.string.posting_to_twitter));
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(String... message) {

			String title = message[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(QAConstants.TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(QAConstants.TWITTER_CONSUMER_SECRET);
				// Access Token
				String access_token = sharedPreferences.getString(
						QAConstants.PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = sharedPreferences.getString(
						QAConstants.PREF_KEY_OAUTH_SECRET, "");
				AccessToken accessToken = new AccessToken(access_token,
						access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build())
						.getInstance(accessToken);
				// Update status
				StatusUpdate statusUpdate = new StatusUpdate(title + " "
						+ appDefaultStatus);
				InputStream image = getResources().openRawResource(
						R.drawable.ic_launcher);
				statusUpdate.setMedia("test.jpg", image);
				twitter.updateStatus(statusUpdate);
			} catch (TwitterException twitterException) {
				twitterException.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			// Dismiss the progress dialog after sharing
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			} else {
				// Do Nothing
			}
			Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
			dialog.setTitle(R.string.success);
			dialog.setMessage(R.string.successfully_posted_to_twitter);
			dialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.cancel();
							if (progressDialog != null) {
								if (progressDialog.isShowing()) {
									progressDialog.dismiss();
								} else {
									// Do Nothing
								}
							} else {
								// Do Nothing
							}
						}
					});
			dialog.show();
		}
	}

	private FacebookDialog.ShareDialogBuilder createShareDialogBuilderForLink() {
		return new FacebookDialog.ShareDialogBuilder(this)
				.setName(getString(R.string.app_name))
				.setPicture(QAConstants.FACEBOOK_APP_ICON_URL)
				.setDescription(appDefaultStatus)
				.setLink(QAConstants.FACEBOOK_REDIRECT_URL);
	}

	// Display feed dialog when facebook app is not present
	private void publishFeedDialog() {

		Bundle parameters = new Bundle();
		parameters.putString(FEED_DIALOG_NAME, getString(R.string.app_name));
		parameters.putString(FEED_DIALOG_CAPTION, appDefaultStatus);
		parameters.putString(FEED_DIALOG_DESCRIPTION, "");
		parameters.putString(FEED_DIALOG_LINK,
				QAConstants.FACEBOOK_REDIRECT_URL);
		parameters.putString(FEED_DIALOG_PICTURE,
				QAConstants.FACEBOOK_APP_ICON_URL);

		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
				QAQuizResultActivity.this, Session.getActiveSession(),
				parameters)).setOnCompleteListener(new OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) {
					// Success, status updated
					final String postId = values.getString(POST_ID);
					if (postId != null) {
						Builder dialog = QAAlert
								.alert(QAQuizResultActivity.this);
						dialog.setTitle(getString(R.string.success));
						dialog.setMessage(getString(R.string.successfully_posted));
						dialog.setPositiveButton(R.string.ok,
								new DialogInterface.OnClickListener() {
									public void onClick(
											final DialogInterface dialog,
											final int which) {
										dialog.cancel();
									}
								});
						dialog.show();
					} else {
						// User clicked the Cancel button
						Builder dialog = QAAlert
								.alert(QAQuizResultActivity.this);
						dialog.setMessage(R.string.cancelled);
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
				} else if (error instanceof FacebookOperationCanceledException) {
					// User clicked the "x" button
					Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
					dialog.setMessage(R.string.cancelled);
					dialog.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										final int which) {
									dialog.cancel();
								}
							});
					dialog.show();
				} else {
					// Network error
					Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
					dialog.setTitle(R.string.error);
					dialog.setMessage(R.string.network_error);
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
			}
		}).build();
		feedDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			} else {
				// Do Nothing
			}
		} else {
			// Do Nothing
		}
		if (isShareToTwitterButtonClicked) {
			if (resultCode == Activity.RESULT_OK) {
				String verifier = data.getExtras().getString(QAConstants.TWITTER_OAUTH_VERIFIER);
				new AccessTokenUpdate().execute(verifier);
			}
			isShareToTwitterButtonClicked = false;
		}
		super.onActivityResult(requestCode, resultCode, data);
		if (isShareToFacebookButtonClicked) {
			isShareToFacebookButtonClicked = false;
			Session.getActiveSession().onActivityResult(
					QAQuizResultActivity.this, requestCode, resultCode, data);
		}
	}

	class AccessTokenUpdate extends AsyncTask<String, String, Void> {
		
		private String verifier;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... message) {

			verifier = message[0];
			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(
						requestToken, verifier);
				saveTwitterInfo(accessToken);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			listener = QAQuizResultActivity.this;
			listener.twitterAccessTokenUpdated();
			super.onPostExecute(result);
		}
	}

	@Override
	public void twitterAccessTokenUpdated() {

		Bundle arguments = new Bundle();
		arguments.putString(QAConstants.DEFAULT_STATUS, appDefaultStatus);
		QATwitterShareDialogFragment dialog = QATwitterShareDialogFragment
				.newInstance();
		dialog.setArguments(arguments);
		dialog.show(QAQuizResultActivity.this.getFragmentManager(),
				SHARE_FRAGMENT_TAG);
	}

	public void submitScoreAndUnlockAchievement() {

		if (isSignedIn()) {
			showingPlayGamesSubmitErrorForFirstTime = PLAY_GAMES_SUBMIT_SUCCESS;
			Games.Leaderboards.submitScore(getApiClient(),
					QAConstants.QUIZAPP_SCORE_LEADERBOARD, score);
			int totalMarks = QAConstants.QUESTIONS_IN_A_SECTION
					* QAConstants.MARKS_FOR_A_QUESTION;
			if (score >= (QAConstants.FRACTION_OF_POINTS_TO_USE_FOR_FREQUENT_PLAYER_INCREMENT * totalMarks)) {
				Games.Achievements.increment(getApiClient(),
						QAConstants.QUIZAPP_FREQUENT_PLAYER_ACHIEVEMENT,
						QAConstants.STEPS_TO_INCREMENT);
			} else {
				// Do Nothing
			}
			if (difficultyLevel.equals(QAConstants.AMATEUR)
					&& score == QAConstants.QUESTIONS_IN_A_SECTION
							* QAConstants.MARKS_FOR_A_QUESTION) {
				Games.Achievements.unlock(getApiClient(),
						QAConstants.QUIZAPP_AMATEUR_ACHIEVEMENT);
			} else if (difficultyLevel.equals(QAConstants.EXPERT)
					&& score == QAConstants.QUESTIONS_IN_A_SECTION
							* QAConstants.MARKS_FOR_A_QUESTION) {
				Games.Achievements.unlock(getApiClient(),
						QAConstants.QUIZAPP_EXPERT_ACHIEVEMENT);
			} else if (difficultyLevel.equals(QAConstants.MASTER)
					&& score == QAConstants.QUESTIONS_IN_A_SECTION
							* QAConstants.MARKS_FOR_A_QUESTION) {
				Games.Achievements.unlock(getApiClient(),
						QAConstants.QUIZAPP_MASTER_ACHIEVEMENT);
			} else {
				// No achievement unlocked
			}
		} else {
			Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
			dialog.setTitle(R.string.sign_in_to_submit_score);
			dialog.setMessage(R.string.want_to_sign_in);
			dialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							beginUserInitiatedSignIn();
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
	}

	@Override
	protected void onRestart() {

		super.onRestart();
		playMusic(getApplicationContext(), QAConstants.QUIZ_RESULT_MUSIC);
	}

	@Override
	public void onSignInFailed() {

		if (QAUtils.isNetworkAvailable(QAQuizResultActivity.this)) {
			if (QAPreference.getSignedInPreference(this)) {
				beginUserInitiatedSignIn();
			} else {
				// Do Nothing
			}
			if (showingNotSignedInForFirstTime == SHOW_NOT_SIGNED_IN
					&& !QAPreference.getSignedInPreference(this)) {
				showingNotSignedInForFirstTime = NOT_SIGNED_IN_ALREADY_SHOWN;
				Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
				dialog.setTitle(R.string.sign_in_to_submit_score);
				dialog.setMessage(R.string.want_to_sign_in);
				dialog.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int which) {
								beginUserInitiatedSignIn();
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
		} else {
			if (showingPlayGamesSubmitErrorForFirstTime == SHOW_PLAY_GAMES_SUBMIT_ERROR) {
				showingPlayGamesSubmitErrorForFirstTime = PLAY_GAMES_SUBMIT_ERROR_ALREADY_SHOWN;
				Builder dialog = QAAlert.alert(QAQuizResultActivity.this);
				dialog.setTitle(R.string.network_error);
				dialog.setMessage(R.string.play_games_submit_error);
				dialog.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int which) {
								dialog.cancel();
							}
						});
				dialog.show();
			} else {
				// Do Nothing
			}
		}
	}

	@Override
	public void onSignInSucceeded() {

		QAPreference.setSignedInPreference(this, true);
		submitScoreAndUnlockAchievement();
	}

	@Override
	public void onTweetShareClick(DialogFragment dialog, String title) {

		new UpdateTwitterStatus().execute(title, appDefaultStatus);
	}

	@Override
	public void OnTweetCancel(DialogFragment dialog) {

		if (progressDialog != null) {
			if (progressDialog.isShowing()) {
				progressDialog.dismiss();
			} else {
				// Do Nothing
			}
		} else {
			// Do Nothing
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {

	}

	@Override
	public void onAnimationEnd(Animation animation) {

		switch (animationCount) {
		case 1:
			pointsScored.startAnimation(pointsScoredAnimation);
			pointsScored.setVisibility(View.VISIBLE);
			animationCount++;
			break;
		case 2:
			scoreTextView.startAnimation(scoreTextViewAnimation);
			scoreTextView.setVisibility(View.VISIBLE);
			animationCount++;
			break;
		case 3:
			facebookShareButton.startAnimation(facebookAnimation);
			facebookShareButton.setVisibility(View.VISIBLE);
			animationCount++;
			break;
		case 4:
			twitterShareButton.startAnimation(twitterAnimation);
			twitterShareButton.setVisibility(View.VISIBLE);
			animationCount++;
			break;
		case 5:
			playAgainButton.startAnimation(playAgainAnimation);
			playAgainButton.setVisibility(View.VISIBLE);
			animationCount++;
			break;
		case 6:
			if (score >= (QAConstants.FRACTION_OF_POINTS_TO_USE_FOR_DISPLAYING_TROPHY
					* QAConstants.QUESTIONS_IN_A_SECTION * QAConstants.MARKS_FOR_A_QUESTION)) {
				trophy.startAnimation(trophyAnimation);
				trophy.setVisibility(View.VISIBLE);
				animationCount++;
			} else {
				// Nothing
			}
			break;
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {

	}
}
