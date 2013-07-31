package org.monospace.smsfilter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

/**
 * SMSFilter
 * Author: rhgb
 */
public class EditFilterDialogFragment extends DialogFragment implements AdapterView.OnItemSelectedListener, TextWatcher {
	public enum FilterType {
		ADDR_START_WITH(0, FilterType.TARGET_ADDR, FilterType.TYPE_START_WITH),
		ADDR_CONTAINS(1, FilterType.TARGET_ADDR, FilterType.TYPE_CONTAINS),
		ADDR_END_WITH(2, FilterType.TARGET_ADDR, FilterType.TYPE_END_WITH),
		CONTENT_CONTAINS(3, FilterType.TARGET_CONTENT, FilterType.TYPE_CONTAINS),
		ADDR_RAW(4, FilterType.TARGET_ADDR, FilterType.TYPE_RAW),
		CONTENT_RAW(5, FilterType.TARGET_CONTENT, FilterType.TYPE_RAW);

		private final int index;
		private final String target;
		private final String type;

		public static final String TARGET_ADDR = "address";
		public static final String TARGET_CONTENT = "content";
		public static final String TYPE_START_WITH = "start_with";
		public static final String TYPE_END_WITH = "end_with";
		public static final String TYPE_CONTAINS = "contains";
		public static final String TYPE_RAW = "raw";

		private FilterType(int i, String tar, String type) {
			this.index = i;
			this.target = tar;
			this.type = type;
		}

		public static FilterType get(int i) {
			for (FilterType t : values()) {
				if (i == t.index) return t;
			}
			return null;
		}

		public String getTarget() {
			return target;
		}

		public String getType() {
			return type;
		}

		public String applyContent(String src) {
			switch (this) {
				case ADDR_RAW:
				case CONTENT_RAW:
					return src;
				case ADDR_START_WITH:
					return src + "%";
				case ADDR_END_WITH:
					return "%" + src;
				case ADDR_CONTAINS:
				case CONTENT_CONTAINS:
					return "%" + src + "%";
			}
			return src;
		}
	}

	public enum FilterState {
		BLOCK(0, "block"),
		PERMIT(1, "permit");

		private final int index;
		private final String key;

		private FilterState(int i, String k) {
			this.index = i;
			this.key = k;
		}

		public static FilterState get(int i) {
			for (FilterState s : values()) {
				if (i == s.index) return s;
			}
			return null;
		}

		public static FilterState get(String key) {
			for (FilterState s : values()) {
				if (key.equals(s.key)) return s;
			}
			return null;
		}

		@Override
		public String toString() {
			return key;
		}
	}

	public interface DialogListener {
		public void onDialogPositiveClick(FilterType type, FilterState state, String content);
	}

	private Spinner mFilterSpinner;
	private Spinner mStateSpinner;
	private EditText mEditText;
	private DialogListener mListener;
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
							mListener.onDialogPositiveClick(FilterType.get((int) mFilterSpinner.getSelectedItemId()),
									FilterState.get((int) mStateSpinner.getSelectedItemId()),
									mEditText.getText().toString());
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

		mEditText = (EditText) customView.findViewById(R.id.filter_content_edit);
		mEditText.addTextChangedListener(this);
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(mEditText.getText().length() > 0);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		FilterType type = FilterType.get((int)id);
		switch (type.getTarget()) {
			case FilterType.TARGET_ADDR:
				mEditText.setInputType(InputType.TYPE_CLASS_PHONE);
				break;
			case FilterType.TARGET_CONTENT:
				mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
				break;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		Button b = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
		b.setEnabled(s.length() > 0);
	}

	@Override
	public void afterTextChanged(Editable s) {}

	public void setDialogListener(DialogListener listener) {
		mListener = listener;
	}
}
