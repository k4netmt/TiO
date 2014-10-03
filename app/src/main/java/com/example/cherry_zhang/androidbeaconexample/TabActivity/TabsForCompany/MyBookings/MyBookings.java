package com.example.cherry_zhang.androidbeaconexample.TabActivity.TabsForCompany.MyBookings;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.cherry_zhang.androidbeaconexample.ListAdapter.ListOfOfficeAdapter;
import com.example.cherry_zhang.androidbeaconexample.Models.OfficeItem;
import com.example.cherry_zhang.androidbeaconexample.R;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class MyBookings extends Fragment {

    TextView bookingDate;

    ProgressBar pv_progressBar;

    ListView lv_listOfNearbyOffices;
    ListOfOfficeAdapter adapter;
    ArrayList<OfficeItem> items;
    ArrayList<String> itemOfficeIds;
    ArrayList<String> itemBookingDates;

    MyBookings_Async load;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemOfficeIds = new ArrayList<String>();
        itemBookingDates = new ArrayList<String>();

        //initialize PARSE
        Parse.initialize(getActivity(), "TsVbzF7jXzY1C0o86V2xxAxgSxvy4jmbyykOabPl",
                "VzamwWm4WswbDFxrxos2oSerQ2Av4RM6J5mNnNgr");

        //initialization of variables
        items = new ArrayList<OfficeItem>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_office_list,container,false);

        lv_listOfNearbyOffices = (ListView) rootView.findViewById(R.id.lv_listOfNearbyOffices);



        pv_progressBar = (ProgressBar) rootView.findViewById(R.id.pv_progressBar);

        adapter = new ListOfOfficeAdapter(getActivity(), items,R.layout.list_item_booking);
        lv_listOfNearbyOffices.setAdapter(adapter);
        lv_listOfNearbyOffices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                bookingDate = (TextView) view.findViewById(R.id.bookingDate);

                if (bookingDate.getVisibility() == View.VISIBLE)
                {
                    bookingDate.setVisibility(View.GONE);
                }
                else
                {
                    bookingDate.setText(itemBookingDates.get(i));
                    bookingDate.setVisibility(View.VISIBLE);
                }

            }
        });

        load = new MyBookings_Async();
        load.execute();

        return rootView;
    }

    private class MyBookings_Async extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids)
        {

            Log.e("ListOfNearbyOffices","doing stuff in background");

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Booking");
            query.whereEqualTo("user", ParseUser.getCurrentUser());
            query.include("images");
            query.include("office.images");
            query.include("office");
            query.include("office.company");
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null && !parseObjects.isEmpty())
                    {
                        for (final ParseObject booking : parseObjects)
                        {
                            itemBookingDates.add(booking.getString("bookingStart"));
                            itemOfficeIds.add(booking.getObjectId());
                            final ParseObject office = booking.getParseObject("office");
                            ArrayList<ParseObject> images = (ArrayList<ParseObject>) office.get("images");
                            Log.e("", images.get(0).getObjectId());
                            ParseFile imageFile = images.get(0).getParseFile("image");
                            imageFile.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] imageBytes, ParseException e) {
                                    if (e == null)
                                    {
                                        Bitmap officeImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                        Log.e("is bitmap null","" + (officeImage == null));
                                        final String title = office.getString("name");
                                        final String address = office.getString("address");
                                        final String company = office.getParseObject("company").getString("name");
                                        final String time = booking.getDate("bookingStart") + " to " + booking.getDate("bookingEnd");

                                        Log.e("MyBookings", title + address + company + time);

                                        items.add(new OfficeItem(officeImage, title,
                                                address,
                                                company,
                                                time));
                                        adapter.notifyDataSetChanged();
                                        Log.e("MyBookings","done stuffs");
                                    }
                                    else
                                    {
                                        Log.e("MyBookings blah",e.getMessage());
                                    }
                                }
                            });
                        }
                    }
                    else if (e != null)
                    {
                        Log.e("MyBookings blah2",e.getMessage());
                    }
                    else if (parseObjects.isEmpty())
                    {
                        Log.e("MyBookings blah3","query empty");
                    }
                }
            });


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            super.onPostExecute(aVoid);
            adapter.notifyDataSetChanged();
            Log.e("number of children in listview: ", "" + lv_listOfNearbyOffices.getChildCount());
            pv_progressBar.setVisibility(View.GONE);
        }
    }
}
