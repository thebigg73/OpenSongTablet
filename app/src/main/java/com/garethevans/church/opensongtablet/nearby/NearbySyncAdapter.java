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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NearbySyncAdapter extends RecyclerView.Adapter<NeabyItemViewHolder> {

    // This adapter updates the recyclerview with the required items

    MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "NearbySyncAdapter";
    private final ArrayList<NearbySyncItem> nearbySyncItems = new ArrayList<>();
    private ArrayList<ShareableObject> shareableObjects = new ArrayList<>();
    private final SyncNearbyFragment syncNearbyFragment;
    private final SyncItemsFragment syncItemsFragment;
    private final String sync_newer, sync_older, sync_missing, sync_same, sync_uuid_mismatch;
    private final Context c;

    NearbySyncAdapter(Context c, SyncNearbyFragment syncNearbyFragment, SyncItemsFragment syncItemsFragment) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        setHasStableIds(false);
        this.syncNearbyFragment = syncNearbyFragment;
        this.syncItemsFragment = syncItemsFragment;
        sync_newer = c.getString(R.string.sync_newer);
        sync_older = c.getString(R.string.sync_older);
        sync_missing = c.getString(R.string.sync_missing);
        sync_same = c.getString(R.string.sync_same);
        sync_uuid_mismatch = c.getString(R.string.sync_uuid_mismatch);
        prepareItems();
    }

    public void prepareItems() {
        // Get a count of the original items (if any)
        int oldSize = getItemCount();
        nearbySyncItems.clear();
        shareableObjects.clear();
        if (oldSize>0) {
            try {
                mainActivityInterface.getMainHandler().post(() -> notifyItemRangeRemoved(0, oldSize));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG,"what:"+syncItemsFragment.getWhat());

        switch (syncItemsFragment.getWhat()) {
            case "sets":
                shareableObjects = syncNearbyFragment.getNearbyJson().getShareableSetObjects();
                break;
            case "profiles":
                shareableObjects = syncNearbyFragment.getNearbyJson().getShareableProfileObjects();
                break;
            case "songs":
            default:
                shareableObjects = syncNearbyFragment.getNearbyJson().getShareableSongObjects();
                break;
        }

        // Just in case the returned items are null...
        if (shareableObjects==null) {
            shareableObjects = new ArrayList<>();
        }

        for (ShareableObject shareableObject : shareableObjects) {
            // If this is a song, we need to check our version (if any)
            boolean matchingUuid = false;
            boolean newer = false;
            boolean older = false;
            boolean sameDate = false;
            boolean exists = false;
            String filename = shareableObject.getFilename();
            String folder = shareableObject.getFolder();

            NearbySyncItem nearbySyncItem = new NearbySyncItem();
            nearbySyncItem.setFilename(shareableObject.getFilename());

            if (syncItemsFragment.getWhat().equals("songs")) {
                nearbySyncItem.setTitle(shareableObject.getFilename());
                nearbySyncItem.setFolder(shareableObject.getFolder());

                // Get our version
                String[] songInfo = mainActivityInterface.getSQLiteHelper().getSongCreationInfo(folder, filename);

                // Compare the Uuid to check if we have the same song
                matchingUuid = songInfo[0].equals(shareableObject.getUuid());

                // Now check the lastModified, converted to millis
                long myLastModified = mainActivityInterface.getTimeTools().getMillisFromIsoTime(songInfo[1]);
                long theirLastModified = mainActivityInterface.getTimeTools().getMillisFromIsoTime(shareableObject.getLastModified());
                if (myLastModified>0 && myLastModified>theirLastModified) {
                    // Ours is the newest version
                    older = true;
                } else if (myLastModified<theirLastModified) {
                    // Theirs is the newest version
                    newer = true;
                } else if (myLastModified==theirLastModified) {
                    // They are the same date
                    sameDate = true;
                }

                exists = Boolean.parseBoolean(songInfo[2]);
                Log.d(TAG, "our song info (" + folder + "/" + filename + "): " + Arrays.toString(songInfo) + "  exists:" + exists + "  newer:" + newer + "  older:" + older + "  sameDate:"+sameDate+"  matchingUuid:"+matchingUuid);

            } else if (syncItemsFragment.getWhat().equals("sets")) {
                // If we have a category, then use this as the folder shown
                String category = mainActivityInterface.getMainfoldername();
                String title = filename;
                if (filename.contains(mainActivityInterface.getSetActions().getSetCategorySeparator())) {
                    String[] bits = filename.split(mainActivityInterface.getSetActions().getSetCategorySeparator());
                    category = bits[0];
                    title = bits[1];
                }
                nearbySyncItem.setTitle(title);
                nearbySyncItem.setFolder(category);

                // For sets we just show them all, with no comparision text other than new files
                Uri itemUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", filename);
                exists = mainActivityInterface.getStorageAccess().uriExists(itemUri);
                matchingUuid = true;  // Assume that matching filenames are the same item

            } else if (syncItemsFragment.getWhat().equals("profiles")) {
                // The title doesn't need the .xml extension
                nearbySyncItem.setTitle(filename.replace(".xml",""));
                nearbySyncItem.setFolder("");

                // For profiles we just show them all, with no comparision text other than new files
                Uri itemUri = mainActivityInterface.getStorageAccess().getUriForItem("Profiles", "", filename);
                exists = mainActivityInterface.getStorageAccess().uriExists(itemUri);
                matchingUuid = true;  // Assume that matching filenames are the same item
            }



            if (!exists) {
                nearbySyncItem.setComparisonText(sync_missing);
                nearbySyncItem.setSelected(true);
            } else {
                String extra = "";
                if (!matchingUuid) {
                    extra = "\n("+sync_uuid_mismatch+")";
                }
                // The same file, so we decide the status based on the lastModified
                if (newer) {
                    nearbySyncItem.setComparisonText(sync_newer+extra);
                    nearbySyncItem.setSelected(true);
                } else if (sameDate) {
                    nearbySyncItem.setComparisonText(sync_same+extra);
                } else if (older) {
                    nearbySyncItem.setComparisonText(sync_older+extra);
                }
            }

            // Add this set item to the array if new/updated or wanting to show all
            if (syncItemsFragment.getShowAll() || !exists || newer) {
                nearbySyncItems.add(nearbySyncItem);
            }
        }
        countItemsSelected();
    }

    @NonNull
    @Override
    public NeabyItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_sync_item, parent, false);
        return new NeabyItemViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(@NonNull NeabyItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (payloads.toString().equals("checked")) {
            position = holder.getAbsoluteAdapterPosition();

            NearbySyncItem nearbySyncItem = nearbySyncItems.get(position);
            // Decide if this value is selected
            holder.checkBox.setChecked(nearbySyncItem.getSelected());
        }
    }

    @Override
    public void onBindViewHolder(@NonNull NeabyItemViewHolder holder, int position) {
        // Prepare the view from the foundSets array
        position = holder.getAbsoluteAdapterPosition();

        // Get the values for this view
        if (position< nearbySyncItems.size()) {
            NearbySyncItem nearbySyncItem = nearbySyncItems.get(position);

            // Set filename
            holder.title.setText(nearbySyncItem.getTitle());
            holder.folder.setText(nearbySyncItem.getFolder());
            holder.folder.setVisibility(nearbySyncItem.getFolder()!=null && !nearbySyncItem.getFolder().isEmpty() ? View.VISIBLE:View.GONE);
            holder.comparison.setText(nearbySyncItem.getComparisonText());

            // Decide if this value is selected
            holder.checkBox.setChecked(nearbySyncItem.getSelected());

            // Set the listener for this item
            int finalPosition = position;
            holder.lastModified.setVisibility(View.GONE);
            holder.itemLayout.setOnClickListener(view -> {
                boolean isCheckedAlready = nearbySyncItems.get(finalPosition).getSelected();
                Log.d(TAG,"isCheckedAlready:"+isCheckedAlready);
                nearbySyncItems.get(finalPosition).setSelected(!isCheckedAlready);
                Log.d(TAG,"now:"+nearbySyncItems.get(finalPosition).getSelected());
                notifyItemChanged(finalPosition);
                countItemsSelected();
            });

        }
    }

    @Override
    public int getItemCount() {
        return nearbySyncItems.size();
    }

    public void changeSortOrder() {
        // Now do the sorting based on the user preference
        String setsSortOrder = mainActivityInterface.getPreferences().getMyPreferenceString("setsSortOrder","oldest");
        switch (setsSortOrder) {
            case "az":
                Collections.sort(nearbySyncItems, (NearbySyncItem a, NearbySyncItem z) -> a.getTitle().compareTo(z.getTitle()));
                break;
            case "za":
                Collections.sort(nearbySyncItems, (NearbySyncItem a, NearbySyncItem z) -> z.getTitle().compareTo(a.getTitle()));
                break;
        }
    }

    public ArrayList<NearbySyncItem> getCheckedItems() {
        ArrayList<NearbySyncItem> checkedItems = new ArrayList<>();
        for (NearbySyncItem nearbySyncItem : nearbySyncItems) {
            if (nearbySyncItem.getSelected()) {
                checkedItems.add(nearbySyncItem);
            }
        }
        return checkedItems;
    }

    public void selectAll(boolean select) {
        // Changed all values to checked
        for (NearbySyncItem nearbySyncItem : nearbySyncItems) {
            nearbySyncItem.setSelected(select);
        }
        notifyItemRangeChanged(0, nearbySyncItems.size(),"checked");
        countItemsSelected();
    }

    public void countItemsSelected() {
        int count = 0;
        for (NearbySyncItem nearbySyncItem : nearbySyncItems) {
            if (nearbySyncItem.getSelected()) {
                count++;
            }
        }
        if (syncItemsFragment!=null) {
            syncItemsFragment.setItemsSelected(count,getItemCount());
        }
    }
}
