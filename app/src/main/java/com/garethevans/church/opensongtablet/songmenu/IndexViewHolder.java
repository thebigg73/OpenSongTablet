package com.garethevans.church.opensongtablet.songmenu;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;

public class IndexViewHolder extends RecyclerView.ViewHolder {
    MyMaterialSimpleTextView textView;

    public IndexViewHolder(View v) {
        super(v);
        textView = v.findViewById(R.id.alphalist);
    }
}
