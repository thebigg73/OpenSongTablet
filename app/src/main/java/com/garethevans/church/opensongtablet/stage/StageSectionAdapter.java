package com.garethevans.church.opensongtablet.stage;

// This deals with displaying the song in StageMode (actually using a recyclerView in PerformanceMode)

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.util.TypedValue;
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
    private final float maxFontSize, stageModeScale;
    private final String alphaChange = "alpha";
    private final float alphaoff = 0.4f;
    private int availableWidth;
    private int availableHeight;
    private final int inlineSetWidth;
    private int padding;
    private boolean fakeClick;
    @SuppressWarnings({"FieldCanBeLocal","unused"})
    private final String TAG = "StageSectionAdapter";
    int spacing = 0;


    public StageSectionAdapter(Context c, MainActivityInterface mainActivityInterface,
                               DisplayInterface displayInterface, int inlineSetWidth) {
        this.mainActivityInterface = mainActivityInterface;
        this.c = c;
        this.displayInterface = displayInterface;
        //density = c.getResources().getDisplayMetrics().density;
        maxFontSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("fontSizeMax",50f);
        stageModeScale = mainActivityInterface.getPreferences().getMyPreferenceFloat("stageModeScale",0.8f);
        sectionInfos = new ArrayList<>();
        floatSizes = new ArrayList<>();
        setSongInfo();
        this.inlineSetWidth = inlineSetWidth;

        setAvailableSize();
    }

    private void setAvailableSize() {
        int[] metrics = mainActivityInterface.getDisplayMetrics();
        int[] viewPadding = mainActivityInterface.getViewMargins();

        Log.d(TAG,"displayW:"+metrics[0]+"  lmargin:"+viewPadding[0]+"  rmargin"+viewPadding[1]+"  inlinesetW:"+inlineSetWidth);
        availableWidth = metrics[0] - viewPadding[0] - viewPadding[1] - inlineSetWidth;
        availableHeight = metrics[1] - viewPadding[2] - viewPadding[3];

        padding = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                16, c.getResources().getDisplayMetrics());
        spacing = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                4, c.getResources().getDisplayMetrics());

        Log.d(TAG,"padding:"+padding);
    }

    private void setSongInfo() {
        // Prepare the info for each section
        floatHeight = 0;

        if (availableWidth==0 || availableHeight==0) {
            setAvailableSize();
        }

        float defFontSize = mainActivityInterface.getProcessSong().getDefFontSize();
        for (int x=0; x<mainActivityInterface.getSectionViews().size(); x++) {
            StageSectionInfo stageSectionInfo = new StageSectionInfo();

            float alpha = 1f;
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                alpha = alphaoff;
            }

            int sectionWidth = mainActivityInterface.getSectionWidths().get(x);
            int sectionHeight = mainActivityInterface.getSectionHeights().get(x);

            float x_scale = (float)(availableWidth - padding)/(float)sectionWidth;
            float y_scale = (float)(availableHeight-mainActivityInterface.getToolbar().getActionBarHeight(mainActivityInterface.needActionBar()))*stageModeScale/(float)sectionHeight;
            float scale = Math.min(x_scale,y_scale);
            // Check the scale isn't bigger than the maximum font size
            scale = Math.min(scale,(maxFontSize / defFontSize));

            float itemHeight = sectionHeight * scale + (spacing);

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
        final int pos = position;
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals(alphaChange)) {
                    // We want to update the highlight colour to off
                    holder.v.post(()->{
                        try {
                            holder.v.setAlpha(sectionInfos.get(pos).alpha);
                            float scale = sectionInfos.get(pos).scale;
                            //holder.v.getLayoutParams().width = availableWidth;
                            holder.sectionView.getLayoutParams().width = availableWidth;
                            //holder.v.getLayoutParams().height = (int) (sectionInfos.get(pos).height * scale);
                            holder.sectionView.getLayoutParams().height = (int) (sectionInfos.get(position).height * scale);
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
        final int pos = position;
        if (pos<sectionInfos.size()) {
            View v = mainActivityInterface.getSectionViews().get(pos);

            if (v.getParent()!=null) {
                ((ViewGroup)v.getParent()).removeView(v);
            }

            int section = sectionInfos.get(pos).section;
            int width = sectionInfos.get(pos).width;
            int height = sectionInfos.get(pos).height;
            float scale = sectionInfos.get(pos).scale;
            float alpha = sectionInfos.get(pos).alpha;

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
                    holder.v.getLayoutParams().width = availableWidth;
                    holder.sectionView.getLayoutParams().width = (int) (width * scale);
                    holder.v.getLayoutParams().height = (int) (height * scale);
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
                        Log.d(TAG,"pos:"+pos);
                        if (fakeClick) {
                            fakeClick = false;
                        } else {
                            sectionSelected(pos);
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
        // TODO TEST
        Log.d(TAG,"TEST:  position:"+position+"  sectionInfos.size():"+sectionInfos.size());
        if (displayInterface.getIsSecondaryDisplaying() &&
                sectionInfos.size()>=position) {
            fakeClick = true;
            sectionSelected(position);
        } else if (sectionInfos.size() > position) {
            fakeClick = true;
            sectionSelected(position);
        }
    }

    public void sectionSelected(int position) {
        // Whatever the previously selected item was, change the alpha to the alphaOff value
        // Only do this alpha change in stage mode

        // Because this is a screen touch, do the necessary UI update (check actionbar/prev/next)
        onTouchAction();

        try {
            if (mainActivityInterface.getMode().equals(c.getString(R.string.mode_stage))) {
                if (sectionInfos.size()>currentSection) {
                    sectionInfos.get(currentSection).alpha = alphaoff;
                    notifyItemChanged(currentSection, alphaChange);
                }

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

            // If this section of the song has inline MIDI messages, send them
            if (mainActivityInterface.getSong().getFiletype().equals("XML")) {
                final String message = mainActivityInterface.getSong().getInlineMidiMessages().get(position, "");
                mainActivityInterface.getMidi().sendMidiHexSequence(message);
            }

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
