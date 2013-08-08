package org.monospace.smsfilter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

/**
 * SMSFilter
 * Author: rhgb
 */
public class EditFilterDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener {

	public interface DialogListener {
		public void onDialogPositiveClick(DbVars.FilterType type, DbVars.FilterState state, String content, String desc);
	}

	private class EditRuleListener implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			Button b = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
			b.setEnabled(s.length() > 0);
		}
		@Override
		public void afterTextChanged(Editable s) {
			inputChanged();
		}

	}

	private class EditDescListener implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			Log.i("EditDescListener", "desc changed");
		}
		@Override
		public void afterTextChanged(Editable s) {}

	}

	private Spinner mFilterSpinner;
	private Spinner mStateSpinner;
	private EditText mEditRule;
	private EditText mEditDesc;
	private DialogListener mListener;
	private boolean mDescEdited = false;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View customView = inflater.inflate(R.layout.dialog_edit_filter, null);

		builder.setTitle(R.string.title_add_filter)
				.setMessage(R.string.desc_add_filter)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mListener != null) {
							mListener.onDialogPositiveClick(DbVars.FilterType.get((int) mFilterSpinner.getSelectedItemId()),
									DbVars.FilterState.get((int) mStateSpinner.getSelectedItemId()),
									mEditRule.getText().toString(),
									mEditDesc.getText().toString()
									);
						}
					}
				}).setNegativeButton(android.R.string.cancel, null)
				.setView(customView);

		Dialog dialog = builder.create();

		mFilterSpinner = (Spinner) customView.findViewById(R.id.filter_type_spinner);
		ArrayAdapter<CharSequence> filterAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.filter_type, android.R.layout.simple_spinner_item);
		filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mFilterSpinner.setAdapter(filterAdapter);
		mFilterSpinner.setOnItemSelectedListener(this);

		mStateSpinner = (Spinner) customView.findViewById(R.id.filter_state_spinner);
		ArrayAdapter<CharSequence> stateAdapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.filter_state, android.R.layout.simple_spinner_item);
		stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mStateSpinner.setAdapter(stateAdapter);
		mStateSpinner.setOnItemSelectedListener(this);

		mEditRule = (EditText) customView.findViewById(R.id.filter_content_edit);
		mEditRule.addTextChangedListener(new EditRuleListener());

		mEditDesc = (EditText) customView.findViewById(R.id.filter_desc_edit);
		mEditDesc.addTextChangedListener(new EditDescListener());
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(mEditRule.getText().length() > 0);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		switch (parent.getId()) {
			case R.id.filter_type_spinner:
				DbVars.FilterType type = DbVars.FilterType.get((int) id);
				switch (type.getTarget()) {
					case DbVars.FilterType.TARGET_ADDR:
						mEditRule.setInputType(InputType.TYPE_CLASS_PHONE);
						break;
					case DbVars.FilterType.TARGET_CONTENT:
						mEditRule.setInputType(InputType.TYPE_CLASS_TEXT);
						break;
				}
				break;
		}
		inputChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	public void setDialogListener(DialogListener listener) {
		mListener = listener;
	}

	public void inputChanged() {
		if (!mDescEdited) {
			String desc = String.format(
					getResources().getStringArray(R.array.filter_desc)[mFilterSpinner.getSelectedItemPosition()],
					mEditRule.getText().toString());
			mEditDesc.setText(desc);
		}
	}
}
