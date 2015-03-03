package com.rey.material.app;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.MultiAutoCompleteTextView;

import com.rey.material.demo.R;
import com.rey.material.text.style.ContactChipSpan;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.EditText;
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

        ContactSuggestionAdapter adapter = new ContactSuggestionAdapter(context);
        setAdapter(adapter);
        setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        setMovementMethod(new RecipientMovementMethod());
    }

    @Override
    public void setTokenizer(MultiAutoCompleteTextView.Tokenizer t) {
        mTokenizer = t;
        super.setTokenizer(t);
    }

    @Override
    protected void replaceText(CharSequence text) {
        int start = mTokenizer.findTokenStart(getText(), getSelectionEnd());
        super.replaceText(text);
        int end = getSelectionEnd();

        Recipient recipient = mRecipientMap.get(text.toString());
        RecipientSpan span = new RecipientSpan(recipient);
        getText().setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    class ContactSuggestionAdapter extends BaseAdapter implements Filterable {

        private Context mContext;

        private final String COLS[] = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY : ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };

        private ArrayList<Recipient> mItems;

        public ContactSuggestionAdapter(Context context) {
            mContext = context;
        }

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
                v = new ContactView(mContext, null, 0, R.style.ContactView);

            Recipient recipient = (Recipient) getItem(position);
            v.setNameText(recipient.name);
            v.setAddressText(recipient.number);

            if(TextUtils.isEmpty(recipient.lookupKey))
                v.setAvatarResource(mDefaultAvatarId);
            else
                Picasso.with(mContext)
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
                    Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, COLS, selection, selectionArgs, sortOrder);
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

    class RecipientSpan extends ContactChipSpan implements Target{

        private Recipient mRecipient;

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

    class RecipientMovementMethod extends LinkMovementMethod{
        private RecipientSpan mTouchedSpan;

        @Override
        public boolean onTouchEvent(android.widget.TextView textView, Spannable spannable, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchedSpan = getTouchedSpan(textView, spannable, event);
                if (mTouchedSpan != null)
                    Selection.setSelection(spannable, spannable.getSpanStart(mTouchedSpan),spannable.getSpanEnd(mTouchedSpan));
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                RecipientSpan touchedSpan = getTouchedSpan(textView, spannable, event);
                if (mTouchedSpan != null && touchedSpan != mTouchedSpan) {
                    mTouchedSpan = null;
                    Selection.removeSelection(spannable);
                }
            } else {
                if (mTouchedSpan != null) {
                    super.onTouchEvent(textView, spannable, event);
                    System.out.println("click asd " + mTouchedSpan.getRecipient());
                }
                mTouchedSpan = null;
                Selection.removeSelection(spannable);
            }
            return true;
        }

        private RecipientSpan getTouchedSpan(android.widget.TextView textView, Spannable spannable, MotionEvent event) {
            int x = (int) event.getX() - textView.getTotalPaddingLeft() + textView.getScrollX();
            int y = (int) event.getY() - textView.getTotalPaddingTop() + textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            System.out.println("line: " + line + " off:" + off);

            RecipientSpan[] spans = spannable.getSpans(off, off, RecipientSpan.class);
            RecipientSpan touchedSpan = null;
            if (spans.length > 0)
                touchedSpan = spans[spans.length - 1];
            return touchedSpan;
        }

    }
}
