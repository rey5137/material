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
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;

import java.text.SimpleDateFormat;

public class DialogsFragment extends Fragment implements View.OnClickListener {

	public static DialogsFragment newInstance(){
		DialogsFragment fragment = new DialogsFragment();
		
		return fragment;
	}

    private MainActivity mActivity;

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

        mActivity = (MainActivity)getActivity();

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
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Discarded", Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Canceled", Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                builder.title("Discard draft?")
                        .positiveAction("DISCARD")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_msg_only:
                builder = new SimpleDialog.Builder(R.style.SimpleDialog){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Deleted", Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled", Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                ((SimpleDialog.Builder)builder).message("Delete this conversation?")
                        .positiveAction("DELETE")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_title_msg:
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Agreed", Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Disagreed", Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                ((SimpleDialog.Builder)builder).message("Let Google help apps determine location. This means sending anonymous location data to Google, even when no apps are running.")
                        .title("Use Google's location service?")
                        .positiveAction("AGREE")
                        .negativeAction("DISAGREE");
                break;
            case R.id.dialog_bt_custom:
                builder = new SimpleDialog.Builder(R.style.SimpleDialog){

                    @Override
                    protected void onBuildDone(Dialog dialog) {
                        dialog.layoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    }

                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        EditText et_pass = (EditText)fragment.getDialog().findViewById(R.id.custom_et_password);
                        Toast.makeText(mActivity, "Connected. pass=" + et_pass.getText().toString(), Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled", Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                builder.title("Google Wi-Fi")
                    .positiveAction("CONNECT")
                    .negativeAction("CANCEL")
                    .contentView(R.layout.layout_dialog_custom);
                break;
            case R.id.dialog_bt_choice:
                builder = new SimpleDialog.Builder(R.style.SimpleDialogLight){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "You have selected " + getSelectedValue() + " as phone ringtone.", Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled" , Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                ((SimpleDialog.Builder)builder).items(new String[]{"None", "Callisto", "Dione", "Ganymede", "Hangouts Call", "Luna", "Oberon", "Phobos"}, 0)
                        .title("Phone Ringtone")
                        .positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_multi_choice:
                builder = new SimpleDialog.Builder(R.style.SimpleDialog){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        CharSequence[] values =  getSelectedValues();
                        if(values == null)
                            Toast.makeText(mActivity, "You have selected nothing.", Toast.LENGTH_SHORT).show();
                        else{
                            StringBuffer sb = new StringBuffer();
                            sb.append("You have selected ");
                            for(int i = 0; i < values.length; i++)
                                sb.append(values[i]).append(i == values.length - 1? "." : ", ");
                            Toast.makeText(mActivity, sb.toString(), Toast.LENGTH_SHORT).show();
                        }
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled" , Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                ((SimpleDialog.Builder)builder).multiChoiceItems(new String[]{"Soup", "Pizza", "Hotdogs", "Hamburguer", "Coffee", "Juice", "Milk", "Water"}, 2, 5)
                        .title("Food Order")
                        .positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_time_light:
                builder = new TimePickerDialog.Builder(6, 00){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        TimePickerDialog dialog = (TimePickerDialog)fragment.getDialog();
                        Toast.makeText(mActivity, "Time is " + dialog.getFormattedTime(SimpleDateFormat.getTimeInstance()), Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled" , Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                builder.positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_date_light:
                builder = new DatePickerDialog.Builder(){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        DatePickerDialog dialog = (DatePickerDialog)fragment.getDialog();
                        String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                        Toast.makeText(mActivity, "Date is " + date, Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled" , Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
                    }
                };

                builder.positiveAction("OK")
                        .negativeAction("CANCEL");
                break;
            case R.id.dialog_bt_time_dark:
                builder = new TimePickerDialog.Builder(R.style.Material_App_Dialog_TimePicker, 24, 00){
                    @Override
                    public void onPositiveActionClicked(DialogFragment fragment) {
                        TimePickerDialog dialog = (TimePickerDialog)fragment.getDialog();
                        Toast.makeText(mActivity, "Time is " + dialog.getFormattedTime(SimpleDateFormat.getTimeInstance()), Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled" , Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
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
                        String date = dialog.getFormattedDate(SimpleDateFormat.getDateInstance());
                        Toast.makeText(mActivity, "Date is " + date, Toast.LENGTH_SHORT).show();
                        super.onPositiveActionClicked(fragment);
                    }

                    @Override
                    public void onNegativeActionClicked(DialogFragment fragment) {
                        Toast.makeText(mActivity, "Cancelled" , Toast.LENGTH_SHORT).show();
                        super.onNegativeActionClicked(fragment);
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
