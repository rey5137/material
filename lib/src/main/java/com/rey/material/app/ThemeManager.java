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
