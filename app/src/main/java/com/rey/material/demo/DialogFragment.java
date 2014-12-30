package com.rey.material.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.widget.DatePicker;

import java.util.Calendar;

public class DialogFragment extends Fragment{

	public static DialogFragment newInstance(){
		DialogFragment fragment = new DialogFragment();
		
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dialog, container, false);

        DatePicker dp = (DatePicker)v.findViewById(R.id.dialog_v);
        dp.setDayRange(1, Calendar.JANUARY, 2014, 31, Calendar.DECEMBER, 2014);
        dp.setDay(20, Calendar.DECEMBER, 2014);

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
