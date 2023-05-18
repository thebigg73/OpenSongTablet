package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.view.View;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class HotZones {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "HotZones";
    private final MainActivityInterface mainActivityInterface;
    private View hotZoneTopLeftView;
    private View hotZoneTopCenterView;
    private View hotZoneBottomCenterView;
    private String hotZoneTopLeftShort, hotZoneTopCenterShort, hotZoneBottomCenterShort,
            hotZoneTopLeftLong, hotZoneTopCenterLong, hotZoneBottomCenterLong;
    private final String mode_performance;

    // This creates an overlay with clickable zones for action assignment
    public HotZones(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        mode_performance = c.getString(R.string.mode_performance);
        getPreferences();
    }

    private void getPreferences() {
        // Get our initial preferences
        hotZoneTopLeftShort = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopLeftShort","");
        hotZoneTopCenterShort = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopCenterShort","");
        hotZoneBottomCenterShort = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneBottomCenterShort","");
        hotZoneTopLeftLong = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopLeftLong","");
        hotZoneTopCenterLong = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopCenterLong","");
        hotZoneBottomCenterLong = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneBottomCenterLong","");
    }
    public void initialiseHotZones(View hotZoneTopLeftView, View hotZoneTopCenterView,
                                   View hotZoneBottomCenterView) {
        this.hotZoneTopLeftView = hotZoneTopLeftView;
        this.hotZoneTopCenterView = hotZoneTopCenterView;
        this.hotZoneBottomCenterView = hotZoneBottomCenterView;

        // Hot zones are only allowed in Performance mode
        // If they are not assigned an action, hide them
        if (hotZoneTopLeftView != null && hotZoneTopCenterView != null &&
                hotZoneBottomCenterView != null) {
            if (!mainActivityInterface.getMode().equals(mode_performance)) {
                hotZoneTopLeftView.setVisibility(View.GONE);
                hotZoneTopCenterView.setVisibility(View.GONE);
                hotZoneBottomCenterView.setVisibility(View.GONE);

            } else {
                hotZoneTopLeftView.setVisibility(
                        hotZoneTopLeftShort != null && hotZoneTopLeftLong != null &&
                                (!hotZoneTopLeftShort.isEmpty() || !hotZoneTopLeftLong.isEmpty()) ?
                                View.VISIBLE : View.GONE);
                hotZoneTopCenterView.setVisibility(
                        hotZoneTopCenterShort != null && hotZoneTopCenterLong != null &&
                                (!hotZoneTopCenterShort.isEmpty() || !hotZoneTopCenterLong.isEmpty()) ?
                                View.VISIBLE : View.GONE);
                hotZoneBottomCenterView.setVisibility(
                        hotZoneBottomCenterShort != null && hotZoneBottomCenterLong != null &&
                                (!hotZoneBottomCenterShort.isEmpty() || !hotZoneBottomCenterLong.isEmpty()) ?
                                View.VISIBLE : View.GONE);

                // Update the listeners
                setListeners();
            }
        }
    }



    private void setListeners() {
        hotZoneTopLeftView.setOnClickListener(view -> mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopLeftShort,false));
        hotZoneTopCenterView.setOnClickListener(view -> mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopCenterShort,false));
        hotZoneBottomCenterView.setOnClickListener(view -> mainActivityInterface.getPerformanceGestures().doAction(hotZoneBottomCenterShort,false));
        hotZoneTopLeftView.setOnLongClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopLeftLong,false);
            return true;
        });
        hotZoneTopCenterView.setOnLongClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopCenterLong,false);
            return true;
        });
        hotZoneBottomCenterView.setOnLongClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneBottomCenterLong,false);
            return true;
        });
    }

    public void setHotZoneTopLeftShort(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTLS",text);
        this.hotZoneTopLeftShort = text;
    }
    public void setHotZoneTopLeftLong(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTopLeftLong",text);
        this.hotZoneTopLeftLong = text;
    }
    public void setHotZoneTopCenterShort(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTopCenterShort",text);
        this.hotZoneTopCenterShort = text;
    }
    public void setHotZoneTopCenterLong(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTopCenterLong",text);
        this.hotZoneTopCenterLong = text;
    }
    public void setHotZoneBottomCenterShort(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneBottomCenterShort",text);
        this.hotZoneBottomCenterShort = text;
    }
    public void setHotZoneBottomCenterLong(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneBottomCenterLong",text);
        this.hotZoneBottomCenterLong = text;
    }

    public String getHotZoneTopLeftShort() {
        return hotZoneTopLeftShort;
    }
    public String getHotZoneTopLeftLong() {
        return hotZoneTopLeftLong;
    }
    public String getHotZoneTopCenterShort() {
        return hotZoneTopCenterShort;
    }
    public String getHotZoneTopCenterLong() {
        return hotZoneTopCenterLong;
    }
    public String getHotZoneBottomCenterShort() {
        return hotZoneBottomCenterShort;
    }
    public String getHotZoneBottomCenterLong() {
        return hotZoneBottomCenterLong;
    }
}
