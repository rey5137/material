package com.rey.material.demo;

import com.rey.material.app.ContactEditText;
import com.rey.material.drawable.ContactChipDrawable;
import com.rey.material.text.style.ContactChipSpan;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.EditText;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.fragment.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import static android.provider.ContactsContract.CommonDataKinds.Phone;

public class TextfieldFragment extends Fragment{

	public static TextfieldFragment newInstance(){
		TextfieldFragment fragment = new TextfieldFragment();
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_textfield, container, false);
				
		final EditText et_helper = (EditText)v.findViewById(R.id.textfield_et_helper);

		et_helper.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
					et_helper.setError("Password is incorrect.");

				return false;
			}

		});

		et_helper.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					et_helper.setError(null);
			}

		});

		final EditText et_helper_error = (EditText)v.findViewById(R.id.textfield_et_helper_error);

		et_helper_error.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
					et_helper_error.setError("Password is incorrect.");

				return false;
			}

		});

		et_helper_error.setOnFocusChangeListener(new View.OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					et_helper_error.setError(null);
			}

		});

        ContactEditText a = (ContactEditText) v.findViewById(R.id.textfield_tv);

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}


}
