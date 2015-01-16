package com.rey.material.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.rey.material.R;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
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
    private Button mNeutralAction;
    private View mContent;
    private CardView mBackground;

    protected int mContentPadding;
    protected int mActionHeight;
    protected int mActionOuterHeight;
    protected int mActionOuterPadding;
    protected int mActionMinWidth;
    protected int mActionPadding;
    protected int mDialogHorizontalPadding;
    protected int mDialogVerticalPadding;

    private boolean mLayoutActionVertical = false;

    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;

    public static final int ACTION_POSITIVE = ViewUtil.generateViewId();
    public static final int ACTION_NEGATIVE = ViewUtil.generateViewId();
    public static final int ACTION_NEUTRAL = ViewUtil.generateViewId();

    public Dialog(Context context) {
        this(context, 0);
    }

    public Dialog(Context context, int style) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);

        init(context, style);
    }

    private void init(Context context, int style){
        mContentPadding = ThemeUtil.dpToPx(context, 24);
        mActionMinWidth = ThemeUtil.dpToPx(context, 64);
        mActionHeight = ThemeUtil.dpToPx(context, 36);
        mActionOuterHeight = ThemeUtil.dpToPx(context, 48);
        mActionPadding = ThemeUtil.dpToPx(context, 8);
        mActionOuterPadding = ThemeUtil.dpToPx(context, 16);
        mDialogHorizontalPadding = ThemeUtil.dpToPx(context, 40);
        mDialogVerticalPadding = ThemeUtil.dpToPx(context, 24);

        mBackground = new CardView(context);
        mContainer = new ContainerFrameLayout(context);
        mTitle = new TextView(context);
        mPositiveAction = new Button(context);
        mNegativeAction = new Button(context);
        mNeutralAction = new Button(context);

        mBackground.setPreventCornerOverlap(false);

        mTitle.setPadding(mContentPadding, mContentPadding, mContentPadding, mContentPadding - mActionPadding);
        mPositiveAction.setId(ACTION_POSITIVE);
        mPositiveAction.setPadding(mActionPadding, 0, mActionPadding, 0);
        mPositiveAction.setBackgroundResource(0);
        mNegativeAction.setId(ACTION_NEGATIVE);
        mNegativeAction.setPadding(mActionPadding, 0, mActionPadding, 0);
        mNegativeAction.setBackgroundResource(0);
        mNeutralAction.setId(ACTION_NEUTRAL);
        mNeutralAction.setPadding(mActionPadding, 0, mActionPadding, 0);
        mNeutralAction.setBackgroundResource(0);

        mContainer.addView(mBackground);
        mContainer.addView(mTitle);
        mContainer.addView(mPositiveAction);
        mContainer.addView(mNegativeAction);
        mContainer.addView(mNeutralAction);

        cancelable(true);
        canceledOnTouchOutside(true);

        clearContent();
        onCreate();
        applyStyle(style);

        super.setContentView(mContainer);
    }

    protected void onCreate(){
    }

    public Dialog applyStyle(int resId){
        if(resId == 0)
            return this;

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

        layoutParams(layout_width, layout_height);

        dimAmount(a.getFloat(R.styleable.Dialog_di_dimAmount, 0.5f));
        backgroundColor(a.getColor(R.styleable.Dialog_di_backgroundColor, 0xFFFFFFFF));/**/
        maxElevation(a.getDimensionPixelOffset(R.styleable.Dialog_di_maxElevation, 0));
        elevation(a.getDimensionPixelOffset(R.styleable.Dialog_di_elevation, 0));
        cornerRadius(a.getDimensionPixelOffset(R.styleable.Dialog_di_cornerRadius, 0));

        titleTextAppearance(a.getResourceId(R.styleable.Dialog_di_titleTextAppearance, R.style.TextAppearance_AppCompat_Title));
        if(ThemeUtil.getType(a, R.styleable.Dialog_di_titleTextColor) != TypedValue.TYPE_NULL)
            titleColor(a.getColor(R.styleable.Dialog_di_titleTextColor, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_actionBackground) != TypedValue.TYPE_NULL)
            actionBackground(a.getResourceId(R.styleable.Dialog_di_actionBackground, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_actionRipple) != TypedValue.TYPE_NULL)
            actionRipple(a.getResourceId(R.styleable.Dialog_di_actionRipple, 0));

        actionTextAppearance(a.getResourceId(R.styleable.Dialog_di_actionTextAppearance, R.style.TextAppearance_AppCompat_Button));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_actionTextColor) != TypedValue.TYPE_NULL)
            actionTextColor(a.getColorStateList(R.styleable.Dialog_di_actionTextColor));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionBackground) != TypedValue.TYPE_NULL)
            positiveActionBackground(a.getResourceId(R.styleable.Dialog_di_positiveActionBackground, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionRipple) != TypedValue.TYPE_NULL)
            positiveActionRipple(a.getResourceId(R.styleable.Dialog_di_positiveActionRipple, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionTextAppearance) != TypedValue.TYPE_NULL)
            positiveActionTextAppearance(a.getResourceId(R.styleable.Dialog_di_positiveActionTextAppearance, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_positiveActionTextColor) != TypedValue.TYPE_NULL)
            positiveActionTextColor(a.getColorStateList(R.styleable.Dialog_di_positiveActionTextColor));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionBackground) != TypedValue.TYPE_NULL)
            negativeActionBackground(a.getResourceId(R.styleable.Dialog_di_negativeActionBackground, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionRipple) != TypedValue.TYPE_NULL)
            negativeActionRipple(a.getResourceId(R.styleable.Dialog_di_negativeActionRipple, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionTextAppearance) != TypedValue.TYPE_NULL)
            negativeActionTextAppearance(a.getResourceId(R.styleable.Dialog_di_negativeActionTextAppearance, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_negativeActionTextColor) != TypedValue.TYPE_NULL)
            negativeActionTextColor(a.getColorStateList(R.styleable.Dialog_di_negativeActionTextColor));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_neutralActionBackground) != TypedValue.TYPE_NULL)
            neutralActionBackground(a.getResourceId(R.styleable.Dialog_di_neutralActionBackground, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_neutralActionRipple) != TypedValue.TYPE_NULL)
            neutralActionRipple(a.getResourceId(R.styleable.Dialog_di_neutralActionRipple, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_neutralActionTextAppearance) != TypedValue.TYPE_NULL)
            neutralActionTextAppearance(a.getResourceId(R.styleable.Dialog_di_neutralActionTextAppearance, 0));

        if(ThemeUtil.getType(a, R.styleable.Dialog_di_neutralActionTextColor) != TypedValue.TYPE_NULL)
            neutralActionTextColor(a.getColorStateList(R.styleable.Dialog_di_neutralActionTextColor));

        dividerColor(a.getColor(R.styleable.Dialog_di_dividerColor, 0x1E000000));
        dividerHeight(a.getDimensionPixelOffset(R.styleable.Dialog_di_dividerHeight, ThemeUtil.dpToPx(context, 1)));
        setCancelable(a.getBoolean(R.styleable.Dialog_di_cancelable, true));
        setCanceledOnTouchOutside(a.getBoolean(R.styleable.Dialog_di_canceledOnTouchOutside, true));

        a.recycle();
        return this;
    }

    public Dialog clearContent(){
        title(0);
        positiveAction(0);
        positiveActionClickListener(null);
        negativeAction(0);
        negativeActionClickListener(null);
        neutralAction(0);
        neutralActionClickListener(null);
        contentView(null);
        return this;
    }

    public Dialog layoutParams(int width, int height){
        mLayoutWidth = width;
        mLayoutHeight = height;
        return this;
    }

    public Dialog dimAmount(float amount){
        Window window = getWindow();
        if(amount > 0f){
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = amount;
            window.setAttributes(lp);
        }
        else
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        return this;
    }

    public Dialog backgroundColor(int color){
        mBackground.setCardBackgroundColor(color);
        return this;
    }

    public Dialog elevation(float radius){
        if(mBackground.getMaxCardElevation() < radius)
            mBackground.setMaxCardElevation(radius);

        mBackground.setCardElevation(radius);
        return this;
    }

    public Dialog maxElevation(float radius){
        mBackground.setMaxCardElevation(radius);
        return this;
    }

    public Dialog cornerRadius(float radius){
        mBackground.setRadius(radius);
        return this;
    }

    public Dialog dividerColor(int color){
        mContainer.setDividerColor(color);
        return this;
    }

    public Dialog dividerHeight(int height){
        mContainer.setDividerHeight(height);
        return this;
    }

    public Dialog title(CharSequence title){
        mTitle.setText(title);
        mTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        return this;
    }

    public Dialog title(int id){
        return title(id == 0 ? null : getContext().getResources().getString(id));
    }

    @Override
    public void setTitle(CharSequence title) {
        title(title);
    }

    @Override
    public void setTitle(int titleId) {
        title(titleId);
    }

    public Dialog titleColor(int color){
        mTitle.setTextColor(color);
        return this;
    }

    public Dialog titleTextAppearance(int resId){
        mTitle.setTextAppearance(getContext(), resId);
        return this;
    }

    public Dialog actionBackground(int id){
        positiveActionBackground(id);
        negativeActionBackground(id);
        neutralActionBackground(id);
        return this;
    }

    public Dialog actionBackground(Drawable drawable){
        positiveActionBackground(drawable);
        negativeActionBackground(drawable);
        neutralActionBackground(drawable);
        return this;
    }

    public Dialog actionRipple(int resId){
        positiveActionRipple(resId);
        negativeActionRipple(resId);
        neutralActionRipple(resId);
        return this;
    }

    public Dialog actionTextAppearance(int resId){
        positiveActionTextAppearance(resId);
        negativeActionTextAppearance(resId);
        neutralActionTextAppearance(resId);
        return this;
    }

    public Dialog actionTextColor(ColorStateList color){
        positiveActionTextColor(color);
        negativeActionTextColor(color);
        neutralActionTextColor(color);
        return this;
    }

    public Dialog actionTextColor(int color){
        positiveActionTextColor(color);
        negativeActionTextColor(color);
        neutralActionTextColor(color);
        return this;
    }

    public Dialog positiveAction(CharSequence action){
        mPositiveAction.setText(action);
        mPositiveAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
        return this;
    }

    public Dialog positiveAction(int id){
        return positiveAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public Dialog positiveActionBackground(Drawable drawable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mPositiveAction.setBackground(drawable);
        else
            mPositiveAction.setBackgroundDrawable(drawable);
        return this;
    }

    public Dialog positiveActionBackground(int id){
        return positiveActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    public Dialog positiveActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        return positiveActionBackground(drawable);
    }

    public Dialog positiveActionTextAppearance(int resId){
        mPositiveAction.setTextAppearance(getContext(), resId);
        return this;
    }

    public Dialog positiveActionTextColor(ColorStateList color){
        mPositiveAction.setTextColor(color);
        return this;
    }

    public Dialog positiveActionTextColor(int color){
        mPositiveAction.setTextColor(color);
        return this;
    }

    public Dialog positiveActionClickListener(View.OnClickListener listener){
        mPositiveAction.setOnClickListener(listener);
        return this;
    }

    public Dialog negativeAction(CharSequence action){
        mNegativeAction.setText(action);
        mNegativeAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
        return this;
    }

    public Dialog negativeAction(int id){
        return negativeAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public Dialog negativeActionBackground(Drawable drawable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mNegativeAction.setBackground(drawable);
        else
            mNegativeAction.setBackgroundDrawable(drawable);
        return this;
    }

    public Dialog negativeActionBackground(int id){
        return negativeActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    public Dialog negativeActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        return negativeActionBackground(drawable);
    }

    public Dialog negativeActionTextAppearance(int resId){
        mNegativeAction.setTextAppearance(getContext(), resId);
        return this;
    }

    public Dialog negativeActionTextColor(ColorStateList color){
        mNegativeAction.setTextColor(color);
        return this;
    }

    public Dialog negativeActionTextColor(int color){
        mNegativeAction.setTextColor(color);
        return this;
    }

    public Dialog negativeActionClickListener(View.OnClickListener listener){
        mNegativeAction.setOnClickListener(listener);
        return this;
    }

    public Dialog neutralAction(CharSequence action){
        mNeutralAction.setText(action);
        mNeutralAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
        return this;
    }

    public Dialog neutralAction(int id){
        return neutralAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressWarnings("deprecation")
    public Dialog neutralActionBackground(Drawable drawable){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mNeutralAction.setBackground(drawable);
        else
            mNeutralAction.setBackgroundDrawable(drawable);
        return this;
    }

    public Dialog neutralActionBackground(int id){
        return neutralActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    public Dialog neutralActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        return neutralActionBackground(drawable);
    }

    public Dialog neutralActionTextAppearance(int resId){
        mNeutralAction.setTextAppearance(getContext(), resId);
        return this;
    }

    public Dialog neutralActionTextColor(ColorStateList color){
        mNeutralAction.setTextColor(color);
        return this;
    }

    public Dialog neutralActionTextColor(int color){
        mNeutralAction.setTextColor(color);
        return this;
    }

    public Dialog neutralActionClickListener(View.OnClickListener listener){
        mNeutralAction.setOnClickListener(listener);
        return this;
    }

    public Dialog showDivider(boolean show){
        mContainer.setShowDivider(show);
        return this;
    }

    public Dialog contentView(View v){
        if(mContent != v) {
            if(mContent != null)
                mContainer.removeView(mContent);

            mContent = v;
        }

        if(mContent != null)
            mContainer.addView(mContent);

        return this;
    }

    public Dialog contentView(int layoutId){
        if(layoutId == 0)
            return this;

        View v = LayoutInflater.from(getContext()).inflate(layoutId, null);
        return contentView(v);
    }

    public Dialog cancelable(boolean cancelable){
        super.setCancelable(cancelable);
        mCancelable = cancelable;
        return this;
    }

    public Dialog canceledOnTouchOutside(boolean cancel){
        super.setCanceledOnTouchOutside(cancel);
        mCanceledOnTouchOutside = cancel;
        return this;
    }

    public Dialog contentMargin(int margin){
        mContainer.setContentMargin(margin);
        return this;
    }

    public Dialog contentMargin(int left, int top, int right, int bottom){
        mContainer.setContentMargin(left, top, right, bottom);
        return this;
    }

    @Override
    public void setCancelable(boolean flag) {
        cancelable(flag);
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        canceledOnTouchOutside(cancel);
    }

    @Override
    public void setContentView(View v){
        contentView(v);
    }

    @Override
    public void setContentView(int layoutId){
        contentView(layoutId);
    }

    @Override
    public void setContentView(View v, ViewGroup.LayoutParams params) {
        contentView(v);
    }

    private class ContainerFrameLayout extends FrameLayout{

        private Paint mDividerPaint;
        private float mDividerPos = -1f;
        private boolean mShowDivider = false;
        private boolean mClickOutside = false;

        private int mContentMarginLeft;
        private int mContentMarginTop;
        private int mContentMarginRight;
        private int mContentMarginBottom;

        public ContainerFrameLayout(Context context) {
            super(context);

            mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDividerPaint.setStyle(Paint.Style.STROKE);
            setWillNotDraw(false);
        }

        public void setContentMargin(int margin){
            setContentMargin(margin, margin, margin, margin);
        }

        public void setContentMargin(int left, int top, int right, int bottom){
            mContentMarginLeft = left;
            mContentMarginTop = top;
            mContentMarginRight = right;
            mContentMarginBottom = bottom;
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

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);

            //skip second measure.
            if(widthSize == getMeasuredWidth() && heightSize == getMeasuredHeight() && widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY){
                setMeasuredDimension(widthSize, heightSize);
                return;
            }

            int paddingLeft = Math.max(mDialogHorizontalPadding, mBackground.getPaddingLeft());
            int paddingRight = Math.max(mDialogHorizontalPadding, mBackground.getPaddingRight());
            int paddingTop = Math.max(mDialogVerticalPadding, mBackground.getPaddingTop());
            int paddingBottom = Math.max(mDialogVerticalPadding, mBackground.getPaddingBottom());

            int maxWidth = widthSize - paddingLeft - paddingRight;
            int maxHeight = heightSize - paddingTop - paddingBottom;

            int width = mLayoutWidth == ViewGroup.LayoutParams.MATCH_PARENT ? maxWidth : mLayoutWidth;
            int height = mLayoutHeight == ViewGroup.LayoutParams.MATCH_PARENT ? maxHeight : mLayoutHeight;

            int widthMs;
            int heightMs;

            int titleWidth = 0;
            int titleHeight = 0;

            if(mTitle.getVisibility() == View.VISIBLE){
                widthMs = MeasureSpec.makeMeasureSpec(width == ViewGroup.LayoutParams.WRAP_CONTENT ? maxWidth : width, MeasureSpec.AT_MOST);
                heightMs = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                mTitle.measure(widthMs, heightMs);
                titleWidth = mTitle.getMeasuredWidth();
                titleHeight = mTitle.getMeasuredHeight();
            }

            int contentWidth = 0;
            int contentHeight = 0;

            if(mContent != null){
                widthMs = MeasureSpec.makeMeasureSpec((width == ViewGroup.LayoutParams.WRAP_CONTENT ? maxWidth : width) - mContentMarginLeft - mContentMarginRight, MeasureSpec.AT_MOST);
                heightMs = MeasureSpec.makeMeasureSpec(maxHeight - mContentMarginTop - mContentMarginBottom, MeasureSpec.AT_MOST);
                mContent.measure(widthMs, heightMs);
                contentWidth = mContent.getMeasuredWidth();
                contentHeight = mContent.getMeasuredHeight();
            }

            int visibleActions = 0;
            int positiveActionWidth = 0;

            if(mPositiveAction.getVisibility() == View.VISIBLE){
                widthMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                heightMs = MeasureSpec.makeMeasureSpec(mActionHeight, MeasureSpec.EXACTLY);
                mPositiveAction.measure(widthMs, heightMs);

                positiveActionWidth = mPositiveAction.getMeasuredWidth();

                if(positiveActionWidth < mActionMinWidth){
                    mPositiveAction.measure(MeasureSpec.makeMeasureSpec(mActionMinWidth, MeasureSpec.EXACTLY), heightMs);
                    positiveActionWidth = mActionMinWidth;
                }

                visibleActions++;
            }

            int negativeActionWidth = 0;

            if(mNegativeAction.getVisibility() == View.VISIBLE){
                widthMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                heightMs = MeasureSpec.makeMeasureSpec(mActionHeight, MeasureSpec.EXACTLY);
                mNegativeAction.measure(widthMs, heightMs);

                negativeActionWidth = mNegativeAction.getMeasuredWidth();

                if(negativeActionWidth < mActionMinWidth){
                    mNegativeAction.measure(MeasureSpec.makeMeasureSpec(mActionMinWidth, MeasureSpec.EXACTLY), heightMs);
                    negativeActionWidth = mActionMinWidth;
                }

                visibleActions++;
            }

            int neutralActionWidth = 0;

            if(mNeutralAction.getVisibility() == View.VISIBLE){
                widthMs = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                heightMs = MeasureSpec.makeMeasureSpec(mActionHeight, MeasureSpec.EXACTLY);
                mNeutralAction.measure(widthMs, heightMs);

                neutralActionWidth = mNeutralAction.getMeasuredWidth();

                if(neutralActionWidth < mActionMinWidth){
                    mNeutralAction.measure(MeasureSpec.makeMeasureSpec(mActionMinWidth, MeasureSpec.EXACTLY), heightMs);
                    neutralActionWidth = mActionMinWidth;
                }

                visibleActions++;
            }

            int actionBarWidth = positiveActionWidth + negativeActionWidth + neutralActionWidth + mActionOuterPadding * 2 + mActionPadding * Math.max(0, visibleActions - 1);

            if(width == ViewGroup.LayoutParams.WRAP_CONTENT)
                width = Math.min(maxWidth, Math.max(titleWidth, Math.max(contentWidth + mContentMarginLeft + mContentMarginRight, actionBarWidth)));

            mLayoutActionVertical = actionBarWidth > width;

            int nonContentHeight = titleHeight + mActionPadding + mContentMarginTop + mContentMarginBottom;
            if(mLayoutActionVertical)
                nonContentHeight += mActionOuterHeight * visibleActions;
            else
                nonContentHeight += (visibleActions > 0) ? mActionOuterHeight : 0;

            if(height == ViewGroup.LayoutParams.WRAP_CONTENT)
                height = Math.min(maxHeight, contentHeight + nonContentHeight);

            if(mContent != null)
                mContent.measure(MeasureSpec.makeMeasureSpec(width - mContentMarginLeft - mContentMarginRight, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height - nonContentHeight, MeasureSpec.EXACTLY));

            mBackground.measure(MeasureSpec.makeMeasureSpec(width + mBackground.getPaddingLeft() + mBackground.getPaddingRight(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height + mBackground.getPaddingTop() + mBackground.getPaddingBottom(), MeasureSpec.EXACTLY));

            setMeasuredDimension(widthSize, heightSize);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = (right - left - mBackground.getMeasuredWidth()) / 2;
            int childTop = (bottom - top - mBackground.getMeasuredHeight()) / 2;
            int childRight = childLeft + mBackground.getMeasuredWidth();
            int childBottom = childTop + mBackground.getMeasuredHeight();

            mBackground.layout(childLeft, childTop, childRight, childBottom);

            childLeft += mBackground.getPaddingLeft();
            childTop += mBackground.getPaddingTop();
            childRight -= mBackground.getPaddingRight();
            childBottom -= mBackground.getPaddingBottom();

            if(mTitle.getVisibility() == View.VISIBLE) {
                mTitle.layout(childLeft, childTop, childRight, childTop + mTitle.getMeasuredHeight());
                childTop += mTitle.getMeasuredHeight();
            }

            childBottom -= mActionPadding;

            int temp = (mActionOuterHeight - mActionHeight) / 2;

            if(mLayoutActionVertical){
                if(mNeutralAction.getVisibility() == View.VISIBLE){
                    mNeutralAction.layout(childRight - mActionOuterPadding - mNeutralAction.getMeasuredWidth(), childBottom - mActionOuterHeight + temp, childRight - mActionOuterPadding, childBottom - temp);
                    childBottom -= mActionOuterHeight;
                }

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
                int actionTop = childBottom - mActionOuterHeight + temp;
                int actionBottom = childBottom - temp;
                boolean hasAction = false;

                if(mPositiveAction.getVisibility() == View.VISIBLE){
                    mPositiveAction.layout(actionRight - mPositiveAction.getMeasuredWidth(), actionTop, actionRight, actionBottom);
                    actionRight -= mPositiveAction.getMeasuredWidth() + mActionPadding;
                    hasAction = true;
                }

                if(mNegativeAction.getVisibility() == View.VISIBLE) {
                    mNegativeAction.layout(actionRight - mNegativeAction.getMeasuredWidth(), actionTop, actionRight, actionBottom);
                    hasAction = true;
                }

                if(mNeutralAction.getVisibility() == View.VISIBLE) {
                    mNeutralAction.layout(childLeft + mActionOuterPadding, actionTop, childLeft + mActionOuterPadding + mNeutralAction.getMeasuredWidth(), actionBottom);
                    hasAction = true;
                }

                if(hasAction)
                    childBottom -= mActionOuterHeight;
            }

            mDividerPos = childBottom - mDividerPaint.getStrokeWidth() / 2f;

            if(mContent != null)
                mContent.layout(childLeft + mContentMarginLeft, childTop + mContentMarginTop, childRight - mContentMarginRight, childBottom - mContentMarginBottom);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);

            if(mShowDivider && (mPositiveAction.getVisibility() == View.VISIBLE || mNegativeAction.getVisibility() == View.VISIBLE || mNeutralAction.getVisibility() == View.VISIBLE))
                canvas.drawLine(mBackground.getLeft() + mBackground.getPaddingLeft(), mDividerPos, mBackground.getRight() - mBackground.getPaddingRight(), mDividerPos, mDividerPaint);
        }

        private boolean isOutsideDialog(float x, float y){
            return x < mBackground.getLeft() + mBackground.getPaddingLeft() || x > mBackground.getRight() - mBackground.getPaddingRight() || y < mBackground.getTop() + mBackground.getPaddingTop() || y > mBackground.getBottom() - mBackground.getPaddingBottom();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            boolean handled = super.onTouchEvent(event);

            if(handled)
                return true;

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(isOutsideDialog(event.getX(), event.getY())){
                        mClickOutside = true;
                        return true;
                    }
                    return false;
                case MotionEvent.ACTION_MOVE:
                    return mClickOutside;
                case MotionEvent.ACTION_CANCEL:
                    mClickOutside = false;
                    return false;
                case MotionEvent.ACTION_UP:
                    if(mClickOutside && isOutsideDialog(event.getX(), event.getY())){
                        mClickOutside = false;
                        if(mCancelable && mCanceledOnTouchOutside)
                            dismiss();
                        return true;
                    }
                    return false;
            }

            return false;
        }
    }

    public static class Builder implements DialogFragment.Builder, Parcelable{

        protected int mStyleId;
        protected int mContentViewId;
        protected CharSequence mTitle;
        protected CharSequence mPositive;
        protected CharSequence mNegative;
        protected CharSequence mNeutral;

        public Builder(){}

        public Builder(int styleId){
            mStyleId = styleId;
        }

        public Builder style(int styleId){
            mStyleId = styleId;
            return this;
        }

        public Builder contentView(int layoutId){
            mContentViewId = layoutId;
            return this;
        }

        public Builder title(CharSequence title){
            mTitle = title;
            return this;
        }

        public Builder positiveAction(CharSequence action){
            mPositive = action;
            return this;
        }

        public Builder negativeAction(CharSequence action){
            mNegative = action;
            return this;
        }

        public Builder neutralAction(CharSequence action){
            mNeutral = action;
            return this;
        }

        @Override
        public void onPositiveActionClicked(DialogFragment fragment) {}

        @Override
        public void onNegativeActionClicked(DialogFragment fragment) {}

        @Override
        public void onNeutralActionClicked(DialogFragment fragment) {}

        @Override
        public Dialog build(Context context) {
            Dialog dialog = onBuild(context, mStyleId);

            dialog.title(mTitle)
                    .positiveAction(mPositive)
                    .negativeAction(mNegative)
                    .neutralAction(mNeutral);

            if(mContentViewId != 0)
                dialog.contentView(mContentViewId);

            return dialog;
        }

        protected Dialog onBuild(Context context, int styleId){
            return new Dialog(context, styleId);
        }

        protected Builder(Parcel in) {
            mStyleId = in.readInt();
            mContentViewId = in.readInt();
            mTitle = (CharSequence)in.readParcelable(null);
            mPositive = (CharSequence)in.readParcelable(null);
            mNegative = (CharSequence)in.readParcelable(null);
            mNeutral = (CharSequence)in.readParcelable(null);

            onReadFromParcel(in);
        }

        protected void onReadFromParcel(Parcel in){}

        protected void onWriteToParcel(Parcel dest, int flags){}

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mStyleId);
            dest.writeInt(mContentViewId);
            dest.writeValue(mTitle);
            dest.writeValue(mPositive);
            dest.writeValue(mNegative);
            dest.writeValue(mNeutral);
            onWriteToParcel(dest, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Parcelable.Creator<Builder> CREATOR = new Parcelable.Creator<Builder>() {
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

    }
}
