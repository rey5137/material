package com.rey.material.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.rey.material.R;
import com.rey.material.drawable.CircularProgressDrawable;
import com.rey.material.drawable.LinearProgressDrawable;
import com.rey.material.util.ViewUtil;

public class ProgressView extends View {

	private boolean mAutostart;
	private boolean mCircular;
	private int mProgressId;
	
	public static final int MODE_DETERMINATE = 0;
	public static final int MODE_INDETERMINATE = 1;
	public static final int MODE_BUFFER = 2;
	public static final int MODE_QUERY = 3;
	
	
	private Drawable mProgressDrawable;
	
	public ProgressView(Context context) {
		this(context, null, 0, 0);
	}

	public ProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}

    public ProgressView(Context context, AttributeSet attrs, int defStyleAttr){
        this(context, attrs, defStyleAttr, 0);
    }

	@SuppressWarnings("deprecation")
    @TargetApi(android.os.Build.VERSION_CODES.JELLY_BEAN)
	public ProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, defStyleRes);
	}

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        applyStyle(context, attrs, defStyleAttr, defStyleRes);
    }

    public void applyStyle(int resId){
        applyStyle(getContext(), null, 0, resId);
    }

    private void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressView, defStyleAttr, defStyleRes);
        mAutostart = a.getBoolean(R.styleable.ProgressView_pv_autostart, true);
        mCircular = a.getBoolean(R.styleable.ProgressView_pv_circular, true);
        mProgressId = a.getResourceId(R.styleable.ProgressView_pv_progressStyle, 0);

        if(mProgressId == 0)
            mProgressId = mCircular ? R.style.Material_Drawable_CircularProgress : R.style.Material_Drawable_LinearProgress;

        if(mCircular) {
            mProgressDrawable = new CircularProgressDrawable.Builder(context, mProgressId).build();
            if(a.hasValue(R.styleable.ProgressView_pv_progressMode))
                ((CircularProgressDrawable)mProgressDrawable).setProgressMode(a.getInt(R.styleable.ProgressView_pv_progressMode, MODE_INDETERMINATE));
        }
        else{
            mProgressDrawable = new LinearProgressDrawable.Builder(context, mProgressId).build();
            if(a.hasValue(R.styleable.ProgressView_pv_progressMode))
                ((LinearProgressDrawable)mProgressDrawable).setProgressMode(a.getInt(R.styleable.ProgressView_pv_progressMode, MODE_INDETERMINATE));
        }

        if(a.hasValue(R.styleable.ProgressView_pv_progress))
            setProgress(a.getFloat(R.styleable.ProgressView_pv_progress, 0));

        if(a.hasValue(R.styleable.ProgressView_pv_secondaryProgress))
            setSecondaryProgress(a.getFloat(R.styleable.ProgressView_pv_secondaryProgress, 0));

        a.recycle();

        ViewUtil.setBackground(this, mProgressDrawable);
    }

	@Override
	protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);

        if(changedView != this)
            return;

	    if (mAutostart) {
	    	if (visibility == GONE || visibility == INVISIBLE)
	    		stop();
            else
             	start();
        }
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
        if(getVisibility() == View.VISIBLE && mAutostart)
	    	start();
	}

	@Override
	protected void onDetachedFromWindow() {
		if (mAutostart)
	    	stop();
		
	    super.onDetachedFromWindow();
	}

	public int getProgressMode(){
		if(mCircular)
			return ((CircularProgressDrawable)mProgressDrawable).getProgressMode();
		else
			return ((LinearProgressDrawable)mProgressDrawable).getProgressMode();
	}

    /**
     * @return The current progress of this view in [0..1] range.
     */
	public float getProgress(){
		if(mCircular)
			return ((CircularProgressDrawable)mProgressDrawable).getProgress();
		else
			return ((LinearProgressDrawable)mProgressDrawable).getProgress();
	}

    /**
     * @return The current secondary progress of this view in [0..1] range.
     */
	public float getSecondaryProgress(){
		if(mCircular)
			return ((CircularProgressDrawable)mProgressDrawable).getSecondaryProgress();
		else
			return ((LinearProgressDrawable)mProgressDrawable).getSecondaryProgress();
	}

    /**
     * Set the current progress of this view.
     * @param percent The progress value in [0..1] range.
     */
	public void setProgress(float percent){
		if(mCircular)
			((CircularProgressDrawable)mProgressDrawable).setProgress(percent);
		else
			((LinearProgressDrawable)mProgressDrawable).setProgress(percent);
	}

    /**
     * Set the current secondary progress of this view.
     * @param percent The progress value in [0..1] range.
     */
	public void setSecondaryProgress(float percent){
		if(mCircular)
			((CircularProgressDrawable)mProgressDrawable).setSecondaryProgress(percent);
		else
			((LinearProgressDrawable)mProgressDrawable).setSecondaryProgress(percent);
	}

    /**
     * Start showing progress.
     */
	public void start(){
		if(mProgressDrawable != null)
			((Animatable)mProgressDrawable).start();
	}

    /**
     * Stop showing progress.
     */
	public void stop(){
		if(mProgressDrawable != null)
			((Animatable)mProgressDrawable).stop();
	}

}
