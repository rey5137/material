package com.rey.material.app;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * Created by Rey on 1/12/2015.
 */
public class DialogFragment extends android.support.v4.app.DialogFragment{

    public interface Builder{
        public Dialog build(Context context);
    }
    
    protected Builder mBuilder;
    
    public static DialogFragment newInstance(Builder builder){
        DialogFragment fragment = new DialogFragment();
        fragment.mBuilder = builder;
        return fragment;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return mBuilder == null ? new Dialog(getActivity()) : mBuilder.build(getActivity());
    }
}
