package com.rey.material.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.rey.material.R;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.TypefaceUtil;
import com.rey.material.util.ViewUtil;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.jar.Attributes;

import de.greenrobot.event.EventBus;

/**
 * Created by Rey on 5/25/2015.
 */
public class ThemeManager {

    private volatile static ThemeManager mInstance;

    private Context mContext;
    private SparseArray<int[]> mStyles =  new SparseArray<>();
    private int mCurrentTheme;
    private int mThemeCount;
    private EventDispatcher mDispatcher;

    private static final String PREF = "theme.pref";
    private static final String KEY_THEME = "theme";

    public static final int THEME_UNDEFINED = Integer.MIN_VALUE;

    /**
     * Apply any View style attributes to a view.
     * @param v The view is applied.
     * @param resId The style resourceId.
     */
    public static void applyStyle(View v, int resId){
        TypedArray a = v.getContext().obtainStyledAttributes(null, R.styleable.View, 0, resId);

        int leftPadding = -1;
        int topPadding = -1;
        int rightPadding = -1;
        int bottomPadding = -1;
        int startPadding = Integer.MIN_VALUE;
        int endPadding = Integer.MIN_VALUE;
        int padding = -1;

        boolean startPaddingDefined = false;
        boolean endPaddingDefined = false;
        boolean leftPaddingDefined = false;
        boolean rightPaddingDefined = false;

        for(int i = 0, count = a.getIndexCount(); i < count; i++){
            int attr = a.getIndex(i);
            if(attr == R.styleable.View_android_background) {
                Drawable bg = a.getDrawable(attr);
                ViewUtil.setBackground(v, bg);
            }
            else if(attr == R.styleable.View_android_backgroundTint){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    v.setBackgroundTintList(a.getColorStateList(attr));
            }
            else if(attr == R.styleable.View_android_backgroundTintMode){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    int value = a.getInt(attr, 3);
                    switch (value){
                        case 3:
                            v.setBackgroundTintMode(PorterDuff.Mode.SRC_OVER);
                            break;
                        case 5:
                            v.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
                            break;
                        case 9:
                            v.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                            break;
                        case 14:
                            v.setBackgroundTintMode(PorterDuff.Mode.MULTIPLY);
                            break;
                        case 15:
                            v.setBackgroundTintMode(PorterDuff.Mode.SCREEN);
                            break;
                        case 16:
                            v.setBackgroundTintMode(PorterDuff.Mode.ADD);
                            break;
                    }
                }
            }
            else if(attr == R.styleable.View_android_elevation){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    v.setElevation(a.getDimensionPixelOffset(attr, 0));
            }
            else if(attr == R.styleable.View_android_padding) {
                padding = a.getDimensionPixelSize(attr, -1);
                leftPaddingDefined = true;
                rightPaddingDefined = true;
            }
            else if(attr == R.styleable.View_android_paddingLeft) {
                leftPadding = a.getDimensionPixelSize(attr, -1);
                leftPaddingDefined = true;
            }
            else if(attr == R.styleable.View_android_paddingTop)
                topPadding = a.getDimensionPixelSize(attr, -1);
            else if(attr == R.styleable.View_android_paddingRight) {
                rightPadding = a.getDimensionPixelSize(attr, -1);
                rightPaddingDefined = true;
            }
            else if(attr == R.styleable.View_android_paddingBottom)
                bottomPadding = a.getDimensionPixelSize(attr, -1);
            else if(attr == R.styleable.View_android_paddingStart) {
                startPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                startPaddingDefined = (startPadding != Integer.MIN_VALUE);
            }
            else if(attr == R.styleable.View_android_paddingEnd) {
                endPadding = a.getDimensionPixelSize(attr, Integer.MIN_VALUE);
                endPaddingDefined = (endPadding != Integer.MIN_VALUE);
            }
            else if(attr == R.styleable.View_android_fadeScrollbars)
                v.setScrollbarFadingEnabled(a.getBoolean(attr, true));
            else if(attr == R.styleable.View_android_fadingEdgeLength)
                v.setFadingEdgeLength(a.getDimensionPixelOffset(attr, 0));
            else if(attr == R.styleable.View_android_minHeight)
                v.setMinimumHeight(a.getDimensionPixelSize(attr, 0));
            else if(attr == R.styleable.View_android_minWidth)
                v.setMinimumWidth(a.getDimensionPixelSize(attr, 0));
            else if(attr == R.styleable.View_android_requiresFadingEdge)
                v.setVerticalFadingEdgeEnabled(a.getBoolean(attr, true));
            else if(attr == R.styleable.View_android_scrollbarDefaultDelayBeforeFade) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    v.setScrollBarDefaultDelayBeforeFade(a.getInteger(attr, 0));
            }
            else if(attr == R.styleable.View_android_scrollbarFadeDuration) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    v.setScrollBarFadeDuration(a.getInteger(attr, 0));
            }
            else if(attr == R.styleable.View_android_scrollbarSize) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    v.setScrollBarSize(a.getDimensionPixelSize(attr, 0));
            }
            else if(attr == R.styleable.View_android_scrollbarStyle) {
                int value = a.getInteger(attr, 0);
                switch (value){
                    case 0x0:
                        v.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                        break;
                    case 0x01000000:
                        v.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
                        break;
                    case 0x02000000:
                        v.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
                        break;
                    case 0x03000000:
                        v.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
                        break;
                }
            }
            else if(attr == R.styleable.View_android_soundEffectsEnabled)
                v.setSoundEffectsEnabled(a.getBoolean(attr, true));
            else if(attr == R.styleable.View_android_textAlignment){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    int value = a.getInteger(attr, 0);
                    switch (value){
                        case 0:
                            v.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);
                            break;
                        case 1:
                            v.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
                            break;
                        case 2:
                            v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                            break;
                        case 3:
                            v.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                            break;
                        case 4:
                            v.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            break;
                        case 5:
                            v.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                            break;
                        case 6:
                            v.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                            break;
                    }
                }
            }
            else if(attr == R.styleable.View_android_textDirection){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    int value = a.getInteger(attr, 0);
                    switch (value){
                        case 0:
                            v.setTextDirection(View.TEXT_DIRECTION_INHERIT);
                            break;
                        case 1:
                            v.setTextDirection(View.TEXT_DIRECTION_FIRST_STRONG);
                            break;
                        case 2:
                            v.setTextDirection(View.TEXT_DIRECTION_ANY_RTL);
                            break;
                        case 3:
                            v.setTextDirection(View.TEXT_DIRECTION_LTR);
                            break;
                        case 4:
                            v.setTextDirection(View.TEXT_DIRECTION_RTL);
                            break;
                        case 5:
                            v.setTextDirection(View.TEXT_DIRECTION_LOCALE);
                            break;
                    }
                }
            }
            else if(attr == R.styleable.View_android_visibility){
                int value = a.getInteger(attr, 0);
                switch (value){
                    case 0:
                        v.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        v.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        v.setVisibility(View.GONE);
                        break;
                }
            }
            else if(attr == R.styleable.View_android_layoutDirection){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    int value = a.getInteger(attr, 0);
                    switch (value){
                        case 0:
                            v.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
                            break;
                        case 1:
                            v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                            break;
                        case 2:
                            v.setLayoutDirection(View.LAYOUT_DIRECTION_INHERIT);
                            break;
                        case 3:
                            v.setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
                            break;
                    }
                }
            }
        }

        if (padding >= 0)
            v.setPadding(padding, padding, padding, padding);
        else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){
            if(startPaddingDefined)
                leftPadding = startPadding;
            if(endPaddingDefined)
                rightPadding = endPadding;

            v.setPadding(leftPadding >= 0 ? leftPadding : v.getPaddingLeft(),
                    topPadding >= 0 ? topPadding : v.getPaddingTop(),
                    rightPadding >= 0 ? rightPadding : v.getPaddingRight(),
                    bottomPadding >= 0 ? bottomPadding : v.getPaddingBottom());
        }
        else{
            if(leftPaddingDefined || rightPaddingDefined)
                v.setPadding(leftPaddingDefined ? leftPadding : v.getPaddingLeft(),
                        topPadding >= 0 ? topPadding : v.getPaddingTop(),
                        rightPaddingDefined ? rightPadding : v.getPaddingRight(),
                        bottomPadding >= 0 ? bottomPadding : v.getPaddingBottom());

            if(startPaddingDefined || endPaddingDefined)
                v.setPaddingRelative(startPaddingDefined ? startPadding : v.getPaddingStart(),
                        topPadding >= 0 ? topPadding : v.getPaddingTop(),
                        endPaddingDefined ? endPadding : v.getPaddingEnd(),
                        bottomPadding >= 0 ? bottomPadding : v.getPaddingBottom());
        }

        a.recycle();

        if(v instanceof TextView)
            applyStyle((TextView)v, resId);
    }

    /**
     * Apply any TextView style attributes to a view.
     * @param v The view is applied.
     * @param resId The style resourceId.
     */
    private static void applyStyle(TextView v, int resId){
        String fontFamily = null;
        int typefaceIndex = -1;
        int styleIndex = -1;
        int shadowColor = 0;
        float dx = 0, dy = 0, r = 0;

        Drawable drawableLeft = null, drawableTop = null, drawableRight = null,
                drawableBottom = null, drawableStart = null, drawableEnd = null;
        boolean drawableDefined = false;
        boolean drawableRelativeDefined = false;

        /*
         * Look the appearance up without checking first if it exists because
         * almost every TextView has one and it greatly simplifies the logic
         * to be able to parse the appearance first and then let specific tags
         * for this View override it.
         */
        TypedArray a = v.getContext().obtainStyledAttributes(null, R.styleable.TextViewAppearance, 0, resId);
        TypedArray appearance = null;
        int ap = a.getResourceId(R.styleable.TextViewAppearance_android_textAppearance, -1);
        a.recycle();

        if (ap != -1)
            appearance = v.getContext().obtainStyledAttributes(ap, R.styleable.TextAppearance);

        if (appearance != null) {
            int n = appearance.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = appearance.getIndex(i);

                if (attr == R.styleable.TextAppearance_android_textColorHighlight) {
                    v.setHighlightColor(appearance.getColor(attr, 0));

                } else if (attr == R.styleable.TextAppearance_android_textColor) {
                    v.setTextColor(appearance.getColorStateList(attr));

                } else if (attr == R.styleable.TextAppearance_android_textColorHint) {
                    v.setHintTextColor(appearance.getColorStateList(attr));

                } else if (attr == R.styleable.TextAppearance_android_textColorLink) {
                    v.setLinkTextColor(appearance.getColorStateList(attr));

                } else if (attr == R.styleable.TextAppearance_android_textSize) {
                    v.setTextSize(TypedValue.COMPLEX_UNIT_PX, appearance.getDimensionPixelSize(attr, 0));

                } else if (attr == R.styleable.TextAppearance_android_typeface) {
                    typefaceIndex = appearance.getInt(attr, -1);

                } else if (attr == R.styleable.TextAppearance_android_fontFamily) {
                    fontFamily = appearance.getString(attr);

                } else if (attr == R.styleable.TextAppearance_android_textStyle) {
                    styleIndex = appearance.getInt(attr, -1);

                } else if (attr == R.styleable.TextAppearance_android_textAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                        v.setAllCaps(appearance.getBoolean(attr, false));

                } else if (attr == R.styleable.TextAppearance_android_shadowColor) {
                    shadowColor = appearance.getInt(attr, 0);

                } else if (attr == R.styleable.TextAppearance_android_shadowDx) {
                    dx = appearance.getFloat(attr, 0);

                } else if (attr == R.styleable.TextAppearance_android_shadowDy) {
                    dy = appearance.getFloat(attr, 0);

                } else if (attr == R.styleable.TextAppearance_android_shadowRadius) {
                    r = appearance.getFloat(attr, 0);

                } else if (attr == R.styleable.TextAppearance_android_elegantTextHeight) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        v.setElegantTextHeight(appearance.getBoolean(attr, false));

                } else if (attr == R.styleable.TextAppearance_android_letterSpacing) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        v.setLetterSpacing(appearance.getFloat(attr, 0));

                } else if (attr == R.styleable.TextAppearance_android_fontFeatureSettings) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        v.setFontFeatureSettings(appearance.getString(attr));

                }
            }

            appearance.recycle();
        }

        a = v.getContext().obtainStyledAttributes(null, R.styleable.TextView, 0, resId);

        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.TextView_android_drawableLeft) {
                drawableLeft = a.getDrawable(attr);
                drawableDefined = true;

            } else if (attr == R.styleable.TextView_android_drawableTop) {
                drawableTop = a.getDrawable(attr);
                drawableDefined = true;

            } else if (attr == R.styleable.TextView_android_drawableRight) {
                drawableRight = a.getDrawable(attr);
                drawableDefined = true;

            } else if (attr == R.styleable.TextView_android_drawableBottom) {
                drawableBottom = a.getDrawable(attr);
                drawableDefined = true;

            } else if (attr == R.styleable.TextView_android_drawableStart) {
                drawableStart = a.getDrawable(attr);
                drawableRelativeDefined = true;

            } else if (attr == R.styleable.TextView_android_drawableEnd) {
                drawableEnd = a.getDrawable(attr);
                drawableRelativeDefined = true;

            } else if (attr == R.styleable.TextView_android_drawablePadding) {
                v.setCompoundDrawablePadding(a.getDimensionPixelSize(attr, 0));

            } else if (attr == R.styleable.TextView_android_maxLines) {
                v.setMaxLines(a.getInt(attr, -1));

            } else if (attr == R.styleable.TextView_android_maxHeight) {
                v.setMaxHeight(a.getDimensionPixelSize(attr, -1));

            } else if (attr == R.styleable.TextView_android_lines) {
                v.setLines(a.getInt(attr, -1));

            } else if (attr == R.styleable.TextView_android_height) {
                v.setHeight(a.getDimensionPixelSize(attr, -1));

            } else if (attr == R.styleable.TextView_android_minLines) {
                v.setMinLines(a.getInt(attr, -1));

            } else if (attr == R.styleable.TextView_android_minHeight) {
                v.setMinHeight(a.getDimensionPixelSize(attr, -1));

            } else if (attr == R.styleable.TextView_android_maxEms) {
                v.setMaxEms(a.getInt(attr, -1));

            } else if (attr == R.styleable.TextView_android_maxWidth) {
                v.setMaxWidth(a.getDimensionPixelSize(attr, -1));

            } else if (attr == R.styleable.TextView_android_ems) {
                v.setEms(a.getInt(attr, -1));

            } else if (attr == R.styleable.TextView_android_width) {
                v.setWidth(a.getDimensionPixelSize(attr, -1));

            } else if (attr == R.styleable.TextView_android_minEms) {
                v.setMinEms(a.getInt(attr, -1));

            } else if (attr == R.styleable.TextView_android_minWidth) {
                v.setMinWidth(a.getDimensionPixelSize(attr, -1));

            } else if (attr == R.styleable.TextView_android_gravity) {
                v.setGravity(a.getInt(attr, -1));

            } else if (attr == R.styleable.TextView_android_scrollHorizontally) {
                v.setHorizontallyScrolling(a.getBoolean(attr, false));

            } else if (attr == R.styleable.TextView_android_includeFontPadding) {
                v.setIncludeFontPadding(a.getBoolean(attr, true));

            } else if (attr == R.styleable.TextView_android_cursorVisible) {
                v.setCursorVisible(a.getBoolean(attr, true));

            } else if (attr == R.styleable.TextView_android_textScaleX) {
                v.setTextScaleX(a.getFloat(attr, 1.0f));

            } else if (attr == R.styleable.TextView_android_shadowColor) {
                shadowColor = a.getInt(attr, 0);

            } else if (attr == R.styleable.TextView_android_shadowDx) {
                dx = a.getFloat(attr, 0);

            } else if (attr == R.styleable.TextView_android_shadowDy) {
                dy = a.getFloat(attr, 0);

            } else if (attr == R.styleable.TextView_android_shadowRadius) {
                r = a.getFloat(attr, 0);

            } else if (attr == R.styleable.TextView_android_textColorHighlight) {
                v.setHighlightColor(a.getColor(attr, 0));

            } else if (attr == R.styleable.TextView_android_textColor) {
                v.setTextColor(a.getColorStateList(attr));

            } else if (attr == R.styleable.TextView_android_textColorHint) {
                v.setHintTextColor(a.getColorStateList(attr));

            } else if (attr == R.styleable.TextView_android_textColorLink) {
                v.setLinkTextColor(a.getColorStateList(attr));

            } else if (attr == R.styleable.TextView_android_textSize) {
                v.setTextSize(TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(attr, 0));

            } else if (attr == R.styleable.TextView_android_typeface) {
                typefaceIndex = a.getInt(attr, -1);

            } else if (attr == R.styleable.TextView_android_textStyle) {
                styleIndex = a.getInt(attr, -1);

            } else if (attr == R.styleable.TextView_android_fontFamily) {
                fontFamily = a.getString(attr);

            } else if (attr == R.styleable.TextView_android_textAllCaps) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                    v.setAllCaps(a.getBoolean(attr, false));

            } else if (attr == R.styleable.TextView_android_elegantTextHeight) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    v.setElegantTextHeight(a.getBoolean(attr, false));

            } else if (attr == R.styleable.TextView_android_letterSpacing) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    v.setLetterSpacing(a.getFloat(attr, 0));

            } else if (attr == R.styleable.TextView_android_fontFeatureSettings) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    v.setFontFeatureSettings(a.getString(attr));

            }
        }
        a.recycle();

        if (shadowColor != 0)
            v.setShadowLayer(r, dx, dy, shadowColor);

        if(drawableDefined) {
            Drawable[] drawables = v.getCompoundDrawables();
            if (drawableStart != null)
                drawables[0] = drawableStart;
            else if (drawableLeft != null)
                drawables[0] = drawableLeft;
            if (drawableTop != null)
                drawables[1] = drawableTop;
            if (drawableEnd != null)
                drawables[2] = drawableEnd;
            else if (drawableRight != null)
                drawables[2] = drawableRight;
            if (drawableBottom != null)
                drawables[3] = drawableBottom;
            v.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
        }

        if(drawableRelativeDefined && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            Drawable[] drawables = v.getCompoundDrawablesRelative();
            if (drawableStart != null)
                drawables[0] = drawableStart;
            if (drawableEnd != null)
                drawables[2] = drawableEnd;
            v.setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3]);
        }

        Typeface tf = null;
        if (fontFamily != null) {
            tf = TypefaceUtil.load(v.getContext(), fontFamily, styleIndex);
            if (tf != null) {
                v.setTypeface(tf);
                return;
            }
        }
        switch (typefaceIndex) {
            case 1:
                tf = Typeface.SANS_SERIF;
                break;
            case 2:
                tf = Typeface.SERIF;
                break;
            case 3:
                tf = Typeface.MONOSPACE;
                break;
        }
        v.setTypeface(tf, styleIndex);
    }

    /**
     * Get the styleId from attributes.
     * @param context
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     * @return The styleId.
     */
    public static int getStyleId(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThemableView, defStyleAttr, defStyleRes);
        int styleId = a.getResourceId(R.styleable.ThemableView_v_styleId, 0);
        a.recycle();

        return styleId;
    }

    /**
     * Init ThemeManager. Should be call in {@link Application#onCreate()}.
     * @param context The context object. Should be {#link Application} object.
     * @param resId The resourceId of array of all styleId.
     */
    public static void init(Context context, int resId){
        getInstance().setup(context, resId);
    }

    /**
     * Get the singleton instance of ThemeManager.
     * @return The singleton instance of ThemeManager.
     */
    public static ThemeManager getInstance(){
        if(mInstance == null){
            synchronized (ThemeManager.class){
                if(mInstance == null)
                    mInstance = new ThemeManager();
            }
        }

        return mInstance;
    }

    protected void setup(Context context, int resId){
        mContext = context;
        mDispatcher = new EventBusDispatcher();
        mCurrentTheme = getSharedPreferences().getInt(KEY_THEME, 0);

        TypedArray array = context.getResources().obtainTypedArray(resId);
        for(int i = 0, size = array.length(); i < size; i++){
            int arrayId = array.getResourceId(i, 0);
            int[] styles = loadStyleList(context, arrayId);
            if(mThemeCount == 0)
                mThemeCount = styles.length;
            mStyles.put(arrayId, styles);
        }
        array.recycle();

        if(mCurrentTheme >= mThemeCount)
            setCurrentTheme(0);
    }

    private int[] loadStyleList(Context context, int resId){
        TypedArray array = context.getResources().obtainTypedArray(resId);
        int[] result = new int[array.length()];
        for(int i = 0; i < result.length; i++)
            result[i] = array.getResourceId(i, 0);
        array.recycle();

        return result;
    }

    private SharedPreferences getSharedPreferences(){
        return mContext.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private void dispatchThemeChanged(int theme){
        mDispatcher.dispatchThemeChanged(theme);
    }

    public Context getContext(){
        return mContext;
    }

    /**
     * Get the current theme.
     * @return The current theme.
     */
    public int getCurrentTheme(){
        return mCurrentTheme;
    }

    /**
     * Set the current theme. Should be called in main thread (UI thread).
     * @param theme The current theme.
     * @return True if set theme successfully, False if method's called on main thread or theme already set.
     */
    public boolean setCurrentTheme(int theme){
        if (Looper.getMainLooper().getThread() != Thread.currentThread())
            return false;

        if(mCurrentTheme != theme){
            mCurrentTheme = theme;
            getSharedPreferences().edit().putInt(KEY_THEME, mCurrentTheme).commit();
            dispatchThemeChanged(mCurrentTheme);
            return true;
        }

        return false;
    }

    /**
     * Get the total theme.
     * @return The total theme.
     */
    public int getThemeCount(){
        return mThemeCount;
    }

    /**
     * Get current style of a styleId.
     * @param styleId The styleId.
     * @return The current style.
     */
    public int getCurrentStyle(int styleId){
        int[] styles = mStyles.get(styleId);
        return styles[getCurrentTheme()];
    }

    /**
     * Get a specific style of a styleId.
     * @param styleId The styleId.
     * @param theme The theme.
     * @return The specific style.
     */
    public int getStyle(int styleId, int theme){
        int[] styles = mStyles.get(styleId);
        return styles[theme];
    }

    /**
     * Register a listener will be called when current theme changed.
     * @param listener A {@link com.rey.material.app.ThemeManager.OnThemeChangedListener} will be registered.
     */
    public void registerOnThemeChangedListener(OnThemeChangedListener listener){
        mDispatcher.registerListener(listener);
    }

    /**
     * Unregister a listener from be called when current theme changed.
     * @param listener A {@link com.rey.material.app.ThemeManager.OnThemeChangedListener} will be unregistered.
     */
    public void unregisterOnThemeChangedListener(OnThemeChangedListener listener){
        mDispatcher.unregisterListener(listener);
    }

    public interface EventDispatcher{

        public void registerListener(OnThemeChangedListener listener);

        public void unregisterListener(OnThemeChangedListener listener);

        public void dispatchThemeChanged(int theme);
    }

    public class EventBusDispatcher implements EventDispatcher{

        EventBus mBus;

        public EventBusDispatcher(){
            mBus = EventBus.builder().eventInheritance(true).build();
        }

        @Override
        public void registerListener(OnThemeChangedListener listener) {
            mBus.register(listener);
        }

        @Override
        public void unregisterListener(OnThemeChangedListener listener) {
            mBus.unregister(listener);
        }

        @Override
        public void dispatchThemeChanged(int theme) {
            mBus.post(new OnThemeChangedEvent(theme));
        }
    }

    public interface OnThemeChangedListener{

        public void onEvent(OnThemeChangedEvent event);

    }

    public class OnThemeChangedEvent{
        public final int theme;

        public OnThemeChangedEvent(int theme){
            this.theme = theme;
        }
    }


}
