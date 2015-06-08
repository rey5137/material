package com.rey.material.widget;

import android.content.Context;
import android.support.v7.internal.widget.ListViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.rey.material.app.ThemeManager;
import com.rey.material.util.ViewUtil;

public class ListView extends ListViewCompat implements ThemeManager.OnThemeChangedListener{

	private RecyclerListener mRecyclerListener;

    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;
	
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

        mStyleId = ThemeManager.getStyleId(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId){
        ViewUtil.applyStyle(this, resId);
        applyStyle(getContext(), null, 0, resId);
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
    }

    @Override
    public void onThemeChanged(ThemeManager.OnThemeChangedEvent event) {
        int style = ThemeManager.getInstance().getCurrentStyle(mStyleId);
        if(mCurrentStyle != style){
            mCurrentStyle = style;
            applyStyle(mCurrentStyle);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if(mStyleId != 0) {
            ThemeManager.getInstance().registerOnThemeChangedListener(this);
            onThemeChanged(null);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(mStyleId != 0)
            ThemeManager.getInstance().unregisterOnThemeChangedListener(this);
    }

    @Override
    public void setRecyclerListener(RecyclerListener listener) {
    	mRecyclerListener = listener;
    }

}
