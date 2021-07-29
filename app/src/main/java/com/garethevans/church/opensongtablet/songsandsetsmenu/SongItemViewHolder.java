package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;

class SongItemViewHolder extends RecyclerView.ViewHolder {

    final TextView itemTitle;
    final TextView itemAuthor;
    final CheckBox itemChecked;
    final LinearLayout itemCard;

    SongItemViewHolder(View v) {
        super(v);
        itemCard = v.findViewById(R.id.songClickSpace);
        itemTitle = v.findViewById(R.id.cardview_title);
        itemAuthor = v.findViewById(R.id.cardview_author);
        itemChecked = v.findViewById(R.id.cardview_setcheck);
    }
}