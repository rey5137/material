package com.rey.material.app;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Rey on 1/12/2015.
 */
public class DialogFragment extends android.support.v4.app.DialogFragment{

    /**
     * Interface definition for passing style data.
     */
    public interface Builder{
        /**
         * Get a Dialog instance used for this fragment.
         * @param context A Context instance.
         * @return The Dialog will be used for this fragment.
         */
        public com.rey.material.app.Dialog build(Context context);

        /**
         * Handle click event on Positive Action.
         */
        public void onPositiveActionClicked(DialogFragment fragment);

        /**
         * Handle click event on Negative Action.
         */
        public void onNegativeActionClicked(DialogFragment fragment);

        /**
         * Handle click event on Neutral Action.
         */
        public void onNeutralActionClicked(DialogFragment fragment);
    }

    protected static final String ARG_BUILDER = "arg_builder";

    protected Builder mBuilder;

    private View.OnClickListener mActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mBuilder == null)
                return;

            if(v.getId() == Dialog.ACTION_POSITIVE)
                mBuilder.onPositiveActionClicked(DialogFragment.this);
            else if(v.getId() == Dialog.ACTION_NEGATIVE)
                mBuilder.onNegativeActionClicked(DialogFragment.this);
            else if(v.getId() == Dialog.ACTION_NEUTRAL)
                mBuilder.onNeutralActionClicked(DialogFragment.this);
        }
    };
    
    public static DialogFragment newInstance(Builder builder){
        DialogFragment fragment = new DialogFragment();
        fragment.mBuilder = builder;
        return fragment;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        com.rey.material.app.Dialog dialog = mBuilder == null ? new Dialog(getActivity()) : mBuilder.build(getActivity());
        dialog.positiveActionClickListener(mActionListener)
                .negativeActionClickListener(mActionListener)
                .neutralActionClickListener(mActionListener);
        return dialog;
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
