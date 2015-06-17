package com.rey.material.drawable;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;

import com.rey.material.app.ThemeManager;

/**
 * Created by Rey on 5/27/2015.
 */
public class ThemeDrawable extends LevelListDrawable implements ThemeManager.OnThemeChangedListener {
    private int mStyleId;

    public ThemeDrawable(int styleId) {
        mStyleId = styleId;

        if(mStyleId != 0) {
            ThemeManager.getInstance().registerOnThemeChangedListener(this);
            initDrawables();
        }
    }

    private void initDrawables(){
        ThemeManager themeManager = ThemeManager.getInstance();
        int count = themeManager.getThemeCount();

        for(int i = 0; i < count; i++){
            Drawable drawable = themeManager.getContext().getResources().getDrawable(themeManager.getStyle(mStyleId, i));
            addLevel(i, i, drawable);
        }

        setLevel(themeManager.getCurrentTheme());
    }

    @Override
    public void onThemeChanged(ThemeManager.OnThemeChangedEvent event) {
        if(getLevel() != event.theme)
            setLevel(event.theme);
    }

}
