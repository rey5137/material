package com.rey.material.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.rey.material.widget.RadioButton;

public class SwitchesFragment extends Fragment{

	RadioButton rb1;
	RadioButton rb2;
	RadioButton rb3;
	
	public static SwitchesFragment newInstance(){
		SwitchesFragment fragment = new SwitchesFragment();
		
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_switches, container, false);
		
		rb1 = (RadioButton)v.findViewById(R.id.switches_rb1);
		rb2 = (RadioButton)v.findViewById(R.id.switches_rb2);
		rb3 = (RadioButton)v.findViewById(R.id.switches_rb3);
								
		CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					rb1.setChecked(rb1 == buttonView);
					rb2.setChecked(rb2 == buttonView);
					rb3.setChecked(rb3 == buttonView);
				}
				
			}
			
		};
		
		rb1.setOnCheckedChangeListener(listener);
		rb2.setOnCheckedChangeListener(listener);
		rb3.setOnCheckedChangeListener(listener);
				
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
