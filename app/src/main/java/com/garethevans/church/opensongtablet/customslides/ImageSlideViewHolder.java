package com.garethevans.church.opensongtablet.customslides;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textview.MaterialTextView;

public class ImageSlideViewHolder extends RecyclerView.ViewHolder {

    MainActivityInterface mainActivityInterface;
    View v;
    ImageView imageSlideImage;
    ImageView imageSlideHighlight;
    MaterialTextView imageSlideNumText;

    public ImageSlideViewHolder(MainActivityInterface mainActivityInterface, View v) {
        super(v);
        this.v = v;
        imageSlideImage = v.findViewById(R.id.pdfPageImage);
        imageSlideHighlight = v.findViewById(R.id.pdfPageHighlight);
        imageSlideNumText = v.findViewById(R.id.pdfPageNumText);
        this.mainActivityInterface = mainActivityInterface;
    }
}
