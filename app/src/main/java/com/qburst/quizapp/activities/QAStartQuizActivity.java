package com.qburst.quizapp.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.databases.QADatabaseOperations;
import com.qburst.quizapp.listeners.QATimerListener;
import com.qburst.quizapp.models.QAQuestions;
import com.qburst.quizapp.utils.QAAlert;
import com.qburst.quizapp.utils.QAProgressIndicator;
import com.qburst.quizapp.utils.QATimerCounter;
import com.qburst.quizapp.utils.QAUtils;

public class QAStartQuizActivity extends QABaseActivity implements
		QATimerListener {

	private static final int MASTER_TIME = 30;
	private static final int EXPERT_TIME = 40;
	private static final int AMATEUR_TIME = 50;
	private static final String AMATEUR = "amateur";
	
	private static final String EXPERT = "expert";
	private static final String MASTER = "master";
	private final int DEFAULT_SCORE = 0;
	private QAQuestions questionFromDatabase = new QAQuestions();
	
	private RadioGroup optionsGroup;
	private RadioButton optionOne;
	private RadioButton optionTwo;
	private RadioButton optionThree;
	
	private RadioButton optionFour;
	private TextView question;
	private Button nextButton;
	private String answer;
	
	private TextView scoreSection;
	private int timeInSeconds;
	private ProgressDialog progressDialog;
	private int questionFetch;
	
	private int questionNumber = 1;
	private int dbSize;
	private QATimerCounter timer;
	private int isOptionsSelected = -1;
	
	public int score;
	public TextView textViewTimer;
	public String difficultyLevel;
	public String tableName;
	private List<Integer> primaryKeys;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getDataFromBundle();
		setContentView(R.layout.section_quiz_layout);
		playMusic(getApplicationContext(), QAConstants.QUIZ_RUNNING_MUSIC);
		setProgressDialog();
		Parse.initialize(this, QAConstants.PARSE_APP_ID,
				QAConstants.PARSE_CLIENT_KEY);
		ParseAnalytics.trackAppOpenedInBackground(getIntent());
		textViewTimer = (TextView) findViewById(R.id.textViewTimer);
		textViewTimer.setText("00:0" + timeInSeconds);
		setTimerValue();
		questionsFetch();
	}

	@Override
	protected void onRestart() {

		super.onRestart();
		playMusic(getApplicationContext(), QAConstants.QUIZ_RUNNING_MUSIC);
	}

	private void setProgressDialog() {

		progressDialog = QAProgressIndicator.progress(QAStartQuizActivity.this);
		progressDialog.setMessage(getString(R.string.loading_questions));
		progressDialog.show();
	}

	private void getDataFromBundle() {

		Bundle bundle = this.getIntent().getExtras();
		tableName = bundle.getString(getString(R.string.section_name));
		difficultyLevel = bundle
				.getString(getString(R.string.difficulty_level));
	}

	private void setTimerValue() {
		switch (difficultyLevel) {
		case AMATEUR:
			timeInSeconds = AMATEUR_TIME;
			break;
		case EXPERT:
			timeInSeconds = EXPERT_TIME;
			break;
		case MASTER:
			timeInSeconds = MASTER_TIME;
			break;
		}
	}

	// Fetching questions from Parse or from database
	public void questionsFetch() {

		score = DEFAULT_SCORE;
		QADatabaseOperations.createNewSection(QAStartQuizActivity.this,
				tableName);
		dbSize = QADatabaseOperations.count(QAStartQuizActivity.this,
				QAConstants.FETCH_ALL + tableName);
		if (dbSize == QAConstants.DB_EMPTY) {
			if (QAUtils.isNetworkAvailable(QAStartQuizActivity.this)) {
				ParseQuery<ParseObject> query = ParseQuery.getQuery(tableName);
				query.findInBackground(new FindCallback<ParseObject>() {
					public void done(List<ParseObject> parse,
							ParseException parseException) {
						if (parseException == null) {
							QADatabaseOperations.insertQuestionsIntoTable(
									QAStartQuizActivity.this, parse, tableName);
							primaryKeys = new ArrayList<Integer>();
							primaryKeys = QADatabaseOperations
									.getPrimaryKeysFromDatabase(
											QAStartQuizActivity.this,
											tableName, difficultyLevel);
							randomIndexGenerator(primaryKeys);
							questionFromDatabase = QADatabaseOperations
									.fetchSingleQuestion(
											QAStartQuizActivity.this,
											tableName, questionFetch);
							QADatabaseOperations.setIsFetchedToTrue(
									QAStartQuizActivity.this, tableName,
									questionFetch);
							setQuizLayout();
						} else {
							parseException.printStackTrace();
						}
					}
				});
			} else {
				Builder dialog = QAAlert.alert(QAStartQuizActivity.this);
				dialog.setTitle(R.string.network_error);
				dialog.setMessage(R.string.network_connection_for_the_first_time);
				dialog.setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int which) {
								dialog.cancel();
								if (QAUtils
										.isNetworkAvailable(QAStartQuizActivity.this)) {
									questionsFetch();
								} else {
									if (progressDialog.isShowing()) {
										progressDialog.dismiss();
									}
									stopMusic(getApplicationContext());
									Intent intent = new Intent(
											QAStartQuizActivity.this,
											QAStarterActivity.class);
									startActivity(intent);
									finish();
								}
							}
						});
				dialog.show();
			}
		} else {
			primaryKeys = new ArrayList<Integer>();
			primaryKeys = QADatabaseOperations
					.getPrimaryKeysFromDatabase(
							QAStartQuizActivity.this,
							tableName, difficultyLevel);
			randomIndexGenerator(primaryKeys);
			questionFromDatabase = QADatabaseOperations.fetchSingleQuestion(
					QAStartQuizActivity.this, tableName, questionFetch);
			QADatabaseOperations.setIsFetchedToTrue(QAStartQuizActivity.this,
					tableName, questionFetch);
			setQuizLayout();
		}
	}

	// Generate a random index to fetch random questions from database
	private void randomIndexGenerator(List<Integer> primaryKeys) {

		Random random = new Random();
		questionFetch = primaryKeys.get(random.nextInt(primaryKeys.size()));
	}

	@Override
	public void onBackPressed() {

		Builder dialog = QAAlert.alert(QAStartQuizActivity.this);
		dialog.setTitle(R.string.return_home);
		dialog.setMessage(R.string.sure);
		dialog.setPositiveButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int which) {
						dialog.cancel();
					}
				});
		dialog.setNegativeButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int which) {
						timer.cancel();
						questionNumber = 1;
						stopMusic(getApplicationContext());
						Intent intent = new Intent(QAStartQuizActivity.this,
								QAStarterActivity.class);
						startActivity(intent);
						finish();
					}
				});
		dialog.show();
	}

	// Handling (fetching and displaying) questions stored in database
	public void setQuizLayout() {

		initQuizView();
		timer = new QATimerCounter(timeInSeconds * 1000, 1000, this);
		scoreSection.setText(String.valueOf(score));
		setQuestionView(questionFromDatabase);
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		} else {
			// Do Nothing
		}
		timer.start();

		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (optionsGroup.getCheckedRadioButtonId() == isOptionsSelected) {
					Builder dialog = QAAlert.alert(QAStartQuizActivity.this);
					dialog.setTitle(R.string.no_answer);
					dialog.setMessage(R.string.select_an_option);
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
					optionsGroup = (RadioGroup) findViewById(R.id.options_group);
					RadioButton userAnswer = (RadioButton) findViewById(optionsGroup
							.getCheckedRadioButtonId());
					if (answer.equals(userAnswer.getText())) {
						score += QAConstants.MARKS_FOR_A_QUESTION;
						if (questionNumber <= (QAConstants.QUESTIONS_IN_A_SECTION)) {
							scoreSection.setText(String.valueOf(score));
							primaryKeys = new ArrayList<Integer>();
							primaryKeys = QADatabaseOperations
									.getPrimaryKeysFromDatabase(
											QAStartQuizActivity.this,
											tableName, difficultyLevel);
							randomIndexGenerator(primaryKeys);
							questionFromDatabase = QADatabaseOperations
									.fetchSingleQuestion(
											QAStartQuizActivity.this,
											tableName, questionFetch);
							QADatabaseOperations.setIsFetchedToTrue(
									QAStartQuizActivity.this, tableName,
									questionFetch);
							setQuestionView(questionFromDatabase);
						} else {
							timer.cancel();
							optionsGroup.clearCheck();
							stopMusic(QAStartQuizActivity.this);
							quizResult();
						}
					} else {
						if (questionNumber <= (QAConstants.QUESTIONS_IN_A_SECTION)) {
							primaryKeys = new ArrayList<Integer>();
							primaryKeys = QADatabaseOperations
									.getPrimaryKeysFromDatabase(
											QAStartQuizActivity.this,
											tableName, difficultyLevel);
							randomIndexGenerator(primaryKeys);
							questionFromDatabase = QADatabaseOperations
									.fetchSingleQuestion(
											QAStartQuizActivity.this,
											tableName, questionFetch);
							QADatabaseOperations.setIsFetchedToTrue(
									QAStartQuizActivity.this, tableName,
									questionFetch);
							setQuestionView(questionFromDatabase);
						} else {
							timer.cancel();
							optionsGroup.clearCheck();
							stopMusic(QAStartQuizActivity.this);
							quizResult();
						}
					}
				}
			}
		});
	}

	// Initialize the quiz layout views
	private void initQuizView() {

		question = (TextView) findViewById(R.id.question);
		optionsGroup = (RadioGroup) findViewById(R.id.options_group);
		optionOne = (RadioButton) findViewById(R.id.option1);
		optionTwo = (RadioButton) findViewById(R.id.option2);
		optionThree = (RadioButton) findViewById(R.id.option3);
		optionFour = (RadioButton) findViewById(R.id.option4);
		nextButton = (Button) findViewById(R.id.next_button);
		scoreSection = (TextView) findViewById(R.id.score_section);
	}

	// Go to Quiz result display page
	public void quizResult() {

		Bundle bundle = new Bundle();
		Intent intent = new Intent(QAStartQuizActivity.this,
				QAQuizResultActivity.class);
		bundle.putInt(getString(R.string.score), score);
		bundle.putString(getString(R.string.section_name), tableName);
		bundle.putString(getString(R.string.difficulty_level), difficultyLevel);
		intent.putExtras(bundle);
		startActivity(intent);
		overridePendingTransition(R.anim.pull_right_in, R.anim.push_left_out);
		finish();
	}

	// Display questions in the view
	public void setQuestionView(QAQuestions questionFromDatabase) {

		if (questionFromDatabase.isFetched() == false) {
			optionsGroup.clearCheck();
			question.setText((questionNumber) + ". "
					+ questionFromDatabase.getQuestion());
			optionOne.setText(questionFromDatabase.getOptionOne());
			optionTwo.setText(questionFromDatabase.getOptionTwo());
			optionThree.setText(questionFromDatabase.getOptionThree());
			optionFour.setText(questionFromDatabase.getOptionFour());
			answer = questionFromDatabase.getAnswer();
			questionNumber++;
		} else {
			primaryKeys = new ArrayList<Integer>();
			primaryKeys = QADatabaseOperations
					.getPrimaryKeysFromDatabase(
							QAStartQuizActivity.this,
							tableName, difficultyLevel);
			randomIndexGenerator(primaryKeys);
			questionFromDatabase = QADatabaseOperations.fetchSingleQuestion(
					QAStartQuizActivity.this, tableName, questionFetch);
			QADatabaseOperations.setIsFetchedToTrue(QAStartQuizActivity.this,
					tableName, questionFetch);
			setQuestionView(questionFromDatabase);
		}
	}

	@Override
	public void timerTicking(String time) {
		textViewTimer.setText(time);
	}

	// When timer finishes, display the quiz result
	@Override
	public void timerFinished() {

		Builder dialog = QAAlert.alert(QAStartQuizActivity.this);
		dialog.setMessage(R.string.timeout);
		dialog.setPositiveButton(R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							final int which) {
						dialog.cancel();
						questionNumber = 1;
						stopMusic(QAStartQuizActivity.this);
						quizResult();
					}
				});
		dialog.show();
	}
}
