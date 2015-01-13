package com.rey.material.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by Rey on 1/12/2015.
 */
public class DialogFragment extends android.support.v4.app.DialogFragment{

    public interface Builder{
        public Dialog build(Context context);
    }

    protected static final String ARG_BUILDER = "arg_builder";

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && mBuilder == null)
            mBuilder = (Builder)savedInstanceState.getParcelable(ARG_BUILDER);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mBuilder != null && mBuilder instanceof Parcelable)
            outState.putParcelable(ARG_BUILDER, (Parcelable)mBuilder);
    }
}
