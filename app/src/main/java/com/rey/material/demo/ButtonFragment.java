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
import com.rey.material.drawable.RevealDrawable;
import com.rey.material.widget.Button;
import com.rey.material.widget.TimePicker;

public class ButtonFragment extends Fragment{

	public static ButtonFragment newInstance(){
		ButtonFragment fragment = new ButtonFragment();
		
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_button, container, false);
		
		Button bt_flat = (Button)v.findViewById(R.id.button_bt_flat);
		Button bt_flat_color = (Button)v.findViewById(R.id.button_bt_flat_color);
		Button bt_flat_wave = (Button)v.findViewById(R.id.button_bt_flat_wave);
		Button bt_flat_wave_color = (Button)v.findViewById(R.id.button_bt_flat_wave_color);
		Button bt_raise = (Button)v.findViewById(R.id.button_bt_raise);
		Button bt_raise_color = (Button)v.findViewById(R.id.button_bt_raise_color);
		Button bt_raise_wave = (Button)v.findViewById(R.id.button_bt_raise_wave);
		Button bt_raise_wave_color = (Button)v.findViewById(R.id.button_bt_raise_wave_color);
		Button bt_float = (Button)v.findViewById(R.id.button_bt_float);
		Button bt_float_color = (Button)v.findViewById(R.id.button_bt_float_color);
		Button bt_float_wave = (Button)v.findViewById(R.id.button_bt_float_wave);
		Button bt_float_wave_color = (Button)v.findViewById(R.id.button_bt_float_wave_color);
									
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Button Clicked!\nEvent's fired when in anim end.", Toast.LENGTH_SHORT).show();				
			}
		};
		
		View.OnClickListener listener_delay = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(getActivity(), "Button Clicked!\nEvent's fired when out anim end.", Toast.LENGTH_SHORT).show();			
			}
		};
		
		bt_flat.setOnClickListener(listener);
		bt_flat_wave.setOnClickListener(listener);
		bt_raise.setOnClickListener(listener);
		bt_raise_wave.setOnClickListener(listener);
		bt_float.setOnClickListener(listener);
		bt_float_wave.setOnClickListener(listener);
		
		bt_flat_color.setOnClickListener(listener_delay);
		bt_flat_wave_color.setOnClickListener(listener_delay);
		bt_raise_color.setOnClickListener(listener_delay);
		bt_raise_wave_color.setOnClickListener(listener_delay);
		bt_float_color.setOnClickListener(listener_delay);
		bt_float_wave_color.setOnClickListener(listener_delay);


        final TimePicker tp = (TimePicker)v.findViewById(R.id.button_tp);

        bt_flat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tp.setMode(TimePicker.MODE_HOUR, true);
            }
        });

        bt_flat_color.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                tp.setMode(TimePicker.MODE_MINUTE, true);
            }
        });

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
