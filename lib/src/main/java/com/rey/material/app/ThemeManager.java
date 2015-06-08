package com.rey.material.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.rey.material.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

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
     * @param totalTheme The total theme.
     * @param defaultTheme The default theme if current theme isn't set.
     * @param dispatcher The {@link EventDispatcher} will be used to dispatch {@link OnThemeChangedEvent}. If null, then use {@link SimpleDispatcher}.
     */
    public static void init(Context context, int totalTheme, int defaultTheme, @Nullable EventDispatcher dispatcher){
        getInstance().setup(context, totalTheme, defaultTheme, dispatcher);
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

    protected void setup(Context context, int totalTheme, int defaultTheme, @Nullable EventDispatcher dispatcher){
        mContext = context;
        mDispatcher = dispatcher != null ? dispatcher : new SimpleDispatcher();
        mThemeCount = totalTheme;
        mCurrentTheme = getSharedPreferences().getInt(KEY_THEME, defaultTheme);
        if(mCurrentTheme >= mThemeCount)
            setCurrentTheme(defaultTheme);
    }

    private int[] loadStyleList(Context context, int resId){
        TypedArray array = context.getResources().obtainTypedArray(resId);
        int[] result = new int[array.length()];
        for(int i = 0; i < result.length; i++)
            result[i] = array.getResourceId(i, 0);
        array.recycle();

        return result;
    }

    private int[] getStyleList(int styleId){
        int[] list = mStyles.get(styleId);
        if(list == null){
            list = loadStyleList(mContext, styleId);
            mStyles.put(styleId, list);
        }

        return list;
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
    @UiThread
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
        return getStyle(styleId, mCurrentTheme);
    }

    /**
     * Get a specific style of a styleId.
     * @param styleId The styleId.
     * @param theme The theme.
     * @return The specific style.
     */
    public int getStyle(int styleId, int theme){
        int[] styles = getStyleList(styleId);
        return styles[theme];
    }

    /**
     * Register a listener will be called when current theme changed.
     * @param listener A {@link com.rey.material.app.ThemeManager.OnThemeChangedListener} will be registered.
     */
    public void registerOnThemeChangedListener(@NonNull OnThemeChangedListener listener){
        mDispatcher.registerListener(listener);
    }

    /**
     * Unregister a listener from be called when current theme changed.
     * @param listener A {@link com.rey.material.app.ThemeManager.OnThemeChangedListener} will be unregistered.
     */
    public void unregisterOnThemeChangedListener(@NonNull OnThemeChangedListener listener){
        mDispatcher.unregisterListener(listener);
    }

    public interface EventDispatcher{

        public void registerListener(OnThemeChangedListener listener);

        public void unregisterListener(OnThemeChangedListener listener);

        public void dispatchThemeChanged(int theme);
    }

    public static class SimpleDispatcher implements EventDispatcher{

        ArrayList<WeakReference<OnThemeChangedListener>> mListeners = new ArrayList<>();

        @Override
        public void registerListener(OnThemeChangedListener listener) {
            boolean exist = false;
            for(int i = mListeners.size() - 1; i >= 0; i--){
                WeakReference<OnThemeChangedListener> ref = mListeners.get(i);
                if(ref.get() == null)
                    mListeners.remove(i);
                else if(ref.get() == listener)
                    exist = true;
            }

            if(!exist)
                mListeners.add(new WeakReference<>(listener));
        }

        @Override
        public void unregisterListener(OnThemeChangedListener listener) {
            for(int i = mListeners.size() - 1; i >= 0; i--){
                WeakReference<OnThemeChangedListener> ref = mListeners.get(i);
                if(ref.get() == null || ref.get() == listener)
                    mListeners.remove(i);
            }
        }

        @Override
        public void dispatchThemeChanged(int theme) {
            OnThemeChangedEvent event = new OnThemeChangedEvent(theme);

            for(int i = mListeners.size() - 1; i >= 0; i--){
                WeakReference<OnThemeChangedListener> ref = mListeners.get(i);
                if(ref.get() == null)
                    mListeners.remove(i);
                else
                    ref.get().onThemeChanged(event);
            }
        }
    }

    public interface OnThemeChangedListener{

        void onThemeChanged(@Nullable OnThemeChangedEvent event);

    }

    public static class OnThemeChangedEvent{
        public final int theme;

        public OnThemeChangedEvent(int theme){
            this.theme = theme;
        }
    }


}
