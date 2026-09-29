package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.screensetup.Palette;

public class MyScrimmedProgress extends RelativeLayout {

    private View scrimBackground;
    private ProgressBar progressBar;
    private Palette palette;

    // Constructor for creating the view programmatically
    public MyScrimmedProgress(Context context) {
        super(context);
        init(context);
    }

    // Constructor required for inflating the view from XML
    public MyScrimmedProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    // Constructor for XML inflation with style attributes
    public MyScrimmedProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        palette = new Palette(context);

        // Correctly inflate the XML layout into this RelativeLayout container
        LayoutInflater.from(context).inflate(R.layout.view_my_scrimmed_progress, this, true);

        // Bind views
        scrimBackground = findViewById(R.id.scrimBackground);
        progressBar = findViewById(R.id.progressBar);

        // Safe to instantiate dynamic application components at runtime
        palette = new Palette(context);
        progressBar.getIndeterminateDrawable().setColorFilter(palette.secondary, PorterDuff.Mode.SRC_IN);
        scrimBackground.setBackgroundColor(palette.background);

        // Allow hiding by clicking
        setOnClickListener(v-> setVisibility(View.GONE));
    }

    /**
     * Shows or hides the entire progress view.
     */
    public void setProgressVisible(boolean visible) {
        setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Customise the scrim background color dynamically if needed.
     */
    public void setScrimColor(int colorInt) {
        if (scrimBackground != null) {
            scrimBackground.setBackgroundColor(colorInt);
        }
    }

    // Optional: Expose getters if you need direct configuration externally
    public View getScrimBackground() {
        return scrimBackground;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

}
