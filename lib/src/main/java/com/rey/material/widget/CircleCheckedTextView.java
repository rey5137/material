package com.rey.material.widget;

import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.animation.Interpolator;

import com.rey.material.drawable.CircleDrawable;

/**
 * Created by Rey on 2/5/2015.
 */
public class CircleCheckedTextView extends android.widget.CheckedTextView {

    private CircleDrawable mBackground;

    public CircleCheckedTextView(Context context) {
        super(context);

        setGravity(Gravity.CENTER);
        setPadding(0, 0, 0, 0);

        mBackground = new CircleDrawable();
        mBackground.setInEditMode(isInEditMode());
        mBackground.setAnimEnable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            setBackground(mBackground);
        else
            setBackgroundDrawable(mBackground);
        mBackground.setAnimEnable(true);
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackground.setColor(color);
    }

    public void setAnimDuration(int duration) {
        mBackground.setAnimDuration(duration);
    }

    public void setInterpolator(Interpolator in, Interpolator out) {
        mBackground.setInterpolator(in, out);
    }

    public void setCheckedImmediately(boolean checked){
        mBackground.setAnimEnable(false);
        setChecked(checked);
        mBackground.setAnimEnable(true);
    }

}
