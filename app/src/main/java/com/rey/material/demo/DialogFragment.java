package com.rey.material.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rey.material.app.SimpleDialog;
import com.rey.material.app.TimePickerDialog;
import com.rey.material.widget.Button;
import com.rey.material.widget.MonthView;

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

        MonthView mv = (MonthView)v.findViewById(R.id.dialog_v);
        mv.setMonth(Calendar.DECEMBER, 2014);
        mv.setSelectedDay(10, false);
        mv.setToday(29);
        mv.setAvailableDay(5, 30);

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
