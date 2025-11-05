package com.garethevans.church.opensongtablet.setprocessing;

import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyFloatingActionButton;
import com.garethevans.church.opensongtablet.customviews.MyMaterialCheckbox;
import com.garethevans.church.opensongtablet.customviews.MyMaterialSimpleTextView;

public class ImportSetItemViewHolder extends RecyclerView.ViewHolder {

    final CardView cardView;
    final MyMaterialSimpleTextView cardItem, cardTitle, cardFilename, cardFolder, cardExists;
    final MyMaterialCheckbox cardCheckBox;
    final RelativeLayout cardLayout;
    final MyFloatingActionButton cardEdit;

    public ImportSetItemViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.card_view);
        cardLayout = itemView.findViewById(R.id.cardview_layout);
        cardItem = itemView.findViewById(R.id.cardview_item);
        cardTitle = itemView.findViewById(R.id.cardview_songtitle);
        cardFilename = itemView.findViewById(R.id.cardview_songfilename);
        cardFolder = itemView.findViewById(R.id.cardview_folder);
        cardEdit = itemView.findViewById(R.id.cardview_edit);
        cardCheckBox = itemView.findViewById(R.id.cardview_checkbox);
        cardCheckBox.setVisibility(View.VISIBLE);
        cardExists = itemView.findViewById(R.id.cardview_exists);
    }
}
