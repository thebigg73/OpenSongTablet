package com.garethevans.church.opensongtablet.drummer;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textview.MaterialTextView;

public class BBSongViewHolder extends RecyclerView.ViewHolder {

    LinearLayout bb_layout;
    MaterialTextView bb_folder, bb_song, bb_timesig, bb_kit;

    public BBSongViewHolder(@NonNull View itemView) {
        super(itemView);
        bb_layout = itemView.findViewById(R.id.bb_layout);
        bb_folder = itemView.findViewById(R.id.bb_folder);
        bb_song = itemView.findViewById(R.id.bb_song);
        bb_timesig = itemView.findViewById(R.id.bb_timesig);
        bb_kit = itemView.findViewById(R.id.bb_kit);
    }
}
