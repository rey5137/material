package com.rey.material.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.Button;
import com.rey.material.widget.SnackBar;

public class SnackbarFragment extends Fragment{
	
	SnackBar mSnackBar;
		
	public static SnackbarFragment newInstance(){
		SnackbarFragment fragment = new SnackbarFragment();
		
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_snackbar, container, false);
		
		Button bt_mobile_single = (Button)v.findViewById(R.id.snackbar_bt_mobile_single);
		Button bt_mobile_multi = (Button)v.findViewById(R.id.snackbar_bt_mobile_multi);
		Button bt_tablet_single = (Button)v.findViewById(R.id.snackbar_bt_tablet_single);
		Button bt_tablet_multi = (Button)v.findViewById(R.id.snackbar_bt_tablet_multi);
		
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mSnackBar.getState() == SnackBar.STATE_SHOWED)
					mSnackBar.dismiss();
				else{
					switch (v.getId()) {
						case R.id.snackbar_bt_mobile_single:
							mSnackBar.text("This is single-line snackbar.")
									.actionText("CLOSE")
									.singleLine(true)
									.width(SnackBar.MATCH_PARENT)
									.minWidth(0)
									.maxWidth(0)
									.height(ThemeUtil.dpToPx(getActivity(), 48))
                                    .maxHeight(0)
									.marginLeft(0)
									.marginBottom(0)
									.verticalPadding(0)
									.show();
							break;
						case R.id.snackbar_bt_mobile_multi:
							mSnackBar.text("This is multi-line snackbar.\nIt will auto-close after 5s.")
									.actionText(null)
									.singleLine(false)
									.maxLines(2)
									.width(SnackBar.MATCH_PARENT)
									.minWidth(0)
									.maxWidth(0)
									.height(SnackBar.WRAP_CONTENT)
                                    .maxHeight(ThemeUtil.dpToPx(getActivity(), 80))
									.marginLeft(0)
									.marginBottom(0)
									.verticalPadding(ThemeUtil.dpToPx(getActivity(), 12))
									.duration(5000)
									.show();
							break;
						case R.id.snackbar_bt_tablet_single:
							mSnackBar.text("This is single-line snackbar.")
									.actionText("CLOSE")
									.singleLine(true)
									.width(SnackBar.WRAP_CONTENT)
									.minWidth(ThemeUtil.dpToPx(getActivity(), 288))
									.maxWidth(ThemeUtil.dpToPx(getActivity(), 568))
									.height(ThemeUtil.dpToPx(getActivity(), 48))
                                    .maxHeight(0)
									.marginLeft(ThemeUtil.dpToPx(getActivity(), 16))
									.marginBottom(ThemeUtil.dpToPx(getActivity(), 16))
									.verticalPadding(0)
									.show();
							break;
						case R.id.snackbar_bt_tablet_multi:
							mSnackBar.text("This is multi-line snackbar.\nIt will auto-close after 5s.")
									.actionText(null)
									.singleLine(false)
									.maxLines(2)
									.width(SnackBar.WRAP_CONTENT)
									.minWidth(ThemeUtil.dpToPx(getActivity(), 288))
									.maxWidth(ThemeUtil.dpToPx(getActivity(), 568))
									.height(SnackBar.WRAP_CONTENT)
                                    .maxHeight(ThemeUtil.dpToPx(getActivity(), 80))
									.marginLeft(ThemeUtil.dpToPx(getActivity(), 16))
									.marginBottom(ThemeUtil.dpToPx(getActivity(), 16))
									.verticalPadding(ThemeUtil.dpToPx(getActivity(), 12))
									.duration(5000)
									.show();
							break;
					}
				}				
			}
		};
		
		
		bt_mobile_single.setOnClickListener(listener);
		bt_mobile_multi.setOnClickListener(listener);
		bt_tablet_single.setOnClickListener(listener);
		bt_tablet_multi.setOnClickListener(listener);
		
		mSnackBar = ((MainActivity)getActivity()).getSnackBar();
				
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
