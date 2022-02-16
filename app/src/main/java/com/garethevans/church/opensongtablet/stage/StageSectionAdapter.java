package com.garethevans.church.opensongtablet.stage;

// This deals with displaying the song in StageMode (actually using a recyclerView in PerformanceMode)

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;
import java.util.List;

public class StageSectionAdapter extends RecyclerView.Adapter<StageViewHolder> {

    // All the helpers we need to access are in the MainActivity
    private final String TAG = "StageSectionAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private ArrayList<StageSectionInfo> sectionInfos;
    private ArrayList<Float> floatSizes;
    private float floatHeight = 0;
    private int currentSection = 0;
    private final float maxFontSize;
    private final float density;
    private final String alphaChange = "alpha";

    public StageSectionAdapter(Context c, MainActivityInterface mainActivityInterface, DisplayInterface displayInterface) {
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        density = c.getResources().getDisplayMetrics().density;
        maxFontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat(c,"fontSizeMax",50f);
        setSongInfo();
    }

    private void setSongInfo() {
        // Prepare the info for each section
        sectionInfos = new ArrayList<>();
        floatSizes = new ArrayList<>();
        floatHeight = 0;

        for (int x=0; x<mainActivityInterface.getSectionViews().size(); x++) {
            StageSectionInfo stageSectionInfo = new StageSectionInfo();
            stageSectionInfo.section = x;

            if (mainActivityInterface.getMode().equals("Stage")) {
                stageSectionInfo.alpha = 0.4f;
            } else {
                stageSectionInfo.alpha = 1f;
            }

            int sectionWidth = mainActivityInterface.getSectionWidths().get(x);
            int sectionHeight = mainActivityInterface.getSectionHeights().get(x);

            stageSectionInfo.width = sectionWidth;
            stageSectionInfo.height = sectionHeight;

            float x_scale = (float)(mainActivityInterface.getDisplayMetrics()[0]-16)/(float)sectionWidth;
            float y_scale = (float)(mainActivityInterface.getDisplayMetrics()[1]-mainActivityInterface.getAppActionBar().getActionBarHeight())*0.75f/(float)sectionHeight;
            float scale = Math.min(x_scale,y_scale);
            // Check the scale isn't bigger than the maximum font size
            scale = Math.min(scale,(maxFontSize/14f));
            stageSectionInfo.scale = scale;

            float itemHeight = sectionHeight * scale + (4f * density);
            floatHeight += itemHeight;
            floatSizes.add (itemHeight);

            sectionInfos.add(stageSectionInfo);
        }
    }

    @NonNull
    @Override
    public StageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_stage_section, parent, false);
        return new StageViewHolder(mainActivityInterface,itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StageViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals(alphaChange)) {
                    // We want to update the highlight colour to off
                    holder.v.setAlpha(sectionInfos.get(position).alpha);
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StageViewHolder holder, int position) {
        int section = sectionInfos.get(position).section;
        int width = sectionInfos.get(position).width;
        int height = sectionInfos.get(position).height;
        float scale = sectionInfos.get(position).scale;
        float alpha = sectionInfos.get(position).alpha;
        CardView cardView = (CardView)holder.v;
        if (mainActivityInterface.getMode().equals("Stage") && position == currentSection) {
            alpha = 1.0f;
        }
        cardView.setAlpha(alpha);
        View v = mainActivityInterface.getSectionViews().get(position);
        v.setPivotX(0);
        v.setPivotY(0);
        v.setScaleX(scale);
        v.setScaleY(scale);
        holder.sectionView.getLayoutParams().width = (int)(width*scale);
        holder.sectionView.getLayoutParams().height = (int)(height*scale);
        cardView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
        ViewCompat.setBackgroundTintList(cardView, ColorStateList.valueOf(mainActivityInterface.getSectionColors().get(position)));
        cardView.getLayoutParams().height = (int)(height*scale);

        // Check it isn't already added to a layout
        if (v.getParent()!=null) {
            v.post(() -> {
                // Run as a post to avoid inproper requestLayout
                ((FrameLayout)v.getParent()).removeAllViews();
                holder.sectionView.addView(v);
            });
        } else {
            holder.sectionView.addView(v);
        }

        cardView.setOnClickListener(view -> sectionSelected(section));
        cardView.setOnLongClickListener(view -> {
            // Do nothing, but consume the event
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mainActivityInterface.getSectionViews().size();
    }

    public int getHeight() {
        return (int) floatHeight;
    }

    private void onTouchAction() {
        mainActivityInterface.getDisplayPrevNext().showAndHide();
        mainActivityInterface.updateOnScreenInfo("showhide");
        mainActivityInterface.showHideActionBar();
    }

    public void sectionSelected(int position) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        // Only do this alpha change in stage mode

        // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
        onTouchAction();

        if (mainActivityInterface.getMode().equals("Stage")) {
            sectionInfos.get(currentSection).alpha = 0.4f;
            notifyItemChanged(currentSection, alphaChange);

            // Now update the newly selected position
            if (position >= 0 && position < sectionInfos.size()) {
                mainActivityInterface.getSong().setCurrentSection(position);
                currentSection = position;
                sectionInfos.get(position).alpha = 1.0f;
                notifyItemChanged(position, alphaChange);
            }
        }

        // Send and update notification to Performance Fragment via the MainActivity
        displayInterface.performanceShowSection(position);
    }

    public ArrayList<Float> getHeights() {
        return floatSizes;
    }

}
