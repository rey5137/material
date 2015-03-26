package com.rey.material.demo;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.widget.FloatingActionButton;

public class FabFragment extends Fragment{

	
	public static FabFragment newInstance(){
		FabFragment fragment = new FabFragment();
		
		return fragment;
	}

    private Drawable[] mDrawables = new Drawable[2];
    private int index = 0;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_fab, container, false);

        final FloatingActionButton fab_line = (FloatingActionButton)v.findViewById(R.id.fab_line);
        fab_line.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_line.setLineMorphingState((fab_line.getLineMorphingState() + 1) % 2, true);
            }
        });

        final FloatingActionButton fab_image = (FloatingActionButton)v.findViewById(R.id.fab_image);
        mDrawables[0] = v.getResources().getDrawable(R.drawable.ic_autorenew_white_24dp);
        mDrawables[1] = v.getResources().getDrawable(R.drawable.ic_done_white_24dp);
        fab_image.setIcon(mDrawables[index], false);
        fab_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index = (index + 1) % 2;
                fab_image.setIcon(mDrawables[index], true);
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
