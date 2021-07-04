package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;

class SongItemViewHolder extends RecyclerView.ViewHolder {

    final TextView itemTitle;
    final TextView itemFolder;
    final TextView itemAuthor;
    final TextView itemKey;
    final CheckBox itemChecked;
    final LinearLayout itemCard;

    SongItemViewHolder(View v) {
        super(v);
        itemCard = v.findViewById(R.id.songClickSpace);
        itemTitle = v.findViewById(R.id.cardview_title);
        itemFolder = v.findViewById(R.id.cardview_folder);
        itemAuthor = v.findViewById(R.id.cardview_author);
        itemKey = v.findViewById(R.id.cardview_key);
        itemChecked = v.findViewById(R.id.cardview_setcheck);
    }
}