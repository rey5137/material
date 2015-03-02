package com.rey.material.app;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.rey.material.demo.R;
import com.rey.material.drawable.DividerDrawable;
import com.rey.material.util.ThemeUtil;
import com.rey.material.util.ViewUtil;
import com.rey.material.widget.EditText;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rey on 3/2/2015.
 */
public class ContactEditText extends EditText{

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
        ContactSuggestionAdapter adapter = new ContactSuggestionAdapter(context);
        setAdapter(adapter);
    }

    static class ContactSuggestionAdapter extends BaseAdapter implements Filterable {

        private Context mContext;

        private static final String COLS[] = new String[]{
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

            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, recipient.lookupKey);
            Picasso.with(mContext)
                    .load(uri)
                    .placeholder(R.drawable.ic_user)
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
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name", recipient.name);
                    obj.put("number", recipient.number);
                } catch (JSONException e) {}                
                return obj.toString();
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
                            recipient.contactId = cursor.getLong(cursor.getColumnIndex(COLS[0]));
                            recipient.lookupKey = cursor.getString(cursor.getColumnIndex(COLS[1]));
                            recipient.name = cursor.getString(cursor.getColumnIndex(COLS[2]));
                            recipient.number = cursor.getString(cursor.getColumnIndex(COLS[3]));
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
}
