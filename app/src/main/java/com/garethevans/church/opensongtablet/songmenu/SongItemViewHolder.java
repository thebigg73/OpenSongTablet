package com.garethevans.church.opensongtablet.songmenu;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialCheckbox;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;

public class SongItemViewHolder extends RecyclerView.ViewHolder {

    final MyMaterialSimpleTextView itemTitle;
    final MyMaterialSimpleTextView itemAuthor;
    final MyMaterialSimpleTextView itemFolderNamePair;
    final MyMaterialCheckbox itemChecked;
    final FrameLayout itemCheckedFrame;
    final LinearLayout itemCard;

    SongItemViewHolder(View v) {
        super(v);
        itemCard = v.findViewById(R.id.songClickSpace);
        itemTitle = v.findViewById(R.id.cardview_title);
        itemAuthor = v.findViewById(R.id.cardview_author);
        itemFolderNamePair = v.findViewById(R.id.cardview_foldernamepair);
        itemChecked = v.findViewById(R.id.cardview_setcheck);
        itemCheckedFrame = v.findViewById(R.id.cardview_setcheck_frame);
    }
}
