package com.rey.material.demo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rey.material.widget.ProgressView;

public class ProgressFragment extends Fragment implements Callback{
	
	private ProgressView pv_circular;
	private ProgressView pv_circular_colors;
	private ProgressView pv_circular_inout;
	private ProgressView pv_circular_inout_colors;
	private ProgressView pv_circular_determinate_in_out;
	private ProgressView pv_circular_determinate;
	private ProgressView pv_linear;
	private ProgressView pv_linear_colors;
	private ProgressView pv_linear_determinate;
	private ProgressView pv_linear_query;
	private ProgressView pv_linear_buffer;
	
	private Handler mHandler;
	
	private static final int MSG_START_PROGRESS = 1000;
	private static final int MSG_STOP_PROGRESS = 1001;
	private static final int MSG_UPDATE_PROGRESS = 1002;
	private static final int MSG_UPDATE_QUERY_PROGRESS = 1003;
	private static final int MSG_UPDATE_BUFFER_PROGRESS = 1004;
	
	private static final long PROGRESS_INTERVAL = 7000;
	private static final long START_DELAY = 2000;
	private static final long PROGRESS_UPDATE_INTERVAL = PROGRESS_INTERVAL / 100;
	private static final long START_QUERY_DELAY = PROGRESS_INTERVAL / 2;
	private static final long QUERY_PROGRESS_UPDATE_INTERVAL = (PROGRESS_INTERVAL - START_QUERY_DELAY) / 100;
	private static final long BUFFER_PROGRESS_UPDATE_INTERVAL = (PROGRESS_INTERVAL - START_QUERY_DELAY) / 100;
		
	public static ProgressFragment newInstance(){
		ProgressFragment fragment = new ProgressFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_progress, container, false);
		
		pv_circular = (ProgressView)v.findViewById(R.id.progress_pv_circular);
		pv_circular_colors = (ProgressView)v.findViewById(R.id.progress_pv_circular_colors);
		pv_circular_inout = (ProgressView)v.findViewById(R.id.progress_pv_circular_inout);
		pv_circular_inout_colors = (ProgressView)v.findViewById(R.id.progress_pv_circular_inout_colors);
		pv_circular_determinate_in_out = (ProgressView)v.findViewById(R.id.progress_pv_circular_determinate_in_out);
		pv_circular_determinate = (ProgressView)v.findViewById(R.id.progress_pv_circular_determinate);
		pv_linear = (ProgressView)v.findViewById(R.id.progress_pv_linear);
		pv_linear_colors = (ProgressView)v.findViewById(R.id.progress_pv_linear_colors);
		pv_linear_determinate = (ProgressView)v.findViewById(R.id.progress_pv_linear_determinate);
		pv_linear_query = (ProgressView)v.findViewById(R.id.progress_pv_linear_query);
		pv_linear_buffer = (ProgressView)v.findViewById(R.id.progress_pv_linear_buffer);
		
		mHandler = new Handler(this);
										
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
		
		mHandler.removeCallbacksAndMessages(null);
		pv_circular_determinate_in_out.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();
		mHandler.sendEmptyMessageDelayed(MSG_START_PROGRESS, START_DELAY);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_START_PROGRESS:
				pv_circular.start();
				pv_circular_colors.start();
				pv_circular_inout.start();
				pv_circular_inout_colors.start();
				pv_circular_determinate_in_out.setProgress(0f);
				pv_circular_determinate_in_out.start();	
				pv_circular_determinate.setProgress(0f);
				pv_circular_determinate.start();	
				pv_linear.start();	
				pv_linear_colors.start();
				pv_linear_determinate.setProgress(0f);
				pv_linear_determinate.start();	
				pv_linear_query.setProgress(0f);
				pv_linear_query.start();	
				pv_linear_buffer.setProgress(0f);
				pv_linear_buffer.setSecondaryProgress(0f);
				pv_linear_buffer.start();	
				mHandler.sendEmptyMessageDelayed(MSG_STOP_PROGRESS, PROGRESS_INTERVAL);
				mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, PROGRESS_UPDATE_INTERVAL);
				mHandler.sendEmptyMessageDelayed(MSG_UPDATE_QUERY_PROGRESS, START_QUERY_DELAY);
				mHandler.sendEmptyMessageDelayed(MSG_UPDATE_BUFFER_PROGRESS, BUFFER_PROGRESS_UPDATE_INTERVAL);
				break;
			case MSG_UPDATE_QUERY_PROGRESS:
				pv_linear_query.setProgress(pv_linear_query.getProgress() + 0.01f);
				
				if(pv_linear_query.getProgress() < 1f)
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_QUERY_PROGRESS, QUERY_PROGRESS_UPDATE_INTERVAL);
				else
					pv_linear_query.stop();
				break;
			case MSG_UPDATE_BUFFER_PROGRESS:
				pv_linear_buffer.setSecondaryProgress(pv_linear_buffer.getSecondaryProgress() + 0.01f);
				
				if(pv_linear_buffer.getSecondaryProgress() < 1f)
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_BUFFER_PROGRESS, BUFFER_PROGRESS_UPDATE_INTERVAL);
				break;
			case MSG_UPDATE_PROGRESS:
				pv_circular_determinate_in_out.setProgress(pv_circular_determinate_in_out.getProgress() + 0.01f);
				pv_circular_determinate.setProgress(pv_circular_determinate.getProgress() + 0.01f);
				
				pv_linear_determinate.setProgress(pv_linear_determinate.getProgress() + 0.01f);
				pv_linear_buffer.setProgress(pv_linear_buffer.getProgress() + 0.01f);				
				if(pv_circular_determinate_in_out.getProgress() < 1f)
					mHandler.sendEmptyMessageDelayed(MSG_UPDATE_PROGRESS, PROGRESS_UPDATE_INTERVAL);	
				else{
					pv_circular_determinate_in_out.stop();
					pv_circular_determinate.stop();
					pv_linear_determinate.stop();
					pv_linear_buffer.stop();
				}
				break;
			case MSG_STOP_PROGRESS:
				pv_circular.stop();
				pv_circular_colors.stop();
				pv_circular_inout.stop();
				pv_circular_inout_colors.stop();		
				pv_linear.stop();	
				pv_linear_colors.stop();	
				mHandler.sendEmptyMessageDelayed(MSG_START_PROGRESS, START_DELAY);
				break;
		}
		return false;
	}	
	
}
