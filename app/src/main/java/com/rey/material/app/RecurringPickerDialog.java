package com.rey.material.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.rey.material.demo.R;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.CompoundButton;
import com.rey.material.widget.EditText;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.Spinner;
import com.rey.material.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rey on 2/2/2015.
 */
public class RecurringPickerDialog extends Dialog implements WeekView.OnDaySelectionChangedListener {

    private ModeAdapter mModeAdapter;
    private EndAdapter mEndAdapter;

    private EditText mPeriodEditText;
    private TextView mPeriodUnitTextView;
    private Spinner mModeSpinner;
    private Spinner mEndSpinner;
    private RadioButton mSameDayRadioButton;
    private RadioButton mSameWeekdayRadioButton;
    private EditText mEndNumEditText;
    private TextView mEndNumUnitTextView;
    private Button mEndDateButton;
    private WeekView mWeekView;

    private HeaderDrawable mHeaderBackground;

    private Recurring mRecurring;
    private int mDatePickerDialogStyleId;

    private static int[] MONTH_SAME_WEEKDAY = {R.string.rd_month_last, R.string.rd_month_first, R.string.rd_month_second, R.string.rd_month_third, R.string.rd_month_fourth};

    private DateFormat mDateFormat = SimpleDateFormat.getDateInstance();

    public RecurringPickerDialog(Context context) {
        super(context);
    }

    public RecurringPickerDialog(Context context, int style) {
        super(context, style);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_recurring, null);
        setContentView(v);

        FrameLayout fl_mode = (FrameLayout)v.findViewById(R.id.rd_fl_mode);
        final ScrollView sv_repeat = (ScrollView)v.findViewById(R.id.rd_sv_repeat);
        final LinearLayout ll_repeat = (LinearLayout)v.findViewById(R.id.rd_ll_repeat);
        mModeSpinner = (Spinner)fl_mode.findViewById(R.id.rd_spn_mode);
        mEndSpinner = (Spinner)v.findViewById(R.id.rd_spn_end);
        mPeriodEditText = (EditText)v.findViewById(R.id.rd_et_period);
        mPeriodUnitTextView = (TextView)v.findViewById(R.id.rd_tv_period_unit);
        mSameDayRadioButton = (RadioButton)v.findViewById(R.id.rd_month_rb_same);
        mSameWeekdayRadioButton = (RadioButton)v.findViewById(R.id.rd_month_rb_week);
        mEndNumEditText = (EditText)v.findViewById(R.id.rd_et_end_num);
        mEndNumUnitTextView = (TextView)v.findViewById(R.id.rd_tv_end_num_unit);
        mEndDateButton = (Button)v.findViewById(R.id.rd_bt_end_date);
        mWeekView = (WeekView)v.findViewById(R.id.rd_wv_week);

        sv_repeat.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                showDivider(ll_repeat.getMeasuredHeight() > sv_repeat.getMeasuredHeight());
            }
        });

        mHeaderBackground = new HeaderDrawable(getContext());

        ViewCompat.setPaddingRelative(fl_mode, mContentPadding, 0, mContentPadding, 0);
        ViewUtil.setBackground(fl_mode, mHeaderBackground);
        ViewCompat.setPaddingRelative(ll_repeat, mContentPadding, mActionOuterPadding, mContentPadding, mActionPadding);

        mModeAdapter = new ModeAdapter();
        mModeSpinner.setAdapter(mModeAdapter);
        mModeSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                onModeSelected(position);
            }
        });

        mEndAdapter = new EndAdapter();
        mEndSpinner.setAdapter(mEndAdapter);
        mEndSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                onEndSelected(position);
            }
        });

        mPeriodEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onPeriodChanged();
            }
        });

        mPeriodEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    String text = mPeriodEditText.getText().toString();
                    if(TextUtils.isEmpty(text))
                        mPeriodEditText.setText(String.valueOf(mRecurring.getPeriod()));
                }
                return false;
            }
        });

        CompoundButton.OnCheckedChangeListener mCheckChangeListener = new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    if(buttonView == mSameDayRadioButton)
                        mSameWeekdayRadioButton.setChecked(false);
                    else
                        mSameDayRadioButton.setChecked(false);
                    onMonthSettingChanged();
                }
            }
        };

        mSameDayRadioButton.setOnCheckedChangeListener(mCheckChangeListener);
        mSameWeekdayRadioButton.setOnCheckedChangeListener(mCheckChangeListener);

        mEndNumEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                onEventNumberChanged();
            }
        });

        mEndNumEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                    String text = mEndNumEditText.getText().toString();
                    if(TextUtils.isEmpty(text))
                        mEndNumEditText.setText(String.valueOf(mRecurring.getEventNumber()));
                }
                return false;
            }
        });

        View.OnClickListener mDateClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final DatePickerDialog dialog = new DatePickerDialog(getContext(), mDatePickerDialogStyleId);
                long minTime = System.currentTimeMillis();
                Calendar cal = dialog.getCalendar();
                cal.setTimeInMillis(minTime);
                cal.add(Calendar.YEAR, 100);
                long maxTime = cal.getTimeInMillis();

                dialog.dateRange(minTime, maxTime)
                        .date((long)mEndDateButton.getTag())
                        .positiveAction(mPositiveAction.getText())
                        .positiveActionClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                onEndDateChanged(dialog.getDate());
                                dialog.dismiss();
                            }
                        })
                        .negativeAction(mNegativeAction.getText())
                        .negativeActionClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        };

        mEndDateButton.setOnClickListener(mDateClickListener);

        mWeekView.setOnDaySelectionChangedListener(this);
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
        mHeaderBackground.updateCorner(radius);
        return super.cornerRadius(radius);
    }

    public RecurringPickerDialog recurring(Recurring recurring){
        mRecurring = recurring;
        updateRecurringData();
        return this;
    }

    public RecurringPickerDialog datePickerLayoutStyle(int styleId){
        mDatePickerDialogStyleId = styleId;
        return this;
    }

    public Recurring getRecurring(){
        return mRecurring;
    }

    private void animOut(final View v, final boolean setGone, final boolean immediately){
        if(!isShowing() || v.getVisibility() != View.VISIBLE || immediately) {
            v.setVisibility(setGone ? View.GONE : View.INVISIBLE);
            return;
        }

        Animation anim = new AlphaAnimation(1f, 0f);
        anim.setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(setGone ? View.GONE : View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        v.startAnimation(anim);
    }

    private void animIn(final View v, boolean immediately){
        if(v.getVisibility() == View.VISIBLE)
            return;

        if(!isShowing() || immediately) {
            v.setVisibility(View.VISIBLE);
            return;
        }

        Animation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime));
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {}

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        v.startAnimation(anim);
    }

    private void updateRecurringData(){
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(mRecurring.getStartTime());
        int order = Recurring.getWeekDayOrderNum(cal);
        String dayOfWeek = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        int formattedTextId = MONTH_SAME_WEEKDAY[(order + 1) % MONTH_SAME_WEEKDAY.length];
        mSameWeekdayRadioButton.setText(getContext().getResources().getString(formattedTextId, dayOfWeek));

        mPeriodEditText.setText(String.valueOf(mRecurring.getPeriod()));

        if(mRecurring.getRepeatMode() == Recurring.REPEAT_WEEKLY) {
            for(int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++)
                mWeekView.setSelected(i, mRecurring.isEnabledWeekday(i), true);
        }
        else{
            int day = cal.get(Calendar.DAY_OF_WEEK);
            for(int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++)
                mWeekView.setSelected(i, i == day, true);

            if(mRecurring.getRepeatMode() == Recurring.REPEAT_MONTHLY){
                mSameDayRadioButton.setCheckedImmediately(mRecurring.getMonthRepeatType() == Recurring.MONTH_SAME_DAY);
                mSameWeekdayRadioButton.setCheckedImmediately(mRecurring.getMonthRepeatType() == Recurring.MONTH_SAME_WEEKDAY);
            }
            else{
                mSameDayRadioButton.setCheckedImmediately(true);
                mSameWeekdayRadioButton.setCheckedImmediately(false);
            }
        }

        if(mModeSpinner.getSelectedItemPosition() != mRecurring.getRepeatMode())
            mModeSpinner.setSelection(mRecurring.getRepeatMode());
        else
            onModeSelected(mRecurring.getRepeatMode());

        mEndNumEditText.setText(String.valueOf(mRecurring.getEndMode() == Recurring.END_FOR_EVENT ? mRecurring.getEventNumber() : 10));

        long date = mRecurring.getEndMode() == Recurring.END_UNTIL_DATE ? mRecurring.getEndDate() : (Math.max(System.currentTimeMillis(), mRecurring.getStartTime()) + 86400000L * 31);
        mEndDateButton.setText(mDateFormat.format(new Date(date)));
        mEndDateButton.setTag(date);

        if(mEndSpinner.getSelectedItemPosition() != mRecurring.getEndMode())
            mEndSpinner.setSelection(mRecurring.getEndMode());
        else
            onEndSelected(mRecurring.getEndMode());
    }

    private void onModeSelected(int mode){
        int oldMode = mRecurring.getRepeatMode();
        mRecurring.setRepeatMode(mode);
        updatePeriodUnit();
        mRecurring.setRepeatSetting(0);

        if(mode == Recurring.REPEAT_NONE){
            mPeriodEditText.setEnabled(false);
            mEndSpinner.setEnabled(false);
            mEndNumEditText.setEnabled(false);
            mEndDateButton.setEnabled(false);
            mSameDayRadioButton.setEnabled(false);
            mSameWeekdayRadioButton.setEnabled(false);
            mWeekView.setEnabled(false);
        }
        else{
            if(oldMode == Recurring.REPEAT_NONE){
                mPeriodEditText.setEnabled(true);
                mEndSpinner.setEnabled(true);
                mEndNumEditText.setEnabled(true);
                mEndDateButton.setEnabled(true);
                mSameDayRadioButton.setEnabled(true);
                mSameWeekdayRadioButton.setEnabled(true);
                mWeekView.setEnabled(true);
            }

            switch (mode){
                case Recurring.REPEAT_DAILY:
                case Recurring.REPEAT_YEARLY:
                    animOut(mSameDayRadioButton, true, true);
                    animOut(mSameWeekdayRadioButton, true, true);
                    animOut(mWeekView, true, true);
                    break;
                case Recurring.REPEAT_MONTHLY:
                    animIn(mSameDayRadioButton, false);
                    animIn(mSameWeekdayRadioButton, false);
                    animOut(mWeekView, true, true);
                    mRecurring.setMonthRepeatType(mSameDayRadioButton.isChecked() ? Recurring.MONTH_SAME_DAY : Recurring.MONTH_SAME_WEEKDAY);
                    break;
                case Recurring.REPEAT_WEEKLY:
                    animOut(mSameDayRadioButton, true, true);
                    animOut(mSameWeekdayRadioButton, true, true);
                    animIn(mWeekView, false);
                    for(int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++)
                        mRecurring.setEnabledWeekday(i, mWeekView.isSelected(i));
                    break;
            }
        }

    }

    private void onEndSelected(int endMode){
        mRecurring.setEndMode(endMode);
        mRecurring.setEndSetting(0);

        switch (endMode){
            case Recurring.END_FOREVER:
                animOut(mEndNumEditText, false, false);
                animOut(mEndNumUnitTextView, false, false);
                animOut(mEndDateButton, false, false);
                break;
            case Recurring.END_UNTIL_DATE:
                animOut(mEndNumEditText, false, true);
                animOut(mEndNumUnitTextView, false, true);
                animIn(mEndDateButton, false);
                mRecurring.setEndDate((Long)mEndDateButton.getTag());
                break;
            case Recurring.END_FOR_EVENT:
                animIn(mEndNumEditText, false);
                animIn(mEndNumUnitTextView, false);
                animOut(mEndDateButton, false, true);
                mRecurring.setEventNumber(Integer.parseInt(mEndNumEditText.getText().toString()));
                break;
        }
    }

    private void onPeriodChanged(){
        String text = mPeriodEditText.getText().toString();
        if(!TextUtils.isEmpty(text)){
            int period = Integer.parseInt(text);
            if(period < 1)
                mPeriodEditText.setText("1");
            else {
                mRecurring.setPeriod(period);
                updatePeriodUnit();
            }
        }
    }

    private void onMonthSettingChanged(){
        mRecurring.setMonthRepeatType(mSameDayRadioButton.isChecked() ? Recurring.MONTH_SAME_DAY : Recurring.MONTH_SAME_WEEKDAY);
    }

    private void onEventNumberChanged(){
        String text = mEndNumEditText.getText().toString();
        if(!TextUtils.isEmpty(text)){
            int num = Integer.parseInt(text);
            if(num < 1)
                mEndNumEditText.setText("1");
            else {
                mRecurring.setEventNumber(num);
                updateNumberUnit();
            }
        }
    }

    private void onEndDateChanged(long date){
        mEndDateButton.setTag(date);
        mEndDateButton.setText(mDateFormat.format(new Date(date)));
        mRecurring.setEndDate(date);
    }

    @Override
    public void onDaySelectionChanged(int dayOfWeek, boolean selected) {
        mRecurring.setEnabledWeekday(dayOfWeek, selected);
    }

    private void updatePeriodUnit(){
        switch (mRecurring.getRepeatMode()){
            case Recurring.REPEAT_DAILY:
                mPeriodUnitTextView.setText(mRecurring.getPeriod() == 1 ? R.string.rd_day : R.string.rd_days);
                break;
            case Recurring.REPEAT_WEEKLY:
                mPeriodUnitTextView.setText(mRecurring.getPeriod() == 1 ? R.string.rd_week : R.string.rd_weeks);
                break;
            case Recurring.REPEAT_MONTHLY:
                mPeriodUnitTextView.setText(mRecurring.getPeriod() == 1 ? R.string.rd_month : R.string.rd_months);
                break;
            case Recurring.REPEAT_YEARLY:
                mPeriodUnitTextView.setText(mRecurring.getPeriod() == 1 ? R.string.rd_year : R.string.rd_years);
                break;
        }
    }

    private void updateNumberUnit(){
        mEndNumUnitTextView.setText(mRecurring.getEventNumber() == 1 ? R.string.rd_event : R.string.rd_events);
    }

    private class ModeAdapter extends BaseAdapter{
        private int[] mItems = {R.string.rd_none, R.string.rd_daily, R.string.rd_weekly, R.string.rd_monthly, R.string.rd_yearly};

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null)
                v = LayoutInflater.from(getContext()).inflate(R.layout.rd_item_mode, parent, false);

            ((TextView)v).setText(mItems[position]);
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null)
                v = LayoutInflater.from(getContext()).inflate(R.layout.rd_item_dropdown_mode, parent, false);

            ((TextView)v).setText(mItems[position]);
            return v;
        }
    }

    private class EndAdapter extends BaseAdapter{
        private int[] mItems = {R.string.rd_forever, R.string.rd_until, R.string.rd_for};
        private int[] mDropDownItems = {R.string.rd_forever, R.string.rd_until_full, R.string.rd_for_full};

        @Override
        public int getCount() {
            return mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null)
                v = LayoutInflater.from(getContext()).inflate(R.layout.rd_item_end, parent, false);

            ((TextView)v).setText(mItems[position]);
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if(v == null)
                v = LayoutInflater.from(getContext()).inflate(R.layout.rd_item_dropdown_end, parent, false);

            ((TextView)v).setText(mDropDownItems[position]);
            return v;
        }
    }

    private class HeaderDrawable extends Drawable{
        private Paint mPaint;
        private float mRadius;
        private Path mPath;

        public HeaderDrawable(Context context){
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(ThemeUtil.colorPrimary(context, 0));
            mPaint.setStyle(Paint.Style.FILL);
            mPath = new Path();
        }

        public void updateCorner(float radius){
            mRadius = radius;
            Rect bounds = getBounds();

            mPath.reset();
            if(radius == 0)
                mPath.addRect(bounds.left, bounds.top, bounds.right, bounds.bottom, Path.Direction.CW);
            else {
                RectF rect = new RectF();
                mPath.moveTo(bounds.left, bounds.top - radius);
                rect.set(bounds.left, bounds.top, bounds.left + radius * 2, bounds.top + radius * 2);
                mPath.arcTo(rect, 180, 90, false);
                mPath.lineTo(bounds.right - radius, bounds.top);
                rect.set(bounds.right - radius * 2, bounds.top, bounds.right, bounds.top + radius * 2);
                mPath.arcTo(rect, 270, 90, false);
                mPath.lineTo(bounds.right, bounds.bottom);
                mPath.lineTo(bounds.left, bounds.bottom);
                mPath.close();
            }

            invalidateSelf();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            updateCorner(mRadius);
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawPath(mPath, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    public static class Builder extends Dialog.Builder{

        private Recurring mRecurring;
        private int mDatePickerLayoutStyleId;

        public Builder() {
            super();
        }

        public Builder(int styleId){
            super(styleId);
        }

        public Builder recurring(Recurring recurring){
            mRecurring = recurring;
            return this;
        }

        public Builder datePickerLayoutStyle(int styleId){
            mDatePickerLayoutStyleId = styleId;
            return this;
        }

        @Override
        public Dialog.Builder contentView(int layoutId) {
            return this;
        }

        @Override
        protected Dialog onBuild(Context context, int styleId) {
            RecurringPickerDialog dialog = new RecurringPickerDialog(context, styleId);
            dialog.recurring(mRecurring)
                    .datePickerLayoutStyle(mDatePickerLayoutStyleId);
            return dialog;
        }

        protected Builder(Parcel in){
            super(in);
        }

        @Override
        protected void onWriteToParcel(Parcel dest, int flags) {
            dest.writeInt(mDatePickerLayoutStyleId);
            dest.writeLong(mRecurring.getStartTime());
            dest.writeInt(mRecurring.getRepeatMode());
            dest.writeInt(mRecurring.getPeriod());
            dest.writeInt(mRecurring.getRepeatSetting());
            dest.writeInt(mRecurring.getEndMode());
            dest.writeLong(mRecurring.getEndSetting());
        }

        @Override
        protected void onReadFromParcel(Parcel in) {
            mDatePickerLayoutStyleId = in.readInt();
            mRecurring = new Recurring();
            mRecurring.setStartTime(in.readLong());
            mRecurring.setRepeatMode(in.readInt());
            mRecurring.setPeriod(in.readInt());
            mRecurring.setRepeatSetting(in.readInt());
            mRecurring.setEndMode(in.readInt());
            mRecurring.setEndSetting(in.readLong());
        }

        public static final Parcelable.Creator<Builder> CREATOR = new Parcelable.Creator<Builder>() {
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

    }
}
