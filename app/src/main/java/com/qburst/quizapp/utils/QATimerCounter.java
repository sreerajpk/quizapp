package com.qburst.quizapp.utils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.os.CountDownTimer;

import com.qburst.quizapp.listeners.QATimerListener;

public class QATimerCounter extends CountDownTimer {

	private QATimerListener listener;
	public QATimerCounter(long millisInFuture, long countDownInterval) {
		super(millisInFuture, countDownInterval);
	}

	public QATimerCounter(long millisInFuture, long countDownInterval, QATimerListener listener) {
		super(millisInFuture, countDownInterval);
		this.listener = listener;
	}
	@Override
	public void onTick(long millisUntilFinished) {
		long millis = millisUntilFinished;
		String hms = String.format(Locale.getDefault(),
				"%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(millis)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
								.toHours(millis)),
				TimeUnit.MILLISECONDS.toSeconds(millis)
						- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
								.toMinutes(millis)));
		listener.timerTicking(hms);
	}

	@Override
	public void onFinish() {
		listener.timerFinished();
	}
}
