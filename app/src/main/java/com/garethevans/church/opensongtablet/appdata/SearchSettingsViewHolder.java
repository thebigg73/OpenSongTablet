package com.garethevans.church.opensongtablet.appdata;

import android.view.View;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;

public class SearchSettingsViewHolder extends RecyclerView.ViewHolder {

    LinearLayout searchItemLayout;
    MyMaterialTextView itemView, menuView;
    String action;

    public SearchSettingsViewHolder(View view) {
        super(view);
        searchItemLayout = view.findViewById(R.id.searchItemLayout);
        itemView = view.findViewById(R.id.searchItem);
        menuView = view.findViewById(R.id.searchMenu);
    }

    public void setItem(String text,String hint,String menu,String action) {
        itemView.setText(text);
        itemView.setHint(hint);
        menuView.setText(menu);
        this.action = action;
    }
}
