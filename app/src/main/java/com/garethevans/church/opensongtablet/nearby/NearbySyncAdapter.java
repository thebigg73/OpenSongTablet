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
    private final String sync_newer, sync_older, sync_missing, sync_same, sync_uuid_mismatch,
            sync_exists, current_set_string, what;
    private String folderChosen = "";
    private boolean accessingTheArray = false;   // To avoid concurrent

    NearbySyncAdapter(Context c, SyncNearbyFragment syncNearbyFragment, SyncItemsFragment syncItemsFragment, String what) {
        mainActivityInterface = (MainActivityInterface) c;
        setHasStableIds(false);
        this.syncNearbyFragment = syncNearbyFragment;
        this.syncItemsFragment = syncItemsFragment;
        sync_newer = c.getString(R.string.sync_newer);
        sync_older = c.getString(R.string.sync_older);
        sync_missing = c.getString(R.string.sync_missing);
        sync_same = c.getString(R.string.sync_same);
        sync_uuid_mismatch = c.getString(R.string.sync_uuid_mismatch);
        sync_exists = c.getString(R.string.sync_exists);
        current_set_string = c.getString(R.string.set_current);
        this.what = what;
        accessingTheArray = false;
    }

    public void prepareItems() {
        Log.d(TAG,"prepareItems()");
        if (!accessingTheArray) {
            accessingTheArray = true;  // Avoid concurrent edit
            syncNearbyFragment.announceNotPrepared(what);
            mainActivityInterface.getThreadPoolExecutor().execute(() -> {
                if (syncNearbyFragment.getNearbyJson() != null) {
                    // Get a count of the original items (if any)
                    int oldSize = getItemCount();
                    nearbySyncItems.clear();
                    shareableObjects.clear();
                    if (oldSize > 0) {
                        try {
                            mainActivityInterface.getMainHandler().post(() -> notifyItemRangeRemoved(0, oldSize));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (syncNearbyFragment.getNearbyJson() != null) {
                        switch (syncItemsFragment.getWhat()) {
                            case "sets":
                                if (syncNearbyFragment.getNearbyJson().getShareableSetObjects() != null) {
                                    shareableObjects.addAll(syncNearbyFragment.getNearbyJson().getShareableSetObjects());
                                }
                                break;
                            case "profiles":
                                if (syncNearbyFragment.getNearbyJson().getShareableProfileObjects() != null) {
                                    shareableObjects.addAll(syncNearbyFragment.getNearbyJson().getShareableProfileObjects());
                                }
                                break;
                            case "songs":
                            default:
                                if (syncNearbyFragment.getNearbyJson().getShareableSongObjects() != null) {
                                    if (folderChosen == null || folderChosen.isEmpty()) {
                                        shareableObjects.addAll(syncNearbyFragment.getNearbyJson().getShareableSongObjects());
                                    } else {
                                        // Go through each item and add the song if it matches the folder above
                                        for (int i = 0; i < syncNearbyFragment.getNearbyJson().getShareableSongObjects().size(); i++) {
                                            ShareableObject tempShareableObject = syncNearbyFragment.getNearbyJson().getShareableSongObjects().get(i);
                                            if (tempShareableObject.getFolder().equals(folderChosen)) {
                                                shareableObjects.add(tempShareableObject);
                                            }
                                        }
                                    }
                                }
                                break;
                        }
                    }

                    // Just in case the returned items are null...
                    if (shareableObjects == null) {
                        shareableObjects = new ArrayList<>();
                    }

                    // Keep the view updated that we are still working
                    //syncNearbyFragment.announceNotPrepared(what);

                    for (int i = 0; i < shareableObjects.size(); i++) {
                        ShareableObject shareableObject = shareableObjects.get(i);
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

                        switch (syncItemsFragment.getWhat()) {
                            case "songs":
                                nearbySyncItem.setTitle(shareableObject.getFilename());
                                nearbySyncItem.setFolder(shareableObject.getFolder());

                                // Get our version
                                String[] songInfo = mainActivityInterface.getSQLiteHelper().getSongCreationInfo(folder, filename);

                                // Compare the Uuid to check if we have the same song
                                matchingUuid = songInfo[0].equals(shareableObject.getUuid());

                                // Now check the lastModified, converted to millis
                                long myLastModified = mainActivityInterface.getTimeTools().getMillisFromIsoTime(songInfo[1]);
                                long theirLastModified = mainActivityInterface.getTimeTools().getMillisFromIsoTime(shareableObject.getLastModified());
                                if (myLastModified > 0 && myLastModified > theirLastModified) {
                                    // Ours is the newest version
                                    older = true;
                                } else if (myLastModified < theirLastModified) {
                                    // Theirs is the newest version
                                    newer = true;
                                } else if (myLastModified == theirLastModified) {
                                    // They are the same date
                                    sameDate = true;
                                }
                                exists = Boolean.parseBoolean(songInfo[2]);
                                break;
                            case "sets": {
                                // If we have a category, then use this as the folder shown
                                String category = mainActivityInterface.getMainfoldername();
                                String title = filename;
                                if (filename.contains(mainActivityInterface.getSetActions().getSetCategorySeparator())) {
                                    String[] bits = filename.split(mainActivityInterface.getSetActions().getSetCategorySeparator());
                                    category = bits[0];
                                    title = bits[1];
                                }
                                if (title.equals(mainActivityInterface.getNearbyActions().currentSetFile)) {
                                    title = current_set_string;
                                    filename = mainActivityInterface.getNearbyActions().currentSetFile;
                                    category = "";
                                }
                                nearbySyncItem.setTitle(title);
                                nearbySyncItem.setFolder(category);

                                // For sets we just show them all, with no comparision text other than new files
                                Uri itemUri = mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", filename);
                                if (!filename.equals(mainActivityInterface.getNearbyActions().currentSetFile)) {
                                    exists = mainActivityInterface.getStorageAccess().uriExists(itemUri);
                                }
                                matchingUuid = true;  // Assume that matching filenames are the same item
                                break;
                            }
                            case "profiles": {
                                // The title doesn't need the .xml extension
                                nearbySyncItem.setTitle(filename.replace(".xml", ""));
                                nearbySyncItem.setFolder("");

                                // For profiles we just show them all, with no comparision text other than new files
                                Uri itemUri = mainActivityInterface.getStorageAccess().getUriForItem("Profiles", "", filename);
                                exists = mainActivityInterface.getStorageAccess().uriExists(itemUri);
                                matchingUuid = true;  // Assume that matching filenames are the same item

                                break;
                            }
                        }

                        if (!exists) {
                            nearbySyncItem.setComparisonText(sync_missing);
                            nearbySyncItem.setSelected(true);
                        } else {
                            String extra = "";
                            if (!matchingUuid) {
                                extra = "\n(" + sync_uuid_mismatch + ")";
                            }
                            // The same file, so we decide the status based on the lastModified
                            if (newer) {
                                nearbySyncItem.setComparisonText(sync_newer + extra);
                                nearbySyncItem.setSelected(true);
                            } else if (sameDate) {
                                nearbySyncItem.setComparisonText(sync_same + extra);
                            } else if (older) {
                                nearbySyncItem.setComparisonText(sync_older + extra);
                            } else {
                                nearbySyncItem.setComparisonText(sync_exists);
                            }
                        }

                        // Add this set item to the array if new/updated or wanting to show all
                        if (!syncItemsFragment.getShowNewUpdate() || !exists || newer) {
                            nearbySyncItems.add(nearbySyncItem);
                        }
                    }
                }
                accessingTheArray = false;
                Log.d(TAG,"Getting here: " + getItemCount() + " items" + " for "+what);
                mainActivityInterface.getMainHandler().post(() -> {
                    notifyItemRangeChanged(0, nearbySyncItems.size());
                    countItemsSelected();
                    syncNearbyFragment.announcePrepared(what);
                });
            });
        }
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
    public void onBindViewHolder(@NonNull NeabyItemViewHolder holder, int thisposition) {
        // Prepare the view from the foundSets array
        int position = holder.getAbsoluteAdapterPosition();

        // Get the values for this view
        if (position < nearbySyncItems.size() && position>=0) {
            NearbySyncItem nearbySyncItem = nearbySyncItems.get(position);

            // Set filename
            holder.title.setText(nearbySyncItem.getTitle());
            holder.folder.setText(nearbySyncItem.getFolder());
            holder.folder.setVisibility(nearbySyncItem.getFolder() != null && !nearbySyncItem.getFolder().isEmpty() ? View.VISIBLE : View.GONE);
            holder.comparison.setText(nearbySyncItem.getComparisonText());

            // Decide if this value is selected
            holder.checkBox.setChecked(nearbySyncItem.getSelected());

            // Set the listener for this item
            holder.lastModified.setVisibility(View.GONE);
            holder.itemLayout.setOnClickListener(view -> {
                boolean isCheckedAlready = nearbySyncItems.get(position).getSelected();
                nearbySyncItems.get(position).setSelected(!isCheckedAlready);
                notifyItemChanged(position);
                countItemsSelected();
            });
        }
    }

    @Override
    public int getItemCount() {
        return nearbySyncItems.size();
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
        if (!accessingTheArray) {
            for (int i = 0; i < nearbySyncItems.size(); i++) {
                NearbySyncItem nearbySyncItem = nearbySyncItems.get(i);
                if (nearbySyncItem.getSelected()) {
                    count++;
                }
            }
            if (syncItemsFragment != null) {
                syncItemsFragment.setItemsSelected(count, getItemCount());
            }
        }
    }

    public NearbyJson getRequestedItems(String filename) {
        // Gather the items that have been selected
        // The filename indicates 'what'
        NearbyJson nearbyJsonRequest = new NearbyJson();
        nearbyJsonRequest.setFilename(filename);
        nearbyJsonRequest.setWhat(mainActivityInterface.getNearbyActions().syncRequestContent);
        nearbyJsonRequest.setDeviceToAction(syncNearbyFragment.getChosenDevice());
        nearbyJsonRequest.setDeviceSending(mainActivityInterface.getNearbyActions().getNearbyConnectionManagement().getDeviceId());
        ArrayList<ShareableObject> requestedSongs = new ArrayList<>();
        ArrayList<ShareableObject> requestedSets = new ArrayList<>();
        ArrayList<ShareableObject> requestedProfiles = new ArrayList<>();

        for (int i=0; i<nearbySyncItems.size(); i++) {
            NearbySyncItem item = nearbySyncItems.get(i);
            if (item.getSelected()) {
                ShareableObject requestedObject = new ShareableObject();
                switch (syncItemsFragment.getWhat()) {
                    case "sets":
                        requestedObject.setFilename(item.getFilename());
                        requestedObject.setFolder("../Sets");
                        requestedSets.add(requestedObject);
                        break;
                    case "profiles":
                        requestedObject.setFilename(item.getFilename());
                        requestedObject.setFolder("../Profiles");
                        requestedProfiles.add(requestedObject);
                        break;
                    case "songs":
                    default:
                        requestedObject.setFilename(item.getFilename());
                        requestedObject.setFolder(item.getFolder());
                        requestedSongs.add(requestedObject);
                        break;
                }
            }
        }

        if (!requestedSongs.isEmpty()) {
            nearbyJsonRequest.setShareableSongObjects(requestedSongs);
        }
        if (!requestedSets.isEmpty()) {
            nearbyJsonRequest.setShareableSetObjects(requestedSets);
        }
        if (!requestedProfiles.isEmpty()) {
            nearbyJsonRequest.setShareableProfileObjects(requestedProfiles);
        }

        return nearbyJsonRequest;
    }

    public void chooseFolder(String folderChosen) {
        this.folderChosen = folderChosen;
    }
}
