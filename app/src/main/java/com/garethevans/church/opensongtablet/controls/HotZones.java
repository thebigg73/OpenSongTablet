package com.garethevans.church.opensongtablet.controls;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyRecyclerView;
import com.garethevans.church.opensongtablet.customviews.MyZoomLayout;
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
    private boolean usingScrollZones;
    private final Handler checkScrollRequiredHandler = new Handler(Looper.getMainLooper());
    private Runnable checkScrollRequiredRunnable;
    private boolean checkedAfterTime;
    private MyRecyclerView myRecyclerView;
    private MyZoomLayout myZoomLayout;

    // This creates an overlay with clickable zones for action assignment
    public HotZones(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        mode_performance = c.getString(R.string.mode_performance);
        getPreferences();
    }

    private void getPreferences() {
        // Get our initial preferences
        hotZoneTopLeftShort = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopLeftShort","");
        hotZoneTopCenterShort = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopCenterShort","scrollup");
        hotZoneBottomCenterShort = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneBottomCenterShort","scrolldown");
        hotZoneTopLeftLong = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopLeftLong","");
        hotZoneTopCenterLong = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneTopCenterLong","");
        hotZoneBottomCenterLong = mainActivityInterface.getPreferences().getMyPreferenceString("hotZoneBottomCenterLong","");
        setUsingScrollZones();
    }

    private void setUsingScrollZones() {
        usingScrollZones = hotZoneTopLeftShort.contains("scroll") || hotZoneTopLeftLong.contains("scroll") ||
                hotZoneTopCenterShort.contains("scroll") || hotZoneTopCenterLong.contains("scroll") ||
                hotZoneBottomCenterShort.contains("scroll") || hotZoneBottomCenterLong.contains("scroll");
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
            checkIfRequired();
            // Runnable check the visibility requirements after 2 seconds
            checkScrollRequiredRunnable = () -> {
                checkedAfterTime = true;
                checkScrollButtonOn(myZoomLayout,myRecyclerView);
            };
        }
        setUsingScrollZones();
    }

    private void checkIfRequired() {
        // Check hotzone required
        if (!mainActivityInterface.getMode().equals(mode_performance)) {
            hotZoneTopLeftView.setVisibility(View.GONE);
            hotZoneTopCenterView.setVisibility(View.GONE);
            hotZoneBottomCenterView.setVisibility(View.GONE);

        } else {
            // If the inline set is open the top left zone is disabled
            // If the inline set size is greater than 40%, the center zones are also disabled
            boolean inlineSet = mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSet",false);
            float inlineSetWidth = mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth",0.3f);

            Log.d(TAG,"checkIfRequired()  inlineSet:"+inlineSet+"  width:"+inlineSetWidth);
            hotZoneTopLeftView.setVisibility(
                    hotZoneTopLeftShort != null && hotZoneTopLeftLong != null && !inlineSet &&
                            (!hotZoneTopLeftShort.isEmpty() || !hotZoneTopLeftLong.isEmpty()) ?
                            View.VISIBLE : View.GONE);
            hotZoneTopCenterView.setVisibility(
                    hotZoneTopCenterShort != null && hotZoneTopCenterLong != null && (!inlineSet || inlineSetWidth<0.5f) &&
                            (!hotZoneTopCenterShort.isEmpty() || !hotZoneTopCenterLong.isEmpty()) ?
                            View.VISIBLE : View.GONE);
            hotZoneBottomCenterView.setVisibility(
                    hotZoneBottomCenterShort != null && hotZoneBottomCenterLong != null && (!inlineSet || inlineSetWidth<0.4f) &&
                            (!hotZoneBottomCenterShort.isEmpty() || !hotZoneBottomCenterLong.isEmpty()) ?
                            View.VISIBLE : View.GONE);
            // Update the listeners
            setListeners();
            setUsingScrollZones();
        }
    }

    private void setListeners() {
        hotZoneTopLeftView.setOnClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopLeftShort,false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> checkScrollButtonOn(myZoomLayout,myRecyclerView),500);
        });
        hotZoneTopCenterView.setOnClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopCenterShort,false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> checkScrollButtonOn(myZoomLayout,myRecyclerView),500);
        });
        hotZoneBottomCenterView.setOnClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneBottomCenterShort,false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> checkScrollButtonOn(myZoomLayout,myRecyclerView),500);
        });
        hotZoneTopLeftView.setOnLongClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopLeftLong,false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> checkScrollButtonOn(myZoomLayout,myRecyclerView),500);
            return true;
        });
        hotZoneTopCenterView.setOnLongClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneTopCenterLong,false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> checkScrollButtonOn(myZoomLayout,myRecyclerView),500);
            return true;
        });
        hotZoneBottomCenterView.setOnLongClickListener(view -> {
            mainActivityInterface.getPerformanceGestures().doAction(hotZoneBottomCenterLong,false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> checkScrollButtonOn(myZoomLayout,myRecyclerView),50);
            return true;
        });
    }

    public void setHotZoneTopLeftShort(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTopLeftShort",text);
        this.hotZoneTopLeftShort = text;
        checkIfRequired();
    }
    public void setHotZoneTopLeftLong(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTopLeftLong",text);
        this.hotZoneTopLeftLong = text;
        checkIfRequired();
    }
    public void setHotZoneTopCenterShort(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTopCenterShort",text);
        this.hotZoneTopCenterShort = text;
        checkIfRequired();
    }
    public void setHotZoneTopCenterLong(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneTopCenterLong",text);
        this.hotZoneTopCenterLong = text;
        checkIfRequired();
    }
    public void setHotZoneBottomCenterShort(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneBottomCenterShort",text);
        this.hotZoneBottomCenterShort = text;
        checkIfRequired();
    }
    public void setHotZoneBottomCenterLong(String text) {
        mainActivityInterface.getPreferences().setMyPreferenceString("hotZoneBottomCenterLong",text);
        this.hotZoneBottomCenterLong = text;
        checkIfRequired();
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

    // To hide scroll up/down hot zones if already there.
    // Called from Performance mode on screen tap and screen update
    // Only hidden if zone has short or long press action with scroll, but nothing else
    public void checkScrollButtonOn(MyZoomLayout myZoomLayout, MyRecyclerView myRecyclerView) {
        this.myZoomLayout = myZoomLayout;
        this.myRecyclerView = myRecyclerView;
        if (usingScrollZones) {
            checkScrollRequiredHandler.removeCallbacks(checkScrollRequiredRunnable);
            boolean scrolledToTop = false;
            boolean scrolledToBottom = false;
            if (mainActivityInterface.getSong().getFiletype()!=null &&
                    mainActivityInterface.getSong().getFiletype().equals("XML") && myZoomLayout!=null) {
                // Check the scroll position of the MyZoomLayout
                scrolledToTop = myZoomLayout.getScrollPos()<=0;
                scrolledToBottom = myZoomLayout.getScrollPos()==myZoomLayout.getMaxScrollY();
            } else if (myRecyclerView!=null) {
                // Check the position of recyclerview scroll
                scrolledToTop = myRecyclerView.getScrollPos()<=0;
                scrolledToBottom = myRecyclerView.getScrollPos()>=myRecyclerView.getScrollYMax();
            }
            if (isScrollOnlyZone(hotZoneTopLeftShort,hotZoneTopLeftLong,"scrollup")) {
                hotZoneTopLeftView.setVisibility(scrolledToTop ? View.GONE:View.VISIBLE);
            } else if (isScrollOnlyZone(hotZoneTopLeftShort,hotZoneTopLeftLong,"scrolldown")) {
                hotZoneTopLeftView.setVisibility(scrolledToBottom ? View.GONE:View.VISIBLE);
            }

            if (isScrollOnlyZone(hotZoneTopCenterShort,hotZoneTopCenterLong,"scrollup")) {
                hotZoneTopCenterView.setVisibility(scrolledToTop ? View.GONE:View.VISIBLE);
            } else if (isScrollOnlyZone(hotZoneTopCenterShort,hotZoneTopCenterLong,"scrolldown")) {
                hotZoneTopCenterView.setVisibility(scrolledToBottom ? View.GONE:View.VISIBLE);
            }

            if (isScrollOnlyZone(hotZoneBottomCenterShort,hotZoneBottomCenterLong,"scrollup")) {
                hotZoneBottomCenterView.setVisibility(scrolledToTop ? View.GONE:View.VISIBLE);
            } else if (isScrollOnlyZone(hotZoneBottomCenterShort,hotZoneBottomCenterLong,"scrolldown")) {
                hotZoneBottomCenterView.setVisibility(scrolledToBottom ? View.GONE:View.VISIBLE);
            }

            if (!checkedAfterTime) {
                // Set the runnable to check again after 2 seconds (for fling, etc)
                if (checkScrollRequiredRunnable != null) {
                    checkScrollRequiredHandler.postDelayed(checkScrollRequiredRunnable, 2000);
                }
            } else {
                checkedAfterTime = false;
            }
        }
    }

    private boolean isScrollOnlyZone(String zoneShort, String zoneLong, String direction) {
        return (zoneShort.equals(direction) && zoneLong.isEmpty()) || (zoneShort.isEmpty() && zoneLong.equals(direction));
    }

    // If the
    public void enableTopLeft(boolean enabled) {
        hotZoneTopLeftView.setVisibility(enabled ? View.VISIBLE:View.GONE);
    }
    public void enableTopCenter(boolean enabled) {
        hotZoneTopCenterView.setVisibility(enabled ? View.VISIBLE:View.GONE);
    }
    public void enableBottomCenter(boolean enabled) {
        hotZoneTopCenterView.setVisibility(enabled ? View.VISIBLE:View.GONE);
    }
}
