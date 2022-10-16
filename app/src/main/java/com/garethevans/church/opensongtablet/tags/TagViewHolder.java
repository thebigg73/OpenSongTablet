package com.garethevans.church.opensongtablet.tags;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialTextView;
import com.google.android.material.card.MaterialCardView;

public class TagViewHolder extends RecyclerView.ViewHolder {

    public MaterialTextView songInfo;
    public MaterialCardView cardView;

    public TagViewHolder(@NonNull View itemView) {
        super(itemView);
        songInfo = itemView.findViewById(R.id.songInfo);
        cardView = itemView.findViewById(R.id.cardView);
    }
}
