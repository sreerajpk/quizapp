package com.qburst.quizapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;

public class QATwitterShareWebViewActivity extends Activity {
	
	private WebView webView;
	private String verifier;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_share_webview_layout);
		setTitle(R.string.login);
		final String url = this.getIntent().getStringExtra(QAConstants.TWITTER_AUTHENTICATION_URL);
		if (null == url) {
			finish();
		}
		webView = (WebView) findViewById(R.id.webView);
		webView.setWebViewClient(new MyWebViewClient());
		webView.loadUrl(url);
	}

	class MyWebViewClient extends WebViewClient {

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.contains(QAConstants.TWITTER_CALLBACK_URL)) {
				new LoginView().execute(url);
				return true;
			}
			return false;
		}
	}

	class LoginView extends AsyncTask<String, String, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... url) {
			Uri uri = Uri.parse(url[0]);
			verifier = uri
					.getQueryParameter(QAConstants.TWITTER_OAUTH_VERIFIER);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			twitterLoginFinished();
		}
	}
	// Sending results back to QuizResult Activity
	public void twitterLoginFinished() {
	
		Intent resultIntent = new Intent();
		resultIntent.putExtra(QAConstants.TWITTER_OAUTH_VERIFIER, verifier);
		setResult(RESULT_OK, resultIntent);
		finish();
	}
}
