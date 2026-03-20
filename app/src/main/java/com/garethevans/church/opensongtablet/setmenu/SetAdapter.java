package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.List;

public class SetAdapter extends RecyclerView.Adapter<SetListItemViewHolder> implements SetItemTouchInterface {

    // Rather than use an array list stored here, use the currentSet object array
    // Only the set adapter should change the indexSongInSet

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SetAdapter";

    private final MainActivityInterface mainActivityInterface;
    private float titleSize, subtitleSizeFile;
    private boolean useTitle;
    private ItemTouchHelper itemTouchHelper;
    private SetListItemCallback setListItemCallback;
    private final RecyclerView recyclerView;
    private final String highlightItem="highlightItem", unhighlightItem="unhighlightItem",
            updateNumber="updateNumber";
    private boolean updatingHighlight = false, removingHighlight = false;
    private boolean highlightChangeAllowed =true;
    private final String divider_string;
    private final Context c;

    //Initialise the class
    public SetAdapter(Context c, RecyclerView recyclerView) {
        mainActivityInterface = (MainActivityInterface) c;
        this.c = c;
        this.recyclerView = recyclerView;
        // Get the size of the text to use
        getUpdatedPreferences();
        divider_string = c.getString(R.string.divider);
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
        titleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuItemSize",14f);
        subtitleSizeFile = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeFile",12f);
        useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
    }

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }
    public void setSetListItemCallback(SetListItemCallback setListItemCallback) {
        this.setListItemCallback = setListItemCallback;
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
        String foldername = si.songfolder;
        String newfoldername = si.songfoldernice;

        // If this is a variation, we can prettify the output (remove the reference to the original folder)
        if (mainActivityInterface.getVariations().getIsNormalOrKeyVariation(foldername,filename)) {
            filename = filename.substring(filename.lastIndexOf("_")).replace("_","");
            titlesongname = filename;
        }

        if (key != null && !key.equals("null") && !key.isEmpty()) {
            titlesongname = titlesongname + " (" + key + ")";
        } else {
            si.songkey = "";
        }

        // If we don't have a indexSongInSet, but this song should be it, do it
        String songfilename = mainActivityInterface.getSong().getFilename();
        String songfolder = mainActivityInterface.getSong().getFolder();
        if (songfolder.equals("../Variations/_cache") && songfilename.contains("_K-")) {
            // Remove the key part from the filename
            songfilename = songfilename.substring(0,songfilename.lastIndexOf("_K-"));
            // Now get the folder part
            if (songfilename.contains("_")) {
                songfolder = songfilename.substring(0,songfilename.lastIndexOf("_"));
                songfilename = songfilename.substring(songfilename.lastIndexOf("_")+1);
            } else {
                songfolder = mainActivityInterface.getMainfoldername();
            }
        }

        // Keep a reference if this is the current set item
        boolean currentSetItem = position == mainActivityInterface.getCurrentSet().getIndexSongInSet();

        // Incase we have a key variation, get the shorter bits
        if (mainActivityInterface.getCurrentSet().getIndexSongInSet()==-1 &&
                songfilename.equals(filename) &&
                songfolder.equals(foldername)) {
                currentSetItem = true;
            mainActivityInterface.getCurrentSet().setIndexSongInSet(position);
        }

        int thisTextColor = mainActivityInterface.getPalette().textColor;
        // Divider
        if (si.songfolder.equals(mainActivityInterface.getSetActions().getDividerIdentifier()) ||
                si.songfolder.contains("**"+mainActivityInterface.getSetActions().getDividerIdentifier()) ||
                si.songfolder.contains("**Divider")) {
            thisTextColor = mainActivityInterface.getPalette().hintColor;
            setColor(holder,mainActivityInterface.getMyThemeColors().getSetBackgroundColor(holder.cardView));
            setFABColor(holder.cardEdit,mainActivityInterface.getMyThemeColors().getSetActiveColor(holder.cardEdit));

        } else if (currentSetItem) {
            setColor(holder,mainActivityInterface.getMyThemeColors().getSetActiveColor(holder.cardView));
            setFABColor(holder.cardEdit,mainActivityInterface.getMyThemeColors().getSetInactiveColor(holder.cardEdit));

        } else {
            setColor(holder,mainActivityInterface.getMyThemeColors().getSetInactiveColor(holder.cardView));
            setFABColor(holder.cardEdit,mainActivityInterface.getMyThemeColors().getSetActiveColor(holder.cardEdit));
        }

        holder.cardItem.setTextSize(titleSize);
        String text = si.songitem + ".";
        holder.cardItem.setText(text);
        holder.cardTitle.setTextSize(titleSize);
        holder.cardFilename.setTextSize(titleSize);
        holder.cardFolder.setTextSize(subtitleSizeFile);
        holder.cardFilename.setTextColor(thisTextColor);
        holder.cardTitle.setTextColor(thisTextColor);
        holder.cardFolder.setTextColor(thisTextColor);
        holder.cardItem.setTextColor(thisTextColor);

        holder.cardFilename.setVisibility(useTitle ? View.GONE:View.VISIBLE);
        holder.cardTitle.setVisibility(useTitle ? View.VISIBLE:View.GONE);

        if (songfolder.equals("**Divider") ||
                songfolder.contains(mainActivityInterface.getSetActions().getDividerIdentifier())) {
            holder.cardFolder.setVisibility(View.GONE);
            holder.cardTitle.setText(titlesongname);
            holder.cardFilename.setText(titlesongname);
        } else {
            // Not a divider
            holder.cardTitle.setText(titlesongname);
            holder.cardFilename.setText(filename);
            holder.cardFolder.setText(newfoldername);

            // Set the listener for the edit button
            holder.cardEdit.setOnClickListener(view -> {
                SetEditItemBottomSheet setEditItemBottomSheet = new SetEditItemBottomSheet(position);
                setEditItemBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"SetEditItemBottomSheet");
            });
        }

        if (si.songicon==null || si.songicon.isEmpty()) {
            si.songicon = mainActivityInterface.getSetActions().getIconIdentifier(foldername,filename);
        }

        // Set the icon
        int icon = mainActivityInterface.getSetActions().getItemIcon(si.songicon);
        Drawable drawable = AppCompatResources.getDrawable(c,icon);
        if (drawable!=null) {
            DrawableCompat.setTint(drawable, mainActivityInterface.getPalette().textColor);
        }
        holder.cardItem.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null,null);


        // Set the click listener for the whole row
        /*holder.itemView.setOnClickListener(v -> {
            // Check if we are currently dragging.
            // If your Callback is dragging, ignore the click.
            if (setListItemCallback != null && setListItemCallback.getIsDragging()) {
                return;
            }
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                onItemClicked(mainActivityInterface, currentPos);
            }
        });*/
        //holder.cardItem.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
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

                String folder = mainActivityInterface.getCurrentSet().getSetItemInfo(position).songfolder;
                int thisTextColor = mainActivityInterface.getPalette().textColor;

                if (payload.equals(highlightItem) || payload.equals(unhighlightItem) || payload.equals(updateNumber)) {
                    // We want to update the highlight colour to on/off

                    if (payload.equals(highlightItem)) {
                        setColor(holder, mainActivityInterface.getMyThemeColors().getSetActiveColor(holder.cardView));
                        setFABColor(holder.cardEdit,mainActivityInterface.getMyThemeColors().getSetInactiveColor(holder.cardEdit));

                    } else if (payload.equals(unhighlightItem)) {
                        if (folder.equals(mainActivityInterface.getSetActions().getDividerIdentifier()) ||
                                folder.contains("**Divider") ||
                                folder.contains("**"+divider_string)) {
                            thisTextColor = mainActivityInterface.getPalette().hintColor;
                            setColor(holder, mainActivityInterface.getMyThemeColors().getSetBackgroundColor(holder.cardView));

                        } else {
                            setColor(holder, mainActivityInterface.getMyThemeColors().getSetInactiveColor(holder.cardView));
                        }
                        setFABColor(holder.cardEdit,mainActivityInterface.getMyThemeColors().getSetActiveColor(holder.cardEdit));
                        holder.cardFilename.setTextColor(thisTextColor);
                        holder.cardFilename.setTextColor(thisTextColor);
                        holder.cardFolder.setTextColor(thisTextColor);
                        holder.cardItem.setTextColor(thisTextColor);
                    }
                }
                if (!payloads.isEmpty() && payloads.contains("PAYLOAD_NUMBER_ONLY")) {
                    // ONLY update the number text, do NOT touch anything else
                    holder.cardItem.setText(String.valueOf(position + 1));
                }
            }

            // Set the listener for the edit button as the position may have changed
            int finalPosition = position;
            holder.cardEdit.setOnClickListener(view -> {
                SetEditItemBottomSheet setEditItemBottomSheet = new SetEditItemBottomSheet(finalPosition);
                setEditItemBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"SetEditItemBottomSheet");
            });
        }
    }

    @Override
    public int getItemCount() {
        return mainActivityInterface.getCurrentSet().getCurrentSetSize();
    }

    // Set the colour of the chosen view
    private void setColor(SetListItemViewHolder holder, int cardColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.cardView.setCardBackgroundColor(cardColor);
            holder.cardView.setCardBackgroundColor(ColorStateList.valueOf(cardColor));
        } else {
            holder.cardView.setBackgroundColor(cardColor);
        }
    }

    private void setFABColor(MyFloatingActionButton fab, int fabColor) {
        fab.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            fab.setBackgroundTintList(ColorStateList.valueOf(fabColor));
        } else {
            fab.setBackgroundColor(fabColor);
        }
    }

    // Update the keys of set items (in case the database wasn't ready)
    public void updateKeys() {
        mainActivityInterface.getMainHandler().post(() -> notifyItemRangeChanged(0,mainActivityInterface.getCurrentSet().getCurrentSetSize()));
        mainActivityInterface.getSetActions().nullMissingKeyPositions();

        // Now send the instruction to the inline set
        mainActivityInterface.notifyInlineSetRangeChanged(0, mainActivityInterface.getCurrentSet().getCurrentSetSize());
    }

    public boolean getUpdatingHighlight() {
        return updatingHighlight;
    }

    public boolean getRemovingHighlight() {
        return removingHighlight;
    }

    // Called when loading a song from the set
    public void updateHighlight(int position) {
        if (!updatingHighlight && highlightChangeAllowed) {
            updatingHighlight = true;
            if (recyclerView != null && !recyclerView.isComputingLayout()) {
                mainActivityInterface.getMainHandler().post(() -> {
                    try {
                        notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(), highlightItem);
                        mainActivityInterface.getCurrentSet().setIndexSongInSet(position);
                        notifyItemChanged(position, highlightItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                // Now send the instruction to the inline set
                mainActivityInterface.notifyInlineSetHighlight();
            }
            updatingHighlight = false;
        }
    }

    public void removeHighlight(int position) {
         if (!removingHighlight && highlightChangeAllowed) {
            removingHighlight = true;
            if (recyclerView != null && !recyclerView.isComputingLayout()) {
                mainActivityInterface.getMainHandler().post(() -> {
                    try {
                        notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(), unhighlightItem);
                        notifyItemChanged(position, unhighlightItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                // Now send the instruction to the inline set
                mainActivityInterface.notifyInlineSetHighlight();
            }
            removingHighlight = false;
        }
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (mainActivityInterface.getCurrentSet().getSetItemInfos() != null) {
            // Swap the data
            mainActivityInterface.getCurrentSet().swapPositions(fromPosition, toPosition);

            // Notify the move (this handles the physical sliding animation)
            notifyItemMoved(fromPosition, toPosition);

            // Update numbers for the two items being swapped using a PAYLOAD
            // This prevents the ViewHolder from being recycled/rebound fully
            notifyItemChanged(fromPosition, "PAYLOAD_NUMBER_ONLY");
            notifyItemChanged(toPosition, "PAYLOAD_NUMBER_ONLY");

            updateInternalIndices(fromPosition, toPosition);
        }
    }

    private void updateInternalIndices(int fromPosition, int toPosition) {
        // 1. Update the persistent preference string in the background
        mainActivityInterface.getCurrentSet().setSetCurrent(
                mainActivityInterface.getSetActions().getSetAsPreferenceString()
        );

        // 2. Update the 'Current Song' index if it was the one being dragged
        if (fromPosition == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
            mainActivityInterface.getCurrentSet().setIndexSongInSet(toPosition);
        } else if (toPosition == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
            mainActivityInterface.getCurrentSet().setIndexSongInSet(fromPosition);
        }

        // 3. Update the 'Previous Song' index for highlighting
        if (fromPosition == mainActivityInterface.getCurrentSet().getPrevIndexSongInSet()) {
            mainActivityInterface.getCurrentSet().setPrevIndexSongInSet(toPosition);
        } else if (toPosition == mainActivityInterface.getCurrentSet().getPrevIndexSongInSet()) {
            mainActivityInterface.getCurrentSet().setPrevIndexSongInSet(fromPosition);
        }

        // 4. Update the mirror list (inline set) logically
        mainActivityInterface.notifyInlineSetMove(fromPosition, toPosition);
    }

    // This method is called when an item is added via the song menu to the end
    public void insertItem() {
        mainActivityInterface.getMainHandler().post(() -> notifyItemInserted(mainActivityInterface.getCurrentSet().getCurrentSetSize()-1));
        // Now update the inline set too
        mainActivityInterface.notifyInlineSetInserted();
    }

    // This method is called when an item is swiped away or unticked in the song menu.
    public void removeItem(int fromPosition,boolean updateMenu) {
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>fromPosition) {
            // --- STEP 1: CAPTURE INFO FIRST ---
            // Get the song info BEFORE we delete it from the list
            String folder = mainActivityInterface.getCurrentSet().getSetItemInfo(fromPosition).songfolder;
            String filename = mainActivityInterface.getCurrentSet().getSetItemInfo(fromPosition).songfilename;

            // --- STEP 2: DATA LOGIC ---
            // Remove the item from the current set
            mainActivityInterface.getCurrentSet().removeFromCurrentSet(fromPosition, null);

            // Adjust the current index if needed
            if (fromPosition < mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                mainActivityInterface.getCurrentSet().setIndexSongInSet(mainActivityInterface.getCurrentSet().getIndexSongInSet() - 1);
            }

            // --- STEP 3: UI UPDATES ---
            mainActivityInterface.getMainHandler().post(() -> {
                // These two calls fix the "empty space" and the index numbers
                notifyItemRemoved(fromPosition);
                notifyItemRangeChanged(fromPosition, mainActivityInterface.getCurrentSet().getCurrentSetSize());

                // Synchronize with other UI elements
                mainActivityInterface.notifyInlineSetRemoved(fromPosition);
                mainActivityInterface.notifyInlineSetRangeChanged(fromPosition, mainActivityInterface.getCurrentSet().getCurrentSetSize());
                mainActivityInterface.getCurrentSet().updateSetTitleView();

                // --- STEP 4: UPDATE SONG MENU ---
                if (updateMenu) {
                    mainActivityInterface.updateCheckForThisSong(
                            mainActivityInterface.getSQLiteHelper().getSpecificSong(folder, filename)
                    );
                }
            });
        }
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
        notifyItemRangeChanged(position,mainActivityInterface.getCurrentSet().getCurrentSetSize()-position);

        // Update the checked items
        updateCheckedItem(setItemInfo);

        // Update the title
        mainActivityInterface.getCurrentSet().updateSetTitleView();

        // Update the inline set to mirror this change
        mainActivityInterface.notifyInlineSetInserted(position);
        mainActivityInterface.notifyInlineSetRangeChanged(position,mainActivityInterface.getCurrentSet().getCurrentSetSize()-position);
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

    // Called when we edited an item from the bottom sheet
    public void updateItem(int position) {
        // Get the current item
        SetItemInfo setItemInfo = mainActivityInterface.getCurrentSet().getSetItemInfo(position);

        // Notify we changed the item
        mainActivityInterface.getMainHandler().post(() -> notifyItemChanged(position));

        // Update the checked item
        updateCheckedItem(setItemInfo);

        // Now send the instruction to the inline set
        mainActivityInterface.notifyInlineSetChanged(position);
    }

    @Override
    public void onItemClicked(MainActivityInterface mainActivityInterface, int position) {
        SetItemInfo si = null;
        try {
            si = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (si!=null && si.songfolder!=null &&
                (si.songfolder.equals(mainActivityInterface.getSetActions().getDividerIdentifier()) ||
                si.songfolder.contains("**Divider") ||
                        si.songfolder.contains("**"+divider_string))) {
            // Do nothing!
            Log.d(TAG,"Divider - do nothing");
        } else {
            mainActivityInterface.loadSongFromSet(position);
        }
    }

    @Override
    public void onRowSelected(SetListItemViewHolder myViewHolder) {
        myViewHolder.itemView.setSelected(false);
    }

    public void recoverCurrentSetPosition() {
        // Get the set position as we might have moved things around
        mainActivityInterface.getMainHandler().post(() -> {
            notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(),unhighlightItem);
            notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(),highlightItem);
        });
    }

    // When we load a song, we stop any highlight changes until loading is completed
    public void setHighlightChangeAllowed(boolean highlightChangeAllowed) {
        this.highlightChangeAllowed = highlightChangeAllowed;
        // If we are allowed to change the highlight, do it
        if (highlightChangeAllowed) {
            notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(),unhighlightItem);
            notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(),highlightItem);
        }
    }

    public boolean getHighlightChangeAllowed() {
        return highlightChangeAllowed;
    }

}
