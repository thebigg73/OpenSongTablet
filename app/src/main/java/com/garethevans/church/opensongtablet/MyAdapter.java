package com.garethevans.church.opensongtablet;

import android.graphics.drawable.GradientDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        String newfoldername = si.songfolder;
        if (newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**","");
        }
        setitemViewHolder.vSongTitle.setText(si.songtitle);
        setitemViewHolder.vSongFolder.setText(newfoldername);
        boolean issong = false;
        if (si.songicon.equals(FullscreenActivity.text_slide)) {
            //setitemViewHolder.vIcon.setImageResource(R.drawable.blackout_project_dark);
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_projector_screen_white_18dp);
        } else if (si.songicon.equals(FullscreenActivity.text_note)) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_note_text_white_18dp);
        } else if (si.songicon.equals(FullscreenActivity.text_scripture)) {
            //setitemViewHolder.vIcon.setImageResource(R.drawable.action_scripture_dark);
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_book_white_18dp);
        } else if (si.songicon.equals(FullscreenActivity.image)) {
            //setitemViewHolder.vIcon.setImageResource(R.drawable.ic_action_picture_dark);
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_image_white_18dp);
        } else if (si.songicon.equals(FullscreenActivity.text_variation)) {
            //setitemViewHolder.vIcon.setImageResource(R.drawable.action_variation_dark);
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_file_xml_white_18dp);
        } else if (si.songicon.equals(".pdf")) {
            //setitemViewHolder.vIcon.setImageResource(R.drawable.action_pdf_dark);
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_file_pdf_white_18dp);
        } else {
            //setitemViewHolder.vIcon.setImageResource(R.drawable.action_song_dark);
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_music_note_white_18dp);
            issong = true;
        }

        String folderrelocate;
        if (si.songicon.equals(FullscreenActivity.image)) {
            folderrelocate = "../Images/_cache";
        } else if (si.songicon.equals(FullscreenActivity.text_note)) {
            folderrelocate = "../Notes/_cache";
        } else if (si.songicon.equals(FullscreenActivity.text_scripture)) {
            folderrelocate = "../Scripture/_cache";
        } else if (si.songicon.equals(FullscreenActivity.text_slide)) {
            folderrelocate = "../Slides/_cache";
        } else if (si.songicon.equals(FullscreenActivity.variation)) {
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
        setitemViewHolder.vCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.songfilename = songname;
                if (songfolder.equals(FullscreenActivity.mainfoldername)) {
                    FullscreenActivity.whatsongforsetwork = "$**_" + songname + "_**$";
                } else {
                    FullscreenActivity.whatsongforsetwork = "$**_" + songfolder + "/" + songname + "_**$";
                }
                //FullscreenActivity.whatsongforsetwork = songname;
                FullscreenActivity.whichSongFolder = songfolder;
                FullscreenActivity.indexSongInSet = item;
                FullscreenActivity.nextSongInSet = "";
                FullscreenActivity.previousSongInSet = "";
                // Get set position
                boolean issue = false;
                if (item > 0 && FullscreenActivity.mSet.length >= item - 1) {
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

                if (FullscreenActivity.whattodo.equals("setitemvariation")) {
                    PopUpSetViewNew.makeVariation();

                } else {
                    PopUpSetViewNew.loadSong();
                }
            }
        });

        if (FullscreenActivity.whattodo.equals("setitemvariation") && !issong) {
            // Only songs should be able to have variations
            setitemViewHolder.vCard.setOnClickListener(null);
        }
    }

    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.recycler_row, viewGroup, false);

        RelativeLayout card = (RelativeLayout) itemView.findViewById(R.id.cardview_layout);
        card.setBackgroundResource(R.drawable.section_box);
        GradientDrawable drawable = (GradientDrawable) card.getBackground();
        drawable.setColor(FullscreenActivity.lyricsBackgroundColor);

        TextView title = (TextView) itemView.findViewById(R.id.cardview_songtitle);
        title.setTextColor(FullscreenActivity.lyricsTextColor);

        TextView item = (TextView) itemView.findViewById(R.id.cardview_item);
        item.setTextColor(FullscreenActivity.lyricsTextColor);

        TextView folder = (TextView) itemView.findViewById(R.id.cardview_folder);
        folder.setTextColor(FullscreenActivity.lyricsTextColor);

        return new SetItemViewHolder(itemView);
    }

    public static class SetItemViewHolder extends RecyclerView.ViewHolder {

        protected TextView vItem;
        protected TextView vSongTitle;
        protected TextView vSongFolder;
        //protected ImageView vIcon;
        protected FloatingActionButton vIcon;
        protected RelativeLayout vCard;

        public SetItemViewHolder(View v) {
            super(v);
            vCard = (RelativeLayout) v.findViewById(R.id.cardview_layout);
            vItem = (TextView) v.findViewById(R.id.cardview_item);
            vSongTitle =  (TextView) v.findViewById(R.id.cardview_songtitle);
            vSongFolder = (TextView)  v.findViewById(R.id.cardview_folder);
            vIcon = (FloatingActionButton)  v.findViewById(R.id.cardview_icon);
            //vIcon = (ImageView)  v.findViewById(R.id.cardview_icon);
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