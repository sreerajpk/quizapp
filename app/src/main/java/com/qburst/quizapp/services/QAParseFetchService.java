package com.qburst.quizapp.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.databases.QADatabaseOperations;
import com.qburst.quizapp.listeners.QAParseOperationListener;
import com.qburst.quizapp.models.QAQuestions;
import com.qburst.quizapp.models.QASections;
import com.qburst.quizapp.utils.QAAlert;
import com.qburst.quizapp.utils.QAUtils;

public class QAParseFetchService extends Service implements
		QAParseOperationListener {
	
	private static final String UPDATED_AT = "updatedAt";
	private static final String SECTION_NAME = "sectionName";
	private static final String PRIMARY_KEY = "primaryKey";
	private static final int FIRST_ELEMENT = 0;
	
	private static final int PARSE_OBJECT_LIST_EMPTY = 0;
	private List<QASections> sectionsListFromDatabase = new ArrayList<QASections>();
	private List<QAQuestions> questionsListFromDatabase = new ArrayList<QAQuestions>();
	private String time;
	
	private SimpleDateFormat format;
	private Date dateInDatabase = null;
	private String tableName;
	private int dbSize;
	
	private int primaryKey;
	private boolean isQuestionPresent;
	private List<ParseObject> listToAddWhenNotPresent;
	private QAParseOperationListener parselistener;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Parse.initialize(this, QAConstants.PARSE_APP_ID,
				QAConstants.PARSE_CLIENT_KEY);
		parselistener = this;
		sectionsListFromDatabase = QADatabaseOperations.fetchSectionsList(this,
				QAConstants.FETCH_ALL + QAConstants.SECTIONS_TABLE);
		time = sectionsListFromDatabase.get(FIRST_ELEMENT).getUpdatedAt();
		format = new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZ yyyy",
				Locale.getDefault());
		try {
			dateInDatabase = format.parse(time);
		} catch (java.text.ParseException error) {
			error.printStackTrace();
		}
		if (QAUtils
				.isNetworkAvailable(QAParseFetchService.this)) {
			ParseQuery<ParseObject> sectionQuery = ParseQuery
					.getQuery(QAConstants.SECTIONS_TABLE);
			sectionQuery.whereGreaterThan(UPDATED_AT, dateInDatabase);
			sectionQuery.findInBackground(new FindCallback<ParseObject>() {
				public void done(List<ParseObject> parse, ParseException parseException) {
					if (parseException == null) {
						QADatabaseOperations.insertSectionsIntoTable(
								QAParseFetchService.this, parse,
								QAConstants.SECTIONS_TABLE);
						for (ParseObject section : parse) {
							QADatabaseOperations.createNewSection(
									QAParseFetchService.this,
									section.getString(SECTION_NAME));
						}
						parselistener.serviceSectionsFetched();
					} else {
						parseException.printStackTrace();
					}
				}
			});
		} else {
			Builder dialog = QAAlert.alert(QAParseFetchService.this);
			dialog.setTitle(R.string.network_error);
			dialog.setMessage(R.string.check_network_connection);
			dialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.cancel();
						}
					});
			dialog.show();
		}
		this.stopSelf();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException(getString(R.string.not_yet_implemented));
	}

	@Override
	public void onCreate() {
		
	}

	@Override
	public void onDestroy() {
		
	}

	@Override
	public void serviceSectionsFetched() {
		if (QAUtils
				.isNetworkAvailable(QAParseFetchService.this)) {
			sectionsListFromDatabase = QADatabaseOperations.fetchSectionsList(
					QAParseFetchService.this, QAConstants.FETCH_ALL
							+ QAConstants.SECTIONS_TABLE);
			for (QASections section : sectionsListFromDatabase) {
				tableName = section.getSectionName();
				QADatabaseOperations.createNewSection(QAParseFetchService.this,
						tableName);
				dbSize = QADatabaseOperations.count(QAParseFetchService.this,
						QAConstants.FETCH_ALL + tableName);
				// For a section that is newly added through push notification
				// or that is empty
				if (dbSize == QAConstants.DB_EMPTY) {
					ParseQuery<ParseObject> questionQuery = ParseQuery
							.getQuery(tableName);
					questionQuery
							.findInBackground(new FindCallback<ParseObject>() {
								public void done(List<ParseObject> parse,
										ParseException parseException) {
									if (parseException == null) {
										QADatabaseOperations.createNewSection(
												QAParseFetchService.this, parse
														.get(FIRST_ELEMENT).getClassName());
										QADatabaseOperations
												.insertQuestionsIntoTable(
														QAParseFetchService.this,
														parse, parse.get(FIRST_ELEMENT)
																.getClassName());
									} else {
										parseException.printStackTrace();
									}
								}
							});
				} else {
					questionsListFromDatabase = QADatabaseOperations
							.fetchAllQuestions(QAParseFetchService.this,
									QAConstants.FETCH_ALL + tableName);
					time = questionsListFromDatabase.get(FIRST_ELEMENT).getUpdatedAt();
					format = new SimpleDateFormat(
							"EEE MMM dd HH:mm:ss ZZZZ yyyy",
							Locale.getDefault());
					try {
						dateInDatabase = format.parse(time);
					} catch (java.text.ParseException error) {
						error.printStackTrace();
					}
					ParseQuery<ParseObject> questionQuery = ParseQuery
							.getQuery(tableName);
					questionQuery.whereGreaterThan(UPDATED_AT, dateInDatabase);
					questionQuery
							.findInBackground(new FindCallback<ParseObject>() {
								public void done(List<ParseObject> parse,
										ParseException parseException) {
									if (parseException == null) {
										for (ParseObject parseObject : parse) {
											primaryKey = parseObject
													.getInt(PRIMARY_KEY);
											isQuestionPresent = QADatabaseOperations
													.checkIfQuestionPresentInDatabase(
															QAParseFetchService.this,
															parseObject
																	.getClassName(),
															primaryKey);
											if (isQuestionPresent) {
												QADatabaseOperations
														.updateQuestionInDatabase(
																QAParseFetchService.this,
																parseObject
																		.getClassName(),
																parseObject,
																primaryKey);
											} else {
												listToAddWhenNotPresent = new ArrayList<ParseObject>();
												listToAddWhenNotPresent
														.add(parseObject);
												QADatabaseOperations
														.insertQuestionsIntoTable(
																QAParseFetchService.this,
																listToAddWhenNotPresent,
																parseObject
																		.getClassName());
											}
										}
										if (parse.size() > PARSE_OBJECT_LIST_EMPTY) {
											QADatabaseOperations
													.setUpdatedTime(
															QAParseFetchService.this,
															parse.get(FIRST_ELEMENT)
																	.getClassName());
										} else {
											// Nothing
										}
									} else {
										parseException.printStackTrace();
									}
								}
							});
				}
			}
		} else {
			Builder dialog = QAAlert.alert(QAParseFetchService.this);
			dialog.setTitle(R.string.network_error);
			dialog.setMessage(R.string.check_network_connection);
			dialog.setPositiveButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog,
								final int which) {
							dialog.cancel();
						}
					});
			dialog.show();
		}
	}
}