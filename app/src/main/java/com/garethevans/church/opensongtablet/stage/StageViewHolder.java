package com.garethevans.church.opensongtablet.stage;

import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class StageViewHolder extends RecyclerView.ViewHolder {

    MainActivityInterface mainActivityInterface;
    View v;
    FrameLayout sectionView;

    public StageViewHolder(MainActivityInterface mainActivityInterface, View v) {
        super(v);
        this.mainActivityInterface = mainActivityInterface;
        this.v = v;
        sectionView = v.findViewById(R.id.sectionView);
    }
}
