package com.rey.material.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ScrollView;

import com.rey.material.R;
import com.rey.material.drawable.BlankDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.CheckBox;
import com.rey.material.widget.CompoundButton;
import com.rey.material.widget.ListView;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.TextView;

/**
 * Created by Rey on 12/16/2014.
 */
public class SimpleDialog extends Dialog {

    private TextView mMessage;
    private InternalScrollView mScrollView;
    private InternalListView mListView;
    private InternalAdapter mAdapter;

    private int mMessageTextAppearanceId;
    private int mMessageTextColor;

    private int mRadioButtonStyle;
    private int mCheckBoxStyle;
    private int mItemHeight;
    private int mItemTextAppearance;

    private int mMode;

    private static final int MODE_NONE = 0;
    private static final int MODE_MESSAGE = 1;
    private static final int MODE_ITEMS = 2;
    private static final int MODE_MULTI_ITEMS = 3;

    public SimpleDialog(Context context) {
        super(context);
    }

    public SimpleDialog(Context context, int style) {
        super(context, style);
    }

    @Override
    public Dialog applyStyle(int resId) {
        super.applyStyle(resId);

        TypedArray a = getContext().obtainStyledAttributes(resId, R.styleable.SimpleDialog);

        messageTextAppearance(a.getResourceId(R.styleable.SimpleDialog_di_messageTextAppearance, R.style.TextAppearance_AppCompat_Body1));

        if(ThemeUtil.getType(a, R.styleable.SimpleDialog_di_messageTextColor) != TypedValue.TYPE_NULL)
            messageTextColor(a.getColor(R.styleable.SimpleDialog_di_messageTextColor, 0));

        mRadioButtonStyle = a.getResourceId(R.styleable.SimpleDialog_di_radioButtonStyle, 0);
        mCheckBoxStyle = a.getResourceId(R.styleable.SimpleDialog_di_checkBoxStyle, 0);
        mItemHeight = a.getDimensionPixelSize(R.styleable.SimpleDialog_di_itemHeight, ViewGroup.LayoutParams.WRAP_CONTENT);
        mItemTextAppearance = a.getResourceId(R.styleable.SimpleDialog_di_itemTextAppearance, R.style.TextAppearance_AppCompat_Body1);

        a.recycle();

        return this;
    }

    @Override
    public Dialog clearContent() {
        super.clearContent();
        mMode = MODE_NONE;
        return this;
    }

    public Dialog message(CharSequence message){
        if(mMessage == null){
            mMessage = new TextView(getContext());
            mMessage.setTextAppearance(getContext(), mMessageTextAppearanceId);
            mMessage.setTextColor(mMessageTextColor);
            mMessage.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);

            mScrollView = new InternalScrollView(getContext());
            mScrollView.setPadding(mContentPadding, 0, mContentPadding, mContentPadding - mActionPadding);
            mScrollView.setClipToPadding(false);
            mScrollView.setFillViewport(true);
            mScrollView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);

            mScrollView.addView(mMessage);
        }

        mMessage.setText(message);
        if(!TextUtils.isEmpty(message)) {
            mMode = MODE_MESSAGE;
            setContentView(mScrollView);
        }
        return this;
    }

    public Dialog message(int id){
        return message(id == 0 ? null : getContext().getResources().getString(id));
    }

    public Dialog messageTextAppearance(int resId){
        if(mMessageTextAppearanceId != resId){
            mMessageTextAppearanceId = resId;
            if(mMessage != null)
                mMessage.setTextAppearance(getContext(), mMessageTextAppearanceId);
        }
        return this;
    }

    public Dialog messageTextColor(int color){
        if(mMessageTextColor != color){
            mMessageTextColor = color;
            if(mMessage != null)
                mMessage.setTextColor(color);
        }
        return this;
    }

    public Dialog radioButtonStyle(int resId){
        if(mRadioButtonStyle != resId){
            mRadioButtonStyle = resId;
            if(mAdapter != null && mMode == MODE_ITEMS)
                mAdapter.notifyDataSetChanged();
        }
        return this;
    }

    public Dialog checkBoxStyle(int resId){
        if(mCheckBoxStyle != resId){
            mCheckBoxStyle = resId;
            if(mAdapter != null && mMode == MODE_MULTI_ITEMS)
                mAdapter.notifyDataSetChanged();
        }
        return this;
    }

    public Dialog itemHeight(int height){
        if(mItemHeight != height){
            mItemHeight = height;
            if(mAdapter != null)
                mAdapter.notifyDataSetChanged();
        }
        return this;
    }

    public Dialog itemTextAppearance(int resId){
        if(mItemTextAppearance != resId){
            mItemTextAppearance = resId;
            if(mAdapter != null)
                mAdapter.notifyDataSetChanged();
        }
        return this;
    }

    private void initListView(){
        mListView = new InternalListView(getContext());
        mListView.setDividerHeight(0);
        mListView.setCacheColorHint(0x00000000);
        mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        mListView.setClipToPadding(false);
        mListView.setSelector(BlankDrawable.getInstance());
        mListView.setPadding(mContentPadding, 0, mContentPadding, mContentPadding - mActionPadding);
        mListView.setVerticalFadingEdgeEnabled(false);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);

        mAdapter = new InternalAdapter();
        mListView.setAdapter(mAdapter);
    }

    public Dialog items(CharSequence[] items, int selectedIndex){
        if(mListView == null)
            initListView();

        mMode = MODE_ITEMS;
        mAdapter.setItems(items, selectedIndex);
        setContentView(mListView);
        return this;
    }

    public Dialog multiChoiceItems(CharSequence[] items, int... selectedIndexes){
        if(mListView == null)
            initListView();

        mMode = MODE_MULTI_ITEMS;
        mAdapter.setItems(items, selectedIndexes);
        setContentView(mListView);
        return this;
    }

    public int[] getSelectedIndexes(){
        return mAdapter == null ? null : mAdapter.getSelectedIndexes();
    }

    public int getSelectedIndex(){
        return mAdapter == null ? -1 : mAdapter.getLastSelectedIndex();
    }

    private class InternalScrollView extends ScrollView{

        public InternalScrollView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);

            View child = getChildAt(0);
            showDivider(child != null && child.getMeasuredHeight() > getMeasuredHeight() - getPaddingTop() - getPaddingBottom());
        }
    }

    private class InternalListView extends ListView{

        public InternalListView(Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if(heightMode == MeasureSpec.UNSPECIFIED){
                if(mItemHeight != ViewGroup.LayoutParams.WRAP_CONTENT)
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemHeight * getAdapter().getCount(), MeasureSpec.EXACTLY);
            }

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);

            int totalHeight = 0;
            int childCount = getChildCount();

            for(int i = 0; i < childCount; i++)
                totalHeight += getChildAt(i).getMeasuredHeight();

            showDivider(totalHeight > getMeasuredHeight() || (totalHeight == getMeasuredHeight() && getAdapter().getCount() > childCount));
        }

    }

    private class InternalAdapter extends BaseAdapter implements CompoundButton.OnCheckedChangeListener{

        private CharSequence[] mItems;
        private boolean[] mSelected;
        private int mLastSelectedIndex;

        public void setItems(CharSequence[] items, int... selectedIndexes){
            mItems = items;

            if(mSelected == null ||  mSelected.length != items.length)
                mSelected = new boolean[items.length];

            for(int i = 0; i < mSelected.length; i++)
                mSelected[i] = false;

            if(selectedIndexes != null)
                for(int index : selectedIndexes) {
                    mSelected[index] = true;
                    mLastSelectedIndex = index;
                }

            notifyDataSetChanged();
        }

        public int getLastSelectedIndex(){
            return mLastSelectedIndex;
        }

        public int[] getSelectedIndexes(){
            int count = 0;
            for(int i = 0; i < mSelected.length; i++)
                if(mSelected[i])
                    count++;

            if(count == 0)
                return null;

            int[] result = new int[count];
            count = 0;
            for(int i = 0; i < mSelected.length; i++)
                if(mSelected[i]){
                    result[count] = i;
                    count++;
                }

            return result;
        }

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems == null ? 0 : mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CompoundButton v = (CompoundButton)convertView;
            if(v == null) {
                v = (mMode == MODE_MULTI_ITEMS) ? new CheckBox(parent.getContext(), null, 0, mCheckBoxStyle) : new RadioButton(parent.getContext(), null, 0, mRadioButtonStyle);
                if(mItemHeight != ViewGroup.LayoutParams.WRAP_CONTENT)
                    v.setMinHeight(mItemHeight);
                v.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);
                v.setTextAppearance(v.getContext(), mItemTextAppearance);
                v.setPadding(mContentPadding, 0, 0, 0);
            }

            v.setTag(position);
            v.setText(mItems[position]);
            if(v instanceof CheckBox)
                ((CheckBox) v).setCheckedImmediately(mSelected[position]);
            else
                ((RadioButton) v).setCheckedImmediately(mSelected[position]);

            v.setOnCheckedChangeListener(this);

            return v;
        }

        @Override
        public void onCheckedChanged(android.widget.CompoundButton v, boolean isChecked) {
            int position = (Integer)v.getTag();
            if(mSelected[position] != isChecked)
                mSelected[position] = isChecked;

            if(mMode == MODE_ITEMS && isChecked && mLastSelectedIndex != position){
                mSelected[mLastSelectedIndex] = false;

                CompoundButton child = (CompoundButton) mListView.getChildAt(mLastSelectedIndex - mListView.getFirstVisiblePosition());
                if(child != null)
                    child.setChecked(false);

                mLastSelectedIndex = position;
            }
        }
    }
}
