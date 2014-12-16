package com.rey.material.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.rey.material.R;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.Button;
import com.rey.material.widget.TextView;

/**
 * Created by Rey on 12/10/2014.
 */
public class Dialog extends android.app.Dialog{

    private ContainerFrameLayout mContainer;
    private int mLayoutWidth;
    private int mLayoutHeight;

    private TextView mTitle;
    private Button mPositiveAction;
    private Button mNegativeAction;
    private View mContent;
    private CardView mBackground;

    protected int mContentPadding;
    protected int mActionHeight;
    protected int mActionOuterHeight;
    protected int mActionOuterPadding;
    protected int mActionMinWidth;
    protected int mActionPadding;

    private boolean mLayoutActionVertical = false;

    public Dialog(Context context) {
        super(context, android.R.style.Theme_Panel);

        init(context, 0);
    }

    public Dialog(Context context, int style) {
        super(context, android.R.style.Theme_Panel);

        init(context, style);
    }

    private void init(Context context, int style){
        mContentPadding = ThemeUtil.dpToPx(context, 24);
        mActionMinWidth = ThemeUtil.dpToPx(context, 64);
        mActionHeight = ThemeUtil.dpToPx(context, 36);
        mActionOuterHeight = ThemeUtil.dpToPx(context, 48);
        mActionPadding = ThemeUtil.dpToPx(context, 8);
        mActionOuterPadding = ThemeUtil.dpToPx(context, 16);

        mBackground = new CardView(context);
        mContainer = new ContainerFrameLayout(context);
        mTitle = new TextView(context);
        mPositiveAction = new Button(context);
        mNegativeAction = new Button(context);

        mTitle.setPadding(mContentPadding, mContentPadding, mContentPadding, mContentPadding - mActionPadding);
        mPositiveAction.setPadding(mActionPadding, 0, mActionPadding, 0);
        mPositiveAction.setBackgroundResource(0);
        mNegativeAction.setPadding(mActionPadding, 0, mActionPadding, 0);
        mNegativeAction.setBackgroundResource(0);


        mContainer.addView(mBackground);
        mContainer.addView(mTitle);
        mContainer.addView(mPositiveAction);
        mContainer.addView(mNegativeAction);

        applyStyle(style);

        clearContent();

        super.setContentView(mContainer);
    }

    public void applyStyle(int resId){
        if(resId == 0)
            return;

        Context context = getContext();
        TypedArray a = context.obtainStyledAttributes(resId, R.styleable.Dialog);

        int layout_width;
        int layout_height;

        if(ThemeUtil.getType(a, R.styleable.Dialog_android_layout_width) == TypedValue.TYPE_DIMENSION)
            layout_width = a.getDimensionPixelSize(R.styleable.Dialog_android_layout_width, 0);
        else
            layout_width = a.getInteger(R.styleable.Dialog_android_layout_width, ViewGroup.LayoutParams.WRAP_CONTENT);

        if(ThemeUtil.getType(a, R.styleable.Dialog_android_layout_height) == TypedValue.TYPE_DIMENSION)
            layout_height = a.getDimensionPixelSize(R.styleable.Dialog_android_layout_height, 0);
        else
            layout_height = a.getInteger(R.styleable.Dialog_android_layout_height, ViewGroup.LayoutParams.WRAP_CONTENT);

        setLayoutParams(layout_width, layout_height);

        setDimAmount(a.getFloat(R.styleable.Dialog_di_dimAmount, 0.5f));
        setBackgroundColor(a.getColor(R.styleable.Dialog_di_backgroundColor, 0xFFFFFFFF));/**/
        setMaxElevation(a.getDimensionPixelOffset(R.styleable.Dialog_di_maxElevation, 0));
        setElevation(a.getDimensionPixelOffset(R.styleable.Dialog_di_elevation, 0));
        setCornerRadius(a.getDimensionPixelOffset(R.styleable.Dialog_di_cornerRadius, 0));

        setTitleTextAppearance(a.getResourceId(R.styleable.Dialog_di_titleTextAppearance, R.style.TextAppearance_AppCompat_Title));
        if(ThemeUtil.getType(a, R.styleable.Dialog_di_titleTextColor) != TypedValue.TYPE_NULL)
            setTitleColor(a.getColor(R.styleable.Dialog_di_titleTextColor, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_actionBackground) != TypedValue.TYPE_NULL)
            setActionBackground(a.getResourceId(R.styleable.Dialog_di_actionBackground, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_actionRipple) != TypedValue.TYPE_NULL)
            setActionRipple(a.getResourceId(R.styleable.Dialog_di_actionRipple, 0));

        setActionTextAppearance(a.getResourceId(R.styleable.Dialog_di_actionTextAppearance, R.style.TextAppearance_AppCompat_Button));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_actionTextColor) != TypedValue.TYPE_NULL)
            setActionTextColor(a.getColorStateList(R.styleable.Dialog_di_actionTextColor));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionBackground) != TypedValue.TYPE_NULL)
            setPositiveActionBackground(a.getResourceId(R.styleable.Dialog_di_positiveActionBackground, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionRipple) != TypedValue.TYPE_NULL)
            setPositiveActionRipple(a.getResourceId(R.styleable.Dialog_di_positiveActionRipple, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionTextAppearance) != TypedValue.TYPE_NULL)
            setPositiveActionTextAppearance(a.getResourceId(R.styleable.Dialog_di_positiveActionTextAppearance, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionTextColor) != TypedValue.TYPE_NULL)
            setPositiveActionTextColor(a.getColorStateList(R.styleable.Dialog_di_positiveActionTextColor));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionBackground) != TypedValue.TYPE_NULL)
            setNegativeActionBackground(a.getResourceId(R.styleable.Dialog_di_negativeActionBackground, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionRipple) != TypedValue.TYPE_NULL)
            setNegativeActionRipple(a.getResourceId(R.styleable.Dialog_di_negativeActionRipple, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionTextAppearance) != TypedValue.TYPE_NULL)
            setNegativeActionTextAppearance(a.getResourceId(R.styleable.Dialog_di_negativeActionTextAppearance, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionTextColor) != TypedValue.TYPE_NULL)
            setNegativeActionTextColor(a.getColorStateList(R.styleable.Dialog_di_negativeActionTextColor));

        setDividerColor(a.getColor(R.styleable.Dialog_di_dividerColor, 0x1E000000));
        setDividerHeight(a.getDimensionPixelSize(R.styleable.Dialog_di_dividerHeight, ThemeUtil.dpToPx(context, 1)));
        setCancelable(a.getBoolean(R.styleable.Dialog_di_cancelable, true));
        setCanceledOnTouchOutside(a.getBoolean(R.styleable.Dialog_di_canceledOnTouchOutside, true));

        a.recycle();

    }

    public void clearContent(){
        setTitle(0);
        setPositiveAction(0);
        setPositiveActionClickListener(null);
        setNegativeAction(0);
        setNegativeActionClickListener(null);
        setContentView(null);
    }

    public void setLayoutParams(int width, int height){
        mLayoutWidth = width;
        mLayoutHeight = height;
    }

    public void setDimAmount(float amount){
        Window window = getWindow();
        if(amount > 0f){
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = amount;
            window.setAttributes(lp);
        }
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }/**/

    public void setBackgroundColor(int color){
        mBackground.setCardBackgroundColor(color);
    }

    public void setElevation(float radius){
        if(mBackground.getMaxCardElevation() < radius)
            mBackground.setMaxCardElevation(radius);

        mBackground.setCardElevation(radius);
    }

    public void setMaxElevation(float radius){
        mBackground.setMaxCardElevation(radius);
    }

    public void setCornerRadius(float radius){
        mBackground.setRadius(radius);
    }

    public void setDividerColor(int color){
        mContainer.setDividerColor(color);
    }

    public void setDividerHeight(int height){
        mContainer.setDividerHeight(height);
    }

    @Override
    public void setTitle(CharSequence title){
        mTitle.setText(title);
        mTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setTitle(int id){
        setTitle(id == 0 ? null : getContext().getResources().getString(id));
    }

    public void setTitleColor(int color){
        mTitle.setTextColor(color);
    }

    public void setTitleTextAppearance(int resId){
        mTitle.setTextAppearance(getContext(), resId);
    }

    public void setActionBackground(int id){
        setPositiveActionBackground(id);
        setNegativeActionBackground(id);
    }

    public void setActionBackground(Drawable drawable){
        setPositiveActionBackground(drawable);
        setNegativeActionBackground(drawable);
    }

    public void setActionRipple(int resId){
        setPositiveActionRipple(resId);
        setNegativeActionRipple(resId);
    }

    public void setActionTextAppearance(int resId){
        setPositiveActionTextAppearance(resId);
        setNegativeActionTextAppearance(resId);
    }

    public void setActionTextColor(ColorStateList color){
        this.setPositiveActionTextColor(color);
        setNegativeActionTextColor(color);
    }

    public void setActionTextColor(int color){
        setPositiveActionTextColor(color);
        setNegativeActionTextColor(color);
    }

    public void setPositiveAction(CharSequence action){
        mPositiveAction.setText(action);
        mPositiveAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
    }

    public void setPositiveAction(int id){
        setPositiveAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public void setPositiveActionBackground(Drawable drawable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mPositiveAction.setBackground(drawable);
        else
            mPositiveAction.setBackgroundDrawable(drawable);
    }

    public void setPositiveActionBackground(int id){
        setPositiveActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    public void setPositiveActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        setPositiveActionBackground(drawable);
    }

    public void setPositiveActionTextAppearance(int resId){
        mPositiveAction.setTextAppearance(getContext(), resId);
    }

    public void setPositiveActionTextColor(ColorStateList color){
        mPositiveAction.setTextColor(color);
    }

    public void setPositiveActionTextColor(int color){
        mPositiveAction.setTextColor(color);
    }

    public void setPositiveActionClickListener(View.OnClickListener listener){
        mPositiveAction.setOnClickListener(listener);
    }

    public void setNegativeAction(CharSequence action){
        mNegativeAction.setText(action);
        mNegativeAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
    }

    public void setNegativeAction(int id){
        setNegativeAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public void setNegativeActionBackground(Drawable drawable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mNegativeAction.setBackground(drawable);
        else
            mNegativeAction.setBackgroundDrawable(drawable);
    }

    public void setNegativeActionBackground(int id){
        setNegativeActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    public void setNegativeActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        setNegativeActionBackground(drawable);
    }

    public void setNegativeActionTextAppearance(int resId){
        mNegativeAction.setTextAppearance(getContext(), resId);
    }

    public void setNegativeActionTextColor(ColorStateList color){
        mNegativeAction.setTextColor(color);
    }

    public void setNegativeActionTextColor(int color){
        mNegativeAction.setTextColor(color);
    }

    public void setNegativeActionClickListener(View.OnClickListener listener){
        mNegativeAction.setOnClickListener(listener);
    }

    public void setShowDivider(boolean show){
        mContainer.setShowDivider(show);
    }

    @Override
    public void setContentView(View v){
        if(mContent != v) {
            if(mContent != null)
                mContainer.removeView(mContent);

            mContent = v;
        }

        if(mContent != null)
            mContainer.addView(mContent);
    }

    @Override
    public void setContentView(int layoutId){
        View v = LayoutInflater.from(getContext()).inflate(layoutId, null);
        setContentView(v);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        setContentView(view);
    }

    private class ContainerFrameLayout extends FrameLayout{

        private Point mWindowSize;
        private Paint mDividerPaint;
        private float mDividerPos = -1f;
        private boolean mShowDivider = false;

        public ContainerFrameLayout(Context context) {
            super(context);

            mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDividerPaint.setStyle(Paint.Style.STROKE);
            setWillNotDraw(false);
        }

        public void setDividerColor(int color){
            mDividerPaint.setColor(color);
            invalidate();
        }

        public void setDividerHeight(int height){
            mDividerPaint.setStrokeWidth(height);
            invalidate();
        }

        public void setShowDivider(boolean show){
            if(mShowDivider != show) {
                mShowDivider = show;
                invalidate();
            }
        }

        private Point getWindowSize(){
            if(mWindowSize == null)
                mWindowSize = new Point();

//            View v = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
//            mWindowSize.set(v.getRight() - v.getLeft(), v.getBottom() - v.getTop());

            Display display = getWindow().getWindowManager().getDefaultDisplay();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
                display.getSize(mWindowSize);
            else
                mWindowSize.set(display.getWidth(), display.getHeight());

            return mWindowSize;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            Point windowSize = getWindowSize();
            int maxWidth = windowSize.x - mBackground.getPaddingLeft() - mBackground.getPaddingRight();
            int maxHeight = windowSize.y - mBackground.getPaddingTop() - mBackground.getPaddingBottom();

            int width = mLayoutWidth == ViewGroup.LayoutParams.MATCH_PARENT ? maxWidth : mLayoutWidth;
            int height = mLayoutHeight == ViewGroup.LayoutParams.MATCH_PARENT ? maxHeight : mLayoutHeight;

            int widthMs = MeasureSpec.makeMeasureSpec(width == ViewGroup.LayoutParams.WRAP_CONTENT ? maxWidth : width, MeasureSpec.AT_MOST);
            int heightMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

            int titleWidth = 0;
            int titleHeight = mContentPadding;

            if(mTitle.getVisibility() == View.VISIBLE){
                mTitle.measure(widthMs, heightMs);
                titleWidth = mTitle.getMeasuredWidth();
                titleHeight = mTitle.getMeasuredHeight();
            }

            int contentWidth = 0;
            int contentHeight = 0;

            if(mContent != null){
                mContent.measure(widthMs, heightMs);
                contentWidth = mContent.getMeasuredWidth();
                contentHeight = mContent.getMeasuredHeight();
            }

            int positiveActionWidth = 0;
            int positiveActionHeight = 0;

            if(mPositiveAction.getVisibility() == View.VISIBLE){
                widthMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                heightMs = MeasureSpec.makeMeasureSpec(mActionHeight, MeasureSpec.EXACTLY);
                mPositiveAction.measure(widthMs, heightMs);

                positiveActionWidth = mPositiveAction.getMeasuredWidth();
                positiveActionHeight = mPositiveAction.getMeasuredHeight();

                if(positiveActionWidth < mActionMinWidth){
                    mPositiveAction.measure(MeasureSpec.makeMeasureSpec(mActionMinWidth, MeasureSpec.EXACTLY), heightMs);
                    positiveActionWidth = mActionMinWidth;
                }
            }

            int negativeActionWidth = 0;
            int negativeActionHeight = 0;

            if(mNegativeAction.getVisibility() == View.VISIBLE){
                widthMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                heightMs = MeasureSpec.makeMeasureSpec(mActionHeight, MeasureSpec.EXACTLY);
                mNegativeAction.measure(widthMs, heightMs);

                negativeActionWidth = mNegativeAction.getMeasuredWidth();
                negativeActionHeight = mNegativeAction.getMeasuredHeight();

                if(negativeActionWidth < mActionMinWidth){
                    mNegativeAction.measure(MeasureSpec.makeMeasureSpec(mActionMinWidth, MeasureSpec.EXACTLY), heightMs);
                    negativeActionWidth = mActionMinWidth;
                }
            }

            int actionBarWidth = positiveActionWidth + negativeActionWidth + mActionOuterPadding * 2 + (positiveActionWidth >= 0 && negativeActionWidth >= 0 ? mActionPadding : 0);

            if(width == ViewGroup.LayoutParams.WRAP_CONTENT)
                width = Math.min(maxWidth, Math.max(titleWidth, Math.max(contentWidth, actionBarWidth)));

            mLayoutActionVertical = actionBarWidth > width;

            int nonContentHeight = titleHeight + mActionPadding;
            if(mLayoutActionVertical)
                nonContentHeight += (positiveActionHeight > 0 ? mActionOuterHeight : 0) + (negativeActionHeight > 0 ? mActionOuterHeight : 0);
            else
                nonContentHeight += (positiveActionHeight > 0 || negativeActionWidth > 0 ? mActionOuterHeight : 0);

            if(height == ViewGroup.LayoutParams.WRAP_CONTENT){
                height = contentHeight + nonContentHeight;

                if(height > maxHeight){
                    if(contentHeight > 0)
                        mContent.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(maxHeight - nonContentHeight, MeasureSpec.EXACTLY));

                    height = maxHeight;
                }
            }
            else{
                if(contentHeight > 0 && contentHeight != height - nonContentHeight)
                    mContent.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - nonContentHeight, MeasureSpec.EXACTLY));
            }

            int dialogWidth = width + mBackground.getPaddingLeft() + mBackground.getPaddingRight();
            int dialogHeight = height + mBackground.getPaddingTop() + mBackground.getPaddingBottom();
            mBackground.measure(MeasureSpec.makeMeasureSpec(dialogWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(dialogHeight, MeasureSpec.EXACTLY));

            setMeasuredDimension(dialogWidth, dialogHeight);

        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = 0;
            int childTop = 0;
            int childRight = right - left;
            int childBottom = bottom - top;

            mBackground.layout(childLeft, childTop, childRight, childBottom);

            childLeft += mBackground.getPaddingLeft();
            childTop += mBackground.getPaddingTop();
            childRight -= mBackground.getPaddingRight();
            childBottom -= mBackground.getPaddingBottom();

            if(mTitle.getVisibility() == View.VISIBLE) {
                mTitle.layout(childLeft, childTop, childRight, childTop + mTitle.getMeasuredHeight());
                childTop += mTitle.getMeasuredHeight();
            }
            else
                childTop += mContentPadding;

            childBottom -= mActionPadding;

            int temp = (mActionOuterHeight - mActionHeight) / 2;

            if(mLayoutActionVertical){
                if(mNegativeAction.getVisibility() == View.VISIBLE){
                    mNegativeAction.layout(childRight - mActionOuterPadding - mNegativeAction.getMeasuredWidth(), childBottom - mActionOuterHeight + temp, childRight - mActionOuterPadding, childBottom - temp);
                    childBottom -= mActionOuterHeight;
                }

                if(mPositiveAction.getVisibility() == View.VISIBLE){
                    mPositiveAction.layout(childRight - mActionOuterPadding - mPositiveAction.getMeasuredWidth(), childBottom - mActionOuterHeight + temp, childRight - mActionOuterPadding, childBottom - temp);
                    childBottom -= mActionOuterHeight;
                }
            }
            else{
                int actionRight = childRight - mActionOuterPadding;
                boolean hasAction = false;

                if(mPositiveAction.getVisibility() == View.VISIBLE){
                    mPositiveAction.layout(actionRight - mPositiveAction.getMeasuredWidth(), childBottom - mActionOuterHeight + temp, actionRight, childBottom - temp);
                    actionRight -= mPositiveAction.getMeasuredWidth() + mActionPadding;
                    hasAction = true;
                }

                if(mNegativeAction.getVisibility() == View.VISIBLE) {
                    mNegativeAction.layout(actionRight - mNegativeAction.getMeasuredWidth(), childBottom - mActionOuterHeight + temp, actionRight, childBottom - temp);
                    hasAction = true;
                }

                if(hasAction)
                    childBottom -= mActionOuterHeight;
            }

            mDividerPos = childBottom - mDividerPaint.getStrokeWidth() / 2f;

            if(mContent != null)
                mContent.layout(childLeft, childTop, childRight, childBottom);
        }

        @Override/**/
        public void draw(Canvas canvas) {
            super.draw(canvas);

            if(mShowDivider)
                canvas.drawLine(mBackground.getPaddingLeft(), mDividerPos, mBackground.getWidth() - mBackground.getPaddingRight(), mDividerPos, mDividerPaint);
        }

    }

}
