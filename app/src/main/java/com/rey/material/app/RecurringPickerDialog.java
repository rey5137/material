package com.rey.material.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.rey.material.demo.R;
import com.rey.material.widget.Spinner;

/**
 * Created by Rey on 2/2/2015.
 */
public class RecurringPickerDialog extends Dialog{

    private float mCornerRadius;

    public RecurringPickerDialog(Context context) {
        super(context);
    }

    public RecurringPickerDialog(Context context, int style) {
        super(context, style);
    }

    @Override
    protected void onCreate() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_recurring, null);
        setContentView(v);

        FrameLayout fl_mode = (FrameLayout)v.findViewById(R.id.rd_fl_mode);
        fl_mode.setPadding(mContentPadding, 0, mContentPadding, 0);

        LinearLayout ll_repeat = (LinearLayout)v.findViewById(R.id.rd_ll_repeat);
        ll_repeat.setPadding(mContentPadding, 0, mContentPadding, 0);

        Spinner spn_mode = (Spinner)fl_mode.findViewById(R.id.rd_spn_mode);
        spn_mode.setAdapter(new ArrayAdapter<>(getContext(),
                R.layout.row_rd_mode,
                new String[]{
                        getContext().getString(R.string.rd_daily),
                        getContext().getString(R.string.rd_weekly),
                        getContext().getString(R.string.rd_monthly),
                        getContext().getString(R.string.rd_yearly)
                }
        ));
    }

    @Override
    public Dialog applyStyle(int resId) {
        return super.applyStyle(resId);
    }

    @Override
    public Dialog layoutParams(int width, int height) {
        return super.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public Dialog cornerRadius(float radius){
        mCornerRadius = radius;
        return super.cornerRadius(radius);
    }


}
