package com.qburst.quizapp.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;

public class QABackgroundMusicService extends Service {

	private MediaPlayer player;
	private int music;

	public IBinder onBind(Intent arg0) {

		throw new UnsupportedOperationException(
				getString(R.string.not_yet_implemented));
	}

	@Override
	public void onCreate() {

		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent == null) {
			return 1;
		} else {
			music = intent
					.getIntExtra(QAConstants.MUSIC, QAConstants.QUIZ_NULL);
			switch (music) {
			case QAConstants.MAIN_BACKGROUND_MUSIC:
				player = MediaPlayer.create(this, R.raw.quizapp_background);
				player.setLooping(true);
				break;
			case QAConstants.QUIZ_RUNNING_MUSIC:
				player = MediaPlayer.create(this, R.raw.quiz_running);
				player.setLooping(true);
				break;
			case QAConstants.QUIZ_RESULT_MUSIC:
				player = MediaPlayer.create(this, R.raw.quiz_result);
				player.setLooping(false);
				break;
			case QAConstants.QUIZ_NULL:
				player = MediaPlayer.create(this, R.raw.quizapp_background);
				player.setLooping(true);
				break;
			}
			player.setVolume(100, 100);
			player.start();
			return 1;
		}
	}

	public IBinder onUnBind(Intent arg0) {

		throw new UnsupportedOperationException(
				getString(R.string.not_yet_implemented));
	}

	@Override
	public void onDestroy() {
		if (player != null) {
			if (player.isPlaying()) {
				player.stop();
				player.release();
			}
		}
	}
}