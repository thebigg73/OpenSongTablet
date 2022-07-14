package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class SongContent extends LinearLayout {

    private final LinearLayout songContent, songContent_col1, songContent_col2, songContent_col3;
    private boolean isDisplaying = false;

    public SongContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inflate(context, R.layout.view_song_content,this);

        songContent = findViewById(R.id.songContent);
        songContent_col1 = findViewById(R.id.songContent_col1);
        songContent_col2 = findViewById(R.id.songContent_col2);
        songContent_col3 = findViewById(R.id.songContent_col3);

        songContent.setId(View.generateViewId());
        songContent_col1.setId(View.generateViewId());
        songContent_col2.setId(View.generateViewId());
        songContent_col3.setId(View.generateViewId());
    }

    public void clearViews() {
        songContent_col1.removeAllViews();
        songContent_col2.removeAllViews();
        songContent_col3.removeAllViews();
    }

    public LinearLayout getCol1() {
        return songContent_col1;
    }
    public LinearLayout getCol2() {
        return songContent_col2;
    }
    public LinearLayout getCol3() {
        return songContent_col3;
    }

    public void setIsDisplaying(boolean isDisplaying) {
        this.isDisplaying = isDisplaying;
    }

    public boolean getIsDisplaying() {
        return isDisplaying;
    }

    public int getVisibility() {
        return songContent.getVisibility();
    }

    public int getVisibilityCol1() {
        return songContent_col1.getVisibility();
    }

    public int getVisibilityCol2() {
        return songContent_col2.getVisibility();
    }

    public int getVisibilityCol3() {
        return songContent_col3.getVisibility();
    }
}
