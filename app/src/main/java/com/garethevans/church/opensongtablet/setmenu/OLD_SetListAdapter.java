/*
package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import java.util.Locale;

public class SetListAdapter extends RecyclerView.Adapter<SetItemViewHolder> implements SetItemTouchInterface {

    // All the helpers we need to access are in the MainActivity
    private final MainActivityInterface mainActivityInterface;
    private ItemTouchHelper itemTouchHelper;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "SetListAdapter";
    private final int onColor, offColor;
    private int currentPosition = -1, dragPosition = -1;
    private ArrayList<SetItemInfo> setList = new ArrayList<>();
    private final SparseBooleanArray highlightedArray = new SparseBooleanArray();
    private final float titleSize;
    private final float subtitleSizeFile;
    private final boolean useTitle;
    private boolean building;
    private final String slide_string, note_string, scripture_string, image_string,
            variation_string, pdf_string;
    private Handler uiHandler;

 */
/*   public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }*//*

*/
/*

    public SetListAdapter(Context context) {
        this.mainActivityInterface = (MainActivityInterface) context;
        onColor = context.getResources().getColor(R.color.colorSecondary);
        offColor = context.getResources().getColor(R.color.colorAltPrimary);
        // Make the title text the same as the alphaIndex size
        titleSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuItemSize",14f);
        // subtitleSizeAuthor = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeAuthor",12f);
        subtitleSizeFile = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuSubItemSizeFile",12f);
        useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
        slide_string = context.getString(R.string.slide);
        note_string = context.getString(R.string.note);
        scripture_string = context.getString(R.string.scripture);
        image_string = context.getString(R.string.image);
        variation_string = context.getString(R.string.variation);
        pdf_string = context.getString(R.string.pdf);
        uiHandler = new Handler(Looper.getMainLooper());
    }

    public void buildSetList() {
        // Only allow one buildSetList at a time!
        if (!building) {
            building = true;
            if (setList.size()>0) {
                setList.clear();
            }

            for (int i = 0; i < mainActivityInterface.getCurrentSet().getSetItems().size(); i++) {
                setList.add(makeSetItem(i));
            }
            uiHandler.post(() -> {
                notifyItemRangeInserted(0, mainActivityInterface.getCurrentSet().getSetItems().size() - 1);
                building = false;
            });
        }
    }

    private SetItemInfo makeSetItem(int i) {
        SetItemInfo si = new SetItemInfo();
        si.songitem = (i+1) + ".";
        if (i<mainActivityInterface.getCurrentSet().getSetFolders().size()) {
            si.songfolder = mainActivityInterface.getCurrentSet().getSetFolders().get(i);
            si.songfoldernice = mainActivityInterface.getCurrentSet().getSetFolders().get(i);
        }
        if (i<mainActivityInterface.getCurrentSet().getSetFilenames().size()) {
            si.songtitle = mainActivityInterface.getCurrentSet().getTitle(i);
            si.songfilename = mainActivityInterface.getCurrentSet().getSetFilenames().get(i);
        }
        if (i<mainActivityInterface.getCurrentSet().getSetKeys().size()) {
            si.songkey = mainActivityInterface.getCurrentSet().getSetKeys().get(i);
        }

        // Decide on the icon to use for the set item
        if (si.songfolder!=null) {
            if (si.songfolder.equals("**Slides")) {
                si.songicon = "Slides";
                si.songfoldernice = slide_string;
            } else if (si.songfolder.equals("**Notes")) {
                si.songicon = "Notes";
                si.songfoldernice = note_string;
            } else if (si.songfolder.equals("**Scripture")) {
                si.songicon = "Scripture";
                si.songfoldernice = scripture_string;
            } else if (si.songfolder.equals("**Images")) {
                si.songicon = "Images";
                si.songfoldernice = image_string;
            } else if (si.songfolder.equals("**Variations")) {
                si.songicon = "Variations";
                si.songfoldernice = variation_string;
            } else if (si.songtitle.toLowerCase(Locale.ROOT).contains(".pdf")) {
                si.songicon = ".pdf";
                si.songfoldernice = pdf_string;
            } else {
                si.songicon = "Songs";
            }
        } else {
            si.songicon = "Songs";
        }
        return si;
    }
*//*


   */
/* @Override
    public int getItemCount() {
        return setList.size();
    }
*//*


   */
/* @Override
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
*//*

    */
/*private void setColor(SetItemViewHolder holder, int cardColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.cardView.post(() -> holder.cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor)));
        } else {
            holder.cardView.post(() -> holder.cardView.setBackgroundColor(cardColor));
        }
    }
*//*

    */
/*@Override
    public void onBindViewHolder(@NonNull SetItemViewHolder setitemViewHolder, int i) {
        SetItemInfo si = setList.get(i);
        String key = si.songkey;
        String titlesongname = si.songtitle;
        String filename = si.songfilename;
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

        Log.d(TAG,"setting item: "+si.songitem+": "+si.songfilename);
        setitemViewHolder.vItem.setTextSize(titleSize);
        setitemViewHolder.vItem.setText(si.songitem);
        String newfoldername = si.songfoldernice;
        if (newfoldername != null && newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**", "");
        }
        setitemViewHolder.vSongTitle.setTextSize(titleSize);
        setitemViewHolder.vSongTitle.setText(titlesongname);
        setitemViewHolder.vSongFilename.setTextSize(titleSize);
        setitemViewHolder.vSongFilename.setText(filename);
        setitemViewHolder.vSongFolder.setTextSize(subtitleSizeFile);
        setitemViewHolder.vSongFilename.setVisibility(useTitle ? View.GONE:View.VISIBLE);
        setitemViewHolder.vSongTitle.setVisibility(useTitle ? View.VISIBLE:View.GONE);
        setitemViewHolder.vSongFolder.setText(newfoldername);
        int icon = mainActivityInterface.getSetActions().getItemIcon(si.songicon);

        setitemViewHolder.vItem.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
    }
*//*

 */
/*   @NonNull
    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.view_set_item, viewGroup, false);

        return new SetItemViewHolder(mainActivityInterface, itemView, itemTouchHelper, this);
    }

*//*

    */
/*@Override
    public void onItemMoved(int fromPosition, int toPosition) {
        if (setList!=null && setList.size()>fromPosition && setList.size()>toPosition &&
                mainActivityInterface.getCurrentSet().getSetItems()!=null &&
                mainActivityInterface.getCurrentSet().getSetItems().size()>fromPosition) {
            dragPosition = toPosition;

            // Swap positions in the current set
            mainActivityInterface.getCurrentSet().swapPositions(fromPosition,toPosition);

            // Update the set string and save it
            mainActivityInterface.getCurrentSet().setSetCurrent(mainActivityInterface.getSetActions().getSetAsPreferenceString());

            //Log.d(TAG,"try to move from:"+fromPosition+" to:"+toPosition +"  (setList.size():"+setList.size()+"  currentSet.size():"+mainActivityInterface.getCurrentSet().getSetItems().size()+")");

            SetItemInfo fromSong = setList.get(fromPosition);
            SetItemInfo toSong = setList.get(toPosition);

            // Update the numbers
            fromSong.songitem = (toPosition + 1) + ".";
            toSong.songitem = (fromPosition + 1) + ".";

            // Now put back
            setList.set(fromPosition,toSong);
            setList.set(toPosition,fromSong);

            // Switch the highlighted array values
            boolean from_highlighted = highlightedArray.get(fromPosition, false);
            boolean to_highlighted = highlightedArray.get(toPosition, false);
            highlightedArray.put(fromPosition, to_highlighted);
            highlightedArray.put(toPosition, from_highlighted);

            // Notify the changes
            uiHandler.post(() -> {
                        notifyItemChanged(fromPosition);
                        notifyItemChanged(toPosition);
                        notifyItemMoved(fromPosition, toPosition);
                    });

            mainActivityInterface.updateInlineSetMove(fromPosition, toPosition);

            // Update the title
            mainActivityInterface.updateSetTitle();
            updateSetPrevNext();
        }
    }
*//*

*/
/*
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
*//*


*/
/*    @Override
    public void onItemClicked(MainActivityInterface mainActivityInterface, int position) {
        updateHighlightedItem(position);
        mainActivityInterface.initialiseInlineSetItem(position);
        mainActivityInterface.loadSongFromSet(position);
    }*//*


*/
/*    @Override
    public void onContentChanged(int position) {
        uiHandler.post(() -> notifyItemChanged(position));
    }*//*


*/
/*    @Override
    public void onRowSelected(SetListItemViewHolder myViewHolder) {

    }

    @Override
    public void onRowClear(SetListItemViewHolder myViewHolder) {

    }*//*


*/
/*    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {

    }*//*


*/
/*
    public void updateHighlightedItem(int position) {
        int oldPosition = currentPosition;
        currentPosition = position;
        if (oldPosition != -1) {
            highlightedArray.put(oldPosition, false);
            uiHandler.post(() -> notifyItemChanged(oldPosition, "highlightItem"));
        }
        highlightedArray.put(currentPosition, true);
        uiHandler.post(() -> notifyItemChanged(currentPosition, "highlightItem"));
    }
*//*

*/
/*

    public ArrayList<SetItemInfo> getSetList() {
        return setList;
    }
*//*


*/
/*
    private void updateSetPrevNext() {
        mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
        mainActivityInterface.getDisplayPrevNext().setPrevNext();
    }
*//*


*/
/*
    public boolean initialiseSetItem(int setPosition) {
        // Only used when app boots and we are already viewing a set item
        // This comes via the MyToolbar where we add a tick for a set item
        if (currentPosition != setPosition) {
            // If we already had a currentPosition, clear it
            if (currentPosition != -1) {
                highlightedArray.put(currentPosition, false);
                uiHandler.post(() -> notifyItemChanged(currentPosition, "highlightItem"));
            }
            // Now highlight the loaded position
            currentPosition = setPosition;
            highlightedArray.put(currentPosition, true);
            uiHandler.post(() -> notifyItemChanged(currentPosition, "highlightItem"));
            mainActivityInterface.initialiseInlineSetItem(setPosition);

            return true;
        }
        return false;
    }
*//*

*/
/*

    public void itemRemoved(int position) {
        //Log.d(TAG,"setListSize before:"+setList.size());
            setList.remove(position);
            //Log.d(TAG,"setListSize after:"+setList.size());
            notifyItemRemoved(position);
            //Log.d(TAG,"itemRemoved called");
            int currentSetPosition = mainActivityInterface.getCurrentSet().getIndexSongInSet();
            // If item is removed before the current item, we need to adjust that down too
            if (position < currentSetPosition && position > -1) {
                highlightedArray.put(currentSetPosition, false);
                highlightedArray.put(currentSetPosition - 1, true);
                mainActivityInterface.getCurrentSet().setIndexSongInSet(currentSetPosition - 1);
            } else if (position == currentSetPosition) {
                // Remove the current set position as no longer valid
                highlightedArray.put(currentSetPosition, false);
                mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
            }

            //Log.d(TAG,"position:"+position);

            // Go through the setList from this position and sort the numbers
            for (int x = position; x < setList.size(); x++) {
                setList.get(x).songitem = (x + 1) + ".";
                //Log.d(TAG,"Updated to: "+setList.get(x).songitem+" "+setList.get(x).songfilename);

            }
        uiHandler.post(() -> notifyItemRangeChanged(position, setList.size() - 1));
            // Update the inline set too
            mainActivityInterface.updateInlineSetRemoved(position);
    }
*//*


    */
/*public void addNewItem(int currentSetPosition) {
        setList.add(makeSetItem(currentSetPosition));
        uiHandler.post(() -> notifyItemInserted(setList.size()-1));

        // Update the inline set too
        mainActivityInterface.updateInlineSetAdded(setList.get(currentSetPosition));
    }*//*


    */
/*public void updateItem(int position) {
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

                mainActivityInterface.getCurrentSet().updateSetTitleView();
                updateHighlightedItem(position);
                uiHandler.post(() -> notifyItemChanged(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*//*


  */
/*  public int getSelectedPosition() {
        return mainActivityInterface.getCurrentSet().getIndexSongInSet();
    }*//*


  */
/*  public void recoverCurrentSetPosition() {
        // Get the set position as we might have moved things around
        notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(),"highlightItem");
    }

    public void clearOldHighlight(int position) {
        highlightedArray.put(position,false);
        uiHandler.post(() -> notifyItemChanged(position,"highlightItem"));
        currentPosition = position;
    }*//*


 */
/*   private void showSetList() {
        for (SetItemInfo setItemInfo:setList) {
            Log.d(TAG,"setItemInfo:  "+setItemInfo.songitem+": "+setItemInfo.songfilename);
        }
    }
*//*


*/
/*    public void updateKeys() {
        for (int position:mainActivityInterface.getSetActions().getMissingKeyPositions()) {
            try {
                setList.get(position).songkey = mainActivityInterface.getCurrentSet().getKey(position);
                uiHandler.post(() -> notifyItemChanged(position));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mainActivityInterface.getSetActions().nullMissingKeyPositions();
    }*//*

*/
/*
    public void resetSetList() {
        if (setList!=null) {
            setList.clear();
        } else {
            setList = new ArrayList<>();
        }
    }*//*

}*/
