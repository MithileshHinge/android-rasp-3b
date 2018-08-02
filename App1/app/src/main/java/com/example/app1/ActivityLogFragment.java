package com.example.app1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Home on 13-07-2017.
 */
public class ActivityLogFragment extends Fragment {

    ActivityLogCustomAdapter logCustomAdapter;
    ExpandableListView expandableListView;
    //public static String datenow;

    List<String> listDataHeaders;
    HashMap<String, List<DatabaseRow>> listDataChild;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activitylog_fragment, container, false);
        getActivity().setTitle("Activity");

        NotifyService.db = new DatabaseHandler(getActivity());

        expandableListView= (ExpandableListView) v.findViewById(R.id.listView1);


        listDataHeaders = new ArrayList<>();
        listDataChild = new HashMap<>();


        List<DatabaseRow> databaseItems = NotifyService.db.getAllRows();

        Log.e("DatabaseRow size", String.valueOf(databaseItems.size()));

        String[] months = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};


        for (int i=databaseItems.size() - 1; i>=0; i--){
            //sort all items into different ArrayLists a/c to date

            DatabaseRow row = databaseItems.get(i);
            String datetime = row.getDateTime();
            System.out.println("ACTIVTIY LOG STRING DATE TIME:" + datetime);
            String date = datetime.substring(8,10) + " " + months[Integer.parseInt(datetime.substring(5,7)) - 1];

            List<DatabaseRow> tempItem;
            if (listDataChild.containsKey(date)){
                tempItem = listDataChild.get(date);
            }else {
                tempItem = new ArrayList<>();
                listDataHeaders.add(date);
            }
            tempItem.add(row);
            listDataChild.put(date, tempItem);
        }

        logCustomAdapter = new ActivityLogCustomAdapter(getContext(), listDataHeaders, listDataChild);

        expandableListView.setAdapter(logCustomAdapter);

        return v;
    }
}
