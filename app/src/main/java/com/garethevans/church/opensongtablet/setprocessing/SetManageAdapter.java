package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
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

public class SetManageAdapter extends RecyclerView.Adapter<SetManageViewHolder> {

    MainActivityInterface mainActivityInterface;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetManageAdapter";
    private ArrayList<FoundSet> foundSets = new ArrayList<>();
    private final ArrayList<String> checkedItems = new ArrayList<>();
    private final String whatView;
    private final SetManageFragment setManageFragment;

    SetManageAdapter(Context c, SetManageFragment setManageFragment, String whatView) {
        mainActivityInterface = (MainActivityInterface) c;
        this.whatView = whatView;
        this.setManageFragment = setManageFragment;
        setHasStableIds(false);
        Log.d(TAG,"whatView:"+whatView);
    }

    public void prepareSetManageInfos() {
        mainActivityInterface.getThreadPoolExecutor().execute(() -> {
            ArrayList<String> setsToUse = mainActivityInterface.getSetActions().getRequiredSets(whatView.equals("renameset"));

            // Get a count of the original items (if any)
            int oldSize = getItemCount();
            foundSets = new ArrayList<>();

            for (String setToUse : setsToUse) {
                FoundSet foundSet = new FoundSet();

                // Add the filename as it is
                foundSet.setFilename(setToUse);

                // Get the set uri
                foundSet.setUri(mainActivityInterface.getStorageAccess().getUriForItem("Sets", "", foundSet.getFilename()));

                // Get the set title and category separately
                String[] bits = mainActivityInterface.getSetActions().getSetCategoryAndName(foundSet.getFilename());
                foundSet.setCategory(bits[0]);
                foundSet.setTitle(bits[1]);

                // Set the tag which is what is shown on the screen to the user
                if (whatView.equals("renameset")) {
                    // Listing all, so include the main category just as the rest
                    foundSet.setTag("(" + foundSet.getCategory() + ") " + foundSet.getTitle());
                } else {
                    // Only looking in one category, so just show the title
                    foundSet.setTag(foundSet.getTitle());
                }

                // Get the last modified date
                long lastModifiedLong = mainActivityInterface.getStorageAccess().getLastModifiedDate(foundSet.getUri());
                String lastModifiedString = mainActivityInterface.getTimeTools().getDateFromMillis(mainActivityInterface.getLocale(), lastModifiedLong);
                foundSet.setLastModifiedLong(lastModifiedLong);
                foundSet.setLastModifiedString(lastModifiedString);

                // Set the set identifier
                foundSet.setIdentifier(mainActivityInterface.getSetActions().getItemStart() +
                        foundSet.getFilename() + mainActivityInterface.getSetActions().getItemEnd());

                // Set the values as checked based on if it is in the checkedItems array
                foundSet.setChecked(checkedItems.contains(foundSet.getIdentifier()));

                // Add this set item to the array
                foundSets.add(foundSet);
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
    public SetManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_checkbox_list_item, parent, false);
        return new SetManageViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull SetManageViewHolder holder, int position) {
        // Prepare the view from the foundSets array
        position = holder.getAbsoluteAdapterPosition();

        // Get the values for this view
        if (position<foundSets.size()) {
            FoundSet foundSet = foundSets.get(position);

            // Set filename

            // Decide if this value is selected (only available/visible when loading set)
            holder.checkBox.setChecked(foundSet.getChecked());
            if (whatView.equals("loadset") || whatView.equals("deleteset")) {
                holder.checkBox.setVisibility(View.VISIBLE);
            } else {
                holder.checkBox.setVisibility(View.GONE);
            }

            holder.checkBox.setChecked(foundSet.getChecked());


            // Set the listener for this item
            int finalPosition = position;
            holder.itemLayout.setOnClickListener(view -> {
                if (whatView.equals("loadset") || whatView.equals("deleteset")) {
                    // Set the item checked value as the opposite to what it currently was
                    foundSets.get(finalPosition).setChecked(!foundSets.get(finalPosition).getChecked());
                    notifyItemChanged(finalPosition);
                    if (foundSets.get(finalPosition).getChecked()) {
                        // Add the item if it isn't already there
                        if (!checkedItems.contains(foundSets.get(finalPosition).getIdentifier())) {
                            checkedItems.add(foundSets.get(finalPosition).getIdentifier());
                        }
                    } else {
                        // Remove the item if it isn't already there
                        checkedItems.remove(foundSets.get(finalPosition).getIdentifier());
                    }
                } else {
                    // Only one item allowed in the other modes
                    checkedItems.clear();
                    try {
                        checkedItems.add(foundSets.get(finalPosition).getIdentifier());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mainActivityInterface.updateFragment("setSelectedSetItem", setManageFragment, checkedItems);
            });

            mainActivityInterface.updateFragment("setSelectedSetItem", setManageFragment, checkedItems);

            holder.itemName.setText(foundSet.getTag());
            holder.modifiedDate.setText(foundSet.getLastModifiedString());
        }
    }

    @Override
    public int getItemCount() {
        return foundSets.size();
    }

    public void changeSortOrder() {
        // Now do the sorting based on the user preference
        String setsSortOrder = mainActivityInterface.getPreferences().getMyPreferenceString("setsSortOrder","oldest");
        switch (setsSortOrder) {
            case "az":
                Collections.sort(foundSets, (FoundSet a, FoundSet z) -> a.getTitle().compareTo(z.getTitle()));
                break;
            case "za":
                Collections.sort(foundSets, (FoundSet a, FoundSet z) -> z.getTitle().compareTo(a.getTitle()));
                break;
            case "newest":
                Collections.sort(foundSets, (o1, o2) -> Long.compare(o2.getLastModifiedLong(), o1.getLastModifiedLong()));
                break;
            case "oldest":
                Collections.sort(foundSets, (o1, o2) -> Long.compare(o1.getLastModifiedLong(), o2.getLastModifiedLong()));
                break;
        }
        mainActivityInterface.getMainHandler().post(() -> notifyItemRangeChanged(0,getItemCount()));
    }

    public ArrayList<String> getCheckedItems() {
        return checkedItems;
    }

}
