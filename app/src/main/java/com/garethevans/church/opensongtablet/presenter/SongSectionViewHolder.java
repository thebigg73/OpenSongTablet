package com.garethevans.church.opensongtablet.presenter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textview.MaterialTextView;

public class SongSectionViewHolder extends RecyclerView.ViewHolder {

    final CardView item;
    final MaterialTextView heading;
    final MaterialTextView content;
    final ImageView image;

    public SongSectionViewHolder(@NonNull View itemView) {
        super(itemView);
        item = itemView.findViewById(R.id.item);
        heading = itemView.findViewById(R.id.title);
        content = itemView.findViewById(R.id.content);
        image = itemView.findViewById(R.id.image);
    }
}
