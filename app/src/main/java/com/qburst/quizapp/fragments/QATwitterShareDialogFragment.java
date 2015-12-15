package com.qburst.quizapp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.qburst.quizapp.R;
import com.qburst.quizapp.constants.QAConstants;

public class QATwitterShareDialogFragment extends DialogFragment {

	private String appDefaultStatus;

	public static QATwitterShareDialogFragment newInstance() {

		QATwitterShareDialogFragment dialog = new QATwitterShareDialogFragment();
		return dialog;
	}

	public interface ShareDialogListener {

		public void onTweetShareClick(DialogFragment dialog, String userTitle);

		public void OnTweetCancel(DialogFragment dialog);
	}

	private ShareDialogListener shareDialogListener;

	@Override
	public void onAttach(Activity activity) {

		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			shareDialogListener = (ShareDialogListener) activity;
		} catch (ClassCastException exception) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement ShareDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Bundle arguments = getArguments();
		appDefaultStatus = arguments.getString(QAConstants.DEFAULT_STATUS);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View shareLayout = inflater
				.inflate(R.layout.twitter_share_layout, null);
		builder.setTitle(R.string.share_to_twitter);
		final EditText title = (EditText) shareLayout
				.findViewById(R.id.user_title);
		final TextView appDefaultMessage = (TextView) shareLayout
				.findViewById(R.id.app_default_message);
		appDefaultMessage.setText(appDefaultStatus);

		builder.setView(shareLayout)
				.setPositiveButton(R.string.tweet,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// Tweet the user message
								shareDialogListener.onTweetShareClick(
										QATwitterShareDialogFragment.this,
										title.getText().toString());

							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								QATwitterShareDialogFragment.this.getDialog()
										.cancel();
								shareDialogListener
										.OnTweetCancel(QATwitterShareDialogFragment.this);
							}
						});
		return builder.create();
	}
}
