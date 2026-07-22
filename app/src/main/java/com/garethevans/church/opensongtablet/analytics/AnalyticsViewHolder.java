package com.garethevans.church.opensongtablet.analytics;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;

public class AnalyticsViewHolder extends RecyclerView.ViewHolder {

    final MyMaterialSimpleTextView date;
    final MyMaterialSimpleTextView counter;
    final MyMaterialTextView songInfo;

    public AnalyticsViewHolder(@NonNull View itemView) {
        super(itemView);
        songInfo = itemView.findViewById(R.id.songInfo);
        date = itemView.findViewById(R.id.date);
        counter = itemView.findViewById(R.id.counter);
    }
}
