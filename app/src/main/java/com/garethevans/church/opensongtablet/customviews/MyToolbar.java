package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.garethevans.church.opensongtablet.MainActivity;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.SongDetailsBottomSheet;

import java.util.ArrayList;
import java.util.List;

// Moved away from MaterialToolbar due to FireHD crashes
public class MyToolbar extends Toolbar {

    // This holds references to the items in the Toolbar (except the battery)
    // Battery changes get sent via the mainactivityInterface
    // The toolbar is set as the supportActionbar received as actionBar, so can called via that as well

    private Context c;
    private MainActivityInterface mainActivityInterface;
    private ActionBar actionBar;
    private final FrameLayout batteryholder;
    private final MyMaterialSimpleTextView title;
    private final MyMaterialSimpleTextView author;
    private final MyMaterialSimpleTextView key;
    private final MyMaterialSimpleTextView capo;
    private final TextClock clock;
    private final MyMaterialSimpleTextView tempo;
    private final ImageView setIcon, batteryimage;
    private final ConstraintLayout songandauthor;
    private final LinearLayout metronomeLayout;
    private final ArrayList<View> beatView;
    private final MyMaterialSimpleTextView batterycharge;
    private Handler delayActionBarHide;
    private Runnable hideActionBarRunnable;
    private int actionBarHideTime = 1200, additionalTopPadding = 0;
    private float clockTextSize;
    private int beats = 4;
    private boolean clock24hFormat, clockOn, hideActionBar, clockSeconds, performanceMode, actionBarTempo;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "MyToolbar";
    private String capoString = "";
    private String keyString = "";

    // Set up the view and view items
    public void initialiseToolbar(Context c, ActionBar actionBar) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        changeTheme();
        actionBar.setElevation(0);
        this.actionBar = actionBar;
        delayActionBarHide = new Handler();
        /*hideActionBarRunnable = () -> {
            if (actionBar.isShowing() && !mainActivityInterface.needActionBar()) {
                actionBar.hide();
            }
        };*/
        hideActionBarRunnable = () -> {
            if (this.getVisibility() == View.VISIBLE && !mainActivityInterface.needActionBar()) {
                this.setVisibility(View.GONE);
                if (getParent() instanceof View) {
                    ((View) getParent()).setVisibility(View.GONE);
                }
                if (mainActivityInterface instanceof MainActivity) {
                    ((MainActivity) mainActivityInterface).updateMargins();
                }
            }
        };
        updateClock();
    }
    public MyToolbar(@NonNull Context context) {
        this(context, null);
    }

    public MyToolbar(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);

        View v = inflate(context,R.layout.view_toolbar_constraint,this);

        this.setContentInsetsRelative(0,0);
        this.setContentInsetStartWithNavigation(0);
        this.setContentInsetEndWithActions(0);

        // Assign our custom popup theme to the toolbar programmatically
        this.setPopupTheme(R.style.DynamicPopupTheme);

        this.setPadding(0,0,0,0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setElevation(0f);
        }

        setIcon = v.findViewById(R.id.setIcon);
        title = v.findViewById(R.id.songtitle_ab);
        key = v.findViewById(R.id.songkey_ab);
        capo = v.findViewById(R.id.songcapo_ab);
        author = v.findViewById(R.id.songauthor_ab);
        tempo = v.findViewById(R.id.songtempo_ab);
        batteryholder = v.findViewById(R.id.batteryholder);
        batteryimage = v.findViewById(R.id.batteryimage);
        batterycharge = v.findViewById(R.id.batterycharge);
        songandauthor = v.findViewById(R.id.songandauthor);
        clock = v.findViewById(R.id.digitalclock);
        metronomeLayout = v.findViewById(R.id.metronomeLayout);
        beatView = new ArrayList<>();
        beatView.add(null);
        beatView.add(v.findViewById(R.id.beat1));
        beatView.add(v.findViewById(R.id.beat2));
        beatView.add(v.findViewById(R.id.beat3));
        beatView.add(v.findViewById(R.id.beat4));
        beatView.add(v.findViewById(R.id.beat5));
        beatView.add(v.findViewById(R.id.beat6));
        beatView.add(v.findViewById(R.id.beat7));
        beatView.add(v.findViewById(R.id.beat8));
        beatView.add(v.findViewById(R.id.beat9));
        beatView.add(v.findViewById(R.id.beat10));
        beatView.add(v.findViewById(R.id.beat11));
        beatView.add(v.findViewById(R.id.beat12));
        beatView.add(v.findViewById(R.id.beat13));
        beatView.add(v.findViewById(R.id.beat14));
        beatView.add(v.findViewById(R.id.beat15));
        beatView.add(v.findViewById(R.id.beat16));

        metronomeLayout.setVisibility(View.GONE);

        batteryholder.setOnClickListener(v1 -> {
            mainActivityInterface.showActionBar();
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_actionbar), 0);
        });
    }

    // Deal with the preferences used for the actionbar
    private void updateActionBarPrefs() {
        clockTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("clockTextSize",9.0f);
        clock24hFormat = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clock24hFormat",true);
        clockOn = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockOn",true);
        clockSeconds = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockSeconds",false);
        hideActionBar = mainActivityInterface.getPreferences().getMyPreferenceBoolean("hideActionBar",false);
        actionBarHideTime = mainActivityInterface.getPreferences().getMyPreferenceInt("actionBarHideTime",1200);
        actionBarTempo = mainActivityInterface.getPreferences().getMyPreferenceBoolean("actionBarTempo",false);
    }
    public void updateActionBarSettings(String prefName, float value, boolean isvisible) {

        switch (prefName) {
            case "batteryDialOn":
                mainActivityInterface.getBatteryStatus().setBatteryDialOn(isvisible);
                break;
            case "batteryDialThickness":
                mainActivityInterface.getBatteryStatus().setBatteryDialThickness((int)value);
                mainActivityInterface.getBatteryStatus().setBatteryImage();
                break;
            case "batteryTextOn":
                mainActivityInterface.getBatteryStatus().setBatteryTextOn(isvisible);
                break;
            case "batteryTextSize":
                mainActivityInterface.getBatteryStatus().setBatteryTextSize(value);
                break;
            case "clockOn":
                clockOn = isvisible;
                if (!mainActivityInterface.getSettingsOpen()) {
                    hideView(clock, !isvisible);
                }
                break;
            case "clock24hFormat":
                clock24hFormat = isvisible;
                updateClock();
                break;
            case "clockSeconds":
                clockSeconds = isvisible;
                updateClock();
                break;
            case "clockTextSize":
                clock.setTextSize(value);
                break;
            case "songTitleSize":
                title.setTextSize(value);
                key.setTextSize(value);
                capo.setTextSize(value);
                break;
            case "songAuthorSize":
                author.setTextSize(value);
                break;
            case "hideActionBar":
                setHideActionBar(!isvisible);
                break;
            case "actionBarHideTime":
                actionBarHideTime = (int)value;
                break;
            case "showBatteryHolder":
                // GE The battery holder is now just the clickable area
                // This is called to stop clicks while changing the settings
                batteryholder.setVisibility(isvisible ? View.VISIBLE : View.GONE);
                break;
            case "actionBarTempo":
                actionBarTempo = isvisible;
                break;
            case "actionBarSettingsMinimise":
                Log.d(TAG,"minimise actionbar icons toggled");
                break;
        }
    }

    public void setHideActionBar(boolean hideActionBar) {
        this.hideActionBar = hideActionBar;
    }
    public boolean getHideActionBar() {
        return hideActionBar;
    }

    // Update the text in the actionbar to either be a song info, or menu title
    public void setActionBar(Context c, String newtitle) {
        if (mainActivityInterface==null) {
            mainActivityInterface = (MainActivityInterface) c;
        }
        // If changing, reset help
        if (title!=null && !title.getText().equals(newtitle)) {
            // By default hide help (can be shown later)
            mainActivityInterface.updateToolbarHelp("");
        }

        if (newtitle == null) {
            // We are in the Performance/Stage mode
            float mainsize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songTitleSize",13.0f);

            if (title != null && mainActivityInterface.getSong().getTitle() != null) {
                // Songs have the gravity bottom for the content
                title.setGravity(Gravity.BOTTOM);
                title.setTextSize(mainsize);
                String text = mainActivityInterface.getSong().getTitle();
                if (mainActivityInterface.getVariations().getIsNormalOrKeyVariation(
                        mainActivityInterface.getSong())) {
                    text = "*" + mainActivityInterface.getVariations().getPreVariationInfo(
                            mainActivityInterface.getSong().getFolder(),mainActivityInterface.getSong().getFilename(),mainActivityInterface.getSong().getKey())[1];
                }
                title.setText(text);
                mainActivityInterface.checkSetMenuItemHighlighted(mainActivityInterface.getCurrentSet().getIndexSongInSet());
                int positionInSet = mainActivityInterface.getCurrentSet().getIndexSongInSet();
                // If we are in a set, show the icon
                if (positionInSet<0) {
                    try {
                        positionInSet = mainActivityInterface.getCurrentSet().getIndexSongInSet();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (positionInSet>-1) {
                    // Check the set menu fragment to see if we need to highlight
                    // This happens if we just loaded a song (not clicking on the set item in the menu)
                    mainActivityInterface.checkSetMenuItemHighlighted(positionInSet);
                    mainActivityInterface.getCurrentSet().setIndexSongInSet(positionInSet);
                    hideView(setIcon, text.isEmpty());
                } else {
                    mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
                    hideView(setIcon,true);
                }
                hideView(title, false);
            } else {
                hideView(setIcon,true);
                hideView(title, true);
            }
            if (author != null && mainActivityInterface.getSong().getAuthor() != null &&
                    !mainActivityInterface.getSong().getAuthor().isEmpty()) {
                author.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("songAuthorSize",11.0f));
                author.setText(mainActivityInterface.getSong().getAuthor());
                hideView(author, false);
                if (title!=null) {
                    title.setGravity(Gravity.BOTTOM|Gravity.START);
                    key.setGravity(Gravity.BOTTOM|Gravity.START);
                    capo.setGravity(Gravity.BOTTOM|Gravity.START);
                    tempo.setGravity(Gravity.BOTTOM|Gravity.START);
                    setIcon.setScaleType(ImageView.ScaleType.FIT_END);
                }
            } else if (author!=null) {
                hideView(author, true);
                if (title!=null) {
                    title.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                    key.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
                    capo.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
                    tempo.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
                    setIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                }
            }
            if (key != null && mainActivityInterface.getSong().getKey() != null &&
                    !mainActivityInterface.getSong().getKey().isEmpty()) {
                keyString = " (" + mainActivityInterface.getSong().getKey() + ")";
                key.setTextSize(mainsize);
                capo.setTextSize(mainsize);
                key.setText(keyString);
                hideView(key, false);
            } else if (key!=null) {
                hideView(key, true);
            }
            if (capo != null && mainActivityInterface.getSong().getCapo() !=null &&
                    !mainActivityInterface.getSong().getCapo().isEmpty()) {
                capoString = mainActivityInterface.getChordDisplayProcessing().getCapoPosition();
            } else {
                capoString = "";
            }

            if (!capoString.isEmpty() && !keyString.isEmpty()) {
                capoString += (" (" + mainActivityInterface.getTranspose().capoKeyTranspose(mainActivityInterface.getSong()) + ")").replace(" ()","");
            }
            String thisCapoString = "";
            if (!capoString.isEmpty()) {
                thisCapoString = " ["+capoString+"]";
            }
            if (thisCapoString.isEmpty() && capo!=null) {
                hideView(capo,true);
            } else if (capo!=null) {
                capo.setText(thisCapoString);
                hideView(capo,false);
            }

            if (actionBarTempo && tempo!=null && mainActivityInterface.getSong().getTempo() != null &&
                !mainActivityInterface.getSong().getTempo().isEmpty()) {
                String tempoString = " " + mainActivityInterface.getSong().getTempo() + " bpm";
                tempo.setTextSize(mainsize);
                tempo.setText(tempoString);
                hideView(tempo, false);
            } else if (tempo!=null) {
                tempo.setText("");
                hideView(tempo, true);
            }

            if (title!=null) {
                title.setOnClickListener(v -> openDetails());
                title.setOnLongClickListener(view -> {
                    editSong();
                    return true;
                });
            }
            if (author!=null) {
                author.setOnClickListener(v -> openDetails());
                author.setOnLongClickListener(view -> {
                    editSong();
                    return true;
                });
            }
            if (key!=null) {
                key.setOnClickListener(v -> openDetails());
                key.setOnLongClickListener(view -> {
                    editSong();
                    return true;
                });
            }
            if (songandauthor!=null) {
                songandauthor.setOnClickListener(v -> openDetails());
                songandauthor.setOnLongClickListener(view -> {
                    editSong();
                    return true;
                });
            }

        } else {
            // We are in a different fragment, so hide the song info stuff
            hideView(setIcon,true);
            if (title != null) {
                title.setOnClickListener(null);
                title.setOnLongClickListener(null);
                // Settings titles move to center gravity
                title.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
                title.setTextSize(18.0f);
                title.setText(newtitle);
            }
            hideView(title, false);
            hideView(author, true);
            hideView(key, true);
            hideView(capo,true);
            hideView(tempo,true);
            if (songandauthor!=null) {
                songandauthor.setOnClickListener(null);
                songandauthor.setOnLongClickListener(null);
            }
        }
    }

    // The onscreen capo display can grab this rather than reprocessing it
    public String getCapoString() {
        return capoString;
    }

    // Clicking on the song title/author/etc. opens up the song details bottom sheet
    private void openDetails() {
        if (mainActivityInterface.getSong()!=null && mainActivityInterface.getSong().getTitle()!=null &&
                !mainActivityInterface.getSong().getTitle().equals("Welcome to OpenSongApp")) {
            SongDetailsBottomSheet songDetailsBottomSheet = new SongDetailsBottomSheet();
            songDetailsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "songDetailsBottomSheet");
        }
    }

    // Allow editing the song by long pressing on the title/author/etc. if not null
    private void editSong() {
        if (mainActivityInterface.getSong()!=null && mainActivityInterface.getSong().getTitle()!=null &&
                !mainActivityInterface.getSong().getTitle().equals("Welcome to OpenSongApp")) {
            mainActivityInterface.navigateToFragment(c.getString(R.string.deeplink_edit), 0);
        }
    }

    // This is used to show/hide various parts of the toolbar text (author, copyright, etc)
    public void hideView(View v, boolean hide) {
        if (v!=null) {
            v.setVisibility(hide ? View.GONE : View.VISIBLE);
            v.requestLayout();
        }
    }

    public void hideSongDetails(boolean hide) {
        hideView(setIcon,hide);
        hideView(title,hide);
        hideView(author,hide);
        hideView(key,hide);
        hideView(capo,hide);
    }

    // Set when entering/exiting performance mode as this is used to determine if we can autohide actionbar
    public void setPerformanceMode(boolean inPerformanceMode) {
        performanceMode = inPerformanceMode;
    }

    // Show/hide the actionbar
    /*public void showActionBar(boolean menuOpen) {
        // Remove any existing callbacks to hide the actionbar
        try {
            if (delayActionBarHide!=null) {
                delayActionBarHide.removeCallbacks(hideActionBarRunnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now show the action bar
        if (actionBar!=null) {

            mainActivityInterface.getMainHandler().post(() -> {
                try {
                    actionBar.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        // If we need to hide the actionbar again, set a runnable, as long as the menu isn't open
        if (hideActionBar && performanceMode && !menuOpen) {
            try {
                if (delayActionBarHide!=null) {
                    delayActionBarHide.postDelayed(hideActionBarRunnable, actionBarHideTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
*/
    public void showActionBar(boolean menuOpen) {
        try {
            if (delayActionBarHide != null) {
                delayActionBarHide.removeCallbacks(hideActionBarRunnable);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainActivityInterface.getMainHandler().post(() -> {
            this.setVisibility(View.VISIBLE);
            // Ensure the parent layout container becomes visible too!
            if (getParent() instanceof View) {
                ((View) getParent()).setVisibility(View.VISIBLE);
            }
            if (mainActivityInterface instanceof MainActivity) {
                // Force layout update so getBottom() calculates correctly next pass
                ((MainActivity) mainActivityInterface).updateMargins();
            }
        });

        if (hideActionBar && performanceMode && !menuOpen) {
            try {
                if (delayActionBarHide != null) {
                    delayActionBarHide.postDelayed(hideActionBarRunnable, actionBarHideTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeCallBacks() {
        delayActionBarHide.removeCallbacks(hideActionBarRunnable);
    }


    // Get the actionbar height - fakes a height of 0 if autohiding
    /*public int getActionBarHeight(boolean forceShown) {

        if (hideActionBar && performanceMode && !forceShown) {
            return mainActivityInterface.getWindowFlags().getCustomMarginTop();
        } else {
            if (getHeight()==0) {
                // Just in case there was a call before drawn
                return 168;
            } else {
                return getHeight();
            }
        }
    }*/
    public int getActionBarHeight(boolean forceShown) {
        if (hideActionBar && performanceMode && !forceShown) {
            return 0; // Underlying view fills the screen when hidden
        } else {
            int height = getHeight();
            if (height <= 0) {
                // Fallback to a safe default standard height (~56dp converted to pixels)
                return (int) (56 * getResources().getDisplayMetrics().density);
            }
            return height;
        }
    }


    public void setAdditionalTopPadding(int additionalTopPadding) {
        this.additionalTopPadding = additionalTopPadding;
    }
    public int getAdditionalTopPadding() {
        return additionalTopPadding;
    }

    public void updateClock() {
        // IV - Refresh from preferences as may have changed
        updateActionBarPrefs();
        mainActivityInterface.getTimeTools().setFormat(clock, mainActivityInterface.getSettingsOpen(),
                clockTextSize, clockOn, clock24hFormat, clockSeconds);
    }

    public void batteryholderVisibility(boolean visible, boolean clickable) {
        batteryholder.setVisibility(visible ? View.VISIBLE:View.GONE);
        batteryholder.setClickable(clickable);
        showClock(visible);
    }

    public FrameLayout getBatteryholder() {
        return batteryholder;
    }
    public ImageView getBatteryimage() {
        return batteryimage;
    }
    public TextClock getClock() {
        return clock;
    }

    public MyMaterialSimpleTextView getBatterycharge() {
        return batterycharge;
    }

    public void showClock(boolean show) {
        // Only show if that is our preference
        clock.setTextSize(clockTextSize);
        clock.setVisibility(show && clockOn ? View.VISIBLE:View.GONE);
        clock.requestLayout();
    }

    @Override
    public void setNavigationOnClickListener(OnClickListener listener) {
        super.setNavigationOnClickListener(listener);
    }

    @Override
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        super.setOnMenuItemClickListener(listener);
    }

    public boolean isHidden() {
        return isShown();
    }

    public void setUpMetronomeBar(int beats) {
        this.beats = beats;

        // Force visibility NOW so it's ready for Beat 1
        if (metronomeLayout != null) {
            metronomeLayout.post(() -> metronomeLayout.setVisibility(View.VISIBLE));
            makeAllBeatsTransparent();
        }

        if (beats==0) {
            // Something wrong, so hide all
            for (int x = 1; x <= 16; x++) {
                if (beatView!=null && beatView.get(x)!=null) {
                    beatView.get(x).setVisibility(View.GONE);
                }
            }
            if (metronomeLayout!=null) {
                metronomeLayout.post(() -> metronomeLayout.setVisibility(View.GONE));
            }
        } else {
            // Make the ones we need visible, but transparent
            for (int x = 1; x <= beats; x++) {
                if (beatView!=null && beatView.get(x)!=null) {
                    beatView.get(x).setBackgroundColor(Color.TRANSPARENT);
                    beatView.get(x).setVisibility(View.VISIBLE);
                }
            }
            // Hide the ones we don't need
            for (int x = beats + 1; x <= 16; x++) {
                if (beatView!=null && beatView.get(x)!=null) {
                    beatView.get(x).setBackgroundColor(Color.TRANSPARENT);
                    beatView.get(x).setVisibility(View.GONE);
                }
            }
            if (metronomeLayout!=null) {
                metronomeLayout.post(() -> metronomeLayout.setVisibility(View.VISIBLE));
            }
        }

        // Add this at the end of the method to ensure the UI thread
        // has actually finished drawing the views before the timer hits
        if (metronomeLayout!=null) {
            metronomeLayout.post(metronomeLayout::requestLayout);
        }
    }

    public void hideMetronomeBar() {
        makeAllBeatsTransparent();
        metronomeLayout.post(() -> metronomeLayout.setVisibility(View.GONE));
    }

    public void makeAllBeatsTransparent() {
        for (int x=1; x<=16; x++) {
            View view = beatView.get(x);
            view.post(() -> view.setBackgroundColor(Color.TRANSPARENT));
        }
    }

    public void highlightBeat(int beat, int colorOn, long flashDuration) {
        post(() -> {
            // Metronome sends 1, 2, 3, 4.
            // Your list is [null, view1, view2, view3, view4]
            int index = beat;

            if (beatView == null || index < 1 || index >= beatView.size()) {
                // This is likely where Beat 1 is dying
                Log.e("MyToolbar", "OUT OF BOUNDS: index=" + index + " size=" + (beatView != null ? beatView.size() : 0));
                return;
            }

            if (metronomeLayout.getVisibility() != View.VISIBLE) {
                metronomeLayout.setVisibility(View.VISIBLE);
            }

            View light = beatView.get(index);
            if (light != null) {
                light.animate().cancel();
                light.setAlpha(1.0f);
                light.setBackgroundColor(colorOn);

                // Calculate gap so beats don't bleed together
                long adjustedDuration = Math.max(10, flashDuration - 20);

                light.animate()
                        .alpha(0f)
                        .setDuration(10)
                        .setStartDelay(adjustedDuration)
                        .start();
            }
        });
    }

    public void changeTheme() {
        Log.d(TAG,"changeTheme() called");
        this.setTitleTextColor(mainActivityInterface.getPalette().textColor);
        this.setSubtitleTextColor(mainActivityInterface.getPalette().textColor);
        this.setBackgroundColor(mainActivityInterface.getPalette().primary);

        // Safely style any active ActionMenuView children inside the toolbar
        this.post(() -> {
            for (int i = 0; i < this.getChildCount(); i++) {
                View child = this.getChildAt(i);
                if (child instanceof androidx.appcompat.widget.ActionMenuView) {
                    // ActionMenuView hosts the overflow menu items
                    child.setBackgroundColor(Color.TRANSPARENT); // Keep container clean
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setElevation(0);
        }
        title.setTextColor(mainActivityInterface.getPalette().textColor);
        author.setTextColor(mainActivityInterface.getPalette().textColor);
        key.setTextColor(mainActivityInterface.getPalette().textColor);
        capo.setTextColor(mainActivityInterface.getPalette().textColor);
        tempo.setTextColor(mainActivityInterface.getPalette().textColor);
        clock.setTextColor(mainActivityInterface.getPalette().textColor);
        setIcon.setColorFilter(mainActivityInterface.getPalette().textColor);

        mainActivityInterface.refreshToolbarMenu();
    }

    public void showCustomOverflowPopup(View anchorView, List<PopupMenuItem> items, OnItemClickListener listener) {
        if (c == null || mainActivityInterface == null || items == null || items.isEmpty() || anchorView == null) return;

        float density = c.getResources().getDisplayMetrics().density;
        float scaledDensity = c.getResources().getDisplayMetrics().scaledDensity;

        // 100dp min width is much more natural for short menu labels on phones; adjust to taste
        int minWidthPx = Math.round(100 * density);
        int maxWidthPx = Math.round(260 * density);
        int padHorizontalPx = Math.round(16 * density);
        int padVerticalPx = Math.round(10 * density);
        int edgeMarginPx = Math.round(8 * density);
        float radiusPx = 8 * density;
        float textSizeSp = 15.0f;

        android.widget.ListPopupWindow popupWindow = new android.widget.ListPopupWindow(c);
        popupWindow.setAnchorView(anchorView);
        popupWindow.setModal(true);
        popupWindow.setWidth(android.widget.ListPopupWindow.WRAP_CONTENT);
        popupWindow.setVerticalOffset(Math.round(4 * density));

        int secondaryColor = mainActivityInterface.getPalette().secondary;
        int textColor = mainActivityInterface.getPalette().textColor;

        android.graphics.drawable.GradientDrawable roundedBackground = new android.graphics.drawable.GradientDrawable();
        roundedBackground.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        roundedBackground.setColor(secondaryColor);
        roundedBackground.setCornerRadius(radiusPx);
        popupWindow.setBackgroundDrawable(roundedBackground);

        android.widget.ArrayAdapter<PopupMenuItem> adapter = new android.widget.ArrayAdapter<PopupMenuItem>(
                c,
                0,
                items
        ) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull android.view.ViewGroup parent) {
                android.widget.TextView view = (convertView instanceof android.widget.TextView)
                        ? (android.widget.TextView) convertView
                        : new android.widget.TextView(c);

                PopupMenuItem item = getItem(position);
                if (item != null) {
                    view.setText(item.title);
                }
                view.setTextColor(textColor);
                view.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, textSizeSp);
                view.setSingleLine(true);
                view.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                view.setPadding(padHorizontalPx, padVerticalPx, padHorizontalPx, padVerticalPx);
                view.setMinWidth(minWidthPx);

                return view;
            }
        };

        popupWindow.setAdapter(adapter);

        int estimatedWidth = estimatePopupWidthPx(items, textSizeSp * scaledDensity, padHorizontalPx, minWidthPx, maxWidthPx);
        popupWindow.setWidth(estimatedWidth);
        popupWindow.setHorizontalOffset(computeRightAlignedOffset(anchorView, estimatedWidth, edgeMarginPx));

        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            PopupMenuItem clickedItem = items.get(position);
            if (listener != null && clickedItem != null) {
                listener.onItemClick(clickedItem.id);
            }
            popupWindow.dismiss();
        });

        popupWindow.show();

        if (popupWindow.getListView() != null) {
            android.widget.ListView listView = popupWindow.getListView();
            listView.post(() -> {
                if (!popupWindow.isShowing() || popupWindow.getListView() == null) return;

                int measuredWidth = popupWindow.getListView().getWidth();
                int clampedWidth = Math.max(minWidthPx, Math.min(maxWidthPx, measuredWidth));

                if (Math.abs(clampedWidth - estimatedWidth) > Math.round(2 * density)) {
                    popupWindow.setWidth(clampedWidth);
                }
                popupWindow.setHorizontalOffset(computeRightAlignedOffset(anchorView, clampedWidth, edgeMarginPx));
                popupWindow.show();
            });
        }
    }

    private int computeRightAlignedOffset(View anchorView, int popupWidthPx, int edgeMarginPx) {
        int[] anchorLoc = new int[2];
        anchorView.getLocationOnScreen(anchorLoc);

        int screenWidth = anchorView.getResources().getDisplayMetrics().widthPixels;
        // Desired left edge so the popup's right edge aligns with the anchor's right edge
        int desiredLeft = (anchorLoc[0] + anchorView.getWidth()) - popupWidthPx;
        // Clamp so it never clips past the right or left edge of the screen
        int maxAllowedLeft = screenWidth - popupWidthPx - edgeMarginPx;
        int clampedLeft = Math.max(edgeMarginPx, Math.min(desiredLeft, maxAllowedLeft));

        return clampedLeft - anchorLoc[0];
    }

    private int estimatePopupWidthPx(List<PopupMenuItem> items, float textSizePx, int padHorizontalPx,
                                     int minWidthPx, int maxWidthPx) {
        android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(textSizePx);

        int maxTextWidth = 0;
        for (PopupMenuItem item : items) {
            if (item != null && item.title != null) {
                int textWidth = (int) Math.ceil(paint.measureText(item.title));
                if (textWidth > maxTextWidth) {
                    maxTextWidth = textWidth;
                }
            }
        }

        // Small buffer (~8dp worth) for ListView / popup window internal insets
        int insetBuffer = Math.round(padHorizontalPx * 0.5f);
        int totalWidth = maxTextWidth + (padHorizontalPx * 2) + insetBuffer;
        return Math.max(minWidthPx, Math.min(maxWidthPx, totalWidth));
    }

    public interface OnItemClickListener {
        void onItemClick(int itemId);
    }

    public void setupCustomOverflowClickListener(Runnable onOverflowClicked) {
        this.post(() -> {
            View overflowButton = findOverflowButton(this);
            if (overflowButton != null) {
                // Override the default click behavior
                overflowButton.setOnClickListener(v -> {
                    onOverflowClicked.run();
                });
            }
        });
    }

    private View findOverflowButton(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof androidx.appcompat.widget.ActionMenuView) {
                ViewGroup actionMenuView = (ViewGroup) child;
                for (int j = 0; j < actionMenuView.getChildCount(); j++) {
                    View actionChild = actionMenuView.getChildAt(j);
                    // The overflow button is typically an ImageView inside the ActionMenuView
                    if (actionChild instanceof ImageView || actionChild.getClass().getSimpleName().contains("OverflowMenuButton")) {
                        return actionChild;
                    }
                }
            } else if (child instanceof ViewGroup) {
                View found = findOverflowButton((ViewGroup) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
