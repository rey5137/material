package com.rey.material.demo;

import android.app.Application;
import android.content.Context;

import com.rey.material.app.ThemeManager;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Rey on 5/22/2015.
 */
public class DemoApplication extends Application{

    public static RefWatcher getRefWatcher(Context context) {
        DemoApplication application = (DemoApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override public void onCreate() {
        super.onCreate();
        ThemeManager.init(this, 2, 0, null);
    }
}
