package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;

public class SongContent extends LinearLayout {

    private LinearLayout songContent, songContent_col1, songContent_col2, songContent_col3;
    private ImageView songContent_img;
    private boolean isDisplaying = false;

    public SongContent(Context context) {
        super(context);
        initialise(context);
    }
    public SongContent(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    private void initialise(Context context) {
        inflate(context, R.layout.view_song_content,this);

        songContent = findViewById(R.id.songContent);
        songContent_col1 = findViewById(R.id.songContent_col1);
        songContent_col2 = findViewById(R.id.songContent_col2);
        songContent_col3 = findViewById(R.id.songContent_col3);
        songContent_img = findViewById(R.id.songContent_img);

        songContent.setId(View.generateViewId());
        songContent_col1.setId(View.generateViewId());
        songContent_col2.setId(View.generateViewId());
        songContent_col3.setId(View.generateViewId());
        songContent_img.setId(View.generateViewId());
    }
    public void clearViews() {
        songContent_col1.removeAllViews();
        songContent_col2.removeAllViews();
        songContent_col3.removeAllViews();
        songContent_img.setImageBitmap(null);
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

    public ImageView getImageView() {
        return songContent_img;
    }

    public void setIsDisplaying(boolean isDisplaying) {
        this.isDisplaying = isDisplaying;
    }

    public boolean getIsDisplaying() {
        return isDisplaying;
    }

    public int getVisibility() {
        if (songContent!=null) {
            return songContent.getVisibility();
        } else {
            try {
                return getVisibility();
            } catch (Exception e) {
                e.printStackTrace();
                return View.GONE;
            }
        }
    }

    public void setVisibility(int visibility) {
        if (songContent!=null) {
            songContent.setVisibility(visibility);
        } else {
            try {
                setVisibility(visibility);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
