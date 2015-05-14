package com.rey.material.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.rey.material.R;
import com.rey.material.drawable.BlankDrawable;
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

    protected TextView mTitle;
    protected Button mPositiveAction;
    protected Button mNegativeAction;
    protected Button mNeutralAction;
    private View mContent;
    private DialogCardView mCardView;

    protected int mContentPadding;
    protected int mActionHeight;
    protected int mActionOuterHeight;
    protected int mActionOuterPadding;
    protected int mActionMinWidth;
    protected int mActionPadding;
    protected int mDialogHorizontalPadding;
    protected int mDialogVerticalPadding;

    protected int mInAnimationId;
    protected int mOutAnimationId;

    private final Handler mHandler = new Handler();
    private final Runnable mDismissAction = new Runnable() {
        public void run() {
            //dirty fix for java.lang.IllegalArgumentException: View not attached to window manager
            try {
                Dialog.super.dismiss();
            }
            catch(IllegalArgumentException ex){}
        }
    };


    private boolean mLayoutActionVertical = false;

    private boolean mCancelable = true;
    private boolean mCanceledOnTouchOutside = true;

    /**
     * The viewId of title view.
     */
    public static final int TITLE = ViewUtil.generateViewId();
    /**
     * The viewId of positive action button.
     */
    public static final int ACTION_POSITIVE = ViewUtil.generateViewId();
    /**
     * The viewId of negative action button.
     */
    public static final int ACTION_NEGATIVE = ViewUtil.generateViewId();
    /**
     * The viewId of neutral action button.
     */
    public static final int ACTION_NEUTRAL = ViewUtil.generateViewId();

    public Dialog(Context context) {
        this(context, R.style.Material_App_Dialog_Light);
    }


    public Dialog(Context context, int style) {
        super(context, style);

        //Override style to ensure not show window's title or background.
        //TODO: find a way to ensure windowIsFloating attribute is false.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(BlankDrawable.getInstance());
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

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

        mCardView = new DialogCardView(context);
        mContainer = new ContainerFrameLayout(context);
        mTitle = new TextView(context);
        mPositiveAction = new Button(context);
        mNegativeAction = new Button(context);
        mNeutralAction = new Button(context);

        mCardView.setPreventCornerOverlap(false);
        mCardView.setUseCompatPadding(true);

        mTitle.setId(TITLE);
        mTitle.setGravity(Gravity.START);
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

        mContainer.addView(mCardView);
        mCardView.addView(mTitle);
        mCardView.addView(mPositiveAction);
        mCardView.addView(mNegativeAction);
        mCardView.addView(mNeutralAction);

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
        backgroundColor(a.getColor(R.styleable.Dialog_di_backgroundColor, ThemeUtil.windowBackground(context, 0xFFFFFFFF)));
        maxElevation(a.getDimensionPixelOffset(R.styleable.Dialog_di_maxElevation, 0));
        elevation(a.getDimensionPixelOffset(R.styleable.Dialog_di_elevation, ThemeUtil.dpToPx(context, 4)));
        cornerRadius(a.getDimensionPixelOffset(R.styleable.Dialog_di_cornerRadius, ThemeUtil.dpToPx(context, 2)));
        layoutDirection(a.getInteger(R.styleable.Dialog_di_layoutDirection, View.LAYOUT_DIRECTION_LOCALE));

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

        inAnimation(a.getResourceId(R.styleable.Dialog_di_inAnimation, 0));
        outAnimation(a.getResourceId(R.styleable.Dialog_di_outAnimation, 0));
        dividerColor(a.getColor(R.styleable.Dialog_di_dividerColor, 0x1E000000));
        dividerHeight(a.getDimensionPixelOffset(R.styleable.Dialog_di_dividerHeight, ThemeUtil.dpToPx(context, 1)));
        setCancelable(a.getBoolean(R.styleable.Dialog_di_cancelable, true));
        setCanceledOnTouchOutside(a.getBoolean(R.styleable.Dialog_di_canceledOnTouchOutside, true));

        a.recycle();
        return this;
    }

    /**
     * Clear the content of this Dialog.
     * @return The Dialog for chaining methods.
     */
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

    /**
     * Set the params of this Dialog layout.
     * @param width The width param. Can be the size in pixels, or {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} or {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}.
     * @param height The height param. Can be the size in pixels, or {@link android.view.ViewGroup.LayoutParams#WRAP_CONTENT} or {@link android.view.ViewGroup.LayoutParams#MATCH_PARENT}.
     * @return The Dialog for chaining methods.
     */
    public Dialog layoutParams(int width, int height){
        mLayoutWidth = width;
        mLayoutHeight = height;
        return this;
    }

    /**
     * Set the dim amount of the region outside this Dialog.
     * @param amount The dim amount in [0..1].
     * @return The Dialog for chaining methods.
     */
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

    /**
     * Set the background color of this Dialog
     * @param color The color value.
     * @return The Dialog for chaining methods.
     */
    public Dialog backgroundColor(int color){
        mCardView.setCardBackgroundColor(color);
        return this;
    }

    /**
     * Set the elevation value of this Dialog.
     * @param elevation
     * @return The Dialog for chaining methods.
     */
    public Dialog elevation(float elevation){
        if(mCardView.getMaxCardElevation() < elevation)
            mCardView.setMaxCardElevation(elevation);

        mCardView.setCardElevation(elevation);
        return this;
    }

    /**
     * Set the maximum elevation value of this Dialog.
     * @param elevation
     * @return The Dialog for chaining methods.
     */
    public Dialog maxElevation(float elevation){
        mCardView.setMaxCardElevation(elevation);
        return this;
    }

    /**
     * Set the corner radius of this Dialog.
     * @param radius The corner radius.
     * @return The Dialog for chaining methods.
     */
    public Dialog cornerRadius(float radius){
        mCardView.setRadius(radius);
        return this;
    }

    /**
     * Set the divider's color of this Dialog.
     * @param color The color value.
     * @return The Dialog for chaining methods.
     */
    public Dialog dividerColor(int color){
        mCardView.setDividerColor(color);
        return this;
    }

    /**
     * Set the height of divider of this Dialog.
     * @param height The size value in pixels.
     * @return The Dialog for chaining methods.
     */
    public Dialog dividerHeight(int height){
        mCardView.setDividerHeight(height);
        return this;
    }

    /**
     * Set the title of this Dialog.
     * @param title The title text.
     * @return The Dialog for chaining methods.
     */
    public Dialog title(CharSequence title){
        mTitle.setText(title);
        mTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        return this;
    }

    /**
     * Set the title of this Dialog.
     * @param id The resourceId of text.
     * @return The Dialog for chaining methods.
     */
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

    /**
     * Set the text's color of Dialog's title.
     * @param color The color value.
     * @return The Dialog for chaining methods.
     */
    public Dialog titleColor(int color){
        mTitle.setTextColor(color);
        return this;
    }

    /**
     * Sets the text color, size, style of the title view from the specified TextAppearance resource.
     * @param resId The resourceId value.
     * @return The Dialog for chaining methods.
     */
    public Dialog titleTextAppearance(int resId){
        mTitle.setTextAppearance(getContext(), resId);
        return this;
    }

    /**
     * Set the background drawable of all action buttons.
     * @param id The resourceId of drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog actionBackground(int id){
        positiveActionBackground(id);
        negativeActionBackground(id);
        neutralActionBackground(id);
        return this;
    }

    /**
     * Set the background drawable of all action buttons.
     * @param drawable The background drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog actionBackground(Drawable drawable){
        positiveActionBackground(drawable);
        negativeActionBackground(drawable);
        neutralActionBackground(drawable);
        return this;
    }

    /**
     * Set the RippleEffect of all action buttons.
     * @param resId The resourceId of style.
     * @return The Dialog for chaining methods.
     */
    public Dialog actionRipple(int resId){
        positiveActionRipple(resId);
        negativeActionRipple(resId);
        neutralActionRipple(resId);
        return this;
    }

    /**
     * Sets the text color, size, style of all action buttons from the specified TextAppearance resource.
     * @param resId The resourceId value.
     * @return The Dialog for chaining methods.
     */
    public Dialog actionTextAppearance(int resId){
        positiveActionTextAppearance(resId);
        negativeActionTextAppearance(resId);
        neutralActionTextAppearance(resId);
        return this;
    }

    /**
     * Sets the text color of all action buttons.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog actionTextColor(ColorStateList color){
        positiveActionTextColor(color);
        negativeActionTextColor(color);
        neutralActionTextColor(color);
        return this;
    }

    /**
     * Sets the text color of all action buttons.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog actionTextColor(int color){
        positiveActionTextColor(color);
        negativeActionTextColor(color);
        neutralActionTextColor(color);
        return this;
    }

    /**
     * Set the text of positive action button.
     * @param action
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveAction(CharSequence action){
        mPositiveAction.setText(action);
        mPositiveAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
        return this;
    }

    /**
     * Set the text of positive action button.
     * @param id The resourceId of text.
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveAction(int id){
        return positiveAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    /**
     * Set the background drawable of positive action button.
     * @param drawable The background drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveActionBackground(Drawable drawable){
        ViewUtil.setBackground(mPositiveAction, drawable);
        return this;
    }

    /**
     * Set the background drawable of positive action button.
     * @param id The resourceId of drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveActionBackground(int id){
        return positiveActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    /**
     * Set the RippleEffect of positive action button.
     * @param resId The resourceId of style.
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        return positiveActionBackground(drawable);
    }

    /**
     * Sets the text color, size, style of positive action button from the specified TextAppearance resource.
     * @param resId The resourceId value.
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveActionTextAppearance(int resId){
        mPositiveAction.setTextAppearance(getContext(), resId);
        return this;
    }

    /**
     * Sets the text color of positive action button.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveActionTextColor(ColorStateList color){
        mPositiveAction.setTextColor(color);
        return this;
    }

    /**
     * Sets the text color of positive action button.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveActionTextColor(int color){
        mPositiveAction.setTextColor(color);
        return this;
    }

    /**
     * Set a listener will be called when positive action button is clicked.
     * @param listener The {@link android.view.View.OnClickListener} will be called.
     * @return The Dialog for chaining methods.
     */
    public Dialog positiveActionClickListener(View.OnClickListener listener){
        mPositiveAction.setOnClickListener(listener);
        return this;
    }

    /**
     * Set the text of negative action button.
     * @param action
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeAction(CharSequence action){
        mNegativeAction.setText(action);
        mNegativeAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
        return this;
    }

    /**
     * Set the text of negative action button.
     * @param id The resourceId of text.
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeAction(int id){
        return negativeAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    /**
     * Set the background drawable of negative action button.
     * @param drawable The background drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeActionBackground(Drawable drawable){
        ViewUtil.setBackground(mNegativeAction, drawable);
        return this;
    }

    /**
     * Set the background drawable of neagtive action button.
     * @param id The resourceId of drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeActionBackground(int id){
        return negativeActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    /**
     * Set the RippleEffect of negative action button.
     * @param resId The resourceId of style.
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        return negativeActionBackground(drawable);
    }

    /**
     * Sets the text color, size, style of negative action button from the specified TextAppearance resource.
     * @param resId The resourceId value.
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeActionTextAppearance(int resId){
        mNegativeAction.setTextAppearance(getContext(), resId);
        return this;
    }

    /**
     * Sets the text color of negative action button.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeActionTextColor(ColorStateList color){
        mNegativeAction.setTextColor(color);
        return this;
    }

    /**
     * Sets the text color of negative action button.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeActionTextColor(int color){
        mNegativeAction.setTextColor(color);
        return this;
    }

    /**
     * Set a listener will be called when negative action button is clicked.
     * @param listener The {@link android.view.View.OnClickListener} will be called.
     * @return The Dialog for chaining methods.
     */
    public Dialog negativeActionClickListener(View.OnClickListener listener){
        mNegativeAction.setOnClickListener(listener);
        return this;
    }

    /**
     * Set the text of neutral action button.
     * @param action
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralAction(CharSequence action){
        mNeutralAction.setText(action);
        mNeutralAction.setVisibility(TextUtils.isEmpty(action) ? View.GONE : View.VISIBLE);
        return this;
    }

    /**
     * Set the text of neutral action button.
     * @param id The resourceId of text.
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralAction(int id){
        return neutralAction(id == 0 ? null : getContext().getResources().getString(id));
    }

    /**
     * Set the background drawable of neutral action button.
     * @param drawable The background drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralActionBackground(Drawable drawable){
        ViewUtil.setBackground(mNeutralAction, drawable);
        return this;
    }

    /**
     * Set the background drawable of neutral action button.
     * @param id The resourceId of drawable.
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralActionBackground(int id){
        return neutralActionBackground(id == 0 ? null : getContext().getResources().getDrawable(id));
    }

    /**
     * Set the RippleEffect of neutral action button.
     * @param resId The resourceId of style.
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralActionRipple(int resId){
        RippleDrawable drawable = new RippleDrawable.Builder(getContext(), resId).build();
        return neutralActionBackground(drawable);
    }

    /**
     * Sets the text color, size, style of neutral action button from the specified TextAppearance resource.
     * @param resId The resourceId value.
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralActionTextAppearance(int resId){
        mNeutralAction.setTextAppearance(getContext(), resId);
        return this;
    }

    /**
     * Sets the text color of neutral action button.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralActionTextColor(ColorStateList color){
        mNeutralAction.setTextColor(color);
        return this;
    }

    /**
     * Sets the text color of neutral action button.
     * @param color
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralActionTextColor(int color){
        mNeutralAction.setTextColor(color);
        return this;
    }

    /**
     * Set a listener will be called when neutral action button is clicked.
     * @param listener The {@link android.view.View.OnClickListener} will be called.
     * @return The Dialog for chaining methods.
     */
    public Dialog neutralActionClickListener(View.OnClickListener listener){
        mNeutralAction.setOnClickListener(listener);
        return this;
    }

    /**
     * Set the layout direction of this Dialog
     * @param direction The layout direction value. Can be {@link android.view.View#LAYOUT_DIRECTION_LTR}, {@link android.view.View#LAYOUT_DIRECTION_RTL} or {@link android.view.View#LAYOUT_DIRECTION_LOCALE}
     * @return The Dialog for chaining methods.
     */
    public Dialog layoutDirection(int direction){
        ViewCompat.setLayoutDirection(mCardView, direction);
        return this;
    }

    /**
     * Set the animation when Dialog enter the screen.
     * @param resId The resourceId of animation.
     * @return The Dialog for chaining methods.
     */
    public Dialog inAnimation(int resId){
        mInAnimationId = resId;
        return this;
    }

    /**
     * Set the animation when Dialog exit the screen.
     * @param resId The resourceId of animation.
     * @return The Dialog for chaining methods.
     */
    public Dialog outAnimation(int resId){
        mOutAnimationId = resId;
        return this;
    }

    /**
     * Indicate that Dialog should show divider when the content is longer than container view.
     * @param show
     * @return The Dialog for chaining methods.
     */
    public Dialog showDivider(boolean show){
        mCardView.setShowDivider(show);
        return this;
    }

    /**
     * Set the content view of this Dialog.
     * @param v The content view.
     * @return The Dialog for chaining methods.
     */
    public Dialog contentView(View v){
        if(mContent != v) {
            if(mContent != null)
                mCardView.removeView(mContent);

            mContent = v;
        }

        if(mContent != null)
            mCardView.addView(mContent);

        return this;
    }

    /**
     * Set the content view of this Dialog.
     * @param layoutId The reourceId of layout.
     * @return The Dialog for chaining methods.
     */
    public Dialog contentView(int layoutId){
        if(layoutId == 0)
            return this;

        View v = LayoutInflater.from(getContext()).inflate(layoutId, null);
        return contentView(v);
    }

    /**
     * Sets whether this dialog is cancelable with the
     * {@link android.view.KeyEvent#KEYCODE_BACK BACK} key.
     *  @return The Dialog for chaining methods.
     */
    public Dialog cancelable(boolean cancelable){
        super.setCancelable(cancelable);
        mCancelable = cancelable;
        return this;
    }

    /**
     * Sets whether this dialog is canceled when touched outside the window's
     * bounds. If setting to true, the dialog is set to be cancelable if not
     * already set.
     *
     * @param cancel Whether the dialog should be canceled when touched outside
     * @return The Dialog for chaining methods.
     */
    public Dialog canceledOnTouchOutside(boolean cancel){
        super.setCanceledOnTouchOutside(cancel);
        mCanceledOnTouchOutside = cancel;
        return this;
    }

    /**
     * Set the margin between content view and Dialog border.
     * @param margin The margin size in pixels.
     * @return The Dialog for chaining methods.
     */
    public Dialog contentMargin(int margin){
        mCardView.setContentMargin(margin);
        return this;
    }

    /**
     * Set the margin between content view and Dialog border.
     * @param left The left margin size in pixels.
     * @param top The top margin size in pixels.
     * @param right The right margin size in pixels.
     * @param bottom The bottom margin size in pixels.
     * @return The Dialog for chaining methods.
     */
    public Dialog contentMargin(int left, int top, int right, int bottom){
        mCardView.setContentMargin(left, top, right, bottom);
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

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        contentView(view);
    }

    @Override
    public void show() {
        if(mInAnimationId != 0)
            mCardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    mCardView.getViewTreeObserver().removeOnPreDrawListener(this);
                    Animation anim = AnimationUtils.loadAnimation(mCardView.getContext(), mInAnimationId);
                    mCardView.startAnimation(anim);
                    return false;
                }
            });
        super.show();
    }

    @Override
    public void dismiss() {
        if(mOutAnimationId != 0){
            Animation anim = AnimationUtils.loadAnimation(mContainer.getContext(), mOutAnimationId);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    mHandler.post(mDismissAction);
                }
            });
            mCardView.startAnimation(anim);
        }
        else
            mHandler.post(mDismissAction);
    }

    private class ContainerFrameLayout extends FrameLayout{

        private boolean mClickOutside = false;

        public ContainerFrameLayout(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            mCardView.measure(widthMeasureSpec, heightMeasureSpec);
            setMeasuredDimension(widthSize, heightSize);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = (right - left - mCardView.getMeasuredWidth()) / 2;
            int childTop = (bottom - top - mCardView.getMeasuredHeight()) / 2;
            int childRight = childLeft + mCardView.getMeasuredWidth();
            int childBottom = childTop + mCardView.getMeasuredHeight();

            mCardView.layout(childLeft, childTop, childRight, childBottom);
        }

        private boolean isOutsideDialog(float x, float y){
            return x < mCardView.getLeft() + mCardView.getPaddingLeft() || x > mCardView.getRight() - mCardView.getPaddingRight() || y < mCardView.getTop() + mCardView.getPaddingTop() || y > mCardView.getBottom() - mCardView.getPaddingBottom();
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

    private class DialogCardView extends CardView{

        private Paint mDividerPaint;
        private float mDividerPos = -1f;
        private boolean mShowDivider = false;

        private int mContentMarginLeft;
        private int mContentMarginTop;
        private int mContentMarginRight;
        private int mContentMarginBottom;

        private boolean mIsRtl = false;

        public DialogCardView(Context context) {
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

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onRtlPropertiesChanged(int layoutDirection) {
            boolean rtl = layoutDirection == LAYOUT_DIRECTION_RTL;
            if(mIsRtl != rtl) {
                mIsRtl = rtl;

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                    int direction = mIsRtl ? TEXT_DIRECTION_RTL : TEXT_DIRECTION_LTR;

                    mTitle.setTextDirection(direction);
                    mPositiveAction.setTextDirection(direction);
                    mNegativeAction.setTextDirection(direction);
                    mNeutralAction.setTextDirection(direction);
                }

                requestLayout();
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);

            int paddingLeft = Math.max(mDialogHorizontalPadding, mCardView.getPaddingLeft());
            int paddingRight = Math.max(mDialogHorizontalPadding, mCardView.getPaddingRight());
            int paddingTop = Math.max(mDialogVerticalPadding, mCardView.getPaddingTop());
            int paddingBottom = Math.max(mDialogVerticalPadding, mCardView.getPaddingBottom());

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

            setMeasuredDimension(width + getPaddingLeft() + getPaddingRight(), height + getPaddingTop() + getPaddingBottom());
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            int childLeft = 0;
            int childTop = 0;
            int childRight = right - left;
            int childBottom = bottom - top;

            childLeft += getPaddingLeft();
            childTop += getPaddingTop();
            childRight -= getPaddingRight();
            childBottom -= getPaddingBottom();

            if(mTitle.getVisibility() == View.VISIBLE) {
                if(mIsRtl)
                    mTitle.layout(childRight - mTitle.getMeasuredWidth(), childTop, childRight, childTop + mTitle.getMeasuredHeight());
                else
                    mTitle.layout(childLeft, childTop, childLeft + mTitle.getMeasuredWidth(), childTop + mTitle.getMeasuredHeight());
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
                int actionLeft = childLeft + mActionOuterPadding;
                int actionRight = childRight - mActionOuterPadding;
                int actionTop = childBottom - mActionOuterHeight + temp;
                int actionBottom = childBottom - temp;
                boolean hasAction = false;

                if(mIsRtl){
                    if (mPositiveAction.getVisibility() == View.VISIBLE) {
                        mPositiveAction.layout(actionLeft , actionTop, actionLeft + mPositiveAction.getMeasuredWidth(), actionBottom);
                        actionLeft += mPositiveAction.getMeasuredWidth() + mActionPadding;
                        hasAction = true;
                    }

                    if (mNegativeAction.getVisibility() == View.VISIBLE) {
                        mNegativeAction.layout(actionLeft, actionTop, actionLeft + mNegativeAction.getMeasuredWidth(), actionBottom);
                        hasAction = true;
                    }

                    if (mNeutralAction.getVisibility() == View.VISIBLE) {
                        mNeutralAction.layout(actionRight - mNeutralAction.getMeasuredWidth(), actionTop, actionRight, actionBottom);
                        hasAction = true;
                    }
                }
                else {

                    if (mPositiveAction.getVisibility() == View.VISIBLE) {
                        mPositiveAction.layout(actionRight - mPositiveAction.getMeasuredWidth(), actionTop, actionRight, actionBottom);
                        actionRight -= mPositiveAction.getMeasuredWidth() + mActionPadding;
                        hasAction = true;
                    }

                    if (mNegativeAction.getVisibility() == View.VISIBLE) {
                        mNegativeAction.layout(actionRight - mNegativeAction.getMeasuredWidth(), actionTop, actionRight, actionBottom);
                        hasAction = true;
                    }

                    if (mNeutralAction.getVisibility() == View.VISIBLE) {
                        mNeutralAction.layout(actionLeft, actionTop, actionLeft + mNeutralAction.getMeasuredWidth(), actionBottom);
                        hasAction = true;
                    }
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
                canvas.drawLine(getPaddingLeft(), mDividerPos, getWidth() - getPaddingRight(), mDividerPos, mDividerPaint);
        }

    }

    public static class Builder implements DialogFragment.Builder, Parcelable{

        protected int mStyleId;
        protected int mContentViewId;
        protected CharSequence mTitle;
        protected CharSequence mPositive;
        protected CharSequence mNegative;
        protected CharSequence mNeutral;

        protected Dialog mDialog;

        public Builder(){
            this(R.style.Material_App_Dialog_Light);
        }

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

        public Dialog getDialog(){
            return mDialog;
        }

        @Override
        public void onPositiveActionClicked(DialogFragment fragment) {
            fragment.dismiss();
        }

        @Override
        public void onNegativeActionClicked(DialogFragment fragment) {
            fragment.dismiss();
        }

        @Override
        public void onNeutralActionClicked(DialogFragment fragment) {
            fragment.dismiss();
        }

        @Override
        public Dialog build(Context context) {
            mDialog = onBuild(context, mStyleId);

            mDialog.title(mTitle)
                    .positiveAction(mPositive)
                    .negativeAction(mNegative)
                    .neutralAction(mNeutral);

            if(mContentViewId != 0)
                mDialog.contentView(mContentViewId);

            onBuildDone(mDialog);

            return mDialog;
        }

        /**
         * Get a appropriate Dialog instance will be used for styling later.
         * Child class should override this function to return appropriate Dialog instance.
         * If you want to apply styling to dialog, or get content view, you should do it in {@link #onBuildDone(Dialog)}
         * @param context A Context instance.
         * @param styleId The resourceId of Dialog's style.
         * @return A Dialog instance will be used for styling later.
         */
        protected Dialog onBuild(Context context, int styleId){
            return new Dialog(context, styleId);
        }

        /**
         * This function will be called after Builder done apply styling to Dialog.
         * @param dialog The Dialog instance.
         */
        protected void onBuildDone(Dialog dialog){}

        protected Builder(Parcel in) {
            mStyleId = in.readInt();
            mContentViewId = in.readInt();
            mTitle = (CharSequence)in.readParcelable(null);
            mPositive = (CharSequence)in.readParcelable(null);
            mNegative = (CharSequence)in.readParcelable(null);
            mNeutral = (CharSequence)in.readParcelable(null);

            onReadFromParcel(in);
        }

        /**
         * Child class should override this function and read back any saved attributes.
         * All read methods should be called after super.onReadFromParcel() call to keep the order.
         */
        protected void onReadFromParcel(Parcel in){}

        /**
         * Child class should override this function and write down all attributes will be saved.
         * All write methods should be called after super.onWriteToParcel() call to keep the order.
         */
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
