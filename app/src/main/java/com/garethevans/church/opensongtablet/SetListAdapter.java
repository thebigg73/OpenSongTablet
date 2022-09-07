package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Collections;
import java.util.List;

class SetListAdapter extends RecyclerView.Adapter<SetListAdapter.SetItemViewHolder> {

    private final List<SetItemInfo> setList;
    private final Context c;
    private final Preferences preferences;
    private final int onColor = 0xff888888, offColor = 0xff555555;
    private final String TAG = "SetListAdapter";

    SetListAdapter(List<SetItemInfo> setList, Context context, Preferences p) {
        this.setList = setList;
        c = context;
        preferences = p;
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    private void setColor(SetItemViewHolder holder, int cardColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.vIcon.setBackgroundTintList(ColorStateList.valueOf(cardColor));
        } else {
            holder.vIcon.setBackgroundColor(cardColor);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SetItemViewHolder setitemViewHolder, int i) {
        SetItemInfo si = setList.get(i);
        String key = si.songkey;
        String titlesongname = si.songtitle;
        if (!key.equals("")) {
            titlesongname = titlesongname + " ("+key+")";
        }

        setitemViewHolder.vItem.setText(si.songitem);
        String newfoldername = si.songfolder;
        if (newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**","");
        }
        setitemViewHolder.vSongTitle.setText(titlesongname);
        setitemViewHolder.vSongFolder.setText(newfoldername);
        boolean issong = false;
        if (si.songicon.equals(c.getResources().getString(R.string.slide))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_projector_screen_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.note))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_note_text_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.scripture))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_book_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.image))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_image_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.variation))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_file_xml_white_36dp);
        } else if (si.songicon.equals(".pdf")) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_file_pdf_white_36dp);
        } else {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_music_note_white_36dp);
            issong = true;
        }

        // IV - Highlight icon of current song when in the set
        // IV/GE - Fix to match on songitem number which is unchanged when the song position is changed by drag
        if (si.songitem.equals((StaticVariables.indexSongInSet + 1) + ".")) {
            Log.d(TAG,"si.songitem="+si.songitem+" On");
            setColor(setitemViewHolder,onColor);
        } else {
            Log.d(TAG,"si.songitem="+si.songitem+" Off");
            setColor(setitemViewHolder,offColor);
        }

        String folderrelocate;
        if (si.songicon.equals(c.getResources().getString(R.string.image))) {
            folderrelocate = "../Images/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.note))) {
            folderrelocate = "../Notes/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.scripture))) {
            folderrelocate = "../Scripture/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.slide))) {
            folderrelocate = "../Slides/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.variation))) {
            folderrelocate = "../Variations";
        } else {
            folderrelocate = si.songfolder;
        }

        final String songname = si.songtitle;
        final String songfolder = folderrelocate;
        int getitemnum;
        try {
            getitemnum = Integer.parseInt(si.songitem.replace(".", ""));
        } catch (Exception e) {
            getitemnum = 0;
        }
        getitemnum--;
        final int item = getitemnum;
        setitemViewHolder.vCard.setOnClickListener(v -> {
            StaticVariables.songfilename = songname;
            StaticVariables.whichSongFolder = songfolder;
            StaticVariables.indexSongInSet = item;
            StaticVariables.nextSongInSet = "";
            StaticVariables.previousSongInSet = "";
            // Get set position
            boolean issue = false;
            if (item > 0 && StaticVariables.mSet.length >= item - 1) {
                StaticVariables.previousSongInSet = StaticVariables.mSetList[item - 1];
            } else {
                issue = true;
            }
            if (item != StaticVariables.setSize - 1 && StaticVariables.mSetList.length > (item + 1)) {
                StaticVariables.nextSongInSet = StaticVariables.mSetList[item + 1];
            } else {
                issue = true;
            }

            if (issue) {
                SetActions setActions = new SetActions();
                setActions.indexSongInSet();
            }

            if (FullscreenActivity.whattodo.equals("setitemvariation")) {
                PopUpSetViewNew.makeVariation(c, preferences);

            } else {
                PopUpSetViewNew.loadSong(c,preferences);
            }
        });

        if (FullscreenActivity.whattodo.equals("setitemvariation") && !issong) {
            // Only songs should be able to have variations
            setitemViewHolder.vCard.setOnClickListener(null);
        }
    }

    @NonNull
    @Override
    public SetItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recycler_row, viewGroup, false);

        return new SetItemViewHolder(itemView);
    }

    static class SetItemViewHolder extends RecyclerView.ViewHolder {

        final TextView vItem;
        final TextView vSongTitle;
        final TextView vSongFolder;
        final FloatingActionButton vIcon;
        final RelativeLayout vCard;

        SetItemViewHolder(View v) {
            super(v);
            vCard = v.findViewById(R.id.cardview_layout);
            vItem = v.findViewById(R.id.cardview_item);
            vSongTitle = v.findViewById(R.id.cardview_songtitle);
            vSongFolder = v.findViewById(R.id.cardview_folder);
            vIcon = v.findViewById(R.id.cardview_icon);
        }
    }

    void swap(int firstPosition, int secondPosition){
        try {
            Collections.swap(setList, firstPosition, secondPosition);
            notifyItemMoved(firstPosition, secondPosition);
            Collections.swap(StaticVariables.mTempSetList, firstPosition, secondPosition);
            Collections.swap(PopUpSetViewNew.mSongName, firstPosition, secondPosition);
            Collections.swap(PopUpSetViewNew.mFolderName, firstPosition, secondPosition);
            StaticVariables.setchanged = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(int position) {
        try {
            setList.remove(position);
            notifyItemRemoved(position);
            StaticVariables.mTempSetList.remove(position);
            PopUpSetViewNew.mSongName.remove(position);
            PopUpSetViewNew.mFolderName.remove(position);
            StaticVariables.setchanged = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}