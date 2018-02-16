package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

class MyAdapter extends RecyclerView.Adapter<MyAdapter.SetItemViewHolder> {

    private List<SetItemInfo> setList;
    Context c;

    MyAdapter(List<SetItemInfo> setList, Context context) {
        this.setList = setList;
        c = context;
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    @Override
    public void onBindViewHolder(SetItemViewHolder setitemViewHolder, int i) {
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
                    PopUpSetViewNew.makeVariation(c);

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

        //RelativeLayout card = (RelativeLayout) itemView.findViewById(R.id.cardview_layout);
        //card.setPadding(8,8,8,8);
        //card.setBackgroundResource(R.drawable.section_box);
        //GradientDrawable drawable = (GradientDrawable) card.getBackground();
        //drawable.setColor(card.getContext().getResources().getColor(R.color.dark));
        //drawable.setStroke(2, 0xffffffff); // set stroke width and stroke color
        //drawable.setCornerRadius(FullscreenActivity.padding);

        //TextView title = (TextView) itemView.findViewById(R.id.cardview_songtitle);
        //title.setTextColor(FullscreenActivity.lyricsTextColor);

        //TextView item = (TextView) itemView.findViewById(R.id.cardview_item);
        //item.setTextColor(FullscreenActivity.lyricsTextColor);

        //TextView folder = (TextView) itemView.findViewById(R.id.cardview_folder);
        //folder.setTextColor(FullscreenActivity.lyricsTextColor);

        return new SetItemViewHolder(itemView);
    }

    static class SetItemViewHolder extends RecyclerView.ViewHolder {

        TextView vItem;
        TextView vSongTitle;
        TextView vSongFolder;
        //protected ImageView vIcon;
        FloatingActionButton vIcon;
        RelativeLayout vCard;

        SetItemViewHolder(View v) {
            super(v);
            vCard = v.findViewById(R.id.cardview_layout);
            vItem = v.findViewById(R.id.cardview_item);
            vSongTitle = v.findViewById(R.id.cardview_songtitle);
            vSongFolder = v.findViewById(R.id.cardview_folder);
            vIcon = v.findViewById(R.id.cardview_icon);
            //vIcon = (ImageView)  v.findViewById(R.id.cardview_icon);
        }
    }

    void swap(int firstPosition, int secondPosition){
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