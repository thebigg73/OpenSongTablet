package com.garethevans.church.opensongtablet.appdata;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;

public class SettingsCategoryViewHolder extends RecyclerView.ViewHolder {

    public final MyMaterialSimpleTextView title;
    public final ImageView icon;

    public SettingsCategoryViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.categoryTitle);
        icon = itemView.findViewById(R.id.categoryIcon);
    }

}
