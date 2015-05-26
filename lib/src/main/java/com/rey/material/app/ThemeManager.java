package com.rey.material.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.rey.material.R;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.util.ViewUtil;

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

    private static void applyStyle(TextView v, int resId){

    }

    public static int getStyleId(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ThemableView, defStyleAttr, defStyleRes);
        int styleId = a.getResourceId(R.styleable.ThemableView_v_styleId, 0);
        a.recycle();

        return styleId;
    }

    public static void init(Context context, int resId){
        getInstance().setup(context, resId);
    }

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

    public int getCurrentTheme(){
        return mCurrentTheme;
    }

    public void setCurrentTheme(int theme){
        if(mCurrentTheme != theme){
            mCurrentTheme = theme;
            getSharedPreferences().edit().putInt(KEY_THEME, mCurrentTheme).commit();
            dispatchThemeChanged(mCurrentTheme);
        }
    }

    public int getThemeCount(){
        return mThemeCount;
    }

    public int getCurrentStyle(int styleId){
        int[] styles = mStyles.get(styleId);
        return styles[getCurrentTheme()];
    }

    public void registerOnThemeChangedListener(View v){
        mDispatcher.registerListener(v);
    }

    public void unregisterOnThemeChangedListener(View v){
        mDispatcher.unregisterListener(v);
    }

    public interface EventDispatcher{

        public void registerListener(View v);

        public void unregisterListener(View v);

        public void dispatchThemeChanged(int theme);
    }

    public class EventBusDispatcher implements EventDispatcher{

        EventBus mBus;

        public EventBusDispatcher(){
            mBus = EventBus.builder().eventInheritance(true).build();
        }

        @Override
        public void registerListener(View v) {
            mBus.register(v);
        }

        @Override
        public void unregisterListener(View v) {
            mBus.unregister(v);
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
