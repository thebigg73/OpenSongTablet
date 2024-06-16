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
import java.util.List;

public class BrowseHostAdapter extends RecyclerView.Adapter<HostViewHolder> {

    MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "BrowseHostAdapter";
    private ArrayList<HostItem> hostItems = new ArrayList<>();

    BrowseHostAdapter(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        setHasStableIds(false);
    }

    public void prepareItems(String[] items, String folder) {
        // Get a count of the original items (if any)
        int oldSize = getItemCount();
        hostItems = new ArrayList<>();

        for (String item : items) {
            HostItem hostItem = new HostItem();

            if (!item.endsWith("/")) {

                // Add the folder
                hostItem.setFolder(folder);

                switch (folder) {
                    case "Sets":
                        // No subfolder in Sets
                        hostItem.setSubfolder("");
                        // Add the filename as it is
                        hostItem.setFilename(item);
                        // Get the category and name
                        String[] bits = mainActivityInterface.getSetActions().getSetCategoryAndName(item);
                        hostItem.setCategory(bits[0]);
                        hostItem.setTitle(bits[1]);
                        // Add the tag as Category/Title
                        hostItem.setTag(hostItem.getCategory() + "/" + hostItem.getTitle());
                        break;

                    case "Profiles":
                        // No subfolder in Profiles
                        hostItem.setSubfolder("");
                        // Add the filename as it is
                        hostItem.setFilename(item);
                        hostItem.setCategory("");
                        hostItem.setTitle(item);
                        hostItem.setTag(item);
                        break;

                    case "Songs":
                        // Make sure we have a subfolder
                        String subfolder = item.substring(0, item.lastIndexOf("/"));
                        String filename = item.replace(subfolder + "/", "");
                        Log.d(TAG, "item:"+item+" subfolder:" + subfolder + " filename:" + filename);
                        hostItem.setSubfolder(subfolder);
                        hostItem.setFilename(filename);
                        hostItem.setTitle(filename);
                        hostItem.setCategory("");
                        hostItem.setTag(item);
                        break;
                }

                // Check if we already have this file
                Uri itemUri = mainActivityInterface.getStorageAccess().getUriForItem(folder, hostItem.getSubfolder(), item);
                hostItem.setExists(mainActivityInterface.getStorageAccess().uriExists(itemUri));


                Log.d(TAG, "hostItem:" + hostItem.getFolder() + " " + hostItem.getFilename());
                // Add this set item to the array
                hostItems.add(hostItem);
            }
        }

        changeSortOrder();

        // Notify the adapter of the changes
        mainActivityInterface.getMainHandler().post(() -> {
            if (oldSize > 0) {
                notifyItemRangeRemoved(0, oldSize);
            }
            notifyItemRangeInserted(0, getItemCount());
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
    public void onBindViewHolder(@NonNull HostViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.toString().equals("checked")) {
            position = holder.getAbsoluteAdapterPosition();

            HostItem hostItem = hostItems.get(position);
            // Decide if this value is selected
            holder.checkBox.setChecked(hostItem.getChecked());
        }
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

    public ArrayList<HostItem> getCheckedItems() {
        ArrayList<HostItem> checkedItems = new ArrayList<>();
        for (HostItem hostItem:hostItems) {
            if (hostItem.getChecked()) {
                checkedItems.add(hostItem);
            }
        }
        return checkedItems;
    }

    public void selectAll(boolean select) {
        // Changed all values to checked
        for (HostItem hostItem:hostItems) {
            hostItem.setChecked(select);
        }
        notifyItemRangeChanged(0, hostItems.size(),"checked");
    }

}
