package com.rey.material.app;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;

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

    private int mContentPadding;
    private int mActionHeight;
    private int mActionOuterHeight;
    private int mActionMinWidth;
    private int mActionPadding;

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

        mContainer = new ContainerFrameLayout(context);
        mTitle = new TextView(context);
        mPositiveAction = new Button(context);
        mNegativeAction = new Button(context);
        mContentHolder = new ScrollView(context);
        mBackground = new CardView(context);

        mContainer.addView(mBackground);
        mContainer.addView(mContentHolder);
        mContainer.addView(mTitle);
        mContainer.addView(mPositiveAction);
        mContainer.addView(mNegativeAction);

        setTitle(0);
        setPositiveAction(0);
        setNegativeAction(0);

        super.setContentView(mContainer);
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
        setTitle(getContext().getResources().getString(id));
    }

    public void setPositiveAction(CharSequence action){
        mPositiveAction.setText(action);
        mPositiveAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
    }

    public void setPositiveAction(int id){
        setPositiveAction(getContext().getResources().getString(id));
    }

    public void setNegativeAction(CharSequence action){
        mNegativeAction.setText(action);
        mNegativeAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
    }

    public void setNegativeAction(int id){
        setNegativeAction(getContext().getResources().getString(id));
    }

    private class ContainerFrameLayout extends FrameLayout{

        private Point mWindowSize;

        public ContainerFrameLayout(Context context) {
            super(context);
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
            int contentWidth = mLayoutWidth;
            int contentHeight = mLayoutHeight;

            Point windowSize = getWindowSize();

            if(contentWidth == ViewGroup.LayoutParams.MATCH_PARENT)
                contentWidth = windowSize.x - mBackground.getPaddingLeft() - mBackground.getPaddingRight();

            if(contentHeight == ViewGroup.LayoutParams.MATCH_PARENT)
                contentHeight = windowSize.y - mBackground.getPaddingTop() - mBackground.getPaddingBottom();

            int titleWidth;
            int titleHeight;

            if(mTitle.getVisibility() == View.VISIBLE){
                int widthMs = contentWidth == ViewGroup.LayoutParams.WRAP_CONTENT ? MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED) : MeasureSpec.makeMeasureSpec(contentWidth - mContentPadding * 2, MeasureSpec.AT_MOST);
                int heightMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                mTitle.measure(widthMs, heightMs);

                titleWidth = mTitle.getMeasuredWidth();
                titleHeight = mTitle.getMeasuredHeight();
            }
            else{
                titleWidth = 0;
                titleHeight = 0;
            }

            int positiveActionWidth;
            int positiveActionHeight;

            if(mPositiveAction.getVisibility() == View.VISIBLE){
                int widthMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                int heightMs = MeasureSpec.makeMeasureSpec(mActionHeight, MeasureSpec.EXACTLY);
                mPositiveAction.measure(widthMs, heightMs);

                positiveActionWidth = mPositiveAction.getMeasuredWidth();
                positiveActionHeight = mPositiveAction.getMeasuredHeight();

                if(positiveActionWidth < mActionMinWidth){
                    mPositiveAction.measure(MeasureSpec.makeMeasureSpec(mActionMinWidth, MeasureSpec.EXACTLY), heightMs);
                    positiveActionWidth = mActionMinWidth;
                }
            }
            else{
                positiveActionWidth = 0;
                positiveActionHeight = 0;
            }

            int negativeActionWidth;
            int negativeActionHeight;

            if(mNegativeAction.getVisibility() == View.VISIBLE){
                int widthMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                int heightMs = MeasureSpec.makeMeasureSpec(mActionHeight, MeasureSpec.EXACTLY);
                mNegativeAction.measure(widthMs, heightMs);

                negativeActionWidth = mNegativeAction.getMeasuredWidth();
                negativeActionHeight = mNegativeAction.getMeasuredHeight();

                if(negativeActionWidth < mActionMinWidth){
                    mNegativeAction.measure(MeasureSpec.makeMeasureSpec(mActionMinWidth, MeasureSpec.EXACTLY), heightMs);
                    negativeActionWidth = mActionMinWidth;
                }
            }
            else{
                negativeActionWidth = 0;
                negativeActionHeight = 0;
            }

            setMeasuredDimension(mLayoutWidth, mLayoutHeight);
            mBackground.measure(MeasureSpec.makeMeasureSpec(mLayoutWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mLayoutHeight, MeasureSpec.EXACTLY));
        }
    }

}
