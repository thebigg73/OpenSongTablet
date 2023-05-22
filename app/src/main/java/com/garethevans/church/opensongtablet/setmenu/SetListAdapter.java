package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.SparseBooleanArray;
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

import java.util.ArrayList;
import java.util.List;

public class SetListAdapter extends RecyclerView.Adapter<SetItemViewHolder> implements SetItemTouchInterface {

    // All the helpers we need to access are in the MainActivity
    private final MainActivityInterface mainActivityInterface;
    private ItemTouchHelper itemTouchHelper;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SetListAdapter";
    private final int onColor, offColor;
    private int currentPosition = -1, dragPosition = -1;
    private ArrayList<SetItemInfo> setList;
    private final SparseBooleanArray highlightedArray = new SparseBooleanArray();
    private final float titleSize;
    private final float subtitleSizeFile;

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    SetListAdapter(Context context) {
        this.mainActivityInterface = (MainActivityInterface) context;
        onColor = context.getResources().getColor(R.color.colorSecondary);
        offColor = context.getResources().getColor(R.color.colorAltPrimary);
        // Make the title text the same as the alphaIndex size
        titleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuItemSize",14f);
        // subtitleSizeAuthor = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeAuthor",12f);
        subtitleSizeFile = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeFile",12f);
    }

    public void updateSetList(ArrayList<SetItemInfo> setItemInfos) {
        if (setList != null) {
            int size = getItemCount();
            setList.clear();
            notifyItemRangeRemoved(0, size);
        } else {
            setList = new ArrayList<>();
        }

        for (int x = 0; x < setItemInfos.size(); x++) {
            setList.add(x, setItemInfos.get(x));
            notifyItemInserted(x);
        }
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SetItemViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals("highlightItem")) {
                    // We want to update the highlight colour to on/off
                    if (highlightedArray.get(position, false)) {
                        setColor(holder, onColor);
                    } else if (dragPosition==position){
                        setColor(holder,onColor);
                        dragPosition=0;
                    } else {
                        setColor(holder, offColor);
                    }
                }
            }
        }
    }

    private void setColor(SetItemViewHolder holder, int cardColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor));
        } else {
            holder.cardView.setBackgroundColor(cardColor);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SetItemViewHolder setitemViewHolder, int i) {
        SetItemInfo si = setList.get(i);
        String key = si.songkey;
        String titlesongname = si.songtitle;
        if (key != null && !key.equals("null") && !key.isEmpty()) {
            titlesongname = titlesongname + " (" + key + ")";
        } else {
            si.songkey = "";
        }
        if (highlightedArray.get(i, false)) {
            setColor(setitemViewHolder, onColor);
        } else if (dragPosition == i) {
            setColor(setitemViewHolder, onColor);
            dragPosition = -1;
        } else {
            setColor(setitemViewHolder, offColor);
        }

        setitemViewHolder.vItem.setTextSize(titleSize);
        setitemViewHolder.vItem.setText(si.songitem);
        String newfoldername = si.songfoldernice;
        if (newfoldername != null && newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**", "");
        }
        setitemViewHolder.vSongTitle.setTextSize(titleSize);
        setitemViewHolder.vSongTitle.setText(titlesongname);
        setitemViewHolder.vSongFolder.setTextSize(subtitleSizeFile);
        setitemViewHolder.vSongFolder.setText(newfoldername);
        int icon = mainActivityInterface.getSetActions().getItemIcon(si.songicon);

        setitemViewHolder.vItem.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }

    @NonNull
    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.view_set_item, viewGroup, false);

        return new SetItemViewHolder(mainActivityInterface, itemView, itemTouchHelper, this);
    }


    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (setList!=null && setList.size()>fromPosition && setList.size()>toPosition &&
                mainActivityInterface.getCurrentSet().getSetItems()!=null &&
                mainActivityInterface.getCurrentSet().getSetItems().size()>fromPosition) {
            dragPosition = toPosition;
            String thisFolder = setList.get(fromPosition).songfolder;
            String thisFilename = setList.get(fromPosition).songfilename;
            String thisKey = setList.get(fromPosition).songkey;
            String thisSetItem = mainActivityInterface.getCurrentSet().getItem(fromPosition);

            // Remove from this position
            mainActivityInterface.getCurrentSet().removeFromCurrentSet(fromPosition, null);

            // Add to the new position
            mainActivityInterface.getCurrentSet().addToCurrentSet(toPosition, thisSetItem, thisFolder, thisFilename, thisKey);

            // Update the set string and save it
            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

            setList.get(fromPosition).songitem = (toPosition + 1) + ".";
            setList.get(toPosition).songitem = (fromPosition + 1) + ".";
            SetItemInfo thisItem = setList.get(fromPosition);
            setList.remove(fromPosition);
            setList.add(toPosition, thisItem);

            boolean from_highlighted = highlightedArray.get(fromPosition, false);
            boolean to_highlighted = highlightedArray.get(toPosition, false);
            highlightedArray.put(fromPosition, to_highlighted);
            highlightedArray.put(toPosition, from_highlighted);
            notifyItemChanged(fromPosition);
            notifyItemChanged(toPosition);
            notifyItemMoved(fromPosition, toPosition);

            mainActivityInterface.updateInlineSetMove(fromPosition, toPosition);

            // Update the title
            mainActivityInterface.updateSetTitle();
            updateSetPrevNext();
        }
    }

    @Override
    public void onItemSwiped(int fromPosition) {
        // Check the setList matches the current set!
        try {
            // Remove the item from the current set
            mainActivityInterface.getCurrentSet().removeFromCurrentSet(fromPosition, null);

            Song songRemoved = mainActivityInterface.getSQLiteHelper().getSpecificSong(
                    setList.get(fromPosition).songfolder, setList.get(fromPosition).songfilename);

            // Update the set string and save it
            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

            // Remove the item from the set list and notify the adapter of changes
            itemRemoved(fromPosition);

            // Update the title
            mainActivityInterface.updateSetTitle();
            mainActivityInterface.updateCheckForThisSong(songRemoved);
            updateSetPrevNext();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClicked(MainActivityInterface mainActivityInterface, int position) {
        updateHighlightedItem(position);
        mainActivityInterface.initialiseInlineSetItem(position);
        mainActivityInterface.loadSongFromSet(position);
    }

    @Override
    public void onContentChanged(int position) {
        notifyItemChanged(position);
    }

    public void updateHighlightedItem(int position) {
        int oldPosition = currentPosition;
        currentPosition = position;
        if (oldPosition != -1) {
            highlightedArray.put(oldPosition, false);
            notifyItemChanged(oldPosition, "highlightItem");
        }
        highlightedArray.put(currentPosition, true);
        notifyItemChanged(currentPosition, "highlightItem");
    }

    public ArrayList<SetItemInfo> getSetList() {
        return setList;
    }

    private void updateSetPrevNext() {
        mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
        mainActivityInterface.getDisplayPrevNext().setPrevNext();
    }

    public boolean initialiseSetItem(int setPosition) {
        // Only used when app boots and we are already viewing a set item
        // This comes via the MyToolbar where we add a tick for a set item
        if (currentPosition != setPosition) {
            // If we already had a currentPosition, clear it
            if (currentPosition != -1) {
                highlightedArray.put(currentPosition, false);
                notifyItemChanged(currentPosition, "highlightItem");
            }
            // Now highlight the loaded position
            currentPosition = setPosition;
            highlightedArray.put(currentPosition, true);
            notifyItemChanged(currentPosition, "highlightItem");
            mainActivityInterface.initialiseInlineSetItem(setPosition);

            return true;
        }
        return false;
    }

    public void itemRemoved(int position) {
        setList.remove(position);
        notifyItemRemoved(position);
        int currentSetPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
        // If item is removed before the current item, we need to adjust that down too
        if (position<currentSetPosition && position>-1) {
            highlightedArray.put(currentSetPosition,false);
            highlightedArray.put(currentSetPosition-1, true);
            mainActivityInterface.getCurrentSet().setIndexSongInSet(currentSetPosition-1);
        } else if (position == currentSetPosition) {
            // Remove the current set position as no longer valid
            highlightedArray.put(currentSetPosition,false);
            mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
        }

            // Go through the setList from this position and sort the numbers
        for (int x = position; x < setList.size(); x++) {
            setList.get(x).songitem = (x + 1) + ".";
            notifyItemChanged(x);
        }
        // Update the inline set too
        mainActivityInterface.updateInlineSetRemoved(position);
    }

    public void itemAdded(SetItemInfo setItemInfo) {
        setList.add(setItemInfo);
        notifyItemInserted(setList.size()-1);

        // Update the inline set too
        mainActivityInterface.updateInlineSetAdded(setItemInfo);
    }

    public int getSelectedPosition() {
        return mainActivityInterface.getCurrentSet().getIndexSongInSet();
    }

    public void recoverCurrentSetPosition() {
        // Get the set position as we might have moved things around
        notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(),"highlightItem");
    }

    public void clearOldHighlight(int position) {
        highlightedArray.put(position,false);
        notifyItemChanged(position,"highlightItem");
        currentPosition = position;
    }
}