package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetListItemViewHolder> implements SetItemTouchInterface {

    // Rather than use an array list stored here, use the currentSet object array
    private final MainActivityInterface mainActivityInterface;
     private final int onColor, offColor;
    private final float titleSize, subtitleSizeFile;
    private final boolean useTitle;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetAdapter";
    private ItemTouchHelper itemTouchHelper;
    private final RecyclerView recyclerView;
    private final String highlightItem="highlightItem", updateNumber="updateNumber";
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    //Initialise the class
    public SetAdapter(Context c, RecyclerView recyclerView) {
        mainActivityInterface = (MainActivityInterface) c;
        this.recyclerView = recyclerView;
        //setItemTouchInterface = (SetItemTouchInterface) c;
        // Get the size of the text to use
        titleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuItemSize",14f);
        subtitleSizeFile = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeFile",12f);
        useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
        onColor = c.getResources().getColor(R.color.colorSecondary);
        offColor = c.getResources().getColor(R.color.colorAltPrimary);
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }


    @NonNull
    @Override
    // Match the view to use for each item
    public SetListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_set_item, parent, false);

        return new SetListItemViewHolder(itemView, mainActivityInterface, itemTouchHelper,this);
    }

    @Override
    // Put the data into the view
    public void onBindViewHolder(@NonNull SetListItemViewHolder holder, int z) {
        int position = holder.getAbsoluteAdapterPosition();
        SetItemInfo si = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
        String key = si.songkey;
        si.songitem = position+1;
        String titlesongname = si.songtitle;
        String filename = si.songfilename;
        if (key != null && !key.equals("null") && !key.isEmpty()) {
            titlesongname = titlesongname + " (" + key + ")";
        } else {
            si.songkey = "";
        }

        String foldername = si.songfolder;
        String newfoldername = si.songfoldernice;
        if (newfoldername==null || newfoldername.isEmpty()) {
            newfoldername = mainActivityInterface.getSetActions().niceCustomLocationFromFolder(foldername);
            si.songfoldernice = newfoldername;
        }
        if (newfoldername != null && newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**", "");
        }

        // If we don't have a indexSongInSet, but this song should be it, do it
        if (mainActivityInterface.getCurrentSet().getIndexSongInSet()==-1 &&
                mainActivityInterface.getSong().getFilename().equals(filename) &&
                mainActivityInterface.getSong().getFolder().equals(foldername)) {
            mainActivityInterface.getCurrentSet().setIndexSongInSet(position);
        }

        // If this is the current set item, highlight it
        if (position == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
            setColor(holder,onColor);
        } else {
            setColor(holder,offColor);
        }

        holder.cardItem.setTextSize(titleSize);
        String text = si.songitem + ".";
        holder.cardItem.setText(text);

        holder.cardTitle.setTextSize(titleSize);
        holder.cardTitle.setText(titlesongname);
        holder.cardFilename.setTextSize(titleSize);
        holder.cardFilename.setText(filename);
        holder.cardFolder.setTextSize(subtitleSizeFile);
        holder.cardFilename.setVisibility(useTitle ? View.GONE:View.VISIBLE);
        holder.cardTitle.setVisibility(useTitle ? View.VISIBLE:View.GONE);
        holder.cardFolder.setText(newfoldername);

        // Set the icon
        int icon = mainActivityInterface.getSetActions().getItemIcon(si.songicon);
        holder.cardItem.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    @Override
    // Use a payload to update the background color or text of the items
    public void onBindViewHolder(@NonNull SetListItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        position = holder.getAbsoluteAdapterPosition();
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals(updateNumber)) {
                    int newNumber = (position+1);
                    mainActivityInterface.getCurrentSet().getSetItemInfo(position).songitem = newNumber;
                    String text = newNumber + ".";
                    holder.cardItem.setText(text);
                }

                if (payload.equals(highlightItem)||payload.equals(updateNumber)) {
                    // We want to update the highlight colour to on/off
                    if (position == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                        setColor(holder, onColor);
                    } else {
                        setColor(holder, offColor);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mainActivityInterface.getCurrentSet().getCurrentSetSize();
    }

    // Set the colour of the chosen view
    private void setColor(SetListItemViewHolder holder, int cardColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor));
        } else {
            holder.cardView.setBackgroundColor(cardColor);
        }
    }


    // Update the keys of set items (in case the database wasn't ready)
    public void updateKeys() {
        uiHandler.post(() -> notifyItemRangeChanged(0,mainActivityInterface.getCurrentSet().getCurrentSetSize()));
        mainActivityInterface.getSetActions().nullMissingKeyPositions();
    }

    // Called when loading a song from the set
    public void updateHighlight() {
        if (recyclerView!=null && !recyclerView.isComputingLayout()) {
            recyclerView.post(() -> {
                try {
                    notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(), highlightItem);
                    notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(), highlightItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }



    // TODO rationalise or check

    // Called when we reset the set list (notify removed and notifiy inserted
    public void resetTheSetList(int originalItems) {
        // This needs to be done on the mainUI
        recyclerView.post(() -> {
            if (originalItems>0) {
                notifyItemRangeRemoved(0, originalItems);
            }
            if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
                notifyItemRangeInserted(0, mainActivityInterface.getCurrentSet().getCurrentSetSize());
            }
        });
    }





    // The callbacks from the SetItemTouchInterface (called from the SetListItemCallback class)
    @Override
    // This method deals with dragging items up and down in the set list
    public void onItemMoved(int fromPosition, int toPosition) {
        if (mainActivityInterface.getCurrentSet().getSetItemInfos()!=null &&
                mainActivityInterface.getCurrentSet().getCurrentSetSize()>fromPosition &&
                mainActivityInterface.getCurrentSet().getCurrentSetSize()>toPosition &&
                mainActivityInterface.getCurrentSet().getSetItemInfos()!=null) {

            // Update the currentSet and save the set string
            mainActivityInterface.getCurrentSet().swapPositions(fromPosition, toPosition);
            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

            // Notify the adapter that we have updated the item numbers
            notifyItemChanged(toPosition, updateNumber);
            notifyItemChanged(fromPosition, updateNumber);

            // Notify the adapter that we have moved items
            notifyItemMoved(fromPosition, toPosition);

            // Check for the current item or prev set item position being changed
            if (fromPosition == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setIndexSongInSet(toPosition);
            } else if (toPosition == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setIndexSongInSet(fromPosition);
            }
            if (fromPosition == mainActivityInterface.getCurrentSet().getPrevIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setPrevIndexSongInSet(toPosition);
            } else if (toPosition == mainActivityInterface.getCurrentSet().getPrevIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setPrevIndexSongInSet(fromPosition);
            }

            // Update the title
            mainActivityInterface.updateSetTitle();

            // Update the inline set to mirror these changes
            mainActivityInterface.updateInlineSetMove(fromPosition, toPosition);
        }
    }

    // This method is called when an item is swiped away.
    public void removeItem(int fromPosition) {
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>fromPosition &&
                mainActivityInterface.getCurrentSet().getCurrentSetSize()>fromPosition) {

            // Remove the item from the current set and save the set
            mainActivityInterface.getCurrentSet().removeFromCurrentSet(fromPosition, null);

            // If the currently selected set item is after this position, we need to drop it by 1
            if (fromPosition<mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setIndexSongInSet(mainActivityInterface.getCurrentSet().getIndexSongInSet()-1);
            }

            // Notify the adapter of changes
            notifyItemRemoved(fromPosition);

            // Update the numbers of the items below this
            updateNumbersBelowPosition(fromPosition);

            // Remove the check mark for this song in the song menu
            mainActivityInterface.updateCheckForThisSong(
                    mainActivityInterface.getSQLiteHelper().getSpecificSong(
                            mainActivityInterface.getCurrentSet().getSetItemInfo(fromPosition).songfolder,
                            mainActivityInterface.getCurrentSet().getSetItemInfo(fromPosition).songfilename));


            // Update the title
            mainActivityInterface.updateSetTitle();

            // Update the inline set to mirror these changes
            mainActivityInterface.updateInlineSetRemoved(fromPosition);
        }
    }

    public void notifyToClearSet() {
        // Notify to clear the set
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            mainActivityInterface.getMainHandler().post(() -> notifyItemRangeRemoved(0,mainActivityInterface.getCurrentSet().getCurrentSetSize()));
        }
    }

    public void notifyToInsertAllSet() {
        // Notify to insert the entire set
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize() > 0) {
            notifyItemRangeInserted(0, mainActivityInterface.getCurrentSet().getCurrentSetSize());
        }
        // Now make sure the set is visible and the progress bar is hidden
        mainActivityInterface.updateFragment("set_showSetList", null, null);
    }


    // This method is used to undo a swiped away item
    public void restoreItem(SetItemInfo setItemInfo, int position) {
        // Add item back to the setList
        // Add it back to the current set
        mainActivityInterface.getCurrentSet().insertIntoCurrentSet(position,setItemInfo);
        mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

        // Notify the change
        notifyItemInserted(position);

        // Update the numbers below this position (+1)
        updateNumbersBelowPosition(position);

        // Update the checked items
        updateCheckedItem(setItemInfo);

        // Update the title
        mainActivityInterface.updateSetTitle();

        // Update the inline set to mirror this change
        mainActivityInterface.updateInlineSetInserted(position);
    }

    // Update the item checkbox in the song menu
    private void updateCheckedItem(SetItemInfo setItemInfo) {
        // Update the checked items
        Song updateSong = new Song();
        updateSong.setFolder(setItemInfo.songfolder);
        updateSong.setFilename(setItemInfo.songfilename);
        updateSong.setTitle(setItemInfo.songtitle);
        updateSong.setKey(setItemInfo.songkey);
        mainActivityInterface.updateCheckForThisSong(updateSong);
    }

    // Change the numbers below the changed item (either +1 or -1)
    // Called when swiping away to delete and item or restoring it
    private void updateNumbersBelowPosition(int fromPosition) {
        for (int i=fromPosition; i<mainActivityInterface.getCurrentSet().getCurrentSetSize(); i++) {
            mainActivityInterface.getCurrentSet().getSetItemInfo(i).songitem = (i+1);
        }

        // Notify the changes from this position and beyond
        int count = mainActivityInterface.getCurrentSet().getCurrentSetSize()-fromPosition;

        // Notify the changes
        notifyItemRangeChanged(fromPosition,count);
    }




    // Called when we edit a set item
    public void updateItem(int position) {
        if (position>=0 && mainActivityInterface.getCurrentSet().getSetItemInfos()!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>position) {
            try {
                // Check for icon
                mainActivityInterface.getCurrentSet().getSetItemInfo(position).songicon = mainActivityInterface.getSetActions().
                        getIconIdentifier(mainActivityInterface.getCurrentSet().getSetItemInfo(position).songfolder,
                                mainActivityInterface.getCurrentSet().getSetItemInfo(position).songfilename);

                // Update the title
                mainActivityInterface.getCurrentSet().updateSetTitleView();
                notifyItemChanged(position);

                // Update the highlighting
                notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(),highlightItem);
                notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(),highlightItem);

                // Update the inline set
                mainActivityInterface.updateInlineSetChanged(position);

                // Update the prev/next
                uiHandler.post(() -> mainActivityInterface.getDisplayPrevNext().setPrevNext());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public boolean initialiseSetItem() {
        // Only used when app boots and we are already viewing a set item
        // This comes via the MyToolbar where we add a tick for a set item
        uiHandler.post(() -> {
            notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(),highlightItem);
            notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet());
        });
        return true;
    }

    @Override
    public void onItemClicked(MainActivityInterface mainActivityInterface, int position) {
        mainActivityInterface.loadSongFromSet(position);
    }

    @Override
    public void onRowSelected(SetListItemViewHolder myViewHolder) {
        myViewHolder.itemView.setSelected(false);
    }

    public void recoverCurrentSetPosition() {
        // Get the set position as we might have moved things around
        uiHandler.post(() -> {
            notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(),highlightItem);
            notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(),highlightItem);
        });
    }


}
