package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;

class SongItemViewHolder extends RecyclerView.ViewHolder {

    final TextView itemTitle;
    final TextView itemFolder;
    final TextView itemAuthor;
    final TextView itemKey;
    final CheckBox itemChecked;
    final CardView itemCard;

    SongItemViewHolder(View v) {
        super(v);
        itemCard = v.findViewById(R.id.card_view);
        itemTitle = v.findViewById(R.id.cardview_title);
        itemFolder = v.findViewById(R.id.cardview_folder);
        itemAuthor = v.findViewById(R.id.cardview_author);
        itemKey = v.findViewById(R.id.cardview_key);
        itemChecked = v.findViewById(R.id.cardview_setcheck);
    }
}