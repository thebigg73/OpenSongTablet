package com.garethevans.church.opensongtablet.presenter;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;

public class SongSectionViewHolder extends RecyclerView.ViewHolder {

    final CardView item;
    final MyMaterialSimpleTextView heading;
    final MyMaterialSimpleTextView content;
    final ImageView image;
    final MyFloatingActionButton edit;

    public SongSectionViewHolder(@NonNull View itemView) {
        super(itemView);
        item = itemView.findViewById(R.id.item);
        heading = itemView.findViewById(R.id.heading);
        content = itemView.findViewById(R.id.content);
        image = itemView.findViewById(R.id.image);
        edit = itemView.findViewById(R.id.edit);
        heading.setFocusable(false);
        heading.setFocusableInTouchMode(false);
        heading.setClickable(false);
        content.setFocusable(false);
        content.setFocusableInTouchMode(false);
        content.setClickable(false);
        image.setFocusable(false);
        image.setFocusableInTouchMode(false);
        image.setClickable(false);
        edit.setFocusable(false);
        edit.setFocusableInTouchMode(false);
        edit.setClickable(false);
    }
}
