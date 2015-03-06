package com.rey.material.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.MultiAutoCompleteTextView;
import android.widget.PopupWindow;

import com.rey.material.demo.R;
import com.rey.material.text.style.ContactChipSpan;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.EditText;
import com.rey.material.widget.ListPopupWindow;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rey on 3/2/2015.
 */
public class ContactEditText extends EditText{

    private MultiAutoCompleteTextView.Tokenizer mTokenizer;

    private HashMap<String, Recipient> mRecipientMap;

    private int mDefaultAvatarId = R.drawable.ic_user;
    private int mSpanHeight;
    private int mSpanMaxWidth;
    private int mSpanPaddingLeft;
    private int mSpanPaddingRight;
    private Typeface mSpanTypeface;
    private int mSpanTextSize;
    private int mSpanTextColor;
    private int mSpanBackgroundColor;
    private int mSpanSpacing;

    private ContactReplaceAdapter mReplacementAdapter;
    private ListPopupWindow mReplacementPopup;
    private RecipientSpan mSelectedSpan;

    private RecipientSpan mTouchedSpan;

    public ContactEditText(Context context) {
        super(context);

        init(context, null, 0, 0);
    }

    public ContactEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, 0, 0);
    }

    public ContactEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, 0);
    }

    public ContactEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        mRecipientMap = new HashMap<>();

        mSpanHeight = ThemeUtil.dpToPx(context, 32);
        mSpanMaxWidth = ThemeUtil.dpToPx(context, 150);
        mSpanPaddingLeft = ThemeUtil.dpToPx(context, 8);
        mSpanPaddingRight = ThemeUtil.dpToPx(context, 12);
        mSpanTypeface = Typeface.DEFAULT;
        mSpanTextSize = ThemeUtil.spToPx(context, 14);
        mSpanTextColor = 0xFF000000;
        mSpanBackgroundColor = 0xFFE0E0E0;
        mSpanSpacing = ThemeUtil.dpToPx(context, 4);

        ContactSuggestionAdapter adapter = new ContactSuggestionAdapter();
        setAdapter(adapter);
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        addTextChangedListener(new ContactTextWatcher());

        setLineSpacing(mSpanSpacing, 1);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mTouchedSpan = getTouchedSpan(event);
                if (mTouchedSpan != null)
                    return true;
                break;
            case MotionEvent.ACTION_MOVE:
                if(mTouchedSpan != null){
                    if(mTouchedSpan != getTouchedSpan(event))
                        mTouchedSpan = null;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchedSpan != null) {
                    onSpanClick(mTouchedSpan);
                    mTouchedSpan = null;
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mTouchedSpan != null) {
                    mTouchedSpan = null;
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    private RecipientSpan getTouchedSpan(MotionEvent event) {
        int off = getOffsetForPosition(event.getX(), event.getY());

        RecipientSpan[] spans = getText().getSpans(off, off, RecipientSpan.class);

        if (spans.length > 0) {
            float x = convertToLocalHorizontalCoordinate(event.getX());
            for(int i = 0; i < spans.length; i++)
                if(spans[i].mX <= x && spans[i].mX + spans[i].mWidth >= x)
                    return spans[i];
        }

        return null;
    }

    @Override
    public void setTokenizer(MultiAutoCompleteTextView.Tokenizer t) {
        mTokenizer = t;
        super.setTokenizer(t);
    }

    @Override
    protected void replaceText(CharSequence text) {
        clearComposingText();

        int end = getSelectionEnd();
        int start = mTokenizer.findTokenStart(getText(), end);
        getText().replace(start, end, mTokenizer.terminateToken(text));

        end = getSelectionEnd();
        Recipient recipient = mRecipientMap.get(text.toString());
        getText().setSpan(new RecipientSpan(recipient), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void onSpanClick(RecipientSpan span){
        if(span != mSelectedSpan) {
            dismissReplacementPopup();
            mSelectedSpan = span;
            if(mReplacementAdapter == null)
                mReplacementAdapter = new ContactReplaceAdapter(mSelectedSpan.getRecipient());
            else
                mReplacementAdapter.setRecipient(mSelectedSpan.getRecipient());

            mReplacementPopup = new ListPopupWindow(getContext());
            mReplacementPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    mReplacementPopup = null;
                    mSelectedSpan = null;
                }
            });
            mReplacementPopup.setAdapter(mReplacementAdapter);
            mReplacementPopup.setAnchorView(this);
            mReplacementPopup.show();
        }
    }

    private void removeSpan(RecipientSpan span){
        Editable text = getText();
        int start = text.getSpanStart(span);
        int end = text.getSpanEnd(span);
        text.delete(start, end);
        text.removeSpan(span);
    }

    private void replaceSpan(RecipientSpan span, Recipient newRecipient){
        Editable text = getText();
        int start = text.getSpanStart(span);
        int end = text.getSpanEnd(span);
        String replace = newRecipient.number;
        text.replace(start, end - 2, newRecipient.number, 0, replace.length());
        span.setRecipient(newRecipient);
        text.setSpan(span, start, start + replace.length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void dismissReplacementPopup(){
        if(mReplacementPopup != null && mReplacementPopup.isShowing()){
            mReplacementPopup.dismiss();
            mReplacementPopup = null;
        }
    }

    class ContactSuggestionAdapter extends BaseAdapter implements Filterable {

        private final String COLS[] = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };

        private ArrayList<Recipient> mItems;

        public ContactSuggestionAdapter() {}

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems == null ? null : mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactView v = (ContactView)convertView;
            if (v == null)
                v = new ContactView(getContext(), null, 0, R.style.ContactView);

            Recipient recipient = (Recipient) getItem(position);
            v.setNameText(recipient.name);
            v.setAddressText(recipient.number);

            if(TextUtils.isEmpty(recipient.lookupKey))
                v.setAvatarResource(mDefaultAvatarId);
            else
                Picasso.with(getContext())
                        .load(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, recipient.lookupKey))
                        .placeholder(mDefaultAvatarId)
                        .into(v);

            return v;
        }

        @Override
        public Filter getFilter() {
            return contactFilter;
        }

        Filter contactFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                Recipient recipient = (Recipient) resultValue;
                mRecipientMap.put(recipient.number, recipient);
                return recipient.number;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint != null) {
                    String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? OR " + COLS[2] + " LIKE ?";
                    String[] selectionArgs = new String[]{"%" + constraint + "%", "%" + constraint + "%"};
                    String sortOrder = COLS[2] + " COLLATE LOCALIZED ASC";
                    Cursor cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, COLS, selection, selectionArgs, sortOrder);
                    if (cursor.getCount() > 0) {
                        ArrayList<Recipient> values = new ArrayList<>();
                        while (cursor.moveToNext()) {
                            Recipient recipient = new Recipient();
                            recipient.contactId = cursor.getLong(0);
                            recipient.lookupKey = cursor.getString(1);
                            recipient.name = cursor.getString(2);
                            recipient.number = cursor.getString(3);
                            values.add(recipient);
                        }

                        results.values = values;
                        results.count = values.size();
                    }
                    cursor.close();
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mItems = (ArrayList<Recipient>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    class ContactReplaceAdapter extends BaseAdapter implements OnClickListener {

        Recipient[] mItems;

        private final String COLS[] = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };

        public ContactReplaceAdapter(Recipient recipient){
            queryNumber(recipient);
        }

        public void setRecipient(Recipient recipient){
            queryNumber(recipient);
        }

        private void queryNumber(Recipient recipient){
            String selection = ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + "=?";
            String[] selectionArgs = new String[]{recipient.lookupKey};
            Cursor cursor = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, COLS, selection, selectionArgs, null);
            if (cursor.getCount() > 0) {
                mItems = new Recipient[cursor.getCount()];
                int index = 1;
                while (cursor.moveToNext()) {
                    long contactId = cursor.getLong(0);
                    String number = cursor.getString(1);

                    if(number.equals(recipient.number))
                        mItems[0] = recipient;
                    else{
                        Recipient newRecipient = new Recipient();
                        newRecipient.contactId = contactId;
                        newRecipient.lookupKey = recipient.lookupKey;
                        newRecipient.name = recipient.name;
                        newRecipient.number = number;
                        mItems[index] = newRecipient;
                        index++;
                    }
                }
            }
            cursor.close();

            notifyDataSetChanged();
        }

        @Override
        public void onClick(View v) {
            int position = (Integer)v.getTag();
            if(position == 0)
                removeSpan(mSelectedSpan);
            else
                replaceSpan(mSelectedSpan, (Recipient)mReplacementAdapter.getItem(position));

            Selection.setSelection(getText(), getText().length());

            dismissReplacementPopup();
        }

        @Override
        public int getCount() {
            return mItems == null ? 0 : mItems.length;
        }

        @Override
        public Object getItem(int position) {
            return mItems == null ? null : mItems[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position == 0 ? 0 : 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactView v = (ContactView)convertView;
            if(v == null) {
                v = new ContactView(getContext(), null, 0, position == 0 ? R.style.SelectedContactView : R.style.ReplacementContactView);
                v.setOnClickListener(this);
            }

            v.setTag(position);

            Recipient recipient = (Recipient)getItem(position);
            v.setNameText(position == 0 ? recipient.name : null);
            v.setAddressText(recipient.number);

            if(TextUtils.isEmpty(recipient.lookupKey))
                v.setAvatarResource(mDefaultAvatarId);
            else
                Picasso.with(getContext())
                        .load(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, recipient.lookupKey))
                        .placeholder(mDefaultAvatarId)
                        .into(v);

            return v;
        }
    }

    class RecipientSpan extends ContactChipSpan implements Target{

        private Recipient mRecipient;
        int mWidth;
        float mX;

        public RecipientSpan(Recipient recipient) {
            super(TextUtils.isEmpty(recipient.name) ? recipient.number : recipient.name,
                    mSpanHeight, mSpanMaxWidth, mSpanPaddingLeft, mSpanPaddingRight, mSpanTypeface, mSpanTextColor, mSpanTextSize, mSpanBackgroundColor);
            mRecipient = recipient;

            if(TextUtils.isEmpty(recipient.lookupKey))
                setImageResource(mDefaultAvatarId);
            else
                Picasso.with(getContext())
                        .load(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, recipient.lookupKey))
                        .placeholder(mDefaultAvatarId)
                        .into(this);
        }

        public void setRecipient(Recipient recipient){
            mRecipient = recipient;
        }

        public Recipient getRecipient(){
            return mRecipient;
        }

        public void setImageResource(int id){
            if(id == 0)
                return;

            Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), id);
            setImage(bm);
        }

        public void setImageDrawable(Drawable drawable) {
            if(drawable == null)
                return;

            if (drawable instanceof BitmapDrawable)
                setImage(((BitmapDrawable) drawable).getBitmap());
            else{
                Bitmap bm = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bm);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                setImage(bm);
            }
        }

        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            mWidth = super.getSize(paint, text, start, end, fm) + mSpanSpacing;
            return mWidth;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            mX = x;
            super.draw(canvas, text, start, end, x, top, y, bottom, paint);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            setImage(bitmap);
            ContactEditText.this.invalidate();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            setImageDrawable(errorDrawable);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            setImageDrawable(placeHolderDrawable);
        }
    }

    class ContactTextWatcher implements TextWatcher{

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            dismissReplacementPopup();
        }

    }
}
