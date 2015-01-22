package com.rey.material.demo;

import com.rey.material.drawable.ContactChipDrawable;
import com.rey.material.text.style.ContactChipSpan;
import com.rey.material.util.ThemeUtil;
import com.rey.material.widget.EditText;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import static android.provider.ContactsContract.CommonDataKinds.Phone;

public class TextfieldFragment extends Fragment{

	public static TextfieldFragment newInstance(){
		TextfieldFragment fragment = new TextfieldFragment();
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_textfield, container, false);
				
		final EditText et_helper = (EditText)v.findViewById(R.id.textfield_et_helper);
		
		et_helper.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
					et_helper.setError("Password is incorrect.");
				
				return false;
			}
			
		});
		
		et_helper.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					et_helper.setError(null);
			}
			
		});
		
		final EditText et_helper_error = (EditText)v.findViewById(R.id.textfield_et_helper_error);
		
		et_helper_error.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP)
					et_helper_error.setError("Password is incorrect.");
				
				return false;
			}
			
		});
		
		et_helper_error.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
					et_helper_error.setError(null);
			}
			
		});

        EditText a = (EditText) v.findViewById(R.id.textfield_tv);
        ContactAdapter adapter = new ContactAdapter(getActivity());
        a.setAdapter(adapter);

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


    static class ContactAdapter extends BaseAdapter implements Filterable{

        private Context mContext;

        private static final String COLS[] = new String[]{
                Phone.CONTACT_ID,
                Phone.LOOKUP_KEY,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? Phone.DISPLAY_NAME_PRIMARY : Phone.DISPLAY_NAME,
                Phone.NUMBER,
        };

        private ArrayList<ContactData> mItems;

        public ContactAdapter(Context context){
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
            View v = convertView;
            if(v == null)
                v = LayoutInflater.from(mContext).inflate(R.layout.row_drawer, parent, false);

            ContactData data = (ContactData)getItem(position);
            ((TextView)v).setText(data.displayName + "-" + data.phoneNumber);

            return v;
        }

        @Override
        public Filter getFilter() {
            return contactFilter;
        }

        Filter contactFilter = new Filter() {
            @Override
            public CharSequence convertResultToString(Object resultValue) {
                ContactData data = (ContactData)resultValue;
                return data.displayName + " " + data.phoneNumber;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if(constraint != null){
                    String selection = Phone.NUMBER + " LIKE ? OR " + COLS[2] + " LIKE ?";
                    String[] selectionArgs = new String[]{"%" + constraint + "%", "%" + constraint + "%"};
                    String sortOrder = COLS[2] + " COLLATE LOCALIZED ASC";
                    Cursor cursor = mContext.getContentResolver().query(Phone.CONTENT_URI, COLS, selection, selectionArgs, sortOrder);
                    if(cursor.getCount() > 0){
                        ArrayList<ContactData> values = new ArrayList<>();
                        while(cursor.moveToNext()){
                            ContactData data = new ContactData();
                            data.lockupKey = cursor.getString(cursor.getColumnIndex(COLS[1]));
                            data.displayName = cursor.getString(cursor.getColumnIndex(COLS[2]));
                            data.phoneNumber = cursor.getString(cursor.getColumnIndex(COLS[3]));
                            values.add(data);
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
                mItems = (ArrayList<ContactData>)results.values;
                notifyDataSetChanged();
            }
        };

        static class ContactData{
            String lockupKey;
            String displayName;
            String phoneNumber;
        }
    }
}
