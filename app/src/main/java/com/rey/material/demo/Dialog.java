package com.rey.material.demo;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.rey.material.drawable.BlankDrawable;

/**
 * Created by Rey on 12/10/2014.
 */
public class Dialog extends android.support.v4.app.DialogFragment{

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);

        android.app.Dialog dialog = super.onCreateDialog(savedInstanceState);
        Window window = dialog.getWindow();
//        window.requestFeature(Window.FEATURE_NO_TITLE);
//        window.setBackgroundDrawable(BlankDrawable.getInstance());
//
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        System.out.println(lp.dimAmount);
//
//        lp.copyFrom(window.getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
//        window.setAttributes(lp);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_dialog, container, false);

        return v;
    }
}
