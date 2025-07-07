package com.garethevans.church.opensongtablet.nearby;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textview.MaterialTextView;

public class NeabyItemViewHolder extends RecyclerView.ViewHolder {

    public final LinearLayout itemLayout;
    public final CheckBox checkBox;
    public final MaterialTextView lastModified;
    public final MaterialTextView title;
    public final MaterialTextView folder;
    public final MaterialTextView comparison;

    public NeabyItemViewHolder(@NonNull View itemView) {
        super(itemView);
        itemLayout = itemView.findViewById(R.id.itemLayout);
        checkBox = itemView.findViewById(R.id.checkBoxItem);
        checkBox.setClickable(false);
        title = itemView.findViewById(R.id.title);
        folder = itemView.findViewById(R.id.folder);
        comparison = itemView.findViewById(R.id.comparison);
        lastModified = itemView.findViewById(R.id.lastModified);
    }
}
