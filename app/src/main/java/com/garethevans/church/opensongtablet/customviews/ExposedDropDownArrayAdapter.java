package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ExposedDropDownArrayAdapter extends ArrayAdapter<String> {

    private final String TAG = "ExposedDropDownAdapter";

    public ExposedDropDownArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        // Because we have not passed in a reference to the exposed dropdown,
        // we will need to do this manually from the calling class directly to the exposed dropdown class
        super(context, resource, objects);
    }

    public ExposedDropDownArrayAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        // Because we have not passed in a reference to the exposed dropdown,
        // we will need to do this manually from the calling class directly to the exposed dropdown class
        super(context, resource, objects);
    }

    public ExposedDropDownArrayAdapter(@NonNull Context context, ExposedDropDown exposedDropDown, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        // Because we have passed in a reference to the exposed dropdown, pass across the arraylist
        // This allows the list to show the currently selected item when displaying popup
        exposedDropDown.setArray(context,objects);
    }

    public ExposedDropDownArrayAdapter(@NonNull Context context, ExposedDropDown exposedDropDown, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        // Because we have passed in a reference to the exposed dropdown, pass across the arraylist
        // This allows the list to show the currently selected item when displaying popup
        exposedDropDown.setArray(context,objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }

}
