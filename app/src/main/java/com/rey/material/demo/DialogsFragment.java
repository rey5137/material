package com.rey.material.demo;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.TimePickerDialog;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.Button;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DialogsFragment extends Fragment implements View.OnClickListener {

	public static DialogsFragment newInstance(){
		DialogsFragment fragment = new DialogsFragment();
		
		return fragment;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_dialog, container, false);

        Button bt_title_only = (Button)v.findViewById(R.id.dialog_bt_title_only);
        Button bt_msg_only = (Button)v.findViewById(R.id.dialog_bt_msg_only);
        Button bt_title_msg = (Button)v.findViewById(R.id.dialog_bt_title_msg);
        Button bt_custom = (Button)v.findViewById(R.id.dialog_bt_custom);
        Button bt_choice = (Button)v.findViewById(R.id.dialog_bt_choice);
        Button bt_multi_choice = (Button)v.findViewById(R.id.dialog_bt_multi_choice);
        Button bt_time_light = (Button)v.findViewById(R.id.dialog_bt_time_light);
        Button bt_date_light = (Button)v.findViewById(R.id.dialog_bt_date_light);
        Button bt_time_dark = (Button)v.findViewById(R.id.dialog_bt_time_dark);
        Button bt_date_dark = (Button)v.findViewById(R.id.dialog_bt_date_dark);

        bt_title_only.setOnClickListener(this);
        bt_msg_only.setOnClickListener(this);
        bt_title_msg.setOnClickListener(this);
        bt_custom.setOnClickListener(this);
        bt_choice.setOnClickListener(this);
        bt_multi_choice.setOnClickListener(this);
        bt_time_light.setOnClickListener(this);
        bt_date_light.setOnClickListener(this);
        bt_time_dark.setOnClickListener(this);
        bt_date_dark.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        Dialog.Builder builder = null;

        switch (v.getId()){
            case R.id.dialog_bt_title_only:
                builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Discarded", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                builder.title("Discard draft?")
                        .positiveAction("DISCARD")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_msg_only:
                builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                ((SimpleDialog.Builder)builder).message("Delete this conversation?")
                        .positiveAction("DELETE")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_title_msg:
                builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Agreed", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Disagreed", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                ((SimpleDialog.Builder)builder).message("Let Google help apps determine location. This means sending anonymous location data to Google, even when no apps are running.")
                        .title("Use Google's location service?")
                        .positiveAction("AGREE")
                        .negativeAction("DISAGREE");
                break;
            case R.id.dialog_bt_custom:
                builder = new SimpleDialog.Builder(R.style.Material_App_Dialog){

                    @Override
                    protected Dialog onBuild(Context context, int styleId) {
                        Dialog dialog = super.onBuild(context, styleId);
                        dialog.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        return dialog;
                    }

                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Connected", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                builder.title("Google Wi-Fi")
                    .positiveAction("CONNECT")
                    .negativeAction("CANCEL")
                    .contentView(R.layout.layout_dialog_custom);
                break;
            case R.id.dialog_bt_choice:
                builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple_Light){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        SimpleDialog dialog = (SimpleDialog)fragment.getDialog();
                        Toast.makeText(fragment.getActivity(), "You have selected " + dialog.getSelectedValue() + " as phone ringtone.", Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled" , Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                ((SimpleDialog.Builder)builder).items(new String[]{"None", "Callisto", "Dione", "Ganymede", "Hangouts Call", "Luna", "Oberon", "Phobos"}, 0)
                        .title("Phone Ringtone")
                        .positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_multi_choice:
                builder = new SimpleDialog.Builder(R.style.Material_App_Dialog_Simple){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        SimpleDialog dialog = (SimpleDialog)fragment.getDialog();
                        CharSequence[] values = dialog.getSelectedValues();
                        if(values == null)
                            Toast.makeText(fragment.getActivity(), "You have selected nothing.", Toast.LENGTH_SHORT).show();
                        else{
                            StringBuffer sb = new StringBuffer();
                            sb.append("You have selected ");
                            for(int i = 0; i < values.length; i++)
                                sb.append(values[i]).append(i == values.length - 1? "." : ", ");
                            Toast.makeText(fragment.getActivity(), sb.toString(), Toast.LENGTH_SHORT).show();
                        }
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled" , Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                ((SimpleDialog.Builder)builder).multiChoiceItems(new String[]{"Soup", "Pizza", "Hotdogs", "Hamburguer", "Coffee", "Juice", "Milk", "Water"}, 2, 5)
                        .title("Food Order")
                        .positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_time_light:
                builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker_Light, 6, 00, true){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        TimePickerDialog dialog = (TimePickerDialog)fragment.getDialog();
                        Toast.makeText(fragment.getActivity(), "Time is " + (dialog.getHour() + 1) + ":" + dialog.getMinute() + (dialog.isAm() ? " am" : "pm"), Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled" , Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                builder.positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_date_light:
                builder = new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker_Light){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        DatePickerDialog dialog = (DatePickerDialog)fragment.getDialog();
                        String date = dialog.getFormatedDate(SimpleDateFormat.getDateInstance());
                        Toast.makeText(fragment.getActivity(), "Date is " + date, Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled" , Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                builder.positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_time_dark:
                builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker, 0, 30, true){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        TimePickerDialog dialog = (TimePickerDialog)fragment.getDialog();
                        Toast.makeText(fragment.getActivity(), "Time is " + (dialog.getHour() + 1) + ":" + dialog.getMinute() + (dialog.isAm() ? " am" : "pm"), Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled" , Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                builder.positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_date_dark:
                builder = new DatePickerDialog.Builder(R.style.Material_App_Dialog_DatePicker){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        DatePickerDialog dialog = (DatePickerDialog)fragment.getDialog();
                        String date = dialog.getFormatedDate(SimpleDateFormat.getDateInstance());
                        Toast.makeText(fragment.getActivity(), "Date is " + date, Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(fragment.getActivity(), "Cancelled" , Toast.LENGTH_SHORT).show();
                        fragment.dismiss();
                    }
                };

                builder.positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
        }

        DialogFragment fragment = DialogFragment.newInstance(builder);
        fragment.show(getFragmentManager(), null);
    }
}
