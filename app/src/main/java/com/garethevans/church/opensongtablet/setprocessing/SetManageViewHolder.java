package com.garethevans.church.opensongtablet.setprocessing;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialCheckbox;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;

public class SetManageViewHolder extends RecyclerView.ViewHolder {

    public final LinearLayout itemLayout;
    public final MyMaterialCheckbox checkBox;
    public final MyMaterialSimpleTextView modifiedDate, itemName;

    public SetManageViewHolder(@NonNull View itemView) {
        super(itemView);
        itemLayout = itemView.findViewById(R.id.itemLayout);
        checkBox = itemView.findViewById(R.id.checkBoxItem);
        checkBox.setClickable(false);
        itemName = itemView.findViewById(R.id.itemName);
        modifiedDate = itemView.findViewById(R.id.modifiedDate);
    }
}
