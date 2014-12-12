package com.rey.material.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
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
    private ScrollView mContentHolder;
    private CardView mBackground;
    private TextView mMessage;

    private int mMessageTextAppearanceId;
    private int mMessageTextColor;
    private int mDividerColor;
    private int mDividerHeight;

    private int mContentPadding;
    private int mActionHeight;
    private int mActionOuterHeight;
    private int mActionOuterPadding;
    private int mActionMinWidth;
    private int mActionPadding;

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
        mMessageTextAppearanceId = R.style.Base_TextAppearance_AppCompat_Body1;

        mBackground = new CardView(context);
        mContainer = new ContainerFrameLayout(context);
        mTitle = new TextView(context);
        mPositiveAction = new Button(context);
        mNegativeAction = new Button(context);
        mContentHolder = new ScrollView(context);

        mTitle.setPadding(mContentPadding, mContentPadding, mContentPadding, mContentPadding - mActionPadding);
        mPositiveAction.setPadding(mActionPadding, 0, mActionPadding, 0);
        mPositiveAction.setBackgroundResource(0);
        mNegativeAction.setPadding(mActionPadding, 0, mActionPadding, 0);
        mNegativeAction.setBackgroundResource(0);
        mContentHolder.setPadding(mContentPadding, 0, mContentPadding, mContentPadding - mActionPadding);
        mContentHolder.setClipToPadding(false);
        mContentHolder.setFillViewport(true);
        mContentHolder.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

        mContainer.addView(mBackground);
        mContainer.addView(mContentHolder);
        mContainer.addView(mTitle);
        mContainer.addView(mPositiveAction);
        mContainer.addView(mNegativeAction);

        applyStyle(style);

        clearContent();

        super.setContentView(mContainer);
    }

    public void applyStyle(int resId){
        Context context = getContext();

        mTitle.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Title);
        mPositiveAction.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Button);
        mNegativeAction.setTextAppearance(context, R.style.Base_TextAppearance_AppCompat_Button);

        mDividerColor = 0x1E000000;
        mDividerHeight = ThemeUtil.dpToPx(context, 1);

        mContainer.setDividerColor(mDividerColor);
        mContainer.setDividerHeight(mDividerHeight);
    }

    public void clearContent(){
        setTitle(0);
        setPositiveAction(0);
        setNegativeAction(0);
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

    public void setPositiveAction(CharSequence action){
        mPositiveAction.setText(action);
        mPositiveAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
    }

    public void setPositiveAction(int id){
        setPositiveAction(id == 0 ? null : getContext().getResources().getString(id));
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
    public void setPositiveActionBackground(Drawable drawable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mPositiveAction.setBackground(drawable);
        else
            mPositiveAction.setBackgroundDrawable(drawable);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public void setNegativeActionBackground(Drawable drawable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mNegativeAction.setBackground(drawable);
        else
            mNegativeAction.setBackgroundDrawable(drawable);
    }

    public void setPositiveActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        setPositiveActionBackground(drawable);
    }

    public void setNegativeActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        setNegativeActionBackground(drawable);
    }

    public void setPositiveActionColor(ColorStateList color){
        mPositiveAction.setTextColor(color);
    }

    public void setPositiveActionColor(int color){
        mPositiveAction.setTextColor(color);
    }

    public void setNegativeActionColor(ColorStateList color){
        mNegativeAction.setTextColor(color);
    }

    public void setNegativeActionColor(int color){
        mNegativeAction.setTextColor(color);
    }

    public void setPositiveTextAppearance(int resId){
        mPositiveAction.setTextAppearance(getContext(), resId);
    }

    public void setNegativeTextAppearance(int resId){
        mNegativeAction.setTextAppearance(getContext(), resId);
    }

    public void setPositiveActionClickListener(View.OnClickListener listener){
        mPositiveAction.setOnClickListener(listener);
    }

    public void setNegativeActionClickListener(View.OnClickListener listener){
        mNegativeAction.setOnClickListener(listener);
    }

    public void setMessage(CharSequence message){
        if(mMessage == null){
            mMessage = new TextView(getContext());
            mMessage.setTextAppearance(getContext(), mMessageTextAppearanceId);
            mMessage.setTextColor(mMessageTextColor);
        }

        mMessage.setText(message);
        setContentView(mMessage);
    }

    public void setMessageTextAppearance(int resId){
        if(mMessageTextAppearanceId != resId){
            mMessageTextAppearanceId = resId;
            if(mMessage != null)
                mMessage.setTextAppearance(getContext(), mMessageTextAppearanceId);
        }
    }

    public void setMessageTextColor(int color){
        if(mMessageTextColor != color){
            mMessageTextColor = color;
            if(mMessage != null)
                mMessage.setTextColor(color);
        }
    }

    @Override
    public void setContentView(View v){
        setContentView(v, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    @Override
    public void setContentView(int layoutId){
        View v = LayoutInflater.from(getContext()).inflate(layoutId, null);
        setContentView(v);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        mContentHolder.removeAllViews();
        if(view != null) {
            mContentHolder.addView(view, params);
            mContentHolder.setVisibility(View.VISIBLE);
        }
        else
            mContentHolder.setVisibility(View.GONE);
    }

    private class ContainerFrameLayout extends FrameLayout{

        private Point mWindowSize;
        private Paint mDividerPaint;
        private float mDividerPos = -1f;

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

        private Point getWindowSize(){
            if(mWindowSize == null)
                mWindowSize = new Point();

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

            if(mContentHolder.getVisibility() == View.VISIBLE){
                mContentHolder.measure(widthMs, heightMs);
                contentWidth = mContentHolder.getMeasuredWidth();
                contentHeight = mContentHolder.getMeasuredHeight();
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
                        mContentHolder.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(maxHeight - nonContentHeight, MeasureSpec.EXACTLY));

                    height = maxHeight;
                }
            }
            else{
                if(contentHeight > 0 && contentHeight != height - nonContentHeight)
                    mContentHolder.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - nonContentHeight, MeasureSpec.EXACTLY));
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

                if(mPositiveAction.getVisibility() == View.VISIBLE){
                    mPositiveAction.layout(actionRight - mPositiveAction.getMeasuredWidth(), childBottom - mActionOuterHeight + temp, actionRight, childBottom - temp);
                    actionRight -= mPositiveAction.getMeasuredWidth() + mActionPadding;
                }

                if(mNegativeAction.getVisibility() == View.VISIBLE)
                    mNegativeAction.layout(actionRight - mNegativeAction.getMeasuredWidth(), childBottom - mActionOuterHeight + temp, actionRight, childBottom - temp);

                childBottom -= mActionOuterHeight;
            }

            if(mContentHolder.getVisibility() == View.VISIBLE) {
                mContentHolder.layout(childLeft, childTop, childRight, childBottom);
                View child = mContentHolder.getChildAt(0);
                if(child != null && child.getMeasuredHeight() > mContentHolder.getMeasuredHeight() - mContentHolder.getPaddingBottom() - mContentHolder.getPaddingTop())
                    mDividerPos = childBottom - mDividerPaint.getStrokeWidth() / 2f;
                else
                    mDividerPos = -1f;
            }
            else
                mDividerPos = -1f;
        }

        @Override/**/
        public void draw(Canvas canvas) {
            super.draw(canvas);

            if(mDividerPos >= 0)
                canvas.drawLine(mBackground.getPaddingLeft(), mDividerPos, mBackground.getWidth() - mBackground.getPaddingRight(), mDividerPos, mDividerPaint);
        }

    }

}
