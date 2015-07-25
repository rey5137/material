package com.rey.material.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParserException;

import android.annotation.TargetApi;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.text.method.MovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.widget.*;

import com.rey.material.R;
import com.rey.material.app.ThemeManager;
import com.rey.material.drawable.DividerDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;

public class EditText extends FrameLayout implements ThemeManager.OnThemeChangedListener{

    protected int mStyleId;
    protected int mCurrentStyle = ThemeManager.THEME_UNDEFINED;

	private boolean mLabelEnable = false;
    private boolean mLabelVisible = false;
    protected int mSupportMode = SUPPORT_MODE_NONE;
    protected int mAutoCompleteMode = AUTOCOMPLETE_MODE_NONE;

    /**
     * Indicate this EditText should not show a support text.  
     */
	public static final int SUPPORT_MODE_NONE 					= 0;
    /**
     * Indicate this EditText should show a helper text, or error text if it's set. 
     */
	public static final int SUPPORT_MODE_HELPER 				= 1;
    /**
     * Indicate this EditText should show a helper text, along with error text if it's set.
     */
	public static final int SUPPORT_MODE_HELPER_WITH_ERROR 		= 2;
    /**
     * Indicate this EditText should show a char counter text.
     */
	public static final int SUPPORT_MODE_CHAR_COUNTER 			= 3;

    public static final int AUTOCOMPLETE_MODE_NONE 				= 0;
    public static final int AUTOCOMPLETE_MODE_SINGLE 			= 1;
    public static final int AUTOCOMPLETE_MODE_MULTI      		= 2;
	
	private ColorStateList mDividerColors;
	private ColorStateList mDividerErrorColors;
    private boolean mDividerCompoundPadding = true;
    private int mDividerPadding = -1;
	
	private ColorStateList mSupportColors;
	private ColorStateList mSupportErrorColors;
	private int mSupportMaxChars;
	private CharSequence mSupportHelper;
	private CharSequence mSupportError;
	
	private int mLabelInAnimId = 0;
	private int mLabelOutAnimId = 0;
	
	protected LabelView mLabelView;
    protected android.widget.EditText mInputView;
    protected LabelView mSupportView;
	private DividerDrawable mDivider;

    private TextView.OnSelectionChangedListener mOnSelectionChangedListener;

    private boolean mIsRtl = false;

    public EditText(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public EditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

	public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		
		init(context, attrs, defStyleAttr, 0);
	}

    public EditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }
	
	@SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
		applyStyle(context, attrs, defStyleAttr, defStyleRes);
        mStyleId = ThemeManager.getStyleId(context, attrs, defStyleAttr, defStyleRes);
	}

    public void applyStyle(int resId){
        ViewUtil.applyStyle(this, resId);
        applyStyle(getContext(), null, 0, resId);
    }

    private LabelView getLabelView(){
        if(mLabelView == null){
            mLabelView = new LabelView(getContext());
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                mLabelView.setTextDirection(mIsRtl ? TEXT_DIRECTION_RTL : TEXT_DIRECTION_LTR);
            mLabelView.setGravity(Gravity.START);
            mLabelView.setSingleLine(true);
        }

        return mLabelView;
    }

    private LabelView getSupportView(){
        if(mSupportView == null)
            mSupportView = new LabelView(getContext());

        return mSupportView;
    }

    private boolean needCreateInputView(int autoCompleteMode){
        if(mInputView == null)
            return true;

        switch (autoCompleteMode){
            case AUTOCOMPLETE_MODE_NONE:
                return !(mInputView instanceof InternalEditText);
            case AUTOCOMPLETE_MODE_SINGLE:
                return !(mInputView instanceof InternalAutoCompleteTextView);
            case AUTOCOMPLETE_MODE_MULTI:
                return !(mInputView instanceof InternalMultiAutoCompleteTextView);
            default:
                return false;
        }
    }

    protected void applyStyle(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        CharSequence text = mInputView == null ? null : mInputView.getText();
        removeAllViews();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EditText, defStyleAttr, defStyleRes);

        int labelPadding = -1;
        int labelTextSize = -1;
        ColorStateList labelTextColor = null;
        int supportPadding = -1;
        int supportTextSize = -1;
        ColorStateList supportColors = null;
        ColorStateList supportErrorColors = null;
        String supportHelper = null;
        String supportError = null;
        ColorStateList dividerColors = null;
        ColorStateList dividerErrorColors = null;
        int dividerHeight = -1;
        int dividerPadding = -1;
        int dividerAnimDuration = -1;

        mAutoCompleteMode = a.getInteger(R.styleable.EditText_et_autoCompleteMode, mAutoCompleteMode);
        if(needCreateInputView(mAutoCompleteMode)){
            switch (mAutoCompleteMode){
                case AUTOCOMPLETE_MODE_SINGLE:
                    mInputView = new InternalAutoCompleteTextView(context, attrs, defStyleAttr);
                    break;
                case AUTOCOMPLETE_MODE_MULTI:
                    mInputView = new InternalMultiAutoCompleteTextView(context, attrs, defStyleAttr);
                    break;
                default:
                    mInputView = new InternalEditText(context, attrs, defStyleAttr);
                    break;
            }
            ViewUtil.applyFont(mInputView, attrs, defStyleAttr, defStyleRes);
            if(text != null)
                mInputView.setText(text);

            mInputView.addTextChangedListener(new InputTextWatcher());

            if(mDivider != null){
                mDivider.setAnimEnable(false);
                ViewUtil.setBackground(mInputView, mDivider);
                mDivider.setAnimEnable(true);
            }
        }
        else
            ViewUtil.applyStyle(mInputView, attrs, defStyleAttr, defStyleRes);
        mInputView.setVisibility(View.VISIBLE);
        mInputView.setFocusableInTouchMode(true);

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);

            if(attr == R.styleable.EditText_et_labelEnable)
                mLabelEnable = a.getBoolean(attr, false);
            else if(attr == R.styleable.EditText_et_labelPadding)
                labelPadding = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.EditText_et_labelTextSize)
                labelTextSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.EditText_et_labelTextColor)
                labelTextColor = a.getColorStateList(attr);
            else if(attr == R.styleable.EditText_et_labelTextAppearance)
                getLabelView().setTextAppearance(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.EditText_et_labelEllipsize){
                int labelEllipsize = a.getInteger(attr, 0);
                switch (labelEllipsize) {
                    case 1:
                        getLabelView().setEllipsize(TruncateAt.START);
                        break;
                    case 2:
                        getLabelView().setEllipsize(TruncateAt.MIDDLE);
                        break;
                    case 3:
                        getLabelView().setEllipsize(TruncateAt.END);
                        break;
                    case 4:
                        getLabelView().setEllipsize(TruncateAt.MARQUEE);
                        break;
                    default:
                        getLabelView().setEllipsize(TruncateAt.END);
                        break;
                }
            }
            else if(attr == R.styleable.EditText_et_labelInAnim)
                mLabelInAnimId = a.getResourceId(attr, 0);
            else if(attr == R.styleable.EditText_et_labelOutAnim)
                mLabelOutAnimId = a.getResourceId(attr, 0);
            else if(attr == R.styleable.EditText_et_supportMode)
                mSupportMode = a.getInteger(attr, 0);
            else if(attr == R.styleable.EditText_et_supportPadding)
                supportPadding = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.EditText_et_supportTextSize)
                supportTextSize = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.EditText_et_supportTextColor)
                supportColors = a.getColorStateList(attr);
            else if(attr == R.styleable.EditText_et_supportTextErrorColor)
                supportErrorColors = a.getColorStateList(attr);
            else if(attr == R.styleable.EditText_et_supportTextAppearance)
                getSupportView().setTextAppearance(context, a.getResourceId(attr, 0));
            else if(attr == R.styleable.EditText_et_supportEllipsize){
                int supportEllipsize = a.getInteger(attr, 0);
                switch (supportEllipsize) {
                    case 1:
                        getSupportView().setEllipsize(TruncateAt.START);
                        break;
                    case 2:
                        getSupportView().setEllipsize(TruncateAt.MIDDLE);
                        break;
                    case 3:
                        getSupportView().setEllipsize(TruncateAt.END);
                        break;
                    case 4:
                        getSupportView().setEllipsize(TruncateAt.MARQUEE);
                        break;
                    default:
                        getSupportView().setEllipsize(TruncateAt.END);
                        break;
                }
            }
            else if(attr == R.styleable.EditText_et_supportMaxLines)
                getSupportView().setMaxLines(a.getInteger(attr, 0));
            else if(attr == R.styleable.EditText_et_supportLines)
                getSupportView().setLines(a.getInteger(attr, 0));
            else if(attr == R.styleable.EditText_et_supportSingleLine)
                getSupportView().setSingleLine(a.getBoolean(attr, false));
            else if(attr == R.styleable.EditText_et_supportMaxChars)
                mSupportMaxChars = a.getInteger(attr, 0);
            else if(attr == R.styleable.EditText_et_helper)
                supportHelper = a.getString(attr);
            else if(attr == R.styleable.EditText_et_error)
                supportError = a.getString(attr);
            else if(attr == R.styleable.EditText_et_inputId)
                mInputView.setId(a.getResourceId(attr, 0));
            else if(attr == R.styleable.EditText_et_dividerColor)
                dividerColors = a.getColorStateList(attr);
            else if(attr == R.styleable.EditText_et_dividerErrorColor)
                dividerErrorColors = a.getColorStateList(attr);
            else if(attr == R.styleable.EditText_et_dividerHeight)
                dividerHeight = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.EditText_et_dividerPadding)
                dividerPadding = a.getDimensionPixelSize(attr, 0);
            else if(attr == R.styleable.EditText_et_dividerAnimDuration)
                dividerAnimDuration = a.getInteger(attr, 0);
            else if(attr == R.styleable.EditText_et_dividerCompoundPadding)
                mDividerCompoundPadding = a.getBoolean(attr, true);
        }

        a.recycle();

        if(mInputView.getId() == 0)
            mInputView.setId(ViewUtil.generateViewId());

        if(mDivider == null){
            mDividerColors = dividerColors;
            mDividerErrorColors = dividerErrorColors;

            if(mDividerColors == null){
                int[][] states = new int[][]{
                        new int[]{-android.R.attr.state_focused},
                        new int[]{android.R.attr.state_focused, android.R.attr.state_enabled},
                };
                int[] colors = new int[]{
                        ThemeUtil.colorControlNormal(context, 0xFF000000),
                        ThemeUtil.colorControlActivated(context, 0xFF000000),
                };

                mDividerColors = new ColorStateList(states, colors);
            }

            if(mDividerErrorColors == null)
                mDividerErrorColors = ColorStateList.valueOf(ThemeUtil.colorAccent(context, 0xFFFF0000));

            if(dividerHeight < 0)
                dividerHeight = 0;

            if(dividerPadding < 0)
                dividerPadding = 0;

            if(dividerAnimDuration < 0)
                dividerAnimDuration = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            mDividerPadding = dividerPadding;
            mInputView.setPadding(0, 0, 0, mDividerPadding + dividerHeight);

            mDivider = new DividerDrawable(dividerHeight, mDividerCompoundPadding ? mInputView.getTotalPaddingLeft() : 0, mDividerCompoundPadding ? mInputView.getTotalPaddingRight() : 0, mDividerColors, dividerAnimDuration);
            mDivider.setInEditMode(isInEditMode());
            mDivider.setAnimEnable(false);
            ViewUtil.setBackground(mInputView, mDivider);
            mDivider.setAnimEnable(true);
        }
        else{
            if(dividerHeight >= 0 || dividerPadding >= 0) {
                if (dividerHeight < 0)
                    dividerHeight = mDivider.getDividerHeight();

                if (dividerPadding >= 0)
                    mDividerPadding = dividerPadding;

                mInputView.setPadding(0, 0, 0, mDividerPadding + dividerHeight);
                mDivider.setDividerHeight(dividerHeight);
                mDivider.setPadding(mDividerCompoundPadding ? mInputView.getTotalPaddingLeft() : 0, mDividerCompoundPadding ? mInputView.getTotalPaddingRight() : 0);
            }

            if(dividerColors != null)
                mDividerColors = dividerColors;

            if(dividerErrorColors != null)
                mDividerErrorColors = dividerErrorColors;

            mDivider.setColor(getError() == null ? mDividerColors : mDividerErrorColors);

            if(dividerAnimDuration >= 0)
                mDivider.setAnimationDuration(dividerAnimDuration);
        }

        if(labelPadding >= 0)
            getLabelView().setPadding(mDivider.getPaddingLeft(), 0, mDivider.getPaddingRight(), labelPadding);

        if(labelTextSize >= 0)
            getLabelView().setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize);

        if(labelTextColor != null)
            getLabelView().setTextColor(labelTextColor);

        if(mLabelEnable){
            mLabelVisible = true;
            mLabelView.setText(mInputView.getHint());
            addView(mLabelView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            setLabelVisible(!TextUtils.isEmpty(mInputView.getText().toString()), false);
        }

        if(supportTextSize >= 0)
            getSupportView().setTextSize(TypedValue.COMPLEX_UNIT_PX, supportTextSize);

        if(supportColors != null)
            mSupportColors = supportColors;
        else if(mSupportColors == null)
            mSupportColors = context.getResources().getColorStateList(R.color.abc_secondary_text_material_light);

        if(supportErrorColors != null)
            mSupportErrorColors = supportErrorColors;
        else if(mSupportErrorColors == null)
            mSupportErrorColors = ColorStateList.valueOf(ThemeUtil.colorAccent(context, 0xFFFF0000));

        if(supportPadding >= 0)
            getSupportView().setPadding(mDivider.getPaddingLeft(), supportPadding, mDivider.getPaddingRight(), 0);

        if(supportHelper == null && supportError == null)
            getSupportView().setTextColor(getError() == null ? mSupportColors : mSupportErrorColors);
        else if(supportHelper != null)
            setHelper(supportHelper);
        else
            setError(supportError);

        if(mSupportMode != SUPPORT_MODE_NONE){
            switch (mSupportMode) {
                case SUPPORT_MODE_CHAR_COUNTER:
                    getSupportView().setGravity(Gravity.END);
                    updateCharCounter(mInputView.getText().length());
                    break;
                case SUPPORT_MODE_HELPER:
                case SUPPORT_MODE_HELPER_WITH_ERROR:
                    getSupportView().setGravity(GravityCompat.START);
                    break;
            }
            addView(mSupportView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        addView(mInputView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        requestLayout();
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        boolean rtl = layoutDirection == LAYOUT_DIRECTION_RTL;
        if(mIsRtl != rtl) {
            mIsRtl = rtl;

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                if(mLabelView != null)
                    mLabelView.setTextDirection(mIsRtl ? TEXT_DIRECTION_RTL : TEXT_DIRECTION_LTR);

                if(mSupportView != null)
                    mSupportView.setTextDirection(mIsRtl ? TEXT_DIRECTION_RTL : TEXT_DIRECTION_LTR);
            }

            requestLayout();
        }
    }

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
				
		int tempWidthSpec = widthMode == MeasureSpec.UNSPECIFIED ? widthMeasureSpec : MeasureSpec.makeMeasureSpec(widthSize - getPaddingLeft() - getPaddingRight(), widthMode);		
		int tempHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
						
		int labelWidth = 0;
		int labelHeight = 0;
		int inputWidth = 0;
		int inputHeight = 0;
		int supportWidth = 0;
		int supportHeight = 0;
		
		if(mLabelView != null && mLabelView.getLayoutParams() != null){
			mLabelView.measure(tempWidthSpec, tempHeightSpec);
			labelWidth = mLabelView.getMeasuredWidth();
			labelHeight = mLabelView.getMeasuredHeight();
		}
		
		mInputView.measure(tempWidthSpec, tempHeightSpec);
		inputWidth = mInputView.getMeasuredWidth();
		inputHeight = mInputView.getMeasuredHeight();
		
		if(mSupportView != null && mSupportView.getLayoutParams() != null){
			mSupportView.measure(tempWidthSpec, tempHeightSpec);
			supportWidth = mSupportView.getMeasuredWidth();
			supportHeight = mSupportView.getMeasuredHeight();
		}
		
		int width = 0;
		int height = 0;
		
		switch (widthMode) {
			case MeasureSpec.UNSPECIFIED:
				width = Math.max(labelWidth, Math.max(inputWidth, supportWidth)) + getPaddingLeft() + getPaddingRight();
				break;
			case MeasureSpec.AT_MOST:
				width = Math.min(widthSize, Math.max(labelWidth, Math.max(inputWidth, supportWidth)) + getPaddingLeft() + getPaddingRight());
				break;
			case MeasureSpec.EXACTLY:
				width = widthSize;
				break;
		}
		
		switch (heightMode) {
			case MeasureSpec.UNSPECIFIED:
				height = labelHeight + inputHeight + supportHeight + getPaddingTop() + getPaddingBottom();
				break;
			case MeasureSpec.AT_MOST:
				height = Math.min(heightSize, labelHeight + inputHeight + supportHeight + getPaddingTop() + getPaddingBottom());
				break;
			case MeasureSpec.EXACTLY:
				height = heightSize;
				break;
		}
		
		setMeasuredDimension(width, height);		
		
		tempWidthSpec = MeasureSpec.makeMeasureSpec(width - getPaddingLeft() - getPaddingRight(),  MeasureSpec.EXACTLY);
		if(mLabelView != null && mLabelView.getLayoutParams() != null)
			mLabelView.measure(tempWidthSpec, tempHeightSpec);
		
		mInputView.measure(tempWidthSpec, tempHeightSpec);
		
		if(mSupportView != null && mSupportView.getLayoutParams() != null)
			mSupportView.measure(tempWidthSpec, tempHeightSpec);				
	}	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = getPaddingLeft();
		int childRight = r - l - getPaddingRight();
		int childTop = getPaddingTop();
		int childBottom = b - t - getPaddingBottom();
		
		if(mLabelView != null){
			mLabelView.layout(childLeft, childTop, childRight, childTop + mLabelView.getMeasuredHeight());
			childTop += mLabelView.getMeasuredHeight();
		}
		
		if(mSupportView != null){
			mSupportView.layout(childLeft, childBottom - mSupportView.getMeasuredHeight(), childRight, childBottom);
			childBottom -= mSupportView.getMeasuredHeight();
		}
		
		mInputView.layout(childLeft, childTop, childRight, childBottom);
	}

    /**
     * Set the helper text of this EditText. Only work with support mode {@link #SUPPORT_MODE_HELPER} and {@link #SUPPORT_MODE_HELPER_WITH_ERROR}.
     * @param helper The helper text.
     */
	public void setHelper(CharSequence helper){
		mSupportHelper = helper;
		setError(mSupportError);
	}

    /**
     * @return The helper text of this EditText.
     */
	public CharSequence getHelper(){
		return mSupportHelper;
	}

    /**
     * Set the error text of this EditText. Only work with support mode {@link #SUPPORT_MODE_HELPER} and {@link #SUPPORT_MODE_HELPER_WITH_ERROR}.
     * @param error The error text. Set null will clear the error.
     */
	public void setError(CharSequence error){
		mSupportError = error;
		
		if(mSupportMode != SUPPORT_MODE_HELPER && mSupportMode != SUPPORT_MODE_HELPER_WITH_ERROR)
			return;		
		
		if(mSupportError != null){
			getSupportView().setTextColor(mSupportErrorColors);
			mDivider.setColor(mDividerErrorColors);
            getSupportView().setText(mSupportMode == SUPPORT_MODE_HELPER ? mSupportError : TextUtils.concat(mSupportHelper, ", ", mSupportError));
		}
		else{
            getSupportView().setTextColor(mSupportColors);
			mDivider.setColor(mDividerColors);
            getSupportView().setText(mSupportHelper);
		}
	}

    /**
     * @return The error text of this EditText.
     */
	public CharSequence getError(){
		return mSupportError;
	}

    /**
     * Clear the error text. Only work with support mode {@link #SUPPORT_MODE_HELPER} and {@link #SUPPORT_MODE_HELPER_WITH_ERROR}.
     */
	public void clearError(){
		setError(null);
	}
	
	private void updateCharCounter(int count){
		if(count == 0){
			getSupportView().setTextColor(mSupportColors);
			mDivider.setColor(mDividerColors);
            getSupportView().setText(null);
		}
		else{
			if(mSupportMaxChars > 0){
                getSupportView().setTextColor(count > mSupportMaxChars ? mSupportErrorColors : mSupportColors);
				mDivider.setColor(count > mSupportMaxChars ? mDividerErrorColors : mDividerColors);
                getSupportView().setText(count + " / " + mSupportMaxChars);
			}
    		else
                getSupportView().setText(String.valueOf(count));
		}
	}

    private void setLabelVisible(boolean visible, boolean animation){
        if(!mLabelEnable || mLabelVisible == visible)
            return;

        mLabelVisible = visible;

        if(animation){
            if(mLabelVisible){
                if(mLabelInAnimId != 0){
                    Animation anim = AnimationUtils.loadAnimation(getContext(), mLabelInAnimId);
                    anim.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                            mLabelView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {}
                    });
                    mLabelView.clearAnimation();
                    mLabelView.startAnimation(anim);
                }
                else
                    mLabelView.setVisibility(View.VISIBLE);
            }
            else{
                if(mLabelOutAnimId != 0){
                    Animation anim = AnimationUtils.loadAnimation(getContext(), mLabelOutAnimId);
                    anim.setAnimationListener(new Animation.AnimationListener() {

                        @Override
                        public void onAnimationStart(Animation animation) {
                            mLabelView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            mLabelView.setVisibility(View.INVISIBLE);
                        }

                    });
                    mLabelView.clearAnimation();
                    mLabelView.startAnimation(anim);
                }
                else
                    mLabelView.setVisibility(View.INVISIBLE);
            }
        }
        else
            mLabelView.setVisibility(mLabelVisible ? View.VISIBLE : View.INVISIBLE);
    }

    /* protected method of AutoCompleteTextView */

    /**
     * <p>Converts the selected item from the drop down list into a sequence
     * of character that can be used in the edit box.</p>
     *
     * @param selectedItem the item selected by the user for completion
     *
     * @return a sequence of characters representing the selected suggestion
     */
    protected CharSequence convertSelectionToString(Object selectedItem) {
        switch (mAutoCompleteMode){
            case AUTOCOMPLETE_MODE_SINGLE:
                return ((InternalAutoCompleteTextView)mInputView).superConvertSelectionToString(selectedItem);
            case AUTOCOMPLETE_MODE_MULTI:
                return ((InternalMultiAutoCompleteTextView)mInputView).superConvertSelectionToString(selectedItem);
            default:
                return null;
        }
    }

    /**
     * <p>Starts filtering the content of the drop down list. The filtering
     * pattern is the content of the edit box. Subclasses should override this
     * method to filter with a different pattern, for instance a substring of
     * <code>text</code>.</p>
     *
     * @param text the filtering pattern
     * @param keyCode the last character inserted in the edit box; beware that
     * this will be null when text is being added through a soft input method.
     */
    protected void performFiltering(CharSequence text, int keyCode) {
        switch (mAutoCompleteMode){
            case AUTOCOMPLETE_MODE_SINGLE:
                ((InternalAutoCompleteTextView)mInputView).superPerformFiltering(text, keyCode);
                break;
            case AUTOCOMPLETE_MODE_MULTI:
                ((InternalMultiAutoCompleteTextView)mInputView).superPerformFiltering(text, keyCode);
                break;
        }
    }

    /**
     * <p>Performs the text completion by replacing the current text by the
     * selected item. Subclasses should override this method to avoid replacing
     * the whole content of the edit box.</p>
     *
     * @param text the selected suggestion in the drop down list
     */
    protected void replaceText(CharSequence text) {
        switch (mAutoCompleteMode){
            case AUTOCOMPLETE_MODE_SINGLE:
                ((InternalAutoCompleteTextView)mInputView).superReplaceText(text);
                break;
            case AUTOCOMPLETE_MODE_MULTI:
                ((InternalMultiAutoCompleteTextView)mInputView).superReplaceText(text);
                break;
        }
    }

    /**
     * Returns the Filter obtained from {@link Filterable#getFilter},
     * or <code>null</code> if {@link #setAdapter} was not called with
     * a Filterable.
     */
    protected Filter getFilter() {
        switch (mAutoCompleteMode){
            case AUTOCOMPLETE_MODE_SINGLE:
                return ((InternalAutoCompleteTextView)mInputView).superGetFilter();
            case AUTOCOMPLETE_MODE_MULTI:
                return ((InternalMultiAutoCompleteTextView)mInputView).superGetFilter();
            default:
                return null;
        }
    }

    /**
     * <p>Starts filtering the content of the drop down list. The filtering
     * pattern is the specified range of text from the edit box. Subclasses may
     * override this method to filter with a different pattern, for
     * instance a smaller substring of <code>text</code>.</p>
     */
    protected void performFiltering(CharSequence text, int start, int end, int keyCode) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_MULTI)
            ((InternalMultiAutoCompleteTextView)mInputView).superPerformFiltering(text, start, end, keyCode);
    }

    /* public method of AutoCompleteTextView */

    /**
     * <p>Sets the optional hint text that is displayed at the bottom of the
     * the matching list.  This can be used as a cue to the user on how to
     * best use the list, or to provide extra information.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param hint the text to be displayed to the user
     *
     * @see #getCompletionHint()
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_completionHint
     */
    public void setCompletionHint(CharSequence hint) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setCompletionHint(hint);
    }

    /**
     * Gets the optional hint text displayed at the bottom of the the matching list.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return The hint text, if any
     *
     * @see #setCompletionHint(CharSequence)
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_completionHint
     */
    public CharSequence getCompletionHint() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE || Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            return null;
        return ((AutoCompleteTextView)mInputView).getCompletionHint();
    }

    /**
     * <p>Returns the current width for the auto-complete drop down list. This can
     * be a fixed width, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill the screen, or
     * {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the width of its anchor view.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the width for the drop down list
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownWidth
     */
    public int getDropDownWidth() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return 0;
        return ((AutoCompleteTextView)mInputView).getDropDownWidth();
    }

    /**
     * <p>Sets the current width for the auto-complete drop down list. This can
     * be a fixed width, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill the screen, or
     * {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the width of its anchor view.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param width the width to use
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownWidth
     */
    public void setDropDownWidth(int width) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setDropDownWidth(width);
    }

    /**
     * <p>Returns the current height for the auto-complete drop down list. This can
     * be a fixed height, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill
     * the screen, or {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the height
     * of the drop down's content.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the height for the drop down list
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownHeight
     */
    public int getDropDownHeight() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return 0;
        return ((AutoCompleteTextView)mInputView).getDropDownHeight();
    }

    /**
     * <p>Sets the current height for the auto-complete drop down list. This can
     * be a fixed height, or {@link ViewGroup.LayoutParams#MATCH_PARENT} to fill
     * the screen, or {@link ViewGroup.LayoutParams#WRAP_CONTENT} to fit the height
     * of the drop down's content.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param height the height to use
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownHeight
     */
    public void setDropDownHeight(int height) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setDropDownHeight(height);
    }

    /**
     * <p>Returns the id for the view that the auto-complete drop down list is anchored to.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the view's id, or {@link View#NO_ID} if none specified
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownAnchor
     */
    public int getDropDownAnchor() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return 0;
        return ((AutoCompleteTextView)mInputView).getDropDownAnchor();
    }

    /**
     * <p>Sets the view to which the auto-complete drop down list should anchor. The view
     * corresponding to this id will not be loaded until the next time it is needed to avoid
     * loading a view which is not yet instantiated.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param id the id to anchor the drop down list view to
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_dropDownAnchor
     */
    public void setDropDownAnchor(int id) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setDropDownAnchor(id);
    }

    /**
     * <p>Gets the background of the auto-complete drop-down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the background drawable
     *
     * @attr ref android.R.styleable#PopupWindow_popupBackground
     */
    public Drawable getDropDownBackground() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return null;
        return ((AutoCompleteTextView)mInputView).getDropDownBackground();
    }

    /**
     * <p>Sets the background of the auto-complete drop-down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param d the drawable to set as the background
     *
     * @attr ref android.R.styleable#PopupWindow_popupBackground
     */
    public void setDropDownBackgroundDrawable(Drawable d) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setDropDownBackgroundDrawable(d);
    }

    /**
     * <p>Sets the background of the auto-complete drop-down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param id the id of the drawable to set as the background
     *
     * @attr ref android.R.styleable#PopupWindow_popupBackground
     */
    public void setDropDownBackgroundResource(int id) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setDropDownBackgroundResource(id);
    }

    /**
     * <p>Sets the vertical offset used for the auto-complete drop-down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param offset the vertical offset
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownVerticalOffset
     */
    public void setDropDownVerticalOffset(int offset) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setDropDownVerticalOffset(offset);
    }

    /**
     * <p>Gets the vertical offset used for the auto-complete drop-down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the vertical offset
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownVerticalOffset
     */
    public int getDropDownVerticalOffset() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return 0;
        return ((AutoCompleteTextView)mInputView).getDropDownVerticalOffset();
    }

    /**
     * <p>Sets the horizontal offset used for the auto-complete drop-down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param offset the horizontal offset
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownHorizontalOffset
     */
    public void setDropDownHorizontalOffset(int offset) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setDropDownHorizontalOffset(offset);
    }

    /**
     * <p>Gets the horizontal offset used for the auto-complete drop-down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the horizontal offset
     *
     * @attr ref android.R.styleable#ListPopupWindow_dropDownHorizontalOffset
     */
    public int getDropDownHorizontalOffset() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return 0;
        return ((AutoCompleteTextView)mInputView).getDropDownHorizontalOffset();
    }

    /**
     * <p>Returns the number of characters the user must type before the drop
     * down list is shown.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the minimum number of characters to type to show the drop down
     *
     * @see #setThreshold(int)
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_completionThreshold
     */
    public int getThreshold() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return 0;
        return ((AutoCompleteTextView)mInputView).getThreshold();
    }

    /**
     * <p>Specifies the minimum number of characters the user has to type in the
     * edit box before the drop down list is shown.</p>
     *
     * <p>When <code>threshold</code> is less than or equals 0, a threshold of
     * 1 is applied.</p>
     *
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param threshold the number of characters to type before the drop down
     *                  is shown
     *
     * @see #getThreshold()
     *
     * @attr ref android.R.styleable#AutoCompleteTextView_completionThreshold
     */
    public void setThreshold(int threshold) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setThreshold(threshold);
    }

    /**
     * <p>Sets the listener that will be notified when the user clicks an item
     * in the drop down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param l the item click listener
     */
    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setOnItemClickListener(l);
    }

    /**
     * <p>Sets the listener that will be notified when the user selects an item
     * in the drop down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param l the item selected listener
     */
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener l) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setOnItemSelectedListener(l);
    }

    /**
     * <p>Returns the listener that is notified whenever the user clicks an item
     * in the drop down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the item click listener
     */
    public AdapterView.OnItemClickListener getOnItemClickListener() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return null;
        return ((AutoCompleteTextView)mInputView).getOnItemClickListener();
    }

    /**
     * <p>Returns the listener that is notified whenever the user selects an
     * item in the drop down list.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the item selected listener
     */
    public AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return null;
        return ((AutoCompleteTextView)mInputView).getOnItemSelectedListener();
    }

    /**
     * <p>Returns a filterable list adapter used for auto completion.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return a data adapter used for auto completion
     */
    public ListAdapter getAdapter() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return null;
        return ((AutoCompleteTextView)mInputView).getAdapter();
    }

    /**
     * <p>Changes the list of data used for auto completion. The provided list
     * must be a filterable list adapter.</p>
     *
     * <p>The caller is still responsible for managing any resources used by the adapter.
     * Notably, when the AutoCompleteTextView is closed or released, the adapter is not notified.
     * A common case is the use of {@link android.widget.CursorAdapter}, which
     * contains a {@link android.database.Cursor} that must be closed.  This can be done
     * automatically (see
     * {@link android.app.Activity#startManagingCursor(android.database.Cursor)
     * startManagingCursor()}),
     * or by manually closing the cursor when the AutoCompleteTextView is dismissed.</p>
     *
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param adapter the adapter holding the auto completion data
     *
     * @see #getAdapter()
     * @see android.widget.Filterable
     * @see android.widget.ListAdapter
     */
    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setAdapter(adapter);
    }

    /**
     * Returns <code>true</code> if the amount of text in the field meets
     * or exceeds the {@link #getThreshold} requirement.  You can override
     * this to impose a different standard for when filtering will be
     * triggered.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     */
    public boolean enoughToFilter() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return false;
        return ((AutoCompleteTextView)mInputView).enoughToFilter();
    }

    /**
     * <p>Indicates whether the popup menu is showing.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return true if the popup menu is showing, false otherwise
     */
    public boolean isPopupShowing() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return false;
        return ((AutoCompleteTextView)mInputView).isPopupShowing();
    }

    /**
     * <p>Clear the list selection.  This may only be temporary, as user input will often bring
     * it back.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     */
    public void clearListSelection() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).clearListSelection();
    }

    /**
     * Set the position of the dropdown view selection.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param position The position to move the selector to.
     */
    public void setListSelection(int position) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setListSelection(position);
    }

    /**
     * Get the position of the dropdown view selection, if there is one.  Returns
     * {@link android.widget.ListView#INVALID_POSITION ListView.INVALID_POSITION} if there is no dropdown or if
     * there is no selection.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @return the position of the current selection, if there is one, or
     * {@link android.widget.ListView#INVALID_POSITION ListView.INVALID_POSITION} if not.
     *
     * @see android.widget.ListView#getSelectedItemPosition()
     */
    public int getListSelection() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return 0;
        return ((AutoCompleteTextView)mInputView).getListSelection();
    }

    /**
     * <p>Performs the text completion by converting the selected item from
     * the drop down list into a string, replacing the text box's content with
     * this string and finally dismissing the drop down menu.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     */
    public void performCompletion() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).performCompletion();
    }

    /**
     * Identifies whether the view is currently performing a text completion, so subclasses
     * can decide whether to respond to text changed events.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     */
    public boolean isPerformingCompletion() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return false;
        return ((AutoCompleteTextView)mInputView).isPerformingCompletion();
    }

    /** <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p> */
    public void onFilterComplete(int count) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            ((InternalAutoCompleteTextView)mInputView).superOnFilterComplete(count);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_MULTI)
            ((InternalMultiAutoCompleteTextView)mInputView).superOnFilterComplete(count);
    }

    /**
     * <p>Closes the drop down if present on screen.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     */
    public void dismissDropDown() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).dismissDropDown();
    }

    /**
     * <p>Displays the drop down on screen.</p>
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     */
    public void showDropDown() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).showDropDown();
    }

    /**
     * Sets the validator used to perform text validation.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @param validator The validator used to validate the text entered in this widget.
     *
     * @see #getValidator()
     * @see #performValidation()
     */
    public void setValidator(AutoCompleteTextView.Validator validator) {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).setValidator(validator);
    }

    /**
     * Returns the Validator set with {@link #setValidator},
     * or <code>null</code> if it was not set.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @see #setValidator(android.widget.AutoCompleteTextView.Validator)
     * @see #performValidation()
     */
    public AutoCompleteTextView.Validator getValidator() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return null;
        return ((AutoCompleteTextView)mInputView).getValidator();
    }

    /**
     * If a validator was set on this view and the current string is not valid,
     * ask the validator to fix it.
     * <p>Only work when autoComplete mode is {@link #AUTOCOMPLETE_MODE_SINGLE} or {@link #AUTOCOMPLETE_MODE_MULTI}</p>
     *
     * @see #getValidator()
     * @see #setValidator(android.widget.AutoCompleteTextView.Validator)
     */
    public void performValidation() {
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return;
        ((AutoCompleteTextView)mInputView).performValidation();
    }

    /**
     * Sets the Tokenizer that will be used to determine the relevant
     * range of the text where the user is typing.
     * <p>Only work when autoCompleMode is AUTOCOMPLETE_MODE_MULTI</p>
     */
    public void setTokenizer(MultiAutoCompleteTextView.Tokenizer t) {
        if(mAutoCompleteMode != AUTOCOMPLETE_MODE_MULTI)
            return;
        ((MultiAutoCompleteTextView)mInputView).setTokenizer(t);
    }

	/* public method of EditText */

    @Override
    public void setEnabled(boolean enabled){
        mInputView.setEnabled(enabled);
    }
	
	/**
     * Convenience for {@link android.text.Selection#extendSelection}.
     */
	public void extendSelection (int index){
		mInputView.extendSelection(index);
	}
	
	public Editable getText (){
		return mInputView.getText();
	}

    /**
     * Convenience for {@link android.text.Selection#selectAll}.
     */
	public void selectAll (){
		mInputView.selectAll();
	}

    /**
     * Causes words in the text that are longer than the view is wide
     * to be ellipsized instead of broken in the middle.  You may also
     * want to {@link #setSingleLine} or {@link #setHorizontallyScrolling}
     * to constrain the text to a single line.  Use <code>null</code>
     * to turn off ellipsizing.
     *
     * If {@link #setMaxLines} has been used to set two or more lines,
     * only {@link android.text.TextUtils.TruncateAt#END} and
     * {@link android.text.TextUtils.TruncateAt#MARQUEE} are supported
     * (other ellipsizing types will not do anything).
     *
     * @attr ref android.R.styleable#TextView_ellipsize
     */
	public void setEllipsize (TruncateAt ellipsis){
		mInputView.setEllipsize(ellipsis);
	}
	
	/**
     * Convenience for {@link android.text.Selection#setSelection(android.text.Spannable, int)}.
     */
	public void setSelection (int index){
		mInputView.setSelection(index);
	}
	
	/**
     * Convenience for {@link android.text.Selection#setSelection(android.text.Spannable, int, int)}.
     */
	public void setSelection (int start, int stop){
		mInputView.setSelection(start, stop);
	}
	
	public void setText (CharSequence text, android.widget.TextView.BufferType type){
		mInputView.setText(text, type);
	}
	
	/**
     * Adds a TextWatcher to the list of those whose methods are called
     * whenever this TextView's text changes.
     * <p>
     * In 1.0, the {@link android.text.TextWatcher#afterTextChanged} method was erroneously
     * not called after {@link #setText} calls.  Now, doing {@link #setText}
     * if there are any text changed listeners forces the buffer type to
     * Editable if it would not otherwise be and does call this method.
     */
	public void addTextChangedListener(TextWatcher textWatcher){
		mInputView.addTextChangedListener(textWatcher);
	}
	
	/**
     * Convenience method: Append the specified text to the TextView's
     * display buffer, upgrading it to BufferType.EDITABLE if it was
     * not already editable.
     */
	public final void append (CharSequence text){
		mInputView.append(text);
	}
	
	/**
     * Convenience method: Append the specified text slice to the TextView's
     * display buffer, upgrading it to BufferType.EDITABLE if it was
     * not already editable.
     */
	public void append (CharSequence text, int start, int end){
		mInputView.append(text, start, end);
	}
	
	public void beginBatchEdit (){
		mInputView.beginBatchEdit();
	}
	
	/**
     * Move the point, specified by the offset, into the view if it is needed.
     * This has to be called after layout. Returns true if anything changed.
     */
	public boolean bringPointIntoView (int offset){
		return mInputView.bringPointIntoView(offset);
	}	
	
	public void cancelLongPress (){
		mInputView.cancelLongPress();
	}
	
	/**
     * Use {@link android.view.inputmethod.BaseInputConnection#removeComposingSpans
     * BaseInputConnection.removeComposingSpans()} to remove any IME composing
     * state from this text view.
     */
	public void clearComposingText (){
		mInputView.clearComposingText();
	}
	
	@Override
	public void computeScroll (){
		mInputView.computeScroll();
	}
	
	@Override
	public void debug (int depth){
		mInputView.debug(depth);
	}
	
	 /**
     * Returns true, only while processing a touch gesture, if the initial
     * touch down event caused focus to move to the text view and as a result
     * its selection changed.  Only valid while processing the touch gesture
     * of interest, in an editable text view.
     */
	public boolean didTouchFocusSelect (){
		return mInputView.didTouchFocusSelect();
	}
	
	public void endBatchEdit (){
		mInputView.endBatchEdit();
	}
	
	/**
     * If this TextView contains editable content, extract a portion of it
     * based on the information in <var>request</var> in to <var>outText</var>.
     * @return Returns true if the text was successfully extracted, else false.
     */
	public boolean extractText (ExtractedTextRequest request, ExtractedText outText){
		return mInputView.extractText(request, outText);
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void findViewsWithText (ArrayList<View> outViews, CharSequence searched, int flags){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			mInputView.findViewsWithText(outViews, searched, flags);
	}
	
	/**
     * Gets the autolink mask of the text.  See {@link
     * android.text.util.Linkify#ALL Linkify.ALL} and peers for
     * possible values.
     *
     * @attr ref android.R.styleable#TextView_autoLink
     */
	public final int getAutoLinkMask (){
		return mInputView.getAutoLinkMask();
	}
	
	@Override
	public int getBaseline (){
		return mInputView.getBaseline();
	}
	
	/**
     * Returns the padding between the compound drawables and the text.
     *
     * @attr ref android.R.styleable#TextView_drawablePadding
     */
	public int getCompoundDrawablePadding (){
		return mInputView.getCompoundDrawablePadding();
	}
	
	/**
     * Returns drawables for the left, top, right, and bottom borders.
     *
     * @attr ref android.R.styleable#TextView_drawableLeft
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableRight
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
	public Drawable[] getCompoundDrawables (){
		return mInputView.getCompoundDrawables();
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public Drawable[] getCompoundDrawablesRelative (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return mInputView.getCompoundDrawablesRelative();
		
		return mInputView.getCompoundDrawables();
	}
	
	/**
     * Returns the bottom padding of the view, plus space for the bottom
     * Drawable if any.
     */
	public int getCompoundPaddingBottom (){
		return mInputView.getCompoundPaddingBottom();
	}
	
	/**
     * Returns the end padding of the view, plus space for the end
     * Drawable if any.
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public int getCompoundPaddingEnd (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return mInputView.getCompoundPaddingEnd();
		
		return mInputView.getCompoundPaddingRight();
	}
	
	/**
     * Returns the left padding of the view, plus space for the left
     * Drawable if any.
     */
	public int getCompoundPaddingLeft (){
		return mInputView.getCompoundPaddingLeft();
	}
	
	/**
     * Returns the right padding of the view, plus space for the right
     * Drawable if any.
     */
	public int getCompoundPaddingRight (){
		return mInputView.getCompoundPaddingRight();
	}
	
	/**
     * Returns the start padding of the view, plus space for the start
     * Drawable if any.
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public int getCompoundPaddingStart (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return mInputView.getCompoundPaddingStart();
		
		return mInputView.getCompoundPaddingLeft();
	}
	
	/**
     * Returns the top padding of the view, plus space for the top
     * Drawable if any.
     */
	public int getCompoundPaddingTop (){
		return mInputView.getCompoundPaddingTop();
	}
	
	/**
     * <p>Return the current color selected to paint the hint text.</p>
     *
     * @return Returns the current hint text color.
     */
	public final int getCurrentHintTextColor (){
		return mInputView.getCurrentHintTextColor();
	}
	
	/**
     * <p>Return the current color selected for normal text.</p>
     *
     * @return Returns the current text color.
     */
	public final int getCurrentTextColor (){
		return mInputView.getCurrentTextColor();
	}
	
	/**
     * Retrieves the value set in {@link #setCustomSelectionActionModeCallback}. Default is null.
     *
     * @return The current custom selection callback.
     */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public ActionMode.Callback getCustomSelectionActionModeCallback (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			return mInputView.getCustomSelectionActionModeCallback();
		
		return null;
	}
	
	/**
     * Return the text the TextView is displaying as an Editable object.  If
     * the text is not editable, null is returned.
     *
     * @see #getText
     */
	public Editable getEditableText (){
		return mInputView.getEditableText();
	}
	
	/**
     * Returns where, if anywhere, words that are longer than the view
     * is wide should be ellipsized.
     */
	public TruncateAt getEllipsize (){
		return mInputView.getEllipsize();
	}
	
	/**
     * Returns the extended bottom padding of the view, including both the
     * bottom Drawable if any and any extra space to keep more than maxLines
     * of text from showing.  It is only valid to call this after measuring.
     */
	public int getExtendedPaddingBottom (){
		return mInputView.getExtendedPaddingBottom();
	}
	
	/**
     * Returns the extended top padding of the view, including both the
     * top Drawable if any and any extra space to keep more than maxLines
     * of text from showing.  It is only valid to call this after measuring.
     */
	public int getExtendedPaddingTop (){
		return mInputView.getExtendedPaddingTop();
	}
	
	/**
     * Returns the current list of input filters.
     *
     * @attr ref android.R.styleable#TextView_maxLength
     */
	public InputFilter[] getFilters (){
		return mInputView.getFilters();
	}
	
	@Override
	public void getFocusedRect (@NonNull Rect r){
		mInputView.getFocusedRect(r);
	}
	
	/**
     * @return the currently set font feature settings.  Default is null.
     *
     * @see #setFontFeatureSettings(String)
     * @see android.graphics.Paint#setFontFeatureSettings
     */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public String getFontFeatureSettings (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return mInputView.getFontFeatureSettings();
		return null;
	}
	
	/**
     * Return whether this text view is including its entire text contents
     * in frozen icicles.
     *
     * @return Returns true if text is included, false if it isn't.
     *
     * @see #setFreezesText
     */
	public boolean getFreezesText (){
		return mInputView.getFreezesText();
	}
	
	/**
     * Returns the horizontal and vertical alignment of this TextView.
     *
     * @see android.view.Gravity
     * @attr ref android.R.styleable#TextView_gravity
     */
	public int getGravity (){
		return mInputView.getGravity();
	}
	
	/**
     * @return the color used to display the selection highlight
     *
     * @see #setHighlightColor(int)
     *
     * @attr ref android.R.styleable#TextView_textColorHighlight
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getHighlightColor (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getHighlightColor();
		
		return 0;
	}
	
	/**
     * Returns the hint that is displayed when the text of the TextView
     * is empty.
     *
     * @attr ref android.R.styleable#TextView_hint
     */
	public CharSequence getHint (){
		return mInputView.getHint();
	}
	
	/**
     * @return the color of the hint text, for the different states of this TextView.
     *
     * @see #setHintTextColor(android.content.res.ColorStateList)
     * @see #setHintTextColor(int)
     * @see #setTextColor(android.content.res.ColorStateList)
     * @see #setLinkTextColor(android.content.res.ColorStateList)
     *
     * @attr ref android.R.styleable#TextView_textColorHint
     */
	public final ColorStateList getHintTextColors (){
		return mInputView.getHintTextColors();
	}
	
	/**
     * Get the IME action ID previous set with {@link #setImeActionLabel}.
     *
     * @see #setImeActionLabel
     * @see android.view.inputmethod.EditorInfo
     */
	public int getImeActionId (){
		return mInputView.getImeActionId();
	}
	
	/**
     * Get the IME action label previous set with {@link #setImeActionLabel}.
     *
     * @see #setImeActionLabel
     * @see android.view.inputmethod.EditorInfo
     */
	public CharSequence getImeActionLabel (){
		return mInputView.getImeActionLabel();
	}
	
	/**
     * Get the type of the IME editor.
     *
     * @see #setImeOptions(int)
     * @see android.view.inputmethod.EditorInfo
     */
	public int getImeOptions (){
		return mInputView.getImeOptions();
	}
	
	/**
     * Gets whether the TextView includes extra top and bottom padding to make
     * room for accents that go above the normal ascent and descent.
     *
     * @see #setIncludeFontPadding(boolean)
     *
     * @attr ref android.R.styleable#TextView_includeFontPadding
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean getIncludeFontPadding (){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && mInputView.getIncludeFontPadding();
    }
	
	/**
     * Retrieve the input extras currently associated with the text view, which
     * can be viewed as well as modified.
     *
     * @param create If true, the extras will be created if they don't already
     * exist.  Otherwise, null will be returned if none have been created.
     * @see #setInputExtras(int)
     * @see android.view.inputmethod.EditorInfo#extras
     * @attr ref android.R.styleable#TextView_editorExtras
     */
	public Bundle getInputExtras (boolean create){
		return mInputView.getInputExtras(create);
	}
	
	/**
     * Get the type of the editable content.
     *
     * @see #setInputType(int)
     * @see android.text.InputType
     */
	public int getInputType (){
		return mInputView.getInputType();
	}
	
	/**
     * @return the current key listener for this TextView.
     * This will frequently be null for non-EditText TextViews.
     *
     * @attr ref android.R.styleable#TextView_numeric
     * @attr ref android.R.styleable#TextView_digits
     * @attr ref android.R.styleable#TextView_phoneNumber
     * @attr ref android.R.styleable#TextView_inputMethod
     * @attr ref android.R.styleable#TextView_capitalize
     * @attr ref android.R.styleable#TextView_autoText
     */
	public final KeyListener getKeyListener (){
		return mInputView.getKeyListener();
	}
	
	/**
     * @return the Layout that is currently being used to display the text.
     * This can be null if the text or width has recently changes.
     */
	public final Layout getLayout (){
		return mInputView.getLayout();
	}
	
	/**
     * @return the extent by which text is currently being letter-spaced.
     * This will normally be 0.
     *
     * @see #setLetterSpacing(float)
     * @see android.graphics.Paint#setLetterSpacing
     */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public float getLetterSpacing (){
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? mInputView.getLetterSpacing() : 0;
	}
	
	/**
     * Return the baseline for the specified line (0...getLineCount() - 1)
     * If bounds is not null, return the top, left, right, bottom extents
     * of the specified line in it. If the internal Layout has not been built,
     * return 0 and set bounds to (0, 0, 0, 0)
     * @param line which line to examine (0..getLineCount() - 1)
     * @param bounds Optional. If not null, it returns the extent of the line
     * @return the Y-coordinate of the baseline
     */
	public int getLineBounds (int line, Rect bounds){
		return mInputView.getLineBounds(line, bounds);
	}
	
	/**
     * Return the number of lines of text, or 0 if the internal Layout has not
     * been built.
     */
	public int getLineCount (){
		return mInputView.getLineCount();
	}
	
	/**
     * @return the height of one standard line in pixels.  Note that markup
     * within the text can cause individual lines to be taller or shorter
     * than this height, and the layout may contain additional first-
     * or last-line padding.
     */
	public int getLineHeight (){
		return mInputView.getLineHeight();
	}
	
	/**
     * Gets the line spacing extra space
     *
     * @return the extra space that is added to the height of each lines of this TextView.
     *
     * @see #setLineSpacing(float, float)
     * @see #getLineSpacingMultiplier()
     *
     * @attr ref android.R.styleable#TextView_lineSpacingExtra
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public float getLineSpacingExtra (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getLineSpacingExtra();
		return 0f;
	}
	
	/**
     * Gets the line spacing multiplier
     *
     * @return the value by which each line's height is multiplied to get its actual height.
     *
     * @see #setLineSpacing(float, float)
     * @see #getLineSpacingExtra()
     *
     * @attr ref android.R.styleable#TextView_lineSpacingMultiplier
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public float getLineSpacingMultiplier (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getLineSpacingMultiplier();
		return 0f;
	}
	
	/**
     * @return the list of colors used to paint the links in the text, for the different states of
     * this TextView
     *
     * @see #setLinkTextColor(android.content.res.ColorStateList)
     * @see #setLinkTextColor(int)
     *
     * @attr ref android.R.styleable#TextView_textColorLink
     */
	public final ColorStateList getLinkTextColors (){
		return mInputView.getLinkTextColors();
	}
	
	/**
     * Returns whether the movement method will automatically be set to
     * {@link android.text.method.LinkMovementMethod} if {@link #setAutoLinkMask} has been
     * set to nonzero and links are detected in {@link #setText}.
     * The default is true.
     *
     * @attr ref android.R.styleable#TextView_linksClickable
     */
	public final boolean getLinksClickable (){
		return mInputView.getLinksClickable();
	}
	
	/**
     * Gets the number of times the marquee animation is repeated. Only meaningful if the
     * TextView has marquee enabled.
     *
     * @return the number of times the marquee animation is repeated. -1 if the animation
     * repeats indefinitely
     *
     * @see #setMarqueeRepeatLimit(int)
     *
     * @attr ref android.R.styleable#TextView_marqueeRepeatLimit
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMarqueeRepeatLimit (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMarqueeRepeatLimit();
		
		return -1;
	}
	
	/**
     * @return the maximum width of the TextView, expressed in ems or -1 if the maximum width
     * was set in pixels instead (using {@link #setMaxWidth(int)} or {@link #setWidth(int)}).
     *
     * @see #setMaxEms(int)
     * @see #setEms(int)
     *
     * @attr ref android.R.styleable#TextView_maxEms
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMaxEms (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMaxEms();
		
		return -1;
	}
	
	/**
     * @return the maximum height of this TextView expressed in pixels, or -1 if the maximum
     * height was set in number of lines instead using {@link #setMaxLines(int) or #setLines(int)}.
     *
     * @see #setMaxHeight(int)
     *
     * @attr ref android.R.styleable#TextView_maxHeight
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMaxHeight (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMaxHeight();
		
		return -1;
	}
	
	/**
     * @return the maximum number of lines displayed in this TextView, or -1 if the maximum
     * height was set in pixels instead using {@link #setMaxHeight(int) or #setDividerHeight(int)}.
     *
     * @see #setMaxLines(int)
     *
     * @attr ref android.R.styleable#TextView_maxLines
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMaxLines (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMaxLines();
		
		return -1;
	}
	
	/**
     * @return the maximum width of the TextView, in pixels or -1 if the maximum width
     * was set in ems instead (using {@link #setMaxEms(int)} or {@link #setEms(int)}).
     *
     * @see #setMaxWidth(int)
     * @see #setWidth(int)
     *
     * @attr ref android.R.styleable#TextView_maxWidth
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMaxWidth (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMaxWidth();
		
		return -1;
	}
	
	/**
     * @return the minimum width of the TextView, expressed in ems or -1 if the minimum width
     * was set in pixels instead (using {@link #setMinWidth(int)} or {@link #setWidth(int)}).
     *
     * @see #setMinEms(int)
     * @see #setEms(int)
     *
     * @attr ref android.R.styleable#TextView_minEms
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMinEms (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMinEms();
		
		return -1;
	}
	
	/**
     * @return the minimum height of this TextView expressed in pixels, or -1 if the minimum
     * height was set in number of lines instead using {@link #setMinLines(int) or #setLines(int)}.
     *
     * @see #setMinHeight(int)
     *
     * @attr ref android.R.styleable#TextView_minHeight
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMinHeight (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMinHeight();
		
		return -1;
	}
	
	/**
     * @return the minimum number of lines displayed in this TextView, or -1 if the minimum
     * height was set in pixels instead using {@link #setMinHeight(int) or #setDividerHeight(int)}.
     *
     * @see #setMinLines(int)
     *
     * @attr ref android.R.styleable#TextView_minLines
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMinLines (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMinLines();
		
		return -1;
	}
	
	/**
     * @return the minimum width of the TextView, in pixels or -1 if the minimum width
     * was set in ems instead (using {@link #setMinEms(int)} or {@link #setEms(int)}).
     *
     * @see #setMinWidth(int)
     * @see #setWidth(int)
     *
     * @attr ref android.R.styleable#TextView_minWidth
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getMinWidth (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getMinWidth();
		
		return -1;
	}
	
	/**
     * @return the movement method being used for this TextView.
     * This will frequently be null for non-EditText TextViews.
     */
	public final MovementMethod getMovementMethod (){
		return mInputView.getMovementMethod();
	}
	
	/**
     * Get the character offset closest to the specified absolute position. A typical use case is to
     * pass the result of {@link android.view.MotionEvent#getX()} and {@link android.view.MotionEvent#getY()} to this method.
     *
     * @param x The horizontal absolute position of a point on screen
     * @param y The vertical absolute position of a point on screen
     * @return the character offset for the character whose position is closest to the specified
     *  position. Returns -1 if there is no layout.
     */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public int getOffsetForPosition (float x, float y){
        if (getLayout() == null) return -1;
        final int line = getLineAtCoordinate(y);
        final int offset = getOffsetAtCoordinate(line, x);
        return offset;
	}

    protected float convertToLocalHorizontalCoordinate(float x) {
        x -= getTotalPaddingLeft();
        // Clamp the position to inside of the view.
        x = Math.max(0.0f, x);
        x = Math.min(getWidth() - getTotalPaddingRight() - 1, x);
        x += getScrollX();
        return x;
    }

    protected int getLineAtCoordinate(float y) {
        y -= getTotalPaddingTop();
        // Clamp the position to inside of the view.
        y = Math.max(0.0f, y);
        y = Math.min(getHeight() - getTotalPaddingBottom() - 1, y);
        y += getScrollY();
        return getLayout().getLineForVertical((int) y);
    }

    protected int getOffsetAtCoordinate(int line, float x) {
        x = convertToLocalHorizontalCoordinate(x);
        return getLayout().getOffsetForHorizontal(line, x);
    }
	
	/**
     * @return the base paint used for the text.  Please use this only to
     * consult the Paint's properties and not to change them.
     */
	public TextPaint getPaint (){
		return mInputView.getPaint();
	}
	
	/**
     * @return the flags on the Paint being used to display the text.
     * @see android.graphics.Paint#getFlags
     */
	public int getPaintFlags (){
		return mInputView.getPaintFlags();
	}
	
	/**
     * Get the private type of the content.
     *
     * @see #setPrivateImeOptions(String)
     * @see android.view.inputmethod.EditorInfo#privateImeOptions
     */
	public String getPrivateImeOptions (){
		return mInputView.getPrivateImeOptions();
	}
	
	/**
     * Convenience for {@link android.text.Selection#getSelectionEnd}.
     */
	public int getSelectionEnd (){
		return mInputView.getSelectionEnd();
	}
	
	/**
     * Convenience for {@link android.text.Selection#getSelectionStart}.
     */
	public int getSelectionStart (){
		return mInputView.getSelectionStart();
	}
	
	/**
     * @return the color of the shadow layer
     *
     * @see #setShadowLayer(float, float, float, int)
     *
     * @attr ref android.R.styleable#TextView_shadowColor
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public int getShadowColor (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getShadowColor();
		
		return 0;
	}
	
	/**
     * @return the horizontal offset of the shadow layer
     *
     * @see #setShadowLayer(float, float, float, int)
     *
     * @attr ref android.R.styleable#TextView_shadowDx
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public float getShadowDx (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getShadowDx();
		
		return 0;
	}
	
	/**
     * @return the vertical offset of the shadow layer
     *
     * @see #setShadowLayer(float, float, float, int)
     *
     * @attr ref android.R.styleable#TextView_shadowDy
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public float getShadowDy (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getShadowDy();
		
		return 0;
	}
	
	/**
     * Gets the radius of the shadow layer.
     *
     * @return the radius of the shadow layer. If 0, the shadow layer is not visible
     *
     * @see #setShadowLayer(float, float, float, int)
     *
     * @attr ref android.R.styleable#TextView_shadowRadius
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public float getShadowRadius (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
			return mInputView.getShadowRadius();
		
		return 0;
	}
	
	 /**
     * Returns whether the soft input method will be made visible when this
     * TextView gets focused. The default is true.
     */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public final boolean getShowSoftInputOnFocus (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			return mInputView.getShowSoftInputOnFocus();
		return true;
	}
	
	/**
     * Gets the text colors for the different states (normal, selected, focused) of the TextView.
     *
     * @see #setTextColor(android.content.res.ColorStateList)
     * @see #setTextColor(int)
     *
     * @attr ref android.R.styleable#TextView_textColor
     */
	public final ColorStateList getTextColors (){
		return mInputView.getTextColors();
	}
	
	/**
     * Get the default {@link java.util.Locale} of the text in this TextView.
     * @return the default {@link java.util.Locale} of the text in this TextView.
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public Locale getTextLocale (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return mInputView.getTextLocale();
		
		return Locale.getDefault();
	}
	
	/**
     * @return the extent by which text is currently being stretched
     * horizontally.  This will usually be 1.
     */
	public float getTextScaleX (){
		return mInputView.getTextScaleX();
	}
	
	/**
     * @return the size (in pixels) of the default text size in this TextView.
     */
	public float getTextSize (){
		return mInputView.getTextSize();
	}
	
	/**
     * Returns the total bottom padding of the view, including the bottom
     * Drawable if any, the extra space to keep more than maxLines
     * from showing, and the vertical offset for gravity, if any.
     */
	public int getTotalPaddingBottom (){
		return getPaddingBottom() + mInputView.getTotalPaddingBottom() + (mSupportMode != SUPPORT_MODE_NONE ? mSupportView.getHeight() : 0);
	}
	
	/**
     * Returns the total end padding of the view, including the end
     * Drawable if any.
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public int getTotalPaddingEnd (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return getPaddingEnd() + mInputView.getTotalPaddingEnd();
		
		return getTotalPaddingRight();
	}
	
	/**
     * Returns the total left padding of the view, including the left
     * Drawable if any.
     */
	public int getTotalPaddingLeft (){
		return getPaddingLeft() + mInputView.getTotalPaddingLeft();
	}
	
	/**
     * Returns the total right padding of the view, including the right
     * Drawable if any.
     */
	public int getTotalPaddingRight (){
		return getPaddingRight() + mInputView.getTotalPaddingRight();
	}
	
	/**
     * Returns the total start padding of the view, including the start
     * Drawable if any.
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public int getTotalPaddingStart (){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			return getPaddingStart() + mInputView.getTotalPaddingStart();
		
		return getTotalPaddingLeft();
	}
	
	 /**
     * Returns the total top padding of the view, including the top
     * Drawable if any, the extra space to keep more than maxLines
     * from showing, and the vertical offset for gravity, if any.
     */
	public int getTotalPaddingTop (){
		return getPaddingTop() + mInputView.getTotalPaddingTop() + (mLabelEnable ? mLabelView.getHeight() : 0);
	}
	
	/**
     * @return the current transformation method for this TextView.
     * This will frequently be null except for single-line and password
     * fields.
     *
     * @attr ref android.R.styleable#TextView_password
     * @attr ref android.R.styleable#TextView_singleLine
     */
	public final TransformationMethod getTransformationMethod (){
		return mInputView.getTransformationMethod();
	}
	
	/**
     * @return the current typeface and style in which the text is being
     * displayed.
     *
     * @see #setTypeface(android.graphics.Typeface)
     *
     * @attr ref android.R.styleable#TextView_fontFamily
     * @attr ref android.R.styleable#TextView_typeface
     * @attr ref android.R.styleable#TextView_textStyle
     */
	public Typeface getTypeface (){
		return mInputView.getTypeface();
	}
	
	/**
     * Returns the list of URLSpans attached to the text
     * (by {@link android.text.util.Linkify} or otherwise) if any.  You can call
     * {@link android.text.style.URLSpan#getURL} on them to find where they link to
     * or use {@link android.text.Spanned#getSpanStart} and {@link android.text.Spanned#getSpanEnd}
     * to find the region of the text they are attached to.
     */
	public URLSpan[] getUrls (){
		return mInputView.getUrls();
	}
	
	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean hasOverlappingRendering (){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && mInputView.hasOverlappingRendering();
    }
	
	/**
     * Return true iff there is a selection inside this text view.
     */
	public boolean hasSelection (){
		return mInputView.hasSelection();
	}
	
	/**
     * @return whether or not the cursor is visible (assuming this TextView is editable)
     *
     * @see #setCursorVisible(boolean)
     *
     * @attr ref android.R.styleable#TextView_cursorVisible
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public boolean isCursorVisible (){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN || mInputView.isCursorVisible();
    }
	
	/**
     * Returns whether this text view is a current input method target.  The
     * default implementation just checks with {@link android.view.inputmethod.InputMethodManager}.
     */
	public boolean isInputMethodTarget (){
		return mInputView.isInputMethodTarget();		
	}
	
	/**
     * Return whether or not suggestions are enabled on this TextView. The suggestions are generated
     * by the IME or by the spell checker as the user types. This is done by adding
     * {@link android.text.style.SuggestionSpan}s to the text.
     *
     * When suggestions are enabled (default), this list of suggestions will be displayed when the
     * user asks for them on these parts of the text. This value depends on the inputType of this
     * TextView.
     *
     * The class of the input type must be {@link android.text.InputType#TYPE_CLASS_TEXT}.
     *
     * In addition, the type variation must be one of
     * {@link android.text.InputType#TYPE_TEXT_VARIATION_NORMAL},
     * {@link android.text.InputType#TYPE_TEXT_VARIATION_EMAIL_SUBJECT},
     * {@link android.text.InputType#TYPE_TEXT_VARIATION_LONG_MESSAGE},
     * {@link android.text.InputType#TYPE_TEXT_VARIATION_SHORT_MESSAGE} or
     * {@link android.text.InputType#TYPE_TEXT_VARIATION_WEB_EDIT_TEXT}.
     *
     * And finally, the {@link android.text.InputType#TYPE_TEXT_FLAG_NO_SUGGESTIONS} flag must <i>not</i> be set.
     *
     * @return true if the suggestions popup window is enabled, based on the inputType.
     */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public boolean isSuggestionsEnabled (){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH && mInputView.isSuggestionsEnabled();
    }
	
	/**
    *
    * Returns the state of the {@code textIsSelectable} flag (See
    * {@link #setTextIsSelectable setTextIsSelectable()}). Although you have to set this flag
    * to allow users to select and copy text in a non-editable TextView, the content of an
    * {@link EditText} can always be selected, independently of the value of this flag.
    * <p>
    *
    * @return True if the text displayed in this TextView can be selected by the user.
    *
    * @attr ref android.R.styleable#TextView_textIsSelectable
    */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public boolean isTextSelectable (){
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB || mInputView.isTextSelectable();
    }
	
	/**
     * Returns the length, in characters, of the text managed by this TextView
     */
	public int length (){
		return mInputView.length();
	}
	
	/**
     * Move the cursor, if needed, so that it is at an offset that is visible
     * to the user.  This will not move the cursor if it represents more than
     * one character (a selection range).  This will only work if the
     * TextView contains spannable text; otherwise it will do nothing.
     *
     * @return True if the cursor was actually moved, false otherwise.
     */
	public boolean moveCursorToVisibleOffset (){
		return mInputView.moveCursorToVisibleOffset();
	}

	/**
     * Called by the framework in response to a text completion from
     * the current input method, provided by it calling
     * {@link android.view.inputmethod.InputConnection#commitCompletion
     * InputConnection.commitCompletion()}.  The default implementation does
     * nothing; text views that are supporting auto-completion should override
     * this to do their desired behavior.
     *
     * @param text The auto complete text the user has selected.
     */
	public void onCommitCompletion (CompletionInfo text){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            ((InternalEditText)mInputView).superOnCommitCompletion(text);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            ((InternalAutoCompleteTextView)mInputView).superOnCommitCompletion(text);
        else
            ((InternalMultiAutoCompleteTextView)mInputView).superOnCommitCompletion(text);
	}
	
	/**
     * Called by the framework in response to a text auto-correction (such as fixing a typo using a
     * a dictionnary) from the current input method, provided by it calling
     * {@link android.view.inputmethod.InputConnection#commitCorrection} InputConnection.commitCorrection()}. The default
     * implementation flashes the background of the corrected word to provide feedback to the user.
     *
     * @param info The auto correct info about the text that was corrected.
     */
	public void onCommitCorrection (CorrectionInfo info){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            ((InternalEditText)mInputView).superOnCommitCorrection(info);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            ((InternalAutoCompleteTextView)mInputView).superOnCommitCorrection(info);
        else
            ((InternalMultiAutoCompleteTextView)mInputView).superOnCommitCorrection(info);
	}
	
	@Override
	public InputConnection onCreateInputConnection (EditorInfo outAttrs){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return ((InternalEditText)mInputView).superOnCreateInputConnection(outAttrs);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            return ((InternalAutoCompleteTextView)mInputView).superOnCreateInputConnection(outAttrs);
        else
            return ((InternalMultiAutoCompleteTextView)mInputView).superOnCreateInputConnection(outAttrs);
	}
	
	/**
     * Called when an attached input method calls
     * {@link android.view.inputmethod.InputConnection#performEditorAction(int)
     * InputConnection.performEditorAction()}
     * for this text view.  The default implementation will call your action
     * listener supplied to {@link #setOnEditorActionListener}, or perform
     * a standard operation for {@link android.view.inputmethod.EditorInfo#IME_ACTION_NEXT
     * EditorInfo.IME_ACTION_NEXT}, {@link android.view.inputmethod.EditorInfo#IME_ACTION_PREVIOUS
     * EditorInfo.IME_ACTION_PREVIOUS}, or {@link android.view.inputmethod.EditorInfo#IME_ACTION_DONE
     * EditorInfo.IME_ACTION_DONE}.
     *
     * <p>For backwards compatibility, if no IME options have been set and the
     * text view would not normally advance focus on enter, then
     * the NEXT and DONE actions received here will be turned into an enter
     * key down/up pair to go through the normal key handling.
     *
     * @param actionCode The code of the action being performed.
     *
     * @see #setOnEditorActionListener
     */
	public void onEditorAction (int actionCode){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            ((InternalEditText)mInputView).superOnEditorAction(actionCode);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            ((InternalAutoCompleteTextView)mInputView).superOnEditorAction(actionCode);
        else
            ((InternalMultiAutoCompleteTextView)mInputView).superOnEditorAction(actionCode);
	}
	
	@Override
	public boolean onKeyDown (int keyCode, KeyEvent event){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return ((InternalEditText)mInputView).superOnKeyDown(keyCode, event);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            return ((InternalAutoCompleteTextView)mInputView).superOnKeyDown(keyCode, event);
        else
            return ((InternalMultiAutoCompleteTextView)mInputView).superOnKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyMultiple (int keyCode, int repeatCount, KeyEvent event){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return ((InternalEditText)mInputView).superOnKeyMultiple(keyCode, repeatCount, event);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            return ((InternalAutoCompleteTextView)mInputView).superOnKeyMultiple(keyCode, repeatCount, event);
        else
            return ((InternalMultiAutoCompleteTextView)mInputView).superOnKeyMultiple(keyCode, repeatCount, event);
	}
	
	@Override
	public boolean onKeyPreIme (int keyCode, KeyEvent event){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return ((InternalEditText)mInputView).superOnKeyPreIme(keyCode, event);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            return ((InternalAutoCompleteTextView)mInputView).superOnKeyPreIme(keyCode, event);
        else
            return ((InternalMultiAutoCompleteTextView)mInputView).superOnKeyPreIme(keyCode, event);
	}
	
	@Override
	public boolean onKeyShortcut (int keyCode, KeyEvent event){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return ((InternalEditText)mInputView).superOnKeyShortcut(keyCode, event);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            return ((InternalAutoCompleteTextView)mInputView).superOnKeyShortcut(keyCode, event);
        else
            return ((InternalMultiAutoCompleteTextView)mInputView).superOnKeyShortcut(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp (int keyCode, KeyEvent event){
        if(mAutoCompleteMode == AUTOCOMPLETE_MODE_NONE)
            return ((InternalEditText)mInputView).superOnKeyUp(keyCode, event);
        else if(mAutoCompleteMode == AUTOCOMPLETE_MODE_SINGLE)
            return ((InternalAutoCompleteTextView)mInputView).superOnKeyUp(keyCode, event);
        else
            return ((InternalMultiAutoCompleteTextView)mInputView).superOnKeyUp(keyCode, event);
	}

    public void setOnSelectionChangedListener(TextView.OnSelectionChangedListener listener){
        mOnSelectionChangedListener = listener;
    }

    /**
     * This method is called when the selection has changed, in case any
     * subclasses would like to know.
     *
     * @param selStart The new selection start location.
     * @param selEnd The new selection end location.
     */
    protected void onSelectionChanged(int selStart, int selEnd) {
        if(mInputView == null)
            return;

        if(mInputView instanceof InternalEditText)
            ((InternalEditText)mInputView).superOnSelectionChanged(selStart, selEnd);
        else if(mInputView instanceof InternalAutoCompleteTextView)
            ((InternalAutoCompleteTextView)mInputView).superOnSelectionChanged(selStart, selEnd);
        else
            ((InternalMultiAutoCompleteTextView)mInputView).superOnSelectionChanged(selStart, selEnd);

        if(mOnSelectionChangedListener != null)
            mOnSelectionChangedListener.onSelectionChanged(this, selStart, selEnd);
    }
	
	/**
     * Removes the specified TextWatcher from the list of those whose
     * methods are called
     * whenever this TextView's text changes.
     */
	public void removeTextChangedListener (TextWatcher watcher){
		mInputView.removeTextChangedListener(watcher);
	}
	
	/**
     * Sets the properties of this field to transform input to ALL CAPS
     * display. This may use a "small caps" formatting if available.
     * This setting will be ignored if this field is editable or selectable.
     *
     * This call replaces the current transformation method. Disabling this
     * will not necessarily restore the previous behavior from before this
     * was enabled.
     *
     * @see #setTransformationMethod(android.text.method.TransformationMethod)
     * @attr ref android.R.styleable#TextView_textAllCaps
     */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void setAllCaps (boolean allCaps){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			mInputView.setAllCaps(allCaps);
	}
	
	/**
     * Sets the autolink mask of the text.  See {@link
     * android.text.util.Linkify#ALL Linkify.ALL} and peers for
     * possible values.
     *
     * @attr ref android.R.styleable#TextView_autoLink
     */
	public final void setAutoLinkMask (int mask){
		mInputView.setAutoLinkMask(mask);
	}
	
	/**
     * Sets the size of the padding between the compound drawables and
     * the text.
     *
     * @attr ref android.R.styleable#TextView_drawablePadding
     */
	public void setCompoundDrawablePadding (int pad){
		mInputView.setCompoundDrawablePadding(pad);
        if(mDividerCompoundPadding) {
            mDivider.setPadding(mInputView.getTotalPaddingLeft(), mInputView.getTotalPaddingRight());
            if(mLabelEnable)
                mLabelView.setPadding(mDivider.getPaddingLeft(), mLabelView.getPaddingTop(), mDivider.getPaddingRight(), mLabelView.getPaddingBottom());
            if(mSupportMode != SUPPORT_MODE_NONE)
                mSupportView.setPadding(mDivider.getPaddingLeft(), mSupportView.getPaddingTop(), mDivider.getPaddingRight(), mSupportView.getPaddingBottom());
        }
	}
	
	/**
     * Sets the Drawables (if any) to appear to the left of, above, to the
     * right of, and below the text. Use {@code null} if you do not want a
     * Drawable there. The Drawables must already have had
     * {@link android.graphics.drawable.Drawable#setBounds} called.
     * <p>
     * Calling this method will overwrite any Drawables previously set using
     * {@link #setCompoundDrawablesRelative} or related methods.
     *
     * @attr ref android.R.styleable#TextView_drawableLeft
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableRight
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
	public void setCompoundDrawables (Drawable left, Drawable top, Drawable right, Drawable bottom){
		mInputView.setCompoundDrawables(left, top, right, bottom);
        if(mDividerCompoundPadding) {
            mDivider.setPadding(mInputView.getTotalPaddingLeft(), mInputView.getTotalPaddingRight());
            if(mLabelEnable)
                mLabelView.setPadding(mDivider.getPaddingLeft(), mLabelView.getPaddingTop(), mDivider.getPaddingRight(), mLabelView.getPaddingBottom());
            if(mSupportMode != SUPPORT_MODE_NONE)
                mSupportView.setPadding(mDivider.getPaddingLeft(), mSupportView.getPaddingTop(), mDivider.getPaddingRight(), mSupportView.getPaddingBottom());
        }
	}
	
	/**
     * Sets the Drawables (if any) to appear to the start of, above, to the end
     * of, and below the text. Use {@code null} if you do not want a Drawable
     * there. The Drawables must already have had {@link android.graphics.drawable.Drawable#setBounds}
     * called.
     * <p>
     * Calling this method will overwrite any Drawables previously set using
     * {@link #setCompoundDrawables} or related methods.
     *
     * @attr ref android.R.styleable#TextView_drawableStart
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableEnd
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void setCompoundDrawablesRelative (Drawable start, Drawable top, Drawable end, Drawable bottom){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			mInputView.setCompoundDrawablesRelative(start, top, end, bottom);
		else
			mInputView.setCompoundDrawables(start, top, end, bottom);
	}
	
	/**
     * Sets the Drawables (if any) to appear to the start of, above, to the end
     * of, and below the text. Use {@code null} if you do not want a Drawable
     * there. The Drawables' bounds will be set to their intrinsic bounds.
     * <p>
     * Calling this method will overwrite any Drawables previously set using
     * {@link #setCompoundDrawables} or related methods.
     *
     * @attr ref android.R.styleable#TextView_drawableStart
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableEnd
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void setCompoundDrawablesRelativeWithIntrinsicBounds (Drawable start, Drawable top, Drawable end, Drawable bottom){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			mInputView.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
		else
			mInputView.setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom);
	}
	
	/**
     * Sets the Drawables (if any) to appear to the start of, above, to the end
     * of, and below the text. Use 0 if you do not want a Drawable there. The
     * Drawables' bounds will be set to their intrinsic bounds.
     * <p>
     * Calling this method will overwrite any Drawables previously set using
     * {@link #setCompoundDrawables} or related methods.
     *
     * @param start Resource identifier of the start Drawable.
     * @param top Resource identifier of the top Drawable.
     * @param end Resource identifier of the end Drawable.
     * @param bottom Resource identifier of the bottom Drawable.
     *
     * @attr ref android.R.styleable#TextView_drawableStart
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableEnd
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void setCompoundDrawablesRelativeWithIntrinsicBounds (int start, int top, int end, int bottom){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			mInputView.setCompoundDrawablesRelativeWithIntrinsicBounds(start, top, end, bottom);
		else
			mInputView.setCompoundDrawablesWithIntrinsicBounds(start, top, end, bottom);
	}
	
	/**
     * Sets the Drawables (if any) to appear to the left of, above, to the
     * right of, and below the text. Use {@code null} if you do not want a
     * Drawable there. The Drawables' bounds will be set to their intrinsic
     * bounds.
     * <p>
     * Calling this method will overwrite any Drawables previously set using
     * {@link #setCompoundDrawablesRelative} or related methods.
     *
     * @attr ref android.R.styleable#TextView_drawableLeft
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableRight
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
	public void setCompoundDrawablesWithIntrinsicBounds (Drawable left, Drawable top, Drawable right, Drawable bottom){
		mInputView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
	}
	
	/**
     * Sets the Drawables (if any) to appear to the left of, above, to the
     * right of, and below the text. Use 0 if you do not want a Drawable there.
     * The Drawables' bounds will be set to their intrinsic bounds.
     * <p>
     * Calling this method will overwrite any Drawables previously set using
     * {@link #setCompoundDrawablesRelative} or related methods.
     *
     * @param left Resource identifier of the left Drawable.
     * @param top Resource identifier of the top Drawable.
     * @param right Resource identifier of the right Drawable.
     * @param bottom Resource identifier of the bottom Drawable.
     *
     * @attr ref android.R.styleable#TextView_drawableLeft
     * @attr ref android.R.styleable#TextView_drawableTop
     * @attr ref android.R.styleable#TextView_drawableRight
     * @attr ref android.R.styleable#TextView_drawableBottom
     */
	public void setCompoundDrawablesWithIntrinsicBounds (int left, int top, int right, int bottom){
		mInputView.setCompoundDrawablesWithIntrinsicBounds(left, top, right, bottom);
	}
	
	/**
     * Set whether the cursor is visible. The default is true. Note that this property only
     * makes sense for editable TextView.
     *
     * @see #isCursorVisible()
     *
     * @attr ref android.R.styleable#TextView_cursorVisible
     */
	public void setCursorVisible (boolean visible){
		mInputView.setCursorVisible(visible);
	}
	
	/**
     * If provided, this ActionMode.Callback will be used to create the ActionMode when text
     * selection is initiated in this View.
     *
     * The standard implementation populates the menu with a subset of Select All, Cut, Copy and
     * Paste actions, depending on what this View supports.
     *
     * A custom implementation can add new entries in the default menu in its
     * {@link android.view.ActionMode.Callback#onPrepareActionMode(android.view.ActionMode, android.view.Menu)} method. The
     * default actions can also be removed from the menu using {@link android.view.Menu#removeItem(int)} and
     * passing {@link android.R.id#selectAll}, {@link android.R.id#cut}, {@link android.R.id#copy}
     * or {@link android.R.id#paste} ids as parameters.
     *
     * Returning false from
     * {@link android.view.ActionMode.Callback#onCreateActionMode(android.view.ActionMode, android.view.Menu)} will prevent
     * the action mode from being started.
     *
     * Action click events should be handled by the custom implementation of
     * {@link android.view.ActionMode.Callback#onActionItemClicked(android.view.ActionMode, android.view.MenuItem)}.
     *
     * Note that text selection mode is not started when a TextView receives focus and the
     * {@link android.R.attr#selectAllOnFocus} flag has been set. The content is highlighted in
     * that case, to allow for quick replacement.
     */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setCustomSelectionActionModeCallback (ActionMode.Callback actionModeCallback){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mInputView.setCustomSelectionActionModeCallback(actionModeCallback);
	}
	
	/**
     * Sets the Factory used to create new Editables.
     */
	public final void setEditableFactory (Editable.Factory factory){
		mInputView.setEditableFactory(factory);
	}
	
	/**
     * Set the TextView's elegant height metrics flag. This setting selects font
     * variants that have not been compacted to fit Latin-based vertical
     * metrics, and also increases top and bottom bounds to provide more space.
     *
     * @param elegant set the paint's elegant metrics flag.
     *
     * @attr ref android.R.styleable#TextView_elegantTextHeight
     */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void setElegantTextHeight (boolean elegant){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			mInputView.setElegantTextHeight(elegant);
	}
	
	/**
     * Makes the TextView exactly this many ems wide
     *
     * @see #setMaxEms(int)
     * @see #setMinEms(int)
     * @see #getMinEms()
     * @see #getMaxEms()
     *
     * @attr ref android.R.styleable#TextView_ems
     */
	public void setEms (int ems){
		mInputView.setEms(ems);
	}
	
	/**
     * Apply to this text view the given extracted text, as previously
     * returned by {@link #extractText(android.view.inputmethod.ExtractedTextRequest, android.view.inputmethod.ExtractedText)}.
     */
	public void setExtractedText (ExtractedText text){
		mInputView.setExtractedText(text);
	}
	
	/**
     * Sets the list of input filters that will be used if the buffer is
     * Editable. Has no effect otherwise.
     *
     * @attr ref android.R.styleable#TextView_maxLength
     */
	public void setFilters (InputFilter[] filters){
		mInputView.setFilters(filters);
	}
	
	/**
     * Sets font feature settings.  The format is the same as the CSS
     * font-feature-settings attribute:
     * http://dev.w3.org/csswg/css-fonts/#propdef-font-feature-settings
     *
     * @param fontFeatureSettings font feature settings represented as CSS compatible string
     * @see #getFontFeatureSettings()
     * @see android.graphics.Paint#getFontFeatureSettings
     *
     * @attr ref android.R.styleable#TextView_fontFeatureSettings
     */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void setFontFeatureSettings (String fontFeatureSettings){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			mInputView.setFontFeatureSettings(fontFeatureSettings);
	}
	
	/**
     * Control whether this text view saves its entire text contents when
     * freezing to an icicle, in addition to dynamic state such as cursor
     * position.  By default this is false, not saving the text.  Set to true
     * if the text in the text view is not being saved somewhere else in
     * persistent storage (such as in a content provider) so that if the
     * view is later thawed the user will not lose their data.
     *
     * @param freezesText Controls whether a frozen icicle should include the
     * entire text data: true to include it, false to not.
     *
     * @attr ref android.R.styleable#TextView_freezesText
     */
	public void setFreezesText (boolean freezesText){
		mInputView.setFreezesText(freezesText);
	}
	
	/**
     * Sets the horizontal alignment of the text and the
     * vertical gravity that will be used when there is extra space
     * in the TextView beyond what is required for the text itself.
     *
     * @see android.view.Gravity
     * @attr ref android.R.styleable#TextView_gravity
     */
	public void setGravity (int gravity){
		mInputView.setGravity(gravity);
	}
	
	/**
     * Sets the color used to display the selection highlight.
     *
     * @attr ref android.R.styleable#TextView_textColorHighlight
     */
	public void setHighlightColor (int color){
		mInputView.setHighlightColor(color);
	}
	
	/**
     * Sets the text to be displayed when the text of the TextView is empty.
     * Null means to use the normal empty text. The hint does not currently
     * participate in determining the size of the view.
     *
     * @attr ref android.R.styleable#TextView_hint
     */
	public final void setHint (CharSequence hint){
		mInputView.setHint(hint);
		if(mLabelView != null)
			mLabelView.setText(hint);
	}
	
	/**
     * Sets the text to be displayed when the text of the TextView is empty,
     * from a resource.
     *
     * @attr ref android.R.styleable#TextView_hint
     */
	public final void setHint (int resid){
		mInputView.setHint(resid);
		if(mLabelView != null)
			mLabelView.setText(resid);
	}
	
	/**
     * Sets the color of the hint text.
     *
     * @see #getHintTextColors()
     * @see #setHintTextColor(int)
     * @see #setTextColor(android.content.res.ColorStateList)
     * @see #setLinkTextColor(android.content.res.ColorStateList)
     *
     * @attr ref android.R.styleable#TextView_textColorHint
     */
	public final void setHintTextColor (ColorStateList colors){
		mInputView.setHintTextColor(colors);
	}
	
	/**
     * Sets the color of the hint text for all the states (disabled, focussed, selected...) of this
     * TextView.
     *
     * @see #setHintTextColor(android.content.res.ColorStateList)
     * @see #getHintTextColors()
     * @see #setTextColor(int)
     *
     * @attr ref android.R.styleable#TextView_textColorHint
     */
	public final void setHintTextColor (int color){
		mInputView.setHintTextColor(color);
	}
	
	/**
     * Sets whether the text should be allowed to be wider than the
     * View is.  If false, it will be wrapped to the width of the View.
     *
     * @attr ref android.R.styleable#TextView_scrollHorizontally
     */
	public void setHorizontallyScrolling (boolean whether){
		mInputView.setHorizontallyScrolling(whether);
	}
	
	/**
     * Change the custom IME action associated with the text view, which
     * will be reported to an IME with {@link android.view.inputmethod.EditorInfo#actionLabel}
     * and {@link android.view.inputmethod.EditorInfo#actionId} when it has focus.
     * @see #getImeActionLabel
     * @see #getImeActionId
     * @see android.view.inputmethod.EditorInfo
     * @attr ref android.R.styleable#TextView_imeActionLabel
     * @attr ref android.R.styleable#TextView_imeActionId
     */
	public void setImeActionLabel (CharSequence label, int actionId){
		mInputView.setImeActionLabel(label, actionId);
	}
	
	/**
     * Change the editor type integer associated with the text view, which
     * will be reported to an IME with {@link android.view.inputmethod.EditorInfo#imeOptions} when it
     * has focus.
     * @see #getImeOptions
     * @see android.view.inputmethod.EditorInfo
     * @attr ref android.R.styleable#TextView_imeOptions
     */
	public void setImeOptions (int imeOptions){
		mInputView.setImeOptions(imeOptions);
	}
	
	/**
     * Set whether the TextView includes extra top and bottom padding to make
     * room for accents that go above the normal ascent and descent.
     * The default is true.
     *
     * @see #getIncludeFontPadding()
     *
     * @attr ref android.R.styleable#TextView_includeFontPadding
     */
	public void setIncludeFontPadding (boolean includepad){
		mInputView.setIncludeFontPadding(includepad);
	}
	
	/**
     * Set the extra input data of the text, which is the
     * {@link android.view.inputmethod.EditorInfo#extras TextBoxAttribute.extras}
     * Bundle that will be filled in when creating an input connection.  The
     * given integer is the resource ID of an XML resource holding an
     * {@link android.R.styleable#InputExtras &lt;input-extras&gt;} XML tree.
     *
     * @see #getInputExtras(boolean)
     * @see android.view.inputmethod.EditorInfo#extras
     * @attr ref android.R.styleable#TextView_editorExtras
     */
	public void setInputExtras (int xmlResId) throws XmlPullParserException, IOException{
		mInputView.setInputExtras(xmlResId);
	}
	
	/**
     * Set the type of the content with a constant as defined for {@link android.view.inputmethod.EditorInfo#inputType}. This
     * will take care of changing the key listener, by calling {@link #setKeyListener(android.text.method.KeyListener)},
     * to match the given content type.  If the given content type is {@link android.view.inputmethod.EditorInfo#TYPE_NULL}
     * then a soft keyboard will not be displayed for this text view.
     *
     * Note that the maximum number of displayed lines (see {@link #setMaxLines(int)}) will be
     * modified if you change the {@link android.view.inputmethod.EditorInfo#TYPE_TEXT_FLAG_MULTI_LINE} flag of the input
     * type.
     *
     * @see #getInputType()
     * @see #setRawInputType(int)
     * @see android.text.InputType
     * @attr ref android.R.styleable#TextView_inputType
     */
	public void setInputType (int type){
		mInputView.setInputType(type);
	}
	
	/**
     * Sets the key listener to be used with this TextView.  This can be null
     * to disallow user input.  Note that this method has significant and
     * subtle interactions with soft keyboards and other input method:
     * see {@link android.text.method.KeyListener#getInputType() KeyListener.getContentType()}
     * for important details.  Calling this method will replace the current
     * content type of the text view with the content type returned by the
     * key listener.
     * <p>
     * Be warned that if you want a TextView with a key listener or movement
     * method not to be focusable, or if you want a TextView without a
     * key listener or movement method to be focusable, you must call
     * {@link #setFocusable} again after calling this to get the focusability
     * back the way you want it.
     *
     * @attr ref android.R.styleable#TextView_numeric
     * @attr ref android.R.styleable#TextView_digits
     * @attr ref android.R.styleable#TextView_phoneNumber
     * @attr ref android.R.styleable#TextView_inputMethod
     * @attr ref android.R.styleable#TextView_capitalize
     * @attr ref android.R.styleable#TextView_autoText
     */
	public void setKeyListener (KeyListener input){
		mInputView.setKeyListener(input);
	}
	
	/**
     * Sets text letter-spacing.  The value is in 'EM' units.  Typical values
     * for slight expansion will be around 0.05.  Negative values tighten text.
     *
     * @see #getLetterSpacing()
     * @see android.graphics.Paint#getLetterSpacing
     *
     * @attr ref android.R.styleable#TextView_letterSpacing
     */
	public void setLetterSpacing (float letterSpacing){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
		    mInputView.setLetterSpacing(letterSpacing);
	}
	
	/**
     * Sets line spacing for this TextView.  Each line will have its height
     * multiplied by <code>mult</code> and have <code>add</code> added to it.
     *
     * @attr ref android.R.styleable#TextView_lineSpacingExtra
     * @attr ref android.R.styleable#TextView_lineSpacingMultiplier
     */
	public void setLineSpacing (float add, float mult){
		mInputView.setLineSpacing(add, mult);
	}
	
	/**
     * Makes the TextView exactly this many lines tall.
     *
     * Note that setting this value overrides any other (minimum / maximum) number of lines or
     * height setting. A single line TextView will set this value to 1.
     *
     * @attr ref android.R.styleable#TextView_lines
     */
	public void setLines (int lines){
		mInputView.setLines(lines);
	}
	
	/**
     * Sets the color of links in the text.
     *
     * @see #setLinkTextColor(int)
     * @see #getLinkTextColors()
     * @see #setTextColor(android.content.res.ColorStateList)
     * @see #setHintTextColor(android.content.res.ColorStateList)
     *
     * @attr ref android.R.styleable#TextView_textColorLink
     */
	public final void setLinkTextColor (ColorStateList colors){
		mInputView.setLinkTextColor(colors);
	}
	
	 /**
     * Sets the color of links in the text.
     *
     * @see #setLinkTextColor(int)
     * @see #getLinkTextColors()
     * @see #setTextColor(android.content.res.ColorStateList)
     * @see #setHintTextColor(android.content.res.ColorStateList)
     *
     * @attr ref android.R.styleable#TextView_textColorLink
     */
	public final void setLinkTextColor (int color){
		mInputView.setLinkTextColor(color);
	}
	
	/**
     * Sets whether the movement method will automatically be set to
     * {@link android.text.method.LinkMovementMethod} if {@link #setAutoLinkMask} has been
     * set to nonzero and links are detected in {@link #setText}.
     * The default is true.
     *
     * @attr ref android.R.styleable#TextView_linksClickable
     */
	public final void setLinksClickable (boolean whether){
		mInputView.setLinksClickable(whether);
	}
	
	/**
     * Sets how many times to repeat the marquee animation. Only applied if the
     * TextView has marquee enabled. Set to -1 to repeat indefinitely.
     *
     * @see #getMarqueeRepeatLimit()
     *
     * @attr ref android.R.styleable#TextView_marqueeRepeatLimit
     */
	public void setMarqueeRepeatLimit (int marqueeLimit){
		mInputView.setMarqueeRepeatLimit(marqueeLimit);
	}
	
	/**
     * Makes the TextView at most this many ems wide
     *
     * @attr ref android.R.styleable#TextView_maxEms
     */
	public void setMaxEms (int maxems){
		mInputView.setMaxEms(maxems);
	}
	
	/**
     * Makes the TextView at most this many pixels tall.  This option is mutually exclusive with the
     * {@link #setMaxLines(int)} method.
     *
     * Setting this value overrides any other (maximum) number of lines setting.
     *
     * @attr ref android.R.styleable#TextView_maxHeight
     */
	public void setMaxHeight (int maxHeight){
		mInputView.setMaxHeight(maxHeight);
	}
	
	/**
     * Makes the TextView at most this many lines tall.
     *
     * Setting this value overrides any other (maximum) height setting.
     *
     * @attr ref android.R.styleable#TextView_maxLines
     */
	public void setMaxLines (int maxlines){
		mInputView.setMaxLines(maxlines);
	}
	
	/**
     * Makes the TextView at most this many pixels wide
     *
     * @attr ref android.R.styleable#TextView_maxWidth
     */
	public void setMaxWidth (int maxpixels){
		mInputView.setMaxWidth(maxpixels);
	}
	
	/**
     * Makes the TextView at least this many ems wide
     *
     * @attr ref android.R.styleable#TextView_minEms
     */
	public void setMinEms (int minems){
		mInputView.setMinEms(minems);
	}
	
	/**
     * Makes the TextView at least this many pixels tall.
     *
     * Setting this value overrides any other (minimum) number of lines setting.
     *
     * @attr ref android.R.styleable#TextView_minHeight
     */
	public void setMinHeight (int minHeight){
		mInputView.setMinHeight(minHeight);
	}
	
	/**
     * Makes the TextView at least this many lines tall.
     *
     * Setting this value overrides any other (minimum) height setting. A single line TextView will
     * set this value to 1.
     *
     * @see #getMinLines()
     *
     * @attr ref android.R.styleable#TextView_minLines
     */
	public void setMinLines (int minlines){
		mInputView.setMinLines(minlines);
	}
	
	/**
     * Makes the TextView at least this many pixels wide
     *
     * @attr ref android.R.styleable#TextView_minWidth
     */
	public void setMinWidth (int minpixels){
		mInputView.setMinWidth(minpixels);
	}
	
	/**
     * Sets the movement method (arrow key handler) to be used for
     * this TextView.  This can be null to disallow using the arrow keys
     * to move the cursor or scroll the view.
     * <p>
     * Be warned that if you want a TextView with a key listener or movement
     * method not to be focusable, or if you want a TextView without a
     * key listener or movement method to be focusable, you must call
     * {@link #setFocusable} again after calling this to get the focusability
     * back the way you want it.
     */
	public final void setMovementMethod (MovementMethod movement){
		mInputView.setMovementMethod(movement);
	}
	
	/**
     * Set a special listener to be called when an action is performed
     * on the text view.  This will be called when the enter key is pressed,
     * or when an action supplied to the IME is selected by the user.  Setting
     * this means that the normal hard key event will not insert a newline
     * into the text view, even if it is multi-line; holding down the ALT
     * modifier will, however, allow the user to insert a newline character.
     */
	public void setOnEditorActionListener (android.widget.TextView.OnEditorActionListener l){
		mInputView.setOnEditorActionListener(l);
	}
	
	/**
     * Register a callback to be invoked when a hardware key is pressed in this view.
     * Key presses in software input methods will generally not trigger the methods of
     * this listener.
     * @param l the key listener to attach to this view
     */
	@Override
    public void setOnKeyListener(OnKeyListener l) {
    	mInputView.setOnKeyListener(l);
    }
	
	/**
     * Register a callback to be invoked when focus of this view changed.
     *
     * @param l The callback that will run.
     */
	@Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
    	mInputView.setOnFocusChangeListener(l);
    }	
	
	/**
     * Directly change the content type integer of the text view, without
     * modifying any other state.
     * @see #setInputType(int)
     * @see android.text.InputType
     * @attr ref android.R.styleable#TextView_inputType
     */
	public void setRawInputType (int type){
		mInputView.setRawInputType(type);
	}
	
	public void setScroller (Scroller s){
		mInputView.setScroller(s);
	}
	
	/**
     * Set the TextView so that when it takes focus, all the text is
     * selected.
     *
     * @attr ref android.R.styleable#TextView_selectAllOnFocus
     */
	public void setSelectAllOnFocus (boolean selectAllOnFocus){
		mInputView.setSelectAllOnFocus(selectAllOnFocus);
	}
	
	@Override
	public void setSelected (boolean selected){
		mInputView.setSelected(selected);
	}
	
	 /**
     * Gives the text a shadow of the specified blur radius and color, the specified
     * distance from its drawn position.
     * <p>
     * The text shadow produced does not interact with the properties on view
     * that are responsible for real time shadows,
     * {@link android.view.View#getElevation() elevation} and
     * {@link android.view.View#getTranslationZ() translationZ}.
     *
     * @see android.graphics.Paint#setShadowLayer(float, float, float, int)
     *
     * @attr ref android.R.styleable#TextView_shadowColor
     * @attr ref android.R.styleable#TextView_shadowDx
     * @attr ref android.R.styleable#TextView_shadowDy
     * @attr ref android.R.styleable#TextView_shadowRadius
     */
	public void setShadowLayer (float radius, float dx, float dy, int color){
		mInputView.setShadowLayer(radius, dx, dy, color);
	}
	
	/**
     * Sets whether the soft input method will be made visible when this
     * TextView gets focused. The default is true.
     */
	public final void setShowSoftInputOnFocus (boolean show){
		mInputView.setShowSoftInputOnFocus(show);
	}
	
	/**
     * Sets the properties of this field (lines, horizontally scrolling,
     * transformation method) to be for a single-line input.
     *
     * @attr ref android.R.styleable#TextView_singleLine
     */
	public void setSingleLine (){
		mInputView.setSingleLine();
	}
	
	/**
     * Sets the Factory used to create new Spannables.
     */
	public final void setSpannableFactory (Spannable.Factory factory){
		mInputView.setSpannableFactory(factory);
	}
	
	public final void setText (int resid){
		mInputView.setText(resid);
	}
	
	public final void setText (char[] text, int start, int len){
		mInputView.setText(text, start, len);
	}
	
	public final void setText (int resid, android.widget.TextView.BufferType type){
		mInputView.setText(resid, type);
	}
	
	public final void setText (CharSequence text){
		mInputView.setText(text);
	}
	
	 /**
     * Sets the text color, size, style, hint color, and highlight color
     * from the specified TextAppearance resource.
     */
	public void setTextAppearance (Context context, int resid){
		mInputView.setTextAppearance(context, resid);
	}
	
	/**
     * Sets the text color.
     *
     * @see #setTextColor(int)
     * @see #getTextColors()
     * @see #setHintTextColor(android.content.res.ColorStateList)
     * @see #setLinkTextColor(android.content.res.ColorStateList)
     *
     * @attr ref android.R.styleable#TextView_textColor
     */
	public void setTextColor (ColorStateList colors){
		mInputView.setTextColor(colors);
	}
	
	/**
     * Sets the text color for all the states (normal, selected,
     * focused) to be this color.
     *
     * @see #setTextColor(android.content.res.ColorStateList)
     * @see #getTextColors()
     *
     * @attr ref android.R.styleable#TextView_textColor
     */
	public void setTextColor (int color){
		mInputView.setTextColor(color);
	}
	
	/**
     * Sets whether the content of this view is selectable by the user. The default is
     * {@code false}, meaning that the content is not selectable.
     * <p>
     * When you use a TextView to display a useful piece of information to the user (such as a
     * contact's address), make it selectable, so that the user can select and copy its
     * content. You can also use set the XML attribute
     * {@link android.R.styleable#TextView_textIsSelectable} to "true".
     * <p>
     * When you call this method to set the value of {@code textIsSelectable}, it sets
     * the flags {@code focusable}, {@code focusableInTouchMode}, {@code clickable},
     * and {@code longClickable} to the same value. These flags correspond to the attributes
     * {@link android.R.styleable#View_focusable android:focusable},
     * {@link android.R.styleable#View_focusableInTouchMode android:focusableInTouchMode},
     * {@link android.R.styleable#View_clickable android:clickable}, and
     * {@link android.R.styleable#View_longClickable android:longClickable}. To restore any of these
     * flags to a state you had set previously, call one or more of the following methods:
     * {@link #setFocusable(boolean) setFocusable()},
     * {@link #setFocusableInTouchMode(boolean) setFocusableInTouchMode()},
     * {@link #setClickable(boolean) setClickable()} or
     * {@link #setLongClickable(boolean) setLongClickable()}.
     *
     * @param selectable Whether the content of this TextView should be selectable.
     */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setTextIsSelectable (boolean selectable){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			mInputView.setTextIsSelectable(selectable);
	}
	
	/**
     * Like {@link #setText(CharSequence)},
     * except that the cursor position (if any) is retained in the new text.
     *
     * @param text The new text to place in the text view.
     *
     * @see #setText(CharSequence)
     */
	public final void setTextKeepState (CharSequence text){
		mInputView.setTextKeepState(text);
	}

	/**
     * Like {@link #setText(CharSequence, android.widget.TextView.BufferType)},
     * except that the cursor position (if any) is retained in the new text.
     *
     * @see #setText(CharSequence, android.widget.TextView.BufferType)
     */
	public final void setTextKeepState (CharSequence text, android.widget.TextView.BufferType type){
		mInputView.setTextKeepState(text, type);
	}
	
	/**
     * Set the default {@link java.util.Locale} of the text in this TextView to the given value. This value
     * is used to choose appropriate typefaces for ambiguous characters. Typically used for CJK
     * locales to disambiguate Hanzi/Kanji/Hanja characters.
     *
     * @param locale the {@link java.util.Locale} for drawing text, must not be null.
     *
     * @see android.graphics.Paint#setTextLocale
     */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void setTextLocale (Locale locale){
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
			mInputView.setTextLocale(locale);
	}
	
	/**
     * Sets the extent by which text should be stretched horizontally.
     *
     * @attr ref android.R.styleable#TextView_textScaleX
     */
	public void setTextScaleX (float size){
		mInputView.setTextScaleX(size);
	}
	
	/**
     * Set the default text size to the given value, interpreted as "scaled
     * pixel" units.  This size is adjusted based on the current density and
     * user font size preference.
     *
     * @param size The scaled pixel size.
     *
     * @attr ref android.R.styleable#TextView_textSize
     */
	public void setTextSize (float size){
		mInputView.setTextSize(size);
	}
	
	/**
     * Set the default text size to a given unit and value.  See {@link
     * android.util.TypedValue} for the possible dimension units.
     *
     * @param unit The desired dimension unit.
     * @param size The desired size in the given units.
     *
     * @attr ref android.R.styleable#TextView_textSize
     */
	public void setTextSize (int unit, float size){
		mInputView.setTextSize(unit, size);
	}
	
	/**
     * Sets the transformation that is applied to the text that this
     * TextView is displaying.
     *
     * @attr ref android.R.styleable#TextView_password
     * @attr ref android.R.styleable#TextView_singleLine
     */
	public final void setTransformationMethod (TransformationMethod method){
		mInputView.setTransformationMethod(method);
	}
	
	/**
     * Sets the typeface and style in which the text should be displayed,
     * and turns on the fake bold and italic bits in the Paint if the
     * Typeface that you provided does not have all the bits in the
     * style that you specified.
     *
     * @attr ref android.R.styleable#TextView_typeface
     * @attr ref android.R.styleable#TextView_textStyle
     */
	public void setTypeface (Typeface tf, int style){
		mInputView.setTypeface(tf, style);
	}
	
	/**
     * Sets the typeface and style in which the text should be displayed.
     * Note that not all Typeface families actually have bold and italic
     * variants, so you may need to use
     * {@link #setTypeface(android.graphics.Typeface, int)} to get the appearance
     * that you actually want.
     *
     * @see #getTypeface()
     *
     * @attr ref android.R.styleable#TextView_fontFamily
     * @attr ref android.R.styleable#TextView_typeface
     * @attr ref android.R.styleable#TextView_textStyle
     */
	public void setTypeface (Typeface tf){
		mInputView.setTypeface(tf);
	}

    /**
     * It would be better to rely on the input type for everything. A password inputType should have
     * a password transformation. We should hence use isPasswordInputType instead of this method.
     *
     * We should:
     * - Call setInputType in setKeyListener instead of changing the input type directly (which
     * would install the correct transformation).
     * - Refuse the installation of a non-password transformation in setTransformation if the input
     * type is password.
     *
     * However, this is like this for legacy reasons and we cannot break existing apps. This method
     * is useful since it matches what the user can see (obfuscated text or not).
     *
     * @return true if the current transformation method is of the password type.
     */
    private boolean hasPasswordTransformationMethod() {
        return getTransformationMethod() != null && getTransformationMethod() instanceof PasswordTransformationMethod;
    }

    public boolean canCut() {
        return !hasPasswordTransformationMethod() && getText().length() > 0 && hasSelection() && getKeyListener() != null;
    }

    public boolean canCopy() {
        return !hasPasswordTransformationMethod() && getText().length() > 0 && hasSelection();
    }

    public boolean canPaste() {
        return (getKeyListener() != null &&
                getSelectionStart() >= 0 &&
                getSelectionEnd() >= 0 &&
                ((ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE)).hasPrimaryClip());
    }

	/* Inner class */
	
	private class InputTextWatcher implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
        	int count = s.length();
        	setLabelVisible(count != 0, true);
            if(mSupportMode == SUPPORT_MODE_CHAR_COUNTER)
                updateCharCounter(count);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    }
	
	private class LabelView extends android.widget.TextView{

		public LabelView(Context context) {
			super(context);
		}
		
		@Override
		protected int[] onCreateDrawableState(int extraSpace) {
			return mInputView.getDrawableState();
		}	
		
	}
	
	private class InternalEditText extends android.widget.EditText{

		public InternalEditText(Context context) {
			super(context);
		}
		
		public InternalEditText(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public InternalEditText(Context context, AttributeSet attrs, int defStyleAttr) {
			super(context, attrs, defStyleAttr);
		}

		@Override
		public void refreshDrawableState() {
			super.refreshDrawableState();
			
			if(mLabelView != null)
				mLabelView.refreshDrawableState();
			
			if(mSupportView != null)
				mSupportView.refreshDrawableState();
		}

        @Override
        public void onCommitCompletion(CompletionInfo text) {
            EditText.this.onCommitCompletion(text);
        }

        @Override
        public void onCommitCorrection(CorrectionInfo info) {
            EditText.this.onCommitCorrection(info);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            return EditText.this.onCreateInputConnection(outAttrs);
        }

        @Override
        public void onEditorAction(int actionCode) {
            EditText.this.onEditorAction(actionCode);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return EditText.this.onKeyDown(keyCode, event);
        }

        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            return EditText.this.onKeyMultiple(keyCode, repeatCount, event);
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            return EditText.this.onKeyPreIme(keyCode, event);
        }

        @Override
        public boolean onKeyShortcut(int keyCode, KeyEvent event) {
            return EditText.this.onKeyShortcut(keyCode, event);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return EditText.this.onKeyUp(keyCode, event);
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            EditText.this.onSelectionChanged(selStart, selEnd);
        }

        void superOnCommitCompletion(CompletionInfo text) {
            super.onCommitCompletion(text);
        }

        void superOnCommitCorrection(CorrectionInfo info) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                super.onCommitCorrection(info);
        }

        InputConnection superOnCreateInputConnection(EditorInfo outAttrs) {
            return super.onCreateInputConnection(outAttrs);
        }

        void superOnEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
        }

        boolean superOnKeyDown(int keyCode, KeyEvent event) {
            return super.onKeyDown(keyCode, event);
        }

        boolean superOnKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }

        boolean superOnKeyPreIme(int keyCode, KeyEvent event) {
            return super.onKeyPreIme(keyCode, event);
        }

        boolean superOnKeyShortcut(int keyCode, KeyEvent event) {
            return super.onKeyShortcut(keyCode, event);
        }

        boolean superOnKeyUp(int keyCode, KeyEvent event) {
            return super.onKeyUp(keyCode, event);
        }

        void superOnSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
        }
    }

    private class InternalAutoCompleteTextView extends android.widget.AutoCompleteTextView{

        public InternalAutoCompleteTextView(Context context) {
            super(context);
        }

        public InternalAutoCompleteTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public InternalAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void refreshDrawableState() {
            super.refreshDrawableState();

            if(mLabelView != null)
                mLabelView.refreshDrawableState();

            if(mSupportView != null)
                mSupportView.refreshDrawableState();
        }

        @Override
        public void onCommitCompletion(CompletionInfo text) {
            EditText.this.onCommitCompletion(text);
        }

        @Override
        public void onCommitCorrection(CorrectionInfo info) {
            EditText.this.onCommitCorrection(info);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            return EditText.this.onCreateInputConnection(outAttrs);
        }

        @Override
        public void onEditorAction(int actionCode) {
            EditText.this.onEditorAction(actionCode);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return EditText.this.onKeyDown(keyCode, event);
        }

        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            return EditText.this.onKeyMultiple(keyCode, repeatCount, event);
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            return EditText.this.onKeyPreIme(keyCode, event);
        }

        @Override
        public boolean onKeyShortcut(int keyCode, KeyEvent event) {
            return EditText.this.onKeyShortcut(keyCode, event);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return EditText.this.onKeyUp(keyCode, event);
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            EditText.this.onSelectionChanged(selStart, selEnd);
        }

        @Override
        protected CharSequence convertSelectionToString(Object selectedItem) {
            return EditText.this.convertSelectionToString(selectedItem);
        }

        @Override
        protected void performFiltering(CharSequence text, int keyCode) {
            EditText.this.performFiltering(text, keyCode);
        }

        @Override
        protected void replaceText(CharSequence text) {
            EditText.this.replaceText(text);
        }

        @Override
        protected Filter getFilter() {
            return EditText.this.getFilter();
        }

        @Override
        public void onFilterComplete(int count) {
            EditText.this.onFilterComplete(count);
        }

        void superOnCommitCompletion(CompletionInfo text) {
            super.onCommitCompletion(text);
        }

        void superOnCommitCorrection(CorrectionInfo info) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                super.onCommitCorrection(info);
        }

        InputConnection superOnCreateInputConnection(EditorInfo outAttrs) {
            return super.onCreateInputConnection(outAttrs);
        }

        void superOnEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
        }

        boolean superOnKeyDown(int keyCode, KeyEvent event) {
            return super.onKeyDown(keyCode, event);
        }

        boolean superOnKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }

        boolean superOnKeyPreIme(int keyCode, KeyEvent event) {
            return super.onKeyPreIme(keyCode, event);
        }

        boolean superOnKeyShortcut(int keyCode, KeyEvent event) {
            return super.onKeyShortcut(keyCode, event);
        }

        boolean superOnKeyUp(int keyCode, KeyEvent event) {
            return super.onKeyUp(keyCode, event);
        }

        void superOnFilterComplete(int count) {
            super.onFilterComplete(count);
        }

        CharSequence superConvertSelectionToString(Object selectedItem) {
            return super.convertSelectionToString(selectedItem);
        }

        void superPerformFiltering(CharSequence text, int keyCode) {
            super.performFiltering(text, keyCode);
        }

        void superReplaceText(CharSequence text) {
            super.replaceText(text);
        }

        Filter superGetFilter() {
            return super.getFilter();
        }

        void superOnSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
        }
    }

    private class InternalMultiAutoCompleteTextView extends android.widget.MultiAutoCompleteTextView{

        public InternalMultiAutoCompleteTextView(Context context) {
            super(context);
        }

        public InternalMultiAutoCompleteTextView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public InternalMultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public void refreshDrawableState() {
            super.refreshDrawableState();

            if(mLabelView != null)
                mLabelView.refreshDrawableState();

            if(mSupportView != null)
                mSupportView.refreshDrawableState();
        }

        @Override
        public void onCommitCompletion(CompletionInfo text) {
            EditText.this.onCommitCompletion(text);
        }

        @Override
        public void onCommitCorrection(CorrectionInfo info) {
            EditText.this.onCommitCorrection(info);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            return EditText.this.onCreateInputConnection(outAttrs);
        }

        @Override
        public void onEditorAction(int actionCode) {
            EditText.this.onEditorAction(actionCode);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            return EditText.this.onKeyDown(keyCode, event);
        }

        @Override
        public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            return EditText.this.onKeyMultiple(keyCode, repeatCount, event);
        }

        @Override
        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            return EditText.this.onKeyPreIme(keyCode, event);
        }

        @Override
        public boolean onKeyShortcut(int keyCode, KeyEvent event) {
            return EditText.this.onKeyShortcut(keyCode, event);
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            return EditText.this.onKeyUp(keyCode, event);
        }

        @Override
        protected void onSelectionChanged(int selStart, int selEnd) {
            EditText.this.onSelectionChanged(selStart, selEnd);
        }

        @Override
        public void onFilterComplete(int count) {
            EditText.this.onFilterComplete(count);
        }

        @Override
        protected CharSequence convertSelectionToString(Object selectedItem) {
            return EditText.this.convertSelectionToString(selectedItem);
        }

        @Override
        protected void performFiltering(CharSequence text, int keyCode) {
            EditText.this.performFiltering(text, keyCode);
        }

        @Override
        protected void replaceText(CharSequence text) {
            EditText.this.replaceText(text);
        }

        @Override
        protected Filter getFilter() {
            return EditText.this.getFilter();
        }

        @Override
        protected void performFiltering(CharSequence text, int start, int end, int keyCode){
            EditText.this.performFiltering(text, start, end, keyCode);
        }

        void superOnCommitCompletion(CompletionInfo text) {
            super.onCommitCompletion(text);
        }

        void superOnCommitCorrection(CorrectionInfo info) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                super.onCommitCorrection(info);
        }

        InputConnection superOnCreateInputConnection(EditorInfo outAttrs) {
            return super.onCreateInputConnection(outAttrs);
        }

        void superOnEditorAction(int actionCode) {
            super.onEditorAction(actionCode);
        }

        boolean superOnKeyDown(int keyCode, KeyEvent event) {
            return super.onKeyDown(keyCode, event);
        }

        boolean superOnKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
            return super.onKeyMultiple(keyCode, repeatCount, event);
        }

        boolean superOnKeyPreIme(int keyCode, KeyEvent event) {
            return super.onKeyPreIme(keyCode, event);
        }

        boolean superOnKeyShortcut(int keyCode, KeyEvent event) {
            return super.onKeyShortcut(keyCode, event);
        }

        boolean superOnKeyUp(int keyCode, KeyEvent event) {
            return super.onKeyUp(keyCode, event);
        }

        void superOnFilterComplete(int count) {
            super.onFilterComplete(count);
        }

        CharSequence superConvertSelectionToString(Object selectedItem) {
            return super.convertSelectionToString(selectedItem);
        }

        void superPerformFiltering(CharSequence text, int keyCode) {
            super.performFiltering(text, keyCode);
        }

        void superReplaceText(CharSequence text) {
            super.replaceText(text);
        }

        Filter superGetFilter() {
            return super.getFilter();
        }

        void superPerformFiltering(CharSequence text, int start, int end, int keyCode){
            super.performFiltering(text, start, end, keyCode);
        }

        void superOnSelectionChanged(int selStart, int selEnd) {
            super.onSelectionChanged(selStart, selEnd);
        }
    }
}
