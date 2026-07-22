package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class ExposedDropDownArrayAdapter extends ArrayAdapter<String> {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ExposedDropDownAdapter";
    private final MainActivityInterface mainActivityInterface;

    public ExposedDropDownArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects) {
        // Because we have not passed in a reference to the exposed dropdown,
        // we will need to do this manually from the calling class directly to the exposed dropdown class
        super(context, resource, objects);
        mainActivityInterface = (MainActivityInterface) context;
    }

    public ExposedDropDownArrayAdapter(@NonNull Context context, int resource, @NonNull String[] objects) {
        // Because we have not passed in a reference to the exposed dropdown,
        // we will need to do this manually from the calling class directly to the exposed dropdown class
        super(context, resource, objects);
        mainActivityInterface = (MainActivityInterface) context;
    }

    public ExposedDropDownArrayAdapter(@NonNull Context context, ExposedDropDown exposedDropDown, int resource, @NonNull String[] objects) {
        super(context, resource, objects);
        // Because we have passed in a reference to the exposed dropdown, pass across the arraylist
        // This allows the list to show the currently selected item when displaying popup
        mainActivityInterface = (MainActivityInterface) context;
        exposedDropDown.setArray(context,objects);
    }

    public ExposedDropDownArrayAdapter(@NonNull Context context, ExposedDropDown exposedDropDown, int resource, @NonNull ArrayList<String> objects) {
        super(context, resource, objects);
        // Because we have passed in a reference to the exposed dropdown, pass across the arraylist
        // This allows the list to show the currently selected item when displaying popup
        mainActivityInterface = (MainActivityInterface) context;
        exposedDropDown.setArray(context, objects);
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

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        MyMaterialSimpleTextView text = view.findViewById(R.id.popupText);
        text.setTextColor(mainActivityInterface.getPalette().textColor); // text color for dropdown items
        return view;
    }
}
