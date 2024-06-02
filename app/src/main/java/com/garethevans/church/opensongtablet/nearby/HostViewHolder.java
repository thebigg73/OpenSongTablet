package com.garethevans.church.opensongtablet.nearby;

import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.google.android.material.textview.MaterialTextView;

public class HostViewHolder extends RecyclerView.ViewHolder {

    public final LinearLayout itemLayout;
    public final CheckBox checkBox;
    public final MaterialTextView modifiedDate, itemName;

    public HostViewHolder(@NonNull View itemView) {
        super(itemView);
        itemLayout = itemView.findViewById(R.id.itemLayout);
        checkBox = itemView.findViewById(R.id.checkBoxItem);
        checkBox.setClickable(false);
        itemName = itemView.findViewById(R.id.itemName);
        modifiedDate = itemView.findViewById(R.id.modifiedDate);
    }
}
