package com.garethevans.church.opensongtablet.screensetup;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.util.Locale;

public class AppActionBar {

    private final ActionBar actionBar;
    private final TextView title;
    private final TextView author;
    private final TextView key;
    private final TextView capo;
    private final ImageView batteryDial;
    private final TextView batteryText;
    private final TextView clock;
    private final BatteryStatus batteryStatus;
    private final Handler delayactionBarHide;
    private final Runnable hideActionBarRunnable;

    private boolean hideActionBar;
    private boolean performanceMode;

    public AppActionBar(ActionBar actionBar, BatteryStatus batteryStatus, TextView title, TextView author, TextView key, TextView capo, ImageView batteryDial,
                        TextView batteryText, TextView clock, boolean hideActionBar) {
        if (batteryStatus==null) {
            this.batteryStatus = new BatteryStatus();
        } else {
            this.batteryStatus = batteryStatus;
        }
        this.actionBar = actionBar;
        this.title = title;
        this.author = author;
        this.key = key;
        this.capo = capo;
        this.batteryDial = batteryDial;
        this.batteryText = batteryText;
        this.clock = clock;
        this.hideActionBar = hideActionBar;
        delayactionBarHide = new Handler();
        hideActionBarRunnable = () -> {
            if (actionBar != null && actionBar.isShowing()) {
                Log.d("AppActionBar","hide actionBar");
                actionBar.hide();
            }
        };
    }

    public void setHideActionBar(boolean hideActionBar) {
        this.hideActionBar = hideActionBar;
    }
    public void setActionBar(Context c, Preferences preferences, Song song, String newtitle) {
        if (song != null) {
            // We are in the Performance/Stage mode
            //showActionBar(false);
            float mainsize = preferences.getMyPreferenceFloat(c,"songTitleSize",13.0f);

            if (title != null && song.getTitle() != null) {
                title.setTextSize(mainsize);
                title.setText(song.getTitle());
            }
            if (author != null && song.getAuthor() != null && !song.getAuthor().isEmpty()) {
                author.setTextSize(preferences.getMyPreferenceFloat(c,"songAuthorSize",11.0f));
                author.setText(song.getAuthor());
                hideView(author, false);
            } else {
                hideView(author, true);
            }
            if (key != null && song.getKey() != null && !song.getKey().isEmpty()) {
                String k = " (" + song.getKey() + ")";
                key.setTextSize(mainsize);
                capo.setTextSize(mainsize);
                key.setText(k);
                hideView(key, false);
            } else {
                hideView(key, true);
            }
        } else if (newtitle !=null ){
            // We are in a different fragment, so don't hide the song info stuff
            actionBar.show();
            if (title != null) {
                title.setTextSize(22.0f);
                title.setText(newtitle);
                hideView(author, true);
                hideView(key, true);
            }
        }
    }

    public void setActionBarCapo(TextView capo, String string) {
        capo.setText(string);
    }

    public void updateActionBarSettings(Context c, Locale locale, Preferences preferences, String prefName, int intval, float floatval, boolean isvisible) {
        switch (prefName) {
            case "batteryDialOn":
                hideView(batteryDial,!isvisible);
                break;
            case "batteryDialThickness":
                batteryStatus.setBatteryImage(c,batteryDial,actionBar.getHeight(),(int) (batteryStatus.getBatteryStatus(c) * 100.0f),intval);
                break;
            case "batteryTextOn":
                hideView(batteryText,!isvisible);
                break;
            case "batteryTextSize":
                batteryText.setTextSize(floatval);
                break;
            case "clockOn":
                hideView(clock,!isvisible);
                break;
            case "clock24hFormat":
                batteryStatus.updateClock(locale,clock,preferences.getMyPreferenceFloat(c,"clockTextSize",9.0f),
                        clock.getVisibility()==View.VISIBLE,isvisible);
                break;
            case "clockTextSize":
                clock.setTextSize(floatval);
                break;
            case "songTitleSize":
                title.setTextSize(floatval);
                key.setTextSize(floatval);
                capo.setTextSize(floatval);
                break;
            case "songAuthorSize":
                author.setTextSize(floatval);
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
                        delayactionBarHide.postDelayed(hideActionBarRunnable, 3000);
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
    public void showActionBar() {
        // Show the ActionBar based on the user preferences
        // If we are in performance mode (boolean set when opening/closing PerformanceFragment)
        // The we can autohide if the user preferences state that's what is wanted
        // If we are not in performance mode, we don't set a runnable to authide them
        try {
            delayactionBarHide.removeCallbacks(hideActionBarRunnable);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (actionBar != null) {
            Log.d("AppActionBar","showActionBar");
            actionBar.show();
        }

        if (hideActionBar && performanceMode) {
            try {
                Log.d("AppActionBar","set delayed hide");
                delayactionBarHide.postDelayed(hideActionBarRunnable, 3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeCallBacks() {
        delayactionBarHide.removeCallbacks(hideActionBarRunnable);
    }

    public void overlayMode() {
        if (hideActionBar && performanceMode) {
            // Change the top padding of the view underneath
        }
    }
}
