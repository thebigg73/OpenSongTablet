package com.garethevans.church.opensongtablet.nearby;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.Collections;

public class BrowseHostAdapter extends RecyclerView.Adapter<HostViewHolder> {

    MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BrowseHostAdapter";
    private ArrayList<HostItem> hostItems = new ArrayList<>();
    private final ArrayList<String> checkedItems = new ArrayList<>();
    private final String whatView;

    BrowseHostAdapter(Context c, String whatView) {
        mainActivityInterface = (MainActivityInterface) c;
        this.whatView = whatView;
        setHasStableIds(false);
        Log.d(TAG,"whatView:"+whatView);
    }

    public void prepareItems(String[] items) {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            // Get a count of the original items (if any)
            int oldSize = getItemCount();
            hostItems = new ArrayList<>();

            for (String item : items) {
                HostItem hostItem = new HostItem();

                // Add the filename as it is
                hostItem.setFilename(item);

                // Get the category and name
                String[] bits = mainActivityInterface.getSetActions().getSetCategoryAndName(item);
                hostItem.setCategory(bits[0]);
                hostItem.setTitle(bits[1]);

                // Add the folder as "Sets"
                hostItem.setFolder("Sets");

                // Add the subfolder as ""
                hostItem.setSubfolder("");

                // Add the tag as Category/Title
                hostItem.setTag(hostItem.getCategory() + "/" + hostItem.getTitle());

                // Check if we already have this file
                Uri itemUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets","",item);
                hostItem.setExists(mainActivityInterface.getStorageAccess().uriExists(itemUri));

                // Set the set identifier
                hostItem.setIdentifier(mainActivityInterface.getSetActions().getItemStart() +
                        item + mainActivityInterface.getSetActions().getItemEnd());

                // Set the values as checked based on if it is in the checkedItems array
                hostItem.setChecked(checkedItems.contains(hostItem.getIdentifier()));

                // Add this set item to the array
                hostItems.add(hostItem);
            }

            changeSortOrder();

            // Notify the adapter of the changes
            mainActivityInterface.getMainHandler().post(() -> {
                if (oldSize > 0) {
                    notifyItemRangeRemoved(0, oldSize);
                }
                notifyItemRangeInserted(0, getItemCount());
            });
        });
    }
    @NonNull
    @Override
    public HostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_checkbox_list_item, parent, false);
        return new HostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position) {
        // Prepare the view from the foundSets array
        position = holder.getAbsoluteAdapterPosition();

        // Get the values for this view
        if (position<hostItems.size()) {
            HostItem hostItem = hostItems.get(position);

            // Set filename

            // Decide if this value is selected
            holder.checkBox.setChecked(hostItem.getChecked());

            // Set the listener for this item
            int finalPosition = position;
            holder.itemLayout.setOnClickListener(view -> {
                // Set the item checked value as the opposite to what it currently was
                hostItems.get(finalPosition).setChecked(!hostItems.get(finalPosition).getChecked());
                notifyItemChanged(finalPosition);
                if (hostItems.get(finalPosition).getChecked()) {
                    // Add the item if it isn't already there
                    if (!checkedItems.contains(hostItems.get(finalPosition).getIdentifier())) {
                        checkedItems.add(hostItems.get(finalPosition).getIdentifier());
                    }
                } else {
                    // Remove the item if it isn't already there
                    checkedItems.remove(hostItems.get(finalPosition).getIdentifier());
                }
            });
            holder.itemName.setText(hostItem.getTag());
        }
    }

    @Override
    public int getItemCount() {
        return hostItems.size();
    }

    public void changeSortOrder() {
        // Now do the sorting based on the user preference
        String setsSortOrder = mainActivityInterface.getPreferences().getMyPreferenceString("setsSortOrder","oldest");
        switch (setsSortOrder) {
            case "az":
                Collections.sort(hostItems, (HostItem a, HostItem z) -> a.getTitle().compareTo(z.getTitle()));
                break;
            case "za":
                Collections.sort(hostItems, (HostItem a, HostItem z) -> z.getTitle().compareTo(a.getTitle()));
                break;
        }
        mainActivityInterface.getMainHandler().post(() -> notifyItemRangeChanged(0,getItemCount()));
    }

    public ArrayList<String> getCheckedItems() {
        return checkedItems;
    }

}
