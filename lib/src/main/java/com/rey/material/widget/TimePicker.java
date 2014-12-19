package com.rey.material.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.rey.material.R;
import com.rey.material.util.ColorUtil;
import com.rey.material.util.ThemeUtil;

/**
 * Created by Rey on 12/19/2014.
 */
public class TimePicker extends View{

    private int mBackgroundColor;
    private int mSelectionColor;
    private int mSelectionRadius;
    private int mTickSize;
    private int mTextSize;
    private int mTextColor;

    private int mAnimDuration;
    private Interpolator mInterpolator;

    private Paint mPaint;

    private PointF mCenterPoint;
    private float mOuterRadius;
    private float mInnerRadius;

    private float mCurAngle;

    private float[] mLocations = new float[48];
    private Rect mRect;
    private static final String[] TICKS = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55", "00"};

    private int mMode = MODE_HOUR;

    public static final int MODE_HOUR = 0;
    public static final int MODE_MINUE = 1;

    public TimePicker(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        mBackgroundColor = a.getColor(R.styleable.TimePicker_tp_backgroundColor, ColorUtil.getColor(ThemeUtil.colorPrimary(context, 0xFF000000), 0.25f));
        mSelectionColor = a.getColor(R.styleable.TimePicker_tp_selectionColor, ThemeUtil.colorPrimary(context, 0xFF000000));
        mSelectionRadius = a.getDimensionPixelOffset(R.styleable.TimePicker_tp_selectionRadius, ThemeUtil.dpToPx(context, 8));
        mTickSize = a.getDimensionPixelSize(R.styleable.TimePicker_tp_tickSize, ThemeUtil.dpToPx(context, 1));
        mTextSize = a.getDimensionPixelSize(R.styleable.TimePicker_tp_textSize, context.getResources().getDimensionPixelOffset(R.dimen.abc_text_size_caption_material));
        mTextColor = a.getColor(R.styleable.TimePicker_tp_textColor, 0xFFFFFFFF);
        mAnimDuration = a.getInteger(R.styleable.TimePicker_tp_animDuration, context.getResources().getInteger(android.R.integer.config_mediumAnimTime));
        int resId = a.getResourceId(R.styleable.TimePicker_tp_interpolator, 0);
        mInterpolator = resId == 0 ? new DecelerateInterpolator() : AnimationUtils.loadInterpolator(context, resId);

        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = (widthMode == MeasureSpec.UNSPECIFIED) ?  mSelectionRadius * 12 : MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = (heightMode == MeasureSpec.UNSPECIFIED) ?  mSelectionRadius * 12 : MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();

        int size = Math.min(widthSize, heightSize);

        int width = (widthMode == MeasureSpec.EXACTLY) ? widthSize : size;
        int height = (heightMode == MeasureSpec.EXACTLY) ? heightSize : size;

        setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(), height + getPaddingTop() + getPaddingBottom());
    }

    private void calculateTextLocation(){
        double step = Math.PI / 6;
        double angle = -Math.PI / 3;
        float x, y;

        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setTextAlign(Paint.Align.LEFT);

        for(int i = 0; i < TICKS.length; i++){
            x = mCenterPoint.x + (float)Math.cos(angle) * mInnerRadius;
            y = mCenterPoint.y + (float)Math.sin(angle) * mInnerRadius;

            mPaint.getTextBounds(TICKS[i], 0, TICKS[i].length(), mRect);
            mLocations[i * 2] = x - mRect.width() / 2f;
            mLocations[i * 2 + 1] = y + mRect.height() / 2f;

            angle += step;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int size = Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom());

        if(mCenterPoint == null)
            mCenterPoint = new PointF();

        mOuterRadius = size / 2f;
        mCenterPoint.set(left + mOuterRadius, top + mOuterRadius);
        mInnerRadius = mOuterRadius - mSelectionRadius - ThemeUtil.dpToPx(getContext(), 4);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        mPaint.setColor(mBackgroundColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mOuterRadius, mPaint);

        mPaint.setColor(mSelectionColor);
        float x = mCenterPoint.x + (float)Math.cos(mCurAngle) * mInnerRadius;
        float y = mCenterPoint.y + (float)Math.sin(mCurAngle) * mInnerRadius;
        canvas.drawCircle(x, y, mSelectionRadius, mPaint);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mTickSize);
        x -= (float)Math.cos(mCurAngle) * mSelectionRadius;
        y -= (float)Math.sin(mCurAngle) * mSelectionRadius;
        canvas.drawLine(mCenterPoint.x, mCenterPoint.y, x, y, mPaint);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mTextColor);
        canvas.drawCircle(mCenterPoint.x, mCenterPoint.y, mTickSize * 2, mPaint);

        calculateTextLocation();

        mPaint.setTextSize(mTextSize);
        mPaint.setTypeface(Typeface.DEFAULT);
        mPaint.setTextAlign(Paint.Align.LEFT);

        if(mMode == MODE_HOUR)
            for(int i = 0; i < 12; i++)
                canvas.drawText(TICKS[i], mLocations[i * 2], mLocations[i * 2 + 1], mPaint);
        else if(mMode == MODE_MINUE)
            for(int i = 12; i < 24; i++)
                canvas.drawText(TICKS[i], mLocations[i * 2], mLocations[i * 2 + 1], mPaint);
    }
}
