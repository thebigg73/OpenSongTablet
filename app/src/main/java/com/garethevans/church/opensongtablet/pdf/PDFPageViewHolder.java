package com.garethevans.church.opensongtablet.pdf;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.textview.MaterialTextView;

public class PDFPageViewHolder extends RecyclerView.ViewHolder {

    MainActivityInterface mainActivityInterface;
    View v;
    ImageView pdfPageImage;
    ImageView pdfPageHighlight;
    MaterialTextView pdfPageNumText;

    public PDFPageViewHolder(MainActivityInterface mainActivityInterface, View v) {
        super(v);
        this.v = v;
        pdfPageImage = v.findViewById(R.id.pdfPageImage);
        pdfPageHighlight = v.findViewById(R.id.pdfPageHighlight);
        pdfPageNumText = v.findViewById(R.id.pdfPageNumText);
        this.mainActivityInterface = mainActivityInterface;
    }
}
