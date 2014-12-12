package com.rey.material.widget;

import android.content.Context;
import android.support.v7.internal.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.View;

public class ListView extends ListViewCompat {

	private RecyclerListener mRecyclerListener;
	
	public ListView(Context context) {
		super(context);
		
		init(context, null, 0, 0);
    }

    public ListView(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	
    	init(context, attrs, 0, 0);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        
        init(context, attrs, defStyleAttr, 0);
    }

    public ListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }
    
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
    	
    	super.setRecyclerListener(new RecyclerListener() {
			
			@Override
			public void onMovedToScrapHeap(View view) {
				RippleManager.cancelRipple(view);
				
				if(mRecyclerListener != null)
					mRecyclerListener.onMovedToScrapHeap(view);
			}
			
		});
    }
    
    @Override
    public void setRecyclerListener(RecyclerListener listener) {
    	mRecyclerListener = listener;
    }

}
