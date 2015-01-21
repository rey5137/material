package com.rey.material.demo;

import com.rey.material.drawable.ContactChipDrawable;
import com.rey.material.text.style.ContactChipSpan;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.EditText;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

        EditText a = (EditText) v.findViewById(R.id.textfield_tv);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("Test 1234567");
        ContactChipSpan span = new ContactChipSpan("Harry Potter", ThemeUtil.dpToPx(getActivity(), 32), ThemeUtil.dpToPx(getActivity(), 128), ThemeUtil.dpToPx(getActivity(), 8), ThemeUtil.dpToPx(getActivity(), 12), Typeface.DEFAULT, 0xFF7A7A7A, ThemeUtil.spToPx(getActivity(), 16), 0xFFE7E7E7);
        Bitmap icon = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.ic_user);
        span.setImage(icon);
        ssb.setSpan(span, 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        a.setText(ssb, TextView.BufferType.SPANNABLE);

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
