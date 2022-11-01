package com.garethevans.church.opensongtablet.stage;

// This deals with displaying the song in StageMode (actually using a recyclerView in PerformanceMode)

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private final MainActivityInterface mainActivityInterface;
    private final Context c;
    private final DisplayInterface displayInterface;
    private final ArrayList<StageSectionInfo> sectionInfos;
    private final ArrayList<Float> floatSizes;
    private float floatHeight = 0;
    private int currentSection = 0;
    private final float maxFontSize, density, stageModeScale;
    private final String alphaChange = "alpha";
    private final float alphaoff = 0.4f;
    private boolean fakeClick;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "StageSectionAdapter";

    public StageSectionAdapter(Context c, MainActivityInterface mainActivityInterface, DisplayInterface displayInterface) {
        this.mainActivityInterface = mainActivityInterface;
        this.c = c;
        this.displayInterface = displayInterface;
        density = c.getResources().getDisplayMetrics().density;
        maxFontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSizeMax",50f);
        stageModeScale = mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale",0.8f);
        sectionInfos = new ArrayList<>();
        floatSizes = new ArrayList<>();
        setSongInfo();
    }

    private void setSongInfo() {
        // Prepare the info for each section
        floatHeight = 0;

        for (int x=0; x<mainActivityInterface.getSectionViews().size(); x++) {
            StageSectionInfo stageSectionInfo = new StageSectionInfo();

            float alpha = 1f;
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                alpha = alphaoff;
            }

            int sectionWidth = mainActivityInterface.getSectionWidths().get(x);
            int sectionHeight = mainActivityInterface.getSectionHeights().get(x);

            float x_scale = (float)(mainActivityInterface.getDisplayMetrics()[0]-16)/(float)sectionWidth;
            float y_scale = (float)(mainActivityInterface.getDisplayMetrics()[1]-mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()))*stageModeScale/(float)sectionHeight;
            float scale = Math.min(x_scale,y_scale);
            // Check the scale isn't bigger than the maximum font size
            scale = Math.min(scale,(maxFontSize/14f));

            float itemHeight = sectionHeight * scale + (4f * density);

            floatHeight += itemHeight;
            floatSizes.add(itemHeight);

            stageSectionInfo.section = x;
            stageSectionInfo.width = sectionWidth;
            stageSectionInfo.height = sectionHeight;
            stageSectionInfo.scale = scale;
            stageSectionInfo.alpha = alpha;
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
                    holder.v.post(()->{
                        try {
                            holder.v.setAlpha(sectionInfos.get(position).alpha);
                            float scale = sectionInfos.get(position).scale;
                            holder.v.getLayoutParams().height = (int) (sectionInfos.get(position).height * scale);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull StageViewHolder holder, int position) {
        if (position<sectionInfos.size()) {
            View v = mainActivityInterface.getSectionViews().get(position);

            if (v.getParent()!=null) {
                ((ViewGroup)v.getParent()).removeView(v);
            }

            int section = sectionInfos.get(position).section;
            int width = sectionInfos.get(position).width;
            int height = sectionInfos.get(position).height;
            float scale = sectionInfos.get(position).scale;
            float alpha = sectionInfos.get(position).alpha;

            CardView cardView = (CardView) holder.v;
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage)) && section == currentSection) {
                alpha = 1.0f;
            }

            if (v.getParent()!=null) {
                ((ViewGroup)v.getParent()).removeView(v);
            }

            v.setPivotX(0);
            v.setPivotY(0);
            v.setScaleX(scale);
            v.setScaleY(scale);

            // Update the views post to ensure drawing is ready
            holder.sectionView.post(()-> {
                try {
                    holder.sectionView.getLayoutParams().width = (int) (width * scale);
                    holder.sectionView.getLayoutParams().height = (int) (height * scale);
                    if (v.getParent()!=null) {
                        ((ViewGroup)v.getParent()).removeView(v);
                    }
                    holder.sectionView.addView(v);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            float finalAlpha = alpha;
            cardView.post(()-> {
                try {
                    cardView.setVisibility(View.INVISIBLE);
                    cardView.setAlpha(finalAlpha);
                    cardView.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                    cardView.getLayoutParams().height = (int) (height * scale);
                    ViewCompat.setBackgroundTintList(cardView, ColorStateList.valueOf(mainActivityInterface.getSectionColors().get(section)));
                    cardView.setVisibility(View.VISIBLE);
                    cardView.setOnClickListener(view -> {
                        if (fakeClick) {
                            fakeClick = false;
                        } else {
                            sectionSelected(position);
                        }
                    });
                    cardView.setOnLongClickListener(view -> {
                        // Do nothing, but consume the event
                        return true;
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
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
        mainActivityInterface.showActionBar();
    }

    public void clickOnSection(int position) {
        fakeClick = true;
        sectionSelected(position);
    }

    public void sectionSelected(int position) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        // Only do this alpha change in stage mode

        // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
        onTouchAction();

        try {
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                sectionInfos.get(currentSection).alpha = alphaoff;
                notifyItemChanged(currentSection, alphaChange);

                // Now update the newly selected position
                if (position >= 0 && position < sectionInfos.size()) {
                    mainActivityInterface.getSong().setCurrentSection(position);
                    sectionInfos.get(position).alpha = 1.0f;
                    notifyItemChanged(position, alphaChange);

                    // Send a nearby notification (the client will ignore if not required or not ready)
                    if (mainActivityInterface.getNearbyConnections().hasValidConnections() &&
                            mainActivityInterface.getNearbyConnections().getIsHost()) {
                        mainActivityInterface.getNearbyConnections().sendSongSectionPayload();
                    }
                }
            }
            currentSection = position;

            // Send and update notification to Performance Fragment via the MainActivity
            displayInterface.performanceShowSection(position);
        } catch (Exception e) {
            // Likely the number of sections isn't what was expected (probably via Nearby)
            e.printStackTrace();
        }
    }

    public ArrayList<Float> getHeights() {
        return floatSizes;
    }

}
