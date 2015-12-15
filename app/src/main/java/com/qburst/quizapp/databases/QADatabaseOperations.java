package com.qburst.quizapp.databases;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.parse.ParseObject;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.models.QAQuestions;
import com.qburst.quizapp.models.QASections;

public class QADatabaseOperations {

	// Column names of the table for each section
	public static final String COLUMN_NAME_PRIMARY_KEY = "primaryKey";
	public static final String COLUMN_NAME_QUESTION = "question";
	public static final String COLUMN_NAME_OPTION1 = "optionOne";
	public static final String COLUMN_NAME_OPTION2 = "optionTwo";
	public static final String COLUMN_NAME_OPTION3 = "optionThree";
	public static final String COLUMN_NAME_OPTION4 = "optionFour";
	public static final String COLUMN_NAME_ANSWER = "answer";
	public static final String COLUMN_NAME_UPDATED_AT = "updatedAt";
	public static final String COLUMN_NAME_IS_FETCHED = "isFetched";
	public static final String COLUMN_NAME_DIFFICULTY_LEVEL = "difficultyLevel";

	// Column names of the table for storing the details of sections
	public static final String SECTIONS_COLUMN_NAME_PRIMARY_KEY = "primaryKey";
	public static final String SECTIONS_COLUMN_NAME_SECTION_NAME = "sectionName";
	public static final String SECTIONS_COLUMN_NAME_IMAGE_URL = "imageUrl";
	public static final String SECTIONS_COLUMN_NAME_UPDATED_AT = "updatedAt";

	// Column names of the table for storing the score of different users
	public static final String SCORE_COLUMN_NAME_USER_EMAIL = "userEmail";
	public static final String SCORE_COLUMN_NAME_SCORE = "score";

	// Column names of the table for storing the details of all achievements
	public static final String ACHIEVEMENTS_COLUMN_NAME_NAME = "name";
	public static final String ACHIEVEMENTS_COLUMN_NAME_ID = "id";
	public static final String ACHIEVEMENTS_COLUMN_NAME_TYPE = "type";

	// Create a new Section table
	public static synchronized void createNewSection(Context context,
			String tableName) throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
				+ COLUMN_NAME_PRIMARY_KEY + " INTEGER PRIMARY KEY, "
				+ COLUMN_NAME_QUESTION + " TEXT, " + COLUMN_NAME_OPTION1
				+ " TEXT, " + COLUMN_NAME_OPTION2 + " TEXT, "
				+ COLUMN_NAME_OPTION3 + " TEXT, " + COLUMN_NAME_OPTION4
				+ " TEXT, " + COLUMN_NAME_ANSWER + " TEXT, "
				+ COLUMN_NAME_UPDATED_AT + " TEXT, " + COLUMN_NAME_IS_FETCHED
				+ " BOOL, " + COLUMN_NAME_DIFFICULTY_LEVEL + " TEXT)";
		db.execSQL(query);
	}

	public static synchronized void insertSectionsIntoTable(Context context,
			List<ParseObject> sectionsList, String tableName)
			throws SQLException, NullPointerException {
		if (sectionsList != null) {
			insertSections(context, sectionsList, tableName);
		}
	}

	// Insert Sections to the local database
	public static synchronized void insertSections(Context context,
			List<ParseObject> sectionsList, String tableName)
			throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();

		for (ParseObject section : sectionsList) {
			values.put(SECTIONS_COLUMN_NAME_PRIMARY_KEY,
					section.getInt(SECTIONS_COLUMN_NAME_PRIMARY_KEY));
			values.put(SECTIONS_COLUMN_NAME_SECTION_NAME,
					section.getString(SECTIONS_COLUMN_NAME_SECTION_NAME));
			values.put(SECTIONS_COLUMN_NAME_IMAGE_URL,
					section.getString(SECTIONS_COLUMN_NAME_IMAGE_URL));

			Formatter formatter = new Formatter();
			Calendar calendar = Calendar.getInstance();
			formatter.format("%tc", calendar);
			formatter.close();

			Date originalDate = calendar.getTime();

			values.put(SECTIONS_COLUMN_NAME_UPDATED_AT, originalDate.toString());
			try {
				db.replace(tableName, null, values);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		db.close();
	}

	// Fetch the list of sections
	public static synchronized ArrayList<QASections> fetchSectionsList(
			Context context, String query) throws SQLException,
			NullPointerException {

		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		ArrayList<QASections> sectionsList = null;

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(query, null);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		cursor.moveToFirst();
		if (cursor != null && cursor.getCount() > QAConstants.DB_EMPTY) {
			sectionsList = new ArrayList<QASections>();
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				QASections section = new QASections();

				section.setPrimaryKey(cursor.getInt(cursor
						.getColumnIndex(SECTIONS_COLUMN_NAME_PRIMARY_KEY)));
				section.setSectionName(cursor.getString(cursor
						.getColumnIndex(SECTIONS_COLUMN_NAME_SECTION_NAME)));
				section.setImageUrl(cursor.getString(cursor
						.getColumnIndex(SECTIONS_COLUMN_NAME_IMAGE_URL)));
				section.setUpdatedAt(cursor.getString(cursor
						.getColumnIndex(SECTIONS_COLUMN_NAME_UPDATED_AT)));
				sectionsList.add(section);
			}
		}
		cursor.close();
		return sectionsList;
	}

	public static synchronized void insertQuestionsIntoTable(Context context,
			List<ParseObject> questionsList, String tableName)
			throws SQLException, NullPointerException {
		if (questionsList != null) {
			insertQuestions(context, questionsList, tableName);
		}
	}

	// Insert Questions to the local database
	public static synchronized void insertQuestions(Context context,
			List<ParseObject> questionsList, String tableName)
			throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();

		for (ParseObject question : questionsList) {
			values.put(COLUMN_NAME_PRIMARY_KEY,
					question.getString(COLUMN_NAME_PRIMARY_KEY));
			values.put(COLUMN_NAME_QUESTION,
					question.getString(COLUMN_NAME_QUESTION));
			values.put(COLUMN_NAME_OPTION1,
					question.getString(COLUMN_NAME_OPTION1));
			values.put(COLUMN_NAME_OPTION2,
					question.getString(COLUMN_NAME_OPTION2));
			values.put(COLUMN_NAME_OPTION3,
					question.getString(COLUMN_NAME_OPTION3));
			values.put(COLUMN_NAME_OPTION4,
					question.getString(COLUMN_NAME_OPTION4));
			values.put(COLUMN_NAME_ANSWER,
					question.getString(COLUMN_NAME_ANSWER));
			values.put(COLUMN_NAME_IS_FETCHED, 0);
			values.put(COLUMN_NAME_DIFFICULTY_LEVEL,
					question.getString(COLUMN_NAME_DIFFICULTY_LEVEL));
			Formatter formatter = new Formatter();
			Calendar calendar = Calendar.getInstance();
			formatter.format("%tc", calendar);
			formatter.close();

			Date originalDate = calendar.getTime();

			values.put(COLUMN_NAME_UPDATED_AT, originalDate.toString());

			try {
				db.replace(tableName, null, values);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		db.close();
	}

	// Get primary keys from database
	public static synchronized List<Integer> getPrimaryKeysFromDatabase(
			Context context, String tableName, String difficultyLevel) {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		List<Integer> primaryKeys = null;

		String query = "SELECT " + COLUMN_NAME_PRIMARY_KEY + " FROM "
				+ tableName + " WHERE " + COLUMN_NAME_DIFFICULTY_LEVEL + " = '"
				+ difficultyLevel + "'";

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(query, null);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		// cursor.moveToFirst();
		if (cursor != null && cursor.getCount() > 0) {
			primaryKeys = new ArrayList<Integer>();
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				primaryKeys.add(cursor.getInt(cursor
						.getColumnIndex(COLUMN_NAME_PRIMARY_KEY)));
			}
		}
		cursor.close();
		return primaryKeys;
	}

	// Fetch a single question from a particular section
	public static synchronized QAQuestions fetchSingleQuestion(Context context,
			String tableName, int primaryKey) throws SQLException,
			NullPointerException {

		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		QAQuestions question = null;

		String query = QAConstants.FETCH_ALL + tableName + " WHERE "
				+ COLUMN_NAME_PRIMARY_KEY + " = " + primaryKey;

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(query, null);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		cursor.moveToFirst();
		if (cursor != null && cursor.getCount() > 0) {
			question = new QAQuestions();
			question.setPrimaryKey(cursor.getInt(cursor
					.getColumnIndex(COLUMN_NAME_PRIMARY_KEY)));
			question.setQuestion(cursor.getString(cursor
					.getColumnIndex(COLUMN_NAME_QUESTION)));
			question.setOptionOne(cursor.getString(cursor
					.getColumnIndex(COLUMN_NAME_OPTION1)));
			question.setOptionTwo(cursor.getString(cursor
					.getColumnIndex(COLUMN_NAME_OPTION2)));
			question.setOptionThree(cursor.getString(cursor
					.getColumnIndex(COLUMN_NAME_OPTION3)));
			question.setOptionFour(cursor.getString(cursor
					.getColumnIndex(COLUMN_NAME_OPTION4)));
			question.setAnswer(cursor.getString(cursor
					.getColumnIndex(COLUMN_NAME_ANSWER)));
			question.setUpdatedAt(cursor.getString(cursor
					.getColumnIndex(COLUMN_NAME_UPDATED_AT)));
			question.setFetched((cursor.getInt(cursor
					.getColumnIndex(COLUMN_NAME_IS_FETCHED)) == 1) ? true
					: false);
		}
		cursor.close();
		return question;
	}

	// Set isFetched field of fetched question to true
	public static synchronized void setIsFetchedToTrue(Context context,
			String tableName, int primaryKey) throws SQLException,
			NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "UPDATE " + tableName + " SET " + COLUMN_NAME_IS_FETCHED
				+ " = " + 1 + " WHERE " + COLUMN_NAME_PRIMARY_KEY + " = "
				+ primaryKey;

		db.execSQL(query);
	}

	// Fetch all questions from a particular section
	public static synchronized ArrayList<QAQuestions> fetchAllQuestions(
			Context context, String query) throws SQLException,
			NullPointerException {

		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		ArrayList<QAQuestions> questionsList = null;

		Cursor cursor = null;
		try {
			cursor = db.rawQuery(query, null);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		cursor.moveToFirst();
		if (cursor != null && cursor.getCount() > 0) {
			questionsList = new ArrayList<QAQuestions>();
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {

				QAQuestions question = new QAQuestions();

				question.setPrimaryKey(cursor.getInt(cursor
						.getColumnIndex(COLUMN_NAME_PRIMARY_KEY)));
				question.setQuestion(cursor.getString(cursor
						.getColumnIndex(COLUMN_NAME_QUESTION)));
				question.setOptionOne(cursor.getString(cursor
						.getColumnIndex(COLUMN_NAME_OPTION1)));
				question.setOptionTwo(cursor.getString(cursor
						.getColumnIndex(COLUMN_NAME_OPTION2)));
				question.setOptionThree(cursor.getString(cursor
						.getColumnIndex(COLUMN_NAME_OPTION3)));
				question.setOptionFour(cursor.getString(cursor
						.getColumnIndex(COLUMN_NAME_OPTION4)));
				question.setAnswer(cursor.getString(cursor
						.getColumnIndex(COLUMN_NAME_ANSWER)));
				question.setUpdatedAt(cursor.getString(cursor
						.getColumnIndex(COLUMN_NAME_UPDATED_AT)));
				question.setFetched((cursor.getInt(cursor
						.getColumnIndex(COLUMN_NAME_IS_FETCHED)) == 1) ? true
						: false);
				questionsList.add(question);
			}
		}
		cursor.close();
		return questionsList;
	}

	// Count entries in the local database
	public static synchronized int count(Context context, String query)
			throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		Cursor cursor = db.rawQuery(query, null);

		return (cursor.getCount());
	}

	// Count questions in the selected level
	public static synchronized int countQuestionsInTheLevel(Context context,
			String tableName, String difficultyLevel) throws SQLException,
			NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String query = QAConstants.FETCH_ALL + tableName + " WHERE "
				+ COLUMN_NAME_DIFFICULTY_LEVEL + " = '" + difficultyLevel + "'";
		Cursor cursor = db.rawQuery(query, null);

		return (cursor.getCount());
	}

	// Set the updated time of all questions of a particular session with
	// current time
	public static synchronized void setUpdatedTime(Context context,
			String tableName) throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		Formatter formatter = new Formatter();
		Calendar calendar = Calendar.getInstance();
		formatter.format("%tc", calendar);
		formatter.close();
		Date originalDate = calendar.getTime();

		String query = "UPDATE " + tableName + " SET " + COLUMN_NAME_UPDATED_AT
				+ " = '" + originalDate.toString() + "'";

		db.execSQL(query);
	}

	// Check if a question is present in a database
	public static synchronized boolean checkIfQuestionPresentInDatabase(
			Context context, String tableName, int primaryKey)
			throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String query = QAConstants.FETCH_ALL + tableName
				+ " WHERE primaryKey = " + primaryKey;
		Cursor cursor = db.rawQuery(query, null);
		if (cursor.getCount() == 0) {
			return false;
		} else {
			return true;
		}
	}

	// Update the questions in a particular session
	public static synchronized void updateQuestionInDatabase(Context context,
			String tableName, ParseObject question, int primaryKey)
			throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "UPDATE " + tableName + " SET "
				+ COLUMN_NAME_PRIMARY_KEY + " = "
				+ question.getInt(COLUMN_NAME_PRIMARY_KEY) + ", "
				+ COLUMN_NAME_QUESTION + " = '"
				+ question.getString(COLUMN_NAME_QUESTION) + "', "
				+ COLUMN_NAME_OPTION1 + " = '"
				+ question.getString(COLUMN_NAME_OPTION1) + "', "
				+ COLUMN_NAME_OPTION2 + " = '"
				+ question.getString(COLUMN_NAME_OPTION2) + "', "
				+ COLUMN_NAME_OPTION3 + " = '"
				+ question.getString(COLUMN_NAME_OPTION3) + "', "
				+ COLUMN_NAME_OPTION4 + " = '"
				+ question.getString(COLUMN_NAME_OPTION4) + "', "
				+ COLUMN_NAME_ANSWER + " = '"
				+ question.getString(COLUMN_NAME_ANSWER) + "', "
				+ COLUMN_NAME_IS_FETCHED + " = 1, "
				+ COLUMN_NAME_DIFFICULTY_LEVEL + " = '"
				+ question.getString(COLUMN_NAME_DIFFICULTY_LEVEL) + "'"
				+ " WHERE primaryKey = " + primaryKey;

		db.execSQL(query);
	}

	// Set the isFetched column to 0 (false) for a particular session
	public static synchronized void setIsFetchedToFalse(Context context,
			String tableName) throws SQLException, NullPointerException {
		QADatabaseHelper dbHelper = QADatabaseHelper.getInstance(context);
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		String query = "UPDATE " + tableName + " SET " + COLUMN_NAME_IS_FETCHED
				+ " = " + 0;

		db.execSQL(query);
	}
}