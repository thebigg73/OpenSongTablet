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

public class StageSectionAdapter extends RecyclerView.Adapter<StageViewHolder> {

    // All the helpers we need to access are in the MainActivity
    private final String TAG = "StageSectionAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private ArrayList<StageSectionInfo> sectionInfos;
    private ArrayList<Float> sectionHeights = new ArrayList<>();
    private int currentSection = 0;
    private final float maxFontSize;

    private final float density;

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
        for (int x=0; x<mainActivityInterface.getSectionViews().size(); x++) {
            StageSectionInfo stageSectionInfo = new StageSectionInfo();
            stageSectionInfo.section = x;
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
            sectionInfos.add(stageSectionInfo);
        }
        notifyItemRangeChanged(0, mainActivityInterface.getSong().getSongSections().size());
    }

    @NonNull
    @Override
    public StageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_stage_section, parent, false);
        return new StageViewHolder(mainActivityInterface,itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StageViewHolder holder, int position) {
        int section = sectionInfos.get(position).section;
        int width = sectionInfos.get(position).width;
        int height = sectionInfos.get(position).height;
        float scale = sectionInfos.get(position).scale;
        CardView cardView = (CardView)holder.v;
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

        float alpha;
        if (position == currentSection) {
            alpha = 1.0f;
        } else {
            alpha = 0.4f;
        }
        cardView.setAlpha(alpha);

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

    public void sectionSelected(int position) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        notifyItemChanged(currentSection);

        // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
        onTouchAction();

        // Now update the newly selected position
        if (position>-1 && position<sectionInfos.size()) {
            mainActivityInterface.getSong().setCurrentSection(position);
            currentSection = position;
            notifyItemChanged(position);
        }

        // Send and update notification to Performance Fragment via the MainActivity
        displayInterface.performanceShowSection(position);
    }

    @Override
    public int getItemCount() {
        return mainActivityInterface.getSectionViews().size();
    }

    public int getTotalHeight() {
        // Add up the height of the scaled views (for autoscroll)
        float size = 0;
        sectionHeights = new ArrayList<>();
        for (int x=0;x<sectionInfos.size();x++) {
            float thisSection  = sectionInfos.get(x).height*sectionInfos.get(x).scale;
            thisSection += 4f*density;
            sectionHeights.add(thisSection);
            size += thisSection;
        }
        return (int)size;
    }

    public ArrayList<Float> getHeights() {
        return sectionHeights;
    }

    private void onTouchAction() {
        mainActivityInterface.getDisplayPrevNext().showAndHide();
        mainActivityInterface.updateOnScreenInfo("showhide");
        mainActivityInterface.showHideActionBar();
    }
}
