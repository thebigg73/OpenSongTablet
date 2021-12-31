package com.garethevans.church.opensongtablet.stage;

// This deals with displaying the song in StageMode (actually using a recyclerView in PerformanceMode)

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class StageSectionAdapter extends RecyclerView.Adapter<StageViewHolder> {

    // All the helpers we need to access are in the MainActivity
    private final String TAG = "StageSectionAdapter";
    private final MainActivityInterface mainActivityInterface;
    private ArrayList<StageSectionInfo> sectionInfos;
    private int currentSection = 0;

    public StageSectionAdapter(MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
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
            float x_scale = (float)mainActivityInterface.getDisplayMetrics()[0]/(float)sectionWidth;
            float y_scale = (float)(mainActivityInterface.getDisplayMetrics()[1]-mainActivityInterface.getAppActionBar().getActionBarHeight())*0.75f/(float)sectionHeight;
            stageSectionInfo.scale = Math.min(x_scale,y_scale);
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
        cardView.setBackgroundColor(mainActivityInterface.getSectionColors().get(position));
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
    }

    public void sectionSelected(int thisPos) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        notifyItemChanged(currentSection);

        // Now update the newly selected position
        setSection(thisPos);

        // Scroll to this position

    }

    @Override
    public int getItemCount() {
        return mainActivityInterface.getSectionViews().size();
    }

    private void setSection(int position) {
        if (position>-1 && position<sectionInfos.size()) {
            mainActivityInterface.getSong().setCurrentSection(position);
            currentSection = position;
            notifyItemChanged(position);
        }
    }
}
