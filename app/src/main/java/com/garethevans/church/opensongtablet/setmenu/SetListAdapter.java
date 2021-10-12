package com.garethevans.church.opensongtablet.setmenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.FastScroller;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;

import java.util.List;

public class SetListAdapter extends RecyclerView.Adapter<SetItemViewHolder> implements FastScroller.SectionIndexer, SetItemTouchInterface {

    // All the helpers we need to access are in the MainActivity
    private final MainActivityInterface mainActivityInterface;
    private List<SetItemInfo> setList;
    private ItemTouchHelper itemTouchHelper;
    private final String TAG = "SetListAdapter";

    public void setTouchHelper(ItemTouchHelper itemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper;
    }

    SetListAdapter(MainActivityInterface mainActivityInterface, List<SetItemInfo> setList) {
        this.mainActivityInterface = mainActivityInterface;
        this.setList = setList;
    }

    public void changeSetList(List<SetItemInfo> setList) {
        this.setList = setList;
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SetItemViewHolder setitemViewHolder, int i) {
        Context c = mainActivityInterface.getActivity();
        SetItemInfo si = setList.get(i);
        String key = si.songkey;
        String titlesongname = si.songtitle;
        if (key!=null && !key.equals("null") && !key.isEmpty()) {
            titlesongname = titlesongname + " ("+key+")";
        } else {
            si.songkey = "";
        }

        setitemViewHolder.vItem.setText(si.songitem);
        String newfoldername = si.songfoldernice;
        if (newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**","");
        }
        setitemViewHolder.vSongTitle.setText(titlesongname);
        setitemViewHolder.vSongFolder.setText(newfoldername);
        int icon = mainActivityInterface.getSetActions().getItemIcon(si.songicon);

        setitemViewHolder.vItem.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);

    }

    @NonNull
    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.view_set_item, viewGroup, false);

        return new SetItemViewHolder(mainActivityInterface,itemView,itemTouchHelper,this);
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        String thisFolder = mainActivityInterface.getCurrentSet().getFolder(fromPosition);
        String thisFilename = mainActivityInterface.getCurrentSet().getFilename(fromPosition);
        String thisKey = mainActivityInterface.getCurrentSet().getKey(fromPosition);
        String thisSetItem = mainActivityInterface.getCurrentSet().getItem(fromPosition);

        // Remove from this position
        mainActivityInterface.getCurrentSet().removeFromCurrentSet(fromPosition,null);

        // Add to the new position
        mainActivityInterface.getCurrentSet().addToCurrentSet(toPosition,thisSetItem,thisFolder,thisFilename,thisKey);

        // Update the set string
        mainActivityInterface.getCurrentSet().setCurrentSetString(mainActivityInterface.getSetActions().getSetAsPreferenceString(mainActivityInterface));

        // Save the preference
        mainActivityInterface.getPreferences().setMyPreferenceString((Context)mainActivityInterface,"setCurrent", mainActivityInterface.getCurrentSet().getCurrentSetString());

        SetItemInfo thisItem = setList.get(fromPosition);
        setList.get(fromPosition).songitem = (toPosition+1) + ".";
        setList.get(toPosition).songitem = (fromPosition+1) + ".";
        setList.remove(thisItem);
        setList.add(toPosition,thisItem);
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
        notifyItemMoved(fromPosition,toPosition);

        // Update the title
        mainActivityInterface.updateSetTitle();
    }

    @Override
    public void onItemSwiped(int fromPosition) {
        try {
            // Remove the item from the current set
            mainActivityInterface.getCurrentSet().removeFromCurrentSet(fromPosition,null);

            // Update the set string
            mainActivityInterface.getCurrentSet().setCurrentSetString(mainActivityInterface.getSetActions().getSetAsPreferenceString(mainActivityInterface));

            // Save the preference
            mainActivityInterface.getPreferences().setMyPreferenceString((Context)mainActivityInterface,"setCurrent", mainActivityInterface.getCurrentSet().getCurrentSetString());

            setList.remove(fromPosition);
            notifyItemRemoved(fromPosition);
            // Go through the setList from this position and sort the numbers
            for (int x = 0; x < setList.size(); x++) {
                setList.get(x).songitem = (x + 1) + ".";
                notifyItemChanged(x);
            }

            // Update the title
            mainActivityInterface.updateSetTitle();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClicked(MainActivityInterface mainActivityInterface, int position) {
        mainActivityInterface.loadSongFromSet(position);
    }

    @Override
    public void onContentChanged(int position) {
        notifyItemChanged(position);
        Log.d(TAG,"CHANGES: setCurrent:"+mainActivityInterface.getCurrentSet().getCurrentSetString());
    }

    @Override
    public CharSequence getSectionText(int position) {
        if (setList!=null && setList.size()>position) {
            return setList.get(position).songitem;
        } else {
            return "";
        }
    }
}