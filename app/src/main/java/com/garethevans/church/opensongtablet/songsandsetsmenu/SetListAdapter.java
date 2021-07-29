package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.SetItemTouchInterface;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class SetListAdapter extends RecyclerView.Adapter<SetItemViewHolder> implements FastScrollRecyclerView.SectionedAdapter, SetItemTouchInterface {

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
        String newfoldername = si.songfolder;
        if (newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**","");
        }
        setitemViewHolder.vSongTitle.setText(titlesongname);
        setitemViewHolder.vSongFolder.setText(newfoldername);
        boolean issong = false;
        int icon = mainActivityInterface.getSetActions().getItemIcon(c,si.songicon);

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


    @NonNull
    @Override
    public String getSectionName(int position) {
        return setList.get(position).songitem;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        SetItemInfo thisItem = setList.get(fromPosition);
        setList.get(fromPosition).songitem = (toPosition+1) + ".";
        setList.get(toPosition).songitem = (fromPosition+1) + ".";
        setList.remove(thisItem);
        setList.add(toPosition,thisItem);
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
        notifyItemMoved(fromPosition,toPosition);
    }

    @Override
    public void onItemSwiped(int fromPosition) {
        try {
            setList.remove(fromPosition);
            notifyItemRemoved(fromPosition);
            // Go through the setList from this position and sort the numbers
            for (int x = fromPosition; x < setList.size(); x++) {
                setList.get(x).songitem = (x + 1) + ".";
                notifyItemChanged(x);
            }
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
    }
}