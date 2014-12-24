package com.rey.material.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.TimePicker;

/**
 * Created by Rey on 12/24/2014.
 */
public class TimePickerDialog extends Dialog{

    private TimePickerLayout mTimePickerLayout;
    private float mCornerRadius;

    public TimePickerDialog(Context context) {
        super(context);
    }

    public TimePickerDialog(Context context, int style) {
        super(context, style);
    }

    @Override
    protected void onCreate() {
        mTimePickerLayout = new TimePickerLayout(getContext());
        contentView(mTimePickerLayout);
    }

    @Override
    public Dialog applyStyle(int resId) {
        super.applyStyle(resId);

        if(resId == 0)
            return this;

        mTimePickerLayout.applyStyle(resId);
        layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return this;
    }

    @Override
    public Dialog layoutParams(int width, int height) {
        return super.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    public Dialog cornerRadius(float radius){
        mCornerRadius = radius;
        return super.cornerRadius(radius);
    }

    private class CircleCheckedTextView extends android.widget.CheckedTextView {

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

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (getMeasuredWidth() != getMeasuredHeight()) {
                int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
                int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
                super.onMeasure(spec, spec);
            }
        }

        private class CircleDrawable extends Drawable implements Animatable {

            private boolean mRunning = false;
            private long mStartTime;
            private float mAnimProgress;
            private int mAnimDuration = 1000;
            private Interpolator mInInterpolator = new DecelerateInterpolator();
            private Interpolator mOutInterpolator = new DecelerateInterpolator();

            private Paint mPaint;

            private float mX;
            private float mY;
            private float mRadius;

            private boolean mVisible;
            private boolean mInEditMode = false;
            private boolean mAnimEnable = true;

            public CircleDrawable() {
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                mPaint.setStyle(Paint.Style.FILL);
            }

            public void setInEditMode(boolean b) {
                mInEditMode = b;
            }

            public void setAnimEnable(boolean b) {
                mAnimEnable = b;
            }

            public void setColor(int color) {
                mPaint.setColor(color);
                invalidateSelf();
            }

            public void setAnimDuration(int duration) {
                mAnimDuration = duration;
            }

            public void setInterpolator(Interpolator in, Interpolator out) {
                mInInterpolator = in;
                mOutInterpolator = out;
            }

            @Override
            public boolean isStateful() {
                return true;
            }

            @Override
            protected boolean onStateChange(int[] state) {
                boolean visible = ViewUtil.hasState(state, android.R.attr.state_checked) || ViewUtil.hasState(state, android.R.attr.state_pressed);

                if (mVisible != visible) {
                    mVisible = visible;
                    if (!mInEditMode && mAnimEnable)
                        start();
                    return true;
                }

                return false;
            }

            @Override
            protected void onBoundsChange(Rect bounds) {
                mX = bounds.exactCenterX();
                mY = bounds.exactCenterY();
                mRadius = Math.min(bounds.width(), bounds.height()) / 2f;
            }

            @Override
            public void draw(Canvas canvas) {
                if (!mRunning) {
                    if (mVisible)
                        canvas.drawCircle(mX, mY, mRadius, mPaint);
                } else {
                    float radius = mVisible ? mInInterpolator.getInterpolation(mAnimProgress) * mRadius : (1f - mOutInterpolator.getInterpolation(mAnimProgress)) * mRadius;
                    canvas.drawCircle(mX, mY, radius, mPaint);
                }
            }

            @Override
            public void setAlpha(int alpha) {
                mPaint.setAlpha(alpha);
            }

            @Override
            public void setColorFilter(ColorFilter cf) {
                mPaint.setColorFilter(cf);
            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }

            private void resetAnimation() {
                mStartTime = SystemClock.uptimeMillis();
                mAnimProgress = 0f;
            }

            @Override
            public void start() {
                resetAnimation();
                scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);
                invalidateSelf();
            }

            @Override
            public void stop() {
                mRunning = false;
                unscheduleSelf(mUpdater);
                invalidateSelf();
            }

            @Override
            public boolean isRunning() {
                return mRunning;
            }

            @Override
            public void scheduleSelf(Runnable what, long when) {
                mRunning = true;
                super.scheduleSelf(what, when);
            }

            private final Runnable mUpdater = new Runnable() {

                @Override
                public void run() {
                    update();
                }

            };

            private void update() {
                long curTime = SystemClock.uptimeMillis();
                mAnimProgress = Math.min(1f, (float) (curTime - mStartTime) / mAnimDuration);

                if (mAnimProgress == 1f)
                    mRunning = false;

                if (isRunning())
                    scheduleSelf(mUpdater, SystemClock.uptimeMillis() + ViewUtil.FRAME_DURATION);

                invalidateSelf();
            }

        }

    }

    private class TimePickerLayout extends android.widget.FrameLayout implements View.OnClickListener{

        private int mHeaderHeight;
        private int mTextTimeColor;
        private int mTextHighlightColor;
        private int mTextTimeSize;
        private int mTextSize;

        private boolean mIsAm = true;
        private int mCheckBoxSize;

        private int mHeaderRealWidth;
        private int mHeaderRealHeight;

        private CircleCheckedTextView mAmView;
        private CircleCheckedTextView mPmView;
        private TimePicker mTimePicker;

        private Paint mPaint;
        private Path mHeaderBackground;
        private RectF mRect;

        public TimePickerLayout(Context context) {
            super(context);

            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHeaderBackground = new Path();
            mRect = new RectF();

            mAmView = new CircleCheckedTextView(context);
            mPmView = new CircleCheckedTextView(context);
            mTimePicker = new TimePicker(context);

            mTimePicker.setPadding(mContentPadding, mContentPadding, mContentPadding, mContentPadding);
            mAmView.setCheckedImmediately(mIsAm);
            mPmView.setCheckedImmediately(!mIsAm);
            mAmView.setOnClickListener(this);
            mPmView.setOnClickListener(this);

            addView(mTimePicker);
            addView(mAmView);
            addView(mPmView);

            setWillNotDraw(false);
        }

        public void applyStyle(int resId){
            mTimePicker.applyStyle(resId);

            Context context = getContext();
            mCheckBoxSize = ThemeUtil.dpToPx(context, 48);

            TypedArray a = context.obtainStyledAttributes(resId, R.styleable.TimePickerDialog);
            mHeaderHeight = a.getDimensionPixelSize(R.styleable.TimePickerDialog_tp_headerHeight, ThemeUtil.dpToPx(context, 120));
            mTextTimeColor = a.getColor(R.styleable.TimePickerDialog_tp_textTimeColor, 0xFF000000);
            mTextTimeSize = a.getDimensionPixelSize(R.styleable.TimePickerDialog_tp_textTimeSize, ThemeUtil.spToPx(context, 24));
            String am = a.getString(R.styleable.TimePickerDialog_tp_am);
            String pm = a.getString(R.styleable.TimePickerDialog_tp_pm);
            a.recycle();

            int[][] states = new int[][]{
                    new int[]{-android.R.attr.state_checked},
                    new int[]{android.R.attr.state_checked},
            };
            int[] colors = new int[]{
                    mTimePicker.getTextColor(),
                    mTimePicker.getTextHighlightColor(),
            };
            mAmView.setBackgroundColor(mTimePicker.getSelectionColor());
            mAmView.setAnimDuration(mTimePicker.getAnimDuration());
            mAmView.setInterpolator(mTimePicker.getInInterpolator(), mTimePicker.getOutInterpolator());
            mAmView.setTypeface(mTimePicker.getTypeface());
            mAmView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTimePicker.getTextSize());
            mAmView.setTextColor(new ColorStateList(states, colors));
            mAmView.setText(am);

            mPmView.setBackgroundColor(mTimePicker.getSelectionColor());
            mPmView.setAnimDuration(mTimePicker.getAnimDuration());
            mPmView.setInterpolator(mTimePicker.getInInterpolator(), mTimePicker.getOutInterpolator());
            mPmView.setTypeface(mTimePicker.getTypeface());
            mPmView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTimePicker.getTextSize());
            mPmView.setTextColor(new ColorStateList(states, colors));
            mPmView.setText(pm);

            mPaint.setTypeface(mTimePicker.getTypeface());
        }

        @Override
        public void onClick(View v) {
            boolean isAm = v == mAmView;
            if(mIsAm != isAm){
                mIsAm = isAm;
                mAmView.setChecked(mIsAm);
                mPmView.setChecked(!mIsAm);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                if(heightMode == MeasureSpec.AT_MOST) {
                    heightSize = Math.min(heightSize, mCheckBoxSize + widthSize + mHeaderHeight);

                    int spec = MeasureSpec.makeMeasureSpec(mCheckBoxSize, MeasureSpec.EXACTLY);
                    mAmView.measure(spec, spec);
                    mPmView.measure(spec, spec);

                    spec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                    mTimePicker.measure(spec, spec);
                }

                setMeasuredDimension(widthSize, heightSize);
            }

            System.out.println("measure: " + widthSize + " " + heightSize + " " + getMeasuredWidth() + " " + getMeasuredHeight());
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;


            if(isPortrait){
                mHeaderRealWidth = w;
                mHeaderRealHeight = h - mCheckBoxSize - w;

                System.out.println(getMeasuredWidth() + " " + getMeasuredHeight() + " " + w + " " + h + " " + mHeaderRealWidth + " " + mHeaderRealHeight);

                mHeaderBackground.reset();
//                if(mCornerRadius == 0)
                    mHeaderBackground.addRect(0, 0, mHeaderRealWidth, mHeaderRealHeight, Path.Direction.CW);
//                else{
//                    mHeaderBackground.moveTo(0, mHeaderRealHeight);
//                    mHeaderBackground.lineTo(0, mCornerRadius);
//                    mRect.set(0, 0, mCornerRadius * 2, mCornerRadius * 2);
//                    mHeaderBackground.arcTo(mRect, (float)Math.PI, (float)Math.PI / 4, true);
//                    mHeaderBackground.lineTo(mHeaderRealWidth - mCornerRadius, 0);
//                    mRect.set(mHeaderRealWidth - mCornerRadius * 2, 0, mHeaderRealWidth, mCornerRadius * 2);
//                    mHeaderBackground.arcTo(mRect, (float)Math.PI * 3 / 2, (float)Math.PI / 4, true);
//                    mHeaderBackground.lineTo(mHeaderRealWidth, mHeaderRealHeight);
//                    mHeaderBackground.close();
//                }

            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = 0;
            int childTop = 0;
            int childRight = right - left;
            int childBottom = bottom - top;

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                int paddingHorizontal = mContentPadding + mActionPadding;
                int paddingVertical = mContentPadding - mActionPadding;
                mAmView.layout(childLeft + paddingHorizontal, childBottom - paddingVertical - mCheckBoxSize, childLeft + paddingHorizontal + mCheckBoxSize, childBottom - paddingVertical);
                mPmView.layout(childRight - paddingHorizontal - mCheckBoxSize, childBottom - paddingVertical - mCheckBoxSize, childRight - paddingHorizontal, childBottom - paddingVertical);

                childTop += mHeaderRealHeight;
                childBottom -= mCheckBoxSize;
                mTimePicker.layout(childLeft, childTop, childRight, childBottom);
            }
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            boolean isPortrait = getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

            if(isPortrait){
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mTimePicker.getSelectionColor());
                canvas.drawPath(mHeaderBackground, mPaint);
            }
        }
    }
}
