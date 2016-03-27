package com.garethevans.church.opensongtablet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.SetItemViewHolder> {

    //private static List<SetItemInfo> setList;
    private List<SetItemInfo> setList;

    public MyAdapter(List<SetItemInfo> setList) {
        this.setList = setList;
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    @Override
    public void onBindViewHolder(SetItemViewHolder setitemViewHolder, int i) {
        SetItemInfo si = setList.get(i);
        setitemViewHolder.vItem.setText(si.songitem);
        setitemViewHolder.vSongTitle.setText(si.songtitle);
        setitemViewHolder.vSongFolder.setText(si.songfolder);
        if (si.songicon.equals(FullscreenActivity.text_slide)) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.blackout_project_dark);
        } else if (si.songicon.equals(FullscreenActivity.text_note)) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_action_view_as_list_dark);
        } else if (si.songicon.equals(FullscreenActivity.text_scripture)) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.action_scripture_dark);
        } else if (si.songicon.equals(FullscreenActivity.image)) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_action_picture_dark);
        } else if (si.songicon.equals(".pdf")) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.action_pdf_dark);
        } else {
            setitemViewHolder.vIcon.setImageResource(R.drawable.action_song_dark);
        }

        String folderrelocate;
        if (si.songicon.equals(FullscreenActivity.image)) {
            folderrelocate = "../Images/_cache";
        } else if (si.songicon.equals(FullscreenActivity.text_note)) {
            folderrelocate = "../Notes/_cache";
        } else if (si.songicon.equals(FullscreenActivity.text_scripture)) {
            folderrelocate = "../OpenSong Scripture/_cache";
        } else if (si.songicon.equals(FullscreenActivity.text_slide)) {
            folderrelocate = "../Slides/_cache";
        } else {
            folderrelocate = si.songfolder;
        }

        final String songname = si.songtitle;
        final String songfolder = folderrelocate;
        int getitemnum;
        try {
            getitemnum = Integer.parseInt(si.songitem.replace(".",""));
        } catch (Exception e) {
            getitemnum = 0;
        }
        getitemnum --;
        final int item = getitemnum;
        setitemViewHolder.vCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.songfilename = songname;
                if (songfolder.equals(FullscreenActivity.mainfoldername)) {
                    FullscreenActivity.whatsongforsetwork = "$**_" + songname + "_**$";
                } else {
                    FullscreenActivity.whatsongforsetwork = "$**_" + songfolder + "/" + songname + "_**$";
                }
                FullscreenActivity.whatsongforsetwork = songname;
                FullscreenActivity.whichSongFolder = songfolder;
                FullscreenActivity.indexSongInSet = item;
                FullscreenActivity.nextSongInSet = "";
                FullscreenActivity.previousSongInSet = "";
                // Get set position
                boolean issue = false;
                if (item>0 && FullscreenActivity.mSet.length>=item-1) {
                    FullscreenActivity.previousSongInSet = FullscreenActivity.mSet[item - 1];
                } else {
                    issue = true;
                }
                if (item != FullscreenActivity.setSize - 1) {
                    FullscreenActivity.nextSongInSet = FullscreenActivity.mSet[item + 1];
                } else {
                    issue = true;
                }

                if (issue) {
                    SetActions.indexSongInSet();
                }

                PopUpSetViewNew.loadSong();
            }
        });
    }

    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recycler_row, viewGroup, false);

        return new SetItemViewHolder(itemView);
    }

    public static class SetItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView vItem;
        protected TextView vSongTitle;
        protected TextView vSongFolder;
        protected ImageView vIcon;
        protected RelativeLayout vCard;

        public SetItemViewHolder(View v) {
            super(v);
            vCard = (RelativeLayout) v.findViewById(R.id.cardview_layout);
            vItem = (TextView) v.findViewById(R.id.cardview_item);
            vSongTitle =  (TextView) v.findViewById(R.id.cardview_songtitle);
            vSongFolder = (TextView)  v.findViewById(R.id.cardview_folder);
            vIcon = (ImageView)  v.findViewById(R.id.cardview_icon);
        }
    }

    public void swap(int firstPosition, int secondPosition){
        Collections.swap(setList, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
        Collections.swap(FullscreenActivity.mTempSetList, firstPosition, secondPosition);
        Collections.swap(PopUpSetViewNew.mSongName, firstPosition, secondPosition);
        Collections.swap(PopUpSetViewNew.mFolderName, firstPosition, secondPosition);
    }

    public void remove(int position) {
        setList.remove(position);
        notifyItemRemoved(position);
        FullscreenActivity.mTempSetList.remove(position);
        PopUpSetViewNew.mSongName.remove(position);
        PopUpSetViewNew.mFolderName.remove(position);
    }

}