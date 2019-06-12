package com.rey.material.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rey.material.widget.Slider;

public class SliderFragment extends Fragment{

	
	public static SliderFragment newInstance(){
		SliderFragment fragment = new SliderFragment();
		
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_slider, container, false);

        Slider sl_continuous = (Slider)v.findViewById(R.id.slider_sl_continuous);
        final TextView tv_continuous = (TextView)v.findViewById(R.id.slider_tv_continuous);
        tv_continuous.setText(String.format("pos=%.1f value=%d", sl_continuous.getPosition(), sl_continuous.getValue()));
        sl_continuous.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                tv_continuous.setText(String.format("pos=%.1f value=%d", newPos, newValue));
            }
        });

        Slider sl_discrete = (Slider)v.findViewById(R.id.slider_sl_discrete);
        final TextView tv_discrete = (TextView)v.findViewById(R.id.slider_tv_discrete);
        tv_discrete.setText(String.format("pos=%.1f value=%d", sl_discrete.getPosition(), sl_discrete.getValue()));
        sl_discrete.setOnPositionChangeListener(new Slider.OnPositionChangeListener() {
            @Override
            public void onPositionChanged(Slider view, boolean fromUser, float oldPos, float newPos, int oldValue, int newValue) {
                tv_discrete.setText(String.format("pos=%.1f value=%d", newPos, newValue));
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
