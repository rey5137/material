package com.rey.material.demo;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.rey.material.widget.FloatingActionButton;
import com.rey.material.widget.Spinner;

public class SpinnersFragment extends Fragment{

	
	public static SpinnersFragment newInstance(){
		SpinnersFragment fragment = new SpinnersFragment();
		
		return fragment;
	}

    private Drawable[] mDrawables = new Drawable[2];
    private int index = 0;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_spinner, container, false);

        Spinner spn_label = (Spinner)v.findViewById(R.id.spinner_label);
        Spinner spn_no_arrow = (Spinner)v.findViewById(R.id.spinner_no_arrow);
		String[] items = new String[20];
		for(int i = 0; i < items.length; i++)
			items[i] = "Item " + String.valueOf(i + 1);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.row_spn, items);
        adapter.setDropDownViewResource(R.layout.row_spn_dropdown);
        spn_label.setAdapter(adapter);
        spn_no_arrow.setAdapter(adapter);

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
