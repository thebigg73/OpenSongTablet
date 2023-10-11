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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SetAdapter extends RecyclerView.Adapter<SetListItemViewHolder> implements SetItemTouchInterface {

    private final MainActivityInterface mainActivityInterface;
    private final String slide_string, note_string, scripture_string, image_string,
            variation_string, pdf_string;
    private final int onColor, offColor;
    private final float titleSize, subtitleSizeFile;
    private final boolean useTitle;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetAdapter";
    private ItemTouchHelper itemTouchHelper;
    private final RecyclerView recyclerView;
    private final String highlightItem="highlightItem", updateNumber="updateNumber";
    private int currentHighlightPosition = -1;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    // The songs in the set are held in and array of SetItemInfos
    ArrayList<SetItemInfo> setList = new ArrayList<>();

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
        slide_string = c.getString(R.string.slide);
        note_string = c.getString(R.string.note);
        scripture_string = c.getString(R.string.scripture);
        image_string = c.getString(R.string.image);
        variation_string = c.getString(R.string.variation);
        pdf_string = c.getString(R.string.pdf);
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    // We build the setList array from the currentSet
    public void buildSetList() {
        // Clear the set
        int size = getItemCount();
        setList.clear();
        uiHandler.post(() -> notifyItemRangeRemoved(0,size));


        // Go through each item in the set and add it into the setList array
        for (int x=0; x<mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
            setList.add(makeSetItem(x));
        }

        // Initialise the first item in the set
        if (setList.size()>0) {
            currentHighlightPosition = 0;
        }
    }
    private SetItemInfo makeSetItem(int position) {
        SetItemInfo setItemInfo = new SetItemInfo();
        setItemInfo.songitem = (position+1) + ".";
        setItemInfo.songfolder = mainActivityInterface.getCurrentSet().getFolder(position);
        setItemInfo.songfoldernice = mainActivityInterface.getCurrentSet().getFolder(position);
        setItemInfo.songfilename = mainActivityInterface.getCurrentSet().getFilename(position);
        setItemInfo.songtitle = mainActivityInterface.getCurrentSet().getTitle(position);
        setItemInfo.songkey = mainActivityInterface.getCurrentSet().getKey(position);

        // Decide on the icon to use for the set item
        if (setItemInfo.songfolder!=null) {
            if (setItemInfo.songfolder.equals("**Slides")) {
                setItemInfo.songicon = "Slides";
                setItemInfo.songfoldernice = slide_string;
            } else if (setItemInfo.songfolder.equals("**Notes")) {
                setItemInfo.songicon = "Notes";
                setItemInfo.songfoldernice = note_string;
            } else if (setItemInfo.songfolder.equals("**Scripture")) {
                setItemInfo.songicon = "Scripture";
                setItemInfo.songfoldernice = scripture_string;
            } else if (setItemInfo.songfolder.equals("**Images")) {
                setItemInfo.songicon = "Images";
                setItemInfo.songfoldernice = image_string;
            } else if (setItemInfo.songfolder.equals("**Variations")) {
                setItemInfo.songicon = "Variations";
                setItemInfo.songfoldernice = variation_string;
            } else if (setItemInfo.songtitle.toLowerCase(Locale.ROOT).contains(".pdf")) {
                setItemInfo.songicon = ".pdf";
                setItemInfo.songfoldernice = pdf_string;
            } else {
                setItemInfo.songicon = "Songs";
            }
        } else {
            setItemInfo.songicon = "Songs";
        }
        return setItemInfo;
    }
    public ArrayList<SetItemInfo> getSetList() {
        return setList;
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
    public void onBindViewHolder(@NonNull SetListItemViewHolder holder, int position) {
        position = holder.getAbsoluteAdapterPosition();
        SetItemInfo si = setList.get(position);
        String key = si.songkey;
        String titlesongname = si.songtitle;
        String filename = si.songfilename;
        if (key != null && !key.equals("null") && !key.isEmpty()) {
            titlesongname = titlesongname + " (" + key + ")";
        } else {
            si.songkey = "";
        }

        // If this is the current set item, highlight it
        if (position == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
            setColor(holder,onColor);
            currentHighlightPosition = position;
        } else {
            setColor(holder,offColor);
        }

        holder.cardItem.setTextSize(titleSize);
        holder.cardItem.setText(si.songitem);
        String newfoldername = si.songfoldernice;
        if (newfoldername != null && newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**", "");
        }
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
                    String newNumber = (position+1) + ".";
                    setList.get(position).songitem = newNumber;
                    holder.cardItem.setText(newNumber);
                }

                if (payload.equals(highlightItem)||payload.equals(updateNumber)) {
                    // We want to update the highlight colour to on/off
                    if (position == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                        currentHighlightPosition = position;
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
        return setList.size();
    }

    // Set the colour of the chosen view
    private void setColor(SetListItemViewHolder holder, int cardColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor));
        } else {
            holder.cardView.setBackgroundColor(cardColor);
        }
    }


    // The callbacks from the SetItemTouchInterface (called from the SetListItemCallback class)
    @Override
    // This method deals with dragging items up and down in the set list
    public void onItemMoved(int fromPosition, int toPosition) {
        if (setList!=null && setList.size()>fromPosition && setList.size()>toPosition &&
                mainActivityInterface.getCurrentSet().getSetItems()!=null &&
                mainActivityInterface.getCurrentSet().getSetItems().size()>fromPosition) {

            // Move the item in the setList array
            setList.add(toPosition,setList.remove(fromPosition));

            // Notify the adapter that we have moved items
            notifyItemMoved(fromPosition, toPosition);

            // Notify the adapter that we have updated the item numbers
            notifyItemChanged(toPosition, updateNumber);
            notifyItemChanged(fromPosition, updateNumber);

            // Update the currentSet and save the set string
            mainActivityInterface.getCurrentSet().swapPositions(fromPosition, toPosition);
            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

            // Check for the current item position being changed
            if (fromPosition == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setIndexSongInSet(toPosition);
            } else if (toPosition == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setIndexSongInSet(fromPosition);
            }

            // Update the inline set to mirror these changes
            mainActivityInterface.updateInlineSetMove(fromPosition, toPosition);

            // Update the title
            mainActivityInterface.updateSetTitle();

            // Update the prev/next
            updateSetPrevNext();
        }
    }

    // This method is called when an item is swiped away.
    public void removeItem(int fromPosition) {

        if (setList.size()>fromPosition &&
                mainActivityInterface.getCurrentSet().getSetItems().size()>fromPosition) {

            // Remove the check mark for this song
            mainActivityInterface.updateCheckForThisSong(mainActivityInterface.getSQLiteHelper().getSpecificSong(setList.get(fromPosition).songfolder,setList.get(fromPosition).songfilename));

            // Remove the item from the current set and save the set
            mainActivityInterface.getCurrentSet().removeFromCurrentSet(fromPosition, null);
            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

            // If the currently selected set item is after this position, we need to drop it by 1
            if (fromPosition<mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setIndexSongInSet(mainActivityInterface.getCurrentSet().getIndexSongInSet()-1);
            }

            // Remove the item from the setList and notify the adapter of changes
            setList.remove(fromPosition);
            notifyItemRemoved(fromPosition);

            // Update the numbers of the items below this
            updateNumbersBelowPosition(fromPosition);

            // Update the inline set to mirror these changes
            mainActivityInterface.updateInlineSetRemoved(fromPosition);

            // Update the title
            mainActivityInterface.updateSetTitle();

            // Update the prev/next
            updateSetPrevNext();
        }
    }

    // This method is used to undo a swiped away item
    public void restoreItem(SetItemInfo setItemInfo, int position) {
        // Add item back to the setList
        setList.add(position,setItemInfo);
        notifyItemInserted(position);

        // Update the numbers below this position (+1)
        updateNumbersBelowPosition(position);

        // Add it back to the current set
        mainActivityInterface.getCurrentSet().insertIntoCurrentSet(position,
                mainActivityInterface.getSetActions().getSongForSetWork(setItemInfo.songfolder,setItemInfo.songfilename,setItemInfo.songkey),
                setItemInfo.songfolder,setItemInfo.songfilename,setItemInfo.songkey);
        mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

        // Update the inline set to mirror this change
        mainActivityInterface.updateInlineSetInserted(position,setItemInfo);

        // Update the title
        mainActivityInterface.updateSetTitle();

        // Update the prev/next
        updateSetPrevNext();
    }

    // Change the numbers below the changed item (either +1 or -1)
    private void updateNumbersBelowPosition(int fromPosition) {
        for (int i=fromPosition; i<setList.size(); i++) {
            setList.get(i).songitem = (i+1) + ".";
        }

        // Notify the changes from this position and beyond
        int count = setList.size()-fromPosition;

        // Notify the changes
        notifyItemRangeChanged(fromPosition,count);
    }

    // New item has been added to the bottom of the set
    public void addNewItem(int currentSetPosition) {
        setList.add(makeSetItem(currentSetPosition));
        uiHandler.post(() -> notifyItemInserted(setList.size()-1));

        // Update the inline set too
        mainActivityInterface.updateInlineSetAdded(setList.get(currentSetPosition));

        // Update the title
        mainActivityInterface.updateSetTitle();

        // Update the prev/next
        updateSetPrevNext();
    }

    // Update item (could be a key change, etc)
    public void updateKeys() {
        for (int position:mainActivityInterface.getSetActions().getMissingKeyPositions()) {
            try {
                setList.get(position).songkey = mainActivityInterface.getCurrentSet().getKey(position);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        uiHandler.post(() -> notifyItemRangeChanged(0,setList.size()));
        mainActivityInterface.getSetActions().nullMissingKeyPositions();
    }
    public void updateItem(int position) {
        if (position>=0 && setList!=null && setList.size()>position) {
            try {
                String folder = mainActivityInterface.getCurrentSet().getFolder(position);
                String filename = mainActivityInterface.getCurrentSet().getFilename(position);
                String title = mainActivityInterface.getCurrentSet().getTitle(position);
                String key = mainActivityInterface.getCurrentSet().getKey(position);
                setList.get(position).songfolder = folder.replace("**", "../");
                setList.get(position).songfoldernice = folder;
                setList.get(position).songfilename = filename;
                setList.get(position).songtitle = title;
                setList.get(position).songkey = key;

                // Check for icon
                setList.get(position).songicon = mainActivityInterface.getSetActions().
                        getIconIdentifier(folder, filename);

                // Update the inline set
                mainActivityInterface.updateInlineSetChanged(position, setList.get(position));

                // Update the title
                mainActivityInterface.getCurrentSet().updateSetTitleView();
                notifyItemChanged(position);

                // Update the highlight on the previous item to off
                if (currentHighlightPosition>=0 && currentHighlightPosition<setList.size()) {
                    notifyItemChanged(currentHighlightPosition,highlightItem);
                }

                // Update the prev/next
                updateSetPrevNext();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void clearOldHighlight(int position) {
        // Update the highlight on the previous item to off
        if (currentHighlightPosition>=0 && currentHighlightPosition<setList.size()) {
            notifyItemChanged(currentHighlightPosition,highlightItem);
        }
        notifyItemChanged(position,highlightItem);
    }

    public boolean initialiseSetItem(int setPosition) {
        // Only used when app boots and we are already viewing a set item
        // This comes via the MyToolbar where we add a tick for a set item
        uiHandler.post(() -> {
            notifyItemChanged(currentHighlightPosition, highlightItem);
            notifyItemChanged(setPosition, highlightItem);
        });
        return true;
    }

    @Override
    public void onItemClicked(MainActivityInterface mainActivityInterface, int position) {
        mainActivityInterface.getCurrentSet().setIndexSongInSet(position);
        mainActivityInterface.initialiseInlineSetItem(position);
        mainActivityInterface.loadSongFromSet(position);
        currentHighlightPosition = position;
    }

    @Override
    public void onRowSelected(SetListItemViewHolder myViewHolder) {
        myViewHolder.itemView.setSelected(false);
    }

    public void recoverCurrentSetPosition() {
        // Get the set position as we might have moved things around
        uiHandler.post(() -> notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(),highlightItem));
    }

    private void updateSetPrevNext() {
        mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
        mainActivityInterface.getDisplayPrevNext().setPrevNext();
    }

    public void notifyAllChanged() {
        if (setList!=null && setList.size()>0) {
            uiHandler.post(()-> notifyItemRangeChanged(0,getItemCount()));

            // Update the inline set
            mainActivityInterface.updateInlineSetAll();

            updateSetPrevNext();
        }
    }

}
