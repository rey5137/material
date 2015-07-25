package com.rey.material.demo;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rey.material.app.DialogFragment;
import com.rey.material.app.Recurring;
import com.rey.material.app.RecurringPickerDialog;
import com.rey.material.drawable.RippleDrawable;
import com.rey.material.widget.Button;
import com.rey.material.widget.FloatingActionButton;

import java.util.Calendar;

public class ButtonFragment extends Fragment{

	public static ButtonFragment newInstance(){
		ButtonFragment fragment = new ButtonFragment();
		
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_button, container, false);
		
		Button bt_flat = (Button)v.findViewById(R.id.button_bt_flat);
		Button bt_flat_color = (Button)v.findViewById(R.id.button_bt_flat_color);
		Button bt_flat_wave = (Button)v.findViewById(R.id.button_bt_flat_wave);
		Button bt_flat_wave_color = (Button)v.findViewById(R.id.button_bt_flat_wave_color);
		Button bt_raise = (Button)v.findViewById(R.id.button_bt_raise);
		Button bt_raise_color = (Button)v.findViewById(R.id.button_bt_raise_color);
		Button bt_raise_wave = (Button)v.findViewById(R.id.button_bt_raise_wave);
		Button bt_raise_wave_color = (Button)v.findViewById(R.id.button_bt_raise_wave_color);
		FloatingActionButton bt_float = (FloatingActionButton)v.findViewById(R.id.button_bt_float);
        FloatingActionButton bt_float_color = (FloatingActionButton)v.findViewById(R.id.button_bt_float_color);
        FloatingActionButton bt_float_wave = (FloatingActionButton)v.findViewById(R.id.button_bt_float_wave);
        FloatingActionButton bt_float_wave_color = (FloatingActionButton)v.findViewById(R.id.button_bt_float_wave_color);
									
		View.OnClickListener listener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                if(v instanceof  FloatingActionButton){
                    FloatingActionButton bt = (FloatingActionButton)v;
                    bt.setLineMorphingState((bt.getLineMorphingState() + 1) % 2, true);
                }

//                System.out.println(v + " " + ((RippleDrawable)v.getBackground()).getDelayClickType());
			}
		};
		
		View.OnClickListener listener_delay = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
                if(v instanceof  FloatingActionButton){
                    FloatingActionButton bt = (FloatingActionButton)v;
                    bt.setLineMorphingState((bt.getLineMorphingState() + 1) % 2, true);
                }

//                System.out.println(v + " " + ((RippleDrawable)v.getBackground()).getDelayClickType());
			}
		};
		
		bt_flat.setOnClickListener(listener);
		bt_flat_wave.setOnClickListener(listener);
		bt_raise.setOnClickListener(listener);
		bt_raise_wave.setOnClickListener(listener);
		bt_float.setOnClickListener(listener);
		bt_float_wave.setOnClickListener(listener);
		
		bt_flat_color.setOnClickListener(listener_delay);
		bt_flat_wave_color.setOnClickListener(listener_delay);
		bt_raise_color.setOnClickListener(listener_delay);
		bt_raise_wave_color.setOnClickListener(listener_delay);
		bt_float_color.setOnClickListener(listener_delay);
		bt_float_wave_color.setOnClickListener(listener_delay);

//        bt_flat.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View v) {
//                RecurringPickerDialog.Builder builder = new RecurringPickerDialog.Builder(R.style.Material_App_Dialog_Light){
//                    @Override
//                    public void onPositiveActionClicked(DialogFragment fragment) {
//                        RecurringPickerDialog dialog = (RecurringPickerDialog)fragment.getDialog();
//                        Toast.makeText(fragment.getActivity(), dialog.getRecurring().toString(), Toast.LENGTH_LONG).show();
//                        fragment.dismiss();
//                    }
//
//                    @Override
//                    public void onNegativeActionClicked(DialogFragment fragment) {
//                        Toast.makeText(fragment.getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
//                        fragment.dismiss();
//                    }
//                };
//                Recurring recurring = new Recurring();
//                recurring.setStartTime(System.currentTimeMillis());
//                recurring.setRepeatMode(Recurring.REPEAT_WEEKLY);
//                recurring.setEnabledWeekday(Calendar.SUNDAY, true);
//                recurring.setEnabledWeekday(Calendar.TUESDAY, true);
//                builder.recurring(recurring)
//                        .datePickerLayoutStyle(R.style.Material_App_Dialog_DatePicker_Light)
//                        .positiveAction("OK")
//                        .negativeAction("CANCEL");
//
//                DialogFragment fragment = DialogFragment.newInstance(builder);
//                fragment.show(getFragmentManager(), null);
//            }
//        });

		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
}
