package com.rey.material.demo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.rey.material.app.BottomSheetDialog;
import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.DialogFragment;
import com.rey.material.app.SimpleDialog;
import com.rey.material.app.ThemeManager;
import com.rey.material.app.TimePickerDialog;
import com.rey.material.drawable.ThemeDrawable;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.Button;
import com.rey.material.widget.EditText;

import java.text.SimpleDateFormat;

public class DialogsFragment extends Fragment implements View.OnClickListener {

	public static DialogsFragment newInstance(){
		DialogsFragment fragment = new DialogsFragment();
		
		return fragment;
	}

    private MainActivity mActivity;

    private BottomSheetDialog mBottomSheetDialog;

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
        Button bt_time = (Button)v.findViewById(R.id.dialog_bt_time);
        Button bt_date = (Button)v.findViewById(R.id.dialog_bt_date);
        Button bt_bottomsheet = (Button)v.findViewById(R.id.dialog_bt_bottomsheet);

        bt_title_only.setOnClickListener(this);
        bt_msg_only.setOnClickListener(this);
        bt_title_msg.setOnClickListener(this);
        bt_custom.setOnClickListener(this);
        bt_choice.setOnClickListener(this);
        bt_multi_choice.setOnClickListener(this);
        bt_time.setOnClickListener(this);
        bt_date.setOnClickListener(this);
        bt_bottomsheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet();
            }
        });

        mActivity = (MainActivity)getActivity();

		return v;
	}

	private void showBottomSheet(){
        mBottomSheetDialog = new BottomSheetDialog(mActivity, R.style.Material_App_BottomSheetDialog);
        View v = LayoutInflater.from(mActivity).inflate(R.layout.view_bottomsheet, null);
        ViewUtil.setBackground(v, new ThemeDrawable(R.array.bg_window));
        Button bt_match = (Button)v.findViewById(R.id.sheet_bt_match);
        bt_match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.heightParam(ViewGroup.LayoutParams.MATCH_PARENT);
            }
        });
        Button bt_wrap = (Button)v.findViewById(R.id.sheet_bt_wrap);
        bt_wrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetDialog.heightParam(ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });

        mBottomSheetDialog.contentView(v)
                .show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mBottomSheetDialog != null){
            mBottomSheetDialog.dismissImmediately();
            mBottomSheetDialog = null;
        }
    }

    @Override
    public void onClick(View v) {
        Dialog.Builder builder = null;

        boolean isLightTheme = ThemeManager.getInstance().getCurrentTheme() == 0;

        switch (v.getId()){
            case R.id.dialog_bt_title_only:
                builder = new SimpleDialog.Builder(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog){
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
                builder = new SimpleDialog.Builder(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog){
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
                builder = new SimpleDialog.Builder(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog){
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
                builder = new SimpleDialog.Builder(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog){

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
                builder = new SimpleDialog.Builder(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog){
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
                builder = new SimpleDialog.Builder(isLightTheme ? R.style.SimpleDialogLight : R.style.SimpleDialog){
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
            case R.id.dialog_bt_time:
                builder = new TimePickerDialog.Builder(isLightTheme ? R.style.Material_App_Dialog_TimePicker_Light : R.style.Material_App_Dialog_TimePicker, 24, 00){
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
            case R.id.dialog_bt_date:
                builder = new DatePickerDialog.Builder(isLightTheme ? R.style.Material_App_Dialog_DatePicker_Light :  R.style.Material_App_Dialog_DatePicker){
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
