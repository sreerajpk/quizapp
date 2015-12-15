package com.qburst.quizapp.activities;

import android.content.Context;
import android.content.Intent;

import com.google.example.games.basegameutils.BaseGameActivity;
import com.qburst.quizapp.constants.QAConstants;
import com.qburst.quizapp.services.QABackgroundMusicService;
import com.qburst.quizapp.utils.QAPreference;

public class QABaseActivity extends BaseGameActivity {

	// Play background music
	public void playMusic(Context context, int musicType) {
		
		if (QAPreference.getSoundPreference(context)) {
			Intent music = new Intent(context, QABackgroundMusicService.class);
			music.putExtra(QAConstants.MUSIC, musicType);
			startService(music);
		} else {
			// Nothing
		}
	}

	// Stop background music
	public void stopMusic(Context context) {
		
		Intent music = new Intent(context, QABackgroundMusicService.class);
		stopService(music);
	}

	@Override
	protected void onPause() {
		
		stopMusic(getApplicationContext());
		super.onPause();
	}

	@Override
	public void onSignInFailed() {

	}

	@Override
	public void onSignInSucceeded() {

	}
}
