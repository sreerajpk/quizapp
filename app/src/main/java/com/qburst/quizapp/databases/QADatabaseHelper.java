package com.qburst.quizapp.databases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import com.qburst.quizapp.constants.QAConstants;

public class QADatabaseHelper extends SQLiteOpenHelper {
	private static QADatabaseHelper instance;
	private Context context;
	private SQLiteDatabase myDataBase;

	public QADatabaseHelper(Context context) {
		
		super(context, QAConstants.DATABASE_NAME, null, QAConstants
				.DATABASE_VERSION);
	}

	public static synchronized QADatabaseHelper getInstance(Context context) {

		if (instance == null) {
			instance = new QADatabaseHelper(context);
			instance.context = context;
		}
		return instance;
	}

	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = instance.context.getAssets().open(
				QAConstants.DATABASE_NAME);

		// Path to the just created empty db
		String outFileName = QAConstants.DB_PATH
				+ QAConstants.DATABASE_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
		} else {
			runCopyDataBaseAsyncTask();
		}
	}

	private void runCopyDataBaseAsyncTask() {
		
		this.getReadableDatabase();
		try {
			copyDataBase();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	private boolean checkDataBase() {
		
		SQLiteDatabase checkDB = null;
		try {
			String myPath = QAConstants.DB_PATH
					+ QAConstants.DATABASE_NAME;
			File file = new File(myPath);
			if (file.exists() && !file.isDirectory()) {
				checkDB = SQLiteDatabase.openDatabase(myPath, null,
						SQLiteDatabase.OPEN_READONLY);
			}
		} catch (SQLiteException exception) {
			// database doesn't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	public void openDataBase() throws SQLException {
		
		// Open the database
		String myPath = QAConstants.DB_PATH + QAConstants.DATABASE_NAME;
		myDataBase = SQLiteDatabase.openDatabase(myPath, null,
				SQLiteDatabase.OPEN_READONLY);
	}

	@Override
	public synchronized void close() {
		
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase database) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {

	}
}
