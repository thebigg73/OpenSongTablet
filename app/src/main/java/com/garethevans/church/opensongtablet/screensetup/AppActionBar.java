package com.garethevans.church.opensongtablet.screensetup;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.SongDetailsBottomSheet;

public class AppActionBar {

    // This holds references to the items in the ActionBar (except the battery)
    // Battery changes get sent via the mainactivityInterface
    private final Activity activity;
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Toolbar toolbar;
    private final ActionBar actionBar;
    private final TextView title;
    private final TextView author;
    private final TextView key;
    private final TextView capo;
    private final TextClock clock;
    private final ImageView setIcon;
    private final ImageView webHelp;
    private final Handler delayactionBarHide;
    private final Runnable hideActionBarRunnable;
    private final int autoHideTime = 1200;
    private float clockTextSize;
    private boolean clock24hFormat, clockOn, hideActionBar, clockSeconds;

    private boolean performanceMode;

    public AppActionBar(Activity activity, Context c, ActionBar actionBar, Toolbar toolbar, ImageView setIcon, TextView title, TextView author,
                        TextView key, TextView capo, TextClock clock, ImageView webHelp) {
        this.activity = activity;
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.actionBar = actionBar;
        this.toolbar = toolbar;
        this.title = title;
        this.author = author;
        this.key = key;
        this.capo = capo;
        this.clock = clock;
        this.setIcon = setIcon;
        this.webHelp = webHelp;
        delayactionBarHide = new Handler();
        hideActionBarRunnable = () -> {
            if (actionBar != null && actionBar.isShowing()) {
                actionBar.hide();
            }
        };

        updateActionBarPrefs();
    }

    private void updateActionBarPrefs() {
        clockTextSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("clockTextSize",9.0f);
        clock24hFormat = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clock24hFormat",true);
        clockOn = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockOn",true);
        clockSeconds = mainActivityInterface.getPreferences().getMyPreferenceBoolean("clockSeconds",false);
        hideActionBar = mainActivityInterface.getPreferences().getMyPreferenceBoolean("hideActionBar",false);
        updateClock();
    }

    public void translateAwayActionBar(boolean moveAway) {
        if (moveAway) {
            toolbar.setTranslationY(-200);
            toolbar.setVisibility(View.GONE);
            justShowOrHide(false);
        } else {
            toolbar.setTranslationY(0);
            toolbar.setVisibility(View.VISIBLE);
            justShowOrHide(true);
        }
    }
    public void setHideActionBar(boolean hideActionBar) {
        this.hideActionBar = hideActionBar;
    }
    public boolean getHideActionBar() {
        return hideActionBar;
    }
    public void setActionBar(String newtitle) {
        // By default hide the webHelp (can be shown later)
        webHelp.setVisibility(View.GONE);

        if (newtitle == null) {
            // We are in the Performance/Stage mode
            float mainsize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songTitleSize",13.0f);

            // If we are in a set, show the icon
            int positionInSet = mainActivityInterface.getSetActions().indexSongInSet(mainActivityInterface.getSong());
            if (positionInSet>-1) {
                setIcon.setVisibility(View.VISIBLE);
                mainActivityInterface.getCurrentSet().setIndexSongInSet(positionInSet);
            } else {
                setIcon.setVisibility(View.GONE);
                mainActivityInterface.getCurrentSet().setIndexSongInSet(-1);
            }

            if (title != null && mainActivityInterface.getSong().getTitle() != null) {
                title.setTextSize(mainsize);
                String text = mainActivityInterface.getSong().getTitle();
                if (mainActivityInterface.getSong().getFolder().startsWith("*")) {
                    text = "*" + text;
                }
                title.setText(text);
            }
            if (author != null && mainActivityInterface.getSong().getAuthor() != null &&
                    !mainActivityInterface.getSong().getAuthor().isEmpty()) {
                author.setTextSize(mainActivityInterface.getPreferences().getMyPreferenceFloat("songAuthorSize",11.0f));
                author.setText(mainActivityInterface.getSong().getAuthor());
                hideView(author, false);
            } else {
                hideView(author, true);
            }
            if (key != null && mainActivityInterface.getSong().getKey() != null &&
                    !mainActivityInterface.getSong().getKey().isEmpty()) {
                String k = " (" + mainActivityInterface.getSong().getKey() + ")";
                key.setTextSize(mainsize);
                capo.setTextSize(mainsize);
                key.setText(k);
                hideView(key, false);
            } else {
                hideView(key, true);
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

        } else {
            // We are in a different fragment, so hide the song info stuff
            setIcon.setVisibility(View.GONE);
            actionBar.show();
            if (title != null) {
                title.setTextSize(18.0f);
                title.setText(newtitle);
                hideView(author, true);
                hideView(key, true);
            }
        }
    }
    public void updateToolbarHelp(String webAddress) {
        // This allows a help button to be shown in the action bar
        // This links to the specific page in the user manul (if webAddress isn't null)
        if (webAddress==null || webAddress.isEmpty()) {
            webHelp.setVisibility(View.GONE);
        } else {
            webHelp.setVisibility(View.VISIBLE);
            webHelp.setOnClickListener(v->mainActivityInterface.openDocument(webAddress));
            // For the first run, show the showcase as well
            mainActivityInterface.getShowCase().singleShowCase(activity, webHelp,null,
                    c.getString(R.string.help),false,"webHelp");
        }
    }

    private void openDetails() {
        if (!mainActivityInterface.getSong().getTitle().equals("Welcome to OpenSongApp")) {
            SongDetailsBottomSheet songDetailsBottomSheet = new SongDetailsBottomSheet();
            songDetailsBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "songDetailsBottomSheet");
        }
    }
    private void editSong() {
        if (!mainActivityInterface.getSong().getTitle().equals("Welcome to OpenSongApp")) {
            mainActivityInterface.navigateToFragment("opensongapp://settings/edit", 0);
        }
    }

    public void setActionBarCapo(TextView capo, String string) {
        capo.setText(string);
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
                hideView(clock,!isvisible);
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
        }
    }

    private void hideView(View v, boolean hide) {
        if (hide) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }

    // Action bar stuff
    public void toggleActionBar(boolean wasScrolling, boolean scrollButton,
                                boolean menusActive) {
        try {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (actionBar != null) {
            if (wasScrolling || scrollButton) {
                if (hideActionBar && !menusActive) {
                    actionBar.hide();
                }
            } else if (!menusActive) {
                if (actionBar.isShowing() && hideActionBar) {
                    delayactionBarHide.postDelayed(hideActionBarRunnable, 500);
                } else {
                    actionBar.show();
                    // Set a runnable to hide it after 3 seconds
                    if (hideActionBar) {
                        delayactionBarHide.postDelayed(hideActionBarRunnable, autoHideTime);
                    }
                }
            }
        }
    }


    // Set when entering/exiting performance mode
    public void setPerformanceMode(boolean inPerformanceMode) {
        performanceMode = inPerformanceMode;
    }

    // Show/hide the actionbar
    public void showActionBar(boolean menuOpen) {
        // Show the ActionBar based on the user preferences
        // If we are in performance mode (boolean set when opening/closing PerformanceFragment)
        // The we can autohide if the user preferences state that's what is wanted
        // If we are not in performance mode, we don't set a runnable to autohide them
        try {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (actionBar != null) {
            actionBar.show();
        }

        if (hideActionBar && performanceMode && !menuOpen) {
            try {
                delayactionBarHide.postDelayed(hideActionBarRunnable, autoHideTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void justShowOrHide(boolean show) {
        if (actionBar!=null && show) {
            actionBar.show();
        } else if (actionBar!=null) {
            actionBar.hide();
        }
    }

    public void removeCallBacks() {
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
    }

    // Flash on/off for metronome
    public void doFlash(int colorBar) {
        actionBar.setBackgroundDrawable(new ColorDrawable(colorBar));
    }

    // Get the actionbar height - fakes a height of 0 if autohiding
    public int getActionBarHeight() {
        if (hideActionBar && performanceMode) {
            return 0;
        } else {
            return actionBar.getHeight();
        }
    }

    public void updateClock() {
        mainActivityInterface.getTimeTools().setFormat(clock,clockTextSize,
                clockOn, clock24hFormat, clockSeconds);
    }
}
