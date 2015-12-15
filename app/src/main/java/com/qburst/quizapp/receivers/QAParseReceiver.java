package com.qburst.quizapp.receivers;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;
import com.qburst.quizapp.services.QAParseFetchService;

public class QAParseReceiver extends ParsePushBroadcastReceiver {

	@Override
	public Class<? extends Activity> getActivity(Context context, Intent intent) {
		return super.getActivity(context, intent);
	}

	@Override
	public Notification getNotification(Context context, Intent intent) {
		
		// Notification display needed in case
		
		/*
		 * Bitmap icon = BitmapFactory.decodeResource(context.getResources(),
		 * R.drawable.ic_launcher);
		 * 
		 * Intent launchActivity = new Intent(context, QASignInActivity.class);
		 * PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
		 * launchActivity, 0);
		 */

		/*
		 * NotificationCompat.Builder builder = new NotificationCompat.Builder(
		 * context).setSmallIcon(R.drawable.ic_launcher)
		 * .setContentTitle("PUSH RECEIVED").setContentText(msg)
		 * .setContentIntent(pendingIntent)
		 * .setDefaults(Notification.DEFAULT_ALL) // requires VIBRATE permission
		 * .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
		 * .setLargeIcon(icon); return builder.build();
		 */
		return null;
	}

	@Override
	protected void onPushReceive(Context context, Intent intent) {
		
		Intent serviceIntent = new Intent(context, QAParseFetchService.class);
		context.startService(serviceIntent);
		super.onPushReceive(context, intent);
	}
}
