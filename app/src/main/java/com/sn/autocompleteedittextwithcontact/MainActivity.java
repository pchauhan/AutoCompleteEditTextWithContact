package com.sn.autocompleteedittextwithcontact;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import java.util.ArrayList;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    EditText mEdtPhoneNumber ;
    AutoCompleteTextView mAutoCompleteTextView ;
    public ArrayList<Contact_Object> arrContact = new ArrayList<Contact_Object>();
    private ContactAdapter mContactAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEdtPhoneNumber = (EditText)findViewById(R.id.edtPhoneNumber);
        mAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);

        mContactAdapter =  new ContactAdapter(this ,R.layout.contact_list_item,arrContact);
        mAutoCompleteTextView.setThreshold(1);

        mAutoCompleteTextView.setAdapter(mContactAdapter);

    }
    public ArrayList<Contact_Object> getPhoneNumber(String name, Context context) {
        ArrayList<Contact_Object> arrayList = new ArrayList<Contact_Object>();
        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'" + name +"%'";
        String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER,ContactsContract.CommonDataKinds.Phone.TYPE,ContactsContract.CommonDataKinds.Phone.PHOTO_URI};
        Cursor cur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, null, null);
        while (cur.moveToNext())
        {
            Contact_Object contactObject = new Contact_Object();
            contactObject.name =  cur.getString(0);
            contactObject.mobileNumber =  cur.getString(1);
            contactObject.type =  cur.getInt(2);
            contactObject.uri =  cur.getString(3);
            arrayList.add(contactObject);

        }

        cur.close();
        return arrayList;
    }
    public class ContactAdapter extends ArrayAdapter<Contact_Object> {

        private ArrayList<Contact_Object> items;

        private int viewResourceId;

        public ContactAdapter(Context context, int viewResourceId, ArrayList<Contact_Object> items) {
            super(context, viewResourceId, items);
            this.items = items;

            this.viewResourceId = viewResourceId;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View v = convertView;
            final  ViewHolder viewHolder ;
            if (v == null) {
                viewHolder = new ViewHolder();
                LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(viewResourceId, null);

                viewHolder.tvName = (TextView) v.findViewById(R.id.tvName);
                viewHolder.tvMobileNumber = (TextView) v.findViewById(R.id.tvMobileNumber);
                viewHolder.imgProfile = (ImageView) v.findViewById(R.id.imgProfile);


                v.setTag(viewHolder);

            }else{

                viewHolder = (ViewHolder)v.getTag();

            }
            final  Contact_Object customer = items.get(position);
            String type = "Other" ;
            switch (customer.type) {
                case Phone.TYPE_HOME:
                    type = "Home";
                    break;
                case Phone.TYPE_MOBILE:
                    type = "Mobile";
                    break;
                case Phone.TYPE_WORK:
                    type = "Work";
                    break;
            }

            viewHolder.tvName.setText(customer.name+" ("+type+")");
            viewHolder.tvMobileNumber.setText(customer.mobileNumber);

            if(customer.uri != null){
                Uri u = Uri.parse( customer.uri) ;
                if (u != null) {
                    viewHolder.imgProfile.setImageURI(u);
                } else {
                    viewHolder.imgProfile.setImageResource(R.mipmap.ic_launcher);
                }
            }else{
                viewHolder.imgProfile.setImageResource(R.mipmap.ic_launcher);
            }

            v.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Contact_Object contactObject = customer ;
                    String number = contactObject.mobileNumber ;
                    mAutoCompleteTextView.setText(viewHolder.tvName.getText());
                    mAutoCompleteTextView.dismissDropDown();
                    mEdtPhoneNumber.setText(PhoneNumberUtils.formatNumber(number));

                }
            });

            return v;
        }

        @Override
        public Filter getFilter() {
            return nameFilter;
        }
        public class ViewHolder{
            public TextView tvName,tvMobileNumber ;
            public ImageView imgProfile ;
        }

        Filter nameFilter = new Filter() {
            @Override
            public String convertResultToString(Object resultValue) {
                String str = ((Contact_Object)(resultValue)).name;
                return str;
            }
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if(constraint != null) {
                    ArrayList<Contact_Object> suggestions = getPhoneNumber(constraint.toString(), getApplicationContext());

                    int size = arrContact.size();
                    System.out.println("suggestions="+suggestions.size() + "=arrContact="+size);
                    FilterResults filterResults = new FilterResults();
                    filterResults.values = suggestions;
                    filterResults.count = suggestions.size();
                    return filterResults;
                } else{

                }
                return new FilterResults();
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<Contact_Object> filteredList = (ArrayList<Contact_Object>) results.values;
                if(results != null && results.count > 0) {
                    clear();
                    for (Contact_Object c : filteredList) {
                        add(c);
                    }
                    notifyDataSetChanged();
                }
            }
        };

    }
}
